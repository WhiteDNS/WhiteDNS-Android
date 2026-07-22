package shop.whitedns.client.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CottenDnsTrafficStatsTest {
    @Test
    fun parseCottenDnsTrafficStatsLineReadsDirectionsAndUnits() {
        val stats = parseCottenDnsTrafficStatsLine(
            "INFO \uD83D\uDCCA ↑ 1.50 KB/s (Total: 3.00 KB) | ↓ 2.00 MB/s (Total: 4.50 MB)",
        )

        requireNotNull(stats)
        assertEquals(4_718_592L, stats.downloadBytes)
        assertEquals(3_072L, stats.uploadBytes)
        assertEquals(2_097_152L, stats.downloadSpeedBytesPerSecond)
        assertEquals(1_536L, stats.uploadSpeedBytesPerSecond)
    }

    @Test
    fun parseCottenDnsTrafficStatsLineIgnoresNonTrafficLogs() {
        assertNull(parseCottenDnsTrafficStatsLine("CottenDns client started"))
    }

    @Test
    fun parseCottenDnsTrafficStatsLineHandlesTimestampedAnsiOutput() {
        val stats = parseCottenDnsTrafficStatsLine(
            "2026/05/15 13:10:11 CottenDns \u001B[32mINFO\u001B[0m " +
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
    fun parseCottenDnsTrafficStatsLineReadsLossAndResolverHealth() {
        val stats = parseCottenDnsTrafficStatsLine(
            "↑ 1.00 KB/s (Total: 2.00 KB) | ↓ 3.00 KB/s (Total: 4.00 KB) | loss 12.5% | resolvers 7",
        )

        requireNotNull(stats)
        assertEquals(12.5, stats.lossPercent, 0.001)
        assertEquals(7, stats.activeResolvers)
    }

    @Test
    fun parseCottenDnsTrafficStatsLineReadsRecoveryTelemetry() {
        val stats = parseCottenDnsTrafficStatsLine(
            "↑ 1 KB/s (Total: 2 KB) | ↓ 3 KB/s (Total: 4 KB) | loss 2.0% | resolvers 5 | " +
                "transport DoH | queues 1/2/3 | drops rx=4 tx=5 | recoveries 6 | stream-fail dial=7 write=8",
        )
        requireNotNull(stats)
        assertEquals("DoH", stats.transport)
        assertEquals(1, stats.txQueueDepth)
        assertEquals(2, stats.encodedQueueDepth)
        assertEquals(3, stats.rxQueueDepth)
        assertEquals(4L, stats.rxDrops)
        assertEquals(5L, stats.txDrops)
        assertEquals(6L, stats.recoveries)
        assertEquals(7L, stats.streamDialFailures)
        assertEquals(8L, stats.streamWriteFailures)
    }

    @Test
    fun trafficAccountingKeepsSessionTotalsAcrossRawCounterResets() {
        val accounting = CottenDnsTrafficAccounting()

        val first = accounting.record(
            CottenDnsTrafficStats(
                downloadBytes = 1_000L,
                uploadBytes = 500L,
                downloadSpeedBytesPerSecond = 100L,
                uploadSpeedBytesPerSecond = 50L,
            ),
        )
        assertEquals(1_000L, first.downloadBytes)
        assertEquals(500L, first.uploadBytes)

        val second = accounting.record(
            CottenDnsTrafficStats(
                downloadBytes = 1_300L,
                uploadBytes = 700L,
                downloadSpeedBytesPerSecond = 120L,
                uploadSpeedBytesPerSecond = 60L,
            ),
        )
        assertEquals(1_300L, second.downloadBytes)
        assertEquals(700L, second.uploadBytes)

        val afterRestart = accounting.record(
            CottenDnsTrafficStats(
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
            CottenDnsTrafficStats(
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
        val accounting = CottenDnsTrafficAccounting()
        accounting.record(
            CottenDnsTrafficStats(
                downloadBytes = 1_000L,
                uploadBytes = 500L,
                downloadSpeedBytesPerSecond = 100L,
                uploadSpeedBytesPerSecond = 50L,
            ),
        )

        accounting.reset()

        val next = accounting.record(
            CottenDnsTrafficStats(
                downloadBytes = 25L,
                uploadBytes = 10L,
                downloadSpeedBytesPerSecond = 5L,
                uploadSpeedBytesPerSecond = 2L,
            ),
        )
        assertEquals(25L, next.downloadBytes)
        assertEquals(10L, next.uploadBytes)
    }
}
