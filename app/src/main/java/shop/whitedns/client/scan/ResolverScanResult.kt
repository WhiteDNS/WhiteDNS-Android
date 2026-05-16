package shop.whitedns.client.scan

import org.json.JSONObject

object ResolverScanResultStatus {
    const val Valid = "valid"
    const val Rejected = "rejected"
}

data class ResolverScanResult(
    val resolver: String,
    val status: String,
    val sourceName: String = "",
    val serverDomain: String = "",
    val latencyMillis: Int? = null,
    val attempts: Int? = null,
    val reason: String = "",
    val observedAtMillis: Long = 0L,
) {
    val isValid: Boolean
        get() = status == ResolverScanResultStatus.Valid

    val score: Int
        get() = resolverScanScore(this)
}

data class ResolverScanRecommendation(
    val resolver: String,
    val score: Int,
    val observationCount: Int,
    val successfulServerCount: Int,
    val bestLatencyMillis: Int?,
    val lastObservedAtMillis: Long,
)

fun resolverScanScore(result: ResolverScanResult): Int {
    if (!result.isValid) {
        return 0
    }
    val latencyPenalty = when (val latency = result.latencyMillis) {
        null -> 12
        else -> (latency / 25).coerceIn(0, 48)
    }
    val attemptPenalty = ((result.attempts ?: 1) - 1).coerceAtLeast(0) * 6
    val confidenceBonus = when {
        result.latencyMillis != null && result.attempts != null -> 18
        result.latencyMillis != null -> 12
        result.attempts != null -> 6
        else -> 0
    }
    return (100 + confidenceBonus - latencyPenalty - attemptPenalty).coerceIn(1, 100)
}

fun rankResolverScanResults(results: Iterable<ResolverScanResult>): List<ResolverScanResult> {
    return results
        .filter { it.isValid && it.resolver.isNotBlank() }
        .groupBy { it.resolver }
        .values
        .map { resolverResults ->
            resolverResults.maxWith(
                compareBy<ResolverScanResult> { it.score }
                    .thenByDescending { it.observedAtMillis },
            )
        }
        .sortedWith(
            compareByDescending<ResolverScanResult> { it.score }
                .thenBy { it.latencyMillis ?: Int.MAX_VALUE }
                .thenByDescending { it.observedAtMillis }
                .thenBy { it.resolver },
        )
}

fun summarizeResolverScanRecommendations(
    results: Iterable<ResolverScanResult>,
): List<ResolverScanRecommendation> {
    return results
        .filter { it.isValid && it.resolver.isNotBlank() }
        .groupBy { it.resolver }
        .map { (resolver, resolverResults) ->
            val successfulServerCount = resolverResults
                .mapNotNull { it.serverDomain.takeIf(String::isNotBlank) }
                .distinct()
                .size
            val bestScore = resolverResults.maxOf { it.score }
            val stabilityBonus = ((resolverResults.size - 1).coerceAtMost(4) * 2) +
                (successfulServerCount.coerceAtMost(4) * 3)
            ResolverScanRecommendation(
                resolver = resolver,
                score = (bestScore + stabilityBonus).coerceIn(1, 100),
                observationCount = resolverResults.size,
                successfulServerCount = successfulServerCount,
                bestLatencyMillis = resolverResults.mapNotNull { it.latencyMillis }.minOrNull(),
                lastObservedAtMillis = resolverResults.maxOf { it.observedAtMillis },
            )
        }
        .sortedWith(
            compareByDescending<ResolverScanRecommendation> { it.score }
                .thenBy { it.bestLatencyMillis ?: Int.MAX_VALUE }
                .thenByDescending { it.successfulServerCount }
                .thenByDescending { it.observationCount }
                .thenByDescending { it.lastObservedAtMillis }
                .thenBy { it.resolver },
        )
}

fun ResolverScanResult.toJsonObject(): JSONObject {
    return JSONObject()
        .put("resolver", resolver)
        .put("status", status)
        .put("sourceName", sourceName)
        .put("serverDomain", serverDomain)
        .put("latencyMillis", latencyMillis)
        .put("attempts", attempts)
        .put("reason", reason)
        .put("observedAtMillis", observedAtMillis)
        .put("score", score)
}

fun resolverScanResultFromJson(json: JSONObject): ResolverScanResult {
    return ResolverScanResult(
        resolver = json.optString("resolver"),
        status = json.optString("status"),
        sourceName = json.optString("sourceName"),
        serverDomain = json.optString("serverDomain"),
        latencyMillis = json.optionalPositiveInt("latencyMillis"),
        attempts = json.optionalPositiveInt("attempts"),
        reason = json.optString("reason"),
        observedAtMillis = json.optLong("observedAtMillis", 0L),
    )
}

private fun JSONObject.optionalPositiveInt(name: String): Int? {
    if (!has(name) || isNull(name)) {
        return null
    }
    return optInt(name).takeIf { it > 0 }
}
