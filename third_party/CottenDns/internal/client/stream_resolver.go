package client

import (
	"math"
	"time"

	"cottendns-go/internal/arq"
	Enums "cottendns-go/internal/enums"
)

// adaptiveDuplicationCeiling caps the duplication count chosen by tier-1
// adaptive duplication; beyond this, loss is better handled by Reed-Solomon FEC.
const adaptiveDuplicationCeiling = 8

// directionalDuplicationCounts returns the effective upload/download and
// upload-setup/download-setup duplication counts. After config normalization
// all four fields are guaranteed to be >= 1, so we just read them directly and
// apply a safety floor of 1 for callers that construct the config in tests
// without going through validation.
func (c *Client) directionalDuplicationCounts() (uploadData, downloadData, uploadSetup, downloadSetup int) {
	uploadData = c.cfg.UploadPacketDuplicationCount
	if uploadData < 1 {
		uploadData = 1
	}
	downloadData = c.cfg.DownloadPacketDuplicationCount
	if downloadData < 1 {
		downloadData = 1
	}
	uploadSetup = c.cfg.UploadSetupPacketDuplicationCount
	if uploadSetup < uploadData {
		uploadSetup = uploadData
	}
	downloadSetup = c.cfg.DownloadSetupPacketDuplicationCount
	if downloadSetup < downloadData {
		downloadSetup = downloadData
	}
	return uploadData, downloadData, uploadSetup, downloadSetup
}

func (c *Client) runtimePacketDuplicationCount(packetType uint8) int {
	if c == nil {
		return 1
	}

	uploadData, downloadData, uploadSetup, downloadSetup := c.directionalDuplicationCounts()

	var count int
	switch packetType {
	case Enums.PACKET_STREAM_DATA, Enums.PACKET_STREAM_RESEND:
		count = uploadData
	case Enums.PACKET_STREAM_DATA_ACK, Enums.PACKET_STREAM_DATA_NACK:
		count = downloadData
	case Enums.PACKET_STREAM_SYN, Enums.PACKET_SOCKS5_SYN:
		count = uploadSetup
	case Enums.PACKET_PACKED_CONTROL_BLOCKS,
		Enums.PACKET_STREAM_CLOSE_READ,
		Enums.PACKET_STREAM_CLOSE_WRITE:
		count = downloadSetup
	case Enums.PACKET_PING:
		// Ping reliability tracks the stronger of the two directional counts
		// since it probes both legs of the path, but is capped at 2 to avoid
		// spamming probes when duplication is turned up high.
		count = uploadData
		if downloadData > count {
			count = downloadData
		}
		return min(count, 2)
	default:
		count = uploadData
	}

	if count < 1 {
		count = 1
	}
	if c.cfg.AdaptiveDuplication {
		count = c.adaptiveDuplicationCount(count)
	}
	// Recent FEC shards mean the server is already spending redundancy on the
	// download leg. Keep ACK/NACK polling diverse, but do not multiply both FEC
	// and DNS copies at full strength at the same time.
	if (packetType == Enums.PACKET_STREAM_DATA_ACK || packetType == Enums.PACKET_STREAM_DATA_NACK) &&
		c.fecRecentlyActive() && count > 2 {
		count = 2
	}
	return count
}

func (c *Client) fecRecentlyActive() bool {
	if c == nil {
		return false
	}
	last := c.lastFECReceived.Load()
	return last != 0 && c.now().Sub(time.Unix(0, last)) <= 10*time.Second
}

// adaptiveDuplicationCount raises base toward the number of copies needed to hit
// the configured target delivery probability given the currently measured loss
// (tier 1). With per-packet loss L and target delivery P, the copies C satisfy
// 1 - L^C >= P, i.e. C = ceil(ln(1-P)/ln(L)). The result is clamped to
// [base, adaptiveDuplicationCeiling]; loss high enough to demand more than the
// ceiling is the regime where Reed-Solomon FEC (tier 2) takes over.
func (c *Client) adaptiveDuplicationCount(base int) int {
	if c == nil || c.balancer == nil {
		return base
	}
	// Use the larger of the DNS-reachability loss and the real tunnel loss
	// (upload retransmit rate); the latter actually reflects data loss, which the
	// DNS sent/acked ratio (≈0 on reachable resolvers) does not.
	lossPM := c.balancer.AggregateLossPerMille()
	if tunnelPM := c.tunnelLossPerMille(); tunnelPM > lossPM {
		lossPM = tunnelPM
	}
	if lossPM == 0 {
		return base
	}
	return duplicationForLoss(base, float64(lossPM)/1000.0, c.cfg.AdaptiveDuplicationTargetDelivery)
}

// duplicationForLoss returns the copy count needed to reach target delivery
// probability under per-packet loss lossFrac, clamped to [base, ceiling]. See
// adaptiveDuplicationCount for the model. Pure (no receiver) so it is unit
// testable in isolation.
func duplicationForLoss(base int, lossFrac, target float64) int {
	if lossFrac <= 0 || lossFrac >= 1 {
		return base
	}
	if target <= 0 || target >= 1 {
		target = 0.95
	}
	need := int(math.Ceil(math.Log(1-target) / math.Log(lossFrac)))
	if need < base {
		need = base
	}
	if need > adaptiveDuplicationCeiling {
		need = adaptiveDuplicationCeiling
	}
	return need
}

func (c *Client) selectTargetConnectionsForPacket(packetType uint8, streamID uint16) ([]Connection, error) {
	targetCount := c.runtimePacketDuplicationCount(packetType)

	if c == nil || c.balancer == nil || streamID == 0 || c.balancer.ValidCount() <= 0 {
		return c.selectUniqueRuntimeConnections(targetCount)
	}

	if packetType != Enums.PACKET_STREAM_DATA && packetType != Enums.PACKET_STREAM_RESEND {
		return c.selectUniqueRuntimeConnections(targetCount)
	}

	stream, ok := c.getStream(streamID)
	if !ok || stream == nil {
		return c.selectUniqueRuntimeConnections(targetCount)
	}

	var (
		preferred Connection
		found     bool
	)

	if packetType == Enums.PACKET_STREAM_RESEND {
		preferred, found = c.selectStreamPreferredConnectionForResend(stream)
	} else {
		preferred, found = c.ensureStreamPreferredConnection(stream)
	}

	if !found {
		return c.selectUniqueRuntimeConnections(targetCount)
	}

	if targetCount <= 1 {
		return []Connection{preferred}, nil
	}

	if cached, ok := c.getCachedStreamConnectionPlan(stream, preferred.Key, targetCount); ok {
		return cached, nil
	}

	selected := make([]Connection, 0, targetCount)
	selected = append(selected, preferred)

	// A6: when enabled, fill the remaining duplicate slots preferring connections
	// on tunnel domains not yet represented, so duplicates take independent paths.
	if c.dupPreferDistinctDomains {
		selected = c.appendDomainDiverseDuplicates(selected, targetCount)
		if len(selected) == 0 {
			return nil, ErrNoValidConnections
		}
		c.cacheStreamConnectionPlan(stream, preferred.Key, targetCount, selected)
		return selected, nil
	}

	// Pull a slightly larger candidate pool than needed and move paced (throttling)
	// resolvers to the back, so the spread prefers resolvers with headroom but
	// still falls back to paced ones rather than under-filling.
	for _, connection := range c.orderByPacing(c.balancer.GetUniqueConnections(targetCount * 2)) {
		if !connection.IsValid || connection.Key == "" {
			continue
		}
		dup := false
		for _, s := range selected {
			if s.Key == connection.Key {
				dup = true
				break
			}
		}
		if dup {
			continue
		}
		selected = append(selected, connection)
		if len(selected) >= targetCount {
			return selected, nil
		}
	}

	if len(selected) == 0 {
		return nil, ErrNoValidConnections
	}

	c.cacheStreamConnectionPlan(stream, preferred.Key, targetCount, selected)
	return selected, nil
}

// appendDomainDiverseDuplicates fills selected up to targetCount, biasing toward
// connections whose tunnel domain is not yet represented (A6). It pulls the full
// valid candidate pool (in balancer-strategy order) and makes two passes: first
// adding only connections that introduce a new domain, then filling any leftover
// slots with remaining unique connections. This never under-fills relative to
// the resolver-only selection — it only reorders preference toward distinct
// domains. Connections are deduplicated by Key, as before.
func (c *Client) appendDomainDiverseDuplicates(selected []Connection, targetCount int) []Connection {
	if len(selected) >= targetCount || c.balancer == nil {
		return selected
	}

	poolSize := c.balancer.ValidCount()
	if poolSize < targetCount {
		poolSize = targetCount
	}
	pool := c.balancer.GetUniqueConnections(poolSize)

	seenKey := make(map[string]struct{}, targetCount)
	seenDomain := make(map[string]struct{}, targetCount)
	for _, s := range selected {
		seenKey[s.Key] = struct{}{}
		seenDomain[s.Domain] = struct{}{}
	}

	fill := func(requireNewDomain bool) {
		for _, conn := range pool {
			if len(selected) >= targetCount {
				return
			}
			if !conn.IsValid || conn.Key == "" {
				continue
			}
			if _, dup := seenKey[conn.Key]; dup {
				continue
			}
			if requireNewDomain {
				if _, seen := seenDomain[conn.Domain]; seen {
					continue
				}
			}
			selected = append(selected, conn)
			seenKey[conn.Key] = struct{}{}
			seenDomain[conn.Domain] = struct{}{}
		}
	}

	fill(true)  // prefer connections on new domains
	fill(false) // then top up with any remaining unique connections
	return selected
}

func (c *Client) selectUniqueRuntimeConnections(requiredCount int) ([]Connection, error) {
	if c == nil {
		return nil, ErrNoValidConnections
	}

	if c.balancer == nil {
		return nil, ErrNoValidConnections
	}

	connections := c.balancer.GetUniqueConnections(requiredCount * 2)
	if len(connections) == 0 {
		return nil, ErrNoValidConnections
	}
	// Prefer resolvers with headroom (paced ones sink to the back).
	connections = c.orderByPacing(connections)

	// Filter out runtime-disabled resolvers so control packets
	// (SYN, CLOSE, RST, ACK, etc.) are not sent to known-bad resolvers.
	filtered := connections[:0]
	for _, conn := range connections {
		if !c.isRuntimeDisabledResolver(conn.Key) {
			filtered = append(filtered, conn)
		}
	}
	if len(filtered) > requiredCount {
		filtered = filtered[:requiredCount]
	}
	if len(filtered) == 0 {
		return nil, ErrNoValidConnections
	}

	return filtered, nil
}

func (c *Client) selectStreamPreferredConnectionForResend(stream *Stream_client) (Connection, bool) {
	if c == nil || stream == nil {
		return Connection{}, false
	}

	stream.resolverMu.Lock()
	stream.ResolverResendStreak++
	streak := stream.ResolverResendStreak
	stream.resolverMu.Unlock()

	if streak >= c.streamResolverFailoverResendThreshold {
		return c.maybeFailoverStreamPreferredConnection(stream)
	}
	return c.ensureStreamPreferredConnection(stream)
}

func (c *Client) getValidStreamPreferredConnection(stream *Stream_client) (Connection, bool) {
	if c == nil || stream == nil {
		return Connection{}, false
	}
	stream.resolverMu.Lock()
	preferredKey := stream.PreferredServerKey
	stream.resolverMu.Unlock()
	if preferredKey == "" {
		return Connection{}, false
	}
	if c.isRuntimeDisabledResolver(preferredKey) {
		return Connection{}, false
	}
	connection, ok := c.GetConnectionByKey(preferredKey)
	if !ok || !connection.IsValid {
		return Connection{}, false
	}
	return connection, true
}

func (c *Client) selectAlternateStreamConnection(excludeKey string) (Connection, bool) {
	if c == nil || c.balancer == nil {
		return Connection{}, false
	}

	now := c.now()
	if excludeKey != "" {
		if replacement, ok := c.balancer.GetBestConnectionExcluding(excludeKey); ok &&
			!c.isRuntimeDisabledResolver(replacement.Key) && !c.pacer.paced(replacement.Key, now) {
			return replacement, true
		}
	}

	var pacedFallback Connection
	var havePacedFallback bool
	for _, connection := range c.balancer.GetAllValidConnections() {
		if !connection.IsValid || connection.Key == "" {
			continue
		}
		if c.isRuntimeDisabledResolver(connection.Key) {
			continue
		}
		if excludeKey != "" && connection.Key == excludeKey {
			continue
		}
		// Prefer a resolver with headroom; remember a paced one only as a fallback
		// so we never strand the stream when everything is briefly cooling down.
		if c.pacer.paced(connection.Key, now) {
			if !havePacedFallback {
				pacedFallback = connection
				havePacedFallback = true
			}
			continue
		}
		return connection, true
	}

	if havePacedFallback {
		return pacedFallback, true
	}
	return Connection{}, false
}

func (c *Client) assignStreamPreferredConnection(stream *Stream_client, connection Connection, markFailover bool) (Connection, bool) {
	if c == nil || stream == nil {
		return Connection{}, false
	}
	if !connection.IsValid || connection.Key == "" {
		stream.resolverMu.Lock()
		stream.PreferredServerKey = ""
		stream.resolverMu.Unlock()
		return Connection{}, false
	}

	stream.resolverMu.Lock()
	stream.PreferredServerKey = connection.Key
	stream.ResolverResendStreak = 0
	stream.CachedResolverPlan = nil
	stream.CachedResolverPlanFor = ""
	stream.CachedResolverPlanSize = 0
	stream.CachedResolverVersion = 0
	if markFailover {
		stream.LastResolverFailoverAt = time.Now()
	}
	stream.resolverMu.Unlock()
	return connection, true
}

func (c *Client) ensureStreamPreferredConnection(stream *Stream_client) (Connection, bool) {
	if preferred, ok := c.getValidStreamPreferredConnection(stream); ok {
		// A stream pins one preferred resolver for ordered delivery, which bypasses
		// the pacer's selection-time redistribution. If that resolver is now in a
		// throttle cooldown, move the stream to one with headroom so a rate-limited
		// resolver cannot pin a stream and hang its transfers. Bounded by the
		// failover cooldown so this can't churn the preferred every packet.
		if c.pacer.paced(preferred.Key, c.now()) && !c.streamFailedOverRecently(stream) {
			if alt, ok := c.selectAlternateStreamConnection(preferred.Key); ok && alt.Key != preferred.Key {
				return c.assignStreamPreferredConnection(stream, alt, true)
			}
		}
		return preferred, true
	}

	stream.resolverMu.Lock()
	excludeKey := stream.PreferredServerKey
	stream.resolverMu.Unlock()
	if fallback, ok := c.selectAlternateStreamConnection(excludeKey); ok {
		return c.assignStreamPreferredConnection(stream, fallback, false)
	}
	return Connection{}, false
}

// streamFailedOverRecently reports whether the stream switched its preferred
// resolver within the failover cooldown, used to rate-limit re-selection.
func (c *Client) streamFailedOverRecently(stream *Stream_client) bool {
	if c == nil || stream == nil {
		return false
	}
	stream.resolverMu.Lock()
	last := stream.LastResolverFailoverAt
	stream.resolverMu.Unlock()
	return !last.IsZero() && time.Since(last) < c.streamResolverFailoverCooldown
}

func (c *Client) maybeFailoverStreamPreferredConnection(stream *Stream_client) (Connection, bool) {
	current, ok := c.getValidStreamPreferredConnection(stream)
	if !ok {
		return c.ensureStreamPreferredConnection(stream)
	}

	stream.resolverMu.Lock()
	lastSwitch := stream.LastResolverFailoverAt
	stream.resolverMu.Unlock()
	if !lastSwitch.IsZero() && time.Since(lastSwitch) < c.streamResolverFailoverCooldown {
		return current, true
	}

	replacement, ok := c.selectAlternateStreamConnection(current.Key)
	if !ok {
		return current, true
	}
	return c.assignStreamPreferredConnection(stream, replacement, true)
}

func (c *Client) noteStreamProgress(streamID uint16) {
	if c == nil || streamID == 0 {
		return
	}
	stream, ok := c.getStream(streamID)
	if !ok || stream == nil {
		return
	}
	stream.resolverMu.Lock()
	stream.ResolverResendStreak = 0
	stream.resolverMu.Unlock()
}

func (c *Client) getCachedStreamConnectionPlan(stream *Stream_client, preferredKey string, targetCount int) ([]Connection, bool) {
	if c == nil || stream == nil || c.balancer == nil || preferredKey == "" || targetCount <= 1 {
		return nil, false
	}

	version := c.balancer.SnapshotVersion()
	stream.resolverMu.Lock()
	defer stream.resolverMu.Unlock()

	if stream.CachedResolverVersion != version ||
		stream.CachedResolverPlanFor != preferredKey ||
		stream.CachedResolverPlanSize != targetCount ||
		len(stream.CachedResolverPlan) == 0 {
		return nil, false
	}

	return stream.CachedResolverPlan, true
}

func (c *Client) cacheStreamConnectionPlan(stream *Stream_client, preferredKey string, targetCount int, selected []Connection) {
	if c == nil || stream == nil || c.balancer == nil || preferredKey == "" || targetCount <= 1 || len(selected) == 0 {
		return
	}

	cached := make([]Connection, len(selected))
	copy(cached, selected)
	version := c.balancer.SnapshotVersion()

	stream.resolverMu.Lock()
	stream.CachedResolverPlan = cached
	stream.CachedResolverPlanFor = preferredKey
	stream.CachedResolverPlanSize = targetCount
	stream.CachedResolverVersion = version
	stream.resolverMu.Unlock()
}

func (c *Client) shouldTransmitQueuedStreamPacket(stream *Stream_client, item *clientStreamTXPacket) bool {
	if c == nil || stream == nil || item == nil {
		return false
	}

	if item.PacketType != Enums.PACKET_STREAM_DATA && item.PacketType != Enums.PACKET_STREAM_RESEND {
		return true
	}

	arqObj, ok := stream.Stream.(*arq.ARQ)
	if !ok || arqObj == nil {
		return false
	}

	return arqObj.HasPendingSequence(item.SequenceNum)
}

func (c *Client) GetConnectionByKey(key string) (Connection, bool) {
	if c == nil || key == "" {
		return Connection{}, false
	}
	if c.balancer != nil {
		if conn, ok := c.balancer.GetConnectionByKey(key); ok {
			return conn, true
		}
	}
	idx, ok := c.connectionsByKey[key]
	if !ok || idx < 0 || idx >= len(c.connections) {
		return Connection{}, false
	}
	return c.connections[idx], true
}
