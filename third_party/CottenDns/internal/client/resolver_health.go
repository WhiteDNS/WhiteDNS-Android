package client

import (
	"context"
	"math"
	"slices"
	"sort"
	"time"

	"cottendns-go/internal/logger"
)

type resolverHealthEvent struct {
	At time.Time
}

type resolverHealthState struct {
	Events           []resolverHealthEvent
	TimeoutOnlySince time.Time
	LastSuccessAt    time.Time
}

type resolverRecheckState struct {
	FailCount int
	NextAt    time.Time
	InFlight  bool
}

type resolverDisabledState struct {
	DisabledAt   time.Time
	NextRetryAt  time.Time
	RetryCount   int
	SuccessCount int
	Cause        string
}

type resolverRecheckCandidate struct {
	key             string
	nextAt          time.Time
	failCount       int
	runtimePriority bool
}

type resolverAutoDisableCandidate struct {
	key            string
	eventCount     int
	span           time.Duration
	oldestAge      time.Duration
	timeoutOnlyAge time.Duration
}

const runtimeDisabledResolverReactivationSuccessThreshold = 2

// resolverPoolPressureThreshold is the valid-resolver count at or below which the
// active pool is treated as "under pressure". When depleted, recovery becomes
// aggressive (a single successful recheck reactivates a resolver) so the pool can
// refill as fast as it drains instead of ratcheting down to the floor over a long
// session. Above it, recovery stays conservative to avoid flapping.
const resolverPoolPressureThreshold = 8

// Speculative discovery (re-probing never-valid resolvers mid-session to find
// fresh working ones) is throttled hard so it can never burst bandwidth away from
// the user's live traffic: at most discoveryRecheckPerBatch probe per recheck
// batch, and no more often than discoveryRecheckMinSpacing. Recovery of
// previously-working resolvers gets a wider but still bounded wave so MTU probes
// cannot overwhelm the live tunnel.
const (
	discoveryRecheckPerBatch   = 1
	discoveryRecheckMinSpacing = 15 * time.Second
	// Runtime recovery is more urgent than speculative discovery, but launching
	// a full MTU probe for every disabled resolver at once can steal bandwidth
	// from the live tunnel. Keep a small recovery wave in flight per batch.
	runtimeRecoveryRecheckPerBatch = 4
)

// reactivationSuccessThreshold reports how many consecutive successful rechecks a
// runtime-disabled resolver must pass before rejoining the active pool. Under pool
// pressure a single success is enough (a depleted pool recovers on the first good
// probe); a healthy pool keeps the two-success confirmation.
func (c *Client) reactivationSuccessThreshold() int {
	if c != nil && c.balancer != nil && c.balancer.ValidCount() <= resolverPoolPressureThreshold {
		return 1
	}
	return runtimeDisabledResolverReactivationSuccessThreshold
}

func (c *Client) resolverHealthDebugEnabled() bool {
	return c != nil && c.log != nil && c.log.Enabled(logger.LevelDebug)
}

func (c *Client) activeResolverCount() int {
	if c == nil || c.balancer == nil {
		return 0
	}

	return c.balancer.ValidCount()
}

// hasStrategy5Reserve reports whether MTU-weighted balancing has a reachable
// reserve tier that can take over after a session restart/re-cluster. The normal
// three-resolver safety floor remains unchanged for every other strategy.
func (c *Client) hasStrategy5Reserve(excludeKey string) bool {
	if c == nil || c.balancer == nil || c.cfg.ResolverBalancingStrategy != int(BalancingMTUWeighted) || !c.cfg.MTUAdaptiveGrouping {
		return false
	}

	usable := 0
	hasReserve := false
	for _, conn := range c.balancer.AllValidConnectionsIncludingBackup() {
		if conn.Key == excludeKey {
			continue
		}
		usable++
		if conn.Backup {
			hasReserve = true
		}
	}
	return usable > 0 && hasReserve
}

func (c *Client) canDisableResolverConnection(serverKey string) bool {
	if c == nil || c.balancer == nil {
		return false
	}
	if c.balancer.ValidCount() > 3 {
		return true
	}
	conn, ok := c.GetConnectionByKey(serverKey)
	return ok && conn.IsValid && !conn.Backup && c.hasStrategy5Reserve(serverKey)
}

func (c *Client) strategy5PoolNeedsRestart() bool {
	if c == nil || c.balancer == nil || c.cfg.ResolverBalancingStrategy != int(BalancingMTUWeighted) || !c.cfg.MTUAdaptiveGrouping {
		return false
	}
	target := max(1, c.cfg.MTUWeightedMinPool)
	primary := 0
	reserve := 0
	for _, conn := range c.balancer.AllValidConnectionsIncludingBackup() {
		if conn.Backup {
			reserve++
		} else {
			primary++
		}
	}
	return reserve > 0 && primary < target
}

func (c *Client) initResolverRecheckMeta() {
	if c == nil {
		return
	}

	now := c.now()
	nextInactive := now.Add(c.recheckInactiveInterval())

	c.resolverStatsMu.Lock()
	c.resolverPending = make(map[resolverSampleKey]resolverSample)
	c.resolverStatsMu.Unlock()

	c.resolverHealthMu.Lock()
	defer c.resolverHealthMu.Unlock()

	c.resolverHealth = make(map[string]*resolverHealthState, len(c.connections))
	c.resolverRecheck = make(map[string]resolverRecheckState, len(c.connections))
	c.runtimeDisabled = make(map[string]resolverDisabledState)

	for idx := range c.connections {
		conn := &c.connections[idx]
		if conn.Key == "" {
			continue
		}
		c.resolverHealth[conn.Key] = &resolverHealthState{
			Events: make([]resolverHealthEvent, 0, 8),
		}
		meta := resolverRecheckState{}
		if !conn.IsValid {
			meta.NextAt = nextInactive
		}
		c.resolverRecheck[conn.Key] = meta
	}
}

func (c *Client) runResolverHealthLoop(ctx context.Context) {
	for {
		if ctx != nil {
			select {
			case <-ctx.Done():
				return
			default:
			}
		}

		now := c.now()
		c.collectExpiredResolverTimeouts(now)
		c.runResolverAutoDisable(now)
		c.runResolverRecheckBatch(ctx, now)

		waitFor := c.nextResolverHealthWait(now)
		timer := time.NewTimer(waitFor)
		if ctx == nil {
			<-timer.C
			continue
		}
		select {
		case <-ctx.Done():
			timer.Stop()
			return
		case <-timer.C:
		}
	}
}

func (c *Client) nextResolverHealthWait(now time.Time) time.Duration {
	waitFor := 2 * time.Second
	if c == nil {
		return waitFor
	}
	if c.cfg.AutoDisableTimeoutServers {
		waitFor = minDuration(waitFor, c.autoDisableCheckInterval())
	}
	if !c.cfg.RecheckInactiveServersEnabled || !c.successMTUChecks {
		return clampResolverHealthWait(waitFor)
	}

	c.resolverHealthMu.RLock()
	defer c.resolverHealthMu.RUnlock()

	for key, meta := range c.resolverRecheck {
		conn, ok := c.GetConnectionByKey(key)
		if !ok || conn.IsValid || meta.NextAt.IsZero() || meta.InFlight {
			continue
		}
		dueIn := time.Until(meta.NextAt)
		if !meta.NextAt.After(now) {
			dueIn = 250 * time.Millisecond
		}
		waitFor = minDuration(waitFor, dueIn)
	}
	return clampResolverHealthWait(waitFor)
}

func clampResolverHealthWait(waitFor time.Duration) time.Duration {
	if waitFor < 250*time.Millisecond {
		return 250 * time.Millisecond
	}
	if waitFor > 5*time.Second {
		return 5 * time.Second
	}
	return waitFor
}

func (c *Client) noteResolverTimeout(serverKey string, at time.Time) {
	if c == nil || serverKey == "" {
		return
	}
	if at.IsZero() {
		at = c.now()
	}
	c.recordResolverHealthEvent(serverKey, false, at)
}
func (c *Client) recordResolverHealthEvent(serverKey string, success bool, now time.Time) {
	if c == nil || serverKey == "" {
		return
	}
	conn, ok := c.GetConnectionByKey(serverKey)
	if !ok || !conn.IsValid {
		return
	}

	// Feed the adaptive pacer: success eases the resolver's cooldown, a failure
	// (RCODE != 0 / timeout — i.e. an overload signal) grows its backoff window.
	if success {
		c.pacer.success(serverKey)
	} else {
		c.pacer.throttle(serverKey, now)
	}

	c.resolverHealthMu.Lock()
	defer c.resolverHealthMu.Unlock()

	state := c.resolverHealth[serverKey]
	if state == nil {
		state = &resolverHealthState{Events: make([]resolverHealthEvent, 0, 8)}
		c.resolverHealth[serverKey] = state
	}
	if success {
		state.Events = state.Events[:0]
		state.TimeoutOnlySince = time.Time{}
		state.LastSuccessAt = now
		return
	}

	c.insertResolverHealthEventLocked(state, resolverHealthEvent{At: now})
	if state.TimeoutOnlySince.IsZero() || now.Before(state.TimeoutOnlySince) {
		state.TimeoutOnlySince = now
	}
	c.pruneResolverHealthLocked(state, now)
	if len(state.Events) == 0 {
		state.TimeoutOnlySince = time.Time{}
	} else if state.TimeoutOnlySince.IsZero() {
		state.TimeoutOnlySince = state.Events[0].At
	}
}

func (c *Client) insertResolverHealthEventLocked(state *resolverHealthState, event resolverHealthEvent) {
	if state == nil {
		return
	}
	n := len(state.Events)
	if n == 0 || !event.At.Before(state.Events[n-1].At) {
		state.Events = append(state.Events, event)
		return
	}

	insertAt := sort.Search(n, func(i int) bool {
		return !state.Events[i].At.Before(event.At)
	})
	state.Events = append(state.Events, resolverHealthEvent{})
	copy(state.Events[insertAt+1:], state.Events[insertAt:])
	state.Events[insertAt] = event
}

func (c *Client) retractResolverTimeoutEvent(serverKey string, timedOutAt time.Time, now time.Time) {
	if c == nil || serverKey == "" || timedOutAt.IsZero() {
		return
	}

	c.resolverHealthMu.Lock()
	defer c.resolverHealthMu.Unlock()

	state := c.resolverHealth[serverKey]
	if state == nil || len(state.Events) == 0 {
		return
	}

	removeIdx := -1
	for i, event := range state.Events {
		if event.At.Equal(timedOutAt) {
			removeIdx = i
			break
		}
	}
	if removeIdx == -1 {
		return
	}

	state.Events = append(state.Events[:removeIdx], state.Events[removeIdx+1:]...)
	c.pruneResolverHealthLocked(state, now)
	if len(state.Events) == 0 {
		state.TimeoutOnlySince = time.Time{}
	} else {
		state.TimeoutOnlySince = state.Events[0].At
	}
}

func (c *Client) pruneResolverHealthLocked(state *resolverHealthState, now time.Time) {
	if state == nil || len(state.Events) == 0 {
		return
	}
	cutoff := now.Add(-c.autoDisableTimeoutWindow())
	dropCount := 0
	for dropCount < len(state.Events) && state.Events[dropCount].At.Before(cutoff) {
		dropCount++
	}
	if dropCount == 0 {
		return
	}
	state.Events = append(state.Events[:0], state.Events[dropCount:]...)
}

func (c *Client) runResolverAutoDisable(now time.Time) {
	if c == nil || !c.cfg.AutoDisableTimeoutServers {
		return
	}

	window := c.autoDisableTimeoutWindow()
	debugEnabled := c.resolverHealthDebugEnabled()
	candidates := make([]resolverAutoDisableCandidate, 0, len(c.connections))
	snap := c.resolverHealthBalancerSnapshot()
	c.resolverHealthMu.Lock()
	for key, state := range c.resolverHealth {
		if state == nil {
			continue
		}
		c.pruneResolverHealthLocked(state, now)
		if !c.resolverConnectionIsValidFromSnapshot(snap, key) {
			continue
		}
		if len(state.Events) < c.autoDisableMinObservations() {
			continue
		}
		if state.TimeoutOnlySince.IsZero() {
			continue
		}
		timeoutOnlyAge := now.Sub(state.TimeoutOnlySince)
		if timeoutOnlyAge < window {
			continue
		}
		candidate := resolverAutoDisableCandidate{key: key}
		if debugEnabled {
			candidate.eventCount = len(state.Events)
			candidate.span = state.Events[len(state.Events)-1].At.Sub(state.Events[0].At)
			candidate.oldestAge = now.Sub(state.Events[0].At)
			candidate.timeoutOnlyAge = timeoutOnlyAge
		}
		candidates = append(candidates, candidate)
	}
	c.resolverHealthMu.Unlock()

	// The three-resolver safety floor deliberately avoids disabling the entire
	// fleet, but when every remaining path has a mature timeout-only window it
	// also means there is nowhere useful to send. Recover the transport now
	// instead of waiting for the longer session watchdog.
	validCount := c.balancer.ValidCount()
	if validCount > 0 && len(candidates) >= validCount {
		c.requestTransportRecovery("all active resolvers reached the timeout window")
		return
	}

	if debugEnabled {
		for _, candidate := range candidates {
			c.log.Debugf(
				"\U0001F6A8 <yellow>Resolver auto-disable candidate</yellow> <magenta>|</magenta> <blue>Resolver</blue>: <cyan>%s</cyan> <magenta>|</magenta> <blue>Events</blue>: <cyan>%d</cyan> <magenta>|</magenta> <blue>Span</blue>: <cyan>%s</cyan> <magenta>|</magenta> <blue>OldestAge</blue>: <cyan>%s</cyan> <magenta>|</magenta> <blue>TimeoutOnlyAge</blue>: <cyan>%s</cyan>",
				candidate.key,
				candidate.eventCount,
				candidate.span.Round(time.Millisecond),
				candidate.oldestAge.Round(time.Millisecond),
				candidate.timeoutOnlyAge.Round(time.Millisecond),
			)
		}
	}

	for _, candidate := range candidates {
		if !c.canDisableResolverConnection(candidate.key) {
			continue
		}
		c.disableResolverConnection(candidate.key, "100% timeout window")
	}
}

func (c *Client) resolverHealthBalancerSnapshot() *balancerSnapshot {
	if c == nil || c.balancer == nil {
		return nil
	}
	return c.balancer.snapshot.Load()
}

func (c *Client) resolverConnectionIsValidFromSnapshot(snap *balancerSnapshot, key string) bool {
	if snap != nil {
		idx, ok := snap.indexByKey[key]
		if !ok || idx < 0 || idx >= len(snap.connections) {
			return false
		}
		return snap.connections[idx].IsValid
	}

	conn, ok := c.GetConnectionByKey(key)
	return ok && conn.IsValid
}

func (c *Client) disableResolverConnection(serverKey string, cause string) bool {
	if c == nil || serverKey == "" {
		return false
	}
	conn, ok := c.GetConnectionByKey(serverKey)
	if !ok || !conn.IsValid || !c.canDisableResolverConnection(serverKey) {
		return false
	}
	now := c.now()
	nextAt := now.Add(maxDuration(5*time.Second, c.recheckServerInterval()*2))

	c.resolverHealthMu.Lock()
	meta := c.resolverRecheck[serverKey]
	c.runtimeDisabled[serverKey] = resolverDisabledState{
		DisabledAt:   now,
		NextRetryAt:  nextAt,
		RetryCount:   meta.FailCount,
		SuccessCount: 0,
		Cause:        cause,
	}
	c.resolverHealthMu.Unlock()

	if !c.balancer.SetConnectionValidity(serverKey, false) {
		c.resolverHealthMu.Lock()
		delete(c.runtimeDisabled, serverKey)
		c.resolverHealthMu.Unlock()
		return false
	}
	if refreshed, ok := c.GetConnectionByKey(serverKey); ok {
		conn = refreshed
	}

	c.resolverHealthMu.Lock()
	meta = c.resolverRecheck[serverKey]
	meta.NextAt = nextAt
	c.resolverRecheck[serverKey] = meta
	c.runtimeDisabled[serverKey] = resolverDisabledState{
		DisabledAt:   now,
		NextRetryAt:  nextAt,
		RetryCount:   meta.FailCount,
		SuccessCount: 0,
		Cause:        cause,
	}
	delete(c.resolverHealth, serverKey)
	c.resolverHealthMu.Unlock()

	c.clearPreferredResolverReferences(serverKey)
	c.balancer.ResetServerStats(serverKey)
	if c.log != nil {
		c.log.Warnf(
			"\U0001F6D1 <yellow>DNS server <cyan>%s</cyan> disabled due to: <red>%s</red></yellow> <magenta>|</magenta> <green>Active Resolvers</green>: <cyan>%d</cyan>",
			conn.ResolverLabel,
			cause,
			c.activeResolverCount(),
		)
	}
	// A resolver just left the active pool; surface the change.
	c.logResolverRuntimeState()
	if c.strategy5PoolNeedsRestart() {
		c.requestSessionRestart("MTU-weighted primary resolver pool fell below its configured minimum; re-clustering reserves")
	}
	return true
}

func (c *Client) clearPreferredResolverReferences(serverKey string) {
	if c == nil || serverKey == "" {
		return
	}

	c.streamsMu.RLock()
	streams := make([]*Stream_client, 0, len(c.active_streams))
	for _, stream := range c.active_streams {
		if stream != nil {
			streams = append(streams, stream)
		}
	}
	c.streamsMu.RUnlock()

	for _, stream := range streams {
		stream.resolverMu.Lock()
		if stream.PreferredServerKey == serverKey {
			stream.PreferredServerKey = ""
			stream.ResolverResendStreak = 0
			stream.CachedResolverPlan = nil
			stream.CachedResolverPlanFor = ""
			stream.CachedResolverPlanSize = 0
			stream.CachedResolverVersion = 0
		}
		stream.resolverMu.Unlock()
	}
}

func (c *Client) reactivateResolverConnection(serverKey string) bool {
	if c == nil || serverKey == "" {
		return false
	}
	conn, ok := c.GetConnectionByKey(serverKey)
	if !ok || conn.IsValid {
		return false
	}
	if !c.balancer.SetConnectionValidity(serverKey, true) {
		return false
	}
	if refreshed, ok := c.GetConnectionByKey(serverKey); ok {
		conn = refreshed
	}

	c.resolverHealthMu.Lock()
	delete(c.runtimeDisabled, serverKey)
	delete(c.resolverHealth, serverKey)
	c.resolverRecheck[serverKey] = resolverRecheckState{}
	// Periodically prune runtimeDisabled entries for connections no longer tracked
	if len(c.runtimeDisabled) > len(c.connections) {
		for k := range c.runtimeDisabled {
			if c.connectionPtrByKey(k) == nil {
				delete(c.runtimeDisabled, k)
			}
		}
	}
	c.resolverHealthMu.Unlock()

	// Seed with a moderate initial score so the balancer doesn't flood the
	// just-reactivated resolver before it has proven itself. Stats were zeroed
	// when the resolver was disabled; seeding conservatively (80% delivery)
	// lets it participate immediately but at lower priority than healthy peers.
	c.balancer.SeedConservativeStats(serverKey)
	if c.log != nil {
		c.log.Infof(
			"\U0001F504 <green>DNS server <cyan>%s</cyan> re-activated after successful recheck.</green> <magenta>|</magenta> <green>Active Resolvers</green>: <cyan>%d</cyan>",
			conn.ResolverLabel,
			c.activeResolverCount(),
		)
	}
	return true
}

func (c *Client) clearResolverRecheckInFlight(serverKey string) {
	if c == nil || serverKey == "" {
		return
	}

	c.resolverHealthMu.Lock()
	meta, ok := c.resolverRecheck[serverKey]
	if ok && meta.InFlight {
		meta.InFlight = false
		c.resolverRecheck[serverKey] = meta
	}
	c.resolverHealthMu.Unlock()
}

func (c *Client) scheduleResolverRecheckFailure(serverKey string, runtimePriority bool, now time.Time) {
	if c == nil || serverKey == "" {
		return
	}

	c.resolverHealthMu.Lock()
	defer c.resolverHealthMu.Unlock()

	meta := c.resolverRecheck[serverKey]
	meta.FailCount++

	var base time.Duration
	if runtimePriority {
		base = maxDuration(10*time.Second, c.recheckServerInterval()*2)
	} else {
		base = maxDuration(30*time.Second, c.recheckInactiveInterval()/4)
	}

	pow := math.Pow(1.8, float64(min(meta.FailCount, 6)))
	delay := time.Duration(float64(base) * pow)
	if delay > c.recheckInactiveInterval() {
		delay = c.recheckInactiveInterval()
	}
	delay += deterministicResolverJitter(serverKey, delay)
	meta.NextAt = now.Add(delay)
	meta.InFlight = false
	c.resolverRecheck[serverKey] = meta

	if state, ok := c.runtimeDisabled[serverKey]; ok {
		state.NextRetryAt = meta.NextAt
		state.RetryCount = meta.FailCount
		state.SuccessCount = 0
		c.runtimeDisabled[serverKey] = state
	}
}

func deterministicResolverJitter(serverKey string, delay time.Duration) time.Duration {
	if serverKey == "" || delay <= 0 {
		return 0
	}
	maxJitter := minDuration(2*time.Second, time.Duration(float64(delay)*0.15))
	if maxJitter <= 0 {
		return 0
	}
	var hash uint64 = 1469598103934665603
	for i := 0; i < len(serverKey); i++ {
		hash ^= uint64(serverKey[i])
		hash *= 1099511628211
	}
	return time.Duration(hash % uint64(maxJitter))
}

func (c *Client) runResolverRecheckBatch(ctx context.Context, now time.Time) {
	if c == nil || !c.cfg.RecheckInactiveServersEnabled || !c.successMTUChecks {
		return
	}

	candidates := c.collectDueResolverRechecks(now)
	if len(candidates) == 0 {
		return
	}

	limit := c.recheckBatchSize()
	if len(candidates) > limit {
		candidates = candidates[:limit]
	}

	discoveryBudget := discoveryRecheckPerBatch
	runtimeRecoveryBudget := runtimeRecoveryRecheckPerBatch
	for _, candidate := range candidates {
		conn, ok := c.GetConnectionByKey(candidate.key)
		if !ok || conn.IsValid {
			continue
		}
		// Gentle discovery: re-probing a never-valid resolver is purely
		// speculative, so trickle it (at most discoveryRecheckPerBatch per batch,
		// spaced by discoveryRecheckMinSpacing) so it never competes with the
		// user's live traffic. Runtime recovery uses its own small per-batch budget.
		if candidate.runtimePriority {
			if runtimeRecoveryBudget <= 0 {
				continue
			}
		} else {
			if discoveryBudget <= 0 {
				continue
			}
			if last := c.lastDiscoveryRecheckUnix.Load(); last != 0 &&
				now.Sub(time.Unix(0, last)) < discoveryRecheckMinSpacing {
				continue
			}
		}
		if !c.tryAcquireResolverRecheckSlot() {
			break
		}
		c.resolverHealthMu.Lock()
		if m, ok := c.resolverRecheck[candidate.key]; ok {
			m.InFlight = true
			c.resolverRecheck[candidate.key] = m
		} else {
			c.resolverHealthMu.Unlock()
			c.releaseResolverRecheckSlot()
			continue
		}
		c.resolverHealthMu.Unlock()

		if candidate.runtimePriority {
			runtimeRecoveryBudget--
		} else {
			discoveryBudget--
			c.lastDiscoveryRecheckUnix.Store(now.UnixNano())
		}

		go func(cand resolverRecheckCandidate, cn Connection) {
			defer func() {
				c.releaseResolverRecheckSlot()
				if r := recover(); r != nil {
					c.scheduleResolverRecheckFailure(cand.key, cand.runtimePriority, c.now())
				}
			}()

			if c.recheckResolverConnection(ctx, &cn) {
				c.handleSuccessfulResolverRecheck(cand.key, c.now())
				return
			}

			c.scheduleResolverRecheckFailure(cand.key, cand.runtimePriority, c.now())
		}(candidate, conn)
	}
}

func (c *Client) handleSuccessfulResolverRecheck(serverKey string, now time.Time) {
	if c == nil || serverKey == "" {
		return
	}

	c.resolverHealthMu.Lock()
	state, runtimeDisabled := c.runtimeDisabled[serverKey]
	if runtimeDisabled {
		state.SuccessCount++
		if state.SuccessCount < c.reactivationSuccessThreshold() {
			nextAt := now.Add(maxDuration(2*time.Second, c.recheckServerInterval()))
			meta := c.resolverRecheck[serverKey]
			meta.InFlight = false
			meta.NextAt = nextAt
			c.resolverRecheck[serverKey] = meta
			state.NextRetryAt = nextAt
			state.RetryCount = meta.FailCount
			c.runtimeDisabled[serverKey] = state
			c.resolverHealthMu.Unlock()
			return
		}
	}
	c.resolverHealthMu.Unlock()

	if !c.applyRecheckedResolverMTU(serverKey) {
		c.clearResolverRecheckInFlight(serverKey)
		return
	}

	if !c.reactivateResolverConnection(serverKey) {
		c.clearResolverRecheckInFlight(serverKey)
		return
	}

	if conn, ok := c.GetConnectionByKey(serverKey); ok {
		c.appendResolverCacheEntry(&conn)
	}

	// A resolver just re-entered the active pool; surface the change.
	c.logResolverRuntimeState()
}

func (c *Client) tryAcquireResolverRecheckSlot() bool {
	if c == nil || c.resolverRecheckSem == nil {
		return true
	}
	select {
	case c.resolverRecheckSem <- struct{}{}:
		return true
	default:
		return false
	}
}

func (c *Client) releaseResolverRecheckSlot() {
	if c == nil || c.resolverRecheckSem == nil {
		return
	}
	select {
	case <-c.resolverRecheckSem:
	default:
	}
}

func (c *Client) collectDueResolverRechecks(now time.Time) []resolverRecheckCandidate {
	if c == nil {
		return nil
	}

	c.resolverHealthMu.RLock()
	defer c.resolverHealthMu.RUnlock()

	runtimeCandidates := make([]resolverRecheckCandidate, 0, len(c.runtimeDisabled))
	normalCandidates := make([]resolverRecheckCandidate, 0, len(c.connections))
	for key, meta := range c.resolverRecheck {
		conn, ok := c.GetConnectionByKey(key)
		if !ok || conn.IsValid || meta.InFlight {
			continue
		}
		if !meta.NextAt.IsZero() && meta.NextAt.After(now) {
			continue
		}

		candidateValue := resolverRecheckCandidate{
			key:       key,
			nextAt:    meta.NextAt,
			failCount: meta.FailCount,
		}
		if state, ok := c.runtimeDisabled[key]; ok {
			candidateValue.runtimePriority = true
			candidateValue.nextAt = state.NextRetryAt
			candidateValue.failCount = state.RetryCount
			runtimeCandidates = append(runtimeCandidates, candidateValue)
			continue
		}
		normalCandidates = append(normalCandidates, candidateValue)
	}

	slices.SortFunc(runtimeCandidates, func(a, b resolverRecheckCandidate) int {
		if cmp := a.nextAt.Compare(b.nextAt); cmp != 0 {
			return cmp
		}
		if a.failCount < b.failCount {
			return -1
		}
		if a.failCount > b.failCount {
			return 1
		}
		if a.key < b.key {
			return -1
		}
		if a.key > b.key {
			return 1
		}
		return 0
	})
	slices.SortFunc(normalCandidates, func(a, b resolverRecheckCandidate) int {
		if a.failCount < b.failCount {
			return -1
		}
		if a.failCount > b.failCount {
			return 1
		}
		if cmp := a.nextAt.Compare(b.nextAt); cmp != 0 {
			return cmp
		}
		if a.key < b.key {
			return -1
		}
		if a.key > b.key {
			return 1
		}
		return 0
	})

	candidates := make([]resolverRecheckCandidate, 0, len(runtimeCandidates)+len(normalCandidates))
	candidates = append(candidates, runtimeCandidates...)
	candidates = append(candidates, normalCandidates...)
	return candidates
}

func (c *Client) recheckResolverConnection(ctx context.Context, conn *Connection) bool {
	if c == nil || conn == nil || c.syncedUploadMTU <= 0 || c.syncedDownloadMTU <= 0 {
		return false
	}
	if c.recheckConnectionFn != nil {
		if !c.recheckConnectionFn(conn) {
			return false
		}
		return true
	}

	transport, err := c.newQueryTransport(conn.ResolverLabel)
	if err != nil {
		return false
	}
	defer transport.Close()

	upOK := false
	for attempt := 0; attempt < c.mtuTestRetries; attempt++ {
		if err := ctx.Err(); err != nil {
			return false
		}
		passed, _, err := c.sendUploadMTUProbe(ctx, conn, transport, c.syncedUploadMTU, mtuProbeOptions{Quiet: true, IsRetry: attempt > 0})
		if err == nil && passed {
			upOK = true
			break
		}
	}
	if !upOK {
		return false
	}

	downOK := false
	for attempt := 0; attempt < c.mtuTestRetries; attempt++ {
		if err := ctx.Err(); err != nil {
			return false
		}
		passed, _, err := c.sendDownloadMTUProbe(ctx, conn, transport, c.syncedDownloadMTU, c.syncedUploadMTU, mtuProbeOptions{Quiet: true, IsRetry: attempt > 0})
		if err == nil && passed {
			downOK = true
			break
		}
	}
	if !downOK {
		return false
	}

	return true
}

func (c *Client) applyRecheckedResolverMTU(serverKey string) bool {
	if c == nil || serverKey == "" {
		return false
	}

	if c.balancer == nil {
		conn := c.connectionPtrByKey(serverKey)
		if conn == nil {
			return false
		}
		conn.UploadMTUBytes = c.syncedUploadMTU
		conn.UploadMTUChars = c.encodedCharsForPayload(c.syncedUploadMTU)
		conn.DownloadMTUBytes = c.syncedDownloadMTU
		return true
	}

	return c.balancer.SetConnectionMTU(
		serverKey,
		c.syncedUploadMTU,
		c.encodedCharsForPayload(c.syncedUploadMTU),
		c.syncedDownloadMTU,
	)
}

func (c *Client) isRuntimeDisabledResolver(serverKey string) bool {
	if c == nil || serverKey == "" {
		return false
	}
	c.resolverHealthMu.RLock()
	_, ok := c.runtimeDisabled[serverKey]
	c.resolverHealthMu.RUnlock()
	return ok
}

func (c *Client) autoDisableTimeoutWindow() time.Duration {
	return time.Duration(c.cfg.AutoDisableTimeoutWindowSeconds * float64(time.Second))
}

func (c *Client) autoDisableCheckInterval() time.Duration {
	return time.Duration(c.cfg.AutoDisableCheckIntervalSeconds * float64(time.Second))
}

func (c *Client) autoDisableMinObservations() int {
	if c.cfg.AutoDisableMinObservations < 1 {
		return 1
	}
	return c.cfg.AutoDisableMinObservations
}

func (c *Client) recheckInactiveInterval() time.Duration {
	return time.Duration(c.cfg.RecheckInactiveIntervalSeconds * float64(time.Second))
}

func (c *Client) recheckServerInterval() time.Duration {
	return time.Duration(c.cfg.RecheckServerIntervalSeconds * float64(time.Second))
}

func (c *Client) recheckBatchSize() int {
	if c.cfg.RecheckBatchSize < 1 {
		return 1
	}
	return c.cfg.RecheckBatchSize
}

func minDuration(a, b time.Duration) time.Duration {
	if a < b {
		return a
	}
	return b
}

func maxDuration(a, b time.Duration) time.Duration {
	if a > b {
		return a
	}
	return b
}

func boolToInt(v bool) int {
	if v {
		return 1
	}
	return 0
}
