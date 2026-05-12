package shop.whitedns.client.proxy

import java.util.concurrent.CopyOnWriteArraySet

sealed class WhiteDnsProxyEvent {
    data class Log(val sessionId: String, val message: String) : WhiteDnsProxyEvent()
    data class Ready(val sessionId: String, val message: String) : WhiteDnsProxyEvent()
    data class Failed(val sessionId: String, val message: String) : WhiteDnsProxyEvent()
}

object WhiteDnsProxyEvents {
    private val listeners = CopyOnWriteArraySet<(WhiteDnsProxyEvent) -> Unit>()

    fun addListener(listener: (WhiteDnsProxyEvent) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (WhiteDnsProxyEvent) -> Unit) {
        listeners.remove(listener)
    }

    fun log(sessionId: String, message: String) {
        emit(WhiteDnsProxyEvent.Log(sessionId, message))
    }

    fun ready(sessionId: String, message: String) {
        emit(WhiteDnsProxyEvent.Ready(sessionId, message))
    }

    fun failed(sessionId: String, message: String) {
        emit(WhiteDnsProxyEvent.Failed(sessionId, message))
    }

    private fun emit(event: WhiteDnsProxyEvent) {
        listeners.forEach { listener ->
            runCatching { listener(event) }
        }
    }
}
