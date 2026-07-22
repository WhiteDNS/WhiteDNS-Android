package client

import (
	"testing"
	"time"

	"cottendns-go/internal/config"
)

func TestRequestTransportRecoveryArmsFullRescanForExplicitTransport(t *testing.T) {
	c := &Client{
		cfg:                config.ClientConfig{ResolverTransport: "udp"},
		sessionResetSignal: make(chan struct{}, 1),
		successMTUChecks:   true,
		connections:        []Connection{{Key: "a", IsValid: true, UploadMTUBytes: 100, DownloadMTUBytes: 1000}},
	}
	c.requestTransportRecovery("test outage")
	if !c.transportRecoveryPending.Load() {
		t.Fatal("path recovery was not armed")
	}
	select {
	case <-c.sessionResetSignal:
	default:
		t.Fatal("session restart was not requested")
	}
	if !c.activatePendingTransportRecovery() {
		t.Fatal("pending recovery was not activated")
	}
	if c.successMTUChecks {
		t.Fatal("MTU discovery must be repeated after a live path outage")
	}
}

func TestRequestTransportRecoveryIsRateLimited(t *testing.T) {
	now := time.Now()
	c := &Client{
		cfg:                config.ClientConfig{ResolverTransport: "auto"},
		nowFn:              func() time.Time { return now },
		sessionResetSignal: make(chan struct{}, 2),
	}
	c.requestTransportRecovery("first")
	c.transportRecoveryPending.Store(false)
	c.clearRuntimeResetRequest()
	c.requestTransportRecovery("second")
	if got := c.transportRecoveryCount.Load(); got != 1 {
		t.Fatalf("recovery count = %d, want one fleet rescan inside cooldown", got)
	}
}

func TestSharedDoHHTTPClientIsReused(t *testing.T) {
	c := &Client{cfg: config.ClientConfig{}}
	first := c.sharedDoHHTTPClient()
	second := c.sharedDoHHTTPClient()
	if first != second {
		t.Fatal("DoH clients must share the HTTP/2 connection pool")
	}
	c.closeSharedDoHHTTPClient()
	if third := c.sharedDoHHTTPClient(); third == first {
		t.Fatal("closing the shared pool must allow a clean replacement")
	}
	c.closeSharedDoHHTTPClient()
}

func TestRuntimeDNSReadBufferIsSizedForConfiguredMTU(t *testing.T) {
	if got := runtimeDNSReadBufferSize(4000); got != runtimeDNSReadBufferFloor {
		t.Fatalf("default MTU buffer = %d, want cache-friendly floor %d", got, runtimeDNSReadBufferFloor)
	}
	if got := runtimeDNSReadBufferSize(20000); got != 22048 {
		t.Fatalf("large MTU buffer = %d, want MTU plus framing slack", got)
	}
	if got := runtimeDNSReadBufferSize(65535); got != RuntimeUDPReadBufferSize {
		t.Fatalf("oversize buffer = %d, want DNS framing ceiling", got)
	}
}
