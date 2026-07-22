// ==============================================================================
// CottenDNS
// Author: tajirax
// Github: https://github.com/TaJirax/CottenDns
// Year: 2026
// ==============================================================================

package client

import (
	"context"
	"crypto/rand"
	"sync"
	"sync/atomic"
	"time"

	Enums "cottendns-go/internal/enums"
)

type PingManager struct {
	client                *Client
	lastPingSentAt        atomic.Int64
	lastPongReceivedAt    atomic.Int64
	lastNonPingSentAt     atomic.Int64
	lastNonPongReceivedAt atomic.Int64
	nextPingSeq           atomic.Uint32

	ctx        context.Context
	cancel     context.CancelFunc
	wg         sync.WaitGroup
	wakeCh     chan struct{}
	lastWokeAt atomic.Int64
}

func newPingManager(client *Client) *PingManager {
	now := time.Now().UnixNano()
	p := &PingManager{
		client: client,
		wakeCh: make(chan struct{}, 1),
	}
	p.lastPingSentAt.Store(now)
	p.lastPongReceivedAt.Store(now)
	p.lastNonPingSentAt.Store(now)
	p.lastNonPongReceivedAt.Store(now)
	p.lastWokeAt.Store(now)
	return p
}

// Start starts the autonomous ping loop.
// Timestamps are reset to now so that the watchdog timer begins from the
// moment the client is fully operational (all resolvers scanned, session
// initialised), not from when the PingManager was first constructed.
func (p *PingManager) Start(parentCtx context.Context) {
	p.Stop() // Ensure old one is stopped

	now := time.Now().UnixNano()
	p.lastPingSentAt.Store(now)
	p.lastPongReceivedAt.Store(now)
	p.lastNonPingSentAt.Store(now)
	p.lastNonPongReceivedAt.Store(now)
	p.lastWokeAt.Store(now)

	p.ctx, p.cancel = context.WithCancel(parentCtx)
	p.wg.Add(1)
	go p.pingLoop()
}

// Stop stops the ping loop.
func (p *PingManager) Stop() {
	if p.cancel != nil {
		p.cancel()
		p.wg.Wait()
		p.cancel = nil
	}
}

func (p *PingManager) NotifyPacket(packetType uint8, isInbound bool) {
	if p == nil {
		return
	}

	isPing := packetType == Enums.PACKET_PING
	isPong := packetType == Enums.PACKET_PONG

	now := time.Now().UnixNano()

	if isInbound {
		if isPong {
			p.lastPongReceivedAt.Store(now)
		} else {
			p.lastNonPongReceivedAt.Store(now)
			p.wake(now)
		}
	} else {
		if isPing {
			p.lastPingSentAt.Store(now)
		} else {
			p.lastNonPingSentAt.Store(now)
			p.wake(now)
		}
	}
}

func (p *PingManager) wake(now int64) {
	// Throttle wakeups to at most once per 100ms to reduce CPU overhead in high traffic
	if now-p.lastWokeAt.Load() < int64(100*time.Millisecond) {
		return
	}
	p.lastWokeAt.Store(now)
	select {
	case p.wakeCh <- struct{}{}:
	default:
	}
}

func (p *PingManager) nextInterval(nowNano int64) time.Duration {
	lastNonPingSent := p.lastNonPingSentAt.Load()
	lastNonPongRecv := p.lastNonPongReceivedAt.Load()

	// Use fast int64 comparisons for intervals
	warmThresholdNano := int64(p.client.cfg.PingWarmThreshold())

	if nowNano-lastNonPingSent < warmThresholdNano || nowNano-lastNonPongRecv < warmThresholdNano {
		return p.client.effectivePingAggressiveInterval()
	}

	idleSent := nowNano - lastNonPingSent
	idleRecv := nowNano - lastNonPongRecv
	minIdle := idleSent
	if idleRecv < minIdle {
		minIdle = idleRecv
	}

	coolThresholdNano := int64(p.client.cfg.PingCoolThreshold())
	coldThresholdNano := int64(p.client.cfg.PingColdThreshold())
	switch {
	case minIdle < coolThresholdNano:
		return p.client.cfg.PingLazyInterval()
	case minIdle < coldThresholdNano:
		return p.client.cfg.PingCooldownInterval()
	default:
		return p.client.cfg.PingColdInterval()
	}
}

func (p *PingManager) pingLoop() {
	defer p.wg.Done()

	p.client.log.Debugf("\U0001F3D3 <cyan>Ping Manager loop started</cyan>")
	timer := time.NewTimer(p.client.effectivePingAggressiveInterval())
	defer timer.Stop()

	for {
		select {
		case <-p.ctx.Done():
			return
		case <-p.wakeCh:
		case <-timer.C:
		}

		now := time.Now()
		nowNano := now.UnixNano()
		interval := p.nextInterval(nowNano)
		lastPing := p.lastPingSentAt.Load()

		if nowNano-lastPing >= int64(interval) {
			if p.client.SessionReady() {
				payload, err := buildClientPingPayload()
				if err == nil {
					// Use Stream 0 for pings
					p.client.streamsMu.RLock()
					s0 := p.client.active_streams[0]
					p.client.streamsMu.RUnlock()

					if s0 != nil {
						s0.PushTXPacket(
							Enums.DefaultPacketPriority(Enums.PACKET_PING),
							Enums.PACKET_PING,
							p.nextPingSequence(),
							0,
							0,
							0,
							0,
							payload,
						)
					}
				}
			}
		}

		// Watchdog: trigger a session restart if the client has been actively
		// sending non-ping packets but has received no server response at all
		// for longer than the configured watchdog timeout. This recovers from
		// the "zombie" state where all resolvers are silently dropping packets
		// (e.g., when the auto-disable minimum resolver threshold is reached)
		// and no server-side message (REJECT, BUSY, ERROR_DROP) arrives to
		// trigger a restart through the normal path.
		if watchdogTimeout := p.client.cfg.PingWatchdogTimeout(); watchdogTimeout > 0 && p.client.SessionReady() {
			lastAnyResponse := p.lastPongReceivedAt.Load()
			if nonPong := p.lastNonPongReceivedAt.Load(); nonPong > lastAnyResponse {
				lastAnyResponse = nonPong
			}
			noResponseFor := time.Duration(nowNano - lastAnyResponse)
			sinceLastNonPingSent := time.Duration(nowNano - p.lastNonPingSentAt.Load())
			// Only restart if the client is actively sending non-ping traffic
			// (sinceLastNonPingSent < watchdogTimeout) so that a genuinely idle
			// but healthy session is never incorrectly restarted.
			if noResponseFor >= watchdogTimeout && sinceLastNonPingSent < watchdogTimeout {
				p.client.log.Warnf(
					"⏱ <yellow>Ping watchdog triggered: no server response for <cyan>%s</cyan>, restarting session</yellow>",
					noResponseFor.Round(time.Second),
				)
				p.client.requestTransportRecovery("ping watchdog: no server response")
			}
		}

		checkInterval := interval / 2
		if checkInterval < 100*time.Millisecond {
			checkInterval = 100 * time.Millisecond
		}
		if checkInterval > 1*time.Second {
			checkInterval = 1 * time.Second
		}

		if !timer.Stop() {
			select {
			case <-timer.C:
			default:
			}
		}
		timer.Reset(checkInterval)
	}
}

func (p *PingManager) nextPingSequence() uint16 {
	if p == nil {
		return 0
	}
	return uint16(p.nextPingSeq.Add(1))
}

func buildClientPingPayload() ([]byte, error) {
	// Pre-allocate the fixed size payload to avoid multiple allocations and appends
	payload := make([]byte, 7)
	payload[0] = 'P'
	payload[1] = 'O'
	payload[2] = ':'

	// Use rand.Read directly into the pre-allocated buffer starting at index 3
	if _, err := rand.Read(payload[3:]); err != nil {
		return nil, err
	}
	return payload, nil
}
