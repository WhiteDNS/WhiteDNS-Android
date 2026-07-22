// ==============================================================================
// CottenDNS
// Author: tajirax
// Github: https://github.com/TaJirax/CottenDns
// Year: 2026
// ==============================================================================

package config

import (
	"flag"
	"os"
	"path/filepath"
	"testing"

	"cottendns-go/internal/compression"
)

func TestLoadClientConfigNormalizesAndLoadsResolvers(t *testing.T) {
	dir := t.TempDir()

	configPath := filepath.Join(dir, "client_config.toml")
	resolversPath := filepath.Join(dir, "client_resolvers.txt")

	if err := os.WriteFile(configPath, []byte(`
PROTOCOL_TYPE = "socks5"
DOMAINS = ["V.Domain.com", "v.domain.com."]
RESOLVER_BALANCING_STRATEGY = 2
BASE_ENCODE_DATA = true
DATA_ENCRYPTION_METHOD = 1
ENCRYPTION_KEY = "secret"
MIN_UPLOAD_MTU = 70
MIN_DOWNLOAD_MTU = 150
MAX_UPLOAD_MTU = 150
MAX_DOWNLOAD_MTU = 200
MTU_TEST_RETRIES_RESOLVERS = 2
MTU_TEST_TIMEOUT_RESOLVERS = 1.5
`), 0o644); err != nil {
		t.Fatalf("WriteFile config failed: %v", err)
	}

	if err := os.WriteFile(resolversPath, []byte(`
# comment
8.8.8.8
1.1.1.1:5353
`), 0o644); err != nil {
		t.Fatalf("WriteFile resolvers failed: %v", err)
	}

	cfg, err := LoadClientConfig(configPath)
	if err != nil {
		t.Fatalf("LoadClientConfig returned error: %v", err)
	}

	if cfg.ProtocolType != "SOCKS5" {
		t.Fatalf("unexpected protocol type: got=%q want=%q", cfg.ProtocolType, "SOCKS5")
	}
	if len(cfg.Domains) != 1 || cfg.Domains[0] != "v.domain.com" {
		t.Fatalf("unexpected domains: %+v", cfg.Domains)
	}
	if cfg.ResolverBalancingStrategy != 2 {
		t.Fatalf("unexpected resolver balancing strategy: got=%d want=%d", cfg.ResolverBalancingStrategy, 2)
	}
	if !cfg.BaseEncodeData {
		t.Fatalf("unexpected base encode flag: got=%v want=%v", cfg.BaseEncodeData, true)
	}
	if cfg.MTUTestTimeout != 1.5 {
		t.Fatalf("unexpected mtu timeout: got=%v want=%v", cfg.MTUTestTimeout, 1.5)
	}
	if cfg.ResolverMap["8.8.8.8"] != 53 {
		t.Fatalf("unexpected resolver port for 8.8.8.8: got=%d want=%d", cfg.ResolverMap["8.8.8.8"], 53)
	}
	if cfg.ResolverMap["1.1.1.1"] != 5353 {
		t.Fatalf("unexpected resolver port for 1.1.1.1: got=%d want=%d", cfg.ResolverMap["1.1.1.1"], 5353)
	}
}

func TestLoadClientConfigRejectsInvalidProtocol(t *testing.T) {
	dir := t.TempDir()

	configPath := filepath.Join(dir, "client_config.toml")
	resolversPath := filepath.Join(dir, "client_resolvers.txt")

	if err := os.WriteFile(configPath, []byte(`
PROTOCOL_TYPE = "udp"
DOMAINS = ["v.domain.com"]
DATA_ENCRYPTION_METHOD = 1
ENCRYPTION_KEY = "secret"
`), 0o644); err != nil {
		t.Fatalf("WriteFile config failed: %v", err)
	}
	if err := os.WriteFile(resolversPath, []byte("8.8.8.8\n"), 0o644); err != nil {
		t.Fatalf("WriteFile resolvers failed: %v", err)
	}

	if _, err := LoadClientConfig(configPath); err == nil {
		t.Fatal("LoadClientConfig should reject an invalid PROTOCOL_TYPE")
	}
}

func TestLoadClientConfigRejectsInvalidResolverBalancingStrategy(t *testing.T) {
	dir := t.TempDir()

	configPath := filepath.Join(dir, "client_config.toml")
	resolversPath := filepath.Join(dir, "client_resolvers.txt")

	if err := os.WriteFile(configPath, []byte(`
PROTOCOL_TYPE = "SOCKS5"
DOMAINS = ["v.domain.com"]
RESOLVER_BALANCING_STRATEGY = 8
DATA_ENCRYPTION_METHOD = 1
ENCRYPTION_KEY = "secret"
`), 0o644); err != nil {
		t.Fatalf("WriteFile config failed: %v", err)
	}
	if err := os.WriteFile(resolversPath, []byte("8.8.8.8\n"), 0o644); err != nil {
		t.Fatalf("WriteFile resolvers failed: %v", err)
	}

	if _, err := LoadClientConfig(configPath); err == nil {
		t.Fatal("LoadClientConfig should reject an invalid RESOLVER_BALANCING_STRATEGY")
	}
}

func TestLoadClientConfigAppliesDefaultsAndClamps(t *testing.T) {
	dir := t.TempDir()

	configPath := filepath.Join(dir, "client_config.toml")
	resolversPath := filepath.Join(dir, "client_resolvers.txt")

	if err := os.WriteFile(configPath, []byte(`
PROTOCOL_TYPE = "tcp"
DOMAINS = ["v.domain.com"]
LISTEN_IP = "  "
LOCAL_DNS_IP = ""
LOCAL_DNS_CACHE_MAX_RECORDS = 0
LOCAL_DNS_CACHE_TTL_SECONDS = 0
LOCAL_DNS_PENDING_TIMEOUT_SECONDS = 0
LOCAL_DNS_CACHE_FLUSH_INTERVAL_SECONDS = 0
COMPRESSION_MIN_SIZE = 0
MTU_TEST_RETRIES_RESOLVERS = 0
MTU_TEST_TIMEOUT_RESOLVERS = 0
MTU_TEST_PARALLELISM_RESOLVERS = 0
MTU_TEST_RETRIES_LOGS = 0
MTU_TEST_TIMEOUT_LOGS = 0
MTU_TEST_PARALLELISM_LOGS = 0
DATA_ENCRYPTION_METHOD = 1
ENCRYPTION_KEY = "secret"
`), 0o644); err != nil {
		t.Fatalf("WriteFile config failed: %v", err)
	}
	if err := os.WriteFile(resolversPath, []byte("8.8.8.8\n"), 0o644); err != nil {
		t.Fatalf("WriteFile resolvers failed: %v", err)
	}

	cfg, err := LoadClientConfig(configPath)
	if err != nil {
		t.Fatalf("LoadClientConfig returned error: %v", err)
	}

	if cfg.LocalDNSCacheMaxRecords != 2000 {
		t.Fatalf("unexpected local dns records default: got=%d want=%d", cfg.LocalDNSCacheMaxRecords, 2000)
	}
	if cfg.ARQInitialRTOSeconds != 0.6 || cfg.ARQMaxRTOSeconds != 3.0 {
		t.Fatalf("unexpected arq rto defaults: initial=%v max=%v", cfg.ARQInitialRTOSeconds, cfg.ARQMaxRTOSeconds)
	}
	if cfg.ARQDataNackMaxGap != 64 {
		t.Fatalf("unexpected ARQ data NACK gap default: got=%d want=64", cfg.ARQDataNackMaxGap)
	}
	if cfg.ARQDataNackRepeatSeconds != 0.8 {
		t.Fatalf("unexpected ARQ data NACK repeat default: got=%v want=%v", cfg.ARQDataNackRepeatSeconds, 0.8)
	}
	if cfg.ARQMaxControlRetries != 120 || cfg.ARQMaxDataRetries != 120 {
		t.Fatalf("unexpected arq retry defaults: control=%d data=%d", cfg.ARQMaxControlRetries, cfg.ARQMaxDataRetries)
	}
	if cfg.CompressionMinSize != compression.DefaultMinSize {
		t.Fatalf("unexpected compression min size default: got=%d want=%d", cfg.CompressionMinSize, compression.DefaultMinSize)
	}
	if cfg.MTUTestRetries != 3 || cfg.MTUTestTimeout != 2.0 || cfg.MTUTestParallelism != 100 {
		t.Fatalf("unexpected mtu defaults: retries=%d timeout=%v parallelism=%d", cfg.MTUTestRetries, cfg.MTUTestTimeout, cfg.MTUTestParallelism)
	}
	if cfg.ProtocolType != "TCP" {
		t.Fatal("tcp mode should be loaded")
	}
}

func TestLoadClientConfigAppliesPresetAndPreservesExplicitValues(t *testing.T) {
	dir := t.TempDir()

	configPath := filepath.Join(dir, "client_config.toml")
	resolversPath := filepath.Join(dir, "client_resolvers.txt")

	if err := os.WriteFile(configPath, []byte(`
CONFIG_PRESET = "speed"
PROTOCOL_TYPE = "SOCKS5"
DOMAINS = ["v.domain.com"]
ENCRYPTION_KEY = "secret"
UPLOAD_PACKET_DUPLICATION_COUNT = 2
QUERY_TYPES = ["TXT"]
`), 0o644); err != nil {
		t.Fatalf("WriteFile config failed: %v", err)
	}
	if err := os.WriteFile(resolversPath, []byte("8.8.8.8\n"), 0o644); err != nil {
		t.Fatalf("WriteFile resolvers failed: %v", err)
	}

	cfg, err := LoadClientConfig(configPath)
	if err != nil {
		t.Fatalf("LoadClientConfig returned error: %v", err)
	}

	if cfg.ConfigPreset != "speed" {
		t.Fatalf("unexpected preset: got=%q want=speed", cfg.ConfigPreset)
	}
	if cfg.ResolverBalancingStrategy != 5 || cfg.ResolverTransport != "auto" {
		t.Fatalf("speed preset did not apply path selection: strategy=%d transport=%q", cfg.ResolverBalancingStrategy, cfg.ResolverTransport)
	}
	if cfg.UploadPacketDuplicationCount != 2 {
		t.Fatalf("explicit upload duplication should win over preset, got %d", cfg.UploadPacketDuplicationCount)
	}
	if cfg.DownloadPacketDuplicationCount != 1 {
		t.Fatalf("speed preset download duplication = %d, want adaptive floor 1", cfg.DownloadPacketDuplicationCount)
	}
	if cfg.SessionInitRacingCount != 5 {
		t.Fatalf("speed preset session init racing count = %d, want 5", cfg.SessionInitRacingCount)
	}
	if !cfg.AdaptiveDuplication || cfg.MTUProbeSamples != 4 || cfg.MTUMaxLoss != 0.25 {
		t.Fatalf("speed preset loss controls not applied: adaptive=%v samples=%d maxLoss=%v", cfg.AdaptiveDuplication, cfg.MTUProbeSamples, cfg.MTUMaxLoss)
	}
	if len(cfg.QueryTypes) != 1 || cfg.QueryTypes[0] != "TXT" {
		t.Fatalf("explicit query types should win over preset, got %+v", cfg.QueryTypes)
	}
}

func TestLoadClientConfigAllowsUsernameOnlySocksAuth(t *testing.T) {
	dir := t.TempDir()

	configPath := filepath.Join(dir, "client_config.toml")
	resolversPath := filepath.Join(dir, "client_resolvers.txt")

	if err := os.WriteFile(configPath, []byte(`
PROTOCOL_TYPE = "SOCKS5"
DOMAINS = ["v.domain.com"]
SOCKS5_AUTH = true
SOCKS5_USER = "user_only"
SOCKS5_PASS = ""
DATA_ENCRYPTION_METHOD = 1
ENCRYPTION_KEY = "secret"
`), 0o644); err != nil {
		t.Fatalf("WriteFile config failed: %v", err)
	}
	if err := os.WriteFile(resolversPath, []byte("8.8.8.8\n"), 0o644); err != nil {
		t.Fatalf("WriteFile resolvers failed: %v", err)
	}

	cfg, err := LoadClientConfig(configPath)
	if err != nil {
		t.Fatalf("LoadClientConfig returned error: %v", err)
	}

	if !cfg.SOCKS5Auth || cfg.SOCKS5User != "user_only" || cfg.SOCKS5Pass != "" {
		t.Fatalf("unexpected socks auth config: auth=%v user=%q pass=%q", cfg.SOCKS5Auth, cfg.SOCKS5User, cfg.SOCKS5Pass)
	}
}

func TestLoadClientConfigAllowsShortAutoDisableWindowForQuickTesting(t *testing.T) {
	dir := t.TempDir()

	configPath := filepath.Join(dir, "client_config.toml")
	resolversPath := filepath.Join(dir, "client_resolvers.txt")

	if err := os.WriteFile(configPath, []byte(`
PROTOCOL_TYPE = "SOCKS5"
DOMAINS = ["v.domain.com"]
DATA_ENCRYPTION_METHOD = 1
ENCRYPTION_KEY = "secret"
AUTO_DISABLE_TIMEOUT_SERVERS = true
AUTO_DISABLE_TIMEOUT_WINDOW_SECONDS = 3.0
AUTO_DISABLE_MIN_OBSERVATIONS = 3
AUTO_DISABLE_CHECK_INTERVAL_SECONDS = 3.0
`), 0o644); err != nil {
		t.Fatalf("WriteFile config failed: %v", err)
	}
	if err := os.WriteFile(resolversPath, []byte("8.8.8.8\n"), 0o644); err != nil {
		t.Fatalf("WriteFile resolvers failed: %v", err)
	}

	cfg, err := LoadClientConfig(configPath)
	if err != nil {
		t.Fatalf("LoadClientConfig returned error: %v", err)
	}

	if cfg.AutoDisableTimeoutWindowSeconds != 3.0 {
		t.Fatalf("unexpected auto-disable timeout window: got=%v want=%v", cfg.AutoDisableTimeoutWindowSeconds, 3.0)
	}
	if cfg.AutoDisableCheckIntervalSeconds != 3.0 {
		t.Fatalf("unexpected auto-disable check interval: got=%v want=%v", cfg.AutoDisableCheckIntervalSeconds, 3.0)
	}
}

func TestLoadClientConfigUsesMergedRX_TX_Workers(t *testing.T) {
	dir := t.TempDir()

	configPath := filepath.Join(dir, "client_config.toml")
	resolversPath := filepath.Join(dir, "client_resolvers.txt")

	if err := os.WriteFile(configPath, []byte(`
PROTOCOL_TYPE = "SOCKS5"
DOMAINS = ["v.domain.com"]
DATA_ENCRYPTION_METHOD = 1
ENCRYPTION_KEY = "secret"
RX_TX_WORKERS = 9
TUNNEL_PROCESS_WORKERS = 2
`), 0o644); err != nil {
		t.Fatalf("WriteFile config failed: %v", err)
	}
	if err := os.WriteFile(resolversPath, []byte("8.8.8.8\n"), 0o644); err != nil {
		t.Fatalf("WriteFile resolvers failed: %v", err)
	}

	cfg, err := LoadClientConfig(configPath)
	if err != nil {
		t.Fatalf("LoadClientConfig returned error: %v", err)
	}

	if cfg.RX_TX_Workers != 9 {
		t.Fatalf("unexpected merged RX/TX workers: got=%d want=%d", cfg.RX_TX_Workers, 9)
	}
	if cfg.TunnelProcessWorkers != 9 {
		t.Fatalf("expected process workers to be raised to RX/TX count: got=%d want=%d", cfg.TunnelProcessWorkers, 9)
	}
}

func TestLoadClientConfigFallsBackToLegacyReaderWriterWorkers(t *testing.T) {
	dir := t.TempDir()

	configPath := filepath.Join(dir, "client_config.toml")
	resolversPath := filepath.Join(dir, "client_resolvers.txt")

	if err := os.WriteFile(configPath, []byte(`
PROTOCOL_TYPE = "SOCKS5"
DOMAINS = ["v.domain.com"]
DATA_ENCRYPTION_METHOD = 1
ENCRYPTION_KEY = "secret"
TUNNEL_READER_WORKERS = 3
TUNNEL_WRITER_WORKERS = 9
TUNNEL_PROCESS_WORKERS = 2
`), 0o644); err != nil {
		t.Fatalf("WriteFile config failed: %v", err)
	}
	if err := os.WriteFile(resolversPath, []byte("8.8.8.8\n"), 0o644); err != nil {
		t.Fatalf("WriteFile resolvers failed: %v", err)
	}

	cfg, err := LoadClientConfig(configPath)
	if err != nil {
		t.Fatalf("LoadClientConfig returned error: %v", err)
	}

	if cfg.RX_TX_Workers != 9 {
		t.Fatalf("expected legacy reader/writer workers to map into merged RX/TX workers: got=%d want=%d", cfg.RX_TX_Workers, 9)
	}
	if cfg.TunnelProcessWorkers != 9 {
		t.Fatalf("expected process workers to be raised to merged RX/TX count: got=%d want=%d", cfg.TunnelProcessWorkers, 9)
	}
}

func TestLoadClientConfigWithOverridesReplacesResolversDomainsAndMTURange(t *testing.T) {
	dir := t.TempDir()

	configPath := filepath.Join(dir, "client_config.toml")
	defaultResolversPath := filepath.Join(dir, "client_resolvers.txt")
	overrideResolversPath := filepath.Join(dir, "custom_resolvers.txt")

	if err := os.WriteFile(configPath, []byte(`
PROTOCOL_TYPE = "SOCKS5"
DOMAINS = ["config.domain.com"]
DATA_ENCRYPTION_METHOD = 1
ENCRYPTION_KEY = "secret"
MIN_UPLOAD_MTU = 40
MAX_UPLOAD_MTU = 64
MIN_DOWNLOAD_MTU = 100
MAX_DOWNLOAD_MTU = 140
`), 0o644); err != nil {
		t.Fatalf("WriteFile config failed: %v", err)
	}
	if err := os.WriteFile(defaultResolversPath, []byte("8.8.8.8\n"), 0o644); err != nil {
		t.Fatalf("WriteFile default resolvers failed: %v", err)
	}
	if err := os.WriteFile(overrideResolversPath, []byte("1.1.1.1:5353\n"), 0o644); err != nil {
		t.Fatalf("WriteFile override resolvers failed: %v", err)
	}

	minUp := 70
	maxUp := 90
	minDown := 180
	maxDown := 220
	cfg, err := LoadClientConfigWithOverrides(configPath, ClientConfigOverrides{
		ResolversFilePath: &overrideResolversPath,
		Values: map[string]any{
			"Domains":        []string{"a.example.com", "b.example.com."},
			"MinUploadMTU":   minUp,
			"MaxUploadMTU":   maxUp,
			"MinDownloadMTU": minDown,
			"MaxDownloadMTU": maxDown,
		},
	})
	if err != nil {
		t.Fatalf("LoadClientConfigWithOverrides returned error: %v", err)
	}

	if cfg.ResolversPath() != overrideResolversPath {
		t.Fatalf("unexpected overridden resolvers path: got=%q want=%q", cfg.ResolversPath(), overrideResolversPath)
	}
	if len(cfg.Domains) != 2 || cfg.Domains[0] != "a.example.com" || cfg.Domains[1] != "b.example.com" {
		t.Fatalf("unexpected overridden domains: %+v", cfg.Domains)
	}
	if cfg.ResolverMap["1.1.1.1"] != 5353 {
		t.Fatalf("unexpected override resolver map entry: got=%d want=%d", cfg.ResolverMap["1.1.1.1"], 5353)
	}
	if cfg.MinUploadMTU != minUp || cfg.MaxUploadMTU != maxUp || cfg.MinDownloadMTU != minDown || cfg.MaxDownloadMTU != maxDown {
		t.Fatalf("unexpected overridden MTU range: up=%d..%d down=%d..%d", cfg.MinUploadMTU, cfg.MaxUploadMTU, cfg.MinDownloadMTU, cfg.MaxDownloadMTU)
	}
}

func TestLoadClientConfigWithOverridesAppliesConfigPreset(t *testing.T) {
	dir := t.TempDir()

	configPath := filepath.Join(dir, "client_config.toml")
	resolversPath := filepath.Join(dir, "client_resolvers.txt")

	if err := os.WriteFile(configPath, []byte(`
PROTOCOL_TYPE = "SOCKS5"
DOMAINS = ["v.domain.com"]
DATA_ENCRYPTION_METHOD = 1
ENCRYPTION_KEY = "secret"
RESOLVER_TRANSPORT = "auto"
`), 0o644); err != nil {
		t.Fatalf("WriteFile config failed: %v", err)
	}
	if err := os.WriteFile(resolversPath, []byte("8.8.8.8\n"), 0o644); err != nil {
		t.Fatalf("WriteFile resolvers failed: %v", err)
	}

	cfg, err := LoadClientConfigWithOverrides(configPath, ClientConfigOverrides{
		Values: map[string]any{
			"ConfigPreset":                 "tcp",
			"UploadPacketDuplicationCount": 2,
		},
	})
	if err != nil {
		t.Fatalf("LoadClientConfigWithOverrides returned error: %v", err)
	}

	if cfg.ConfigPreset != "tcp-survival" || cfg.ResolverTransport != "tcp" {
		t.Fatalf("tcp preset override not applied: preset=%q transport=%q", cfg.ConfigPreset, cfg.ResolverTransport)
	}
	if cfg.UploadPacketDuplicationCount != 2 {
		t.Fatalf("explicit override should win over preset, got %d", cfg.UploadPacketDuplicationCount)
	}
	if cfg.DownloadPacketDuplicationCount != 2 {
		t.Fatalf("tcp preset download duplication = %d, want 2", cfg.DownloadPacketDuplicationCount)
	}
}

func TestClientConfigFlagBinderBuildsOverridesForSetFlagsOnly(t *testing.T) {
	fs := flag.NewFlagSet("client", flag.ContinueOnError)
	binder, err := NewClientConfigFlagBinder(fs)
	if err != nil {
		t.Fatalf("NewClientConfigFlagBinder returned error: %v", err)
	}

	if err := fs.Parse([]string{
		"-domains=a.example.com,b.example.com",
		"-min-upload-mtu=70",
		"-max-download-mtu=220",
		"-encryption-key=override-secret",
		"-base-encode-data",
	}); err != nil {
		t.Fatalf("flag parse failed: %v", err)
	}

	overrides := binder.Overrides()
	if got, ok := overrides.Values["MinUploadMTU"].(int); !ok || got != 70 {
		t.Fatalf("unexpected min upload mtu override: %#v", overrides.Values["MinUploadMTU"])
	}
	if got, ok := overrides.Values["MaxDownloadMTU"].(int); !ok || got != 220 {
		t.Fatalf("unexpected max download mtu override: %#v", overrides.Values["MaxDownloadMTU"])
	}
	if got, ok := overrides.Values["EncryptionKey"].(string); !ok || got != "override-secret" {
		t.Fatalf("unexpected encryption key override: %#v", overrides.Values["EncryptionKey"])
	}
	if got, ok := overrides.Values["BaseEncodeData"].(bool); !ok || !got {
		t.Fatalf("unexpected base encode override: %#v", overrides.Values["BaseEncodeData"])
	}
	gotDomains, ok := overrides.Values["Domains"].([]string)
	if !ok || len(gotDomains) != 2 || gotDomains[0] != "a.example.com" || gotDomains[1] != "b.example.com" {
		t.Fatalf("unexpected domains override: %#v", overrides.Values["Domains"])
	}
	if _, exists := overrides.Values["MaxUploadMTU"]; exists {
		t.Fatalf("did not expect unset flag to appear in overrides: %#v", overrides.Values["MaxUploadMTU"])
	}
}
