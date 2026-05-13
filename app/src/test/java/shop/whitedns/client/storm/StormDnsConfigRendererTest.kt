package shop.whitedns.client.storm

import org.junit.Assert.assertTrue
import org.junit.Test
import shop.whitedns.client.model.ConnectionProfile
import shop.whitedns.client.model.ResolverProfile
import shop.whitedns.client.model.WhiteDnsSettings

class StormDnsConfigRendererTest {
    @Test
    fun renderClientTomlFromConnectionProfileIncludesCompleteServerInfo() {
        val resolverProfile = ResolverProfile(
            id = "resolver-main",
            name = "Main",
            resolverText = "1.1.1.1",
        )
        val connectionProfile = ConnectionProfile(
            id = "profile-main",
            name = "Main",
            customServerDomain = "server.example.com.",
            customServerEncryptionKey = "secret-key",
            customServerEncryptionMethod = 5,
            resolverProfileId = resolverProfile.id,
            connectionMode = "proxy",
        )
        val settings = WhiteDnsSettings(
            connectionProfiles = listOf(connectionProfile),
            resolverProfiles = listOf(resolverProfile),
            listenPort = "12345",
            httpProxyEnabled = false,
            uploadDuplication = "4",
            logLevel = "INFO",
        )

        val toml = StormDnsConfigRenderer.renderClientToml(
            connectionProfile = connectionProfile,
            settings = settings,
        )

        assertTrue(toml.contains("""DOMAINS = ["server.example.com"]"""))
        assertTrue(toml.contains("DATA_ENCRYPTION_METHOD = 5"))
        assertTrue(toml.contains("ENCRYPTION_KEY = \"secret-key\""))
        assertTrue(toml.contains("LISTEN_PORT = 12345"))
        assertTrue(toml.contains("UPLOAD_PACKET_DUPLICATION_COUNT = 4"))
        assertTrue(toml.contains("LOG_LEVEL = \"INFO\""))
    }

    @Test
    fun renderClientTomlAcceptsMultipleServerDomains() {
        val connectionProfile = ConnectionProfile(
            id = "profile-main",
            name = "Main",
            customServerDomain = "one.example.com, two.example.com\nthree.example.com.",
            customServerEncryptionKey = "secret-key",
        )

        val toml = StormDnsConfigRenderer.renderClientToml(
            connectionProfile = connectionProfile,
            settings = WhiteDnsSettings(resolverText = "1.1.1.1"),
        )

        assertTrue(toml.contains("""DOMAINS = ["one.example.com", "two.example.com", "three.example.com"]"""))
    }

    @Test
    fun renderClientTomlEscapesControlCharacters() {
        val toml = StormDnsConfigRenderer.renderClientToml(
            serverProfile = StormDnsServerProfile(
                id = "custom",
                label = "Custom",
                domain = "server.example.com",
                encryptionKey = "line\nquote\"tab\tctrl\u0001slash\\",
                encryptionMethod = 1,
            ),
            settings = WhiteDnsSettings(
                resolverText = "1.1.1.1",
                listenIp = "127.0.0.1",
                socksUsername = "user\tname",
                socksPassword = "pass\nword",
                logLevel = "INFO",
            ),
        )

        assertTrue(toml.contains("ENCRYPTION_KEY = \"line\\nquote\\\"tab\\tctrl\\u0001slash\\\\\""))
        assertTrue(toml.contains("SOCKS5_USER = \"user\\tname\""))
        assertTrue(toml.contains("SOCKS5_PASS = \"pass\\nword\""))
    }
}
