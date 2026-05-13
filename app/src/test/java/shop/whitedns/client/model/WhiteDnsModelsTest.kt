package shop.whitedns.client.model

import java.util.Base64
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhiteDnsModelsTest {
    @Test
    fun defaultSettingsStartWithBlankCustomConnection() {
        val settings = WhiteDnsSettings().syncSelectedConnectionProfileFields()
        val profile = settings.selectedConnectionProfile()

        assertEquals(ConnectionProfile.DefaultId, profile.id)
        assertEquals("custom", profile.serverMode)
        assertEquals("", profile.customServerDomain)
        assertEquals("", profile.customServerEncryptionKey)
        assertEquals("custom", settings.serverMode)
    }

    @Test
    fun runtimeConnectionSettingsEnableTunneledDnsForProxyMode() {
        val runtimeSettings = WhiteDnsSettings(
            connectionMode = "proxy",
            localDnsEnabled = false,
            localDnsPort = "53",
        ).runtimeConnectionSettings()
        val resolvedSettings = runtimeSettings.resolve()

        assertEquals("proxy", resolvedSettings.connectionMode)
        assertEquals(true, resolvedSettings.localDnsEnabled)
        assertEquals(WhiteDnsRuntimeProxy.LocalDnsPortInt, resolvedSettings.localDnsPort)
    }

    @Test
    fun runtimeConnectionSettingsKeepFullVpnOnVirtualTunDnsPath() {
        val runtimeSettings = WhiteDnsSettings(
            connectionMode = "vpn",
            listenIp = "0.0.0.0",
            listenPort = "12345",
            httpProxyEnabled = true,
            socks5Authentication = true,
            localDnsEnabled = true,
            localDnsPort = "53",
        ).runtimeConnectionSettings()
        val resolvedSettings = runtimeSettings.resolve()

        assertEquals("vpn", resolvedSettings.connectionMode)
        assertEquals(WhiteDnsRuntimeProxy.ListenIp, resolvedSettings.listenIp)
        assertEquals(WhiteDnsRuntimeProxy.ListenPortInt, resolvedSettings.listenPort)
        assertEquals(false, resolvedSettings.httpProxyEnabled)
        assertEquals(false, resolvedSettings.socks5Authentication)
        assertEquals(false, resolvedSettings.localDnsEnabled)
        assertEquals(WhiteDnsRuntimeProxy.LocalDnsPortInt, resolvedSettings.localDnsPort)
    }

    @Test
    fun resolveDisablesSocksAuthenticationWhenCredentialsAreBlank() {
        val resolvedSettings = WhiteDnsSettings(
            socks5Authentication = true,
            socksUsername = "   ",
            socksPassword = "secret",
        ).resolve()

        assertEquals(false, resolvedSettings.socks5Authentication)
        assertEquals("   ", resolvedSettings.socksUsername)
        assertEquals("secret", resolvedSettings.socksPassword)
    }

    @Test
    fun syncSelectedConnectionProfileFieldsUsesSelectedResolverProfileText() {
        val resolverProfile = ResolverProfile(
            id = "resolver-main",
            name = "Main",
            resolverText = "1.1.1.1\n8.8.8.8",
        )
        val settings = WhiteDnsSettings(
            selectedConnectionProfileId = "profile-main",
            connectionProfiles = listOf(
                ConnectionProfile(
                    id = "profile-main",
                    name = "Main",
                    resolverProfileId = resolverProfile.id,
                ),
            ),
            selectedResolverProfileId = resolverProfile.id,
            resolverProfiles = listOf(resolverProfile),
            resolverText = "",
        )

        val syncedSettings = settings.syncSelectedConnectionProfileFields()

        assertEquals(resolverProfile.resolverText, syncedSettings.resolverText)
        assertEquals(listOf("1.1.1.1", "8.8.8.8"), syncedSettings.resolve().resolverEntries)
    }

    @Test
    fun updateManualResolverTextClearsSelectedResolverProfileAndKeepsTypedResolvers() {
        val resolverProfile = ResolverProfile(
            id = "resolver-main",
            name = "Main",
            resolverText = "1.1.1.1",
        )
        val settings = WhiteDnsSettings(
            selectedConnectionProfileId = "profile-main",
            connectionProfiles = listOf(
                ConnectionProfile(
                    id = "profile-main",
                    name = "Main",
                    resolverProfileId = resolverProfile.id,
                ),
            ),
            selectedResolverProfileId = resolverProfile.id,
            resolverProfiles = listOf(resolverProfile),
            resolverText = resolverProfile.resolverText,
        )
        val typedResolvers = "1.1.1.1\n8.8.8.8\n9.9.9.9"

        val updatedSettings = settings.updateManualResolverText(typedResolvers)

        assertEquals("", updatedSettings.selectedResolverProfileId)
        assertEquals("", updatedSettings.selectedConnectionProfile().resolverProfileId)
        assertEquals(typedResolvers, updatedSettings.resolverText)
        assertEquals(
            listOf("1.1.1.1", "8.8.8.8", "9.9.9.9"),
            updatedSettings.resolve().resolverEntries,
        )
    }

    @Test
    fun updateManualResolverTextClearsResolverProfileWhenSelectedConnectionIdIsStale() {
        val resolverProfile = ResolverProfile(
            id = "resolver-main",
            name = "Main",
            resolverText = "1.1.1.1",
        )
        val settings = WhiteDnsSettings(
            selectedConnectionProfileId = "missing-profile",
            connectionProfiles = listOf(
                ConnectionProfile(
                    id = "profile-main",
                    name = "Main",
                    resolverProfileId = resolverProfile.id,
                ),
            ),
            selectedResolverProfileId = resolverProfile.id,
            resolverProfiles = listOf(resolverProfile),
            resolverText = resolverProfile.resolverText,
        )

        val updatedSettings = settings.updateManualResolverText("8.8.8.8\n9.9.9.9")

        assertEquals("profile-main", updatedSettings.selectedConnectionProfileId)
        assertEquals("", updatedSettings.selectedConnectionProfile().resolverProfileId)
        assertEquals("", updatedSettings.selectedResolverProfileId)
        assertEquals(listOf("8.8.8.8", "9.9.9.9"), updatedSettings.resolve().resolverEntries)
    }

    @Test
    fun syncSelectedConnectionProfileFieldsPersistsSelectedConnectionMode() {
        val settings = WhiteDnsSettings(
            selectedConnectionProfileId = "profile-main",
            connectionProfiles = listOf(
                ConnectionProfile(
                    id = "profile-main",
                    name = "Main",
                    connectionMode = "proxy",
                ),
            ),
            connectionMode = "vpn",
        )

        val syncedSettings = settings.syncSelectedConnectionProfileFields()

        assertEquals("vpn", syncedSettings.connectionMode)
        assertEquals("vpn", syncedSettings.selectedConnectionProfile().connectionMode)
    }

    @Test
    fun moveConnectionProfileReordersCustomProfilesForSelectionLists() {
        val first = ConnectionProfile(id = "profile-first", name = "First", serverMode = "custom")
        val second = ConnectionProfile(id = "profile-second", name = "Second", serverMode = "custom")
        val third = ConnectionProfile(id = "profile-third", name = "Third", serverMode = "custom")
        val settings = WhiteDnsSettings(
            connectionProfiles = listOf(ConnectionProfile.defaultProfile(), first, second, third),
        )

        val updatedSettings = settings.moveConnectionProfile("profile-third", -1)

        assertEquals(
            listOf(ConnectionProfile.DefaultId, "profile-first", "profile-third", "profile-second"),
            updatedSettings.normalizedConnectionProfiles().map { it.id },
        )
    }

    @Test
    fun moveConnectionProfileToIndexReordersToDropTarget() {
        val first = ConnectionProfile(id = "profile-first", name = "First", serverMode = "custom")
        val second = ConnectionProfile(id = "profile-second", name = "Second", serverMode = "custom")
        val third = ConnectionProfile(id = "profile-third", name = "Third", serverMode = "custom")
        val settings = WhiteDnsSettings(
            connectionProfiles = listOf(ConnectionProfile.defaultProfile(), first, second, third),
        )

        val updatedSettings = settings.moveConnectionProfileToIndex("profile-first", 2)

        assertEquals(
            listOf(ConnectionProfile.DefaultId, "profile-second", "profile-first", "profile-third"),
            updatedSettings.normalizedConnectionProfiles().map { it.id },
        )
    }

    @Test
    fun moveResolverProfileReordersProfilesAndKeepsSelection() {
        val first = ResolverProfile(id = "resolver-first", name = "First", resolverText = "1.1.1.1")
        val second = ResolverProfile(id = "resolver-second", name = "Second", resolverText = "8.8.8.8")
        val third = ResolverProfile(id = "resolver-third", name = "Third", resolverText = "9.9.9.9")
        val settings = WhiteDnsSettings(
            selectedResolverProfileId = second.id,
            resolverProfiles = listOf(first, second, third),
        )

        val updatedSettings = settings.moveResolverProfile("resolver-second", 1)

        assertEquals(
            listOf("resolver-first", "resolver-third", "resolver-second"),
            updatedSettings.normalizedResolverProfiles().map { it.id },
        )
        assertEquals(second.id, updatedSettings.selectedResolverProfileId)
    }

    @Test
    fun moveResolverProfileToIndexReordersToDropTarget() {
        val first = ResolverProfile(id = "resolver-first", name = "First", resolverText = "1.1.1.1")
        val second = ResolverProfile(id = "resolver-second", name = "Second", resolverText = "8.8.8.8")
        val third = ResolverProfile(id = "resolver-third", name = "Third", resolverText = "9.9.9.9")
        val settings = WhiteDnsSettings(
            selectedResolverProfileId = first.id,
            resolverProfiles = listOf(first, second, third),
        )

        val updatedSettings = settings.moveResolverProfileToIndex("resolver-first", 2)

        assertEquals(
            listOf("resolver-second", "resolver-third", "resolver-first"),
            updatedSettings.normalizedResolverProfiles().map { it.id },
        )
        assertEquals(first.id, updatedSettings.selectedResolverProfileId)
    }

    @Test
    fun upsertResolverProfileNormalizesCommaSeparatedTextAndDefaultPorts() {
        val settings = WhiteDnsSettings()

        val updatedSettings = settings.upsertResolverProfile(
            ResolverProfile(
                id = "resolver-main",
                name = "Main",
                resolverText = "1.1.1.1:53, 8.8.8.8:53, 9.9.9.9:5353",
            ),
        )

        assertEquals("1.1.1.1\n8.8.8.8\n9.9.9.9:5353", updatedSettings.resolverText)
        assertEquals(updatedSettings.resolverText, updatedSettings.resolverProfiles.single().resolverText)
        assertEquals(
            listOf("1.1.1.1", "8.8.8.8", "9.9.9.9:5353"),
            updatedSettings.resolve().resolverEntries,
        )
    }

    @Test
    fun saveSelectedAdvancedProfileDoesNotPersistDefaultProfile() {
        val settings = WhiteDnsSettings(uploadDuplication = "9")

        val updatedSettings = settings.saveSelectedAdvancedProfile()

        assertEquals(AdvancedSettingsProfile.DefaultId, updatedSettings.selectedAdvancedProfileId)
        assertEquals("9", updatedSettings.uploadDuplication)
        assertTrue(updatedSettings.advancedProfiles.isEmpty())
    }

    @Test
    fun saveCurrentAdvancedProfileAsPersistsCustomProfileAndCanReturnToDefault() {
        val savedSettings = WhiteDnsSettings(
            uploadDuplication = "9",
            logLevel = "DEBUG",
        ).saveCurrentAdvancedProfileAs("Fast Tunnel")
        val customProfileId = savedSettings.selectedAdvancedProfileId

        assertEquals(1, savedSettings.advancedProfiles.size)
        assertEquals("Fast Tunnel", savedSettings.selectedAdvancedProfile().name)
        assertEquals("9", savedSettings.selectedAdvancedProfile().uploadDuplication)
        assertEquals("DEBUG", savedSettings.selectedAdvancedProfile().logLevel)

        val defaultSettings = savedSettings.selectAdvancedProfile(AdvancedSettingsProfile.DefaultId)

        assertEquals(AdvancedSettingsProfile.DefaultId, defaultSettings.selectedAdvancedProfileId)
        assertEquals("3", defaultSettings.uploadDuplication)
        assertEquals("WARN", defaultSettings.logLevel)
        assertEquals(customProfileId, defaultSettings.advancedProfiles.single().id)
    }

    @Test
    fun saveSelectedAdvancedProfileUpdatesCustomProfileInPlace() {
        val savedSettings = WhiteDnsSettings(
            uploadDuplication = "4",
        ).saveCurrentAdvancedProfileAs("Fast Tunnel")

        val updatedSettings = savedSettings
            .copy(uploadDuplication = "6")
            .saveSelectedAdvancedProfile()

        assertEquals(savedSettings.selectedAdvancedProfileId, updatedSettings.selectedAdvancedProfileId)
        assertEquals("Fast Tunnel", updatedSettings.selectedAdvancedProfile().name)
        assertEquals("6", updatedSettings.selectedAdvancedProfile().uploadDuplication)
    }

    @Test
    fun resolveBoundsTrafficWarmupSettings() {
        val settings = WhiteDnsSettings(
            trafficWarmupMode = WhiteDnsOptions.TrafficWarmupCustom,
            trafficWarmupProbeCount = "99",
            trafficKeepaliveIntervalSeconds = "1",
        )

        val resolvedSettings = settings.resolve()

        assertEquals(true, resolvedSettings.trafficWarmupEnabled)
        assertEquals(10, resolvedSettings.trafficWarmupProbeCount)
        assertEquals(2, resolvedSettings.trafficKeepaliveIntervalSeconds)
    }

    @Test
    fun resolveAppliesAdaptiveTrafficWarmupModes() {
        val offSettings = WhiteDnsSettings(
            trafficWarmupMode = WhiteDnsOptions.TrafficWarmupOff,
            trafficWarmupProbeCount = "9",
            trafficKeepaliveIntervalSeconds = "9",
        ).resolve()
        val startupOnlySettings = WhiteDnsSettings(
            trafficWarmupMode = WhiteDnsOptions.TrafficWarmupStartupOnly,
        ).resolve()
        val balancedSettings = WhiteDnsSettings(
            trafficWarmupMode = WhiteDnsOptions.TrafficWarmupBalanced,
        ).resolve()
        val aggressiveSettings = WhiteDnsSettings(
            trafficWarmupMode = WhiteDnsOptions.TrafficWarmupAggressive,
        ).resolve()

        assertEquals(false, offSettings.trafficWarmupEnabled)
        assertEquals(0, offSettings.trafficWarmupProbeCount)
        assertEquals(0, offSettings.trafficKeepaliveIntervalSeconds)
        assertEquals(2, startupOnlySettings.trafficWarmupProbeCount)
        assertEquals(0, startupOnlySettings.trafficKeepaliveIntervalSeconds)
        assertEquals(3, balancedSettings.trafficWarmupProbeCount)
        assertEquals(30, balancedSettings.trafficKeepaliveIntervalSeconds)
        assertEquals(6, aggressiveSettings.trafficWarmupProbeCount)
        assertEquals(5, aggressiveSettings.trafficKeepaliveIntervalSeconds)
    }

    @Test
    fun resolveNormalizesVpnIpv6StrategyAndMtuPreset() {
        val defaultResolvedSettings = WhiteDnsSettings(
            vpnIpv6Strategy = "invalid",
            vpnMtuPreset = "invalid",
            vpnCustomMtu = "42",
        ).resolve()
        val customResolvedSettings = WhiteDnsSettings(
            vpnIpv6Strategy = WhiteDnsOptions.VpnIpv6StrategyBypass,
            vpnMtuPreset = WhiteDnsOptions.VpnMtuPresetCustom,
            vpnCustomMtu = "99999",
        ).resolve()

        assertEquals(WhiteDnsOptions.VpnIpv6StrategyBlock, defaultResolvedSettings.vpnIpv6Strategy)
        assertEquals(WhiteDnsOptions.VpnMtuPreset1500, defaultResolvedSettings.vpnMtuPreset)
        assertEquals(1500, defaultResolvedSettings.vpnMtu)
        assertEquals(WhiteDnsOptions.VpnIpv6StrategyBypass, customResolvedSettings.vpnIpv6Strategy)
        assertEquals(WhiteDnsOptions.VpnMtuPresetCustom, customResolvedSettings.vpnMtuPreset)
        assertEquals(9000, customResolvedSettings.vpnMtu)
    }

    @Test
    fun resolveUsesAppMtuDefaults() {
        val resolvedSettings = WhiteDnsSettings().resolve()

        assertEquals(40, resolvedSettings.minUploadMtu)
        assertEquals(300, resolvedSettings.minDownloadMtu)
        assertEquals(140, resolvedSettings.maxUploadMtu)
        assertEquals(3000, resolvedSettings.maxDownloadMtu)
    }

    @Test
    fun resolveUsesFullScanStartupAndBoundsNativeReliabilitySettings() {
        val settings = WhiteDnsSettings(
            startupMode = "invalid",
            maxActiveStreams = "999999",
            localHandshakeTimeoutSeconds = "0",
            minUploadMtu = "300",
            maxUploadMtu = "100",
            minDownloadMtu = "5000",
            maxDownloadMtu = "1000",
        )

        val resolvedSettings = settings.resolve()

        assertEquals("resolvers", resolvedSettings.startupMode)
        assertEquals(65535, resolvedSettings.maxActiveStreams)
        assertEquals(5.0, resolvedSettings.localHandshakeTimeoutSeconds, 0.0)
        assertEquals(300, resolvedSettings.maxUploadMtu)
        assertEquals(5000, resolvedSettings.maxDownloadMtu)
    }

    @Test
    fun importStormDnsProfileLinkAcceptsRequiredPayloadOnly() {
        val payload = """
            {
              "schema": "whitedns.profile",
              "version": 1,
              "profile": {
                "name": "Imported Profile",
                "server": {
                  "domain": "server.example.com",
                  "encryption_key": "secret-key",
                  "encryption_method": 2
                }
              }
            }
        """.trimIndent()
        val link = "stormdns://${Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())}"

        val importedSettings = WhiteDnsSettings().importStormDnsProfileLink(link, nowMillis = 42L)
        val importedProfile = importedSettings.selectedConnectionProfile()

        assertEquals("profile-imported-42", importedProfile.id)
        assertEquals("Imported Profile", importedProfile.name)
        assertEquals("custom", importedProfile.serverMode)
        assertEquals("server.example.com", importedProfile.customServerDomain)
        assertEquals("secret-key", importedProfile.customServerEncryptionKey)
        assertEquals(2, importedProfile.customServerEncryptionMethod)
        assertEquals("proxy", importedSettings.connectionMode)
    }

    @Test
    fun exportAndImportStormDnsProfileLinkUsesOnlyRequiredProfileFields() {
        val resolverProfile = ResolverProfile(
            id = "resolver-main",
            name = "Main Resolvers",
            resolverText = "1.1.1.1\n8.8.8.8",
        )
        val connectionProfile = ConnectionProfile(
            id = "profile-main",
            name = "Main Profile",
            serverMode = "custom",
            customServerDomain = "server.example.com",
            customServerEncryptionKey = "secret-key",
            customServerEncryptionMethod = 5,
            resolverProfileId = resolverProfile.id,
            connectionMode = "vpn",
        )
        val settings = WhiteDnsSettings(
            selectedConnectionProfileId = connectionProfile.id,
            connectionProfiles = listOf(ConnectionProfile.defaultProfile(), connectionProfile),
            selectedResolverProfileId = resolverProfile.id,
            resolverProfiles = listOf(resolverProfile),
            resolverText = resolverProfile.resolverText,
            listenPort = "12345",
            httpProxyEnabled = false,
            balancingStrategy = 4,
            uploadDuplication = "2",
            downloadDuplication = "6",
            rxTxWorkers = "8",
            startupMode = "logs",
            trafficWarmupEnabled = false,
            trafficKeepaliveIntervalSeconds = "15",
            splitTunnelMode = WhiteDnsOptions.SplitTunnelModeExclude,
            splitTunnelPackages = listOf("org.telegram.messenger"),
            logLevel = "INFO",
        )

        val link = settings.exportStormDnsProfileLink(connectionProfile)
        val exportedProfileJson = JSONObject(decodeStormDnsProfilePayload(link)).getJSONObject("profile")
        val exportedServerJson = exportedProfileJson.getJSONObject("server")
        val importedSettings = WhiteDnsSettings().importStormDnsProfileLink(link, nowMillis = 100L)
        val importedProfile = importedSettings.selectedConnectionProfile()

        assertTrue(link.startsWith("stormdns://"))
        assertEquals(setOf("name", "server"), exportedProfileJson.keys().asSequence().toSet())
        assertEquals(
            setOf("domain", "encryption_key", "encryption_method"),
            exportedServerJson.keys().asSequence().toSet(),
        )
        assertEquals("Main Profile", importedProfile.name)
        assertEquals("server.example.com", importedProfile.customServerDomain)
        assertEquals("secret-key", importedProfile.customServerEncryptionKey)
        assertEquals(5, importedProfile.customServerEncryptionMethod)
        assertEquals("", importedProfile.resolverProfileId)
        assertEquals("proxy", importedSettings.connectionMode)
        assertEquals(emptyList<String>(), importedSettings.resolve().resolverEntries)
        assertEquals("10886", importedSettings.listenPort)
        assertEquals(true, importedSettings.httpProxyEnabled)
        assertEquals(3, importedSettings.balancingStrategy)
        assertEquals("3", importedSettings.uploadDuplication)
        assertEquals("7", importedSettings.downloadDuplication)
        assertEquals("4", importedSettings.rxTxWorkers)
        assertEquals("resolvers", importedSettings.startupMode)
        assertEquals(true, importedSettings.trafficWarmupEnabled)
        assertEquals("5", importedSettings.trafficKeepaliveIntervalSeconds)
        assertEquals(WhiteDnsOptions.SplitTunnelModeOff, importedSettings.splitTunnelMode)
        assertEquals(emptyList<String>(), importedSettings.splitTunnelPackages)
        assertEquals("WARN", importedSettings.logLevel)
    }

    @Test
    fun exportStormDnsProfileLinkAlwaysWritesRequiredPayloadOnly() {
        val connectionProfile = ConnectionProfile(
            id = "profile-main",
            name = "Main Profile",
            serverMode = "custom",
            customServerDomain = "server.example.com",
            customServerEncryptionKey = "secret-key",
            customServerEncryptionMethod = 5,
            connectionMode = "vpn",
        )
        val settings = WhiteDnsSettings(
            selectedConnectionProfileId = connectionProfile.id,
            connectionProfiles = listOf(ConnectionProfile.defaultProfile(), connectionProfile),
            listenPort = "12345",
            httpProxyEnabled = false,
            trafficWarmupEnabled = false,
            logLevel = "INFO",
        )

        val link = settings.exportStormDnsProfileLink(profile = connectionProfile)
        val profileJson = JSONObject(decodeStormDnsProfilePayload(link)).getJSONObject("profile")
        val importedSettings = WhiteDnsSettings().importStormDnsProfileLink(link, nowMillis = 300L)
        val importedProfile = importedSettings.selectedConnectionProfile()

        assertEquals(setOf("name", "server"), profileJson.keys().asSequence().toSet())
        assertEquals("Main Profile", importedProfile.name)
        assertEquals("server.example.com", importedProfile.customServerDomain)
        assertEquals("secret-key", importedProfile.customServerEncryptionKey)
        assertEquals(5, importedProfile.customServerEncryptionMethod)
        assertEquals("proxy", importedSettings.connectionMode)
        assertEquals("10886", importedSettings.listenPort)
        assertEquals(true, importedSettings.httpProxyEnabled)
        assertEquals(true, importedSettings.trafficWarmupEnabled)
        assertEquals("WARN", importedSettings.logLevel)
    }

    @Test
    fun exportAndImportStormDnsProfileLinkPreservesMultipleDomains() {
        val connectionProfile = ConnectionProfile(
            id = "profile-main",
            name = "Multi Domain",
            serverMode = "custom",
            customServerDomain = "one.example.com, two.example.com\nthree.example.com.",
            customServerEncryptionKey = "secret-key",
            customServerEncryptionMethod = 2,
        )

        val link = WhiteDnsSettings(
            selectedConnectionProfileId = connectionProfile.id,
            connectionProfiles = listOf(connectionProfile),
        ).exportStormDnsProfileLink(profile = connectionProfile)
        val serverJson = JSONObject(decodeStormDnsProfilePayload(link))
            .getJSONObject("profile")
            .getJSONObject("server")
        val importedProfile = WhiteDnsSettings()
            .importStormDnsProfileLink(link, nowMillis = 301L)
            .selectedConnectionProfile()

        assertEquals("one.example.com", serverJson.getString("domain"))
        assertEquals(listOf("one.example.com", "two.example.com", "three.example.com"), importedProfile.customServerDomain.lines())
    }

    @Test
    fun importStormDnsProfileLinkIgnoresResolverPayload() {
        val existingResolverProfile = ResolverProfile(
            id = "resolver-existing",
            name = "Existing",
            resolverText = "9.9.9.9",
        )
        val existingSettings = WhiteDnsSettings(
            selectedResolverProfileId = existingResolverProfile.id,
            resolverProfiles = listOf(existingResolverProfile),
            resolverText = existingResolverProfile.resolverText,
        )
        val payload = """
            {
              "schema": "whitedns.profile",
              "version": 1,
              "profile": {
                "name": "Imported",
                "server": {
                  "domain": "server.example.com",
                  "encryption_key": "secret-key",
                  "encryption_method": 2
                },
                "connection": {
                  "mode": "vpn"
                },
                "local_proxy": {
                  "listen_port": "12345"
                },
                "resolvers": {
                  "name": "Imported Resolvers",
                  "entries": ["1.1.1.1", "8.8.8.8"]
                }
              }
            }
        """.trimIndent()
        val link = "stormdns://${Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())}"

        val importedSettings = existingSettings.importStormDnsProfileLink(link, nowMillis = 200L)
        val importedProfile = importedSettings.selectedConnectionProfile()

        assertEquals("Imported", importedProfile.name)
        assertEquals("", importedProfile.resolverProfileId)
        assertEquals(listOf(existingResolverProfile), importedSettings.resolverProfiles)
        assertEquals(existingResolverProfile.id, importedSettings.selectedResolverProfileId)
        assertEquals("proxy", importedSettings.connectionMode)
        assertEquals("10886", importedSettings.listenPort)
        assertEquals(listOf("9.9.9.9"), importedSettings.resolve().resolverEntries)
    }

    @Test
    fun exportAllStormDnsProfileLinksWritesOneLinkPerCustomProfile() {
        val first = ConnectionProfile(
            id = "profile-first",
            name = "First",
            serverMode = "custom",
            customServerDomain = "first.example.com",
            customServerEncryptionKey = "first-key",
            customServerEncryptionMethod = 1,
        )
        val second = ConnectionProfile(
            id = "profile-second",
            name = "Second",
            serverMode = "custom",
            customServerDomain = "second.example.com",
            customServerEncryptionKey = "second-key",
            customServerEncryptionMethod = 2,
        )
        val settings = WhiteDnsSettings(
            connectionProfiles = listOf(ConnectionProfile.defaultProfile(), first, second),
        )

        val exportedLinks = settings.exportAllStormDnsProfileLinks().lineSequence().toList()

        assertEquals(2, exportedLinks.size)
        assertTrue(exportedLinks.all { it.startsWith("stormdns://") })
        assertEquals("first.example.com", WhiteDnsSettings().importStormDnsProfileLink(exportedLinks[0]).customServerDomain)
        assertEquals("second.example.com", WhiteDnsSettings().importStormDnsProfileLink(exportedLinks[1]).customServerDomain)
    }

    @Test
    fun importStormDnsProfileLinksImportsManyLinksLineByLine() {
        fun linkFor(domain: String, key: String): String {
            val payload = """
                {
                  "schema": "whitedns.profile",
                  "version": 1,
                  "profile": {
                    "name": "$domain",
                    "server": {
                      "domain": "$domain",
                      "encryption_key": "$key",
                      "encryption_method": 1
                    }
                  }
                }
            """.trimIndent()
            return "stormdns://${Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())}"
        }
        val firstLink = linkFor("first.example.com", "first-key")
        val secondLink = linkFor("second.example.com", "second-key")

        val importedSettings = WhiteDnsSettings().importStormDnsProfileLinks(
            rawLinks = "$firstLink\n\n$secondLink",
            nowMillis = 500L,
        )
        val importedProfiles = importedSettings.normalizedConnectionProfiles()
            .filter { it.customServerDomain.isNotBlank() }

        assertEquals(listOf("first.example.com", "second.example.com"), importedProfiles.map { it.customServerDomain })
        assertEquals(listOf("profile-imported-500", "profile-imported-501"), importedProfiles.map { it.id })
        assertEquals("second.example.com", importedSettings.selectedConnectionProfile().customServerDomain)
    }

    @Test
    fun validateResolverTextAcceptsSupportedResolverIpFormats() {
        val validation = validateResolverText(
            """
            # comment
            1.1.1.1, 8.8.8.8:5353
            [2001:4860:4860::8888]:53
            192.168.10.0/30:5300
            """.trimIndent(),
        )

        assertEquals(emptyList<String>(), validation.invalidEntries)
        assertEquals(
            listOf(
                "1.1.1.1",
                "8.8.8.8:5353",
                "2001:4860:4860:0:0:0:0:8888",
                "192.168.10.0/30:5300",
            ),
            validation.normalizedResolvers,
        )
    }

    @Test
    fun resolveNormalizesCommaSeparatedResolverTextAndDefaultPorts() {
        val settings = WhiteDnsSettings(
            resolverText = "1.1.1.1:53, 8.8.8.8:53\n9.9.9.9",
        )

        assertEquals(
            listOf("1.1.1.1", "8.8.8.8", "9.9.9.9"),
            settings.resolve().resolverEntries,
        )
    }

    @Test
    fun validateResolverTextRejectsInvalidResolverEntries() {
        val validation = validateResolverText(
            """
            1.1.1.1
            google.com
            999.1.1.1
            8.8.8.8:70000
            10.0.0.0/8
            """.trimIndent(),
        )

        assertEquals(listOf("1.1.1.1"), validation.normalizedResolvers)
        assertEquals(
            listOf("google.com", "999.1.1.1", "8.8.8.8:70000", "10.0.0.0/8"),
            validation.invalidEntries,
        )
    }

    @Test
    fun validateConnectionSettingsBlocksFatalLaunchErrors() {
        val validation = WhiteDnsSettings(
            connectionProfiles = listOf(
                ConnectionProfile(
                    id = ConnectionProfile.DefaultId,
                    name = "Connection",
                    customServerDomain = "bad domain",
                    customServerEncryptionKey = "",
                ),
            ),
            resolverText = "not-a-resolver",
            listenPort = "70000",
        ).let(::validateConnectionSettings)

        assertEquals(false, validation.canConnect)
        assertTrue(validation.fatalIssues.any { it.field == "server" })
        assertTrue(validation.fatalIssues.any { it.field == "resolvers" })
        assertTrue(validation.fatalIssues.any { it.field == "listenPort" })
    }

    @Test
    fun validateConnectionSettingsWarnsForRiskyButUsableSettings() {
        val validation = WhiteDnsSettings(
            connectionProfiles = listOf(
                ConnectionProfile(
                    id = ConnectionProfile.DefaultId,
                    name = "Connection",
                    customServerDomain = "server.example.com",
                    customServerEncryptionKey = "secret",
                ),
            ),
            resolverText = (1..300).joinToString("\n") { "192.0.${it / 250}.${(it % 250) + 1}" },
            rxTxWorkers = "40",
            tunnelProcessWorkers = "40",
        ).let(::validateConnectionSettings)

        assertEquals(true, validation.canConnect)
        assertTrue(validation.warnings.any { it.field == "resolvers" })
        assertTrue(validation.warnings.any { it.field == "workers" })
    }

    private fun decodeStormDnsProfilePayload(link: String): String {
        val payload = link.removePrefix("stormdns://")
        val paddedPayload = payload.padEnd(payload.length + ((4 - payload.length % 4) % 4), '=')
        return Base64.getUrlDecoder().decode(paddedPayload).toString(Charsets.UTF_8)
    }
}
