package shop.whitedns.client.proxy

import org.junit.Assert.assertEquals
import org.junit.Test

class WhiteDnsProxyEventsTest {
    @Test
    fun eventsCarrySessionId() {
        val receivedEvents = mutableListOf<WhiteDnsProxyEvent>()
        val listener: (WhiteDnsProxyEvent) -> Unit = { event ->
            receivedEvents += event
        }

        WhiteDnsProxyEvents.addListener(listener)
        try {
            WhiteDnsProxyEvents.log("session-a", "log")
            WhiteDnsProxyEvents.ready("session-a", "ready")
            WhiteDnsProxyEvents.failed("session-a", "failed")
        } finally {
            WhiteDnsProxyEvents.removeListener(listener)
        }

        assertEquals(
            listOf(
                WhiteDnsProxyEvent.Log("session-a", "log"),
                WhiteDnsProxyEvent.Ready("session-a", "ready"),
                WhiteDnsProxyEvent.Failed("session-a", "failed"),
            ),
            receivedEvents,
        )
    }
}
