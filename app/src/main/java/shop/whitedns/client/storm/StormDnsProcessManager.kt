package shop.whitedns.client.storm

import android.content.Context
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.UUID
import kotlin.concurrent.thread
import shop.whitedns.client.model.StormDnsServerProfile
import shop.whitedns.client.model.WhiteDnsSettings

data class StormDnsLaunchSpec(
    val binaryFile: File,
    val workingDirectory: File,
    val configFile: File,
    val resolversFile: File,
)

class StormDnsProcessManager(
    private val context: Context,
    private val binaryInstaller: StormDnsBinaryInstaller = StormDnsBinaryInstaller(context),
) {

    private var process: Process? = null
    private var currentLaunchSpec: StormDnsLaunchSpec? = null

    fun prepareLaunch(
        serverProfile: StormDnsServerProfile,
        settings: WhiteDnsSettings,
    ): StormDnsLaunchSpec {
        val runtimeDir = File(context.noBackupFilesDir, "stormdns/runtime").apply {
            mkdirs()
        }
        cleanupStaleLaunchFiles(runtimeDir)
        val binaryFile = binaryInstaller.installExecutable()
        val launchId = UUID.randomUUID().toString()
        val configFile = File(runtimeDir, ".wd-$launchId.toml")
        val resolversFile = File(runtimeDir, ".wd-$launchId.resolvers")

        configFile.writeText(
            StormDnsConfigRenderer.renderClientToml(
                serverProfile = serverProfile,
                settings = settings,
            ),
        )
        resolversFile.writeText(StormDnsConfigRenderer.renderResolvers(settings))

        return StormDnsLaunchSpec(
            binaryFile = binaryFile,
            workingDirectory = runtimeDir,
            configFile = configFile,
            resolversFile = resolversFile,
        )
    }

    fun start(
        serverProfile: StormDnsServerProfile,
        settings: WhiteDnsSettings,
        onOutput: (String) -> Unit = {},
    ): StormDnsLaunchSpec {
        stop()
        val launchSpec = prepareLaunch(serverProfile, settings)
        currentLaunchSpec = launchSpec
        onOutput("Runtime prepared")
        process = try {
            ProcessBuilder(
                launchSpec.binaryFile.absolutePath,
                "-config",
                launchSpec.configFile.absolutePath,
                "-resolvers",
                launchSpec.resolversFile.absolutePath,
            )
                .directory(launchSpec.workingDirectory)
                .redirectErrorStream(true)
                .start()
                .also { activeProcess ->
                    onOutput("StormDNS process started")
                    drainProcessOutput(activeProcess, onOutput)
                }
        } catch (error: IOException) {
            cleanupLaunchFiles()
            throw error
        }
        return launchSpec
    }

    fun stop(gracePeriodMillis: Long = 1_500) {
        val activeProcess = process ?: return
        activeProcess.destroy()
        try {
            activeProcess.waitFor(gracePeriodMillis, TimeUnit.MILLISECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        if (activeProcess.isAlive) {
            activeProcess.destroyForcibly()
            try {
                activeProcess.waitFor(gracePeriodMillis, TimeUnit.MILLISECONDS)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        process = null
        cleanupLaunchFiles()
    }

    fun cleanupLaunchFiles() {
        val launchSpec = currentLaunchSpec ?: return
        runCatching { launchSpec.configFile.delete() }
        runCatching { launchSpec.resolversFile.delete() }
        currentLaunchSpec = null
    }

    fun isRunning(): Boolean = process?.isAlive == true

    fun exitCodeOrNull(): Int? {
        val activeProcess = process ?: return null
        return if (activeProcess.isAlive) {
            null
        } else {
            val exitCode = activeProcess.exitValue()
            process = null
            cleanupLaunchFiles()
            exitCode
        }
    }

    private fun drainProcessOutput(
        process: Process,
        onOutput: (String) -> Unit,
    ) {
        thread(
            name = "stormdns-output",
            isDaemon = true,
        ) {
            try {
                process.inputStream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            onOutput(line)
                        }
                    }
                }
            } catch (_: IOException) {
                // Destroying the process closes this stream on another thread during normal shutdown.
            }
        }
    }

    companion object {
        private const val StaleLaunchFileMaxAgeMillis = 24L * 60L * 60L * 1_000L
        private val LaunchFileRegex = Regex("""\.wd-[A-Za-z0-9-]+\.(toml|resolvers)""")

        internal fun cleanupStaleLaunchFiles(
            runtimeDir: File,
            nowMillis: Long = System.currentTimeMillis(),
            maxAgeMillis: Long = StaleLaunchFileMaxAgeMillis,
        ) {
            runtimeDir.listFiles()
                ?.asSequence()
                ?.filter { file ->
                    file.isFile &&
                        LaunchFileRegex.matches(file.name) &&
                        nowMillis - file.lastModified() > maxAgeMillis
                }
                ?.forEach { file ->
                    runCatching { file.delete() }
                }
        }
    }
}
