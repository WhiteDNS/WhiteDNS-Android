package shop.whitedns.client.vpn

import android.content.Context
import android.util.Log
import com.github.shadowsocks.bg.Tun2proxy

data class Tun2proxySettings(
    val maxSessions: Int = 1_024,
    val tcpTimeoutSeconds: Int = 300,
    val udpTimeoutSeconds: Int = 120,
    val ipv6Enabled: Boolean = true,
)

class Tun2SocksProcessManager(
    context: Context,
    private val binaryInstaller: Tun2SocksBinaryInstaller = Tun2SocksBinaryInstaller(context),
) {

    private val ownerToken = Any()

    fun requireBinary() {
        binaryInstaller.requireLibrary()
    }

    fun start(
        tunFileDescriptor: Int,
        closeTunFileDescriptorOnDrop: Boolean = true,
        socksHost: String,
        socksPort: Int,
        socksUsername: String? = null,
        socksPassword: String? = null,
		settings: Tun2proxySettings = Tun2proxySettings(),
        onOutput: (String) -> Unit = {},
        onExit: (Int) -> Unit = {},
    ) {
        if (!stop(StopBeforeStartGracePeriodMillis, force = true, signalNative = false)) {
            throw IllegalStateException("Previous tun2proxy runner is still stopping")
        }
        binaryInstaller.requireLibrary()
        val proxyUrl = buildSocksProxyUrl(
            host = socksHost,
            port = socksPort,
            username = socksUsername,
            password = socksPassword,
        )
		val cliArgs = buildTun2proxyCliArgs(
			proxyUrl = proxyUrl,
			tunFileDescriptor = tunFileDescriptor,
			closeTunFileDescriptorOnDrop = closeTunFileDescriptorOnDrop,
			settings = settings,
		)
        val activeThread = Thread {
            val exitCode = try {
				synchronized(NativeStateLock) {
					if (runnerThread === Thread.currentThread()) {
						nativeRunEntered = true
					}
				}
                Tun2proxy.run(cliArgs, TunMtu.toChar())
            } catch (error: Throwable) {
                runCatching {
                    onOutput("tun2proxy native runner failed: ${error.message ?: error::class.java.simpleName}")
                }
                NativeRunnerFailureExitCode
            }
            val shouldReportExit = synchronized(NativeStateLock) {
                if (runnerThread === Thread.currentThread()) {
                    runnerThread = null
                    runnerOwnerToken = null
                    stopSignalSentThread = null
					nativeRunEntered = false
                    true
                } else {
                    false
                }
            }
            if (shouldReportExit) {
                runCatching { onExit(exitCode) }
            }
        }.apply {
            name = "tun2proxy-runner"
            isDaemon = true
        }
        synchronized(NativeOperationLock) {
            val existingThread = synchronized(NativeStateLock) { runnerThread }
            if (existingThread?.isAlive == true) {
                throw IllegalStateException("tun2proxy runner is already active")
            }
            synchronized(NativeStateLock) {
                runnerThread = activeThread
                runnerOwnerToken = ownerToken
                stopSignalSentThread = null
				nativeRunEntered = false
            }
            activeThread.start()
        }
		onOutput(
			"tun2proxy native runner starting " +
				"(maxSessions=${settings.maxSessions}, tcpTimeout=${settings.tcpTimeoutSeconds}s, " +
				"udpTimeout=${settings.udpTimeoutSeconds}s, ipv6=${settings.ipv6Enabled})",
		)
    }

	fun isRunning(): Boolean = synchronized(NativeStateLock) {
		runnerOwnerToken === ownerToken && runnerThread?.isAlive == true && nativeRunEntered
	}

	fun awaitRunning(timeoutMillis: Long = 1_500): Boolean {
		val deadline = System.currentTimeMillis() + timeoutMillis.coerceAtLeast(0)
		var stableSince = 0L
		do {
			if (isRunning()) {
				if (stableSince == 0L) stableSince = System.currentTimeMillis()
				if (System.currentTimeMillis() - stableSince >= RunnerStabilityMillis) return true
			} else {
				stableSince = 0L
			}
			try {
				Thread.sleep(RunnerPollMillis)
			} catch (_: InterruptedException) {
				Thread.currentThread().interrupt()
				return false
			}
		} while (System.currentTimeMillis() < deadline)
		return false
	}

	fun requestStop(force: Boolean = false): Boolean {
		return synchronized(NativeOperationLock) operation@{
			val activeThread = synchronized(NativeStateLock) state@{
				if (!force && runnerOwnerToken !== ownerToken) return@state null
				runnerThread
			} ?: return@operation true
			val shouldSignal = synchronized(NativeStateLock) {
				if (!nativeRunEntered || stopSignalSentThread === activeThread) {
					false
				} else {
					stopSignalSentThread = activeThread
					true
				}
			}
			if (!shouldSignal) return@operation true
			runCatching { Tun2proxy.stop() }
				.onFailure { error -> Log.w(Tag, "Failed to request tun2proxy stop", error) }
				.isSuccess
		}
	}

    fun stop(
        gracePeriodMillis: Long = 3_000,
        force: Boolean = false,
        signalNative: Boolean = true,
    ): Boolean {
        return synchronized(NativeOperationLock) {
            stopLocked(gracePeriodMillis, force, signalNative)
        }
    }

    private fun stopLocked(
        gracePeriodMillis: Long,
        force: Boolean,
        signalNative: Boolean,
    ): Boolean {
        val activeThread = synchronized(NativeStateLock) {
            if (!force && runnerOwnerToken !== ownerToken) {
                return true
            }
            runnerThread
        }
        if (activeThread == null) {
            return true
        }
        val shouldSignalNative = signalNative && synchronized(NativeStateLock) {
            if (stopSignalSentThread === activeThread) {
                false
            } else {
                stopSignalSentThread = activeThread
                true
            }
        }
        if (shouldSignalNative) {
            runCatching {
                Tun2proxy.stop()
            }.onFailure { error ->
                Log.w(Tag, "Failed to stop tun2proxy native runner", error)
            }
        }
        try {
            activeThread.join(gracePeriodMillis)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            return false
        }
        val stopped = !activeThread.isAlive
        if (stopped) {
            synchronized(NativeStateLock) {
                if (runnerThread === activeThread) {
                    runnerThread = null
                    runnerOwnerToken = null
                    stopSignalSentThread = null
					nativeRunEntered = false
                }
            }
        } else {
            Log.w(Tag, "tun2proxy native runner did not stop within ${gracePeriodMillis}ms")
        }
        return stopped
    }

    private fun buildSocksProxyUrl(
        host: String,
        port: Int,
        username: String?,
        password: String?,
    ): String {
        val authorityHost = if (host.contains(":") && !host.startsWith("[")) {
            "[$host]"
        } else {
            host
        }
        val userInfo = if (!username.isNullOrEmpty()) {
            "${percentEncode(username)}:${percentEncode(password.orEmpty())}@"
        } else {
            ""
        }
        return "socks5://$userInfo$authorityHost:$port"
    }

    private fun percentEncode(value: String): String {
        val hex = "0123456789ABCDEF"
        return buildString {
            value.toByteArray(Charsets.UTF_8).forEach { byte ->
                val code = byte.toInt() and 0xff
                val isUnreserved =
                    code in 'A'.code..'Z'.code ||
                        code in 'a'.code..'z'.code ||
                        code in '0'.code..'9'.code ||
                        code == '-'.code ||
                        code == '.'.code ||
                        code == '_'.code ||
                        code == '~'.code
                if (isUnreserved) {
                    append(code.toChar())
                } else {
                    append('%')
                    append(hex[code shr 4])
                    append(hex[code and 0x0f])
                }
            }
        }
    }

    private companion object {
        const val Tag = "Tun2SocksProcessManager"
        const val TunMtu = 1500
        const val NativeRunnerFailureExitCode = -1
        const val StopBeforeStartGracePeriodMillis = 5_000L
		const val RunnerStabilityMillis = 100L
		const val RunnerPollMillis = 20L
        val NativeOperationLock = Any()
        val NativeStateLock = Any()

        @Volatile
        var runnerThread: Thread? = null

        @Volatile
        var runnerOwnerToken: Any? = null

        @Volatile
        var stopSignalSentThread: Thread? = null

		@Volatile
		var nativeRunEntered: Boolean = false
    }
}

internal fun buildTun2proxyCliArgs(
	proxyUrl: String,
	tunFileDescriptor: Int,
	closeTunFileDescriptorOnDrop: Boolean,
	settings: Tun2proxySettings,
): String {
	return buildList {
		add("tun2proxy-bin")
		add("--tun-fd")
		add(tunFileDescriptor.toString())
		add("--close-fd-on-drop")
		add(closeTunFileDescriptorOnDrop.toString())
		add("--proxy")
		add(proxyUrl)
		add("--dns")
		add("virtual")
		add("--verbosity")
		add("warn")
		add("--max-sessions")
		add(settings.maxSessions.coerceIn(64, 8_192).toString())
		add("--tcp-timeout")
		add(settings.tcpTimeoutSeconds.coerceIn(30, 3_600).toString())
		add("--udp-timeout")
		add(settings.udpTimeoutSeconds.coerceIn(10, 3_600).toString())
		if (settings.ipv6Enabled) add("--ipv6-enabled")
		add("--exit-on-fatal-error")
	}.joinToString(" ") { tun2proxyShellQuote(it) }
}

private fun tun2proxyShellQuote(value: String): String {
	if (value.all { it.isLetterOrDigit() || it in "-._:/[]%" }) return value
	return "'${value.replace("'", "'\\''")}'"
}
