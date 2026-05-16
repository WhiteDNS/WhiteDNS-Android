package shop.whitedns.client.scan

sealed class StormDnsScanTelemetry {
    data class Valid(
        val resolver: String,
        val latencyMillis: Int? = null,
        val attempts: Int? = null,
    ) : StormDnsScanTelemetry()

    data class Rejected(
        val resolver: String,
        val reason: String = "",
        val latencyMillis: Int? = null,
        val attempts: Int? = null,
    ) : StormDnsScanTelemetry()

    data class Complete(
        val total: Int,
        val valid: Int,
        val rejected: Int,
    ) : StormDnsScanTelemetry()
}

fun parseStormDnsScanLine(line: String): StormDnsScanTelemetry? {
    val cleanLine = line
        .replace(AnsiEscapeRegex, "")
        .trim()
    val markerIndex = cleanLine.indexOf(ScanMarker)
    if (markerIndex < 0) {
        return null
    }

    val fields = ScanFieldRegex.findAll(cleanLine.substring(markerIndex + ScanMarker.length))
        .associate { match -> match.groupValues[1] to match.groupValues[2] }
    return when (fields["event"]) {
        "valid" -> fields["resolver"]
            ?.takeIf(String::isNotBlank)
            ?.let { resolver ->
                StormDnsScanTelemetry.Valid(
                    resolver = resolver,
                    latencyMillis = fields.optionalPositiveInt("latency_ms", "latency"),
                    attempts = fields.optionalPositiveInt("attempts"),
                )
            }
        "rejected" -> fields["resolver"]
            ?.takeIf(String::isNotBlank)
            ?.let { resolver ->
                StormDnsScanTelemetry.Rejected(
                    resolver = resolver,
                    reason = fields["reason"].orEmpty(),
                    latencyMillis = fields.optionalPositiveInt("latency_ms", "latency"),
                    attempts = fields.optionalPositiveInt("attempts"),
                )
            }
        "complete" -> StormDnsScanTelemetry.Complete(
            total = fields["total"].toIntOrZero(),
            valid = fields["valid"].toIntOrZero(),
            rejected = fields["rejected"].toIntOrZero(),
        )
        else -> null
    }
}

private fun String?.toIntOrZero(): Int {
    return this?.toIntOrNull() ?: 0
}

private fun Map<String, String>.optionalPositiveInt(vararg names: String): Int? {
    return names.firstNotNullOfOrNull { name ->
        this[name]?.toIntOrNull()?.takeIf { it > 0 }
    }
}

private const val ScanMarker = "WD_SCAN"
private val ScanFieldRegex = Regex("""(\w+)=([^\s]+)""")
private val AnsiEscapeRegex = Regex("${27.toChar()}\\[[;?0-9]*[ -/]*[@-~]")
