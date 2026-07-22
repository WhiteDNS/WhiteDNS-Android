package config

import (
	"reflect"
	"strings"
)

type configKeyDefinedFunc func(string) bool

func normalizeConfigPresetName(name string) string {
	name = strings.ToLower(strings.TrimSpace(name))
	name = strings.ReplaceAll(name, "_", "-")
	if name == "" {
		return "default"
	}
	switch name {
	case "tcp", "tcp-survival", "tcp-survive":
		return "tcp-survival"
	default:
		return name
	}
}

func isKnownConfigPreset(name string) bool {
	switch normalizeConfigPresetName(name) {
	case "default", "speed", "survival", "tcp-survival":
		return true
	default:
		return false
	}
}

func configKeyUnset(isDefined configKeyDefinedFunc, key string) bool {
	return isDefined == nil || !isDefined(key)
}

func overrideValuesDefineTOMLKey(values map[string]any, typ reflect.Type, tomlKey string) bool {
	if len(values) == 0 || tomlKey == "" {
		return false
	}
	for i := 0; i < typ.NumField(); i++ {
		field := typ.Field(i)
		if field.Tag.Get("toml") != tomlKey {
			continue
		}
		_, ok := values[field.Name]
		return ok
	}
	return false
}

func applyClientConfigPreset(cfg *ClientConfig, isDefined configKeyDefinedFunc) error {
	if cfg == nil {
		return nil
	}
	preset := normalizeConfigPresetName(cfg.ConfigPreset)
	if !isKnownConfigPreset(preset) {
		return invalidConfigPresetError(preset)
	}
	cfg.ConfigPreset = preset

	switch preset {
	case "speed":
		applyClientSpeedPreset(cfg, isDefined)
	case "survival":
		applyClientSurvivalPreset(cfg, isDefined)
	case "tcp-survival":
		applyClientTCPSurvivalPreset(cfg, isDefined)
	}
	return nil
}

func applyServerConfigPreset(cfg *ServerConfig, isDefined configKeyDefinedFunc) error {
	if cfg == nil {
		return nil
	}
	preset := normalizeConfigPresetName(cfg.ConfigPreset)
	if !isKnownConfigPreset(preset) {
		return invalidConfigPresetError(preset)
	}
	cfg.ConfigPreset = preset

	switch preset {
	case "speed":
		applyServerSpeedPreset(cfg, isDefined)
	case "survival":
		applyServerSurvivalPreset(cfg, isDefined)
	case "tcp-survival":
		applyServerTCPSurvivalPreset(cfg, isDefined)
	}
	return nil
}

func invalidConfigPresetError(name string) error {
	return presetError{name: name}
}

type presetError struct {
	name string
}

func (e presetError) Error() string {
	return "invalid CONFIG_PRESET: " + e.name + " (valid: default, speed, survival, tcp-survival)"
}

func applyClientSpeedPreset(cfg *ClientConfig, isDefined configKeyDefinedFunc) {
	setClientInt(isDefined, "RESOLVER_BALANCING_STRATEGY", &cfg.ResolverBalancingStrategy, 5)
	setClientString(isDefined, "RESOLVER_TRANSPORT", &cfg.ResolverTransport, "auto")
	setClientInt(isDefined, "UPLOAD_PACKET_DUPLICATION_COUNT", &cfg.UploadPacketDuplicationCount, 1)
	// Keep healthy data/ACK traffic at one copy. Adaptive duplication raises it
	// from this floor when measured loss demands redundancy and naturally falls
	// back to one as the loss windows decay.
	setClientInt(isDefined, "DOWNLOAD_PACKET_DUPLICATION_COUNT", &cfg.DownloadPacketDuplicationCount, 1)
	setClientInt(isDefined, "UPLOAD_SETUP_PACKET_DUPLICATION_COUNT", &cfg.UploadSetupPacketDuplicationCount, 2)
	setClientInt(isDefined, "DOWNLOAD_SETUP_PACKET_DUPLICATION_COUNT", &cfg.DownloadSetupPacketDuplicationCount, 4)
	setClientBool(isDefined, "DUPLICATION_PREFER_DISTINCT_DOMAINS", &cfg.DuplicationPreferDistinctDomains, true)
	setClientBool(isDefined, "ADAPTIVE_DUPLICATION", &cfg.AdaptiveDuplication, true)
	setClientFloat(isDefined, "ADAPTIVE_DUPLICATION_TARGET_DELIVERY", &cfg.AdaptiveDuplicationTargetDelivery, 0.95)
	setClientInt(isDefined, "MTU_PROBE_SAMPLES", &cfg.MTUProbeSamples, 4)
	setClientFloat(isDefined, "MTU_MAX_LOSS", &cfg.MTUMaxLoss, 0.25)
	setClientBool(isDefined, "MTU_ADAPTIVE_GROUPING", &cfg.MTUAdaptiveGrouping, true)
	setClientInt(isDefined, "MTU_TEST_RETRIES_RESOLVERS", &cfg.MTUTestRetriesResolvers, 2)
	setClientFloat(isDefined, "MTU_TEST_TIMEOUT_RESOLVERS", &cfg.MTUTestTimeoutResolvers, 1.5)
	setClientInt(isDefined, "MTU_TEST_PARALLELISM_RESOLVERS", &cfg.MTUTestParallelismResolvers, 100)
	setClientInt(isDefined, "MTU_TEST_RETRIES_LOGS", &cfg.MTUTestRetriesLogs, 3)
	setClientFloat(isDefined, "MTU_TEST_TIMEOUT_LOGS", &cfg.MTUTestTimeoutLogs, 1.5)
	// Race the handshake across more resolvers than the default 3. Connect time
	// is dominated by waiting out a resolver that may simply be dead, and the
	// server keys sessions by the init signature, so the extra copies still
	// yield exactly one session. This matches the parallelism this preset
	// already leans on elsewhere, and costs a few extra queries once, at connect.
	setClientInt(isDefined, "SESSION_INIT_RACING_COUNT", &cfg.SessionInitRacingCount, 5)
	setClientFloat(isDefined, "PING_WATCHDOG_TIMEOUT_SECONDS", &cfg.PingWatchdogTimeoutSeconds, 20.0)
	setClientInt(isDefined, "MAX_PACKETS_PER_BATCH", &cfg.MaxPacketsPerBatch, 12)
	setClientInt(isDefined, "ARQ_WINDOW_SIZE", &cfg.ARQWindowSize, 1500)
	setClientFloat(isDefined, "ARQ_INITIAL_RTO_SECONDS", &cfg.ARQInitialRTOSeconds, 0.45)
	setClientFloat(isDefined, "ARQ_MAX_RTO_SECONDS", &cfg.ARQMaxRTOSeconds, 2.5)
	setClientFloat(isDefined, "ARQ_DATA_NACK_INITIAL_DELAY_SECONDS", &cfg.ARQDataNackInitialDelaySeconds, 0.25)
	setClientFloat(isDefined, "ARQ_DATA_NACK_REPEAT_SECONDS", &cfg.ARQDataNackRepeatSeconds, 0.5)
	setClientInt(isDefined, "UPLOAD_COMPRESSION_TYPE", &cfg.UploadCompressionType, 2)
	setClientInt(isDefined, "DOWNLOAD_COMPRESSION_TYPE", &cfg.DownloadCompressionType, 2)
	setClientInt(isDefined, "COMPRESSION_MIN_SIZE", &cfg.CompressionMinSize, 180)
	setClientInt(isDefined, "QNAME_LABEL_LENGTH", &cfg.QNameLabelLength, 63)
	setClientInt(isDefined, "EDNS_UDP_SIZE", &cfg.EDNSUDPSize, 4096)
	setClientStrings(isDefined, "QUERY_TYPES", &cfg.QueryTypes, []string{"TXT", "HTTPS"})
}

func applyClientSurvivalPreset(cfg *ClientConfig, isDefined configKeyDefinedFunc) {
	setClientInt(isDefined, "RESOLVER_BALANCING_STRATEGY", &cfg.ResolverBalancingStrategy, 3)
	setClientString(isDefined, "RESOLVER_TRANSPORT", &cfg.ResolverTransport, "auto")
	setClientInt(isDefined, "UPLOAD_PACKET_DUPLICATION_COUNT", &cfg.UploadPacketDuplicationCount, 2)
	setClientInt(isDefined, "DOWNLOAD_PACKET_DUPLICATION_COUNT", &cfg.DownloadPacketDuplicationCount, 6)
	setClientInt(isDefined, "UPLOAD_SETUP_PACKET_DUPLICATION_COUNT", &cfg.UploadSetupPacketDuplicationCount, 4)
	setClientInt(isDefined, "DOWNLOAD_SETUP_PACKET_DUPLICATION_COUNT", &cfg.DownloadSetupPacketDuplicationCount, 8)
	setClientBool(isDefined, "DUPLICATION_PREFER_DISTINCT_DOMAINS", &cfg.DuplicationPreferDistinctDomains, true)
	setClientBool(isDefined, "ADAPTIVE_DUPLICATION", &cfg.AdaptiveDuplication, true)
	setClientFloat(isDefined, "PING_WATCHDOG_TIMEOUT_SECONDS", &cfg.PingWatchdogTimeoutSeconds, 15.0)
	setClientFloat(isDefined, "ADAPTIVE_DUPLICATION_TARGET_DELIVERY", &cfg.AdaptiveDuplicationTargetDelivery, 0.97)
	setClientInt(isDefined, "MIN_UPLOAD_MTU", &cfg.MinUploadMTU, 80)
	setClientInt(isDefined, "MAX_UPLOAD_MTU", &cfg.MaxUploadMTU, 180)
	setClientInt(isDefined, "MIN_DOWNLOAD_MTU", &cfg.MinDownloadMTU, 700)
	setClientInt(isDefined, "MAX_DOWNLOAD_MTU", &cfg.MaxDownloadMTU, 2500)
	setClientInt(isDefined, "MTU_PROBE_SAMPLES", &cfg.MTUProbeSamples, 5)
	setClientFloat(isDefined, "MTU_MAX_LOSS", &cfg.MTUMaxLoss, 0.2)
	setClientBool(isDefined, "MTU_ADAPTIVE_GROUPING", &cfg.MTUAdaptiveGrouping, true)
	setClientFloat(isDefined, "MTU_TEST_TIMEOUT_RESOLVERS", &cfg.MTUTestTimeoutResolvers, 2.5)
	setClientInt(isDefined, "MTU_TEST_PARALLELISM_RESOLVERS", &cfg.MTUTestParallelismResolvers, 64)
	setClientInt(isDefined, "MAX_PACKETS_PER_BATCH", &cfg.MaxPacketsPerBatch, 8)
	setClientFloat(isDefined, "ARQ_INITIAL_RTO_SECONDS", &cfg.ARQInitialRTOSeconds, 0.7)
	setClientFloat(isDefined, "ARQ_MAX_RTO_SECONDS", &cfg.ARQMaxRTOSeconds, 4.0)
	setClientFloat(isDefined, "ARQ_DATA_NACK_INITIAL_DELAY_SECONDS", &cfg.ARQDataNackInitialDelaySeconds, 0.35)
	setClientFloat(isDefined, "ARQ_DATA_NACK_REPEAT_SECONDS", &cfg.ARQDataNackRepeatSeconds, 0.8)
	setClientInt(isDefined, "UPLOAD_COMPRESSION_TYPE", &cfg.UploadCompressionType, 2)
	setClientInt(isDefined, "DOWNLOAD_COMPRESSION_TYPE", &cfg.DownloadCompressionType, 2)
	setClientInt(isDefined, "COMPRESSION_MIN_SIZE", &cfg.CompressionMinSize, 120)
	setClientInt(isDefined, "QNAME_LABEL_LENGTH", &cfg.QNameLabelLength, 42)
	setClientInt(isDefined, "EDNS_UDP_SIZE", &cfg.EDNSUDPSize, 1232)
	setClientStrings(isDefined, "QUERY_TYPES", &cfg.QueryTypes, []string{"TXT", "CNAME", "HTTPS", "A"})
}

func applyClientTCPSurvivalPreset(cfg *ClientConfig, isDefined configKeyDefinedFunc) {
	setClientInt(isDefined, "RESOLVER_BALANCING_STRATEGY", &cfg.ResolverBalancingStrategy, 5)
	setClientString(isDefined, "RESOLVER_TRANSPORT", &cfg.ResolverTransport, "tcp")
	setClientInt(isDefined, "UPLOAD_PACKET_DUPLICATION_COUNT", &cfg.UploadPacketDuplicationCount, 1)
	setClientInt(isDefined, "DOWNLOAD_PACKET_DUPLICATION_COUNT", &cfg.DownloadPacketDuplicationCount, 2)
	setClientInt(isDefined, "UPLOAD_SETUP_PACKET_DUPLICATION_COUNT", &cfg.UploadSetupPacketDuplicationCount, 3)
	setClientInt(isDefined, "DOWNLOAD_SETUP_PACKET_DUPLICATION_COUNT", &cfg.DownloadSetupPacketDuplicationCount, 4)
	setClientBool(isDefined, "DUPLICATION_PREFER_DISTINCT_DOMAINS", &cfg.DuplicationPreferDistinctDomains, true)
	setClientBool(isDefined, "ADAPTIVE_DUPLICATION", &cfg.AdaptiveDuplication, true)
	setClientFloat(isDefined, "ADAPTIVE_DUPLICATION_TARGET_DELIVERY", &cfg.AdaptiveDuplicationTargetDelivery, 0.95)
	setClientInt(isDefined, "MTU_PROBE_SAMPLES", &cfg.MTUProbeSamples, 4)
	setClientFloat(isDefined, "MTU_MAX_LOSS", &cfg.MTUMaxLoss, 0.25)
	setClientInt(isDefined, "MTU_TEST_PARALLELISM_RESOLVERS", &cfg.MTUTestParallelismResolvers, 32)
	setClientFloat(isDefined, "MTU_TEST_TIMEOUT_RESOLVERS", &cfg.MTUTestTimeoutResolvers, 3.0)
	setClientInt(isDefined, "MAX_PACKETS_PER_BATCH", &cfg.MaxPacketsPerBatch, 12)
	setClientInt(isDefined, "ARQ_WINDOW_SIZE", &cfg.ARQWindowSize, 1500)
	setClientFloat(isDefined, "ARQ_INITIAL_RTO_SECONDS", &cfg.ARQInitialRTOSeconds, 0.5)
	setClientFloat(isDefined, "ARQ_MAX_RTO_SECONDS", &cfg.ARQMaxRTOSeconds, 3.0)
	setClientFloat(isDefined, "ARQ_DATA_NACK_INITIAL_DELAY_SECONDS", &cfg.ARQDataNackInitialDelaySeconds, 0.3)
	setClientFloat(isDefined, "ARQ_DATA_NACK_REPEAT_SECONDS", &cfg.ARQDataNackRepeatSeconds, 0.6)
	setClientInt(isDefined, "UPLOAD_COMPRESSION_TYPE", &cfg.UploadCompressionType, 2)
	setClientInt(isDefined, "DOWNLOAD_COMPRESSION_TYPE", &cfg.DownloadCompressionType, 2)
	setClientInt(isDefined, "COMPRESSION_MIN_SIZE", &cfg.CompressionMinSize, 180)
	setClientInt(isDefined, "QNAME_LABEL_LENGTH", &cfg.QNameLabelLength, 63)
	setClientInt(isDefined, "EDNS_UDP_SIZE", &cfg.EDNSUDPSize, 4096)
	setClientStrings(isDefined, "QUERY_TYPES", &cfg.QueryTypes, []string{"TXT", "HTTPS"})
}

func applyServerSpeedPreset(cfg *ServerConfig, isDefined configKeyDefinedFunc) {
	setServerBool(isDefined, "TCP_LISTENER_ENABLED", &cfg.TCPListenerEnabled, true)
	setServerInt(isDefined, "TCP_MAX_CONNS", &cfg.TCPMaxConns, 4096)
	setServerInt(isDefined, "TCP_MAX_CONNS_PER_IP", &cfg.TCPMaxConnsPerIP, 256)
	setServerInt(isDefined, "TCP_MAX_QUERIES_PER_CONN", &cfg.TCPMaxQueriesPerConn, 0)
	setServerFloat(isDefined, "TCP_READ_IDLE_TIMEOUT_SECONDS", &cfg.TCPReadIdleTimeoutSeconds, 45.0)
	setServerFloat(isDefined, "TCP_WRITE_TIMEOUT_SECONDS", &cfg.TCPWriteTimeoutSeconds, 15.0)
	setServerInt(isDefined, "MAX_CONCURRENT_REQUESTS", &cfg.MaxConcurrentRequests, 32768)
	setServerInt(isDefined, "MAX_PACKETS_PER_BATCH", &cfg.MaxPacketsPerBatch, 12)
	setServerInt(isDefined, "ARQ_WINDOW_SIZE", &cfg.ARQWindowSize, 3000)
	setServerFloat(isDefined, "ARQ_INITIAL_RTO_SECONDS", &cfg.ARQInitialRTOSeconds, 0.5)
	setServerFloat(isDefined, "ARQ_MAX_RTO_SECONDS", &cfg.ARQMaxRTOSeconds, 3.0)
	setServerFloat(isDefined, "ARQ_DATA_NACK_INITIAL_DELAY_SECONDS", &cfg.ARQDataNackInitialDelaySeconds, 0.3)
	setServerFloat(isDefined, "ARQ_DATA_NACK_REPEAT_SECONDS", &cfg.ARQDataNackRepeatSeconds, 0.6)
	setServerBool(isDefined, "FEC_AUTO_ENABLED", &cfg.FECAutoEnabled, true)
	setServerFloat(isDefined, "FEC_AUTO_LOSS_THRESHOLD", &cfg.FECAutoLossThreshold, 0.25)
	setServerInts(isDefined, "SUPPORTED_UPLOAD_COMPRESSION_TYPES", &cfg.SupportedUploadCompressionTypes, []int{0, 1, 2, 3})
	setServerInts(isDefined, "SUPPORTED_DOWNLOAD_COMPRESSION_TYPES", &cfg.SupportedDownloadCompressionTypes, []int{0, 1, 2, 3})
}

func applyServerSurvivalPreset(cfg *ServerConfig, isDefined configKeyDefinedFunc) {
	setServerBool(isDefined, "TCP_LISTENER_ENABLED", &cfg.TCPListenerEnabled, true)
	setServerInt(isDefined, "TCP_MAX_CONNS", &cfg.TCPMaxConns, 4096)
	setServerInt(isDefined, "TCP_MAX_CONNS_PER_IP", &cfg.TCPMaxConnsPerIP, 256)
	setServerInt(isDefined, "TCP_MAX_QUERIES_PER_CONN", &cfg.TCPMaxQueriesPerConn, 0)
	setServerFloat(isDefined, "TCP_READ_IDLE_TIMEOUT_SECONDS", &cfg.TCPReadIdleTimeoutSeconds, 60.0)
	setServerFloat(isDefined, "TCP_WRITE_TIMEOUT_SECONDS", &cfg.TCPWriteTimeoutSeconds, 20.0)
	setServerInt(isDefined, "MAX_CONCURRENT_REQUESTS", &cfg.MaxConcurrentRequests, 32768)
	setServerInt(isDefined, "MAX_PACKETS_PER_BATCH", &cfg.MaxPacketsPerBatch, 8)
	setServerInt(isDefined, "PACKET_BLOCK_CONTROL_DUPLICATION", &cfg.PacketBlockControlDuplication, 2)
	setServerBool(isDefined, "FEC_AUTO_ENABLED", &cfg.FECAutoEnabled, true)
	setServerFloat(isDefined, "FEC_AUTO_LOSS_THRESHOLD", &cfg.FECAutoLossThreshold, 0.2)
	setServerInt(isDefined, "FEC_BLOCK_SIZE", &cfg.FECBlockSize, 4)
	setServerInt(isDefined, "FEC_PARITY", &cfg.FECParity, 4)
	setServerInt(isDefined, "FEC_AUTO_MAX_PARITY", &cfg.FECAutoMaxParity, 16)
	setServerFloat(isDefined, "DNS_INFLIGHT_WAIT_TIMEOUT_SECONDS", &cfg.DNSInflightWaitTimeoutSecs, 10.0)
	setServerInts(isDefined, "SUPPORTED_UPLOAD_COMPRESSION_TYPES", &cfg.SupportedUploadCompressionTypes, []int{0, 1, 2, 3})
	setServerInts(isDefined, "SUPPORTED_DOWNLOAD_COMPRESSION_TYPES", &cfg.SupportedDownloadCompressionTypes, []int{0, 1, 2, 3})
}

func applyServerTCPSurvivalPreset(cfg *ServerConfig, isDefined configKeyDefinedFunc) {
	setServerBool(isDefined, "TCP_LISTENER_ENABLED", &cfg.TCPListenerEnabled, true)
	setServerInt(isDefined, "TCP_MAX_CONNS", &cfg.TCPMaxConns, 4096)
	setServerInt(isDefined, "TCP_MAX_CONNS_PER_IP", &cfg.TCPMaxConnsPerIP, 256)
	setServerInt(isDefined, "TCP_MAX_QUERIES_PER_CONN", &cfg.TCPMaxQueriesPerConn, 0)
	setServerFloat(isDefined, "TCP_READ_IDLE_TIMEOUT_SECONDS", &cfg.TCPReadIdleTimeoutSeconds, 75.0)
	setServerFloat(isDefined, "TCP_WRITE_TIMEOUT_SECONDS", &cfg.TCPWriteTimeoutSeconds, 20.0)
	setServerInt(isDefined, "MAX_CONCURRENT_REQUESTS", &cfg.MaxConcurrentRequests, 32768)
	setServerInt(isDefined, "MAX_PACKETS_PER_BATCH", &cfg.MaxPacketsPerBatch, 12)
	setServerInt(isDefined, "ARQ_WINDOW_SIZE", &cfg.ARQWindowSize, 3000)
	setServerFloat(isDefined, "ARQ_INITIAL_RTO_SECONDS", &cfg.ARQInitialRTOSeconds, 0.5)
	setServerFloat(isDefined, "ARQ_MAX_RTO_SECONDS", &cfg.ARQMaxRTOSeconds, 3.0)
	setServerBool(isDefined, "FEC_AUTO_ENABLED", &cfg.FECAutoEnabled, true)
	setServerFloat(isDefined, "FEC_AUTO_LOSS_THRESHOLD", &cfg.FECAutoLossThreshold, 0.25)
	setServerInts(isDefined, "SUPPORTED_UPLOAD_COMPRESSION_TYPES", &cfg.SupportedUploadCompressionTypes, []int{0, 1, 2, 3})
	setServerInts(isDefined, "SUPPORTED_DOWNLOAD_COMPRESSION_TYPES", &cfg.SupportedDownloadCompressionTypes, []int{0, 1, 2, 3})
}

func setClientBool(isDefined configKeyDefinedFunc, key string, dst *bool, value bool) {
	if configKeyUnset(isDefined, key) {
		*dst = value
	}
}

func setClientInt(isDefined configKeyDefinedFunc, key string, dst *int, value int) {
	if configKeyUnset(isDefined, key) {
		*dst = value
	}
}

func setClientFloat(isDefined configKeyDefinedFunc, key string, dst *float64, value float64) {
	if configKeyUnset(isDefined, key) {
		*dst = value
	}
}

func setClientString(isDefined configKeyDefinedFunc, key string, dst *string, value string) {
	if configKeyUnset(isDefined, key) {
		*dst = value
	}
}

func setClientStrings(isDefined configKeyDefinedFunc, key string, dst *[]string, value []string) {
	if configKeyUnset(isDefined, key) {
		*dst = append((*dst)[:0], value...)
	}
}

func setServerBool(isDefined configKeyDefinedFunc, key string, dst *bool, value bool) {
	if configKeyUnset(isDefined, key) {
		*dst = value
	}
}

func setServerInt(isDefined configKeyDefinedFunc, key string, dst *int, value int) {
	if configKeyUnset(isDefined, key) {
		*dst = value
	}
}

func setServerFloat(isDefined configKeyDefinedFunc, key string, dst *float64, value float64) {
	if configKeyUnset(isDefined, key) {
		*dst = value
	}
}

func setServerInts(isDefined configKeyDefinedFunc, key string, dst *[]int, value []int) {
	if configKeyUnset(isDefined, key) {
		*dst = append((*dst)[:0], value...)
	}
}
