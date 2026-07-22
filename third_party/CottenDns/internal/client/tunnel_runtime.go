// ==============================================================================
// CottenDNS
// Author: tajirax
// Github: https://github.com/TaJirax/CottenDns
// Year: 2026
// ==============================================================================
// Package client provides the core logic for the CottenDns client.
// This file (tunnel_runtime.go) handles low-level UDP network operations,
// including sending DNS-encapsulated packets and receiving responses.
// ==============================================================================

package client

import (
	"context"
	"encoding/binary"
	"errors"
	"net"
	"time"

	"cottendns-go/internal/dnsparser"
	VpnProto "cottendns-go/internal/vpnproto"
)

const (
	// RuntimeUDPReadBufferSize defines the maximum size of the UDP read buffer.
	RuntimeUDPReadBufferSize         = 65535
	runtimeDNSReadBufferFloor        = 8192
	runtimeUDPMaxMismatchedResponses = 64
	runtimeUDPDrainGrace             = time.Millisecond

	// pooledConnMaxAge is the maximum time a UDP connection can remain idle in
	// the resolver pool before it is discarded and re-dialed. Stale connections can have their
	// NAT mappings expired, causing silent packet loss (writes succeed but
	// responses route to a dead port).
	pooledConnMaxAge = 90 * time.Second
)

func runtimeDNSReadBufferSize(maxDownloadMTU int) int {
	size := maxDownloadMTU + 2048 // DNS framing, TXT chunks and encryption slack.
	if size < runtimeDNSReadBufferFloor {
		size = runtimeDNSReadBufferFloor
	}
	if size > RuntimeUDPReadBufferSize {
		size = RuntimeUDPReadBufferSize
	}
	return size
}

func (c *Client) runtimeDNSReadBufferSize() int {
	if c == nil || c.runtimeReadBufferSize <= 0 {
		return RuntimeUDPReadBufferSize
	}
	return c.runtimeReadBufferSize
}

type pooledUDPConn struct {
	conn     *net.UDPConn
	pooledAt time.Time
}

func (c *Client) drainStaleUDPResponses(conn *net.UDPConn, buffer []byte) error {
	if conn == nil || len(buffer) == 0 {
		return nil
	}

	for drained := 0; drained < runtimeUDPMaxMismatchedResponses*2; drained++ {
		if err := conn.SetReadDeadline(time.Now().Add(runtimeUDPDrainGrace)); err != nil {
			return err
		}

		_, err := conn.Read(buffer)
		if err != nil {
			if ne, ok := err.(net.Error); ok && ne.Timeout() {
				return nil
			}
			return err
		}
	}

	return nil
}

// exchangeUDPQueryWithConn sends one UDP packet through the provided connection
// and waits for a response with a matching DNS transaction ID.
func (c *Client) exchangeUDPQueryWithConn(conn *net.UDPConn, packet []byte, timeout time.Duration) ([]byte, error) {
	if len(packet) < 2 {
		return nil, errors.New("malformed dns query")
	}
	expectedID := binary.BigEndian.Uint16(packet[:2])

	buffer := c.getRuntimeUDPBuffer()
	defer c.putRuntimeUDPBuffer(buffer)
	defer func() {
		_ = conn.SetDeadline(time.Time{})
	}()

	if err := c.drainStaleUDPResponses(conn, buffer); err != nil {
		return nil, err
	}

	writeDeadline := time.Now().Add(timeout)
	if err := conn.SetWriteDeadline(writeDeadline); err != nil {
		return nil, err
	}

	if _, err := conn.Write(packet); err != nil {
		return nil, err
	}

	if err := conn.SetReadDeadline(writeDeadline); err != nil {
		return nil, err
	}

	mismatchedResponses := 0

	for {
		n, err := conn.Read(buffer)
		if err != nil {
			return nil, err
		}

		if n >= 2 && binary.BigEndian.Uint16(buffer[:2]) == expectedID {
			// Copy matched response out so the pooled buffer can be recycled.
			result := make([]byte, n)
			copy(result, buffer[:n])
			return result, nil
		}

		mismatchedResponses++
		if mismatchedResponses >= runtimeUDPMaxMismatchedResponses {
			return nil, errors.New("too many mismatched dns responses on shared udp socket")
		}
	}
}

func (c *Client) sendOneWayDNSQuery(resolver Connection, packet []byte, deadline time.Time) error {
	if c.usesStreamTransport() {
		// Best-effort one-shot over the active stream transport (e.g. the
		// session-close burst). DoH has no one-way form, so it reuses the normal
		// request/response exchanger and discards the answer.
		if c.activeTransport() == transportDoH {
			transport, err := c.newDoHQueryTransport(resolver.ResolverLabel)
			if err != nil {
				return err
			}
			defer transport.Close()
			_, err = transport.exchange(packet, time.Until(deadline))
			return err
		}

		var (
			conn net.Conn
			err  error
		)
		if c.activeTransport() == transportDoT {
			conn, err = c.dialDoTResolver(resolver.ResolverLabel, time.Until(deadline))
		} else {
			d := net.Dialer{Timeout: time.Until(deadline)}
			conn, err = d.Dial("tcp", resolver.ResolverLabel)
		}
		if err != nil {
			return err
		}
		_ = conn.SetWriteDeadline(deadline)
		werr := writeTCPDNSFramed(conn, packet)
		_ = conn.Close()
		return werr
	}

	udpConn, err := c.getUDPConn(resolver.ResolverLabel)
	if err != nil {
		return err
	}

	if err := udpConn.SetWriteDeadline(deadline); err != nil {
		_ = udpConn.Close()
		return err
	}

	if _, err := udpConn.Write(packet); err != nil {
		_ = udpConn.Close()
		return err
	}

	c.putUDPConn(resolver.ResolverLabel, udpConn)
	return nil
}

// getUDPConn retrieves a UDP connection from the pool for the specified resolver.
// If no connection is available in the pool, it dials a new one.
func (c *Client) getUDPConn(resolverLabel string) (*net.UDPConn, error) {
	c.resolverConnsMu.Lock()
	pool, ok := c.resolverConns[resolverLabel]
	if !ok {
		pool = make(chan pooledUDPConn, c.cfg.ResolverUDPConnectionPoolSize)
		c.resolverConns[resolverLabel] = pool
	}
	c.resolverConnsMu.Unlock()

	now := time.Now()
	for {
		select {
		case pc := <-pool:
			if now.Sub(pc.pooledAt) > pooledConnMaxAge {
				_ = pc.conn.Close()
				continue // discard stale, try next
			}
			return pc.conn, nil
		default:
			return dialUDPResolver(resolverLabel)
		}
	}
}

// putUDPConn returns a UDP connection to the pool for the specified resolver.
// If the pool is full, the connection is closed.
func (c *Client) putUDPConn(resolverLabel string, conn *net.UDPConn) {
	if conn == nil {
		return
	}

	c.resolverConnsMu.Lock()
	pool := c.resolverConns[resolverLabel]
	c.resolverConnsMu.Unlock()

	if pool == nil {
		_ = conn.Close()
		return
	}

	select {
	case pool <- pooledUDPConn{conn: conn, pooledAt: time.Now()}:
	default:
		_ = conn.Close()
	}
}

func (c *Client) closeResolverConnPools() {
	if c == nil {
		return
	}

	c.resolverConnsMu.Lock()
	pools := c.resolverConns
	c.resolverConns = make(map[string]chan pooledUDPConn)
	c.resolverConnsMu.Unlock()

	for _, pool := range pools {
		for {
			select {
			case pc := <-pool:
				if pc.conn != nil {
					_ = pc.conn.Close()
				}
			default:
				goto nextPool
			}
		}
	nextPool:
	}
}

// getRuntimeUDPBuffer retrieves a byte slice from the internal buffer pool.
// This is used to reduce allocations during high-frequency network operations.
func (c *Client) getRuntimeUDPBuffer() []byte {
	if c == nil {
		return make([]byte, RuntimeUDPReadBufferSize)
	}

	size := c.runtimeDNSReadBufferSize()
	buf, _ := c.udpBufferPool.Get().([]byte)
	if cap(buf) < size {
		return make([]byte, size)
	}

	return buf[:size]
}

// putRuntimeUDPBuffer returns a byte slice to the internal buffer pool.
func (c *Client) putRuntimeUDPBuffer(buf []byte) {
	if c == nil || buf == nil {
		return
	}
	size := c.runtimeDNSReadBufferSize()
	if cap(buf) < size {
		return
	}

	c.udpBufferPool.Put(buf[:size])
}

// dialUDPResolver resolves the resolver address and establishes a new UDP connection.
func dialUDPResolver(resolverLabel string) (*net.UDPConn, error) {
	addr, err := net.ResolveUDPAddr("udp", resolverLabel)
	if err != nil {
		return nil, err
	}
	return net.DialUDP("udp", nil, addr)
}

// normalizeTimeout ensures the timeout is positive, falling back to a default if necessary.
func normalizeTimeout(timeout time.Duration, fallback time.Duration) time.Duration {
	if timeout <= 0 {
		return fallback
	}
	return timeout
}

// udpQueryTransport wraps a UDP connection for synchronous queries and satisfies
// queryExchanger (see transport.go).
type udpQueryTransport struct {
	client *Client
	conn   *net.UDPConn
}

func (t *udpQueryTransport) exchange(packet []byte, timeout time.Duration) ([]byte, error) {
	if t == nil || t.conn == nil || t.client == nil {
		return nil, net.ErrClosed
	}
	return t.client.exchangeUDPQueryWithConn(t.conn, packet, timeout)
}

func (t *udpQueryTransport) Close() error {
	if t == nil || t.conn == nil {
		return nil
	}
	return t.conn.Close()
}

// exchangeDNSOverConnection sends a DNS query and returns the extracted VPN
// packet, over the client's active transport (UDP, or DNS-over-TCP in TCP mode).
func (c *Client) exchangeDNSOverConnection(conn Connection, query []byte, timeout time.Duration) (VpnProto.Packet, error) {
	var response []byte

	if c.usesStreamTransport() {
		transport, err := c.newQueryTransport(conn.ResolverLabel)
		if err != nil {
			return VpnProto.Packet{}, err
		}
		response, err = transport.exchange(query, timeout)
		_ = transport.Close()
		if err != nil {
			return VpnProto.Packet{}, err
		}
	} else {
		udpConn, err := c.getUDPConn(conn.ResolverLabel)
		if err != nil {
			return VpnProto.Packet{}, err
		}
		response, err = c.exchangeUDPQueryWithConn(udpConn, query, timeout)
		if err != nil {
			_ = udpConn.Close()
			return VpnProto.Packet{}, err
		}
		c.putUDPConn(conn.ResolverLabel, udpConn)
	}

	packet, err := dnsparser.ExtractVPNResponseMatching(response, c.responseMode == mtuProbeBase64Reply, c.cfg.Domains)
	if err != nil {
		return VpnProto.Packet{}, err
	}

	return packet, nil
}

// exchangeDNSOverConnectionContext is used by raced control exchanges. Each
// racer owns its transport, so cancelling the race can close losing UDP/TCP/TLS
// exchanges immediately instead of keeping sockets and HTTP streams alive until
// their full timeout.
func (c *Client) exchangeDNSOverConnectionContext(ctx context.Context, conn Connection, query []byte, timeout time.Duration) (VpnProto.Packet, error) {
	transport, err := c.newQueryTransport(conn.ResolverLabel)
	if err != nil {
		return VpnProto.Packet{}, err
	}
	defer transport.Close()

	type result struct {
		response []byte
		err      error
	}
	done := make(chan result, 1)
	go func() {
		response, exchangeErr := transport.exchange(query, timeout)
		done <- result{response: response, err: exchangeErr}
	}()

	select {
	case <-ctx.Done():
		_ = transport.Close()
		return VpnProto.Packet{}, ctx.Err()
	case res := <-done:
		if res.err != nil {
			return VpnProto.Packet{}, res.err
		}
		return dnsparser.ExtractVPNResponseMatching(res.response, c.responseMode == mtuProbeBase64Reply, c.cfg.Domains)
	}
}
