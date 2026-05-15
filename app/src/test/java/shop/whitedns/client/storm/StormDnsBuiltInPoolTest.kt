package shop.whitedns.client.storm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StormDnsBuiltInPoolTest {
    @Test
    fun builtInServerProfilesHaveStableUniqueIds() {
        val profiles = StormDnsBuiltInPool.profiles

        assertTrue(profiles.isNotEmpty())
        assertEquals(profiles.size, profiles.map { it.id }.distinct().size)
    }

    @Test
    fun repeatedDonorLabelsAreNumbered() {
        val whiteDnsProfiles = StormDnsBuiltInPool.profiles
            .filter { it.label.startsWith("@WhiteDNS ") }

        assertTrue(whiteDnsProfiles.size > 1)
        assertEquals("@WhiteDNS 01", whiteDnsProfiles.first().label)
        assertTrue(whiteDnsProfiles.any { it.label == "@WhiteDNS 02" })
    }

    @Test
    fun presetLabelsUseDonorNames() {
        val labels = StormDnsBuiltInPool.profiles.map { it.label }.toSet()

        assertTrue(labels.any { it.startsWith("@PersiaTMChannel ") })
        assertTrue(labels.any { it.startsWith("Ali / @link_dakheli_app ") })
        assertTrue(labels.any { it.startsWith("@Masir_Sefid ") })
        assertTrue(labels.any { it.startsWith("@pythash ") })
    }
}
