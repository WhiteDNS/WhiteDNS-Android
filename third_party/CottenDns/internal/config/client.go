// ==============================================================================
// CottenDNS
// Author: tajirax
// Github: https://github.com/TaJirax/CottenDns
// Year: 2026
// ==============================================================================

package config

import (
	"flag"
	"fmt"
	"os"
	"path/filepath"
	"reflect"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/BurntSushi/toml"

	"cottendns-go/internal/compression"
	Enums "cottendns-go/internal/enums"
)

type ClientConfig struct {
	ConfigDir                 string   `toml:"-"`
	ConfigPath                string   `toml:"-"`
	ResolversFilePath         string   `toml:"-"`
	explicitRX_TX_Workers     bool     `toml:"-"`
	ConfigPreset              string   `toml:"CONFIG_PRESET"`
	ProtocolType              string   `toml:"PROTOCOL_TYPE"`
	Domains                   []string `toml:"DOMAINS"`
	ListenIP                  string   `toml:"LISTEN_IP"`
	ListenPort                int      `toml:"LISTEN_PORT"`
	SOCKS5Auth                bool     `toml:"SOCKS5_AUTH"`
	SOCKS5User                string   `toml:"SOCKS5_USER"`
	SOCKS5Pass                string   `toml:"SOCKS5_PASS"`
	LocalDNSEnabled           bool     `toml:"LOCAL_DNS_ENABLED"`
	LocalDNSIP                string   `toml:"LOCAL_DNS_IP"`
	LocalDNSPort              int      `toml:"LOCAL_DNS_PORT"`
	LocalDNSCacheMaxRecords   int      `toml:"LOCAL_DNS_CACHE_MAX_RECORDS"`
	LocalDNSCacheTTLSeconds   float64  `toml:"LOCAL_DNS_CACHE_TTL_SECONDS"`
	LocalDNSPendingTimeoutSec float64  `toml:"LOCAL_DNS_PENDING_TIMEOUT_SECONDS"`
	LocalDNSCachePersist      bool     `toml:"LOCAL_DNS_CACHE_PERSIST_TO_FILE"`
	LocalDNSCacheFlushSec     float64  `toml:"LOCAL_DNS_CACHE_FLUSH_INTERVAL_SECONDS"`
	ResolverBalancingStrategy int      `toml:"RESOLVER_BALANCING_STRATEGY"`
	// QNameLabelLength is the target maximum DNS label length when laying the
	// tunnel payload into the query name (1..63). The default 63 packs the payload
	// into the fewest, longest labels (max capacity); a smaller value produces
	// shorter, more numerous labels that look less like a classic DNS tunnel, at
	// the cost of some payload capacity per query. Server-transparent (the server
	// reassembles the labels regardless of how they are split).
	QNameLabelLength int `toml:"QNAME_LABEL_LENGTH"`
	// ResolverRateLimitEnabled turns on per-resolver adaptive pacing: a resolver
	// that signals overload (RCODE != 0 or repeated timeouts) is briefly cooled
	// down and deprioritized so its load shifts to resolvers with headroom. It is
	// self-gating (does nothing to healthy resolvers) and never idles the client.
	// Default true.
	ResolverRateLimitEnabled bool `toml:"RESOLVER_RATE_LIMIT_ENABLED"`
	// ResolverTransport selects how DNS queries reach resolvers:
	//   "auto" (default) — probe over UDP first; if no resolver passes MTU
	//                       testing, retry the whole fleet over TCP/53.
	//   "udp"            — UDP only (legacy).
	//   "tcp"            — TCP/53 only (for networks that block UDP/53).
	ResolverTransport                     string  `toml:"RESOLVER_TRANSPORT"`
	// Encrypted-resolver settings, used only by the dot/doh transports.
	// ResolverTLSServerName is the SNI + certificate name presented to the
	// resolver (leave empty to use the resolver IP itself). ResolverTLSPin is an
	// optional base64 SHA-256 of the server certificate's SubjectPublicKeyInfo:
	// set it to trust a self-signed server without disabling verification.
	ResolverTLSServerName                 string  `toml:"RESOLVER_TLS_SERVER_NAME"`
	ResolverTLSPin                        string  `toml:"RESOLVER_TLS_PIN"`
	ResolverTLSInsecureSkipVerify         bool    `toml:"RESOLVER_TLS_INSECURE_SKIP_VERIFY"`
	ResolverDoTPort                       int     `toml:"RESOLVER_DOT_PORT"`
	ResolverDoHPort                       int     `toml:"RESOLVER_DOH_PORT"`
	ResolverDoHPath                       string  `toml:"RESOLVER_DOH_PATH"`
	UploadPacketDuplicationCount          int     `toml:"UPLOAD_PACKET_DUPLICATION_COUNT"`
	DownloadPacketDuplicationCount        int     `toml:"DOWNLOAD_PACKET_DUPLICATION_COUNT"`
	UploadSetupPacketDuplicationCount     int     `toml:"UPLOAD_SETUP_PACKET_DUPLICATION_COUNT"`
	DownloadSetupPacketDuplicationCount   int     `toml:"DOWNLOAD_SETUP_PACKET_DUPLICATION_COUNT"`
	StreamResolverFailoverResendThreshold int     `toml:"STREAM_RESOLVER_FAILOVER_RESEND_THRESHOLD"`
	StreamResolverFailoverCooldownSec     float64 `toml:"STREAM_RESOLVER_FAILOVER_COOLDOWN"`
	RecheckInactiveServersEnabled         bool    `toml:"RECHECK_INACTIVE_SERVERS_ENABLED"`
	RecheckInactiveIntervalSeconds        float64 `toml:"RECHECK_INACTIVE_INTERVAL_SECONDS"`
	RecheckServerIntervalSeconds          float64 `toml:"RECHECK_SERVER_INTERVAL_SECONDS"`
	RecheckBatchSize                      int     `toml:"RECHECK_BATCH_SIZE"`
	AutoDisableTimeoutServers             bool    `toml:"AUTO_DISABLE_TIMEOUT_SERVERS"`
	AutoDisableTimeoutWindowSeconds       float64 `toml:"AUTO_DISABLE_TIMEOUT_WINDOW_SECONDS"`
	AutoDisableMinObservations            int     `toml:"AUTO_DISABLE_MIN_OBSERVATIONS"`
	AutoDisableCheckIntervalSeconds       float64 `toml:"AUTO_DISABLE_CHECK_INTERVAL_SECONDS"`
	BaseEncodeData                        bool    `toml:"BASE_ENCODE_DATA"`
	// LegacySessionID selects the 1-byte on-wire session-ID format used by
	// MasterDNS/StormDNS/WhiteDNS servers. Default false uses CottenDns's 2-byte
	// native format. Must match the target server's engine generation.
	LegacySessionID bool `toml:"LEGACY_SESSION_ID"`
	// MaxActiveStreams caps concurrent local tunnel streams (0 = unlimited).
	MaxActiveStreams int `toml:"MAX_ACTIVE_STREAMS"`
	// LocalHandshakeTimeoutSeconds bounds the local SOCKS5/TCP client handshake.
	LocalHandshakeTimeoutSeconds float64 `toml:"LOCAL_HANDSHAKE_TIMEOUT_SECONDS"`
	UploadCompressionType        int     `toml:"UPLOAD_COMPRESSION_TYPE"`
	DownloadCompressionType      int     `toml:"DOWNLOAD_COMPRESSION_TYPE"`
	CompressionMinSize           int     `toml:"COMPRESSION_MIN_SIZE"`
	DataEncryptionMethod         int     `toml:"DATA_ENCRYPTION_METHOD"`
	EncryptionKey                string  `toml:"ENCRYPTION_KEY"`
	MinUploadMTU                 int     `toml:"MIN_UPLOAD_MTU"`
	MinDownloadMTU               int     `toml:"MIN_DOWNLOAD_MTU"`
	MaxUploadMTU                 int     `toml:"MAX_UPLOAD_MTU"`
	MaxDownloadMTU               int     `toml:"MAX_DOWNLOAD_MTU"`
	MTUTestRetriesResolvers      int     `toml:"MTU_TEST_RETRIES_RESOLVERS"`
	MTUTestRetriesLogs           int     `toml:"MTU_TEST_RETRIES_LOGS"`
	MTUTestTimeoutResolvers      float64 `toml:"MTU_TEST_TIMEOUT_RESOLVERS"`
	MTUTestTimeoutLogs           float64 `toml:"MTU_TEST_TIMEOUT_LOGS"`
	MTUTestParallelismResolvers  int     `toml:"MTU_TEST_PARALLELISM_RESOLVERS"`
	MTUTestParallelismLogs       int     `toml:"MTU_TEST_PARALLELISM_LOGS"`
	// Adaptive per-group MTU (loss-aware probing + clustering).
	// MTUProbeSamples > 1 enables loss-aware probing: each candidate MTU is
	// probed this many times and accepted only if its measured loss is at or
	// below MTUMaxLoss (instead of the legacy "pass if any retry succeeds").
	// =1 (default) keeps the legacy retry behavior unchanged.
	MTUProbeSamples int     `toml:"MTU_PROBE_SAMPLES"`
	MTUMaxLoss      float64 `toml:"MTU_MAX_LOSS"`
	// MTUGroupGapRatio controls resolver clustering by viable MTU: a new group
	// starts wherever the gap between consecutive sorted download MTUs exceeds
	// this fraction of the larger value (e.g. 0.25 = 25%).
	MTUGroupGapRatio float64 `toml:"MTU_GROUP_GAP_RATIO"`
	// MTUAdaptiveGrouping, when true (default), raises the session MTU to the
	// throughput-optimal operating point — the MTU D that maximizes D × (number
	// of resolvers that can sustain D) — and holds resolvers that cannot sustain
	// it in a backup tier (kept valid and used only as failover when the active
	// pool is exhausted). When false, the legacy global-minimum MTU across all
	// valid resolvers is used.
	MTUAdaptiveGrouping bool `toml:"MTU_ADAPTIVE_GROUPING"`
	// MTUWeightedMinPool applies only when RESOLVER_BALANCING_STRATEGY is
	// MTU-weighted (5). In that mode the session runs at the *highest* MTU a viable
	// subset of resolvers can sustain — instead of the throughput-optimal common
	// denominator — so resolvers that support a higher MTU actually use it. This is
	// the minimum number of resolvers that must sustain the raised MTU before it is
	// adopted, trading a little redundancy for the larger MTU. Default 2.
	MTUWeightedMinPool int `toml:"MTU_WEIGHTED_MIN_POOL"`
	// FastConnect allows startup to continue after a small safe pool has passed
	// MTU probing. The remaining resolvers keep probing in the background and are
	// folded into the active/reserve pool as they pass.
	FastConnect bool `toml:"FAST_CONNECT"`
	// Active MTU test parameters resolved from the startup mode at runtime.
	// Populated by ApplyStartupModeMTU after the mode is known. Not loaded from TOML.
	MTUTestRetries                       int               `toml:"-"`
	MTUTestTimeout                       float64           `toml:"-"`
	MTUTestParallelism                   int               `toml:"-"`
	RX_TX_Workers                        int               `toml:"RX_TX_WORKERS"`
	LegacyTunnelReaderWorkers            int               `toml:"TUNNEL_READER_WORKERS"`
	LegacyTunnelWriterWorkers            int               `toml:"TUNNEL_WRITER_WORKERS"`
	TunnelProcessWorkers                 int               `toml:"TUNNEL_PROCESS_WORKERS"`
	TunnelPacketTimeoutSec               float64           `toml:"TUNNEL_PACKET_TIMEOUT_SECONDS"`
	DispatcherIdlePollIntervalSeconds    float64           `toml:"DISPATCHER_IDLE_POLL_INTERVAL_SECONDS"`
	PingAggressiveIntervalSeconds        float64           `toml:"PING_AGGRESSIVE_INTERVAL_SECONDS"`
	PingLazyIntervalSeconds              float64           `toml:"PING_LAZY_INTERVAL_SECONDS"`
	PingCooldownIntervalSeconds          float64           `toml:"PING_COOLDOWN_INTERVAL_SECONDS"`
	PingColdIntervalSeconds              float64           `toml:"PING_COLD_INTERVAL_SECONDS"`
	PingWarmThresholdSeconds             float64           `toml:"PING_WARM_THRESHOLD_SECONDS"`
	PingCoolThresholdSeconds             float64           `toml:"PING_COOL_THRESHOLD_SECONDS"`
	PingColdThresholdSeconds             float64           `toml:"PING_COLD_THRESHOLD_SECONDS"`
	PingWatchdogTimeoutSeconds           float64           `toml:"PING_WATCHDOG_TIMEOUT_SECONDS"`
	TXChannelSize                        int               `toml:"TX_CHANNEL_SIZE"`
	RXChannelSize                        int               `toml:"RX_CHANNEL_SIZE"`
	ResolverUDPConnectionPoolSize        int               `toml:"RESOLVER_UDP_CONNECTION_POOL_SIZE"`
	StreamQueueInitialCapacity           int               `toml:"STREAM_QUEUE_INITIAL_CAPACITY"`
	OrphanQueueInitialCapacity           int               `toml:"ORPHAN_QUEUE_INITIAL_CAPACITY"`
	DNSResponseFragmentStoreCap          int               `toml:"DNS_RESPONSE_FRAGMENT_STORE_CAPACITY"`
	DNSResponseFragmentTimeoutSeconds    float64           `toml:"DNS_RESPONSE_FRAGMENT_TIMEOUT_SECONDS"`
	SOCKSUDPAssociateReadTimeoutSeconds  float64           `toml:"SOCKS_UDP_ASSOCIATE_READ_TIMEOUT_SECONDS"`
	ClientTerminalStreamRetentionSeconds float64           `toml:"CLIENT_TERMINAL_STREAM_RETENTION_SECONDS"`
	ClientCancelledSetupRetentionSeconds float64           `toml:"CLIENT_CANCELLED_SETUP_RETENTION_SECONDS"`
	SessionInitRetryBaseSeconds          float64           `toml:"SESSION_INIT_RETRY_BASE_SECONDS"`
	SessionInitRetryStepSeconds          float64           `toml:"SESSION_INIT_RETRY_STEP_SECONDS"`
	SessionInitRetryLinearAfter          int               `toml:"SESSION_INIT_RETRY_LINEAR_AFTER"`
	SessionInitRetryMaxSeconds           float64           `toml:"SESSION_INIT_RETRY_MAX_SECONDS"`
	SessionInitBusyRetryIntervalSeconds  float64           `toml:"SESSION_INIT_BUSY_RETRY_INTERVAL_SECONDS"`
	// SessionInitRacingCount is how many distinct resolvers one SESSION_INIT is
	// raced across. The server keys sessions by the init signature and reuses
	// within SESSION_INIT_REUSE_TTL, so the same init arriving via several
	// resolvers still yields exactly one session. Higher values connect faster
	// on lossy networks where any single resolver may be dead, at the cost of
	// that many queries per attempt. 1 disables racing.
	SessionInitRacingCount int `toml:"SESSION_INIT_RACING_COUNT"`
	LogLevel                             string            `toml:"LOG_LEVEL"`
	LogToFile                            bool              `toml:"LOG_TO_FILE"`
	LogDir                               string            `toml:"LOG_DIR"`
	LogFileName                          string            `toml:"LOG_FILE_NAME"`
	StatsReportIntervalSeconds           float64           `toml:"STATS_REPORT_INTERVAL_SECONDS"`
	StartupMode                          string            `toml:"STARTUP_MODE"`
	LogScanMaxDays                       int               `toml:"LOG_SCAN_MAX_DAYS"`
	LogScanMaxResolvers                  int               `toml:"LOG_SCAN_MAX_RESOLVERS"`
	LogBasedMTUVerify                    bool              `toml:"LOG_BASED_MTU_VERIFY"`
	MaxPacketsPerBatch                   int               `toml:"MAX_PACKETS_PER_BATCH"`
	ARQWindowSize                        int               `toml:"ARQ_WINDOW_SIZE"`
	ARQInitialRTOSeconds                 float64           `toml:"ARQ_INITIAL_RTO_SECONDS"`
	ARQMaxRTOSeconds                     float64           `toml:"ARQ_MAX_RTO_SECONDS"`
	ARQControlInitialRTOSeconds          float64           `toml:"ARQ_CONTROL_INITIAL_RTO_SECONDS"`
	ARQControlMaxRTOSeconds              float64           `toml:"ARQ_CONTROL_MAX_RTO_SECONDS"`
	ARQMaxControlRetries                 int               `toml:"ARQ_MAX_CONTROL_RETRIES"`
	ARQInactivityTimeoutSeconds          float64           `toml:"ARQ_INACTIVITY_TIMEOUT_SECONDS"`
	ARQDataPacketTTLSeconds              float64           `toml:"ARQ_DATA_PACKET_TTL_SECONDS"`
	ARQControlPacketTTLSeconds           float64           `toml:"ARQ_CONTROL_PACKET_TTL_SECONDS"`
	ARQMaxDataRetries                    int               `toml:"ARQ_MAX_DATA_RETRIES"`
	ARQDataNackMaxGap                    int               `toml:"ARQ_DATA_NACK_MAX_GAP"`
	ARQDataNackInitialDelaySeconds       float64           `toml:"ARQ_DATA_NACK_INITIAL_DELAY_SECONDS"`
	ARQDataNackRepeatSeconds             float64           `toml:"ARQ_DATA_NACK_REPEAT_SECONDS"`
	ARQTerminalDrainTimeoutSec           float64           `toml:"ARQ_TERMINAL_DRAIN_TIMEOUT_SECONDS"`
	ARQTerminalAckWaitTimeoutSec         float64           `toml:"ARQ_TERMINAL_ACK_WAIT_TIMEOUT_SECONDS"`
	Resolvers                            []ResolverAddress `toml:"-"`
	ResolverMap                          map[string]int    `toml:"-"`
	// QUERY_TYPES is the set of DNS record types the client rotates over when
	// building tunnel queries (A1, DPI-resistance). Names are case-insensitive,
	// e.g. ["TXT", "CNAME", "A", "AAAA"]. Empty / unset preserves the historical
	// behavior of TXT-only. Unknown names are rejected at load time.
	QueryTypes []string `toml:"QUERY_TYPES"`
	// QueryTypeCodes is the validated numeric form of QueryTypes, always
	// non-empty after finalizeClientConfig (defaults to [TXT]).
	QueryTypeCodes []uint16 `toml:"-"`
	// DUPLICATION_PREFER_DISTINCT_DOMAINS biases packet duplication (A6) so the
	// duplicate copies of a packet traverse distinct tunnel domains where
	// possible, for independent-path delivery on lossy links and signature
	// spread. Default false preserves the historical resolver-only selection.
	DuplicationPreferDistinctDomains bool `toml:"DUPLICATION_PREFER_DISTINCT_DOMAINS"`
	// ADAPTIVE_DUPLICATION (tier 1 loss reducer): when true, the per-packet
	// duplication count is scaled up from the configured base toward the count
	// needed to hit ADAPTIVE_DUPLICATION_TARGET_DELIVERY given the measured loss,
	// clamped to the duplication ceiling (8). Default false.
	AdaptiveDuplication bool `toml:"ADAPTIVE_DUPLICATION"`
	// ADAPTIVE_DUPLICATION_TARGET_DELIVERY is the per-packet delivery probability
	// adaptive duplication aims for (0.5..0.999). Default 0.95.
	AdaptiveDuplicationTargetDelivery float64 `toml:"ADAPTIVE_DUPLICATION_TARGET_DELIVERY"`

	// --- DNS query-shaping knobs (client-only, server-transparent) ---
	// None of the following require a server update: the server echoes the DNS
	// transaction ID without validating it, lowercases the whole QNAME before
	// decoding, and (for the EDNS cookie) never even sees it because the recursive
	// resolver terminates EDNS on the client->resolver leg.

	// DNS_RANDOMIZE_QUERY_ID randomizes the DNS transaction ID of every tunnel
	// query instead of using a sequential counter. Real stub resolvers randomize
	// the ID per query (anti-spoofing); a monotonic sequence to one zone is a
	// tunnel tell and a mild spoofing weakness. Default true.
	DNSRandomizeQueryID bool `toml:"DNS_RANDOMIZE_QUERY_ID"`
	// DNS_EDNS_COOKIE adds an RFC 7873 EDNS Client Cookie (8 random bytes) to the
	// OPT record of each tunnel query so the query looks like a modern stub
	// resolver on the client->resolver leg (where restrictive-network DPI observes
	// it). The recursive resolver terminates EDNS, so the cookie never reaches the
	// CottenDNS server. Default true.
	DNSEDNSCookie bool `toml:"DNS_EDNS_COOKIE"`
	// DNS_QNAME_CASE_RANDOMIZATION applies DNS 0x20 mixed-case encoding to the
	// query name (random per-letter case). The server lowercases the whole QNAME
	// before decoding, so it is server-transparent and can never desync. It makes
	// the all-lowercase base32 payload look less uniform but does NOT reduce label
	// entropy, so its anti-detection value is modest. Default false (opt-in).
	DNSQNameCaseRandomization bool `toml:"DNS_QNAME_CASE_RANDOMIZATION"`
	// EDNS_UDP_SIZE is the requestor's UDP payload size advertised in the OPT
	// record (bytes). Larger values let resolvers return larger answers (needed for
	// high download MTU / throughput); a smaller value (e.g. 1232) looks more like
	// a modern stub but can cap the answer size. Default 4096, clamped to
	// [512, 4096] (unset falls back to 4096).
	EDNSUDPSize int `toml:"EDNS_UDP_SIZE"`

	// RESOLVER_IGNORE_INJECTED_NXDOMAIN hardens the client against on-path DNS
	// poisoning. Restrictive networks race a forged NXDOMAIN back to the client
	// while the real authoritative answer still arrives, so the resolver stays
	// usable — but counting that forgery as a failure lets the censor throttle
	// and disable perfectly working resolvers for free (and thus slow you down).
	// When true (default), a NXDOMAIN response carrying no tunnel payload is
	// treated as injection noise: it does not throttle, disable, or otherwise
	// penalize the resolver. Genuine unreachability is still caught by the one
	// signal injection cannot forge — the query times out with no real answer.
	// Costs nothing (no extra queries or bytes); it only stops the client from
	// punishing resolvers the censor is forging failures for. Set false to
	// restore the legacy behavior of counting every NXDOMAIN as a failure.
	ResolverIgnoreInjectedNXDOMAIN bool `toml:"RESOLVER_IGNORE_INJECTED_NXDOMAIN"`
}

type ClientConfigOverrides struct {
	ResolversFilePath *string
	Values            map[string]any
	// Resolvers, when non-nil, replaces the resolvers loaded from the resolvers file.
	Resolvers []ResolverAddress
}

type ClientConfigFlagBinder struct {
	values      ClientConfig
	setFields   map[string]struct{}
	flagToField map[string]string
}

func defaultClientConfig() ClientConfig {
	return ClientConfig{
		ConfigPreset:                          "default",
		ProtocolType:                          "SOCKS5",
		Domains:                               nil,
		ListenIP:                              "127.0.0.1",
		ListenPort:                            18000,
		SOCKS5Auth:                            false,
		SOCKS5User:                            "master_dns_vpn",
		SOCKS5Pass:                            "master_dns_vpn",
		LocalDNSEnabled:                       false,
		LocalDNSIP:                            "127.0.0.1",
		LocalDNSPort:                          53,
		LocalDNSCacheMaxRecords:               10000,
		LocalDNSCacheTTLSeconds:               14400.0,
		LocalDNSPendingTimeoutSec:             300.0,
		LocalDNSCachePersist:                  true,
		LocalDNSCacheFlushSec:                 60.0,
		ResolverBalancingStrategy:             3,
		QNameLabelLength:                      63,
		ResolverRateLimitEnabled:              true,
		ResolverTransport:                     "auto",
		ResolverDoTPort:                       853,
		ResolverDoHPort:                       443,
		ResolverDoHPath:                       "/dns-query",
		UploadPacketDuplicationCount:          3,
		DownloadPacketDuplicationCount:        7,
		UploadSetupPacketDuplicationCount:     4,
		DownloadSetupPacketDuplicationCount:   8,
		StreamResolverFailoverResendThreshold: 1,
		StreamResolverFailoverCooldownSec:     0.5,
		RecheckInactiveServersEnabled:         true,
		RecheckInactiveIntervalSeconds:        30.0,
		RecheckServerIntervalSeconds:          1.0,
		RecheckBatchSize:                      30,
		AutoDisableTimeoutServers:             true,
		// A resolver is only culled after this many seconds of *uninterrupted*
		// timeouts (zero successes). 20s was too twitchy for long/lossy sessions:
		// resolvers that were merely having a rough patch got disabled faster than
		// recovery could bring them back, ratcheting the pool down to the floor and
		// collapsing throughput. 90s keeps genuinely-dead resolvers out while
		// tolerating transient loss.
		AutoDisableTimeoutWindowSeconds:       90.0,
		AutoDisableMinObservations:            3,
		AutoDisableCheckIntervalSeconds:       1.0,
		BaseEncodeData:                        false,
		LegacySessionID:                       false,
		MaxActiveStreams:                      2048,
		LocalHandshakeTimeoutSeconds:          5.0,
		UploadCompressionType:                 2,
		DownloadCompressionType:               2,
		CompressionMinSize:                    compression.DefaultMinSize,
		DataEncryptionMethod:                  1,
		EncryptionKey:                         "",
		MinUploadMTU:                          100,
		MinDownloadMTU:                        1000,
		MaxUploadMTU:                          200,
		MaxDownloadMTU:                        4000,
		MTUTestRetriesResolvers:               3,
		MTUTestRetriesLogs:                    5,
		MTUTestTimeoutResolvers:               2.0,
		MTUTestTimeoutLogs:                    2.0,
		MTUTestParallelismResolvers:           100,
		MTUTestParallelismLogs:                32,
		MTUProbeSamples:                       1,
		MTUMaxLoss:                            0.0,
		MTUGroupGapRatio:                      0.25,
		MTUAdaptiveGrouping:                   true,
		MTUWeightedMinPool:                    2,
		FastConnect:                           false,
		DNSRandomizeQueryID:                   true,
		DNSEDNSCookie:                         true,
		DNSQNameCaseRandomization:             false,
		EDNSUDPSize:                           4096,
		ResolverIgnoreInjectedNXDOMAIN:        true,
		RX_TX_Workers:                         4,
		TunnelProcessWorkers:                  4,
		TunnelPacketTimeoutSec:                10.0,
		DispatcherIdlePollIntervalSeconds:     0.020,
		PingAggressiveIntervalSeconds:         0.200,
		PingLazyIntervalSeconds:               0.750,
		PingCooldownIntervalSeconds:           2.0,
		PingColdIntervalSeconds:               15.0,
		PingWarmThresholdSeconds:              5.0,
		PingCoolThresholdSeconds:              15.0,
		PingColdThresholdSeconds:              30.0,
		PingWatchdogTimeoutSeconds:            30.0,
		TXChannelSize:                         2048,
		RXChannelSize:                         2048,
		ResolverUDPConnectionPoolSize:         256,
		StreamQueueInitialCapacity:            512,
		OrphanQueueInitialCapacity:            128,
		DNSResponseFragmentStoreCap:           1024,
		DNSResponseFragmentTimeoutSeconds:     60.0,
		SOCKSUDPAssociateReadTimeoutSeconds:   30.0,
		ClientTerminalStreamRetentionSeconds:  45.0,
		ClientCancelledSetupRetentionSeconds:  120.0,
		SessionInitRetryBaseSeconds:           1.0,
		SessionInitRetryStepSeconds:           1.0,
		SessionInitRetryLinearAfter:           5,
		SessionInitRetryMaxSeconds:            60.0,
		SessionInitBusyRetryIntervalSeconds:   60.0,
		SessionInitRacingCount:                3,
		LogLevel:                              "INFO",
		LogToFile:                             true,
		LogDir:                                "logs",
		LogFileName:                           "cottendns_{time}.log",
		StatsReportIntervalSeconds:            5.0,
		StartupMode:                           "logs",
		LogScanMaxDays:                        30,
		LogScanMaxResolvers:                   0,
		LogBasedMTUVerify:                     true,
		MaxPacketsPerBatch:                    8,
		ARQWindowSize:                         1000,
		ARQInitialRTOSeconds:                  0.6,
		ARQMaxRTOSeconds:                      3.0,
		ARQControlInitialRTOSeconds:           0.5,
		ARQControlMaxRTOSeconds:               2.0,
		ARQMaxControlRetries:                  120,
		// A stalled/half-open stream (server stopped answering, lost FIN) is only
		// reaped once ARQ declares it inactive, so 1800s (30 min) let dead streams
		// pile up in active_streams on lossy links, bloating the per-tick reap
		// scan. 600s still tolerates genuinely idle-but-alive connections (a
		// slow-but-progressing stream keeps resetting its activity clock and is
		// never reaped) while draining dead ones ~3x sooner.
		ARQInactivityTimeoutSeconds:           600.0,
		ARQDataPacketTTLSeconds:               2400.0,
		ARQControlPacketTTLSeconds:            1200.0,
		ARQMaxDataRetries:                     120,
		ARQDataNackMaxGap:                     64,
		ARQDataNackInitialDelaySeconds:        0.4,
		ARQDataNackRepeatSeconds:              0.8,
		ARQTerminalDrainTimeoutSec:            120.0,
		ARQTerminalAckWaitTimeoutSec:          90.0,
	}
}

func LoadClientConfig(filename string) (ClientConfig, error) {
	cfg, err := loadClientConfigFile(filename)
	if err != nil {
		return cfg, err
	}
	return finalizeClientConfig(cfg)
}

// ApplyStartupModeMTU resolves the active MTU-test parameters (MTUTestRetries,
// MTUTestTimeout, MTUTestParallelism) from the per-mode configuration values
// based on the supplied startup mode. Any value other than "logs" is treated as
// the resolvers mode.
func (cfg *ClientConfig) ApplyStartupModeMTU(mode string) {
	if cfg == nil {
		return
	}
	switch strings.ToLower(strings.TrimSpace(mode)) {
	case "logs":
		cfg.MTUTestRetries = cfg.MTUTestRetriesLogs
		cfg.MTUTestTimeout = cfg.MTUTestTimeoutLogs
		cfg.MTUTestParallelism = cfg.MTUTestParallelismLogs
	default:
		cfg.MTUTestRetries = cfg.MTUTestRetriesResolvers
		cfg.MTUTestTimeout = cfg.MTUTestTimeoutResolvers
		cfg.MTUTestParallelism = cfg.MTUTestParallelismResolvers
	}
}

func loadClientConfigFile(filename string) (ClientConfig, error) {
	cfg := defaultClientConfig()
	path, err := filepath.Abs(filename)
	if err != nil {
		return cfg, err
	}

	if _, err := os.Stat(path); err != nil {
		return cfg, fmt.Errorf("config file not found: %s", path)
	}

	meta, err := toml.DecodeFile(path, &cfg)
	if err != nil {
		return cfg, fmt.Errorf("parse TOML failed for %s: %w", path, err)
	}

	cfg.ConfigPath = path
	cfg.ConfigDir = filepath.Dir(path)
	cfg.ResolversFilePath = ""
	cfg.explicitRX_TX_Workers = meta.IsDefined("RX_TX_WORKERS")
	if err := applyClientConfigPreset(&cfg, func(key string) bool {
		return meta.IsDefined(key)
	}); err != nil {
		return cfg, err
	}
	return cfg, nil
}

func LoadClientConfigWithOverrides(filename string, overrides ClientConfigOverrides) (ClientConfig, error) {
	cfg, err := loadClientConfigFile(filename)
	if err != nil {
		return cfg, err
	}

	if overrides.ResolversFilePath != nil {
		cfg.ResolversFilePath = strings.TrimSpace(*overrides.ResolversFilePath)
	}
	if len(overrides.Values) > 0 {
		if err := applyClientConfigOverrideValues(&cfg, overrides.Values); err != nil {
			return cfg, err
		}
		if _, ok := overrides.Values["ConfigPreset"]; ok {
			clientType := reflect.TypeOf(ClientConfig{})
			if err := applyClientConfigPreset(&cfg, func(key string) bool {
				return overrideValuesDefineTOMLKey(overrides.Values, clientType, key)
			}); err != nil {
				return cfg, err
			}
		}
	}

	cfg, err = finalizeClientConfig(cfg)
	if err != nil {
		return cfg, err
	}

	// When explicit resolvers are provided (e.g. from log-based startup), override the
	// file-loaded ones after finalization so the rest of the config is still validated.
	if len(overrides.Resolvers) > 0 {
		cfg.Resolvers = overrides.Resolvers
		rm := make(map[string]int, len(overrides.Resolvers))
		for _, r := range overrides.Resolvers {
			rm[r.IP] = r.Port
		}
		cfg.ResolverMap = rm
	}

	return cfg, nil
}

func finalizeClientConfig(cfg ClientConfig) (ClientConfig, error) {
	cfg.ConfigPreset = normalizeConfigPresetName(cfg.ConfigPreset)
	if !isKnownConfigPreset(cfg.ConfigPreset) {
		return cfg, fmt.Errorf("invalid CONFIG_PRESET: %q (valid: default, speed, survival, tcp-survival)", cfg.ConfigPreset)
	}

	cfg.ProtocolType = strings.ToUpper(strings.TrimSpace(cfg.ProtocolType))
	cfg.LogLevel = strings.TrimSpace(cfg.LogLevel)
	if cfg.LogLevel == "" {
		cfg.LogLevel = "INFO"
	}

	switch cfg.ProtocolType {
	case "", "SOCKS5":
		cfg.ProtocolType = "SOCKS5"
	case "TCP":
	case "HTTP":
	default:
		return cfg, fmt.Errorf("invalid PROTOCOL_TYPE: %q (valid: SOCKS5, HTTP, TCP)", cfg.ProtocolType)
	}

	if cfg.DataEncryptionMethod < 0 || cfg.DataEncryptionMethod > 5 {
		return cfg, fmt.Errorf("invalid DATA_ENCRYPTION_METHOD: %d", cfg.DataEncryptionMethod)
	}

	queryTypeCodes, err := normalizeQueryTypes(cfg.QueryTypes)
	if err != nil {
		return cfg, err
	}
	cfg.QueryTypeCodes = queryTypeCodes

	if cfg.AdaptiveDuplicationTargetDelivery <= 0 {
		cfg.AdaptiveDuplicationTargetDelivery = 0.95
	}
	if cfg.AdaptiveDuplicationTargetDelivery < 0.5 {
		cfg.AdaptiveDuplicationTargetDelivery = 0.5
	}
	if cfg.AdaptiveDuplicationTargetDelivery > 0.999 {
		cfg.AdaptiveDuplicationTargetDelivery = 0.999
	}

	cfg.ListenIP = defaultString(strings.TrimSpace(cfg.ListenIP), "127.0.0.1")

	if cfg.ListenPort < 0 || cfg.ListenPort > 65535 {
		return cfg, fmt.Errorf("invalid LISTEN_PORT: %d", cfg.ListenPort)
	}

	if len(cfg.SOCKS5User) > 255 {
		return cfg, fmt.Errorf("SOCKS5_USER cannot exceed 255 bytes")
	}

	if len(cfg.SOCKS5Pass) > 255 {
		return cfg, fmt.Errorf("SOCKS5_PASS cannot exceed 255 bytes")
	}

	if cfg.SOCKS5Auth && cfg.SOCKS5User == "" {
		return cfg, fmt.Errorf("SOCKS5_AUTH requires SOCKS5_USER")
	}

	cfg.LocalDNSIP = defaultString(strings.TrimSpace(cfg.LocalDNSIP), "127.0.0.1")

	if cfg.LocalDNSPort < 0 || cfg.LocalDNSPort > 65535 {
		return cfg, fmt.Errorf("invalid LOCAL_DNS_PORT: %d", cfg.LocalDNSPort)
	}

	cfg.LocalDNSCacheMaxRecords = defaultIntBelow(cfg.LocalDNSCacheMaxRecords, 1, 2000)
	cfg.LocalDNSCacheTTLSeconds = defaultFloatAtMostZero(cfg.LocalDNSCacheTTLSeconds, 3600.0)
	cfg.LocalDNSPendingTimeoutSec = defaultFloatAtMostZero(cfg.LocalDNSPendingTimeoutSec, 600.0)
	cfg.LocalDNSCacheFlushSec = defaultFloatAtMostZero(cfg.LocalDNSCacheFlushSec, 60.0)

	if cfg.UploadCompressionType < compression.TypeOff || cfg.UploadCompressionType > compression.TypeZLIB {
		return cfg, fmt.Errorf("invalid UPLOAD_COMPRESSION_TYPE: %d", cfg.UploadCompressionType)
	}

	if cfg.DownloadCompressionType < compression.TypeOff || cfg.DownloadCompressionType > compression.TypeZLIB {
		return cfg, fmt.Errorf("invalid DOWNLOAD_COMPRESSION_TYPE: %d", cfg.DownloadCompressionType)
	}

	cfg.CompressionMinSize = defaultIntBelow(cfg.CompressionMinSize, 100, compression.DefaultMinSize)

	if cfg.ResolverBalancingStrategy < 0 || cfg.ResolverBalancingStrategy > 5 {
		return cfg, fmt.Errorf("invalid RESOLVER_BALANCING_STRATEGY: %d", cfg.ResolverBalancingStrategy)
	}

	if cfg.QNameLabelLength <= 0 || cfg.QNameLabelLength > 63 {
		cfg.QNameLabelLength = 63
	}

	if cfg.MaxActiveStreams < 0 {
		cfg.MaxActiveStreams = 0
	} else if cfg.MaxActiveStreams > 65535 {
		cfg.MaxActiveStreams = 65535
	}
	if cfg.LocalHandshakeTimeoutSeconds <= 0 {
		cfg.LocalHandshakeTimeoutSeconds = 5.0
	}
	cfg.LocalHandshakeTimeoutSeconds = clampFloat(cfg.LocalHandshakeTimeoutSeconds, 0.5, 60.0)

	// EDNS_UDP_SIZE: unset/non-positive falls back to the default; otherwise clamp
	// to a sane DNS range so a typo can neither underflow below a minimal message
	// nor advertise an implausibly large buffer.
	if cfg.EDNSUDPSize <= 0 {
		cfg.EDNSUDPSize = 4096
	} else if cfg.EDNSUDPSize < 512 {
		cfg.EDNSUDPSize = 512
	} else if cfg.EDNSUDPSize > 4096 {
		cfg.EDNSUDPSize = 4096
	}

	switch strings.ToLower(strings.TrimSpace(cfg.ResolverTransport)) {
	case "", "auto":
		cfg.ResolverTransport = "auto"
	case "udp":
		cfg.ResolverTransport = "udp"
	case "tcp":
		cfg.ResolverTransport = "tcp"
	case "dot":
		cfg.ResolverTransport = "dot"
	case "doh":
		cfg.ResolverTransport = "doh"
	default:
		return cfg, fmt.Errorf("invalid RESOLVER_TRANSPORT: %q (want auto|udp|tcp|dot|doh)", cfg.ResolverTransport)
	}
	cfg.ResolverDoTPort = clampInt(defaultIntBelow(cfg.ResolverDoTPort, 1, 853), 1, 65535)
	cfg.ResolverDoHPort = clampInt(defaultIntBelow(cfg.ResolverDoHPort, 1, 443), 1, 65535)
	if cfg.ResolverDoHPath == "" || cfg.ResolverDoHPath[0] != '/' {
		cfg.ResolverDoHPath = "/dns-query"
	}

	cfg.UploadPacketDuplicationCount = clampInt(defaultIntBelow(cfg.UploadPacketDuplicationCount, 1, 3), 1, 8)
	cfg.DownloadPacketDuplicationCount = clampInt(defaultIntBelow(cfg.DownloadPacketDuplicationCount, 1, 7), 1, 8)

	// Setup duplication is clamped to be at least as high as the corresponding
	// directional data duplication, so setup packets never underperform data
	// packets in either direction.
	cfg.UploadSetupPacketDuplicationCount = clampInt(defaultIntBelow(cfg.UploadSetupPacketDuplicationCount, 1, 4), cfg.UploadPacketDuplicationCount, 8)
	cfg.DownloadSetupPacketDuplicationCount = clampInt(defaultIntBelow(cfg.DownloadSetupPacketDuplicationCount, 1, 8), cfg.DownloadPacketDuplicationCount, 8)
	cfg.StreamResolverFailoverResendThreshold = clampInt(defaultIntBelow(cfg.StreamResolverFailoverResendThreshold, 1, 1), 1, 128)
	cfg.StreamResolverFailoverCooldownSec = clampFloat(defaultFloatAtMostZero(cfg.StreamResolverFailoverCooldownSec, 0.5), 0.1, 120.0)
	cfg.RecheckInactiveIntervalSeconds = clampFloat(defaultFloatAtMostZero(cfg.RecheckInactiveIntervalSeconds, 30.0), 30.0, 86400.0)
	cfg.RecheckServerIntervalSeconds = clampFloat(defaultFloatAtMostZero(cfg.RecheckServerIntervalSeconds, 1.0), 1.0, 600.0)
	cfg.RecheckBatchSize = clampInt(defaultIntBelow(cfg.RecheckBatchSize, 1, 30), 1, 1024)
	cfg.AutoDisableTimeoutWindowSeconds = clampFloat(defaultFloatAtMostZero(cfg.AutoDisableTimeoutWindowSeconds, 90.0), 1.0, 86400.0)
	cfg.AutoDisableMinObservations = clampInt(defaultIntBelow(cfg.AutoDisableMinObservations, 1, 3), 1, 10000)
	cfg.AutoDisableCheckIntervalSeconds = clampFloat(defaultFloatAtMostZero(cfg.AutoDisableCheckIntervalSeconds, 1.0), 0.25, 600.0)
	cfg.MaxPacketsPerBatch = clampInt(defaultIntBelow(cfg.MaxPacketsPerBatch, 1, 10), 1, 64)
	cfg.ARQWindowSize = clampInt(defaultIntBelow(cfg.ARQWindowSize, 1, 600), 1, 6000)
	cfg.ARQInitialRTOSeconds = clampFloat(defaultFloatAtMostZero(cfg.ARQInitialRTOSeconds, 1.0), 0.05, 60.0)
	cfg.ARQMaxRTOSeconds = clampFloat(defaultFloatAtMostZero(cfg.ARQMaxRTOSeconds, 8.0), cfg.ARQInitialRTOSeconds, 120.0)
	cfg.ARQControlInitialRTOSeconds = clampFloat(defaultFloatAtMostZero(cfg.ARQControlInitialRTOSeconds, 1.0), 0.05, 60.0)
	cfg.ARQControlMaxRTOSeconds = clampFloat(defaultFloatAtMostZero(cfg.ARQControlMaxRTOSeconds, 8.0), cfg.ARQControlInitialRTOSeconds, 120.0)
	cfg.ARQMaxControlRetries = clampInt(defaultIntBelow(cfg.ARQMaxControlRetries, 1, 80), 5, 5000)
	cfg.ARQInactivityTimeoutSeconds = clampFloat(defaultFloatAtMostZero(cfg.ARQInactivityTimeoutSeconds, 600.0), 30.0, 86400.0)
	cfg.ARQDataPacketTTLSeconds = clampFloat(defaultFloatAtMostZero(cfg.ARQDataPacketTTLSeconds, 1800.0), 30.0, 86400.0)
	cfg.ARQControlPacketTTLSeconds = clampFloat(defaultFloatAtMostZero(cfg.ARQControlPacketTTLSeconds, 900.0), 30.0, 86400.0)
	cfg.ARQMaxDataRetries = clampInt(defaultIntBelow(cfg.ARQMaxDataRetries, 1, 800), 60, 100000)
	cfg.ARQDataNackMaxGap = clampInt(defaultIntBelow(cfg.ARQDataNackMaxGap, 0, 0), 0, cfg.ARQWindowSize/4)
	cfg.ARQDataNackInitialDelaySeconds = clampFloat(defaultFloatAtMostZero(cfg.ARQDataNackInitialDelaySeconds, 0.0), 0.05, 30.0)
	cfg.ARQDataNackRepeatSeconds = clampFloat(defaultFloatAtMostZero(cfg.ARQDataNackRepeatSeconds, 2.0), 0.08, 30.0)
	cfg.ARQTerminalDrainTimeoutSec = clampFloat(defaultFloatAtMostZero(cfg.ARQTerminalDrainTimeoutSec, 90.0), 10.0, 3600.0)
	cfg.ARQTerminalAckWaitTimeoutSec = clampFloat(defaultFloatAtMostZero(cfg.ARQTerminalAckWaitTimeoutSec, 60.0), 5.0, 3600.0)

	if cfg.MinUploadMTU < 0 || cfg.MinDownloadMTU < 0 || cfg.MaxUploadMTU < 0 || cfg.MaxDownloadMTU < 0 {
		return cfg, fmt.Errorf("mtu values cannot be negative")
	}

	if cfg.MaxUploadMTU > 0 && cfg.MinUploadMTU > cfg.MaxUploadMTU {
		return cfg, fmt.Errorf("MIN_UPLOAD_MTU cannot be greater than MAX_UPLOAD_MTU")
	}

	if cfg.MaxDownloadMTU > 0 && cfg.MinDownloadMTU > cfg.MaxDownloadMTU {
		return cfg, fmt.Errorf("MIN_DOWNLOAD_MTU cannot be greater than MAX_DOWNLOAD_MTU")
	}

	cfg.MTUTestRetriesResolvers = defaultIntBelow(cfg.MTUTestRetriesResolvers, 1, 3)
	cfg.MTUTestRetriesLogs = defaultIntBelow(cfg.MTUTestRetriesLogs, 1, 5)
	cfg.MTUTestTimeoutResolvers = defaultFloatAtMostZero(cfg.MTUTestTimeoutResolvers, 2.0)
	cfg.MTUTestTimeoutLogs = defaultFloatAtMostZero(cfg.MTUTestTimeoutLogs, 2.0)
	cfg.MTUTestParallelismResolvers = defaultIntBelow(cfg.MTUTestParallelismResolvers, 1, 100)
	cfg.MTUTestParallelismLogs = defaultIntBelow(cfg.MTUTestParallelismLogs, 1, 32)
	cfg.MTUProbeSamples = defaultIntBelow(cfg.MTUProbeSamples, 1, 1)
	if cfg.MTUMaxLoss < 0 {
		cfg.MTUMaxLoss = 0
	}
	if cfg.MTUMaxLoss >= 1 {
		cfg.MTUMaxLoss = 0.99
	}
	if cfg.MTUGroupGapRatio <= 0 {
		cfg.MTUGroupGapRatio = 0.25
	}
	if cfg.MTUWeightedMinPool < 1 {
		cfg.MTUWeightedMinPool = 2
	}
	// Default the active MTU-test trio to the resolvers-mode values until the
	// caller selects a startup mode via ApplyStartupModeMTU.
	cfg.ApplyStartupModeMTU("resolvers")
	legacyRX_TX_Workers := max(cfg.LegacyTunnelReaderWorkers, cfg.LegacyTunnelWriterWorkers)
	if !cfg.explicitRX_TX_Workers && legacyRX_TX_Workers > 0 {
		cfg.RX_TX_Workers = legacyRX_TX_Workers
	}

	cfg.RX_TX_Workers = clampInt(defaultIntBelow(cfg.RX_TX_Workers, 1, 4), 1, 64)
	cfg.TunnelProcessWorkers = max(clampInt(defaultIntBelow(cfg.TunnelProcessWorkers, 1, 4), 1, 64), cfg.RX_TX_Workers)

	cfg.TunnelPacketTimeoutSec = clampFloat(defaultFloatAtMostZero(cfg.TunnelPacketTimeoutSec, 8.0), 0.5, 120.0)
	cfg.DispatcherIdlePollIntervalSeconds = clampFloat(defaultFloatAtMostZero(cfg.DispatcherIdlePollIntervalSeconds, 0.020), 0.001, 1.0)
	cfg.PingAggressiveIntervalSeconds = clampFloat(defaultFloatAtMostZero(cfg.PingAggressiveIntervalSeconds, 0.100), 0.05, 30.0)
	cfg.PingLazyIntervalSeconds = clampFloat(defaultFloatAtMostZero(cfg.PingLazyIntervalSeconds, 1.0), cfg.PingAggressiveIntervalSeconds, 60.0)
	cfg.PingCooldownIntervalSeconds = clampFloat(defaultFloatAtMostZero(cfg.PingCooldownIntervalSeconds, 3.0), cfg.PingLazyIntervalSeconds, 300.0)
	cfg.PingColdIntervalSeconds = clampFloat(defaultFloatAtMostZero(cfg.PingColdIntervalSeconds, 30.0), cfg.PingCooldownIntervalSeconds, 3600.0)
	cfg.PingWarmThresholdSeconds = clampFloat(defaultFloatAtMostZero(cfg.PingWarmThresholdSeconds, 5.0), 0.1, 600.0)
	cfg.PingCoolThresholdSeconds = clampFloat(defaultFloatAtMostZero(cfg.PingCoolThresholdSeconds, 10.0), cfg.PingWarmThresholdSeconds, 1800.0)
	cfg.PingColdThresholdSeconds = clampFloat(defaultFloatAtMostZero(cfg.PingColdThresholdSeconds, 20.0), cfg.PingCoolThresholdSeconds, 3600.0)
	cfg.PingWatchdogTimeoutSeconds = clampFloat(defaultFloatAtMostZero(cfg.PingWatchdogTimeoutSeconds, 30.0), 10.0, 3600.0)
	cfg.TXChannelSize = clampInt(defaultIntBelow(cfg.TXChannelSize, 1, 2048), 64, 65536)
	cfg.RXChannelSize = clampInt(defaultIntBelow(cfg.RXChannelSize, 1, 2048), 64, 65536)
	cfg.ResolverUDPConnectionPoolSize = clampInt(defaultIntBelow(cfg.ResolverUDPConnectionPoolSize, 1, 64), 1, 1024)
	cfg.StreamQueueInitialCapacity = clampInt(defaultIntBelow(cfg.StreamQueueInitialCapacity, 1, 128), 8, 65536)
	cfg.OrphanQueueInitialCapacity = clampInt(defaultIntBelow(cfg.OrphanQueueInitialCapacity, 1, 32), 4, 4096)
	cfg.DNSResponseFragmentStoreCap = clampInt(defaultIntBelow(cfg.DNSResponseFragmentStoreCap, 1, 256), 16, 16384)
	cfg.DNSResponseFragmentTimeoutSeconds = clampFloat(defaultFloatAtMostZero(cfg.DNSResponseFragmentTimeoutSeconds, 10.0), 1.0, 600.0)
	cfg.SOCKSUDPAssociateReadTimeoutSeconds = clampFloat(defaultFloatAtMostZero(cfg.SOCKSUDPAssociateReadTimeoutSeconds, 30.0), 1.0, 3600.0)
	cfg.ClientTerminalStreamRetentionSeconds = clampFloat(defaultFloatAtMostZero(cfg.ClientTerminalStreamRetentionSeconds, 45.0), 1.0, 3600.0)
	cfg.ClientCancelledSetupRetentionSeconds = clampFloat(defaultFloatAtMostZero(cfg.ClientCancelledSetupRetentionSeconds, 120.0), 1.0, 3600.0)
	cfg.SessionInitRetryBaseSeconds = clampFloat(defaultFloatAtMostZero(cfg.SessionInitRetryBaseSeconds, 1.0), 0.1, 60.0)
	cfg.SessionInitRetryStepSeconds = clampFloat(defaultFloatAtMostZero(cfg.SessionInitRetryStepSeconds, 1.0), 0.0, 60.0)
	cfg.SessionInitRetryLinearAfter = clampInt(defaultIntBelow(cfg.SessionInitRetryLinearAfter, 0, 5), 0, 1000)
	cfg.SessionInitRetryMaxSeconds = clampFloat(defaultFloatAtMostZero(cfg.SessionInitRetryMaxSeconds, 60.0), cfg.SessionInitRetryBaseSeconds, 3600.0)
	cfg.SessionInitBusyRetryIntervalSeconds = clampFloat(defaultFloatAtMostZero(cfg.SessionInitBusyRetryIntervalSeconds, 60.0), 1.0, 3600.0)
	// Capped well below the resolver pool size: racing every resolver would turn
	// one connect into a burst large enough to look like a flood.
	cfg.SessionInitRacingCount = clampInt(defaultIntBelow(cfg.SessionInitRacingCount, 1, 3), 1, 8)

	cfg.LogDir = strings.TrimSpace(cfg.LogDir)
	if cfg.LogDir == "" {
		cfg.LogDir = "logs"
	}
	cfg.LogFileName = strings.TrimSpace(cfg.LogFileName)
	if cfg.LogFileName == "" {
		cfg.LogFileName = "cottendns_{time}.log"
	}
	cfg.StartupMode = strings.ToLower(strings.TrimSpace(cfg.StartupMode))
	switch cfg.StartupMode {
	case "", "ask":
		cfg.StartupMode = "ask"
	case "resolvers", "logs":
		// valid
	default:
		cfg.StartupMode = "ask"
	}
	if cfg.LogScanMaxDays < 0 {
		cfg.LogScanMaxDays = 0
	}
	if cfg.LogScanMaxResolvers < 0 {
		cfg.LogScanMaxResolvers = 0
	}
	if cfg.StatsReportIntervalSeconds > 0 {
		cfg.StatsReportIntervalSeconds = clampFloat(cfg.StatsReportIntervalSeconds, 1.0, 3600.0)
	}

	cfg.EncryptionKey = strings.TrimSpace(cfg.EncryptionKey)
	if cfg.EncryptionKey == "" {
		return cfg, fmt.Errorf("ENCRYPTION_KEY is required in client config")
	}

	cfg.Domains = normalizeClientDomains(cfg.Domains)
	if len(cfg.Domains) == 0 {
		return cfg, fmt.Errorf("DOMAINS must contain at least one domain")
	}

	cfg.ResolversFilePath = strings.TrimSpace(cfg.ResolversFilePath)

	resolvers, resolverMap, err := LoadClientResolvers(cfg.ResolversPath())
	if err != nil {
		return cfg, err
	}
	cfg.Resolvers = resolvers
	cfg.ResolverMap = resolverMap
	return cfg, nil
}

func (c ClientConfig) ResolversPath() string {
	if c.ResolversFilePath != "" {
		if filepath.IsAbs(c.ResolversFilePath) {
			return c.ResolversFilePath
		}
		if c.ConfigDir != "" {
			return filepath.Join(c.ConfigDir, c.ResolversFilePath)
		}
		return c.ResolversFilePath
	}
	return filepath.Join(c.ConfigDir, "client_resolvers.txt")
}

func (c ClientConfig) LocalDNSCachePath() string {
	return filepath.Join(c.ConfigDir, "local_dns_cache.bin")
}

// normalizeQueryTypes validates the configured QUERY_TYPES names and returns
// their numeric qTypes, preserving order and dropping duplicates. An empty or
// unset list defaults to TXT-only (the historical behavior). An unrecognized
// name is a hard error so misconfiguration is caught at startup rather than
// silently degrading the tunnel.
func normalizeQueryTypes(names []string) ([]uint16, error) {
	if len(names) == 0 {
		return []uint16{Enums.DNS_RECORD_TYPE_TXT}, nil
	}

	codes := make([]uint16, 0, len(names))
	seen := make(map[uint16]struct{}, len(names))
	for _, name := range names {
		trimmed := strings.TrimSpace(name)
		if trimmed == "" {
			continue
		}
		code, ok := Enums.DNSRecordTypeFromName(trimmed)
		if !ok {
			return nil, fmt.Errorf("invalid QUERY_TYPES entry: %q", name)
		}
		if _, dup := seen[code]; dup {
			continue
		}
		seen[code] = struct{}{}
		codes = append(codes, code)
	}

	if len(codes) == 0 {
		return []uint16{Enums.DNS_RECORD_TYPE_TXT}, nil
	}
	return codes, nil
}

func normalizeClientDomains(domains []string) []string {
	if len(domains) == 0 {
		return nil
	}

	unique := make(map[string]struct{}, len(domains))
	for _, domain := range domains {
		normalized := strings.TrimSuffix(strings.ToLower(strings.TrimSpace(domain)), ".")
		if normalized == "" || normalized == "." {
			continue
		}
		unique[normalized] = struct{}{}
	}

	if len(unique) == 0 {
		return nil
	}

	normalized := make([]string, 0, len(unique))
	for domain := range unique {
		normalized = append(normalized, domain)
	}

	sort.Slice(normalized, func(i, j int) bool {
		if len(normalized[i]) == len(normalized[j]) {
			return normalized[i] < normalized[j]
		}
		return len(normalized[i]) > len(normalized[j])
	})

	return normalized
}

func defaultString(value string, fallback string) string {
	if value == "" {
		return fallback
	}
	return value
}

func defaultIntBelow(value int, minValue int, fallback int) int {
	if value < minValue {
		return fallback
	}
	return value
}

func (c ClientConfig) DispatcherIdlePollInterval() time.Duration {
	return time.Duration(c.DispatcherIdlePollIntervalSeconds * float64(time.Second))
}

func (c ClientConfig) PingAggressiveInterval() time.Duration {
	return time.Duration(c.PingAggressiveIntervalSeconds * float64(time.Second))
}

func (c ClientConfig) PingLazyInterval() time.Duration {
	return time.Duration(c.PingLazyIntervalSeconds * float64(time.Second))
}

func (c ClientConfig) PingCooldownInterval() time.Duration {
	return time.Duration(c.PingCooldownIntervalSeconds * float64(time.Second))
}

func (c ClientConfig) PingColdInterval() time.Duration {
	return time.Duration(c.PingColdIntervalSeconds * float64(time.Second))
}

func (c ClientConfig) PingWarmThreshold() time.Duration {
	return time.Duration(c.PingWarmThresholdSeconds * float64(time.Second))
}

func (c ClientConfig) PingCoolThreshold() time.Duration {
	return time.Duration(c.PingCoolThresholdSeconds * float64(time.Second))
}

func (c ClientConfig) PingColdThreshold() time.Duration {
	return time.Duration(c.PingColdThresholdSeconds * float64(time.Second))
}

func (c ClientConfig) PingWatchdogTimeout() time.Duration {
	return time.Duration(c.PingWatchdogTimeoutSeconds * float64(time.Second))
}

func (c ClientConfig) DNSResponseFragmentTimeout() time.Duration {
	return time.Duration(c.DNSResponseFragmentTimeoutSeconds * float64(time.Second))
}

func (c ClientConfig) SOCKSUDPAssociateReadTimeout() time.Duration {
	return time.Duration(c.SOCKSUDPAssociateReadTimeoutSeconds * float64(time.Second))
}

// LocalHandshakeTimeout is the bound for a local SOCKS5/TCP client handshake.
func (c ClientConfig) LocalHandshakeTimeout() time.Duration {
	return time.Duration(c.LocalHandshakeTimeoutSeconds * float64(time.Second))
}

func (c ClientConfig) ClientTerminalStreamRetention() time.Duration {
	return time.Duration(c.ClientTerminalStreamRetentionSeconds * float64(time.Second))
}

func (c ClientConfig) ClientCancelledSetupRetention() time.Duration {
	return time.Duration(c.ClientCancelledSetupRetentionSeconds * float64(time.Second))
}

func (c ClientConfig) SessionInitRetryBase() time.Duration {
	return time.Duration(c.SessionInitRetryBaseSeconds * float64(time.Second))
}

func (c ClientConfig) SessionInitRetryStep() time.Duration {
	return time.Duration(c.SessionInitRetryStepSeconds * float64(time.Second))
}

func (c ClientConfig) SessionInitRetryMax() time.Duration {
	return time.Duration(c.SessionInitRetryMaxSeconds * float64(time.Second))
}

func (c ClientConfig) SessionInitBusyRetryInterval() time.Duration {
	return time.Duration(c.SessionInitBusyRetryIntervalSeconds * float64(time.Second))
}

func (c ClientConfig) StatsReportInterval() time.Duration {
	if c.StatsReportIntervalSeconds <= 0 {
		return 0
	}
	return time.Duration(c.StatsReportIntervalSeconds * float64(time.Second))
}

// ResolvedLogDir returns the absolute path of the directory where log files are written.
func (c ClientConfig) ResolvedLogDir() string {
	dir := c.LogDir
	if dir == "" {
		dir = "logs"
	}
	if filepath.IsAbs(dir) {
		return dir
	}
	if c.ConfigDir != "" {
		return filepath.Join(c.ConfigDir, dir)
	}
	return dir
}

// ResolvedLogFilePath returns the absolute path for the session log file, or an empty
// string when LOG_TO_FILE is false. The path is computed once at call time (the {time}
// placeholder is expanded to the current timestamp), so callers should invoke this once
// at startup and store the result.
func (c ClientConfig) ResolvedLogFilePath() string {
	if !c.LogToFile {
		return ""
	}
	name := c.LogFileName
	if name == "" {
		name = "cottendns_{time}.log"
	}
	if strings.Contains(name, "{time}") {
		ts := time.Now().Format("20060102_150405")
		name = strings.ReplaceAll(name, "{time}", ts)
	}
	return filepath.Join(c.ResolvedLogDir(), name)
}

// ResolvedResolverCacheLogPath returns the absolute path for this session's resolver
// cache log, or an empty string when LOG_TO_FILE is false.
func (c ClientConfig) ResolvedResolverCacheLogPath() string {
	if !c.LogToFile {
		return ""
	}
	ts := time.Now().Format("20060102_150405")
	name := "resolver_cache_" + ts + ".log"
	return filepath.Join(c.ResolvedLogDir(), name)
}

// ClientStartupPreConfig holds only the fields needed before the full client config
// is loaded, allowing main to decide the startup mode with minimal parsing.
type ClientStartupPreConfig struct {
	StartupMode         string `toml:"STARTUP_MODE"`
	LogDir              string `toml:"LOG_DIR"`
	LogScanMaxDays      int    `toml:"LOG_SCAN_MAX_DAYS"`
	LogScanMaxResolvers int    `toml:"LOG_SCAN_MAX_RESOLVERS"`
	ConfigDir           string `toml:"-"`
}

// ResolvedLogDir returns the absolute path of the log directory for this pre-config.
func (c ClientStartupPreConfig) ResolvedLogDir() string {
	dir := strings.TrimSpace(c.LogDir)
	if dir == "" {
		dir = "logs"
	}
	if filepath.IsAbs(dir) {
		return dir
	}
	if c.ConfigDir != "" {
		return filepath.Join(c.ConfigDir, dir)
	}
	return dir
}

// PeekClientStartupConfig reads only the startup-related fields from the config file
// without performing full validation. Returns sensible defaults if the file cannot be
// decoded. This is intentionally lenient so the startup prompt can always be shown.
func PeekClientStartupConfig(configPath string) ClientStartupPreConfig {
	pre := ClientStartupPreConfig{
		StartupMode:         "ask",
		LogDir:              "logs",
		LogScanMaxDays:      30,
		LogScanMaxResolvers: 0,
	}

	path, err := filepath.Abs(configPath)
	if err != nil {
		return pre
	}
	pre.ConfigDir = filepath.Dir(path)

	// Decode only the fields we care about; ignore all errors.
	_, _ = toml.DecodeFile(path, &pre)

	pre.StartupMode = strings.ToLower(strings.TrimSpace(pre.StartupMode))
	switch pre.StartupMode {
	case "", "ask":
		pre.StartupMode = "ask"
	case "resolvers", "logs":
		// valid
	default:
		pre.StartupMode = "ask"
	}
	if pre.LogScanMaxDays < 0 {
		pre.LogScanMaxDays = 0
	}
	if pre.LogScanMaxResolvers < 0 {
		pre.LogScanMaxResolvers = 0
	}
	return pre
}

func applyClientConfigOverrideValues(cfg *ClientConfig, values map[string]any) error {
	if cfg == nil || len(values) == 0 {
		return nil
	}

	elem := reflect.ValueOf(cfg).Elem()
	typ := elem.Type()
	for fieldName, rawValue := range values {
		field, ok := typ.FieldByName(fieldName)
		if !ok {
			return fmt.Errorf("unknown client config override field: %s", fieldName)
		}
		value := elem.FieldByName(fieldName)
		if !value.CanSet() {
			return fmt.Errorf("client config override field is not settable: %s", fieldName)
		}
		if err := assignClientConfigOverrideValue(value, rawValue, field.Name); err != nil {
			return err
		}
	}
	return nil
}

func assignClientConfigOverrideValue(target reflect.Value, rawValue any, fieldName string) error {
	if !target.IsValid() {
		return fmt.Errorf("invalid client config override target: %s", fieldName)
	}

	switch target.Kind() {
	case reflect.String:
		v, ok := rawValue.(string)
		if !ok {
			return fmt.Errorf("invalid string override for %s", fieldName)
		}
		target.SetString(v)
		return nil
	case reflect.Bool:
		v, ok := rawValue.(bool)
		if !ok {
			return fmt.Errorf("invalid bool override for %s", fieldName)
		}
		target.SetBool(v)
		return nil
	case reflect.Int:
		v, ok := rawValue.(int)
		if !ok {
			return fmt.Errorf("invalid int override for %s", fieldName)
		}
		target.SetInt(int64(v))
		return nil
	case reflect.Float64:
		v, ok := rawValue.(float64)
		if !ok {
			return fmt.Errorf("invalid float override for %s", fieldName)
		}
		target.SetFloat(v)
		return nil
	case reflect.Slice:
		if target.Type().Elem().Kind() == reflect.String {
			v, ok := rawValue.([]string)
			if !ok {
				return fmt.Errorf("invalid string slice override for %s", fieldName)
			}
			target.Set(reflect.ValueOf(append([]string(nil), v...)))
			return nil
		}
	}

	return fmt.Errorf("unsupported client config override type for %s", fieldName)
}

func NewClientConfigFlagBinder(fs *flag.FlagSet) (*ClientConfigFlagBinder, error) {
	if fs == nil {
		return nil, fmt.Errorf("flag set is required")
	}

	binder := &ClientConfigFlagBinder{
		values:      defaultClientConfig(),
		setFields:   make(map[string]struct{}),
		flagToField: make(map[string]string),
	}

	valueElem := reflect.ValueOf(&binder.values).Elem()
	valueType := valueElem.Type()
	for i := 0; i < valueType.NumField(); i++ {
		field := valueType.Field(i)
		tomlTag := field.Tag.Get("toml")
		if tomlTag == "" || tomlTag == "-" {
			continue
		}

		flagName := clientConfigFlagName(tomlTag)
		binder.flagToField[flagName] = field.Name
		target := valueElem.Field(i)
		usage := fmt.Sprintf("Override %s from config file", tomlTag)

		switch target.Kind() {
		case reflect.String:
			fs.Var(newClientConfigStringFlag(target.Addr().Interface().(*string), binder, field.Name), flagName, usage)
		case reflect.Bool:
			fs.Var(newClientConfigBoolFlag(target.Addr().Interface().(*bool), binder, field.Name), flagName, usage)
		case reflect.Int:
			fs.Var(newClientConfigIntFlag(target.Addr().Interface().(*int), binder, field.Name), flagName, usage)
		case reflect.Float64:
			fs.Var(newClientConfigFloatFlag(target.Addr().Interface().(*float64), binder, field.Name), flagName, usage)
		case reflect.Slice:
			if target.Type().Elem().Kind() != reflect.String {
				continue
			}
			fs.Var(newClientConfigStringSliceFlag(target.Addr().Interface().(*[]string), binder, field.Name), flagName, usage+" (comma-separated)")
		default:
			continue
		}
	}

	return binder, nil
}

func (b *ClientConfigFlagBinder) Overrides() ClientConfigOverrides {
	overrides := ClientConfigOverrides{
		Values: make(map[string]any, len(b.setFields)),
	}
	if b == nil {
		return overrides
	}

	valueElem := reflect.ValueOf(&b.values).Elem()
	for fieldName := range b.setFields {
		field := valueElem.FieldByName(fieldName)
		if !field.IsValid() {
			continue
		}
		switch field.Kind() {
		case reflect.String:
			overrides.Values[fieldName] = field.String()
		case reflect.Bool:
			overrides.Values[fieldName] = field.Bool()
		case reflect.Int:
			overrides.Values[fieldName] = int(field.Int())
		case reflect.Float64:
			overrides.Values[fieldName] = field.Float()
		case reflect.Slice:
			if field.Type().Elem().Kind() == reflect.String {
				src := field.Interface().([]string)
				overrides.Values[fieldName] = append([]string(nil), src...)
			}
		}
	}

	return overrides
}

func (b *ClientConfigFlagBinder) markSet(fieldName string) {
	if b == nil || fieldName == "" {
		return
	}
	b.setFields[fieldName] = struct{}{}
}

func clientConfigFlagName(tomlTag string) string {
	return strings.ToLower(strings.ReplaceAll(tomlTag, "_", "-"))
}

type clientConfigStringFlag struct {
	target    *string
	binder    *ClientConfigFlagBinder
	fieldName string
}

func newClientConfigStringFlag(target *string, binder *ClientConfigFlagBinder, fieldName string) *clientConfigStringFlag {
	return &clientConfigStringFlag{target: target, binder: binder, fieldName: fieldName}
}

func (f *clientConfigStringFlag) String() string {
	if f == nil || f.target == nil {
		return ""
	}
	return *f.target
}

func (f *clientConfigStringFlag) Set(value string) error {
	if f == nil || f.target == nil {
		return nil
	}
	*f.target = value
	f.binder.markSet(f.fieldName)
	return nil
}

type clientConfigBoolFlag struct {
	target    *bool
	binder    *ClientConfigFlagBinder
	fieldName string
}

func newClientConfigBoolFlag(target *bool, binder *ClientConfigFlagBinder, fieldName string) *clientConfigBoolFlag {
	return &clientConfigBoolFlag{target: target, binder: binder, fieldName: fieldName}
}

func (f *clientConfigBoolFlag) String() string {
	if f == nil || f.target == nil {
		return "false"
	}
	return strconv.FormatBool(*f.target)
}

func (f *clientConfigBoolFlag) Set(value string) error {
	if f == nil || f.target == nil {
		return nil
	}
	parsed, err := strconv.ParseBool(value)
	if err != nil {
		return err
	}
	*f.target = parsed
	f.binder.markSet(f.fieldName)
	return nil
}

func (f *clientConfigBoolFlag) IsBoolFlag() bool { return true }

type clientConfigIntFlag struct {
	target    *int
	binder    *ClientConfigFlagBinder
	fieldName string
}

func newClientConfigIntFlag(target *int, binder *ClientConfigFlagBinder, fieldName string) *clientConfigIntFlag {
	return &clientConfigIntFlag{target: target, binder: binder, fieldName: fieldName}
}

func (f *clientConfigIntFlag) String() string {
	if f == nil || f.target == nil {
		return "0"
	}
	return strconv.Itoa(*f.target)
}

func (f *clientConfigIntFlag) Set(value string) error {
	if f == nil || f.target == nil {
		return nil
	}
	parsed, err := strconv.Atoi(value)
	if err != nil {
		return err
	}
	*f.target = parsed
	f.binder.markSet(f.fieldName)
	return nil
}

type clientConfigFloatFlag struct {
	target    *float64
	binder    *ClientConfigFlagBinder
	fieldName string
}

func newClientConfigFloatFlag(target *float64, binder *ClientConfigFlagBinder, fieldName string) *clientConfigFloatFlag {
	return &clientConfigFloatFlag{target: target, binder: binder, fieldName: fieldName}
}

func (f *clientConfigFloatFlag) String() string {
	if f == nil || f.target == nil {
		return "0"
	}
	return strconv.FormatFloat(*f.target, 'f', -1, 64)
}

func (f *clientConfigFloatFlag) Set(value string) error {
	if f == nil || f.target == nil {
		return nil
	}
	parsed, err := strconv.ParseFloat(value, 64)
	if err != nil {
		return err
	}
	*f.target = parsed
	f.binder.markSet(f.fieldName)
	return nil
}

type clientConfigStringSliceFlag struct {
	target    *[]string
	binder    *ClientConfigFlagBinder
	fieldName string
}

func newClientConfigStringSliceFlag(target *[]string, binder *ClientConfigFlagBinder, fieldName string) *clientConfigStringSliceFlag {
	return &clientConfigStringSliceFlag{target: target, binder: binder, fieldName: fieldName}
}

func (f *clientConfigStringSliceFlag) String() string {
	if f == nil || f.target == nil {
		return ""
	}
	return strings.Join(*f.target, ",")
}

func (f *clientConfigStringSliceFlag) Set(value string) error {
	if f == nil || f.target == nil {
		return nil
	}
	parts := strings.Split(value, ",")
	items := make([]string, 0, len(parts))
	for _, part := range parts {
		part = strings.TrimSpace(part)
		if part == "" {
			continue
		}
		items = append(items, part)
	}
	*f.target = items
	f.binder.markSet(f.fieldName)
	return nil
}

func clampInt(value int, minValue int, maxValue int) int {
	if value < minValue {
		return minValue
	}
	if value > maxValue {
		return maxValue
	}
	return value
}

func defaultFloatAtMostZero(value float64, fallback float64) float64 {
	if value <= 0 {
		return fallback
	}
	return value
}

func defaultFloatBelow(value float64, minValue float64, fallback float64) float64 {
	if value < minValue {
		return fallback
	}
	return value
}

func clampFloat(value float64, minValue float64, maxValue float64) float64 {
	if value < minValue {
		return minValue
	}
	if value > maxValue {
		return maxValue
	}
	return value
}
