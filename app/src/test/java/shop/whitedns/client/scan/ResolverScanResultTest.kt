package shop.whitedns.client.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolverScanResultTest {
    @Test
    fun rankResolverScanResultsPrefersLowLatencySuccessfulResults() {
        val ranked = rankResolverScanResults(
            listOf(
                ResolverScanResult(
                    resolver = "8.8.8.8",
                    status = ResolverScanResultStatus.Valid,
                    latencyMillis = 240,
                    attempts = 2,
                    observedAtMillis = 10L,
                ),
                ResolverScanResult(
                    resolver = "1.1.1.1",
                    status = ResolverScanResultStatus.Valid,
                    latencyMillis = 80,
                    attempts = 1,
                    observedAtMillis = 20L,
                ),
                ResolverScanResult(
                    resolver = "9.9.9.9",
                    status = ResolverScanResultStatus.Rejected,
                    reason = "timeout",
                    observedAtMillis = 30L,
                ),
            ),
        )

        assertEquals(listOf("1.1.1.1", "8.8.8.8"), ranked.map { it.resolver })
        assertTrue(ranked.first().score > ranked.last().score)
    }

    @Test
    fun rankResolverScanResultsKeepsBestObservationPerResolver() {
        val ranked = rankResolverScanResults(
            listOf(
                ResolverScanResult(
                    resolver = "1.1.1.1",
                    status = ResolverScanResultStatus.Valid,
                    latencyMillis = 200,
                    attempts = 2,
                    observedAtMillis = 10L,
                ),
                ResolverScanResult(
                    resolver = "1.1.1.1",
                    status = ResolverScanResultStatus.Valid,
                    latencyMillis = 70,
                    attempts = 1,
                    observedAtMillis = 20L,
                ),
            ),
        )

        assertEquals(1, ranked.size)
        assertEquals(70, ranked.first().latencyMillis)
    }

    @Test
    fun resolverScanResultRoundTripsJson() {
        val result = ResolverScanResult(
            resolver = "1.1.1.1",
            status = ResolverScanResultStatus.Valid,
            sourceName = "Default list",
            serverDomain = "server.example.com",
            latencyMillis = 90,
            attempts = 1,
            observedAtMillis = 42L,
        )

        assertEquals(result, resolverScanResultFromJson(result.toJsonObject()))
    }

    @Test
    fun summarizeResolverScanRecommendationsRewardsRepeatedServerSuccess() {
        val recommendations = summarizeResolverScanRecommendations(
            listOf(
                ResolverScanResult(
                    resolver = "8.8.8.8",
                    status = ResolverScanResultStatus.Valid,
                    serverDomain = "server-a.example.com",
                    latencyMillis = 95,
                    attempts = 1,
                    observedAtMillis = 10L,
                ),
                ResolverScanResult(
                    resolver = "8.8.8.8",
                    status = ResolverScanResultStatus.Valid,
                    serverDomain = "server-b.example.com",
                    latencyMillis = 110,
                    attempts = 1,
                    observedAtMillis = 20L,
                ),
                ResolverScanResult(
                    resolver = "1.1.1.1",
                    status = ResolverScanResultStatus.Valid,
                    serverDomain = "server-a.example.com",
                    latencyMillis = 95,
                    attempts = 1,
                    observedAtMillis = 30L,
                ),
            ),
        )

        assertEquals("8.8.8.8", recommendations.first().resolver)
        assertEquals(2, recommendations.first().observationCount)
        assertEquals(2, recommendations.first().successfulServerCount)
        assertEquals(95, recommendations.first().bestLatencyMillis)
        assertTrue(recommendations.first().score > recommendations.last().score)
    }
}
