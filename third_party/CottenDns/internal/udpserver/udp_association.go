package udpserver

import (
	"context"
	"io"
	"net"
	"strconv"
	"strings"
	"sync"
	"time"

	"cottendns-go/internal/udpframe"
)

const (
	maxUDPAssociationTargets = 32
	udpEndpointIdleTimeout   = 2 * time.Minute
	udpResolveTimeout        = 5 * time.Second
)

type udpAssociationEndpoint struct {
	conn     *net.UDPConn
	lastUsed time.Time
}

// udpAssociationConn adapts addressed UDP datagrams to the byte-stream
// interface used by ARQ. A connected UDP socket is retained per destination,
// preserving the stable remote 5-tuple required by QUIC, calls, and games.
type udpAssociationConn struct {
	mu           sync.Mutex
	writeBuffer  []byte
	readPending  []byte
	readDeadline time.Time
	endpoints    map[string]*udpAssociationEndpoint
	aliases      map[string]string
	rx           chan []byte
	closed       chan struct{}
	closeOnce    sync.Once
}

func newUDPAssociationConn() *udpAssociationConn {
	return &udpAssociationConn{
		endpoints: make(map[string]*udpAssociationEndpoint),
		aliases:   make(map[string]string),
		rx:        make(chan []byte, 64),
		closed:    make(chan struct{}),
	}
}

func (c *udpAssociationConn) Write(p []byte) (int, error) {
	select {
	case <-c.closed:
		return 0, net.ErrClosed
	default:
	}

	c.mu.Lock()
	c.writeBuffer = append(c.writeBuffer, p...)
	if len(c.writeBuffer) > udpframe.MaxFramePayload+2 {
		c.writeBuffer = nil
		c.mu.Unlock()
		return len(p), nil
	}
	var bodies [][]byte
	for {
		body, rest, ready, err := udpframe.Pop(c.writeBuffer)
		if err != nil {
			c.writeBuffer = nil
			break
		}
		if !ready {
			break
		}
		bodies = append(bodies, append([]byte(nil), body...))
		c.writeBuffer = rest
	}
	c.mu.Unlock()

	for _, body := range bodies {
		_, host, port, payload, err := udpframe.DecodeBody(body)
		if err != nil || len(payload) == 0 {
			continue
		}
		endpoint, err := c.endpointFor(host, port)
		if err != nil {
			continue
		}
		if _, err := endpoint.conn.Write(payload); err == nil {
			c.mu.Lock()
			endpoint.lastUsed = time.Now()
			c.mu.Unlock()
		} else {
			c.removeEndpoint(endpoint)
		}
	}
	return len(p), nil
}

func (c *udpAssociationConn) endpointFor(host string, port uint16) (*udpAssociationEndpoint, error) {
	requestKey := net.JoinHostPort(strings.ToLower(host), strconv.Itoa(int(port)))
	now := time.Now()
	c.mu.Lock()
	if endpointKey := c.aliases[requestKey]; endpointKey != "" {
		if endpoint := c.endpoints[endpointKey]; endpoint != nil {
			endpoint.lastUsed = now
			c.mu.Unlock()
			return endpoint, nil
		}
		delete(c.aliases, requestKey)
	}
	c.mu.Unlock()

	addr, err := resolvePublicUDPAddr(host, port)
	if err != nil {
		return nil, err
	}
	key := addr.String()

	c.mu.Lock()
	if endpoint := c.endpoints[key]; endpoint != nil {
		endpoint.lastUsed = now
		c.aliases[requestKey] = key
		c.mu.Unlock()
		return endpoint, nil
	}
	for endpointKey, endpoint := range c.endpoints {
		if now.Sub(endpoint.lastUsed) >= udpEndpointIdleTimeout {
			delete(c.endpoints, endpointKey)
			c.removeAliasesLocked(endpointKey)
			_ = endpoint.conn.Close()
		}
	}
	if len(c.endpoints) >= maxUDPAssociationTargets {
		var oldestKey string
		var oldestTime time.Time
		for endpointKey, endpoint := range c.endpoints {
			if oldestKey == "" || endpoint.lastUsed.Before(oldestTime) {
				oldestKey, oldestTime = endpointKey, endpoint.lastUsed
			}
		}
		oldest := c.endpoints[oldestKey]
		delete(c.endpoints, oldestKey)
		c.removeAliasesLocked(oldestKey)
		if oldest != nil {
			_ = oldest.conn.Close()
		}
	}
	c.mu.Unlock()

	network := "udp6"
	if addr.IP.To4() != nil {
		network = "udp4"
	}
	conn, err := net.DialUDP(network, nil, addr)
	if err != nil {
		return nil, err
	}
	endpoint := &udpAssociationEndpoint{conn: conn, lastUsed: now}

	c.mu.Lock()
	if existing := c.endpoints[key]; existing != nil {
		c.aliases[requestKey] = key
		c.mu.Unlock()
		_ = conn.Close()
		return existing, nil
	}
	c.endpoints[key] = endpoint
	c.aliases[requestKey] = key
	c.mu.Unlock()
	go c.readEndpoint(endpoint)
	return endpoint, nil
}

func (c *udpAssociationConn) removeAliasesLocked(endpointKey string) {
	for requestKey, resolvedKey := range c.aliases {
		if resolvedKey == endpointKey {
			delete(c.aliases, requestKey)
		}
	}
}

func (c *udpAssociationConn) removeEndpoint(target *udpAssociationEndpoint) {
	if target == nil {
		return
	}
	c.mu.Lock()
	for endpointKey, endpoint := range c.endpoints {
		if endpoint == target {
			delete(c.endpoints, endpointKey)
			c.removeAliasesLocked(endpointKey)
			break
		}
	}
	c.mu.Unlock()
	_ = target.conn.Close()
}

func resolvePublicUDPAddr(host string, port uint16) (*net.UDPAddr, error) {
	if err := validateSOCKSTargetHost(host); err != nil {
		return nil, err
	}
	if ip := net.ParseIP(host); ip != nil {
		return &net.UDPAddr{IP: ip, Port: int(port)}, nil
	}
	ctx, cancel := context.WithTimeout(context.Background(), udpResolveTimeout)
	defer cancel()
	addresses, err := net.DefaultResolver.LookupIPAddr(ctx, host)
	if err != nil {
		return nil, err
	}
	for _, address := range addresses {
		if validateSOCKSTargetHost(address.IP.String()) == nil {
			return &net.UDPAddr{IP: address.IP, Zone: address.Zone, Port: int(port)}, nil
		}
	}
	return nil, &blockedSOCKSTargetError{host: host}
}

func (c *udpAssociationConn) readEndpoint(endpoint *udpAssociationEndpoint) {
	buffer := make([]byte, 65535)
	for {
		n, err := endpoint.conn.Read(buffer)
		if err != nil {
			c.removeEndpoint(endpoint)
			return
		}
		remote, ok := endpoint.conn.RemoteAddr().(*net.UDPAddr)
		if !ok || remote == nil || n == 0 {
			continue
		}
		atyp := byte(udpframe.AddressTypeIPv6)
		if remote.IP.To4() != nil {
			atyp = udpframe.AddressTypeIPv4
		}
		frame, err := udpframe.Encode(atyp, remote.IP.String(), uint16(remote.Port), buffer[:n])
		if err != nil {
			continue
		}
		select {
		case c.rx <- frame:
		case <-c.closed:
			return
		}
	}
}

func (c *udpAssociationConn) Read(p []byte) (int, error) {
	for {
		c.mu.Lock()
		if len(c.readPending) > 0 {
			n := copy(p, c.readPending)
			c.readPending = c.readPending[n:]
			c.mu.Unlock()
			return n, nil
		}
		deadline := c.readDeadline
		c.mu.Unlock()

		var timer <-chan time.Time
		var stopTimer func()
		if !deadline.IsZero() {
			remaining := time.Until(deadline)
			if remaining <= 0 {
				return 0, &udpAssociationTimeoutError{}
			}
			t := time.NewTimer(remaining)
			timer = t.C
			stopTimer = func() { t.Stop() }
		}
		select {
		case frame := <-c.rx:
			if stopTimer != nil {
				stopTimer()
			}
			c.mu.Lock()
			c.readPending = frame
			c.mu.Unlock()
		case <-timer:
			return 0, &udpAssociationTimeoutError{}
		case <-c.closed:
			if stopTimer != nil {
				stopTimer()
			}
			return 0, io.EOF
		}
	}
}

func (c *udpAssociationConn) SetReadDeadline(deadline time.Time) error {
	c.mu.Lock()
	c.readDeadline = deadline
	c.mu.Unlock()
	return nil
}

func (c *udpAssociationConn) Close() error {
	c.closeOnce.Do(func() {
		close(c.closed)
		c.mu.Lock()
		for key, endpoint := range c.endpoints {
			_ = endpoint.conn.Close()
			delete(c.endpoints, key)
		}
		clear(c.aliases)
		c.mu.Unlock()
	})
	return nil
}

type udpAssociationTimeoutError struct{}

func (*udpAssociationTimeoutError) Error() string   { return "UDP association read timeout" }
func (*udpAssociationTimeoutError) Timeout() bool   { return true }
func (*udpAssociationTimeoutError) Temporary() bool { return true }

var _ io.ReadWriteCloser = (*udpAssociationConn)(nil)
