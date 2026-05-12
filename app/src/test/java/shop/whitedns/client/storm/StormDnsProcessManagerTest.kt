package shop.whitedns.client.storm

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StormDnsProcessManagerTest {
    @Test
    fun cleanupStaleLaunchFilesDeletesOnlyOldGeneratedFiles() {
        val runtimeDir = Files.createTempDirectory("stormdns-runtime").toFile()
        val nowMillis = 2_000_000L
        val oldMillis = 1_000_000L
        val freshMillis = nowMillis - 1_000L
        val oldConfig = runtimeDir.resolve(".wd-old.toml").apply {
            writeText("config")
            setLastModified(oldMillis)
        }
        val oldResolvers = runtimeDir.resolve(".wd-old.resolvers").apply {
            writeText("resolvers")
            setLastModified(oldMillis)
        }
        val freshConfig = runtimeDir.resolve(".wd-fresh.toml").apply {
            writeText("config")
            setLastModified(freshMillis)
        }
        val unrelated = runtimeDir.resolve("client.toml").apply {
            writeText("config")
            setLastModified(oldMillis)
        }

        StormDnsProcessManager.cleanupStaleLaunchFiles(
            runtimeDir = runtimeDir,
            nowMillis = nowMillis,
            maxAgeMillis = 10_000L,
        )

        assertFalse(oldConfig.exists())
        assertFalse(oldResolvers.exists())
        assertTrue(freshConfig.exists())
        assertTrue(unrelated.exists())
    }
}
