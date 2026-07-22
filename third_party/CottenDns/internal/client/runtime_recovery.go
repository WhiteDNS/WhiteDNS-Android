package client

import (
	"strings"
	"time"
)

const (
	runtimeTransportRecoveryCooldown = 15 * time.Second
	runtimeSessionInitFailureLimit   = 3
)

// transportCanFallback reports whether the configured mode authorizes moving
// between resolver transports. Explicit udp/tcp choices remain strict.
func (c *Client) transportCanFallback() bool {
	if c == nil {
		return false
	}
	switch strings.ToLower(strings.TrimSpace(c.cfg.ResolverTransport)) {
	case "udp", "tcp":
		return false
	default:
		return true
	}
}

// requestTransportRecovery restarts the session and asks the main loop to
// repeat transport/MTU discovery. It is intentionally rate-limited: a single
// unstable resolver must not cause an expensive fleet scan or transport flap.
func (c *Client) requestTransportRecovery(reason string) {
	if c == nil {
		return
	}
	now := c.now()
	if last := c.lastTransportRecovery.Load(); last != 0 && now.Sub(time.Unix(0, last)) < runtimeTransportRecoveryCooldown {
		c.requestSessionRestart(reason)
		return
	}
	if c.transportRecoveryPending.CompareAndSwap(false, true) {
		c.lastTransportRecovery.Store(now.UnixNano())
		c.transportRecoveryCount.Add(1)
		if c.log != nil {
			c.log.Warnf("<yellow>Resolver path recovery armed</yellow>: <cyan>%s</cyan>", reason)
		}
	}
	c.requestSessionRestart(reason)
}

// activatePendingTransportRecovery runs only on the main loop after the async
// runtime has stopped, so resetting MTU state cannot race active send workers.
func (c *Client) activatePendingTransportRecovery() bool {
	if c == nil || !c.transportRecoveryPending.Swap(false) {
		return false
	}
	c.successMTUChecks = false
	c.connectionsHavePreknownMTU = false
	for i := range c.connections {
		c.prepareConnectionMTUScanState(&c.connections[i])
	}
	return true
}
