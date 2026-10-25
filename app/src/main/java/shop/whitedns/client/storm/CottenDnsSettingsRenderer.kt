package shop.whitedns.client.storm

import shop.whitedns.client.model.CottenDnsProfileSettings
import shop.whitedns.client.model.CottenMaxOptions

/**
 * CottenDNS-only TOML, ported from the cottendns-engine-ui branch's
 * CottenDnsConfigRenderer. StormDNS profiles never reach this: StormDNS rejects
 * these keys, so [StormDnsConfigRenderer] gates the call on the engine.
 *
 * Layering rule throughout: Compatibility (a legacy MasterDNS/StormDNS server
 * driven by the CottenDNS binary) forces the safe subset; otherwise an explicit
 * user override wins over the preset, and "preset" defers to it.
 */
internal object CottenDnsSettingsRenderer {

    fun StringBuilder.appendCottenDnsSettingsToml(
        settings: CottenDnsProfileSettings,
        escape: (String) -> String,
        primaryDomain: String = "",
    ) {
        val cotten = settings.normalized()
        val isCompatibility = cotten.isCompatibility
        val preset = cotten.configPreset

        // CONFIG_PRESET only accepts the engine's own presets; the app-level
        // "master-storm" preset expands to explicit legacy-safe keys over a
        // default base (explicit keys always win over the preset in the engine).
        appendLine("CONFIG_PRESET = \"${escape(enginePresetBase(preset, isCompatibility))}\"")
        appendLine("LEGACY_SESSION_ID = $isCompatibility")
        if (!isCompatibility) {
            appendLine("TUNNEL_MODE = \"${cotten.tunnelMode}\"")
            appendCottenOptionsToml(cotten, primaryDomain, escape)
        }
        appendLine("TERMINAL_UI = \"plain\"")
        appendLine("RESOLVER_IP_MODE = \"${resolverIpMode(cotten)}\"")

        // Query rate limit and timing mask. Client-side only (no wire change),
        // so Compatibility keeps them: a rate-limited network blocks legacy
        // servers just the same. Any cap makes the engine drop duplication to 1.
        val rate = cotten.queryRateLimit.toIntOrNull() ?: 0
        appendLine("QUERY_RATE_LIMIT_PER_SECOND = ${if (cotten.queryRateScope == "total") rate else 0}")
        appendLine("QUERY_RATE_LIMIT_PER_RESOLVER_PER_SECOND = ${if (cotten.queryRateScope == "resolver") rate else 0}")
        appendLine("QUERY_RATE_LIMIT_PER_DOMAIN_PER_SECOND = ${if (cotten.queryRateScope == "domain") rate else 0}")
        val jitter = when (cotten.timingMask) {
            "light" -> 0.3
            "strong" -> 0.6
            else -> 0.0
        }
        appendLine("QUERY_TIMING_JITTER = $jitter")

        val transport = when {
            isCompatibility -> "udp"
            cotten.transportMode != CottenDnsProfileSettings.ModePreset -> cotten.transportMode
            else -> resolverTransport(preset)
        }
        val queryTypes = when {
            isCompatibility -> listOf("TXT")
            cotten.deliveryMode != CottenDnsProfileSettings.ModePreset -> deliveryTypesFor(cotten.deliveryMode)
            else -> queryTypeSet(preset)
        }
        appendLine("RESOLVER_TRANSPORT = \"$transport\"")
        appendEncryptedResolverToml(transport, cotten, escape)
        appendLine("QUERY_TYPES = ${queryTypesToml(queryTypes)}")

        // QNAME reshaping is reassembled correctly by CottenDNS and StormDNS, but
        // older MasterDNS variants are unverified, so Compatibility pins the
        // classic 63-char labels.
        val qnameLen = when {
            isCompatibility -> 63
            cotten.qnameMode == "off" -> 63
            cotten.qnameMode == "moderate" -> 42
            cotten.qnameMode == "aggressive" -> 32
            else -> qnameLabelLength(preset)
        }
        appendLine("QNAME_LABEL_LENGTH = $qnameLen")

        // Client-side startup behavior, independent of the server generation.
        // On the cottendns-engine-ui branch this comes from shared client
        // settings; here it stays in the CottenDNS block because StormDNS has no
        // such key.
        appendLine("FAST_CONNECT = true")

        // Only the sweep that continues behind a live tunnel. The initial scan
        // stays on the shared MTU_TEST_PARALLELISM_RESOLVERS so connecting is
        // not slowed down. CottenDNS-only key; older engines ignore it and keep
        // their hard-coded single worker.
        appendLine("MTU_BACKGROUND_PARALLELISM = ${cotten.backgroundScanParallelism}")

        // Resolver-hop hardening: does not change the tunnel protocol, so both
        // server generations keep it.
        appendLine("RESOLVER_RATE_LIMIT_ENABLED = true")
        appendLine("DNS_RANDOMIZE_QUERY_ID = true")
        appendLine("DNS_QNAME_CASE_RANDOMIZATION = false")
        appendLine("RESOLVER_IGNORE_INJECTED_NXDOMAIN = true")

        // CottenDNS-only optimization suite. Adaptive duplication amplifies query
        // volume and EDNS cookies change the wire shape, so neither may leak into
        // a legacy profile even when the preset names a CottenDNS mode.
        if (!isCompatibility) {
            appendLine("ADAPTIVE_DUPLICATION = true")
            appendLine("DUPLICATION_PREFER_DISTINCT_DOMAINS = true")
            appendLine("ADAPTIVE_DUPLICATION_TARGET_DELIVERY = ${adaptiveDuplicationTarget(preset)}")
            appendLine("DNS_EDNS_COOKIE = true")
            appendLine("EDNS_UDP_SIZE = ${ednsUdpSize(preset)}")
            appendLine("MTU_PROBE_SAMPLES = ${mtuProbeSamples(preset)}")
            appendLine("MTU_MAX_LOSS = ${mtuMaxLoss(preset)}")
            appendLine("MTU_ADAPTIVE_GROUPING = true")
            appendLine("MTU_GROUP_GAP_RATIO = 0.25")
        } else {
            // Classic single global MTU scan. Emitting these explicitly keeps the
            // two scan styles from conflicting when a profile switches type.
            appendLine("ADAPTIVE_DUPLICATION = false")
            appendLine("DUPLICATION_PREFER_DISTINCT_DOMAINS = false")
            appendLine("DNS_EDNS_COOKIE = false")
            appendLine("MTU_ADAPTIVE_GROUPING = false")
            appendLine("MTU_PROBE_SAMPLES = 1")
            appendLine("MTU_MAX_LOSS = 0.0")
        }
    }

    private fun StringBuilder.appendCottenOptionsToml(
        cotten: CottenDnsProfileSettings,
        primaryDomain: String,
        escape: (String) -> String,
    ) {
        val options = cotten.cottenMaxOptions.normalized()
        val transport = if (cotten.transportMode == "preset") resolverTransport(cotten.configPreset) else cotten.transportMode
        if (transport in setOf("dot", "doh", "doh3", "doq") && options.dnsPaddingBlockSize > 0) {
            appendLine("DNS_PADDING_BLOCK_SIZE = ${options.dnsPaddingBlockSize}")
        }
        if (transport == "doh3" && options.doh3FallbackToDoh) {
            appendLine("RESOLVER_HTTP3_FALLBACK = true")
        }
        if (!cotten.usesCottenMax) return
        appendLine("COTTENMAX_TRAFFIC_PROFILE = \"${options.trafficProfile}\"")
        appendLine("COTTENMAX_LOW_REDUCTION_PERCENT = ${options.lowReductionPercent}")
        val primary = primaryDomain.split(',').map { it.trim().trimEnd('.').lowercase() }.distinct()
        val standbys = CottenMaxOptions.entries(options.standbyDomains).filterNot { it in primary }.take((8 - primary.size).coerceAtLeast(0))
        if (standbys.isNotEmpty()) appendLine("STANDBY_DOMAINS = ${standbys.joinToString(prefix = "[", postfix = "]") { "\"${escape(it)}\"" }}")
        appendLine("COTTENMAX_FAILOVER_AFTER_MILLISECONDS = ${options.failoverAfterMs}")
        val labelLength = when (cotten.qnameMode) {
            "off" -> 63
            "moderate" -> 42
            "aggressive" -> 32
            else -> qnameLabelLength(cotten.configPreset)
        }
        // Max's initial HELLO needs 55 base32 characters plus its reserved
        // subtree and shaping separators. Panel account domains can exceed
        // the capacity of the engine's 101-character starting budget.
        val handshakeLength = (primary + standbys).maxOfOrNull {
            "cottenmax-v1.$it".length + 1 + 55 + 54 / labelLength +
                if (options.labelCountJitter) 3 else 0
        } ?: 101
        require(handshakeLength <= 253) { "CottenMax domain leaves insufficient DNS handshake capacity" }
        val safeQName = maxOf(options.safeQName, handshakeLength)
        appendLine("COTTENMAX_SAFE_QNAME = $safeQName")
        appendLine("COTTENMAX_MAX_QNAME = ${maxOf(options.maxQName, safeQName)}")
        appendLine("COTTENMAX_MAX_ANSWER = ${options.maxAnswer}")
        appendLine("COTTENMAX_MAX_PATH_WINDOW = ${options.maxPathWindow}")
        appendLine("COTTENMAX_MAX_INFLIGHT = ${options.maxInflight}")
        appendLine("COTTENMAX_FEC_DOWNLOAD_ENABLED = ${options.fecDownloadEnabled}")
        appendLine("COTTENMAX_FEC_DATA_SHARDS = ${options.fecDataShards}")
        appendLine("COTTENMAX_FEC_PARITY_SHARDS = ${options.fecParityShards}")
        appendLine("COTTENMAX_SESSION_IDLE_MILLISECONDS = ${options.sessionIdleMs}")
        appendLine("COTTENMAX_HOLD_MILLISECONDS = ${options.holdMs}")
        appendLine("QNAME_LABEL_COUNT_JITTER = ${options.labelCountJitter}")
        if (options.qnameLossGuard) appendLine("COTTENMAX_QNAME_LOSS_GUARD = true")
        appendLine("RESOLVER_DISCOVERY_ENABLED = ${options.discoveryEnabled}")
        if (options.discoveryEnabled) {
            appendLine("RESOLVER_DISCOVERY_COUNTRY = \"${escape(options.discoveryCountry)}\"")
            appendLine("RESOLVER_DISCOVERY_SAMPLE_RANGES = ${options.discoverySampleRanges}")
            appendLine("RESOLVER_DISCOVERY_WANT = ${options.discoveryWant}")
            appendLine("RESOLVER_DISCOVERY_CONCURRENCY = ${options.discoveryConcurrency}")
            appendLine("RESOLVER_DISCOVERY_PACE_MILLISECONDS = ${options.discoveryPaceMs}")
            val candidates = CottenMaxOptions.entries(options.discoveryCandidates)
            if (candidates.isNotEmpty()) appendLine("RESOLVER_DISCOVERY_CANDIDATES = ${candidates.joinToString(prefix = "[", postfix = "]") { "\"${escape(it)}\"" }}")
            if (options.discoveryCacheEnabled) {
                appendLine("RESOLVER_DISCOVERY_CACHE_FILE = \"cottendns-discovery-cache.json\"")
                appendLine("RESOLVER_DISCOVERY_NETWORK_ID = \"${escape(options.discoveryNetworkId)}\"")
            }
        }
    }

    /** What the profile will actually emit, for the read-only UI summary. */
    data class Summary(
        val transport: String,
        val delivery: String,
        val ipMode: String,
        val mtu: String,
    )

    /**
     * Derived from the same helpers the renderer uses, so the summary cannot
     * drift away from the emitted TOML the way a hand-written table does.
     */
    fun summarize(settings: CottenDnsProfileSettings): Summary {
        val cotten = settings.normalized()
        val compat = cotten.isCompatibility
        val preset = cotten.configPreset

        val transport = when {
            compat -> "udp"
            cotten.transportMode != CottenDnsProfileSettings.ModePreset -> cotten.transportMode
            else -> resolverTransport(preset)
        }
        val transportText = when (transport) {
            "udp" -> "UDP/53 only"
            "tcp" -> "forced DNS-over-TCP/53"
            "dot" -> "DoT over TLS, falls back to UDP then TCP/53"
            "doh" -> "DoH over HTTPS"
            "doh3" -> "DoH over HTTP/3 (QUIC)"
            "doq" -> "DNS over QUIC"
            else -> "UDP/53 with TCP/53 fallback"
        }

        val types = when {
            compat -> listOf("TXT")
            cotten.deliveryMode != CottenDnsProfileSettings.ModePreset -> deliveryTypesFor(cotten.deliveryMode)
            else -> queryTypeSet(preset)
        }
        val deliveryText = if (types.size == 1) "${types.first()} only" else types.joinToString(" + ")

        val ipModeText = when (resolverIpMode(cotten)) {
            "dual" -> "IPv4 + IPv6"
            "ipv4" -> "IPv4 only"
            "ipv6" -> "IPv6 only"
            else -> "IPv4, IPv6 fallback"
        }

        val qnameLen = when {
            compat -> 63
            cotten.qnameMode == "off" -> 63
            cotten.qnameMode == "moderate" -> 42
            cotten.qnameMode == "aggressive" -> 32
            else -> qnameLabelLength(preset)
        }
        val mtuText = if (compat) {
            "single global scan, 1 probe sample, ${qnameLen}-char labels"
        } else {
            "adaptive per-group MTU, ${mtuProbeSamples(preset)} probe sample(s), " +
                "${mtuMaxLoss(preset)} max loss, ${qnameLen}-char labels"
        }

        return Summary(transport = transportText, delivery = deliveryText, ipMode = ipModeText, mtu = mtuText)
    }

    /**
     * RESOLVER_IP_MODE. Compatibility forces "auto": a legacy MasterDNS/StormDNS
     * server may publish no IPv6 resolvers, and pinning "ipv6"/"dual" there would
     * strand the client on an empty pool. Otherwise an explicit user choice wins,
     * and "preset" defers to the engine default of "auto".
     */
    private fun resolverIpMode(cotten: CottenDnsProfileSettings): String = when {
        cotten.isCompatibility -> "auto"
        cotten.ipMode != CottenDnsProfileSettings.ModePreset -> cotten.ipMode
        else -> "auto"
    }

    private fun StringBuilder.appendEncryptedResolverToml(
        transport: String,
        cotten: CottenDnsProfileSettings,
        escape: (String) -> String,
        primaryDomain: String = "",
    ) {
        if (transport !in setOf("dot", "doh", "doh3", "doq")) {
            return
        }
        if (cotten.resolverTlsServerName.isNotEmpty()) {
            appendLine("RESOLVER_TLS_SERVER_NAME = \"${escape(cotten.resolverTlsServerName)}\"")
        }
        if (cotten.resolverTlsPin.isNotEmpty()) {
            appendLine("RESOLVER_TLS_PIN = \"${escape(cotten.resolverTlsPin)}\"")
        }
        if (transport == "doq") {
            appendLine("RESOLVER_DOQ_PORT = ${cotten.resolverDoTPort}")
        } else if (transport == "dot") {
            appendLine("RESOLVER_DOT_PORT = ${cotten.resolverDoTPort}")
        } else {
            appendLine("RESOLVER_DOH_PORT = ${cotten.resolverDoHPort}")
            appendLine("RESOLVER_DOH_PATH = \"${escape(cotten.resolverDoHPath)}\"")
        }
    }

    /** Maps an app-level preset to the engine's own CONFIG_PRESET vocabulary. */
    private fun enginePresetBase(preset: String, isCompatibility: Boolean): String {
        if (isCompatibility) {
            // Never let a CottenDNS preset silently enable native-only transport
            // or traffic-amplifying behavior on a legacy connection.
            return "default"
        }
        return when (preset) {
            "speed", "survival", "tcp-survival" -> preset
            else -> "default" // "default" and "master-storm"
        }
    }

    private fun resolverTransport(preset: String): String = when (preset) {
        "tcp-survival" -> "tcp"
        "master-storm" -> "udp"
        else -> "auto"
    }

    private fun adaptiveDuplicationTarget(preset: String): String =
        if (preset == "survival") "0.97" else "0.95"

    private fun ednsUdpSize(preset: String): Int = if (preset == "survival") 1232 else 4096

    private fun qnameLabelLength(preset: String): Int = if (preset == "survival") 42 else 63

    // The distinguishing CottenDNS feature is adaptive per-group MTU, not the
    // probe count; multi-sample probing is heavier and reserved for survival.
    private fun mtuProbeSamples(preset: String): Int = if (preset == "survival") 5 else 1

    private fun mtuMaxLoss(preset: String): String = if (preset == "survival") "0.5" else "0.0"

    // TXT-only by default: on filtered networks non-TXT records (NULL/HTTPS
    // especially) are often dropped, so rotating to them costs retransmits.
    private fun queryTypeSet(preset: String): List<String> = when (preset) {
        "speed" -> listOf("TXT", "HTTPS")
        "survival" -> listOf("TXT", "CNAME", "HTTPS", "A")
        "tcp-survival" -> listOf("TXT", "HTTPS")
        else -> listOf("TXT")
    }

    private fun deliveryTypesFor(mode: String): List<String> = when (mode) {
        "txt" -> listOf("TXT")
        "txt-cname" -> listOf("TXT", "CNAME")
        "txt-https" -> listOf("TXT", "HTTPS")
        // AAAA is a dense IPv6 download carrier (15 data bytes/record). It rides
        // the resolver hop even when that hop is IPv4, and the server enables
        // AAAA delivery by default. Oversized frames fall back to CNAME/TXT.
        "txt-aaaa" -> listOf("TXT", "AAAA")
        "all" -> listOf("TXT", "CNAME", "NULL", "HTTPS", "AAAA")
        else -> listOf("TXT")
    }

    private fun queryTypesToml(types: List<String>): String =
        types.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
}
