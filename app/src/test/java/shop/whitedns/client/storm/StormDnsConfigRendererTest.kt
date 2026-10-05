package shop.whitedns.client.storm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import shop.whitedns.client.model.ConnectionProfile
import shop.whitedns.client.model.CottenDnsProfileSettings
import shop.whitedns.client.model.DnsClientEngine
import shop.whitedns.client.model.ResolverProfile
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.importAdvancedSettingsProfileFromToml
import shop.whitedns.client.model.resolve

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
        assertFalse(toml.contains("LEGACY_SESSION_ID"))
        assertFalse(toml.contains("FAST_CONNECT"))
    }

    @Test
    fun renderClientTomlAddsCottenDnsDefaultsOnlyForCottenDnsProfiles() {
        val connectionProfile = ConnectionProfile(
            id = "cotten",
            name = "CottenDNS",
            customServerDomain = "cotten.example.com",
            customServerEncryptionKey = "secret-key",
            engine = DnsClientEngine.CottenDns,
        )

        val toml = StormDnsConfigRenderer.renderClientToml(
            connectionProfile = connectionProfile,
            settings = WhiteDnsSettings(connectionProfiles = listOf(connectionProfile)),
        )

        assertTrue(toml.contains("LEGACY_SESSION_ID = false"))
        assertTrue(toml.contains("RESOLVER_TRANSPORT = \"auto\""))
        assertTrue(toml.contains("""QUERY_TYPES = ["TXT"]"""))
        assertTrue(toml.contains("FAST_CONNECT = true"))
    }

    private fun cottenProfile(
        domain: String = "cotten.example.com",
        cotten: CottenDnsProfileSettings = CottenDnsProfileSettings(),
    ): ConnectionProfile = ConnectionProfile(
        id = "cotten",
        name = "CottenDNS",
        customServerDomain = domain,
        customServerEncryptionKey = "secret-key",
        engine = DnsClientEngine.CottenDns,
        cottenSettings = cotten,
    )

    private fun render(profile: ConnectionProfile): String =
        StormDnsConfigRenderer.renderClientToml(
            connectionProfile = profile,
            settings = WhiteDnsSettings(connectionProfiles = listOf(profile)),
        )

    @Test
    fun renderClientTomlEmitsEveryDomainInTheCommaSeparatedList() {
        val toml = render(cottenProfile(domain = "a.example.com, b.example.com., a.example.com"))

        // Trailing dot stripped, duplicate dropped, order preserved.
        assertTrue(toml.contains("""DOMAINS = ["a.example.com", "b.example.com"]"""))
    }

    @Test
    fun renderClientTomlKeepsSingleDomainOutputUnchanged() {
        val toml = render(cottenProfile(domain = "only.example.com"))

        assertTrue(toml.contains("""DOMAINS = ["only.example.com"]"""))
    }

    @Test
    fun renderClientTomlAppliesCottenDnsPreset() {
        val toml = render(cottenProfile(cotten = CottenDnsProfileSettings(configPreset = "survival")))

        assertTrue(toml.contains("CONFIG_PRESET = \"survival\""))
        assertTrue(toml.contains("""QUERY_TYPES = ["TXT", "CNAME", "HTTPS", "A"]"""))
        assertTrue(toml.contains("QNAME_LABEL_LENGTH = 42"))
        assertTrue(toml.contains("MTU_PROBE_SAMPLES = 5"))
        assertTrue(toml.contains("EDNS_UDP_SIZE = 1232"))
    }

    @Test
    fun renderClientTomlLetsExplicitOverridesBeatThePreset() {
        val toml = render(
            cottenProfile(
                cotten = CottenDnsProfileSettings(
                    configPreset = "survival",
                    transportMode = "tcp",
                    deliveryMode = "txt",
                    qnameMode = "aggressive",
                ),
            ),
        )

        assertTrue(toml.contains("RESOLVER_TRANSPORT = \"tcp\""))
        assertTrue(toml.contains("""QUERY_TYPES = ["TXT"]"""))
        assertTrue(toml.contains("QNAME_LABEL_LENGTH = 32"))
    }

    @Test
    fun renderClientTomlEmitsEncryptedResolverKeysOnlyForDotAndDoh() {
        val doh = render(
            cottenProfile(
                cotten = CottenDnsProfileSettings(
                    transportMode = "doh",
                    resolverTlsServerName = "dns.example.com",
                ),
            ),
        )
        assertTrue(doh.contains("RESOLVER_DOH_PORT = 443"))
        assertTrue(doh.contains("RESOLVER_DOH_PATH = \"/dns-query\""))
        assertTrue(doh.contains("RESOLVER_TLS_SERVER_NAME = \"dns.example.com\""))
        assertFalse(doh.contains("RESOLVER_DOT_PORT"))

        val udp = render(cottenProfile(cotten = CottenDnsProfileSettings(transportMode = "udp")))
        assertFalse(udp.contains("RESOLVER_DOH_PORT"))
        assertFalse(udp.contains("RESOLVER_TLS_SERVER_NAME"))
    }

    /**
     * A legacy server must never receive the native-only optimization suite.
     * Selecting the Master/Storm preset is what declares the legacy target now.
     */
    @Test
    fun renderClientTomlForcesTheSafeSubsetForTheMasterStormPreset() {
        val toml = render(
            cottenProfile(
                cotten = CottenDnsProfileSettings(
                    configPreset = CottenDnsProfileSettings.PresetMasterStorm,
                    transportMode = "doh",
                    deliveryMode = "all",
                    qnameMode = "aggressive",
                ),
            ),
        )

        assertTrue(toml.contains("LEGACY_SESSION_ID = true"))
        assertTrue(toml.contains("CONFIG_PRESET = \"default\""))
        assertTrue(toml.contains("RESOLVER_TRANSPORT = \"udp\""))
        assertTrue(toml.contains("""QUERY_TYPES = ["TXT"]"""))
        assertTrue(toml.contains("QNAME_LABEL_LENGTH = 63"))
        assertTrue(toml.contains("ADAPTIVE_DUPLICATION = false"))
        assertTrue(toml.contains("DNS_EDNS_COOKIE = false"))
        assertTrue(toml.contains("MTU_ADAPTIVE_GROUPING = false"))
        assertFalse(toml.contains("RESOLVER_DOH_PORT"))
    }

    /**
     * The summary exists to tell the user what the profile emits, so it is only
     * useful if it agrees with the emitted TOML.
     */
    @Test
    fun summaryAgreesWithTheGeneratedToml() {
        val cases = listOf(
            CottenDnsProfileSettings(),
            CottenDnsProfileSettings(configPreset = "survival"),
            CottenDnsProfileSettings(configPreset = "speed", transportMode = "tcp"),
            CottenDnsProfileSettings(deliveryMode = "all", qnameMode = "aggressive"),
            CottenDnsProfileSettings(configPreset = CottenDnsProfileSettings.PresetMasterStorm),
        )

        cases.forEach { settings ->
            val toml = render(cottenProfile(cotten = settings))
            val summary = CottenDnsSettingsRenderer.summarize(settings)

            val qnameLen = Regex("QNAME_LABEL_LENGTH = (\\d+)").find(toml)!!.groupValues[1]
            assertTrue(
                "summary MTU line lost the label length for $settings",
                summary.mtu.contains("$qnameLen-char labels"),
            )

            val types = Regex("QUERY_TYPES = \\[(.*)]").find(toml)!!.groupValues[1]
                .split(",").map { it.trim().trim('"') }
            types.forEach { type ->
                assertTrue("summary delivery dropped $type for $settings", summary.delivery.contains(type))
            }
        }
    }

    /**
     * A profile can carry CottenDNS settings while running the StormDNS engine —
     * the user may have switched engines, or the values may have been restored
     * from JSON. None of it may reach the StormDNS binary, which does not know
     * these keys and drives its own background MTU scan.
     */
    @Test
    fun renderClientTomlKeepsCottenDnsKeysOutOfStormDnsProfiles() {
        val loaded = CottenDnsProfileSettings(
            configPreset = "survival",
            transportMode = "doh",
            deliveryMode = "all",
            qnameMode = "aggressive",
            resolverTlsServerName = "leak.example.com",
            resolverTlsPin = "leaked-pin",
            resolverDoHPath = "/leaked",
        )
        val toml = render(cottenProfile(cotten = loaded).copy(engine = DnsClientEngine.StormDns))

        val cottenOnlyKeys = listOf(
            "TERMINAL_UI", "RESOLVER_IP_MODE", "CONFIG_PRESET", "LEGACY_SESSION_ID", "RESOLVER_TRANSPORT", "QUERY_TYPES",
            "QNAME_LABEL_LENGTH", "FAST_CONNECT", "RESOLVER_RATE_LIMIT_ENABLED",
            "DNS_RANDOMIZE_QUERY_ID", "DNS_QNAME_CASE_RANDOMIZATION",
            "RESOLVER_IGNORE_INJECTED_NXDOMAIN", "ADAPTIVE_DUPLICATION",
            "DUPLICATION_PREFER_DISTINCT_DOMAINS", "ADAPTIVE_DUPLICATION_TARGET_DELIVERY",
            "DNS_EDNS_COOKIE", "EDNS_UDP_SIZE", "MTU_PROBE_SAMPLES", "MTU_MAX_LOSS",
            "MTU_ADAPTIVE_GROUPING", "MTU_GROUP_GAP_RATIO",
            "RESOLVER_TLS_SERVER_NAME", "RESOLVER_TLS_PIN",
            "RESOLVER_DOT_PORT", "RESOLVER_DOH_PORT", "RESOLVER_DOH_PATH",
        )
        cottenOnlyKeys.forEach { key ->
            assertFalse("CottenDNS key $key leaked into a StormDNS profile", toml.contains(key))
        }
        assertFalse(toml.contains("leak.example.com"))
        assertFalse(toml.contains("leaked-pin"))
    }

    /**
     * The pre-connection scan must stay on the shared app-wide value for both
     * engines, so connecting is never slowed by the CottenDNS background knob.
     */
    @Test
    fun initialScanParallelismStaysSharedAndFast() {
        val shared = WhiteDnsSettings().resolve().mtuTestParallelismResolvers
        assertTrue("the shared initial value should be parallel", shared > 1)

        listOf(DnsClientEngine.CottenDns, DnsClientEngine.StormDns).forEach { engine ->
            val toml = render(
                cottenProfile(cotten = CottenDnsProfileSettings(backgroundScanParallelism = 1))
                    .copy(engine = engine),
            )
            assertTrue(
                "$engine initial scan was not the shared value",
                toml.contains("MTU_TEST_PARALLELISM_RESOLVERS = $shared"),
            )
        }
    }

    /** The background sweep is CottenDNS-only and defaults to one resolver. */
    @Test
    fun backgroundScanParallelismIsCottenDnsOnly() {
        val cotten = render(cottenProfile())
        assertTrue(cotten.contains("MTU_BACKGROUND_PARALLELISM = 1"))
        assertTrue(cotten.contains("TERMINAL_UI = \"plain\""))
        assertTrue(cotten.contains("RESOLVER_IP_MODE = \"auto\""))

        val tuned = render(
            cottenProfile(cotten = CottenDnsProfileSettings(backgroundScanParallelism = 6)),
        )
        assertTrue(tuned.contains("MTU_BACKGROUND_PARALLELISM = 6"))

        val storm = render(
            cottenProfile(cotten = CottenDnsProfileSettings(backgroundScanParallelism = 6))
                .copy(engine = DnsClientEngine.StormDns),
        )
        assertFalse("background key leaked to StormDNS", storm.contains("MTU_BACKGROUND_PARALLELISM"))
    }

    /**
     * IP mode and the AAAA delivery carrier are CottenDNS-only. An explicit
     * choice is emitted; Compatibility pins "auto" and TXT so a legacy server is
     * never asked for IPv6 resolvers or an AAAA answer it cannot make.
     */
    @Test
    fun ipModeAndAaaaDeliveryAreUserSelectableAndGated() {
        val ipv6 = render(cottenProfile(cotten = CottenDnsProfileSettings(ipMode = "ipv6")))
        assertTrue(ipv6.contains("RESOLVER_IP_MODE = \"ipv6\""))

        val dual = render(cottenProfile(cotten = CottenDnsProfileSettings(ipMode = "dual")))
        assertTrue(dual.contains("RESOLVER_IP_MODE = \"dual\""))

        val aaaa = render(cottenProfile(cotten = CottenDnsProfileSettings(deliveryMode = "txt-aaaa")))
        assertTrue(aaaa.contains("""QUERY_TYPES = ["TXT", "AAAA"]"""))

        val all = render(cottenProfile(cotten = CottenDnsProfileSettings(deliveryMode = "all")))
        assertTrue(all.contains("""QUERY_TYPES = ["TXT", "CNAME", "NULL", "HTTPS", "AAAA"]"""))

        // Compatibility forces the legacy-safe subset regardless of the request.
        val compat = render(
            cottenProfile(
                cotten = CottenDnsProfileSettings(
                    configPreset = CottenDnsProfileSettings.PresetMasterStorm,
                    ipMode = "ipv6",
                    deliveryMode = "txt-aaaa",
                ),
            ),
        )
        assertTrue(compat.contains("RESOLVER_IP_MODE = \"auto\""))
        assertTrue(compat.contains("""QUERY_TYPES = ["TXT"]"""))

        // Never leak to a StormDNS profile.
        val storm = render(
            cottenProfile(cotten = CottenDnsProfileSettings(ipMode = "ipv6", deliveryMode = "txt-aaaa"))
                .copy(engine = DnsClientEngine.StormDns),
        )
        assertFalse("AAAA carrier leaked to StormDNS", storm.contains("AAAA"))
        assertFalse("IP mode leaked to StormDNS", storm.contains("RESOLVER_IP_MODE"))
    }

    @Test
    fun queryRateLimitAndTimingMaskRenderPerScope() {
        val off = render(cottenProfile(cotten = CottenDnsProfileSettings()))
        assertTrue(off.contains("QUERY_RATE_LIMIT_PER_SECOND = 0"))
        assertTrue(off.contains("QUERY_TIMING_JITTER = 0.0"))

        val total = render(cottenProfile(cotten = CottenDnsProfileSettings(queryRateLimit = "5", timingMask = "light")))
        assertTrue(total.contains("QUERY_RATE_LIMIT_PER_SECOND = 5"))
        assertTrue(total.contains("QUERY_RATE_LIMIT_PER_RESOLVER_PER_SECOND = 0"))
        assertTrue(total.contains("QUERY_TIMING_JITTER = 0.3"))

        val perResolver = render(
            cottenProfile(cotten = CottenDnsProfileSettings(queryRateLimit = "3", queryRateScope = "resolver")),
        )
        assertTrue(perResolver.contains("QUERY_RATE_LIMIT_PER_SECOND = 0"))
        assertTrue(perResolver.contains("QUERY_RATE_LIMIT_PER_RESOLVER_PER_SECOND = 3"))

        // Client-only, so Compatibility keeps it.
        val compat = render(
            cottenProfile(
                cotten = CottenDnsProfileSettings(
                    configPreset = CottenDnsProfileSettings.PresetMasterStorm,
                    queryRateLimit = "3",
                ),
            ),
        )
        assertTrue(compat.contains("QUERY_RATE_LIMIT_PER_SECOND = 3"))

        // Garbage falls back to off; StormDNS never sees the keys.
        val bad = render(cottenProfile(cotten = CottenDnsProfileSettings(queryRateLimit = "999")))
        assertTrue(bad.contains("QUERY_RATE_LIMIT_PER_SECOND = 0"))
        val storm = render(
            cottenProfile(cotten = CottenDnsProfileSettings(queryRateLimit = "5"))
                .copy(engine = DnsClientEngine.StormDns),
        )
        assertFalse("rate limit leaked to StormDNS", storm.contains("QUERY_RATE_LIMIT"))
    }

    @Test
    fun backgroundScanParallelismStaysClamped() {
        val tooLow = render(
            cottenProfile(cotten = CottenDnsProfileSettings(backgroundScanParallelism = 0)),
        )
        assertTrue(tooLow.contains("MTU_BACKGROUND_PARALLELISM = 1"))

        val tooHigh = render(
            cottenProfile(cotten = CottenDnsProfileSettings(backgroundScanParallelism = 9999)),
        )
        assertTrue(
            tooHigh.contains(
                "MTU_BACKGROUND_PARALLELISM = " +
                    "${CottenDnsProfileSettings.MaxBackgroundScanParallelism}",
            ),
        )
    }

    /**
     * The reverse direction: everything the shared block emits must be a key both
     * engines understand, so a StormDNS-shaped setting never reaches CottenDNS as
     * an unknown key.
     */
    @Test
    fun stormDnsAndCottenDnsShareTheSameBaseKeys() {
        fun keysFor(engine: String): Set<String> =
            render(cottenProfile().copy(engine = engine))
                .lines()
                .mapNotNull { it.substringBefore(" =", "").ifBlank { null } }
                .toSet()

        val storm = keysFor(DnsClientEngine.StormDns)
        val cotten = keysFor(DnsClientEngine.CottenDns)

        assertEquals(
            "StormDNS emitted keys CottenDNS never sees",
            emptySet<String>(),
            storm - cotten,
        )
        // The difference is exactly the CottenDNS-only block.
        assertTrue((cotten - storm).contains("CONFIG_PRESET"))
        assertTrue((cotten - storm).contains("MTU_ADAPTIVE_GROUPING"))
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
}
