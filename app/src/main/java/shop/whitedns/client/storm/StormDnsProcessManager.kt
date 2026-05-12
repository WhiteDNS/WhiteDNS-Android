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

    private val processLock = Any()
    private var process: Process? = null
    private var currentLaunchSpec: StormDnsLaunchSpec? = null
    private var outputDrainThread: Thread? = null

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
        onOutput("Runtime prepared")
        try {
            val startedProcess = ProcessBuilder(
                launchSpec.binaryFile.absolutePath,
                "-config",
                launchSpec.configFile.absolutePath,
                "-resolvers",
                launchSpec.resolversFile.absolutePath,
            )
                .directory(launchSpec.workingDirectory)
                .redirectErrorStream(true)
                .start()
            val drainThread = drainProcessOutput(startedProcess, onOutput)
            synchronized(processLock) {
                currentLaunchSpec = launchSpec
                process = startedProcess
                outputDrainThread = drainThread
            }
            drainThread.start()
            onOutput("StormDNS process started")
        } catch (error: IOException) {
            cleanupLaunchFiles(launchSpec)
            throw error
        }
        return launchSpec
    }

    fun stop(gracePeriodMillis: Long = 1_500) {
        val activeProcess: Process
        val drainThread: Thread?
        synchronized(processLock) {
            activeProcess = process ?: return
            drainThread = outputDrainThread
        }
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
        synchronized(processLock) {
            if (process === activeProcess) {
                process = null
                outputDrainThread = null
                cleanupLaunchFilesLocked()
            }
        }
        if (drainThread != null && drainThread !== Thread.currentThread()) {
            try {
                drainThread.join(OutputDrainJoinMillis)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    fun cleanupLaunchFiles() {
        synchronized(processLock) {
            cleanupLaunchFilesLocked()
        }
    }

    private fun cleanupLaunchFiles(launchSpec: StormDnsLaunchSpec) {
        runCatching { launchSpec.configFile.delete() }
        runCatching { launchSpec.resolversFile.delete() }
    }

    private fun cleanupLaunchFilesLocked() {
        val launchSpec = currentLaunchSpec ?: return
        cleanupLaunchFiles(launchSpec)
        currentLaunchSpec = null
    }

    fun isRunning(): Boolean {
        return synchronized(processLock) {
            process?.isAlive == true
        }
    }

    fun exitCodeOrNull(): Int? {
        val activeProcess = synchronized(processLock) {
            process
        } ?: return null
        if (activeProcess.isAlive) {
            return null
        }
        return synchronized(processLock) {
            if (process !== activeProcess) {
                return@synchronized null
            }
            val exitCode = activeProcess.exitValue()
            process = null
            outputDrainThread = null
            cleanupLaunchFilesLocked()
            exitCode
        }
    }

    private fun drainProcessOutput(
        process: Process,
        onOutput: (String) -> Unit,
    ): Thread {
        return thread(
            name = "stormdns-output",
            isDaemon = true,
            start = false,
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
            } finally {
                cleanupExitedProcess(process)
            }
        }
    }

    private fun cleanupExitedProcess(finishedProcess: Process) {
        synchronized(processLock) {
            if (process !== finishedProcess || finishedProcess.isAlive) {
                return
            }
            process = null
            outputDrainThread = null
            cleanupLaunchFilesLocked()
        }
    }

    companion object {
        private const val OutputDrainJoinMillis = 500L
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
