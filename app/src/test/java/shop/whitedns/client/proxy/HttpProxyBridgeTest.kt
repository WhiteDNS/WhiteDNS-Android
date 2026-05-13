package shop.whitedns.client.proxy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HttpProxyBridgeTest {
    @Test
    fun parseHttpProxyHostPortUsesExplicitPort() {
        assertEquals("example.com" to 8443, parseHttpProxyHostPort("example.com:8443", defaultPort = 443))
    }

    @Test
    fun parseHttpProxyHostPortUsesDefaultPort() {
        assertEquals("example.com" to 443, parseHttpProxyHostPort("example.com", defaultPort = 443))
    }

    @Test
    fun parseHttpProxyHostPortSupportsBracketedIpv6() {
        assertEquals("2001:db8::1" to 443, parseHttpProxyHostPort("[2001:db8::1]:443", defaultPort = 80))
    }

    @Test
    fun parseHttpProxyHostPortRejectsInvalidPort() {
        assertNull(parseHttpProxyHostPort("example.com:99999", defaultPort = 443))
    }

    @Test
    fun parseHttpProxyHostPortRejectsOversizedHost() {
        val oversizedHost = "a".repeat(256)

        assertNull(parseHttpProxyHostPort(oversizedHost, defaultPort = 443, maxHostLength = 255))
    }

    @Test
    fun bridgeLimitsRejectInvalidBounds() {
        val error = runCatching {
            HttpProxyBridgeLimits(maxClients = 0)
        }.exceptionOrNull()

        assertEquals("maxClients must be in 1..512", error?.message)
    }
}
