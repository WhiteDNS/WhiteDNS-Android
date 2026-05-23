package shop.whitedns.client.runtime

import org.junit.Assert.assertEquals
import org.junit.Test

class WhiteDnsRuntimeStateStoreTest {
    @Test
    fun modeStateFromPreviousPreservesFailedStateForSameStoppedSession() {
        val previous = WhiteDnsRuntimeState(
            sessionId = "session-1",
            mode = WhiteDnsRuntimeStateStore.ModeVpn,
            status = WhiteDnsRuntimeStateStore.StatusFailed,
            connectionProfileId = "profile-1",
            listenIp = "127.0.0.1",
            listenPort = 10886,
            updatedAtMillis = 100L,
            message = "VPN failed to start",
        )

        val next = modeStateFromPrevious(
            previous = previous,
            mode = WhiteDnsRuntimeStateStore.ModeVpn,
            sessionId = "session-1",
            status = WhiteDnsRuntimeStateStore.StatusStopped,
            message = "VPN service stopped",
            nowMillis = 200L,
        )

        assertEquals(WhiteDnsRuntimeStateStore.StatusFailed, next.status)
        assertEquals("VPN failed to start", next.message)
        assertEquals("profile-1", next.connectionProfileId)
        assertEquals(10886, next.listenPort)
        assertEquals(200L, next.updatedAtMillis)
    }

    @Test
    fun modeStateFromPreviousAllowsStoppedStateForDifferentSession() {
        val previous = WhiteDnsRuntimeState(
            sessionId = "session-1",
            mode = WhiteDnsRuntimeStateStore.ModeProxy,
            status = WhiteDnsRuntimeStateStore.StatusFailed,
            connectionProfileId = "profile-1",
            listenIp = "127.0.0.1",
            listenPort = 10886,
            updatedAtMillis = 100L,
            message = "Proxy failed",
        )

        val next = modeStateFromPrevious(
            previous = previous,
            mode = WhiteDnsRuntimeStateStore.ModeProxy,
            sessionId = "session-2",
            status = WhiteDnsRuntimeStateStore.StatusStopped,
            message = "Proxy service stopped",
            nowMillis = 200L,
        )

        assertEquals(WhiteDnsRuntimeStateStore.StatusStopped, next.status)
        assertEquals("Proxy service stopped", next.message)
        assertEquals("session-2", next.sessionId)
    }
}
