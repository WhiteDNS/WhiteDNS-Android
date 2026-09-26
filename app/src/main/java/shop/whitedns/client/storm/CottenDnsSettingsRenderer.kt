package shop.whitedns.client.storm

import shop.whitedns.client.model.CottenDnsProfileSettings

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
    ) {
        val cotten = settings.normalized()
        val isCompatibility = cotten.isCompatibility
        val preset = cotten.configPreset

        // CONFIG_PRESET only accepts the engine's own presets; the app-level
        // "master-storm" preset expands to explicit legacy-safe keys over a
        // default base (explicit keys always win over the preset in the engine).
        appendLine("CONFIG_PRESET = \"${escape(enginePresetBase(preset, isCompatibility))}\"")
        appendLine("LEGACY_SESSION_ID = $isCompatibility")
        appendLine("TERMINAL_UI = \"plain\"")
        appendLine("RESOLVER_IP_MODE = \"auto\"")

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

    /** What the profile will actually emit, for the read-only UI summary. */
    data class Summary(
        val transport: String,
        val delivery: String,
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
            "doh" -> "DoH over HTTPS, falls back to UDP then TCP/53"
            else -> "UDP/53 with TCP/53 fallback"
        }

        val types = when {
            compat -> listOf("TXT")
            cotten.deliveryMode != CottenDnsProfileSettings.ModePreset -> deliveryTypesFor(cotten.deliveryMode)
            else -> queryTypeSet(preset)
        }
        val deliveryText = if (types.size == 1) "${types.first()} only" else types.joinToString(" + ")

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

        return Summary(transport = transportText, delivery = deliveryText, mtu = mtuText)
    }

    private fun StringBuilder.appendEncryptedResolverToml(
        transport: String,
        cotten: CottenDnsProfileSettings,
        escape: (String) -> String,
    ) {
        if (transport != "dot" && transport != "doh") {
            return
        }
        if (cotten.resolverTlsServerName.isNotEmpty()) {
            appendLine("RESOLVER_TLS_SERVER_NAME = \"${escape(cotten.resolverTlsServerName)}\"")
        }
        if (cotten.resolverTlsPin.isNotEmpty()) {
            appendLine("RESOLVER_TLS_PIN = \"${escape(cotten.resolverTlsPin)}\"")
        }
        if (transport == "dot") {
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
        "all" -> listOf("TXT", "CNAME", "NULL", "HTTPS")
        else -> listOf("TXT")
    }

    private fun queryTypesToml(types: List<String>): String =
        types.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
}
