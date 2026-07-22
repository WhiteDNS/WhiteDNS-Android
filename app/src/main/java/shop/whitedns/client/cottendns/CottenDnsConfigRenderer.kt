package shop.whitedns.client.cottendns

import shop.whitedns.client.model.ConnectionProfile
import shop.whitedns.client.model.ResolvedWhiteDnsSettings
import shop.whitedns.client.model.CottenDnsServerProfile
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.normalizedResolverProfiles
import shop.whitedns.client.model.resolve
import shop.whitedns.client.model.runtimeConnectionSettings

object CottenDnsConfigRenderer {

    fun renderClientToml(
        connectionProfile: ConnectionProfile,
        settings: WhiteDnsSettings,
    ): String {
        val resolverProfile = settings.normalizedResolverProfiles()
            .firstOrNull { it.id == connectionProfile.resolverProfileId }
        val exportSettings = settings.copy(
            selectedConnectionProfileId = connectionProfile.id,
            selectedResolverProfileId = resolverProfile?.id.orEmpty(),
            resolverText = resolverProfile?.resolverText ?: settings.resolverText,
            connectionMode = when (connectionProfile.connectionMode) {
                "proxy", "vpn" -> connectionProfile.connectionMode
                else -> settings.connectionMode
            },
        ).runtimeConnectionSettings()
        return renderClientToml(
            serverProfile = connectionProfile.toCottenDnsServerProfile(),
            settings = exportSettings,
        )
    }

    fun renderClientToml(
        serverProfile: CottenDnsServerProfile,
        settings: WhiteDnsSettings,
    ): String {
        val resolved = settings.resolve()

        return buildString {
            appendLine("DOMAINS = ${domainsToml(serverProfile.domain)}")
            appendLine("DATA_ENCRYPTION_METHOD = ${serverProfile.encryptionMethod}")
            appendLine("ENCRYPTION_KEY = \"${escape(serverProfile.encryptionKey)}\"")
            appendLine("PROTOCOL_TYPE = \"${escape(resolved.protocolType)}\"")
            appendServerTypeToml(serverProfile.serverType, resolved.configPreset, resolved.transportMode, resolved.deliveryMode, resolved.qnameMode, resolved)
            appendClientSettingsToml(resolved)
        }.trimEnd()
    }

    // Emits the per-connection wire-format and feature boundary. Compatibility
    // preserves the legacy session width and conservative DNS shape; safe
    // client-only recovery remains shared, while CottenDns-only transport and
    // traffic-amplifying features are isolated to native profiles.
    private fun StringBuilder.appendServerTypeToml(
        serverType: String,
        configPreset: String,
        transportMode: String,
        deliveryMode: String,
        qnameMode: String,
        resolved: ResolvedWhiteDnsSettings,
    ) {
        val isCompatibility =
            ConnectionProfile.normalizeServerType(serverType) == ConnectionProfile.ServerTypeCompatibility
        val preset = normalizeConfigPreset(configPreset)

        // CONFIG_PRESET only accepts the engine's own presets; the app-level
        // "master-storm" preset expands to explicit legacy-safe keys over a
        // default base (explicit keys always win over the preset in the engine).
        appendLine("CONFIG_PRESET = \"${escape(enginePresetBase(preset, isCompatibility))}\"")
        appendLine("LEGACY_SESSION_ID = $isCompatibility")

        // Server-generation-sensitive knobs. Compatibility forces the safe subset
        // so a MasterDNS/StormDNS server always works. Otherwise an explicit user
        // override wins over the preset-derived value; "preset" defers to the preset.
        val transport = when {
            isCompatibility -> "udp"
            transportMode != "preset" -> transportMode
            else -> resolverTransport(preset)
        }
        val queryTypes = when {
            isCompatibility -> listOf("TXT")
            deliveryMode != "preset" -> deliveryTypesFor(deliveryMode)
            else -> queryTypeSet(preset)
        }
        appendLine("RESOLVER_TRANSPORT = \"$transport\"")
        appendEncryptedResolverToml(transport, resolved)
        appendLine("QUERY_TYPES = ${queryTypesToml(queryTypes)}")

        // DNS query-id/EDNS/NXDOMAIN hardening applies to the resolver hop, not
        // the tunnel server, so it is safe for both generations.
        //
        // QNAME reshaping (shorter labels) is reassembled correctly by CottenDns
        // and by the StormDNS engine (both strip the domain and remove all label
        // dots regardless of length). But older MasterDNS variants are not
        // verified here, so Compatibility mode forces the classic 63-char labels
        // to guarantee legacy connectivity; CottenDns keeps preset-driven reshaping.
        val qnameLen = when {
            isCompatibility -> 63
            qnameMode == "off" -> 63
            qnameMode == "moderate" -> 42
            qnameMode == "aggressive" -> 32
            else -> qnameLabelLength(preset)
        }
        appendLine("QNAME_LABEL_LENGTH = $qnameLen")

        // These protections are entirely client/resolver-side and do not change
        // the tunnel protocol, so both CottenDns and legacy Storm/Master profiles
        // retain them.
        appendLine("RESOLVER_RATE_LIMIT_ENABLED = true")
        appendLine("DNS_RANDOMIZE_QUERY_ID = true")
        appendLine("DNS_QNAME_CASE_RANDOMIZATION = false")
        appendLine("RESOLVER_IGNORE_INJECTED_NXDOMAIN = true")

        // CottenDns-only optimization suite. Adaptive/domain-diverse duplication
        // can amplify query volume and EDNS cookies change the DNS wire shape, so
        // neither is allowed to leak into a Storm/Master compatibility profile.
        // Compatibility explicitly disables them even when the app's global
        // settings currently name a CottenDns speed/survival preset.
        if (!isCompatibility) {
			// Native clients begin at one copy and add independent-path copies only
			// when measured loss justifies them. This preserves bandwidth on clean
			// links while retaining survival behavior when filtering becomes lossy.
			appendLine("ADAPTIVE_DUPLICATION = true")
			appendLine("DUPLICATION_PREFER_DISTINCT_DOMAINS = true")
            appendLine("ADAPTIVE_DUPLICATION_TARGET_DELIVERY = ${adaptiveDuplicationTarget(preset)}")
            appendLine("DNS_EDNS_COOKIE = true")
            appendLine("EDNS_UDP_SIZE = ${ednsUdpSize(preset)}")
            // CottenDns MTU style: adaptive per-group MTU runs each resolver group
            // at its own throughput-optimal operating point.
            appendLine("MTU_PROBE_SAMPLES = ${mtuProbeSamples(preset)}")
            appendLine("MTU_MAX_LOSS = ${mtuMaxLoss(preset)}")
            appendLine("MTU_ADAPTIVE_GROUPING = true")
            appendLine("MTU_GROUP_GAP_RATIO = 0.25")
        } else {
            appendLine("ADAPTIVE_DUPLICATION = false")
            appendLine("DUPLICATION_PREFER_DISTINCT_DOMAINS = false")
            appendLine("DNS_EDNS_COOKIE = false")
            // Master/Storm DNS MTU style: the classic single global MTU scan the
            // original engine used (one synced MTU across resolvers), not CottenDns's
            // adaptive per-group MTU, with a simple one-sample probe. Emitting these
            // explicitly keeps the two scans from conflicting when a profile switches.
            appendLine("MTU_ADAPTIVE_GROUPING = false")
            appendLine("MTU_PROBE_SAMPLES = 1")
            appendLine("MTU_MAX_LOSS = 0.0")
        }
    }


    private fun normalizeConfigPreset(configPreset: String): String {
        return when (configPreset.trim().lowercase()) {
            "speed" -> "speed"
            "survival" -> "survival"
            "tcp", "tcp-survival", "tcp_survival" -> "tcp-survival"
            "master", "storm", "master-storm", "master_storm" -> "master-storm"
            else -> "default"
        }
    }

    // Maps an app-level preset to the engine's own CONFIG_PRESET vocabulary.
    private fun enginePresetBase(preset: String, isCompatibility: Boolean): String {
        if (isCompatibility) {
            // Never let a CottenDns engine preset silently enable native-only
            // transport or traffic-amplifying behavior on a legacy connection.
            return "default"
        }
        return when (preset) {
            "speed", "survival", "tcp-survival" -> preset
            else -> "default" // "default" and "master-storm"
        }
    }

    private fun resolverTransport(configPreset: String): String {
        return when (configPreset) {
            "tcp-survival" -> "tcp"
            "master-storm" -> "udp"
            else -> "auto"
        }
    }

    private fun adaptiveDuplicationTarget(configPreset: String): String {
        return if (configPreset == "survival") "0.97" else "0.95"
    }

    private fun ednsUdpSize(configPreset: String): Int {
        return if (configPreset == "survival") 1232 else 4096
    }

    private fun qnameLabelLength(configPreset: String): Int {
        return if (configPreset == "survival") 42 else 63
    }

    // CottenDns MTU scan. Its distinct feature vs the legacy Master/Storm scan is
    // adaptive per-group MTU (MTU_ADAPTIVE_GROUPING = true), not the probe count:
    // the fast desktop client uses a single-sample probe, so the default does too.
    // Multi-sample loss-aware probing is heavier (more scan traffic) and is
    // reserved for the survival preset on very lossy links.
    private fun mtuProbeSamples(configPreset: String): Int {
        return when (configPreset) {
            "survival" -> 5
            else -> 1
        }
    }

    private fun mtuMaxLoss(configPreset: String): String {
        return when (configPreset) {
            "survival" -> "0.5"
            else -> "0.0"
        }
    }

    private fun queryTypeSet(configPreset: String): List<String> {
        // Default is TXT-only, matching the fast desktop client: on filtered
        // networks non-TXT records (NULL/HTTPS especially) are often dropped, so
        // rotating to them causes packet loss and retransmits. Richer rotation is
        // opt-in via the delivery dropdown or the survival preset.
        return when (configPreset) {
            "speed" -> listOf("TXT", "HTTPS")
            "survival" -> listOf("TXT", "CNAME", "HTTPS", "A")
            "tcp-survival" -> listOf("TXT", "HTTPS")
            else -> listOf("TXT")
        }
    }

    private fun queryTypesToml(types: List<String>): String {
        return types.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
    }

    // Maps a user delivery-method override to the concrete query-type set.
    private fun deliveryTypesFor(mode: String): List<String> {
        return when (mode) {
            "txt" -> listOf("TXT")
            "txt-cname" -> listOf("TXT", "CNAME")
            "txt-https" -> listOf("TXT", "HTTPS")
            "all" -> listOf("TXT", "CNAME", "NULL", "HTTPS")
            else -> listOf("TXT")
        }
    }

    fun renderScanClientToml(
        serverProfile: CottenDnsServerProfile,
        settings: WhiteDnsSettings,
    ): String {
        val resolved = settings.copy(
            startupMode = "resolvers",
            trafficWarmupEnabled = false,
        ).resolve()
        return buildString {
            appendLine("DOMAINS = ${domainsToml(serverProfile.domain)}")
            appendLine("DATA_ENCRYPTION_METHOD = ${serverProfile.encryptionMethod}")
            appendLine("ENCRYPTION_KEY = \"${escape(serverProfile.encryptionKey)}\"")
            appendLine("PROTOCOL_TYPE = \"${escape(resolved.protocolType)}\"")
            appendServerTypeToml(serverProfile.serverType, resolved.configPreset, resolved.transportMode, resolved.deliveryMode, resolved.qnameMode, resolved)
            appendClientSettingsToml(
                resolved = resolved,
                listenIp = "127.0.0.1",
                listenPort = 0,
                localDnsEnabled = false,
                localDnsPort = 0,
                // Scan probes resolvers in parallel using the dedicated scanner
                // resolver-parallelism setting (separate from the connect-time MTU
                // scan and from Parallel Test), so "how many resolvers are tested at
                // the same time" is controllable per scan. Each scan worker process
                // still gets its own shard; the engine caps the pool to the shard size.
                mtuTestParallelismResolvers = resolved.scanResolverParallelism,
            )
        }.trimEnd()
    }

    fun renderAdvancedSettingsToml(settings: WhiteDnsSettings): String {
        val resolved = settings.resolve()
        return buildString {
            appendAdvancedSettingsToml(resolved)
        }.trimEnd()
    }

    fun renderResolvers(settings: WhiteDnsSettings): String {
        return settings.resolve().resolverEntries.joinToString(separator = "\n")
    }

    private fun StringBuilder.appendClientSettingsToml(
        resolved: ResolvedWhiteDnsSettings,
        listenIp: String = resolved.listenIp,
        listenPort: Int = resolved.listenPort,
        localDnsEnabled: Boolean = resolved.localDnsEnabled,
        localDnsPort: Int = resolved.localDnsPort,
        mtuTestParallelismResolvers: Int = resolved.mtuTestParallelismResolvers,
    ) {
        appendLine("LISTEN_IP = \"${escape(listenIp)}\"")
        appendLine("LISTEN_PORT = $listenPort")
        appendLine("SOCKS5_AUTH = ${resolved.socks5Authentication}")
        appendLine("SOCKS5_USER = \"${escape(resolved.socksUsername)}\"")
        appendLine("SOCKS5_PASS = \"${escape(resolved.socksPassword)}\"")
        appendLine("LOCAL_DNS_ENABLED = $localDnsEnabled")
        appendLine("LOCAL_DNS_IP = \"127.0.0.1\"")
        appendLine("LOCAL_DNS_PORT = $localDnsPort")
        appendLine("RESOLVER_BALANCING_STRATEGY = ${resolved.balancingStrategy}")
        appendLine("UPLOAD_PACKET_DUPLICATION_COUNT = ${resolved.uploadDuplication}")
        appendLine("DOWNLOAD_PACKET_DUPLICATION_COUNT = ${resolved.downloadDuplication}")
        // Setup/control packets are duplicated more than bulk data so stream
        // establishment survives loss (engine clamps into [data-duplication, 8]).
        appendLine("UPLOAD_SETUP_PACKET_DUPLICATION_COUNT = 4")
        appendLine("DOWNLOAD_SETUP_PACKET_DUPLICATION_COUNT = 8")
        appendLine("UPLOAD_COMPRESSION_TYPE = ${resolved.uploadCompression}")
        appendLine("DOWNLOAD_COMPRESSION_TYPE = ${resolved.downloadCompression}")
        appendLine("BASE_ENCODE_DATA = ${resolved.baseEncodeData}")
        appendLine("MIN_UPLOAD_MTU = ${resolved.minUploadMtu}")
        appendLine("MIN_DOWNLOAD_MTU = ${resolved.minDownloadMtu}")
        appendLine("MAX_UPLOAD_MTU = ${resolved.maxUploadMtu}")
        appendLine("MAX_DOWNLOAD_MTU = ${resolved.maxDownloadMtu}")
        appendLine("MTU_TEST_RETRIES_RESOLVERS = ${resolved.mtuTestRetriesResolvers}")
        appendLine("MTU_TEST_TIMEOUT_RESOLVERS = ${resolved.mtuTestTimeoutResolvers}")
        appendLine("MTU_TEST_PARALLELISM_RESOLVERS = $mtuTestParallelismResolvers")
        appendLine("MTU_TEST_RETRIES_LOGS = ${resolved.mtuTestRetriesLogs}")
        appendLine("MTU_TEST_TIMEOUT_LOGS = ${resolved.mtuTestTimeoutLogs}")
        appendLine("MTU_TEST_PARALLELISM_LOGS = ${resolved.mtuTestParallelismLogs}")
        appendLine("FAST_CONNECT = ${resolved.fastConnectEnabled}")
        appendLine("RX_TX_WORKERS = ${resolved.rxTxWorkers}")
        appendLine("TUNNEL_PROCESS_WORKERS = ${resolved.tunnelProcessWorkers}")
        appendLine("TUNNEL_PACKET_TIMEOUT_SECONDS = ${resolved.tunnelPacketTimeoutSeconds}")
        appendLine("DISPATCHER_IDLE_POLL_INTERVAL_SECONDS = ${resolved.dispatcherIdlePollIntervalSeconds}")
        appendLine("TX_CHANNEL_SIZE = ${resolved.txChannelSize}")
        appendLine("RX_CHANNEL_SIZE = ${resolved.rxChannelSize}")
        appendLine("RESOLVER_UDP_CONNECTION_POOL_SIZE = ${resolved.resolverUdpConnectionPoolSize}")
        appendLine("STREAM_QUEUE_INITIAL_CAPACITY = ${resolved.streamQueueInitialCapacity}")
        appendLine("ORPHAN_QUEUE_INITIAL_CAPACITY = ${resolved.orphanQueueInitialCapacity}")
        appendLine("DNS_RESPONSE_FRAGMENT_STORE_CAPACITY = ${resolved.dnsResponseFragmentStoreCapacity}")
        appendLine("MAX_ACTIVE_STREAMS = ${resolved.maxActiveStreams}")
        appendLine("LOCAL_HANDSHAKE_TIMEOUT_SECONDS = ${resolved.localHandshakeTimeoutSeconds}")
        appendLine("SOCKS_UDP_ASSOCIATE_READ_TIMEOUT_SECONDS = ${resolved.socksUdpAssociateReadTimeoutSeconds}")
        appendLine("CLIENT_TERMINAL_STREAM_RETENTION_SECONDS = ${resolved.clientTerminalStreamRetentionSeconds}")
        appendLine("CLIENT_CANCELLED_SETUP_RETENTION_SECONDS = ${resolved.clientCancelledSetupRetentionSeconds}")
        appendLine("SESSION_INIT_RETRY_BASE_SECONDS = ${resolved.sessionInitRetryBaseSeconds}")
        appendLine("SESSION_INIT_RETRY_STEP_SECONDS = ${resolved.sessionInitRetryStepSeconds}")
        appendLine("SESSION_INIT_RETRY_LINEAR_AFTER = ${resolved.sessionInitRetryLinearAfter}")
        appendLine("SESSION_INIT_RETRY_MAX_SECONDS = ${resolved.sessionInitRetryMaxSeconds}")
        appendLine("SESSION_INIT_BUSY_RETRY_INTERVAL_SECONDS = ${resolved.sessionInitBusyRetryIntervalSeconds}")
        appendLine("STARTUP_MODE = \"${escape(resolved.startupMode)}\"")
        appendLine("LOG_SCAN_MAX_DAYS = 14")
        appendLine("LOG_SCAN_MAX_RESOLVERS = 128")
        appendLine("LOG_BASED_MTU_VERIFY = true")
        appendLine("STATS_REPORT_INTERVAL_SECONDS = 1.0")
        appendLine("PING_WATCHDOG_TIMEOUT_SECONDS = ${resolved.pingWatchdogSeconds}")
        appendLine("LOG_LEVEL = \"${escape(resolved.logLevel)}\"")
        appendLine("LOG_TO_FILE = true")
        appendLine("LOG_DIR = \"logs\"")
    }

    private fun StringBuilder.appendAdvancedSettingsToml(resolved: ResolvedWhiteDnsSettings) {
        appendLine("CONFIG_PRESET = \"${escape(resolved.configPreset)}\"")
        appendLine("LISTEN_IP = \"${escape(resolved.listenIp)}\"")
        appendLine("LISTEN_PORT = ${resolved.listenPort}")
        appendLine("HTTP_PROXY_ENABLED = ${resolved.httpProxyEnabled}")
        appendLine("HTTP_PROXY_PORT = ${resolved.httpProxyPort}")
        appendLine("SOCKS5_AUTH = ${resolved.socks5Authentication}")
        appendLine("SOCKS5_USER = \"${escape(resolved.socksUsername)}\"")
        appendLine("SOCKS5_PASS = \"${escape(resolved.socksPassword)}\"")
        appendLine("LOCAL_DNS_ENABLED = ${resolved.localDnsEnabled}")
        appendLine("LOCAL_DNS_PORT = ${resolved.localDnsPort}")
        appendLine("RESOLVER_BALANCING_STRATEGY = ${resolved.balancingStrategy}")
        appendLine("UPLOAD_PACKET_DUPLICATION_COUNT = ${resolved.uploadDuplication}")
        appendLine("DOWNLOAD_PACKET_DUPLICATION_COUNT = ${resolved.downloadDuplication}")
        // Setup/control packets are duplicated more than bulk data so stream
        // establishment survives loss (engine clamps into [data-duplication, 8]).
        appendLine("UPLOAD_SETUP_PACKET_DUPLICATION_COUNT = 4")
        appendLine("DOWNLOAD_SETUP_PACKET_DUPLICATION_COUNT = 8")
        appendLine("UPLOAD_COMPRESSION_TYPE = ${resolved.uploadCompression}")
        appendLine("DOWNLOAD_COMPRESSION_TYPE = ${resolved.downloadCompression}")
        appendLine("BASE_ENCODE_DATA = ${resolved.baseEncodeData}")
        appendLine("MIN_UPLOAD_MTU = ${resolved.minUploadMtu}")
        appendLine("MIN_DOWNLOAD_MTU = ${resolved.minDownloadMtu}")
        appendLine("MAX_UPLOAD_MTU = ${resolved.maxUploadMtu}")
        appendLine("MAX_DOWNLOAD_MTU = ${resolved.maxDownloadMtu}")
        appendLine("MTU_TEST_RETRIES_RESOLVERS = ${resolved.mtuTestRetriesResolvers}")
        appendLine("MTU_TEST_TIMEOUT_RESOLVERS = ${resolved.mtuTestTimeoutResolvers}")
        appendLine("MTU_TEST_PARALLELISM_RESOLVERS = ${resolved.mtuTestParallelismResolvers}")
        appendLine("MTU_TEST_RETRIES_LOGS = ${resolved.mtuTestRetriesLogs}")
        appendLine("MTU_TEST_TIMEOUT_LOGS = ${resolved.mtuTestTimeoutLogs}")
        appendLine("MTU_TEST_PARALLELISM_LOGS = ${resolved.mtuTestParallelismLogs}")
        appendLine("RX_TX_WORKERS = ${resolved.rxTxWorkers}")
        appendLine("TUNNEL_PROCESS_WORKERS = ${resolved.tunnelProcessWorkers}")
        appendLine("TUNNEL_PACKET_TIMEOUT_SECONDS = ${resolved.tunnelPacketTimeoutSeconds}")
        appendLine("DISPATCHER_IDLE_POLL_INTERVAL_SECONDS = ${resolved.dispatcherIdlePollIntervalSeconds}")
        appendLine("TX_CHANNEL_SIZE = ${resolved.txChannelSize}")
        appendLine("RX_CHANNEL_SIZE = ${resolved.rxChannelSize}")
        appendLine("RESOLVER_UDP_CONNECTION_POOL_SIZE = ${resolved.resolverUdpConnectionPoolSize}")
        appendLine("STREAM_QUEUE_INITIAL_CAPACITY = ${resolved.streamQueueInitialCapacity}")
        appendLine("ORPHAN_QUEUE_INITIAL_CAPACITY = ${resolved.orphanQueueInitialCapacity}")
        appendLine("DNS_RESPONSE_FRAGMENT_STORE_CAPACITY = ${resolved.dnsResponseFragmentStoreCapacity}")
        appendLine("MAX_ACTIVE_STREAMS = ${resolved.maxActiveStreams}")
        appendLine("LOCAL_HANDSHAKE_TIMEOUT_SECONDS = ${resolved.localHandshakeTimeoutSeconds}")
        appendLine("SOCKS_UDP_ASSOCIATE_READ_TIMEOUT_SECONDS = ${resolved.socksUdpAssociateReadTimeoutSeconds}")
        appendLine("CLIENT_TERMINAL_STREAM_RETENTION_SECONDS = ${resolved.clientTerminalStreamRetentionSeconds}")
        appendLine("CLIENT_CANCELLED_SETUP_RETENTION_SECONDS = ${resolved.clientCancelledSetupRetentionSeconds}")
        appendLine("SESSION_INIT_RETRY_BASE_SECONDS = ${resolved.sessionInitRetryBaseSeconds}")
        appendLine("SESSION_INIT_RETRY_STEP_SECONDS = ${resolved.sessionInitRetryStepSeconds}")
        appendLine("SESSION_INIT_RETRY_LINEAR_AFTER = ${resolved.sessionInitRetryLinearAfter}")
        appendLine("SESSION_INIT_RETRY_MAX_SECONDS = ${resolved.sessionInitRetryMaxSeconds}")
        appendLine("SESSION_INIT_BUSY_RETRY_INTERVAL_SECONDS = ${resolved.sessionInitBusyRetryIntervalSeconds}")
        appendLine("STARTUP_MODE = \"${escape(resolved.startupMode)}\"")
        appendLine("PING_WATCHDOG_TIMEOUT_SECONDS = ${resolved.pingWatchdogSeconds}")
        appendLine("TRAFFIC_WARMUP_ENABLED = ${resolved.trafficWarmupEnabled}")
        appendLine("TRAFFIC_WARMUP_PROBE_COUNT = ${resolved.trafficWarmupProbeCount}")
        appendLine("TRAFFIC_KEEPALIVE_INTERVAL_SECONDS = ${resolved.trafficKeepaliveIntervalSeconds}")
        appendLine("AUTO_TUNE_ENABLED = ${resolved.autoTuneEnabled}")
        appendLine("LOG_LEVEL = \"${escape(resolved.logLevel)}\"")
    }

    // CottenDns supports multiple tunnel domains. The profile stores them as a
    // comma-separated string; render each as its own DOMAINS array entry.
    // Emits the encrypted-resolver keys, and only for the transports that use
    // them, so a UDP/TCP config stays exactly as it was before DoT/DoH existed.
    //
    // The hostname is deliberately NOT defaulted to the tunnel domain. The common
    // case is a public resolver (Cloudflare, Google, Quad9), which is reached at
    // its own IP and whose certificate carries that IP as a SAN — so leaving the
    // key unset lets the engine verify against the resolver's own identity and
    // works with no configuration. Sending the tunnel domain as SNI to a public
    // resolver would instead fail verification and silently drop back to UDP/TCP.
    // Pointing the client straight at your own DoT/DoH server is the case that
    // needs a hostname, and that is what the setting is for.
    private fun StringBuilder.appendEncryptedResolverToml(
        transport: String,
        resolved: ResolvedWhiteDnsSettings,
    ) {
        if (transport != "dot" && transport != "doh") {
            return
        }

        if (resolved.resolverTlsServerName.isNotEmpty()) {
            appendLine("RESOLVER_TLS_SERVER_NAME = \"${escape(resolved.resolverTlsServerName)}\"")
        }
        if (resolved.resolverTlsPin.isNotEmpty()) {
            appendLine("RESOLVER_TLS_PIN = \"${escape(resolved.resolverTlsPin)}\"")
        }
        if (transport == "dot") {
            appendLine("RESOLVER_DOT_PORT = ${resolved.resolverDoTPort}")
        } else {
            appendLine("RESOLVER_DOH_PORT = ${resolved.resolverDoHPort}")
            appendLine("RESOLVER_DOH_PATH = \"${escape(resolved.resolverDoHPath)}\"")
        }
    }

    private fun domainsToml(domain: String): String {
        val domains = domain.split(',')
            .map { it.trim().trimEnd('.') }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf(domain.trim().trimEnd('.')) }
        return domains.joinToString(prefix = "[", postfix = "]", separator = ", ") { "\"${escape(it)}\"" }
    }

    private fun escape(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }

    private fun ConnectionProfile.toCottenDnsServerProfile(): CottenDnsServerProfile {
        val domain = customServerDomain.trim().trimEnd('.')
        val encryptionKey = customServerEncryptionKey.trim()
        if (domain.isBlank() || encryptionKey.isBlank()) {
            throw IllegalArgumentException("Custom server domain and encryption key are required to export TOML")
        }
        return CottenDnsServerProfile(
            id = id.ifBlank { "custom" },
            label = name.ifBlank { "Custom CottenDns Server" },
            domain = domain,
            encryptionKey = encryptionKey,
            encryptionMethod = customServerEncryptionMethod.coerceIn(0, 5),
            serverType = ConnectionProfile.normalizeServerType(serverType),
        )
    }
}
