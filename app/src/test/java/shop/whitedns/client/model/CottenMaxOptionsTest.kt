package shop.whitedns.client.model

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import shop.whitedns.client.storm.StormDnsConfigRenderer

class CottenMaxOptionsTest {
    @Test
    fun trafficProfileDefaultsToFullAndNormalizesStoredValues() {
        for (json in listOf(null, JSONObject(), JSONObject().put("trafficProfile", "unknown"))) {
            assertEquals("full", CottenMaxOptions.fromJson(json).trafficProfile)
            assertEquals(35, CottenMaxOptions.fromJson(json).lowReductionPercent)
        }
        assertEquals("low", CottenMaxOptions.fromJson(JSONObject().put("trafficProfile", " LOW ")).trafficProfile)
        assertEquals(10, CottenMaxOptions(lowReductionPercent = 0).normalized().lowReductionPercent)
        assertEquals(90, CottenMaxOptions(lowReductionPercent = 100).normalized().lowReductionPercent)
    }

    @Test
    fun trafficProfileSurvivesJsonProfileLinksAndToml() {
        for (mode in listOf("full", "low")) {
            val options = CottenMaxOptions(trafficProfile = mode, lowReductionPercent = 65, fecDownloadEnabled = true)
            assertEquals(options, CottenMaxOptions.fromJson(options.toJson()))
            val profile = maxProfile(options)
            val settings = settings(profile)
            val linked = WhiteDnsSettings().importStormDnsProfileLink(settings.exportStormDnsProfileLink(profile))
            assertEquals(options, linked.selectedConnectionProfile().cottenSettings.cottenMaxOptions)
            val toml = StormDnsConfigRenderer.renderClientToml(profile, settings)
            org.junit.Assert.assertTrue(toml.contains("COTTENMAX_TRAFFIC_PROFILE = \"$mode\""))
            org.junit.Assert.assertTrue(toml.contains("COTTENMAX_LOW_REDUCTION_PERCENT = 65"))
            val imported = settings.importAdvancedSettingsProfileFromToml("Max traffic", toml)
            assertEquals(options, imported.selectedConnectionProfile().cottenSettings.cottenMaxOptions)
        }
    }

    @Test
    fun normalModeDoesNotEmitMaxTrafficProfile() {
        val profile = maxProfile(CottenMaxOptions(trafficProfile = "low")).let {
            it.copy(cottenSettings = it.cottenSettings.copy(tunnelMode = "legacy"))
        }
        assertFalse(StormDnsConfigRenderer.renderClientToml(profile, settings(profile)).contains("COTTENMAX_TRAFFIC_PROFILE"))
    }

    @Test
    fun tomlRejectsUnknownTrafficProfileRatherThanSilentlyRemovingTheLimit() {
        val settings = settings(maxProfile(CottenMaxOptions(trafficProfile = "low")))
        assertThrows(IllegalArgumentException::class.java) {
            settings.importAdvancedSettingsProfileFromToml("Invalid Max", "COTTENMAX_TRAFFIC_PROFILE = \"turbo\"")
        }
        val imported = settings.importAdvancedSettingsProfileFromToml("Full Max", "COTTENMAX_TRAFFIC_PROFILE = \"FULL\"")
        assertEquals("full", imported.selectedConnectionProfile().cottenSettings.cottenMaxOptions.trafficProfile)
        for (value in listOf(9, 91)) {
            assertThrows(IllegalArgumentException::class.java) {
                settings.importAdvancedSettingsProfileFromToml("Invalid target", "COTTENMAX_LOW_REDUCTION_PERCENT = $value")
            }
        }
    }

    @Test
    fun panelDomainsAndStandbysFitInitialHandshakeWithoutChangingSavedOptions() {
        val standby = "cot.abcdefghijklmnopqrstuvwxyz.backup.example.org"
        val options = CottenMaxOptions(standbyDomains = standby, maxQName = 101, labelCountJitter = true)
        val profile = maxProfile(options).copy(
            customServerDomain = "cot.abcdefghijklmnopqrstuvwxyz.soar.example.org",
            cottenSettings = maxProfile(options).cottenSettings.copy(qnameMode = "aggressive"),
        )
        val toml = StormDnsConfigRenderer.renderClientToml(profile, settings(profile))
        val required = "cottenmax-v1.$standby".length + 1 + 55 + 1 + 3
        org.junit.Assert.assertTrue(toml.contains("COTTENMAX_SAFE_QNAME = $required"))
        org.junit.Assert.assertTrue(toml.contains("COTTENMAX_MAX_QNAME = $required"))
        assertEquals(101, profile.cottenSettings.cottenMaxOptions.safeQName)
    }

    @Test
    fun olderProfilesKeepFecOffAndExistingBudgets() {
        for (json in listOf(null, JSONObject())) {
            val options = CottenMaxOptions.fromJson(json)
            assertFalse(options.fecDownloadEnabled)
            assertEquals(4, options.fecDataShards)
            assertEquals(1, options.fecParityShards)
            assertEquals(16, options.discoveryWant)
            assertEquals(256, options.maxInflight)
        }
    }

    @Test
    fun fecAndLargeBudgetsSurviveJsonAndProfileLinks() {
        val options = CottenMaxOptions(
            fecDownloadEnabled = true,
            fecDataShards = 16,
            fecParityShards = 4,
            discoveryEnabled = true,
            discoveryWant = 4096,
            maxInflight = 4096,
        )
        assertEquals(options, CottenMaxOptions.fromJson(options.toJson()))
        val profile = maxProfile(options)
        val settings = settings(profile)
        val imported = WhiteDnsSettings().importStormDnsProfileLink(settings.exportStormDnsProfileLink(profile))
        assertEquals(options, imported.selectedConnectionProfile().cottenSettings.cottenMaxOptions)
    }

    @Test
    fun invalidStoredValuesClampWithoutExpandingOtherLimits() {
        val options = CottenMaxOptions(
            fecDataShards = 1,
            fecParityShards = 99,
            discoveryWant = 9999,
            maxInflight = 9999,
            maxPathWindow = 9999,
            discoveryConcurrency = 9999,
        ).normalized()
        assertEquals(2, options.fecDataShards)
        assertEquals(4, options.fecParityShards)
        assertEquals(4096, options.discoveryWant)
        assertEquals(4096, options.maxInflight)
        assertEquals(1024, options.maxPathWindow)
        assertEquals(64, options.discoveryConcurrency)
        val lower = CottenMaxOptions(fecDataShards = 99, fecParityShards = 0, discoveryWant = 0, maxInflight = 0).normalized()
        assertEquals(16, lower.fecDataShards)
        assertEquals(1, lower.fecParityShards)
        assertEquals(1, lower.discoveryWant)
        assertEquals(1, lower.maxInflight)
    }

    @Test
    fun fecAndLargeBudgetsSurviveTomlExportImport() {
        val options = CottenMaxOptions(
            fecDownloadEnabled = true,
            fecDataShards = 8,
            fecParityShards = 2,
            discoveryEnabled = true,
            discoveryWant = 4096,
            maxInflight = 4096,
        )
        val profile = maxProfile(options)
        val settings = settings(profile)
        val toml = StormDnsConfigRenderer.renderClientToml(profile, settings)
        val imported = settings.importAdvancedSettingsProfileFromToml("FEC Max", toml)
        assertEquals(options, imported.selectedConnectionProfile().cottenSettings.cottenMaxOptions)
    }

    @Test
    fun tomlRejectsUnsupportedFecValuesAndOversizedBudgets() {
        val settings = settings(maxProfile(CottenMaxOptions()))
        for (assignment in listOf(
            "COTTENMAX_FEC_DOWNLOAD_ENABLED = 1",
            "COTTENMAX_FEC_DATA_SHARDS = 1",
            "COTTENMAX_FEC_DATA_SHARDS = 17",
            "COTTENMAX_FEC_PARITY_SHARDS = 0",
            "COTTENMAX_FEC_PARITY_SHARDS = 5",
            "RESOLVER_DISCOVERY_WANT = 4097",
            "COTTENMAX_MAX_INFLIGHT = 4097",
        )) {
            assertThrows(assignment, IllegalArgumentException::class.java) {
                settings.importAdvancedSettingsProfileFromToml("Invalid Max", assignment)
            }
        }
    }

    private fun maxProfile(options: CottenMaxOptions) = ConnectionProfile(
        id = "max",
        name = "Max",
        customServerDomain = "a.example",
        customServerEncryptionKey = "test-key",
        engine = DnsClientEngine.CottenDns,
        cottenSettings = CottenDnsProfileSettings(tunnelMode = "cottenmax", cottenMaxOptions = options),
    )

    private fun settings(profile: ConnectionProfile) = WhiteDnsSettings(
        selectedConnectionProfileId = profile.id,
        connectionProfiles = listOf(profile),
    )
}
