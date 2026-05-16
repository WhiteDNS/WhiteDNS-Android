package shop.whitedns.client.runtime

import java.util.Locale
import kotlin.math.roundToLong

data class StormDnsTrafficStats(
    val downloadBytes: Long,
    val uploadBytes: Long,
    val downloadSpeedBytesPerSecond: Long,
    val uploadSpeedBytesPerSecond: Long,
)

fun StormDnsTrafficStats.estimateDeduplicatedTraffic(
    uploadDuplication: Int,
    downloadDuplication: Int,
): StormDnsTrafficStats {
    val safeUploadDuplication = uploadDuplication.coerceAtLeast(1).toLong()
    val safeDownloadDuplication = downloadDuplication.coerceAtLeast(1).toLong()
    return copy(
        downloadBytes = downloadBytes.divideRoundedUp(safeDownloadDuplication),
        uploadBytes = uploadBytes.divideRoundedUp(safeUploadDuplication),
        downloadSpeedBytesPerSecond = downloadSpeedBytesPerSecond.divideRoundedUp(safeDownloadDuplication),
        uploadSpeedBytesPerSecond = uploadSpeedBytesPerSecond.divideRoundedUp(safeUploadDuplication),
    )
}

class StormDnsTrafficAccounting {
    private var lastRawStats: StormDnsTrafficStats? = null
    private var accumulatedDownloadBytes: Long = 0L
    private var accumulatedUploadBytes: Long = 0L
    private var latestStats: StormDnsTrafficStats? = null

    @Synchronized
    fun reset() {
        lastRawStats = null
        accumulatedDownloadBytes = 0L
        accumulatedUploadBytes = 0L
        latestStats = null
    }

    @Synchronized
    fun record(rawStats: StormDnsTrafficStats): StormDnsTrafficStats {
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
    fun latest(): StormDnsTrafficStats? = latestStats

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

fun parseStormDnsTrafficStatsLine(line: String): StormDnsTrafficStats? {
    val cleanLine = line
        .replace(AnsiEscapeRegex, "")
        .trim()
    val match = StormDnsTrafficStatsRegex.find(cleanLine) ?: return null
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

    return StormDnsTrafficStats(
        downloadBytes = downloadTotal,
        uploadBytes = uploadTotal,
        downloadSpeedBytesPerSecond = downloadSpeed,
        uploadSpeedBytesPerSecond = uploadSpeed,
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

fun formatTrafficNotificationText(stats: StormDnsTrafficStats): String {
    return "Down ${formatTrafficSpeed(stats.downloadSpeedBytesPerSecond)} | Up ${formatTrafficSpeed(stats.uploadSpeedBytesPerSecond)}"
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

private fun Long.divideRoundedUp(divisor: Long): Long {
    val safeValue = coerceAtLeast(0)
    return if (safeValue == 0L) {
        0L
    } else {
        1L + ((safeValue - 1L) / divisor.coerceAtLeast(1L))
    }
}

private val AnsiEscapeRegex = Regex("\\u001B\\[[;\\d]*m")
private val StormDnsTrafficStatsRegex = Regex(
    """([0-9]+(?:\.[0-9]+)?)\s*([KMGT]?B)/s\s*\(Total:\s*([0-9]+(?:\.[0-9]+)?)\s*([KMGT]?B)\)\s*\|\s*[^0-9]*([0-9]+(?:\.[0-9]+)?)\s*([KMGT]?B)/s\s*\(Total:\s*([0-9]+(?:\.[0-9]+)?)\s*([KMGT]?B)\)""",
)
