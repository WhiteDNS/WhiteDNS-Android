// ==============================================================================
// CottenDNS
// Author: tajirax
// Github: https://github.com/TaJirax/CottenDns
// Year: 2026
// ==============================================================================
package client

import (
	"testing"
	"time"

	"cottendns-go/internal/config"
	Enums "cottendns-go/internal/enums"
)

func buildTestClientWithResolvers(cfg config.ClientConfig, keys ...string) *Client {
	c := New(cfg, nil, nil)
	c.connections = make([]Connection, 0, len(keys))
	c.connectionsByKey = make(map[string]int, len(keys))
	c.active_streams = make(map[uint16]*Stream_client)

	for i, key := range keys {
		conn := Connection{
			Key:           key,
			Domain:        key + ".example.com",
			Resolver:      "127.0.0.1",
			ResolverPort:  5300 + i,
			ResolverLabel: "127.0.0.1:" + string(rune('0'+i)),
			IsValid:       true,
		}
		c.connections = append(c.connections, conn)
		c.connectionsByKey[key] = i
	}

	ptrs := make([]*Connection, len(c.connections))
	for i := range c.connections {
		ptrs[i] = &c.connections[i]
	}
	c.balancer.SetConnections(ptrs)
	return c
}

func testStream(id uint16) *Stream_client {
	return &Stream_client{
		StreamID: id,
		Status:   streamStatusActive,
	}
}

func TestSelectTargetConnectionsForPacketPrefersStickyStreamResolver(t *testing.T) {
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount: 2,
	}, "a", "b", "c")

	stream := testStream(7)
	stream.PreferredServerKey = "b"
	stream.LastResolverFailoverAt = time.Now()
	c.active_streams[stream.StreamID] = stream

	selected, err := c.selectTargetConnectionsForPacket(Enums.PACKET_STREAM_DATA, stream.StreamID)
	if err != nil {
		t.Fatalf("selectTargetConnectionsForPacket returned error: %v", err)
	}
	if len(selected) != 2 {
		t.Fatalf("unexpected selected count: got=%d want=2", len(selected))
	}
	if selected[0].Key != "b" {
		t.Fatalf("expected preferred resolver first, got=%q", selected[0].Key)
	}
}

func TestEnsureStreamPreferredConnectionSkipsRuntimeDisabledResolver(t *testing.T) {
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount: 1,
	}, "a", "b")

	stream := testStream(8)
	stream.PreferredServerKey = "a"
	c.active_streams[stream.StreamID] = stream

	c.resolverHealthMu.Lock()
	c.runtimeDisabled["a"] = resolverDisabledState{
		DisabledAt:  time.Now(),
		NextRetryAt: time.Now().Add(time.Minute),
		Cause:       "test",
	}
	c.resolverHealthMu.Unlock()

	selected, err := c.selectTargetConnectionsForPacket(Enums.PACKET_STREAM_DATA, stream.StreamID)
	if err != nil {
		t.Fatalf("selectTargetConnectionsForPacket returned error: %v", err)
	}
	if len(selected) != 1 {
		t.Fatalf("unexpected selected count: got=%d want=1", len(selected))
	}
	if selected[0].Key != "b" {
		t.Fatalf("expected runtime-disabled preferred resolver to be skipped, got=%q", selected[0].Key)
	}
	if stream.PreferredServerKey != "b" {
		t.Fatalf("expected preferred resolver to switch immediately, got=%q", stream.PreferredServerKey)
	}
}

func TestSelectTargetConnectionsForPacketFailsOverOnResendStreak(t *testing.T) {
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount:          1,
		StreamResolverFailoverResendThreshold: 1,
		StreamResolverFailoverCooldownSec:     0.1,
	}, "a", "b")

	stream := testStream(9)
	stream.PreferredServerKey = "a"
	stream.LastResolverFailoverAt = time.Now().Add(-2 * time.Second)
	c.active_streams[stream.StreamID] = stream

	selected, err := c.selectTargetConnectionsForPacket(Enums.PACKET_STREAM_RESEND, stream.StreamID)
	if err != nil {
		t.Fatalf("selectTargetConnectionsForPacket returned error: %v", err)
	}
	if len(selected) != 1 {
		t.Fatalf("unexpected selected count: got=%d want=1", len(selected))
	}
	if selected[0].Key != "b" {
		t.Fatalf("expected resend failover to switch preferred resolver, got=%q", selected[0].Key)
	}
}

func TestEnsureStreamPreferredConnectionDoesNotStartFailoverCooldown(t *testing.T) {
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount:          1,
		StreamResolverFailoverResendThreshold: 1,
		StreamResolverFailoverCooldownSec:     5.0,
	}, "a", "b")

	stream := testStream(12)
	c.active_streams[stream.StreamID] = stream

	selected, err := c.selectTargetConnectionsForPacket(Enums.PACKET_STREAM_DATA, stream.StreamID)
	if err != nil {
		t.Fatalf("selectTargetConnectionsForPacket returned error: %v", err)
	}
	if len(selected) != 1 {
		t.Fatalf("unexpected selected count: got=%d want=1", len(selected))
	}
	if selected[0].Key != "a" {
		t.Fatalf("expected initial preferred resolver to be a, got=%q", selected[0].Key)
	}
	if !stream.LastResolverFailoverAt.IsZero() {
		t.Fatal("expected initial preferred assignment to not stamp LastResolverFailoverAt")
	}

	selected, err = c.selectTargetConnectionsForPacket(Enums.PACKET_STREAM_RESEND, stream.StreamID)
	if err != nil {
		t.Fatalf("selectTargetConnectionsForPacket returned error: %v", err)
	}
	if len(selected) != 1 {
		t.Fatalf("unexpected selected count after resend: got=%d want=1", len(selected))
	}
	if selected[0].Key != "b" {
		t.Fatalf("expected immediate failover from initial preferred resolver, got=%q", selected[0].Key)
	}
	if stream.LastResolverFailoverAt.IsZero() {
		t.Fatal("expected real failover to stamp LastResolverFailoverAt")
	}
}

func TestSelectTargetConnectionsForPacketRespectsFailoverCooldown(t *testing.T) {
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount:          1,
		StreamResolverFailoverResendThreshold: 1,
		StreamResolverFailoverCooldownSec:     5.0,
	}, "a", "b")

	stream := testStream(10)
	stream.PreferredServerKey = "a"
	stream.LastResolverFailoverAt = time.Now()
	c.active_streams[stream.StreamID] = stream

	selected, err := c.selectTargetConnectionsForPacket(Enums.PACKET_STREAM_RESEND, stream.StreamID)
	if err != nil {
		t.Fatalf("selectTargetConnectionsForPacket returned error: %v", err)
	}
	if len(selected) != 1 {
		t.Fatalf("unexpected selected count: got=%d want=1", len(selected))
	}
	if selected[0].Key != "a" {
		t.Fatalf("expected cooldown to keep current preferred resolver, got=%q", selected[0].Key)
	}
}

func TestSelectTargetConnectionsForPacketUsesSetupDuplicationCount(t *testing.T) {
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount:      1,
		UploadSetupPacketDuplicationCount: 3,
	}, "a", "b", "c")

	selected, err := c.selectTargetConnectionsForPacket(Enums.PACKET_STREAM_SYN, 99)
	if err != nil {
		t.Fatalf("selectTargetConnectionsForPacket returned error: %v", err)
	}
	if len(selected) != 3 {
		t.Fatalf("unexpected selected count: got=%d want=3", len(selected))
	}
}

func TestSelectTargetConnectionsForPacketAppliesDuplicationCountToPing(t *testing.T) {
	// Both directional counts exceed 2 to verify the ping cap kicks in.
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount:   3,
		DownloadPacketDuplicationCount: 4,
	}, "a", "b", "c", "d")

	selected, err := c.selectTargetConnectionsForPacket(Enums.PACKET_PING, 42)
	if err != nil {
		t.Fatalf("packet %s: unexpected error: %v", Enums.PacketTypeName(Enums.PACKET_PING), err)
	}
	if len(selected) != 2 {
		t.Fatalf("packet %s: expected duplication count to be capped at 2, got=%d", Enums.PacketTypeName(Enums.PACKET_PING), len(selected))
	}
}

func TestNoteStreamProgressResetsResolverResendStreak(t *testing.T) {
	c := buildTestClientWithResolvers(config.ClientConfig{}, "a")

	stream := testStream(11)
	stream.ResolverResendStreak = 4
	c.active_streams[stream.StreamID] = stream

	c.noteStreamProgress(stream.StreamID)

	if stream.ResolverResendStreak != 0 {
		t.Fatalf("expected resend streak reset, got=%d", stream.ResolverResendStreak)
	}
}

func TestSelectAlternateStreamConnectionUsesBestScoredCandidate(t *testing.T) {
	c := buildTestClientWithResolvers(config.ClientConfig{
		ResolverBalancingStrategy: BalancingLowestLatency,
	}, "a", "b", "c")

	for i := 0; i < 6; i++ {
		c.balancer.ReportSend("a")
		c.balancer.ReportSuccess("a", 2*time.Millisecond)
		c.balancer.ReportSend("b")
		c.balancer.ReportSuccess("b", 6*time.Millisecond)
		c.balancer.ReportSend("c")
		c.balancer.ReportSuccess("c", 10*time.Millisecond)
	}

	selected, ok := c.selectAlternateStreamConnection("a")
	if !ok {
		t.Fatal("expected alternate connection")
	}
	if selected.Key != "b" {
		t.Fatalf("expected best candidate after excluding a to be b, got=%q", selected.Key)
	}
}

func TestRuntimePacketDuplicationCountDirectionalDispatch(t *testing.T) {
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount:        1,
		DownloadPacketDuplicationCount:      5,
		UploadSetupPacketDuplicationCount:   2,
		DownloadSetupPacketDuplicationCount: 6,
	}, "a")

	cases := []struct {
		name       string
		packetType uint8
		want       int
	}{
		{"upload data", Enums.PACKET_STREAM_DATA, 1},
		{"upload resend", Enums.PACKET_STREAM_RESEND, 1},
		{"download ack", Enums.PACKET_STREAM_DATA_ACK, 5},
		{"download nack", Enums.PACKET_STREAM_DATA_NACK, 5},
		{"upload setup syn", Enums.PACKET_STREAM_SYN, 2},
		{"upload setup socks5 syn", Enums.PACKET_SOCKS5_SYN, 2},
		{"download setup packed control", Enums.PACKET_PACKED_CONTROL_BLOCKS, 6},
		{"download setup close read", Enums.PACKET_STREAM_CLOSE_READ, 6},
		{"download setup close write", Enums.PACKET_STREAM_CLOSE_WRITE, 6},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			got := c.runtimePacketDuplicationCount(tc.packetType)
			if got != tc.want {
				t.Fatalf("packet %s: got=%d want=%d", Enums.PacketTypeName(tc.packetType), got, tc.want)
			}
		})
	}
}

func TestRuntimePacketDuplicationCountUsesDirectionalValues(t *testing.T) {
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount:        3,
		DownloadPacketDuplicationCount:      3,
		UploadSetupPacketDuplicationCount:   4,
		DownloadSetupPacketDuplicationCount: 4,
	}, "a", "b", "c", "d")

	if got := c.runtimePacketDuplicationCount(Enums.PACKET_STREAM_DATA); got != 3 {
		t.Fatalf("upload data: got=%d want=3", got)
	}
	if got := c.runtimePacketDuplicationCount(Enums.PACKET_STREAM_DATA_ACK); got != 3 {
		t.Fatalf("download ack: got=%d want=3", got)
	}
	if got := c.runtimePacketDuplicationCount(Enums.PACKET_STREAM_SYN); got != 4 {
		t.Fatalf("upload setup: got=%d want=4", got)
	}
	if got := c.runtimePacketDuplicationCount(Enums.PACKET_PACKED_CONTROL_BLOCKS); got != 4 {
		t.Fatalf("download setup: got=%d want=4", got)
	}
}

func TestRuntimePacketDuplicationCountPingUsesMaxDirectionCapped(t *testing.T) {
	// Ping should track the stronger of the two directional counts but stay
	// capped at 2.
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount:   1,
		DownloadPacketDuplicationCount: 5,
	}, "a", "b", "c", "d")

	if got := c.runtimePacketDuplicationCount(Enums.PACKET_PING); got != 2 {
		t.Fatalf("ping count: got=%d want=2 (capped)", got)
	}

	c2 := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount:   1,
		DownloadPacketDuplicationCount: 1,
	}, "a", "b")
	if got := c2.runtimePacketDuplicationCount(Enums.PACKET_PING); got != 1 {
		t.Fatalf("ping count low: got=%d want=1", got)
	}
}

func TestSelectTargetConnectionsForPacketUsesDownloadDuplicationForAcks(t *testing.T) {
	// With limited upload bandwidth, a user may want ACKs duplicated more
	// than data packets. Verify selectTargetConnectionsForPacket honors that.
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount:   1,
		DownloadPacketDuplicationCount: 3,
	}, "a", "b", "c")

	ackSelected, err := c.selectTargetConnectionsForPacket(Enums.PACKET_STREAM_DATA_ACK, 0)
	if err != nil {
		t.Fatalf("selectTargetConnectionsForPacket(ACK) error: %v", err)
	}
	if len(ackSelected) != 3 {
		t.Fatalf("ack selected count: got=%d want=3", len(ackSelected))
	}

	dataSelected, err := c.selectTargetConnectionsForPacket(Enums.PACKET_STREAM_DATA, 0)
	if err != nil {
		t.Fatalf("selectTargetConnectionsForPacket(DATA) error: %v", err)
	}
	if len(dataSelected) != 1 {
		t.Fatalf("data selected count: got=%d want=1", len(dataSelected))
	}
}

func TestRuntimePacketDuplicationCoordinatesWithRecentFEC(t *testing.T) {
	c := buildTestClientWithResolvers(config.ClientConfig{
		UploadPacketDuplicationCount:   1,
		DownloadPacketDuplicationCount: 6,
	}, "a", "b", "c", "d", "e", "f")
	c.lastFECReceived.Store(c.now().UnixNano())
	if got := c.runtimePacketDuplicationCount(Enums.PACKET_STREAM_DATA_ACK); got != 2 {
		t.Fatalf("download duplication with active FEC = %d, want 2", got)
	}
}
