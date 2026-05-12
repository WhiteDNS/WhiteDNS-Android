package shop.whitedns.client.runtime

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeLaunchRequestStoreTest {
    @Test
    fun cleanupStaleDeletesOnlyOldSafeRequestFiles() {
        val root = Files.createTempDirectory("launch-store").toFile()
        val launchDir = root.resolve("runtime-launch").apply { mkdirs() }

        val oldRequest = launchDir.resolve("session-old.json").apply {
            writeText("{}")
            setLastModified(1_000L)
        }
        val freshRequest = launchDir.resolve("session-fresh.json").apply {
            writeText("{}")
            setLastModified(9_500L)
        }
        val unsafeName = launchDir.resolve("unsafe/name.json")
        val unrelated = launchDir.resolve("notes.txt").apply {
            writeText("keep")
            setLastModified(1_000L)
        }

        RuntimeLaunchRequestStore.cleanupStaleDirectory(
            launchDirectory = launchDir,
            nowMillis = 10_000L,
            maxAgeMillis = 2_000L,
        )

        assertFalse(oldRequest.exists())
        assertTrue(freshRequest.exists())
        assertTrue(unrelated.exists())
        assertFalse(unsafeName.exists())
    }
}
