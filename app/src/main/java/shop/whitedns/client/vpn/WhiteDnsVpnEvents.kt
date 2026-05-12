package shop.whitedns.client.vpn

import java.util.concurrent.CopyOnWriteArraySet

sealed class WhiteDnsVpnEvent {
    data class Log(val sessionId: String, val message: String) : WhiteDnsVpnEvent()
    data class Ready(val sessionId: String, val message: String) : WhiteDnsVpnEvent()
    data class Failed(val sessionId: String, val message: String) : WhiteDnsVpnEvent()
}

object WhiteDnsVpnEvents {
    private val listeners = CopyOnWriteArraySet<(WhiteDnsVpnEvent) -> Unit>()

    fun addListener(listener: (WhiteDnsVpnEvent) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (WhiteDnsVpnEvent) -> Unit) {
        listeners.remove(listener)
    }

    fun log(sessionId: String, message: String) {
        emit(WhiteDnsVpnEvent.Log(sessionId, message))
    }

    fun ready(sessionId: String, message: String) {
        emit(WhiteDnsVpnEvent.Ready(sessionId, message))
    }

    fun failed(sessionId: String, message: String) {
        emit(WhiteDnsVpnEvent.Failed(sessionId, message))
    }

    private fun emit(event: WhiteDnsVpnEvent) {
        listeners.forEach { listener ->
            runCatching { listener(event) }
        }
    }
}
