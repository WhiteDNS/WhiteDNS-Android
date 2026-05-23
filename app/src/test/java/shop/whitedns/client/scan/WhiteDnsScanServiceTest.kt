package shop.whitedns.client.scan

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhiteDnsScanServiceTest {
    @Test
    fun shouldPublishScanHeartbeatAfterInterval() {
        assertFalse(
            shouldPublishScanHeartbeat(
                nowMillis = 14_999L,
                lastHeartbeatMillis = 0L,
                heartbeatIntervalMillis = 15_000L,
            ),
        )
        assertTrue(
            shouldPublishScanHeartbeat(
                nowMillis = 15_000L,
                lastHeartbeatMillis = 0L,
                heartbeatIntervalMillis = 15_000L,
            ),
        )
    }
}
