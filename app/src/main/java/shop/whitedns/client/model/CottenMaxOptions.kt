package shop.whitedns.client.model

import java.io.Serializable
import org.json.JSONObject

// Optional settings belong to the existing CottenDNS connection. They never
// introduce another engine, domain registration, credential or launch config.
data class CottenMaxOptions(
    val trafficProfile: String = "full",
    val lowReductionPercent: Int = 35,
    val standbyDomains: String = "",
    val failoverAfterMs: Int = 3000,
    val safeQName: Int = 101,
    val maxQName: Int = 253,
    val maxAnswer: Int = 1232,
    val maxPathWindow: Int = 256,
    val maxInflight: Int = 256,
    val fecDownloadEnabled: Boolean = false,
    val fecDataShards: Int = 4,
    val fecParityShards: Int = 1,
    val sessionIdleMs: Int = 90000,
    val holdMs: Int = 500,
    val discoveryEnabled: Boolean = false,
    val discoveryCountry: String = "",
    val discoverySampleRanges: Int = 0,
    val discoveryCacheEnabled: Boolean = false,
    val discoveryNetworkId: String = "",
    val discoveryCandidates: String = "",
    val discoveryWant: Int = 16,
    val discoveryConcurrency: Int = 16,
    val discoveryPaceMs: Int = 20,
    val dnsPaddingBlockSize: Int = 0,
    val labelCountJitter: Boolean = false,
    val qnameLossGuard: Boolean = false,
    val doh3FallbackToDoh: Boolean = false,
) : Serializable {
    fun normalized(): CottenMaxOptions = copy(
        trafficProfile = if (trafficProfile.trim().equals("low", ignoreCase = true)) "low" else "full",
        lowReductionPercent = lowReductionPercent.coerceIn(10, 90),
        standbyDomains = entries(standbyDomains).map { it.trimEnd('.').lowercase() }.distinct().take(7).joinToString(", "),
        failoverAfterMs = failoverAfterMs.coerceIn(100, 60000),
        safeQName = safeQName.coerceIn(64, 253),
        maxQName = maxQName.coerceIn(safeQName.coerceIn(64, 253), 253),
        maxAnswer = maxAnswer.coerceIn(512, 1232),
        maxPathWindow = maxPathWindow.coerceIn(1, 1024),
        maxInflight = maxInflight.coerceIn(1, 4096),
        fecDataShards = fecDataShards.coerceIn(2, 16),
        fecParityShards = fecParityShards.coerceIn(1, 4),
        sessionIdleMs = sessionIdleMs.coerceIn(1000, 90000),
        holdMs = holdMs.coerceIn(0, 1500) / 100 * 100,
        discoveryCountry = discoveryCountry.trim().lowercase().takeIf { it.matches(Regex("[a-z]{2}")) }.orEmpty(),
        discoverySampleRanges = discoverySampleRanges.coerceIn(0, 4096),
        discoveryNetworkId = discoveryNetworkId.take(256),
        discoveryCandidates = entries(discoveryCandidates).take(256).joinToString(", "),
        discoveryWant = discoveryWant.coerceIn(1, 4096),
        discoveryConcurrency = discoveryConcurrency.coerceIn(1, 64),
        discoveryPaceMs = discoveryPaceMs.coerceIn(1, 60000),
        dnsPaddingBlockSize = if (dnsPaddingBlockSize <= 0) 0 else dnsPaddingBlockSize.coerceIn(16, 4096),
    )

    fun toJson(): JSONObject = JSONObject().apply {
        put("trafficProfile", trafficProfile)
        put("lowReductionPercent", lowReductionPercent)
        put("standbyDomains", standbyDomains)
        put("failoverAfterMs", failoverAfterMs)
        put("safeQName", safeQName)
        put("maxQName", maxQName)
        put("maxAnswer", maxAnswer)
        put("maxPathWindow", maxPathWindow)
        put("maxInflight", maxInflight)
        put("fecDownloadEnabled", fecDownloadEnabled)
        put("fecDataShards", fecDataShards)
        put("fecParityShards", fecParityShards)
        put("sessionIdleMs", sessionIdleMs)
        put("holdMs", holdMs)
        put("discoveryEnabled", discoveryEnabled)
        put("discoveryCountry", discoveryCountry)
        put("discoverySampleRanges", discoverySampleRanges)
        put("discoveryCacheEnabled", discoveryCacheEnabled)
        put("discoveryNetworkId", discoveryNetworkId)
        put("discoveryCandidates", discoveryCandidates)
        put("discoveryWant", discoveryWant)
        put("discoveryConcurrency", discoveryConcurrency)
        put("discoveryPaceMs", discoveryPaceMs)
        put("dnsPaddingBlockSize", dnsPaddingBlockSize)
        put("labelCountJitter", labelCountJitter)
        put("qnameLossGuard", qnameLossGuard)
        put("doh3FallbackToDoh", doh3FallbackToDoh)
    }

    companion object {
        fun entries(text: String): List<String> = text.split(Regex("[,;\\s]+"))
            .map(String::trim).filter(String::isNotEmpty).distinct()

        fun fromJson(json: JSONObject?): CottenMaxOptions {
            if (json == null) return CottenMaxOptions()
            val defaults = CottenMaxOptions()
            return CottenMaxOptions(
                trafficProfile = json.optString("trafficProfile", defaults.trafficProfile),
                lowReductionPercent = json.optInt("lowReductionPercent", defaults.lowReductionPercent),
                standbyDomains = json.optString("standbyDomains"),
                failoverAfterMs = json.optInt("failoverAfterMs", defaults.failoverAfterMs),
                safeQName = json.optInt("safeQName", defaults.safeQName),
                maxQName = json.optInt("maxQName", defaults.maxQName),
                maxAnswer = json.optInt("maxAnswer", defaults.maxAnswer),
                maxPathWindow = json.optInt("maxPathWindow", defaults.maxPathWindow),
                maxInflight = json.optInt("maxInflight", defaults.maxInflight),
                fecDownloadEnabled = json.optBoolean("fecDownloadEnabled", defaults.fecDownloadEnabled),
                fecDataShards = json.optInt("fecDataShards", defaults.fecDataShards),
                fecParityShards = json.optInt("fecParityShards", defaults.fecParityShards),
                sessionIdleMs = json.optInt("sessionIdleMs", defaults.sessionIdleMs),
                holdMs = json.optInt("holdMs", defaults.holdMs),
                discoveryEnabled = json.optBoolean("discoveryEnabled", defaults.discoveryEnabled),
                discoveryCountry = json.optString("discoveryCountry"),
                discoverySampleRanges = json.optInt("discoverySampleRanges", defaults.discoverySampleRanges),
                discoveryCacheEnabled = json.optBoolean("discoveryCacheEnabled", defaults.discoveryCacheEnabled),
                discoveryNetworkId = json.optString("discoveryNetworkId"),
                discoveryCandidates = json.optString("discoveryCandidates"),
                discoveryWant = json.optInt("discoveryWant", defaults.discoveryWant),
                discoveryConcurrency = json.optInt("discoveryConcurrency", defaults.discoveryConcurrency),
                discoveryPaceMs = json.optInt("discoveryPaceMs", defaults.discoveryPaceMs),
                dnsPaddingBlockSize = json.optInt("dnsPaddingBlockSize", defaults.dnsPaddingBlockSize),
                labelCountJitter = json.optBoolean("labelCountJitter", defaults.labelCountJitter),
                qnameLossGuard = json.optBoolean("qnameLossGuard", defaults.qnameLossGuard),
                doh3FallbackToDoh = json.optBoolean("doh3FallbackToDoh", defaults.doh3FallbackToDoh),
            ).normalized()
        }
    }
}


fun WhiteDnsSettings.canBootstrapResolversWithCottenMax(): Boolean {
    val profile = selectedConnectionProfile()
    return resolverText.isBlank() && DnsClientEngine.normalize(profile.engine) == DnsClientEngine.CottenDns &&
        profile.cottenSettings.usesCottenMax && profile.cottenSettings.cottenMaxOptions.discoveryEnabled
}

fun StormDnsServerProfile.usesCottenMax(): Boolean =
    DnsClientEngine.normalize(engine) == DnsClientEngine.CottenDns && cottenSettings.usesCottenMax

fun StormDnsServerProfile.canBootstrapResolversWithCottenMax(settings: WhiteDnsSettings): Boolean =
    settings.resolverText.isBlank() && usesCottenMax() && cottenSettings.cottenMaxOptions.discoveryEnabled
