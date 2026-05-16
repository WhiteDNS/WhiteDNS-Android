package shop.whitedns.client.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StormDnsScanTelemetryTest {
    @Test
    fun parseStormDnsScanLineParsesValidResolver() {
        val telemetry = parseStormDnsScanLine("2026 WD_SCAN event=valid resolver=1.1.1.1:53 latency_ms=84 attempts=2")

        assertEquals(
            StormDnsScanTelemetry.Valid(
                resolver = "1.1.1.1:53",
                latencyMillis = 84,
                attempts = 2,
            ),
            telemetry,
        )
    }

    @Test
    fun parseStormDnsScanLineParsesRejectedResolver() {
        val telemetry = parseStormDnsScanLine("2026 WD_SCAN event=rejected resolver=8.8.8.8:53 reason=timeout attempts=3")

        assertEquals(
            StormDnsScanTelemetry.Rejected(
                resolver = "8.8.8.8:53",
                reason = "timeout",
                attempts = 3,
            ),
            telemetry,
        )
    }

    @Test
    fun parseStormDnsScanLineParsesCompletion() {
        val telemetry = parseStormDnsScanLine("WD_SCAN event=complete total=10 valid=3 rejected=7")

        assertEquals(StormDnsScanTelemetry.Complete(total = 10, valid = 3, rejected = 7), telemetry)
    }

    @Test
    fun parseStormDnsScanLineIgnoresOtherLines() {
        assertNull(parseStormDnsScanLine("WD_PROGRESS phase=mtu percent=50"))
    }
}
