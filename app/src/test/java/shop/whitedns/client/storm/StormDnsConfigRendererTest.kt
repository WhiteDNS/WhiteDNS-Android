package shop.whitedns.client.storm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import shop.whitedns.client.model.ConnectionProfile
import shop.whitedns.client.model.ResolverProfile
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.importAdvancedSettingsProfileFromToml

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
        assertTrue(toml.contains("STATS_REPORT_INTERVAL_SECONDS = 1.0"))
        assertTrue(toml.contains("LOG_LEVEL = \"INFO\""))
    }

    @Test
    fun renderAdvancedSettingsTomlExportsSettingsWithoutServerInfo() {
        val settings = WhiteDnsSettings(
            customServerDomain = "server.example.com",
            customServerEncryptionKey = "secret-key",
            listenIp = "0.0.0.0",
            listenPort = "12345",
            httpProxyEnabled = false,
            httpProxyPort = "12346",
            uploadDuplication = "4",
            tunnelPacketTimeoutSeconds = "11.5",
            trafficWarmupEnabled = false,
            trafficWarmupProbeCount = "2",
            autoTuneEnabled = true,
            logLevel = "INFO",
        )

        val toml = StormDnsConfigRenderer.renderAdvancedSettingsToml(settings)

        assertTrue(toml.contains("LISTEN_IP = \"0.0.0.0\""))
        assertTrue(toml.contains("LISTEN_PORT = 12345"))
        assertTrue(toml.contains("HTTP_PROXY_ENABLED = false"))
        assertTrue(toml.contains("HTTP_PROXY_PORT = 12346"))
        assertTrue(toml.contains("UPLOAD_PACKET_DUPLICATION_COUNT = 4"))
        assertTrue(toml.contains("TUNNEL_PACKET_TIMEOUT_SECONDS = 11.5"))
        assertTrue(toml, toml.contains("TRAFFIC_WARMUP_ENABLED = false"))
        assertTrue(toml.contains("TRAFFIC_WARMUP_PROBE_COUNT = 2"))
        assertTrue(toml.contains("AUTO_TUNE_ENABLED = true"))
        assertTrue(toml.contains("LOG_LEVEL = \"INFO\""))
        assertFalse(toml.contains("DOMAINS"))
        assertFalse(toml.contains("DATA_ENCRYPTION_METHOD"))
        assertFalse(toml.contains("ENCRYPTION_KEY"))
        assertFalse(toml.contains("server.example.com"))
        assertFalse(toml.contains("secret-key"))

        val imported = WhiteDnsSettings().importAdvancedSettingsProfileFromToml("Imported", toml)
        assertEquals("12345", imported.listenPort)
        assertEquals(false, imported.httpProxyEnabled)
        assertEquals("4", imported.uploadDuplication)
        assertEquals("11.5", imported.tunnelPacketTimeoutSeconds)
        assertEquals(false, imported.trafficWarmupEnabled)
        assertEquals(true, imported.autoTuneEnabled)
    }

    @Test
    fun renderScanClientTomlDisablesLocalListenersAndUsesSingleProbeWorker() {
        val toml = StormDnsConfigRenderer.renderScanClientToml(
            serverProfile = shop.whitedns.client.model.StormDnsServerProfile(
                id = "server",
                label = "Server",
                domain = "scan.example.com",
                encryptionKey = "secret-key",
                encryptionMethod = 1,
            ),
            settings = WhiteDnsSettings(
                listenPort = "10886",
                localDnsEnabled = true,
                localDnsPort = "10888",
                mtuTestParallelismResolvers = "50",
                startupMode = "logs",
                trafficWarmupEnabled = true,
            ),
        )

        assertTrue(toml.contains("LISTEN_PORT = 0"))
        assertTrue(toml.contains("LOCAL_DNS_ENABLED = false"))
        assertTrue(toml.contains("LOCAL_DNS_PORT = 0"))
        assertTrue(toml.contains("MTU_TEST_PARALLELISM_RESOLVERS = 1"))
        assertTrue(toml.contains("STARTUP_MODE = \"resolvers\""))
    }

    @Test
    fun renderClientTomlEscapesControlCharactersInStrings() {
        val toml = StormDnsConfigRenderer.renderClientToml(
            serverProfile = shop.whitedns.client.model.StormDnsServerProfile(
                id = "server",
                label = "Server",
                domain = "server.example.com",
                encryptionKey = "line\nkey\t\"\\${1.toChar()}",
                encryptionMethod = 1,
            ),
            settings = WhiteDnsSettings(
                socks5Authentication = true,
                socksUsername = "user\rname",
                socksPassword = "pass\bword",
            ),
        )

        assertTrue(toml.contains("ENCRYPTION_KEY = \"line\\nkey\\t\\\"\\\\\\u0001\""))
        assertTrue(toml.contains("SOCKS5_USER = \"user\\rname\""))
        assertTrue(toml.contains("SOCKS5_PASS = \"pass\\bword\""))
    }
}
