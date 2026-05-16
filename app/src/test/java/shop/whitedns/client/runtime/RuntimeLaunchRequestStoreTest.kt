package shop.whitedns.client.runtime

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeLaunchRequestStoreTest {

    @Test
    fun staleRequestFilesSelectsOnlyExpiredSafeJsonRequests() {
        val directory = Files.createTempDirectory("whitedns-runtime-launch").toFile()
        try {
            val nowMillis = 2_000_000_000L
            val oldSafe = touchRequest(
                directory = directory,
                name = "session-old.json",
                lastModifiedMillis = nowMillis - RuntimeLaunchRequestStore.DefaultRequestMaxAgeMillis - 1_000L,
            )
            touchRequest(
                directory = directory,
                name = "session-fresh.json",
                lastModifiedMillis = nowMillis,
            )
            touchRequest(
                directory = directory,
                name = "session-old.tmp",
                lastModifiedMillis = nowMillis - RuntimeLaunchRequestStore.DefaultRequestMaxAgeMillis - 1_000L,
            )
            touchRequest(
                directory = directory,
                name = "unsafe id.json",
                lastModifiedMillis = nowMillis - RuntimeLaunchRequestStore.DefaultRequestMaxAgeMillis - 1_000L,
            )

            val staleFiles = RuntimeLaunchRequestStore.staleRequestFiles(
                directory = directory,
                nowMillis = nowMillis,
            )

            assertEquals(listOf(oldSafe.name), staleFiles.map(File::getName))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun pruneStaleRequestFilesDeletesOnlyExpiredSafeJsonRequests() {
        val directory = Files.createTempDirectory("whitedns-runtime-launch").toFile()
        try {
            val nowMillis = 2_000_000_000L
            val oldSafe = touchRequest(
                directory = directory,
                name = "session-old.json",
                lastModifiedMillis = nowMillis - RuntimeLaunchRequestStore.DefaultRequestMaxAgeMillis - 1_000L,
            )
            val freshSafe = touchRequest(
                directory = directory,
                name = "session-fresh.json",
                lastModifiedMillis = nowMillis,
            )
            val unsafeOld = touchRequest(
                directory = directory,
                name = "unsafe id.json",
                lastModifiedMillis = nowMillis - RuntimeLaunchRequestStore.DefaultRequestMaxAgeMillis - 1_000L,
            )

            val deletedCount = RuntimeLaunchRequestStore.pruneStaleRequestFiles(
                directory = directory,
                nowMillis = nowMillis,
            )

            assertEquals(1, deletedCount)
            assertFalse(oldSafe.exists())
            assertTrue(freshSafe.exists())
            assertTrue(unsafeOld.exists())
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun touchRequest(
        directory: File,
        name: String,
        lastModifiedMillis: Long,
    ): File {
        val file = directory.resolve(name)
        file.writeText("{}", Charsets.UTF_8)
        Files.setLastModifiedTime(file.toPath(), FileTime.fromMillis(lastModifiedMillis))
        return file
    }
}
