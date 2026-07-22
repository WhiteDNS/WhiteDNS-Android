package shop.whitedns.client.runtime

import java.util.Locale
import kotlin.math.roundToLong

data class CottenDnsTrafficStats(
    val downloadBytes: Long,
    val uploadBytes: Long,
    val downloadSpeedBytesPerSecond: Long,
    val uploadSpeedBytesPerSecond: Long,
    val lossPercent: Double = 0.0,
    val activeResolvers: Int = 0,
	val transport: String = "",
	val txQueueDepth: Int = 0,
	val encodedQueueDepth: Int = 0,
	val rxQueueDepth: Int = 0,
	val rxDrops: Long = 0,
	val txDrops: Long = 0,
	val recoveries: Long = 0,
	val streamDialFailures: Long = 0,
	val streamWriteFailures: Long = 0,
) {
    fun hasTraffic(): Boolean {
        return downloadBytes > 0L ||
            uploadBytes > 0L ||
            downloadSpeedBytesPerSecond > 0L ||
            uploadSpeedBytesPerSecond > 0L
    }
}

class CottenDnsTrafficAccounting {
    private var lastRawStats: CottenDnsTrafficStats? = null
    private var accumulatedDownloadBytes: Long = 0L
    private var accumulatedUploadBytes: Long = 0L
    private var latestStats: CottenDnsTrafficStats? = null

    @Synchronized
    fun reset() {
        lastRawStats = null
        accumulatedDownloadBytes = 0L
        accumulatedUploadBytes = 0L
        latestStats = null
    }

    @Synchronized
    fun record(rawStats: CottenDnsTrafficStats): CottenDnsTrafficStats {
        val previous = lastRawStats
        accumulatedDownloadBytes += rawStats.downloadBytes.deltaSince(previous?.downloadBytes)
        accumulatedUploadBytes += rawStats.uploadBytes.deltaSince(previous?.uploadBytes)
        lastRawStats = rawStats
        return rawStats.copy(
            downloadBytes = accumulatedDownloadBytes,
            uploadBytes = accumulatedUploadBytes,
        ).also { latestStats = it }
    }

    @Synchronized
    fun latest(): CottenDnsTrafficStats? = latestStats

    private fun Long.deltaSince(previous: Long?): Long {
        if (previous == null) {
            return coerceAtLeast(0)
        }
        return if (this >= previous) {
            this - previous
        } else {
            coerceAtLeast(0)
        }
    }
}

fun parseCottenDnsTrafficStatsLine(line: String): CottenDnsTrafficStats? {
    val cleanLine = line
        .replace(AnsiEscapeRegex, "")
        .trim()
    val match = CottenDnsTrafficStatsRegex.find(cleanLine) ?: return null
    val uploadSpeed = parseDataAmount(
        value = match.groupValues[1],
        unit = match.groupValues[2],
    ) ?: return null
    val uploadTotal = parseDataAmount(
        value = match.groupValues[3],
        unit = match.groupValues[4],
    ) ?: return null
    val downloadSpeed = parseDataAmount(
        value = match.groupValues[5],
        unit = match.groupValues[6],
    ) ?: return null
    val downloadTotal = parseDataAmount(
        value = match.groupValues[7],
        unit = match.groupValues[8],
    ) ?: return null

    return CottenDnsTrafficStats(
        downloadBytes = downloadTotal,
        uploadBytes = uploadTotal,
        downloadSpeedBytesPerSecond = downloadSpeed,
        uploadSpeedBytesPerSecond = uploadSpeed,
        lossPercent = match.groupValues.getOrNull(9)?.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0,
        activeResolvers = match.groupValues.getOrNull(10)?.toIntOrNull()?.coerceAtLeast(0) ?: 0,
		transport = TransportRegex.find(cleanLine)?.groupValues?.getOrNull(1).orEmpty(),
		txQueueDepth = QueuesRegex.find(cleanLine)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0,
		encodedQueueDepth = QueuesRegex.find(cleanLine)?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 0,
		rxQueueDepth = QueuesRegex.find(cleanLine)?.groupValues?.getOrNull(3)?.toIntOrNull() ?: 0,
		rxDrops = DropsRegex.find(cleanLine)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0,
		txDrops = DropsRegex.find(cleanLine)?.groupValues?.getOrNull(2)?.toLongOrNull() ?: 0,
		recoveries = RecoveriesRegex.find(cleanLine)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0,
		streamDialFailures = StreamFailuresRegex.find(cleanLine)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0,
		streamWriteFailures = StreamFailuresRegex.find(cleanLine)?.groupValues?.getOrNull(2)?.toLongOrNull() ?: 0,
    )
}

fun formatTrafficSpeed(bytesPerSecond: Long): String {
    val units = listOf("B/s", "KB/s", "MB/s", "GB/s", "TB/s")
    var value = bytesPerSecond.coerceAtLeast(0).toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex += 1
    }
    val pattern = if (unitIndex == 0 || value >= 100.0) "%.0f %s" else "%.1f %s"
    return String.format(Locale.US, pattern, value, units[unitIndex])
}

fun formatTrafficNotificationText(stats: CottenDnsTrafficStats): String {
    return buildString {
        append("Down ${formatTrafficSpeed(stats.downloadSpeedBytesPerSecond)}")
        append(" | Up ${formatTrafficSpeed(stats.uploadSpeedBytesPerSecond)}")
        append(" | Loss ${String.format(Locale.US, "%.1f%%", stats.lossPercent)}")
        if (stats.activeResolvers > 0) {
            append(" | DNS ${stats.activeResolvers}")
        }
		if (stats.transport.isNotBlank()) {
			append(" | ${stats.transport}")
		}
		if (stats.recoveries > 0) {
			append(" | Recovery ${stats.recoveries}")
		}
    }
}

private fun parseDataAmount(
    value: String,
    unit: String,
): Long? {
    val amount = value.toDoubleOrNull() ?: return null
    val multiplier = when (unit.uppercase(Locale.US)) {
        "B" -> 1.0
        "KB" -> 1024.0
        "MB" -> 1024.0 * 1024.0
        "GB" -> 1024.0 * 1024.0 * 1024.0
        "TB" -> 1024.0 * 1024.0 * 1024.0 * 1024.0
        else -> return null
    }
    return (amount * multiplier).roundToLong().coerceAtLeast(0)
}

private val AnsiEscapeRegex = Regex("\\u001B\\[[;\\d]*m")
private val CottenDnsTrafficStatsRegex = Regex(
    """([0-9]+(?:\.[0-9]+)?)\s*([KMGT]?B)/s\s*\(Total:\s*([0-9]+(?:\.[0-9]+)?)\s*([KMGT]?B)\)\s*\|\s*[^0-9]*([0-9]+(?:\.[0-9]+)?)\s*([KMGT]?B)/s\s*\(Total:\s*([0-9]+(?:\.[0-9]+)?)\s*([KMGT]?B)\)(?:.*?loss\s*([0-9]+(?:\.[0-9]+)?)%.*?resolvers\s*(\d+))?""",
    RegexOption.IGNORE_CASE,
)

private val TransportRegex = Regex("""transport\s+([^|\s]+)""", RegexOption.IGNORE_CASE)
private val QueuesRegex = Regex("""queues\s+(\d+)/(\d+)/(\d+)""", RegexOption.IGNORE_CASE)
private val DropsRegex = Regex("""drops\s+rx=(\d+)\s+tx=(\d+)""", RegexOption.IGNORE_CASE)
private val RecoveriesRegex = Regex("""recoveries\s+(\d+)""", RegexOption.IGNORE_CASE)
private val StreamFailuresRegex = Regex("""stream-fail\s+dial=(\d+)\s+write=(\d+)""", RegexOption.IGNORE_CASE)
