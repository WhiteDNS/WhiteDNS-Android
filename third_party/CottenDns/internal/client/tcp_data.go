// ==============================================================================
// CottenDNS
// Author: tajirax
// Github: https://github.com/TaJirax/CottenDns
// Year: 2026
// ==============================================================================
// tcp_data.go — high-throughput data-plane transport over DNS-over-TCP/53, used
// when the client runs in TCP mode (RESOLVER_TRANSPORT=tcp, or the auto fallback
// after a UDP scan finds zero resolvers). It keeps one persistent TCP connection
// per resolver, writes length-prefixed queries, and runs a read loop per
// connection that pushes responses into the SAME rxChannel the UDP reader feeds —
// so handleInboundPacket processes TCP and UDP responses identically. Broken
// connections are re-dialed lazily on the next send.
// ==============================================================================

package client

import (
	"context"
	"encoding/binary"
	"io"
	"net"
	"sync"
	"sync/atomic"
	"time"

	Enums "cottendns-go/internal/enums"
)

const (
	tcpDataDialTimeout  = 4 * time.Second
	tcpDataWriteTimeout = 10 * time.Second
	tcpDataWorkers      = 8
	tcpDataStripes      = 2
	tcpDataQueueCap     = 256
	tcpControlQueueCap  = 64
)

// streamDataTransport is the data-plane contract shared by every persistent
// (non-UDP) resolver transport. TCP and DoT are served by tcpDataManager — DoT
// is the same length-prefixed framing, only dialed over TLS — while DoH has its
// own request/response implementation. The dispatcher only ever sees this
// interface, so adding a transport does not touch the send path.
type streamDataTransport interface {
	Start(ctx context.Context)
	Stop()
	Send(serverKey string, addr *net.UDPAddr, packet []byte, priority int, now time.Time)
}

type tcpDataManager struct {
	client *Client
	ctx    context.Context
	cancel context.CancelFunc
	// dial opens one connection to a resolver. Swapping it is the only difference
	// between plain TCP/53 and DoT, so both share every other line in this file.
	dial      func(addr *net.UDPAddr) (net.Conn, error)
	transport string // for logs: "TCP/53" or "DoT"

	mu       sync.Mutex
	conns    map[string]*tcpDataConn // keyed by resolver address string
	dead     bool
	controlQ chan tcpDataJob
	dataQ    chan tcpDataJob
	wg       sync.WaitGroup
	next     atomic.Uint64
}

type tcpDataJob struct {
	serverKey string
	addr      *net.UDPAddr
	packet    []byte
	now       time.Time
}

type tcpDataConn struct {
	manager      *tcpDataManager
	key          string // resolver address string (map key)
	resolverAddr *net.UDPAddr
	localAddr    string

	writeMu sync.Mutex
	conn    net.Conn
}

func newTCPDataManager(c *Client) *tcpDataManager {
	return &tcpDataManager{
		client:    c,
		conns:     make(map[string]*tcpDataConn),
		controlQ:  make(chan tcpDataJob, tcpControlQueueCap),
		dataQ:     make(chan tcpDataJob, tcpDataQueueCap),
		transport: "TCP/53",
		dial: func(addr *net.UDPAddr) (net.Conn, error) {
			d := net.Dialer{Timeout: tcpDataDialTimeout}
			return d.Dial("tcp", net.JoinHostPort(addr.IP.String(), itoaInt(addr.Port)))
		},
	}
}

// newDoTDataManager reuses the TCP data plane over TLS. DoT is DNS-over-TCP
// framing inside TLS, so only the dial changes: the connection pooling, framing,
// read loop and rxChannel hand-off are all shared with TCP/53.
func newDoTDataManager(c *Client) *tcpDataManager {
	return &tcpDataManager{
		client:    c,
		conns:     make(map[string]*tcpDataConn),
		controlQ:  make(chan tcpDataJob, tcpControlQueueCap),
		dataQ:     make(chan tcpDataJob, tcpDataQueueCap),
		transport: "DoT",
		dial: func(addr *net.UDPAddr) (net.Conn, error) {
			return c.dialDoTResolver(addr.String(), tcpDataDialTimeout)
		},
	}
}

func (m *tcpDataManager) Start(ctx context.Context) {
	workerCtx, cancel := context.WithCancel(ctx)
	m.mu.Lock()
	m.ctx = workerCtx
	m.cancel = cancel
	m.dead = false
	m.mu.Unlock()
	for i := 0; i < tcpDataWorkers; i++ {
		m.wg.Add(1)
		go m.worker(workerCtx)
	}
}

// Stop closes every connection and prevents new ones.
func (m *tcpDataManager) Stop() {
	if m == nil {
		return
	}
	m.mu.Lock()
	m.dead = true
	if m.cancel != nil {
		m.cancel()
		m.cancel = nil
	}
	conns := m.conns
	m.conns = make(map[string]*tcpDataConn)
	m.mu.Unlock()
	for _, dc := range conns {
		dc.close()
	}
	m.wg.Wait()
}

// Send transmits one already-built DNS query to the resolver over its persistent
// TCP connection, dialing lazily and re-dialing on failure. On success it mirrors
// the UDP writer's bookkeeping (resolver send tracking + tx byte counter).
func (m *tcpDataManager) Send(serverKey string, addr *net.UDPAddr, packet []byte, priority int, now time.Time) {
	if m == nil || addr == nil || len(packet) == 0 {
		return
	}
	job := tcpDataJob{serverKey: serverKey, addr: addr, packet: append([]byte(nil), packet...), now: now}
	queue := m.dataQ
	if priority <= Enums.PacketPriorityHigh {
		queue = m.controlQ
	}
	select {
	case queue <- job:
	default:
		m.client.txAdmissionDrops.Add(1)
	}
}

func (m *tcpDataManager) worker(ctx context.Context) {
	defer m.wg.Done()
	for {
		select {
		case <-ctx.Done():
			return
		case job := <-m.controlQ:
			m.sendJob(job)
			continue
		default:
		}
		select {
		case <-ctx.Done():
			return
		case job := <-m.controlQ:
			m.sendJob(job)
		case job := <-m.dataQ:
			m.sendJob(job)
		}
	}
}

func (m *tcpDataManager) sendJob(job tcpDataJob) {
	slot := int(m.next.Add(1)-1) % tcpDataStripes
	dc, err := m.connFor(job.addr, slot)
	if err != nil || dc == nil {
		m.client.streamDialFailures.Add(1)
		return
	}

	dc.writeMu.Lock()
	_ = dc.conn.SetWriteDeadline(time.Now().Add(tcpDataWriteTimeout))
	werr := writeTCPDNSFramed(dc.conn, job.packet)
	dc.writeMu.Unlock()

	if werr != nil {
		m.client.streamWriteFailures.Add(1)
		dc.close()
		m.remove(dc)
		return
	}

	m.client.trackResolverSend(job.packet, job.addr.String(), dc.localAddr, job.serverKey, job.now)
	m.client.txTotalBytes.Add(uint64(len(job.packet)))
}

// connFor returns the existing connection for a resolver or dials a new one and
// starts its read loop.
func (m *tcpDataManager) connFor(addr *net.UDPAddr, slot int) (*tcpDataConn, error) {
	key := addr.String() + "#" + itoaInt(slot)

	m.mu.Lock()
	if m.dead {
		m.mu.Unlock()
		return nil, net.ErrClosed
	}
	if dc, ok := m.conns[key]; ok {
		m.mu.Unlock()
		return dc, nil
	}
	m.mu.Unlock()

	// Dial outside the lock (network I/O). The dialer decides TCP vs TLS(DoT).
	conn, err := m.dial(addr)
	if err != nil {
		return nil, err
	}

	dc := &tcpDataConn{
		manager:      m,
		key:          key,
		resolverAddr: addr,
		conn:         conn,
	}
	if la := conn.LocalAddr(); la != nil {
		dc.localAddr = la.String()
	}

	m.mu.Lock()
	if m.dead {
		m.mu.Unlock()
		_ = conn.Close()
		return nil, net.ErrClosed
	}
	// Lost a race with another sender — use the winner, drop ours.
	if existing, ok := m.conns[key]; ok {
		m.mu.Unlock()
		_ = conn.Close()
		return existing, nil
	}
	m.conns[key] = dc
	ctx := m.ctx
	m.mu.Unlock()

	go dc.readLoop(ctx)
	return dc, nil
}

func (m *tcpDataManager) remove(dc *tcpDataConn) {
	if dc == nil {
		return
	}
	m.mu.Lock()
	if cur, ok := m.conns[dc.key]; ok && cur == dc {
		delete(m.conns, dc.key)
	}
	m.mu.Unlock()
}

func (dc *tcpDataConn) close() {
	if dc == nil || dc.conn == nil {
		return
	}
	_ = dc.conn.Close()
}

// readLoop reads length-prefixed DNS responses and feeds them into the client's
// rxChannel using pooled buffers, exactly like the UDP reader, so the existing
// processor/handleInboundPacket path handles them unchanged.
func (dc *tcpDataConn) readLoop(ctx context.Context) {
	defer func() {
		dc.close()
		dc.manager.remove(dc)
	}()

	var lenBuf [2]byte
	for {
		if ctx != nil && ctx.Err() != nil {
			return
		}
		if _, err := io.ReadFull(dc.conn, lenBuf[:]); err != nil {
			return
		}
		n := int(binary.BigEndian.Uint16(lenBuf[:]))
		if n < 12 || n > dc.manager.client.runtimeDNSReadBufferSize() {
			return
		}

		buf := dc.manager.client.getRuntimeUDPBuffer()
		if _, err := io.ReadFull(dc.conn, buf[:n]); err != nil {
			dc.manager.client.putRuntimeUDPBuffer(buf)
			return
		}

		// Only DNS responses (QR=1) are of interest, mirroring the UDP reader.
		if (buf[2] & 0x80) == 0 {
			dc.manager.client.putRuntimeUDPBuffer(buf)
			continue
		}

		c := dc.manager.client
		c.rxTotalBytes.Add(uint64(n))
		select {
		case c.rxChannel <- asyncReadPacket{data: buf[:n], addr: dc.resolverAddr, localAddr: dc.localAddr}:
		default:
			c.putRuntimeUDPBuffer(buf)
			c.onRXDrop(dc.resolverAddr)
		}
	}
}

// itoaInt is a small allocation-light int-to-string for a port number.
func itoaInt(v int) string {
	if v == 0 {
		return "0"
	}
	neg := v < 0
	if neg {
		v = -v
	}
	var b [11]byte
	i := len(b)
	for v > 0 {
		i--
		b[i] = byte('0' + v%10)
		v /= 10
	}
	if neg {
		i--
		b[i] = '-'
	}
	return string(b[i:])
}
