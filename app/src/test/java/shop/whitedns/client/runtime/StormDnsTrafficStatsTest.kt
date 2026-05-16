package shop.whitedns.client.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StormDnsTrafficStatsTest {
    @Test
    fun parseStormDnsTrafficStatsLineReadsDirectionsAndUnits() {
        val stats = parseStormDnsTrafficStatsLine(
            "INFO \uD83D\uDCCA ↑ 1.50 KB/s (Total: 3.00 KB) | ↓ 2.00 MB/s (Total: 4.50 MB)",
        )

        requireNotNull(stats)
        assertEquals(4_718_592L, stats.downloadBytes)
        assertEquals(3_072L, stats.uploadBytes)
        assertEquals(2_097_152L, stats.downloadSpeedBytesPerSecond)
        assertEquals(1_536L, stats.uploadSpeedBytesPerSecond)
    }

    @Test
    fun parseStormDnsTrafficStatsLineIgnoresNonTrafficLogs() {
        assertNull(parseStormDnsTrafficStatsLine("StormDNS client started"))
    }

    @Test
    fun parseStormDnsTrafficStatsLineHandlesTimestampedAnsiOutput() {
        val stats = parseStormDnsTrafficStatsLine(
            "2026/05/15 13:10:11 StormDNS \u001B[32mINFO\u001B[0m " +
                "\uD83D\uDCCA \u001B[36m↑\u001B[0m \u001B[33m256 B/s\u001B[0m " +
                "(Total: \u001B[33m512 B\u001B[0m) | \u001B[36m↓\u001B[0m " +
                "\u001B[33m1.00 KB/s\u001B[0m (Total: \u001B[33m2.00 KB\u001B[0m)",
        )

        requireNotNull(stats)
        assertEquals(2_048L, stats.downloadBytes)
        assertEquals(512L, stats.uploadBytes)
        assertEquals(1_024L, stats.downloadSpeedBytesPerSecond)
        assertEquals(256L, stats.uploadSpeedBytesPerSecond)
    }

    @Test
    fun trafficAccountingKeepsSessionTotalsAcrossRawCounterResets() {
        val accounting = StormDnsTrafficAccounting()

        val first = accounting.record(
            StormDnsTrafficStats(
                downloadBytes = 1_000L,
                uploadBytes = 500L,
                downloadSpeedBytesPerSecond = 100L,
                uploadSpeedBytesPerSecond = 50L,
            ),
        )
        assertEquals(1_000L, first.downloadBytes)
        assertEquals(500L, first.uploadBytes)

        val second = accounting.record(
            StormDnsTrafficStats(
                downloadBytes = 1_300L,
                uploadBytes = 700L,
                downloadSpeedBytesPerSecond = 120L,
                uploadSpeedBytesPerSecond = 60L,
            ),
        )
        assertEquals(1_300L, second.downloadBytes)
        assertEquals(700L, second.uploadBytes)

        val afterRestart = accounting.record(
            StormDnsTrafficStats(
                downloadBytes = 200L,
                uploadBytes = 50L,
                downloadSpeedBytesPerSecond = 80L,
                uploadSpeedBytesPerSecond = 20L,
            ),
        )
        assertEquals(1_500L, afterRestart.downloadBytes)
        assertEquals(750L, afterRestart.uploadBytes)
        assertEquals(80L, afterRestart.downloadSpeedBytesPerSecond)
        assertEquals(20L, afterRestart.uploadSpeedBytesPerSecond)

        val duplicateRestartSample = accounting.record(
            StormDnsTrafficStats(
                downloadBytes = 200L,
                uploadBytes = 50L,
                downloadSpeedBytesPerSecond = 0L,
                uploadSpeedBytesPerSecond = 0L,
            ),
        )
        assertEquals(1_500L, duplicateRestartSample.downloadBytes)
        assertEquals(750L, duplicateRestartSample.uploadBytes)
    }

    @Test
    fun trafficAccountingCanResetSession() {
        val accounting = StormDnsTrafficAccounting()
        accounting.record(
            StormDnsTrafficStats(
                downloadBytes = 1_000L,
                uploadBytes = 500L,
                downloadSpeedBytesPerSecond = 100L,
                uploadSpeedBytesPerSecond = 50L,
            ),
        )

        accounting.reset()

        val next = accounting.record(
            StormDnsTrafficStats(
                downloadBytes = 25L,
                uploadBytes = 10L,
                downloadSpeedBytesPerSecond = 5L,
                uploadSpeedBytesPerSecond = 2L,
            ),
        )
        assertEquals(25L, next.downloadBytes)
        assertEquals(10L, next.uploadBytes)
    }

    @Test
    fun estimateDeduplicatedTrafficDividesTunnelCountersByDirectionDuplication() {
        val stats = StormDnsTrafficStats(
            downloadBytes = 401L,
            uploadBytes = 301L,
            downloadSpeedBytesPerSecond = 81L,
            uploadSpeedBytesPerSecond = 61L,
        ).estimateDeduplicatedTraffic(
            uploadDuplication = 3,
            downloadDuplication = 4,
        )

        assertEquals(101L, stats.downloadBytes)
        assertEquals(101L, stats.uploadBytes)
        assertEquals(21L, stats.downloadSpeedBytesPerSecond)
        assertEquals(21L, stats.uploadSpeedBytesPerSecond)
    }
}
