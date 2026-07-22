// ==============================================================================
// CottenDNS
// Author: tajirax
// Github: https://github.com/TaJirax/CottenDns
// Year: 2026
// ==============================================================================
// Package client provides the core logic for the CottenDns client.
// This file (session.go) handles session states and initialization requests.
// ==============================================================================
package client

import (
	"bytes"
	"context"
	"encoding/binary"
	"errors"
	"sync"
	"time"

	"cottendns-go/internal/compression"
	Enums "cottendns-go/internal/enums"
	VpnProto "cottendns-go/internal/vpnproto"
)

var (
	ErrSessionInitFailed = errors.New("session init failed")
	ErrSessionInitBusy   = errors.New("session init busy")
)

const (
	sessionInitPayloadSize      = 10
	sessionAcceptPayloadSize    = 8
	sessionBusyPayloadSize      = 4
	sessionCloseBurstMaxTargets = 10
	sessionCloseBurstRounds     = 3
)

func (c *Client) InitializeSession(maxAttempts int) error {
	// Re-derive the adaptive operating point over the surviving resolver pool
	// before (re)establishing the session, so a restart after primary-pool loss
	// promotes backups at a viable lower MTU rather than reusing a stale one.
	c.recomputeMTUOperatingPoint()

	if c.syncedUploadMTU <= 0 || c.syncedDownloadMTU <= 0 {
		return ErrSessionInitFailed
	}

	if maxAttempts < 1 {
		maxAttempts = 1
	}

	for attempt := 0; attempt < maxAttempts; attempt++ {
		if err := c.initializeSessionOnce(); err == nil {
			return nil
		} else if errors.Is(err, ErrNoValidConnections) || errors.Is(err, ErrSessionInitBusy) {
			return err
		}
	}

	return ErrSessionInitFailed
}

// sessionInitRaceCount is how many distinct resolvers a single SESSION_INIT is
// raced across. The server keys sessions by the init signature and reuses within
// SESSION_INIT_REUSE_TTL, so the identical init reaching it via several resolvers
// yields one session (whichever answers first wins) — no duplicate sessions.
// Racing turns a slow serial "try one, wait a full timeout, try the next" connect
// into "ask a few at once, take the first reply", which is a large win on lossy
// networks where any single resolver may be dead.
// Operator-tunable via SESSION_INIT_RACING_COUNT; the default preserves the
// long-standing hardcoded value of 3.
func (c *Client) sessionInitRaceCount() int {
	if c.cfg.SessionInitRacingCount < 1 {
		return 1
	}
	return c.cfg.SessionInitRacingCount
}

func (c *Client) initializeSessionOnce() error {
	conns, initPayload, verifyCode, err := c.nextSessionInitRacers(c.sessionInitRaceCount())
	if err != nil {
		return err
	}
	timeout := c.mtuTestTimeout * 3

	if len(conns) == 1 {
		return c.exchangeSessionInit(conns[0], initPayload, verifyCode, timeout)
	}

	type initResult struct {
		packet VpnProto.Packet
		ok     bool
	}
	// Buffered to len(conns) so stragglers can always send their result and exit
	// even after we have already returned on the first ACCEPT (no goroutine leak).
	results := make(chan initResult, len(conns))
	raceCtx, cancelRace := context.WithCancel(context.Background())
	defer cancelRace()
	for _, conn := range conns {
		go func(conn Connection) {
			query, buildErr := c.buildSessionQuery(conn.Domain, Enums.PACKET_SESSION_INIT, initPayload)
			if buildErr != nil {
				results <- initResult{}
				return
			}
			packet, exErr := c.exchangeDNSOverConnectionContext(raceCtx, conn, query, timeout)
			results <- initResult{packet: packet, ok: exErr == nil}
		}(conn)
	}

	sawBusy := false
	for i := 0; i < len(conns); i++ {
		res := <-results
		if !res.ok {
			continue
		}
		switch {
		case res.packet.PacketType == Enums.PACKET_SESSION_ACCEPT:
			if c.applySessionAccept(res.packet, initPayload, verifyCode) {
				cancelRace()
				return nil
			}
		case c.isSessionBusy(res.packet, verifyCode):
			sawBusy = true
		}
	}
	if sawBusy {
		c.setSessionInitBusyUntil(time.Now().Add(c.cfg.SessionInitBusyRetryInterval()))
		return ErrSessionInitBusy
	}
	return ErrSessionInitFailed
}

// exchangeSessionInit runs a single-resolver SESSION_INIT (also the fallback when
// only one valid resolver exists). Behaviour is identical to the pre-race path.
func (c *Client) exchangeSessionInit(conn Connection, initPayload []byte, verifyCode [4]byte, timeout time.Duration) error {
	query, err := c.buildSessionQuery(conn.Domain, Enums.PACKET_SESSION_INIT, initPayload)
	if err != nil {
		return ErrSessionInitFailed
	}
	packet, err := c.exchangeDNSOverConnection(conn, query, timeout)
	if err != nil {
		return ErrSessionInitFailed
	}
	switch {
	case packet.PacketType == Enums.PACKET_SESSION_ACCEPT:
		if c.applySessionAccept(packet, initPayload, verifyCode) {
			return nil
		}
		return ErrSessionInitFailed
	case c.isSessionBusy(packet, verifyCode):
		c.setSessionInitBusyUntil(time.Now().Add(c.cfg.SessionInitBusyRetryInterval()))
		return ErrSessionInitBusy
	default:
		return ErrSessionInitFailed
	}
}

// applySessionAccept validates a SESSION_ACCEPT against the init verify code and,
// on success, commits the session state and returns true. Invoked only from the
// single init collector goroutine, so the state writes are unsynchronized exactly
// as in the original sequential path.
//
// The accept payload carries the server-assigned session ID at the same width as
// the packet header, so its layout shifts by one byte between the native 2-byte
// format and the legacy 1-byte MasterDNS/StormDNS format:
// [sid(1|2)] [cookie] [compression] [verifyCode(4)].
func (c *Client) applySessionAccept(packet VpnProto.Packet, initPayload []byte, verifyCode [4]byte) bool {
	sidLen := 2
	if VpnProto.LegacySessionID() {
		sidLen = 1
	}
	verifyStart := sidLen + 2
	acceptSize := verifyStart + len(verifyCode)
	if len(packet.Payload) < acceptSize || !bytes.Equal(packet.Payload[verifyStart:verifyStart+len(verifyCode)], verifyCode[:]) {
		return false
	}

	if sidLen == 1 {
		c.sessionID = uint16(packet.Payload[0])
	} else {
		c.sessionID = uint16(packet.Payload[0])<<8 | uint16(packet.Payload[1])
	}
	c.sessionCookie = packet.Payload[sidLen]
	c.responseMode = initPayload[0]
	c.uploadCompression, c.downloadCompression = compression.SplitPair(packet.Payload[sidLen+1])
	// Apply the server's ceilings before the session is announced as ready.
	// Everything below derives from them (the compression decision reads the
	// effective minimum size, and the policy refreshes maxPackedBlocks), and
	// the send path starts reading those values the moment sessionReady is set
	// -- so the policy has to be in place first, not merely stored afterwards.
	c.applyServerClientPolicy(packet.Payload)
	c.sessionReady = true
	c.applySessionCompressionPolicy()
	c.clearSessionInitBusyUntil()
	c.resetSessionInitState()
	c.clearSessionResetPending()
	return true
}

// isSessionBusy reports whether packet is a SESSION_BUSY that matches our init.
func (c *Client) isSessionBusy(packet VpnProto.Packet, verifyCode [4]byte) bool {
	return packet.PacketType == Enums.PACKET_SESSION_BUSY &&
		len(packet.Payload) >= sessionBusyPayloadSize &&
		bytes.Equal(packet.Payload[:sessionBusyPayloadSize], verifyCode[:])
}

func (c *Client) buildSessionInitPayload() ([]byte, bool, [4]byte, error) {
	var verifyCode [4]byte
	randomPart, err := randomBytes(len(verifyCode))
	if err != nil {
		return nil, false, verifyCode, err
	}
	copy(verifyCode[:], randomPart)

	payload := make([]byte, sessionInitPayloadSize)
	if c.cfg.BaseEncodeData {
		payload[0] = mtuProbeBase64Reply
	}
	payload[1] = compression.PackPair(c.uploadCompression, c.downloadCompression)
	binary.BigEndian.PutUint16(payload[2:4], uint16(c.syncedUploadMTU))
	binary.BigEndian.PutUint16(payload[4:6], uint16(c.syncedDownloadMTU))
	copy(payload[6:10], verifyCode[:])
	return payload, payload[0] == mtuProbeBase64Reply, verifyCode, nil
}

func (c *Client) nextSessionInitAttempt() (Connection, []byte, [4]byte, error) {
	var empty [4]byte
	if c == nil {
		return Connection{}, nil, empty, ErrSessionInitFailed
	}

	c.initStateMu.Lock()
	defer c.initStateMu.Unlock()

	// Persistence Check: reuse existing token/payload if already ready
	if !c.sessionInitReady {
		payload, responseBase64, verifyCode, err := c.buildSessionInitPayload()
		if err != nil {
			return Connection{}, nil, empty, err
		}
		c.sessionInitPayload = payload
		c.sessionInitBase64 = responseBase64
		c.sessionInitVerify = verifyCode
		c.sessionInitReady = true
		c.sessionInitCursor = 0
	}

	snap := c.balancer.snapshot.Load()
	if snap == nil || len(snap.valid) == 0 {
		return Connection{}, nil, empty, ErrNoValidConnections
	}

	// Use the cursor to rotate between valid resolvers in a Round-Robin fashion
	validLen := len(snap.valid)
	start := c.sessionInitCursor
	for checked := 0; checked < validLen; checked++ {
		idxInValid := (start + checked) % validLen
		connIdx := snap.valid[idxInValid]

		conn, ok := derefConnection(snap.connections, connIdx)
		if !ok {
			continue
		}

		c.sessionInitCursor = (idxInValid + 1) % validLen
		return conn, c.sessionInitPayload, c.sessionInitVerify, nil
	}

	return Connection{}, nil, empty, ErrNoValidConnections
}

// nextSessionInitRacers returns up to n distinct valid resolvers to race a single
// SESSION_INIT across, all sharing one init payload/verify code. The cursor is
// advanced past every returned resolver so successive attempts rotate onward. It
// is the multi-resolver generalisation of nextSessionInitAttempt; with n==1 it is
// equivalent.
func (c *Client) nextSessionInitRacers(n int) ([]Connection, []byte, [4]byte, error) {
	var empty [4]byte
	if c == nil || n < 1 {
		return nil, nil, empty, ErrSessionInitFailed
	}

	c.initStateMu.Lock()
	defer c.initStateMu.Unlock()

	if !c.sessionInitReady {
		payload, responseBase64, verifyCode, err := c.buildSessionInitPayload()
		if err != nil {
			return nil, nil, empty, err
		}
		c.sessionInitPayload = payload
		c.sessionInitBase64 = responseBase64
		c.sessionInitVerify = verifyCode
		c.sessionInitReady = true
		c.sessionInitCursor = 0
	}

	snap := c.balancer.snapshot.Load()
	if snap == nil || len(snap.valid) == 0 {
		return nil, nil, empty, ErrNoValidConnections
	}

	validLen := len(snap.valid)
	if n > validLen {
		n = validLen
	}
	conns := make([]Connection, 0, n)
	start := c.sessionInitCursor
	for checked := 0; checked < validLen && len(conns) < n; checked++ {
		idxInValid := (start + checked) % validLen
		conn, ok := derefConnection(snap.connections, snap.valid[idxInValid])
		if !ok {
			continue
		}
		conns = append(conns, conn)
		c.sessionInitCursor = (idxInValid + 1) % validLen
	}
	if len(conns) == 0 {
		return nil, nil, empty, ErrNoValidConnections
	}
	return conns, c.sessionInitPayload, c.sessionInitVerify, nil
}

func (c *Client) resetSessionInitState() {
	if c == nil {
		return
	}
	c.initStateMu.Lock()
	c.sessionInitPayload = nil
	c.sessionInitVerify = [4]byte{}
	c.sessionInitBase64 = false
	c.sessionInitReady = false
	c.sessionInitCursor = 0
	c.initStateMu.Unlock()
}

func (c *Client) setSessionInitBusyUntil(deadline time.Time) {
	if c == nil {
		return
	}
	c.sessionInitBusyUnix.Store(deadline.UnixNano())
}

func (c *Client) clearSessionInitBusyUntil() {
	if c == nil {
		return
	}
	c.sessionInitBusyUnix.Store(0)
}

func (c *Client) sessionInitBusyUntil() time.Time {
	if c == nil {
		return time.Time{}
	}
	unixNano := c.sessionInitBusyUnix.Load()
	if unixNano <= 0 {
		return time.Time{}
	}
	return time.Unix(0, unixNano)
}

func (c *Client) buildSessionQuery(domain string, packetType uint8, payload []byte) ([]byte, error) {
	return c.buildTunnelQuery(domain, 0, packetType, payload)
}

func (c *Client) buildTunnelQuery(domain string, sessionID uint16, packetType uint8, payload []byte) ([]byte, error) {
	return c.buildTunnelTXTQueryRaw(domain, VpnProto.BuildOptions{
		SessionID:  sessionID,
		PacketType: packetType,
		Payload:    payload,
	})
}

func (c *Client) clearSessionResetPending() {
	if c != nil {
		c.sessionResetPending.Store(false)
	}
}

func (c *Client) notifySessionCloseBurst(timeout time.Duration) {
	if c == nil || !c.SessionReady() || c.sessionID == 0 {
		return
	}
	if !c.sessionResetPending.CompareAndSwap(false, true) {
		return
	}

	targets := c.selectSessionCloseTargets(sessionCloseBurstMaxTargets)
	if len(targets) == 0 {
		c.sessionResetPending.Store(false)
		return
	}

	timeout = normalizeTimeout(timeout, time.Second)
	deadline := time.Now().Add(timeout)

	rounds := sessionCloseBurstRounds
	if rounds < 1 {
		rounds = 1
	}
	interval := timeout / time.Duration(rounds)
	if interval <= 0 {
		interval = timeout
	}

	for round := 0; round < rounds; round++ {
		c.sendSessionCloseRound(targets, deadline)
		if round == rounds-1 {
			break
		}

		remaining := time.Until(deadline)
		if remaining <= 0 {
			break
		}
		sleepFor := interval
		if sleepFor > remaining {
			sleepFor = remaining
		}
		time.Sleep(sleepFor)
	}

	if c.log != nil {
		c.log.Debugf(
			"\U0001F6AA <yellow>Client Session Close Burst Sent</yellow> <magenta>|</magenta> <blue>Session</blue>: <cyan>%d</cyan> <magenta>|</magenta> <blue>Targets</blue>: <cyan>%d</cyan>",
			c.sessionID,
			len(targets),
		)
	}
}

func (c *Client) selectSessionCloseTargets(maxTargets int) []Connection {
	if c == nil {
		return nil
	}

	if maxTargets < 1 {
		maxTargets = 1
	}

	targets := c.balancer.GetUniqueConnections(maxTargets)
	if len(targets) > 0 {
		return targets
	}

	if best, ok := c.balancer.GetBestConnection(); ok {
		return []Connection{best}
	}
	return nil
}

func (c *Client) sendSessionCloseRound(targets []Connection, deadline time.Time) {
	if c == nil || len(targets) == 0 {
		return
	}

	var wg sync.WaitGroup
	for _, conn := range targets {
		conn := conn
		wg.Add(1)
		go func() {
			defer wg.Done()
			query, err := c.buildTunnelTXTQueryRaw(conn.Domain, VpnProto.BuildOptions{
				SessionID:     c.sessionID,
				SessionCookie: c.sessionCookie,
				PacketType:    Enums.PACKET_SESSION_CLOSE,
			})
			if err != nil {
				return
			}
			c.sendOneWayDNSQuery(conn, query, deadline)
		}()
	}
	wg.Wait()
}

// applySyncedMTUState updates the client's internal MTU state after successful probing.
func (c *Client) applySyncedMTUState(uploadMTU int, downloadMTU int, uploadChars int) {
	if c == nil {
		return
	}
	c.syncedUploadMTU = uploadMTU
	c.syncedDownloadMTU = downloadMTU
	c.syncedUploadChars = uploadChars
	c.safeUploadMTU = computeSafeUploadMTU(uploadMTU, c.mtuCryptoOverhead)
	c.maxPackedBlocks = VpnProto.CalculateMaxPackedBlocks(uploadMTU, 80, c.effectiveMaxPacketsPerBatch())
	c.applySessionCompressionPolicy()
	if c.log != nil && c.successMTUChecks {
		c.log.Infof("\U0001F4CF <green>MTU state applied: UP=%d, DOWN=%d</green>", uploadMTU, downloadMTU)
	}
}

func (c *Client) applySessionCompressionPolicy() {
	if c == nil {
		return
	}

	minSize := c.effectiveCompressionMinSize()
	if minSize <= 0 {
		minSize = compression.DefaultMinSize
	}

	uploadCompression := compression.NormalizeAvailableType(c.uploadCompression)
	downloadCompression := compression.NormalizeAvailableType(c.downloadCompression)

	const mtuWarningThreshold = 100

	if c.syncedUploadMTU > 0 && c.syncedUploadMTU < mtuWarningThreshold {
		if uploadCompression != compression.TypeOff && c.log != nil {
			c.log.Warnf(
				"⚠️ <red>Session Compression Upload: <cyan>%s</cyan> (Disabled due to low MTU: <cyan>%d</cyan>)</red>",
				compression.TypeName(uploadCompression),
				c.syncedUploadMTU,
			)
		}
		uploadCompression = compression.TypeOff
		c.cfg.UploadCompressionType = int(compression.TypeOff)
	} else if c.syncedUploadMTU > 0 && c.syncedUploadMTU <= minSize {
		if uploadCompression != compression.TypeOff && c.log != nil {
			c.log.Infof(
				"\U0001F5DC <green>Session Compression Upload: <cyan>%s</cyan> (Disabled due to MinSize MTU: <cyan>%d</cyan>)</green>",
				compression.TypeName(uploadCompression),
				c.syncedUploadMTU,
			)
		}
		uploadCompression = compression.TypeOff
	}

	if c.syncedDownloadMTU > 0 && c.syncedDownloadMTU < mtuWarningThreshold {
		if downloadCompression != compression.TypeOff && c.log != nil {
			c.log.Warnf(
				"⚠️ <red>Session Compression Download: <cyan>%s</cyan> (Disabled due to low MTU: <cyan>%d</cyan>)</red>",
				compression.TypeName(downloadCompression),
				c.syncedDownloadMTU,
			)
		}
		downloadCompression = compression.TypeOff
		c.cfg.DownloadCompressionType = int(compression.TypeOff)
	} else if c.syncedDownloadMTU > 0 && c.syncedDownloadMTU <= minSize {
		if downloadCompression != compression.TypeOff && c.log != nil {
			c.log.Infof(
				"\U0001F5DC <green>Session Compression Download: <cyan>%s</cyan> (Disabled due to MinSize MTU: <cyan>%d</cyan>)</green>",
				compression.TypeName(downloadCompression),
				c.syncedDownloadMTU,
			)
		}
		downloadCompression = compression.TypeOff
	}

	c.uploadCompression = uploadCompression
	c.downloadCompression = downloadCompression

	if c.log != nil {
		c.log.Infof(
			"\U0001F9E9 <green>Effective Compression Upload: <cyan>%s</cyan> Download: <cyan>%s</cyan></green>",
			compression.TypeName(c.uploadCompression),
			compression.TypeName(c.downloadCompression),
		)
	}
}
