package shop.whitedns.client.model

object WhiteDnsProxyExposurePolicy {
    fun isLanReachableListenIp(listenIp: String): Boolean {
        val host = listenIp.trim().lowercase().removeSurrounding("[", "]")
        return when {
            host.startsWith("127.") -> false
            host in setOf("", "localhost", "::1") -> false
            else -> true
        }
    }

    fun hasCompleteSocksCredentials(settings: ResolvedWhiteDnsSettings): Boolean {
        return hasCompleteSocksCredentials(
            enabled = settings.socks5Authentication,
            username = settings.socksUsername,
            password = settings.socksPassword,
        )
    }

    fun hasCompleteSocksCredentials(
        enabled: Boolean,
        username: String,
        password: String,
    ): Boolean {
        return enabled && username.isNotBlank() && password.isNotBlank()
    }

    fun requiresCompleteSocksCredentials(settings: ResolvedWhiteDnsSettings): Boolean {
        return isLanReachableListenIp(settings.listenIp) && !hasCompleteSocksCredentials(settings)
    }
}
