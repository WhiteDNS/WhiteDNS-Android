// ==============================================================================
// CottenDNS
// Author: tajirax
// Github: https://github.com/TaJirax/CottenDns
// Year: 2026
// ==============================================================================

package udpserver

import (
	"bytes"
	"context"
	"errors"
	"net"
	"strconv"
	"strings"
	"time"

	Enums "cottendns-go/internal/enums"
	SocksProto "cottendns-go/internal/socksproto"
	"cottendns-go/internal/udpframe"
	VpnProto "cottendns-go/internal/vpnproto"
)

const maxDeferredConnectAttemptTimeout = 15 * time.Second

func (s *Server) deferredConnectAttemptTimeout() time.Duration {
	timeout := s.socksConnectTimeout
	if timeout <= 0 {
		timeout = s.cfg.SOCKSConnectTimeout()
	}
	if timeout <= 0 {
		timeout = 8 * time.Second
	}
	if timeout > maxDeferredConnectAttemptTimeout {
		return maxDeferredConnectAttemptTimeout
	}
	return timeout
}

func (s *Server) processDeferredDNSQuery(ctx context.Context, sessionID uint16, sessionCookie uint8, sequenceNum uint16, downloadCompression uint8, downloadMTUBytes int, assembledQuery []byte) {
	if ctx != nil && ctx.Err() != nil {
		return
	}
	if !s.shouldExecuteDeferredPacket(VpnProto.Packet{SessionID: sessionID, SessionCookie: sessionCookie, StreamID: 0}) {
		return
	}

	if !s.sessions.HasActive(sessionID) {
		return
	}

	rawResponse := s.buildDNSQueryResponsePayload(assembledQuery, sessionID, sequenceNum)
	if len(rawResponse) == 0 {
		return
	}

	fragments := s.fragmentDNSResponsePayload(rawResponse, downloadMTUBytes)
	if len(fragments) == 0 {
		return
	}

	totalFragments := uint8(len(fragments))
	for fragmentID, fragmentPayload := range fragments {
		if ctx != nil && ctx.Err() != nil {
			return
		}
		if !s.shouldExecuteDeferredPacket(VpnProto.Packet{SessionID: sessionID, SessionCookie: sessionCookie, StreamID: 0}) {
			return
		}
		_ = s.queueMainSessionPacket(sessionID, VpnProto.Packet{
			PacketType:      Enums.PACKET_DNS_QUERY_RES,
			StreamID:        0,
			SequenceNum:     sequenceNum,
			FragmentID:      uint8(fragmentID),
			TotalFragments:  totalFragments,
			CompressionType: downloadCompression,
			Payload:         fragmentPayload,
		})
	}
}

func (s *Server) finalizeDeferredConnectStream(sessionID uint16, streamID uint16, kind string, outcome string) {
	if s == nil || sessionID == 0 || streamID == 0 {
		return
	}
	s.finalizeDeferredPacketsForStream(sessionID, streamID)
}

func (s *Server) processDeferredStreamSyn(ctx context.Context, vpnPacket VpnProto.Packet) {
	if ctx != nil && ctx.Err() != nil {
		return
	}
	if !s.shouldExecuteDeferredPacket(vpnPacket) {
		return
	}

	record, ok := s.sessions.Get(vpnPacket.SessionID)
	if !ok {
		return
	}

	if s.cfg.ForwardIP == "" || s.cfg.ForwardPort <= 0 {
		stream := record.getOrCreateStream(vpnPacket.StreamID, s.streamARQConfig(record.DownloadCompression), nil, s.log)
		if stream == nil || stream.ARQ == nil {
			record.enqueueOrphanReset(Enums.PACKET_STREAM_RST, vpnPacket.StreamID, 0)
			return
		}

		stream.ARQ.SendControlPacketWithTTL(
			Enums.PACKET_STREAM_CONNECT_FAIL,
			vpnPacket.SequenceNum,
			0,
			0,
			nil,
			Enums.DefaultPacketPriority(Enums.PACKET_STREAM_CONNECT_FAIL),
			true,
			nil,
			s.cfg.StreamFailurePacketTTL(),
		)
		s.finalizeDeferredConnectStream(vpnPacket.SessionID, vpnPacket.StreamID, "stream", "forward-disabled")
		return
	}

	stream := record.getOrCreateStream(vpnPacket.StreamID, s.streamARQConfig(record.DownloadCompression), nil, s.log)
	if stream == nil || stream.ARQ == nil {
		record.enqueueOrphanReset(Enums.PACKET_STREAM_RST, vpnPacket.StreamID, 0)
		return
	}
	s.maybeEnableStreamFEC(stream)

	stream.mu.RLock()
	alreadyConnected := stream.Connected && stream.TargetHost == s.cfg.ForwardIP && stream.TargetPort == uint16(s.cfg.ForwardPort)
	stream.mu.RUnlock()
	if alreadyConnected {
		stream.ARQ.SendControlPacketWithTTL(
			Enums.PACKET_STREAM_CONNECTED,
			vpnPacket.SequenceNum,
			0,
			0,
			nil,
			Enums.DefaultPacketPriority(Enums.PACKET_STREAM_CONNECTED),
			true,
			nil,
			s.cfg.StreamResultPacketTTL(),
		)
		s.finalizeDeferredConnectStream(vpnPacket.SessionID, vpnPacket.StreamID, "stream", "already-connected")
		return
	}

	if !s.shouldExecuteDeferredPacket(vpnPacket) {
		return
	}

	attemptTimeout := s.deferredConnectAttemptTimeout()
	attemptCtx, cancelAttempt := context.WithTimeout(ctx, attemptTimeout)
	defer cancelAttempt()

	upstreamConn, err := s.dialTCPTargetContext(attemptCtx, net.JoinHostPort(s.cfg.ForwardIP, strconv.Itoa(s.cfg.ForwardPort)))

	if err != nil {
		timedOut := errors.Is(attemptCtx.Err(), context.DeadlineExceeded)
		cancelled := ctx != nil && ctx.Err() != nil && !timedOut

		if cancelled {
			return
		}
		if !s.shouldExecuteDeferredPacket(vpnPacket) {
			return
		}
		stream.ARQ.SendControlPacketWithTTL(
			Enums.PACKET_STREAM_CONNECT_FAIL,
			vpnPacket.SequenceNum,
			0,
			0,
			nil,
			Enums.DefaultPacketPriority(Enums.PACKET_STREAM_CONNECT_FAIL),
			true,
			nil,
			s.cfg.StreamFailurePacketTTL(),
		)
		s.finalizeDeferredConnectStream(vpnPacket.SessionID, vpnPacket.StreamID, "stream", "dial-error")
		return
	}
	if upstreamConn == nil {
		if !s.shouldExecuteDeferredPacket(vpnPacket) {
			return
		}
		stream.ARQ.SendControlPacketWithTTL(
			Enums.PACKET_STREAM_CONNECT_FAIL,
			vpnPacket.SequenceNum,
			0,
			0,
			nil,
			Enums.DefaultPacketPriority(Enums.PACKET_STREAM_CONNECT_FAIL),
			true,
			nil,
			s.cfg.StreamFailurePacketTTL(),
		)
		s.finalizeDeferredConnectStream(vpnPacket.SessionID, vpnPacket.StreamID, "stream", "nil-conn")
		return
	}
	if ctx != nil && ctx.Err() != nil {
		_ = upstreamConn.Close()
		return
	}

	if record.isClosed() || !stream.attachUpstreamConn(upstreamConn, s.cfg.ForwardIP, uint16(s.cfg.ForwardPort), "CONNECTED") {
		_ = upstreamConn.Close()
		s.finalizeDeferredConnectStream(vpnPacket.SessionID, vpnPacket.StreamID, "stream", "attach-rejected")
		return
	}

	if !s.shouldExecuteDeferredPacket(vpnPacket) {
		_ = upstreamConn.Close()
		s.finalizeDeferredConnectStream(vpnPacket.SessionID, vpnPacket.StreamID, "stream", "stale-after-dial")
		return
	}

	stream.ARQ.SetLocalConn(upstreamConn)
	stream.ARQ.SendControlPacketWithTTL(
		Enums.PACKET_STREAM_CONNECTED,
		vpnPacket.SequenceNum,
		0,
		0,
		nil,
		Enums.DefaultPacketPriority(Enums.PACKET_STREAM_CONNECTED),
		true,
		nil,
		s.cfg.StreamResultPacketTTL(),
	)
	stream.ARQ.SetIOReady(true)
	s.finalizeDeferredConnectStream(vpnPacket.SessionID, vpnPacket.StreamID, "stream", "connected")
}

func (s *Server) processDeferredSOCKS5Syn(ctx context.Context, vpnPacket VpnProto.Packet) {
	if ctx != nil && ctx.Err() != nil {
		return
	}
	if !s.shouldExecuteDeferredPacket(vpnPacket) {
		return
	}

	record, ok := s.sessions.Get(vpnPacket.SessionID)
	if !ok {
		return
	}

	now := time.Now()
	totalFragments := vpnPacket.TotalFragments
	if totalFragments == 0 {
		totalFragments = 1
	}

	assembledTarget, ready, completed := s.collectSOCKS5SynFragments(
		vpnPacket.SessionID,
		vpnPacket.StreamID,
		vpnPacket.SequenceNum,
		vpnPacket.Payload,
		vpnPacket.FragmentID,
		totalFragments,
		now,
	)

	if completed || !ready {
		return
	}

	if !s.shouldExecuteDeferredPacket(vpnPacket) {
		return
	}

	stream := record.getOrCreateStream(vpnPacket.StreamID, s.streamARQConfig(record.DownloadCompression), nil, s.log)
	if stream == nil || stream.ARQ == nil {
		record.enqueueOrphanReset(Enums.PACKET_STREAM_RST, vpnPacket.StreamID, 0)
		s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)
		return
	}
	s.maybeEnableStreamFEC(stream)

	// A native UDP association uses the existing reliable stream machinery but
	// has no fixed destination: each length-framed datagram carries its own
	// SOCKS address. Older servers reject this reserved marker as an unsupported
	// address type, so compatibility failure is explicit rather than corrupting
	// traffic.
	if bytes.Equal(assembledTarget, udpframe.AssociationMarker) {
		stream.mu.RLock()
		alreadyConnected := stream.Connected
		stream.mu.RUnlock()
		if alreadyConnected {
			stream.ARQ.SendControlPacketWithTTL(
				Enums.PACKET_SOCKS5_CONNECTED,
				vpnPacket.SequenceNum, 0, 0, nil,
				Enums.DefaultPacketPriority(Enums.PACKET_SOCKS5_CONNECTED),
				true, nil, s.cfg.StreamResultPacketTTL(),
			)
			s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)
			return
		}

		association, associationErr := s.newUDPAssociationContext(ctx)
		if associationErr != nil {
			packetType := s.mapSOCKSConnectError(associationErr)
			stream.ARQ.SendControlPacketWithTTL(
				packetType,
				vpnPacket.SequenceNum, 0, 0, nil,
				Enums.DefaultPacketPriority(packetType),
				true, nil, s.cfg.StreamFailurePacketTTL(),
			)
			s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)
			return
		}
		if record.isClosed() || !stream.attachUpstreamConn(association, "udp-association", 0, "CONNECTED") {
			_ = association.Close()
			s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)
			return
		}
		stream.ARQ.SetLocalConn(association)
		stream.ARQ.SetIOReady(true)
		stream.ARQ.SendControlPacketWithTTL(
			Enums.PACKET_SOCKS5_CONNECTED,
			vpnPacket.SequenceNum, 0, 0, nil,
			Enums.DefaultPacketPriority(Enums.PACKET_SOCKS5_CONNECTED),
			true, nil, s.cfg.StreamResultPacketTTL(),
		)
		s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)
		return
	}

	target, err := SocksProto.ParseTargetPayload(assembledTarget)
	if err != nil {
		if !s.shouldExecuteDeferredPacket(vpnPacket) {
			return
		}
		packetType := uint8(Enums.PACKET_SOCKS5_CONNECT_FAIL)
		if errors.Is(err, SocksProto.ErrUnsupportedAddressType) || errors.Is(err, SocksProto.ErrInvalidDomainLength) {
			packetType = uint8(Enums.PACKET_SOCKS5_ADDRESS_TYPE_UNSUPPORTED)
		}

		stream.ARQ.SendControlPacketWithTTL(
			packetType,
			vpnPacket.SequenceNum,
			0,
			0,
			nil,
			Enums.DefaultPacketPriority(packetType),
			true,
			nil,
			s.cfg.StreamFailurePacketTTL(),
		)
		s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)
		return
	}

	stream.mu.RLock()
	prevConnected := stream.Connected
	prevHost := stream.TargetHost
	prevPort := stream.TargetPort
	stream.mu.RUnlock()

	if prevConnected {
		if prevHost == target.Host && prevPort == target.Port {
			if s.log != nil {
				s.log.Debugf("🧦 <green>SOCKS5_SYN Fast-Ack (Existing), Session: <cyan>%d</cyan> | Stream: <cyan>%d</cyan></green>", vpnPacket.SessionID, vpnPacket.StreamID)
			}

			stream.ARQ.SendControlPacketWithTTL(
				Enums.PACKET_SOCKS5_CONNECTED,
				vpnPacket.SequenceNum,
				0,
				0,
				nil,
				Enums.DefaultPacketPriority(Enums.PACKET_SOCKS5_CONNECTED),
				true,
				nil,
				s.cfg.StreamResultPacketTTL(),
			)
			s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)
			return
		}

		stream.ARQ.SendControlPacketWithTTL(
			Enums.PACKET_SOCKS5_CONNECT_FAIL,
			vpnPacket.SequenceNum,
			0,
			0,
			nil,
			Enums.DefaultPacketPriority(Enums.PACKET_SOCKS5_CONNECT_FAIL),
			true,
			nil,
			s.cfg.StreamFailurePacketTTL(),
		)
		s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)
		return
	}

	if !s.shouldExecuteDeferredPacket(vpnPacket) {
		return
	}

	attemptTimeout := s.deferredConnectAttemptTimeout()
	attemptCtx, cancelAttempt := context.WithTimeout(ctx, attemptTimeout)
	defer cancelAttempt()

	upstreamConn, err := s.dialSOCKSStreamTargetContext(attemptCtx, target.Host, target.Port, assembledTarget)

	if err != nil {
		timedOut := errors.Is(attemptCtx.Err(), context.DeadlineExceeded)
		cancelled := ctx != nil && ctx.Err() != nil && !timedOut

		if cancelled {
			return
		}

		if !s.shouldExecuteDeferredPacket(vpnPacket) {
			return
		}
		packetType := s.mapSOCKSConnectError(err)
		if s.log != nil {
			s.log.Debugf(
				"\U0001F9E6 <yellow>SOCKS5 Upstream Connect Failed</yellow> <magenta>|</magenta> <blue>Session</blue>: <cyan>%d</cyan> <magenta>|</magenta> <blue>Stream</blue>: <cyan>%d</cyan> <magenta>|</magenta> <blue>Target</blue>: <cyan>%s:%d</cyan> <magenta>|</magenta> <blue>Packet</blue>: <yellow>%s</yellow> <magenta>|</magenta> <cyan>%v</cyan>",
				vpnPacket.SessionID,
				vpnPacket.StreamID,
				target.Host,
				target.Port,
				Enums.PacketTypeName(packetType),
				err,
			)
		}
		stream.ARQ.SendControlPacketWithTTL(
			packetType,
			vpnPacket.SequenceNum,
			0,
			0,
			nil,
			Enums.DefaultPacketPriority(packetType),
			true,
			nil,
			s.cfg.StreamFailurePacketTTL(),
		)
		s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)

		return
	}
	if upstreamConn == nil {
		if !s.shouldExecuteDeferredPacket(vpnPacket) {
			return
		}

		packetType := uint8(Enums.PACKET_SOCKS5_CONNECT_FAIL)
		stream.ARQ.SendControlPacketWithTTL(
			packetType,
			vpnPacket.SequenceNum,
			0,
			0,
			nil,
			Enums.DefaultPacketPriority(packetType),
			true,
			nil,
			s.cfg.StreamFailurePacketTTL(),
		)
		s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)

		return
	}
	if ctx != nil && ctx.Err() != nil {
		_ = upstreamConn.Close()
		return
	}

	if record.isClosed() || !stream.attachUpstreamConn(upstreamConn, target.Host, target.Port, "CONNECTED") {
		_ = upstreamConn.Close()
		s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)
		return
	}

	if !s.shouldExecuteDeferredPacket(vpnPacket) {
		_ = upstreamConn.Close()
		s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)
		return
	}

	stream.ARQ.SetLocalConn(upstreamConn)
	stream.ARQ.SetIOReady(true)

	if s.log != nil {
		s.log.Debugf(
			"\U0001F9E6 <green>SOCKS5 Stream Prepared</green> <magenta>|</magenta> <blue>Session</blue>: <cyan>%d</cyan> <magenta>|</magenta> <blue>Stream</blue>: <cyan>%d</cyan> <magenta>|</magenta> <blue>Target</blue>: <cyan>%s:%d</cyan>",
			vpnPacket.SessionID,
			vpnPacket.StreamID,
			target.Host,
			target.Port,
		)
	}

	stream.ARQ.SendControlPacketWithTTL(
		Enums.PACKET_SOCKS5_CONNECTED,
		vpnPacket.SequenceNum,
		0,
		0,
		nil,
		Enums.DefaultPacketPriority(Enums.PACKET_SOCKS5_CONNECTED),
		true,
		nil,
		s.cfg.StreamResultPacketTTL(),
	)
	s.finalizeStreamArtifacts(vpnPacket.SessionID, vpnPacket.StreamID)
}

func (s *Server) mapSOCKSConnectError(err error) uint8 {
	if err == nil {
		return Enums.PACKET_SOCKS5_CONNECT_FAIL
	}

	if errors.Is(err, context.DeadlineExceeded) {
		return Enums.PACKET_SOCKS5_TTL_EXPIRED
	}

	var blockedErr *blockedSOCKSTargetError
	if errors.As(err, &blockedErr) {
		return Enums.PACKET_SOCKS5_RULESET_DENIED
	}

	var upstreamErr *upstreamSOCKS5Error
	if errors.As(err, &upstreamErr) {
		return upstreamErr.packetType
	}

	var dnsErr *net.DNSError
	if errors.As(err, &dnsErr) {
		return Enums.PACKET_SOCKS5_HOST_UNREACHABLE
	}

	var opErr *net.OpError
	if errors.As(err, &opErr) && opErr.Timeout() {
		return Enums.PACKET_SOCKS5_TTL_EXPIRED
	}

	var netErr net.Error
	if errors.As(err, &netErr) && netErr.Timeout() {
		return Enums.PACKET_SOCKS5_TTL_EXPIRED
	}

	message := strings.ToLower(err.Error())
	switch {
	case strings.Contains(message, "connection refused"):
		return Enums.PACKET_SOCKS5_CONNECTION_REFUSED
	case strings.Contains(message, "network is unreachable"):
		return Enums.PACKET_SOCKS5_NETWORK_UNREACHABLE
	case strings.Contains(message, "no route to host"),
		strings.Contains(message, "host is unreachable"),
		strings.Contains(message, "no such host"):
		return Enums.PACKET_SOCKS5_HOST_UNREACHABLE
	case strings.Contains(message, "i/o timeout"),
		strings.Contains(message, "timed out"):
		return Enums.PACKET_SOCKS5_TTL_EXPIRED
	default:
		return Enums.PACKET_SOCKS5_UPSTREAM_UNAVAILABLE
	}
}
