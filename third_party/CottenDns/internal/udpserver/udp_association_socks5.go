package udpserver

import (
	"context"
	"encoding/binary"
	"errors"
	"io"
	"net"
	"strconv"
	"sync"
	"time"

	Enums "cottendns-go/internal/enums"
	"cottendns-go/internal/udpframe"
)

type externalSOCKSUDPAssociation struct {
	mu          sync.Mutex
	control     net.Conn
	relay       *net.UDPConn
	writeBuffer []byte
	readPending []byte
	validated   map[string]time.Time
	closeOnce   sync.Once
}

func (s *Server) newUDPAssociationContext(ctx context.Context) (io.ReadWriteCloser, error) {
	if !s.useExternalSOCKS5 {
		return newUDPAssociationConn(), nil
	}
	return s.newExternalSOCKSUDPAssociation(ctx)
}

func (s *Server) newExternalSOCKSUDPAssociation(ctx context.Context) (*externalSOCKSUDPAssociation, error) {
	control, err := s.dialTCPTargetContext(ctx, s.externalSOCKS5Address)
	if err != nil {
		return nil, err
	}
	fail := func(err error) (*externalSOCKSUDPAssociation, error) {
		_ = control.Close()
		return nil, err
	}
	timeout := s.socksConnectTimeout
	if timeout <= 0 {
		timeout = s.cfg.SOCKSConnectTimeout()
	}
	if timeout > 0 {
		_ = control.SetDeadline(time.Now().Add(timeout))
	}
	if err := writeAll(control, s.externalSOCKS5Greeting()); err != nil {
		return fail(&upstreamSOCKS5Error{packetType: Enums.PACKET_SOCKS5_UPSTREAM_UNAVAILABLE, err: err})
	}
	var greeting [2]byte
	if _, err := io.ReadFull(control, greeting[:]); err != nil {
		return fail(&upstreamSOCKS5Error{packetType: Enums.PACKET_SOCKS5_UPSTREAM_UNAVAILABLE, err: err})
	}
	if greeting[0] != 0x05 {
		return fail(&upstreamSOCKS5Error{packetType: Enums.PACKET_SOCKS5_UPSTREAM_UNAVAILABLE, err: errors.New("upstream proxy is not SOCKS5")})
	}
	if err := s.handleExternalSOCKS5Auth(control, greeting[1]); err != nil {
		return fail(err)
	}
	// UDP ASSOCIATE with an unspecified client endpoint lets the upstream use
	// the source of our first relay datagram, as required for NAT-safe clients.
	if err := writeAll(control, []byte{0x05, 0x03, 0x00, 0x01, 0, 0, 0, 0, 0, 0}); err != nil {
		return fail(&upstreamSOCKS5Error{packetType: Enums.PACKET_SOCKS5_UPSTREAM_UNAVAILABLE, err: err})
	}
	var header [4]byte
	if _, err := io.ReadFull(control, header[:]); err != nil {
		return fail(&upstreamSOCKS5Error{packetType: Enums.PACKET_SOCKS5_UPSTREAM_UNAVAILABLE, err: err})
	}
	if header[0] != 0x05 || header[1] != 0x00 {
		return fail(&upstreamSOCKS5Error{packetType: socks5ReplyPacketType(header[1]), err: errors.New("upstream SOCKS5 rejected UDP ASSOCIATE")})
	}
	relayAddr, err := readSOCKS5BoundUDPAddress(control, header[3])
	if err != nil {
		return fail(&upstreamSOCKS5Error{packetType: Enums.PACKET_SOCKS5_UPSTREAM_UNAVAILABLE, err: err})
	}
	if relayAddr.IP == nil || relayAddr.IP.IsUnspecified() {
		if remote, ok := control.RemoteAddr().(*net.TCPAddr); ok {
			relayAddr.IP = append(net.IP(nil), remote.IP...)
			relayAddr.Zone = remote.Zone
		}
	}
	network := "udp6"
	if relayAddr.IP.To4() != nil {
		network = "udp4"
	}
	relay, err := net.DialUDP(network, nil, relayAddr)
	if err != nil {
		return fail(err)
	}
	_ = control.SetDeadline(time.Time{})
	association := &externalSOCKSUDPAssociation{
		control:   control,
		relay:     relay,
		validated: make(map[string]time.Time),
	}
	go func() {
		var one [1]byte
		_, _ = control.Read(one[:])
		_ = association.Close()
	}()
	return association, nil
}

func readSOCKS5BoundUDPAddress(conn net.Conn, atyp byte) (*net.UDPAddr, error) {
	var host string
	switch atyp {
	case udpframe.AddressTypeIPv4:
		var raw [4]byte
		if _, err := io.ReadFull(conn, raw[:]); err != nil {
			return nil, err
		}
		host = net.IP(raw[:]).String()
	case udpframe.AddressTypeIPv6:
		var raw [16]byte
		if _, err := io.ReadFull(conn, raw[:]); err != nil {
			return nil, err
		}
		host = net.IP(raw[:]).String()
	case udpframe.AddressTypeDomain:
		var length [1]byte
		if _, err := io.ReadFull(conn, length[:]); err != nil {
			return nil, err
		}
		if length[0] == 0 {
			return nil, errors.New("empty SOCKS5 UDP relay hostname")
		}
		raw := make([]byte, int(length[0]))
		if _, err := io.ReadFull(conn, raw); err != nil {
			return nil, err
		}
		host = string(raw)
	default:
		return nil, errors.New("unsupported SOCKS5 UDP relay address type")
	}
	var portBytes [2]byte
	if _, err := io.ReadFull(conn, portBytes[:]); err != nil {
		return nil, err
	}
	port := int(binary.BigEndian.Uint16(portBytes[:]))
	if port == 0 {
		return nil, errors.New("invalid SOCKS5 UDP relay port")
	}
	return net.ResolveUDPAddr("udp", net.JoinHostPort(host, strconv.Itoa(port)))
}

func (c *externalSOCKSUDPAssociation) Write(p []byte) (int, error) {
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
		// Resolve as a policy check even though the upstream proxy performs the
		// actual lookup for domain frames. This prevents obvious DNS rebinding
		// requests from using the server as a private-network UDP relay.
		validationKey := net.JoinHostPort(host, strconv.Itoa(int(port)))
		now := time.Now()
		c.mu.Lock()
		validatedAt := c.validated[validationKey]
		c.mu.Unlock()
		if validatedAt.IsZero() || now.Sub(validatedAt) >= udpEndpointIdleTimeout {
			if _, err := resolvePublicUDPAddr(host, port); err != nil {
				continue
			}
			c.mu.Lock()
			c.validated[validationKey] = now
			c.mu.Unlock()
		}
		packet := make([]byte, 3, len(body)+3)
		packet = append(packet, body...)
		if _, err := c.relay.Write(packet); err != nil {
			return len(p), err
		}
	}
	return len(p), nil
}

func (c *externalSOCKSUDPAssociation) Read(p []byte) (int, error) {
	for {
		c.mu.Lock()
		if len(c.readPending) > 0 {
			n := copy(p, c.readPending)
			c.readPending = c.readPending[n:]
			c.mu.Unlock()
			return n, nil
		}
		c.mu.Unlock()
		buffer := make([]byte, 65535)
		n, err := c.relay.Read(buffer)
		if err != nil {
			return 0, err
		}
		if n < 6 || buffer[0] != 0 || buffer[1] != 0 || buffer[2] != 0 {
			continue
		}
		body := buffer[3:n]
		if _, _, _, _, err := udpframe.DecodeBody(body); err != nil {
			continue
		}
		framed := make([]byte, 2, len(body)+2)
		binary.BigEndian.PutUint16(framed, uint16(len(body)))
		framed = append(framed, body...)
		c.mu.Lock()
		c.readPending = framed
		c.mu.Unlock()
	}
}

func (c *externalSOCKSUDPAssociation) SetReadDeadline(deadline time.Time) error {
	return c.relay.SetReadDeadline(deadline)
}

func (c *externalSOCKSUDPAssociation) Close() error {
	c.closeOnce.Do(func() {
		_ = c.relay.Close()
		_ = c.control.Close()
	})
	return nil
}

var _ io.ReadWriteCloser = (*externalSOCKSUDPAssociation)(nil)
