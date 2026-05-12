package shop.whitedns.client.runtime

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeReconnectGuardTest {
    @Test
    fun recordFailureBlocksAfterWindowLimit() {
        val guard = RuntimeReconnectGuard(maxFailures = 2, windowMillis = 1_000L)

        assertTrue(guard.recordFailure(nowMillis = 1_000L))
        assertTrue(guard.recordFailure(nowMillis = 1_500L))
        assertFalse(guard.recordFailure(nowMillis = 1_800L))
    }

    @Test
    fun oldFailuresDoNotCountAgainstWindow() {
        val guard = RuntimeReconnectGuard(maxFailures = 2, windowMillis = 1_000L)

        assertTrue(guard.recordFailure(nowMillis = 1_000L))
        assertTrue(guard.recordFailure(nowMillis = 1_500L))
        assertTrue(guard.recordFailure(nowMillis = 2_100L))
    }

    @Test
    fun resetClearsFailureWindow() {
        val guard = RuntimeReconnectGuard(maxFailures = 1, windowMillis = 1_000L)

        assertTrue(guard.recordFailure(nowMillis = 1_000L))
        assertFalse(guard.recordFailure(nowMillis = 1_100L))
        guard.reset()

        assertTrue(guard.recordFailure(nowMillis = 1_200L))
    }

    @Test
    fun failureCountReportsCurrentWindow() {
        val guard = RuntimeReconnectGuard(maxFailures = 3, windowMillis = 1_000L)

        guard.recordFailure(nowMillis = 1_000L)
        guard.recordFailure(nowMillis = 1_500L)

        assertEquals(1, guard.failureCount(nowMillis = 2_100L))
    }
}
