package shop.whitedns.client.vpn

import org.junit.Assert.assertEquals
import org.junit.Test

class WhiteDnsVpnEventsTest {
    @Test
    fun eventsCarrySessionId() {
        val receivedEvents = mutableListOf<WhiteDnsVpnEvent>()
        val listener: (WhiteDnsVpnEvent) -> Unit = { event ->
            receivedEvents += event
        }

        WhiteDnsVpnEvents.addListener(listener)
        try {
            WhiteDnsVpnEvents.log("session-b", "log")
            WhiteDnsVpnEvents.ready("session-b", "ready")
            WhiteDnsVpnEvents.failed("session-b", "failed")
        } finally {
            WhiteDnsVpnEvents.removeListener(listener)
        }

        assertEquals(
            listOf(
                WhiteDnsVpnEvent.Log("session-b", "log"),
                WhiteDnsVpnEvent.Ready("session-b", "ready"),
                WhiteDnsVpnEvent.Failed("session-b", "failed"),
            ),
            receivedEvents,
        )
    }
}
