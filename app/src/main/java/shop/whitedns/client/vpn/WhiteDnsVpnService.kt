package shop.whitedns.client.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import shop.whitedns.client.MainActivity
import shop.whitedns.client.R
import shop.whitedns.client.model.ConnectionProfile
import shop.whitedns.client.model.ResolvedWhiteDnsSettings
import shop.whitedns.client.model.CottenDnsServerProfile
import shop.whitedns.client.model.WhiteDnsOptions
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.WhiteDnsSettingsStore
import shop.whitedns.client.model.resolve
import shop.whitedns.client.model.runtimeConnectionSettings
import shop.whitedns.client.model.selectedConnectionProfile
import shop.whitedns.client.proxy.WhiteDnsProxyService
import shop.whitedns.client.runtime.RuntimeLaunchRequestStore
import shop.whitedns.client.runtime.WhiteDnsRuntimeStateStore
import shop.whitedns.client.runtime.WhiteDnsTrafficWarmup
import shop.whitedns.client.runtime.formatTrafficNotificationText
import shop.whitedns.client.runtime.parseCottenDnsTrafficStatsLine
import shop.whitedns.client.cottendns.CottenDnsProcessManager

class WhiteDnsVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var foregroundStarted = false
    private var startJob: Job? = null
    private var keepaliveJob: Job? = null
    private var networkRestartJob: Job? = null
    private var runtimeReady = false
    private var lastTrafficNotificationUpdateMillis = 0L
    private var currentSessionId = ""
    @Volatile
    private var stopping = false
	@Volatile
	private var activeResolvedSettings: ResolvedWhiteDnsSettings? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val CottenDnsProcessManager by lazy {
        CottenDnsProcessManager(applicationContext)
    }
    private val tun2SocksProcessManager by lazy {
        Tun2SocksProcessManager(applicationContext)
    }
    private val connectivityManager by lazy {
        getSystemService(ConnectivityManager::class.java)
    }
    @Volatile
    private var defaultNetworkSignature = ""
    private val defaultNetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
			scheduleNetworkHandover(network, "available")
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
			// Transport/capability churn is common on unstable mobile links. The
			// native engine already rebalances resolvers and paths in-place, so it
			// must not be killed for these non-identity changes.
			if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return
        }

        override fun onLost(network: Network) {
			if (defaultNetworkSignature == network.toString()) {
				scheduleNetworkHandover(connectivityManager.activeNetwork, "lost")
			}
        }
    }

    override fun onCreate() {
        super.onCreate()
        runCatching {
            connectivityManager.registerDefaultNetworkCallback(defaultNetworkCallback)
        }.onFailure { error ->
            Log.w(Tag, "Unable to monitor default-network changes", error)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ActionStop -> {
				stopping = true
				val activeStart = startJob
				serviceScope.launch {
					activeStart?.cancelAndJoin()
					stopVpn()
					exitForeground()
					stopSelf()
				}
                START_NOT_STICKY
            }
            else -> {
                try {
                    enterForeground("Preparing CottenDns")
                    startVpn(intent)
                    START_REDELIVER_INTENT
                } catch (error: Exception) {
                    logError("Failed to start foreground VPN service", error)
                    stopVpn()
                    exitForeground()
                    stopSelf()
                    START_NOT_STICKY
                }
            }
        }
    }

    override fun onDestroy() {
		stopping = true
        startJob?.cancel()
        networkRestartJob?.cancel()
        runCatching { connectivityManager.unregisterNetworkCallback(defaultNetworkCallback) }
		// Normal disconnects finish teardown in the IO scope before stopSelf.
		// This bounded fallback prevents Android's main thread from waiting up
		// to the full native grace period if the system destroys us directly.
		runCatching { vpnInterface?.close() }
		vpnInterface = null
		Thread {
			runCatching { tun2SocksProcessManager.stop(250, signalNative = true) }
			runCatching { CottenDnsProcessManager.stop() }
		}.apply {
			name = "whitedns-vpn-destroy-cleanup"
			isDaemon = true
			start()
		}
        exitForeground()
        serviceScope.cancel()
        super.onDestroy()
    }

	override fun onRevoke() {
		stopping = true
		val activeStart = startJob
		serviceScope.launch {
			activeStart?.cancelAndJoin()
			stopVpn()
			exitForeground()
			stopSelf()
		}
		super.onRevoke()
	}

    private fun enterForeground(statusText: String) {
        createNotificationChannel()
        val notification = buildForegroundNotification(statusText)
        if (foregroundStarted) {
            updateForegroundNotification(statusText)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NotificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED,
            )
        } else {
            startForeground(NotificationId, notification)
        }
        foregroundStarted = true
    }

    private fun updateForegroundNotification(statusText: String) {
        if (!foregroundStarted) {
            return
        }
        getSystemService(NotificationManager::class.java)
            .notify(NotificationId, buildForegroundNotification(statusText))
    }

    private fun exitForeground() {
        if (!foregroundStarted) {
            return
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        foregroundStarted = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            NotificationChannelId,
            "WhiteDNS VPN",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows the active WhiteDNS VPN connection"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildForegroundNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            pendingIntentFlags,
        )
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, WhiteDnsVpnService::class.java).setAction(ActionStop),
            pendingIntentFlags,
        )

        return NotificationCompat.Builder(this, NotificationChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("WhiteDNS VPN")
            .setContentText(statusText)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_notification, "Disconnect", stopPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .build()
    }

    private fun startVpn(intent: Intent?) {
        val previousJob = startJob
        val sessionId = intent?.getStringExtra(ExtraSessionId).orEmpty()
        startJob = serviceScope.launch {
            previousJob?.cancelAndJoin()
            currentSessionId = sessionId
            stopping = false
            var startedOnce = false
            var restartDelayMillis = RestartInitialDelayMillis
            while (isActive && !stopping) {
                try {
                    val launchRequest = RuntimeLaunchRequestStore.load(applicationContext, sessionId)
                        ?: throw IllegalStateException("Runtime launch request is missing")
                    val settings = launchRequest.settings.runtimeConnectionSettings()
                    val resolvedSettings = settings.resolve()
                    if (resolvedSettings.connectionMode != "vpn") {
                        throw IllegalStateException("VPN mode is not enabled")
                    }
                    if (resolvedSettings.resolverEntries.isEmpty()) {
                        throw IllegalStateException("Resolvers are required to connect")
                    }
                    val serverProfile = launchRequest.serverProfile

                    stopVpn()
                    stopping = false
                    WhiteDnsProxyService.stop(applicationContext)
                    waitForLocalPortToClose(resolvedSettings.listenPort)
                    runtimeReady = false
                    lastTrafficNotificationUpdateMillis = 0L
                    WhiteDnsRuntimeStateStore.markStarting(
                        context = applicationContext,
                        settings = settings,
                        sessionId = sessionId,
                        message = if (startedOnce) "Reconnecting full-device VPN" else "Starting full-device VPN",
                    )
                    logInfo("Using custom CottenDns server")
                    logInfo("Starting internal SOCKS bridge")
                    startCottenDnsAndVpn(sessionId, serverProfile, settings, resolvedSettings)
                    startedOnce = true
                    restartDelayMillis = RestartInitialDelayMillis
                    monitorCottenDnsProcess()
                } catch (error: CancellationException) {
                    stopVpn()
                    throw error
                } catch (error: Exception) {
                    val shouldRetry = startedOnce && isActive && !stopping
                    stopVpn()
                    if (!shouldRetry) {
                        failAndStopVpn("Failed to start WhiteDNS VPN", error)
                        return@launch
                    }
                    stopping = false
                    updateForegroundNotification("VPN reconnecting")
                    logWarning(
                        "CottenDns stopped unexpectedly: ${error.message ?: error::class.java.simpleName}. " +
                            "Restarting in ${restartDelayMillis / 1_000}s",
                    )
                    delay(restartDelayMillis)
                    restartDelayMillis = (restartDelayMillis * 2).coerceAtMost(RestartMaxDelayMillis)
                }
            }
        }
    }

    private suspend fun startCottenDnsAndVpn(
        sessionId: String,
        serverProfile: CottenDnsServerProfile,
        settings: WhiteDnsSettings,
        resolvedSettings: ResolvedWhiteDnsSettings,
    ) {
        val startupFailure = AtomicReference<String?>(null)
        val lastStartupActivityMillis = AtomicLong(System.currentTimeMillis())
        CottenDnsProcessManager.start(serverProfile, settings) { line ->
            lastStartupActivityMillis.set(System.currentTimeMillis())
            logInfo(line)
            detectCottenDnsStartupFailure(line)?.let { failure ->
                startupFailure.compareAndSet(null, failure)
            }
        }
        waitForProxyPort(
            listenPort = resolvedSettings.listenPort,
            startupFailure = { startupFailure.get() },
            lastActivityMillis = { lastStartupActivityMillis.get() },
        )
        logInfo("SOCKS proxy is ready")
        startVpnRouting(sessionId, settings, resolvedSettings)
    }

    private fun scheduleNetworkHandover(network: Network?, reason: String) {
		val signature = network?.toString().orEmpty()
        if (signature == defaultNetworkSignature) {
            return
        }
        defaultNetworkSignature = signature
        if (!runtimeReady || stopping) {
            return
        }

        networkRestartJob?.cancel()
        networkRestartJob = serviceScope.launch {
			delay(NetworkRecoveryGraceMillis)
            if (!runtimeReady || stopping || !isActive) {
                return@launch
            }
			if (defaultNetworkSignature != signature) return@launch
			val settings = activeResolvedSettings ?: return@launch
			val recovered = WhiteDnsTrafficWarmup.runProbe(settings) || run {
				delay(NetworkRecoveryProbeSpacingMillis)
				WhiteDnsTrafficWarmup.runProbe(settings)
			}
			if (recovered) {
				logInfo("Native tunnel recovered after default network $reason without a restart")
				return@launch
			}
			logWarning("Default network $reason did not recover after grace period; restarting native tunnel")
			CottenDnsProcessManager.stop()
        }
    }

    private suspend fun waitForProxyPort(
        listenPort: Int,
        startupFailure: () -> String?,
        lastActivityMillis: () -> Long,
    ) {
        while (true) {
            startupFailure()?.let { failure ->
                throw IllegalStateException("CottenDns startup failed: $failure")
            }
            val now = System.currentTimeMillis()
            if (!CottenDnsProcessManager.isRunning()) {
                val exitCode = CottenDnsProcessManager.exitCodeOrNull()
                throw IllegalStateException(
                    "CottenDns process exited before SOCKS was ready${exitCode?.let { " (exit code $it)" }.orEmpty()}",
                )
            }
            if (canConnectToLocalPort(listenPort)) {
                return
            }
            if (now - lastActivityMillis() >= ProxyStartupIdleTimeoutMillis) {
                throw IllegalStateException(
                    "Timed out waiting for CottenDns SOCKS listener on port $listenPort after startup logs stopped",
                )
            }
            delay(500)
        }
    }

    private suspend fun waitForLocalPortToClose(port: Int) {
        val deadline = System.currentTimeMillis() + PreviousRuntimeStopTimeoutMillis
        while (canConnectToLocalPort(port)) {
            if (System.currentTimeMillis() >= deadline) {
                throw IllegalStateException("Previous local proxy listener is still active on port $port")
            }
            delay(PreviousRuntimeStopPollMillis)
        }
    }

    private fun canConnectToLocalPort(port: Int): Boolean {
        return runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("127.0.0.1", port), 300)
            }
            true
        }.getOrDefault(false)
    }

    private fun detectCottenDnsStartupFailure(line: String): String? {
        val normalized = line.lowercase()
        return when {
            "no valid connections found after mtu testing" in normalized ||
                "mtu tests failed: no valid connections" in normalized ||
                "no valid connections after mtu testing" in normalized ->
                "No DNS resolver passed MTU testing"
            else -> null
        }
    }

    private suspend fun monitorCottenDnsProcess() {
        while (true) {
            if (!CottenDnsProcessManager.isRunning()) {
                val exitCode = CottenDnsProcessManager.exitCodeOrNull()
                throw IllegalStateException(
                    "CottenDns process exited while VPN was active${exitCode?.let { " (exit code $it)" }.orEmpty()}",
                )
            }
            delay(1_000)
        }
    }

    private fun startVpnRouting(
        sessionId: String,
        settings: WhiteDnsSettings,
        resolvedSettings: ResolvedWhiteDnsSettings,
    ) {
        try {
            val socksHost = selectVpnSocksHost(resolvedSettings.listenIp)
            val socksPort = resolvedSettings.listenPort
            val socksUsername = if (resolvedSettings.socks5Authentication) {
                resolvedSettings.socksUsername
            } else {
                null
            }
            val socksPassword = if (resolvedSettings.socks5Authentication) {
                resolvedSettings.socksPassword
            } else {
                null
            }
            logInfo("Preparing Android VPN interface with virtual DNS")
            val tun = Builder()
                .setSession("WhiteDNS")
                .setMtu(VpnMtu)
                .addAddress(TunIpv4Address, TunIpv4PrefixLength)
				// CottenDNS now carries IPv6 TCP targets and generic IPv6 UDP
				// datagrams, so dual-stack traffic can remain native end-to-end.
                .addAddress(TunIpv6Address, TunIpv6PrefixLength)
                .addDnsServer(TunDnsServer)
                .addRoute(TunDnsServer, 32)
                .addRoute("0.0.0.0", 0)
                .addRoute("::", 0)
                .apply {
                    configureSplitTunnelApplications(
                        splitTunnelMode = resolvedSettings.splitTunnelMode,
                        splitTunnelPackages = resolvedSettings.splitTunnelPackages,
                    )
                }
                .establish()
                ?: throw IllegalStateException("Failed to establish WhiteDNS VPN interface")

            vpnInterface = tun
            val tunFd = tun.fd
            logInfo("Routing device traffic to SOCKS $socksHost:$socksPort")
            tun2SocksProcessManager.start(
                tunFileDescriptor = tunFd,
                closeTunFileDescriptorOnDrop = false,
                socksHost = socksHost,
                socksPort = socksPort,
                socksUsername = socksUsername,
                socksPassword = socksPassword,
                onOutput = { line ->
                    logInfo("tun2proxy: $line")
                },
                onExit = { exitCode ->
                    if (stopping) {
                        Log.i(Tag, "tun2proxy stopped with code $exitCode")
                    } else {
                        logWarning("tun2proxy exited with code $exitCode; restarting the VPN runtime")
                        CottenDnsProcessManager.stop()
                    }
                },
            )
			if (!tun2SocksProcessManager.awaitRunning(Tun2proxyStartupTimeoutMillis)) {
				throw IllegalStateException("tun2proxy exited before the VPN route became ready")
			}
			activeResolvedSettings = resolvedSettings
            updateForegroundNotification("Full-device VPN is active")
            runtimeReady = true
            WhiteDnsRuntimeStateStore.markReady(
                context = applicationContext,
                settings = settings,
                sessionId = sessionId,
                message = "Full-device VPN routing started",
            )
            reportReady("Full-device VPN routing started")
            startTrafficKeepalive(resolvedSettings)
        } catch (error: Exception) {
            throw error
        }
    }

    private fun stopVpn() {
        stopping = true
        networkRestartJob?.cancel()
        networkRestartJob = null
        runtimeReady = false
		activeResolvedSettings = null
        lastTrafficNotificationUpdateMillis = 0L
        stopTrafficKeepalive()
		// Signal native shutdown, then close the TUN immediately so any blocked
		// read wakes before we wait for the runner thread.
		runCatching { tun2SocksProcessManager.requestStop() }
		val interfaceToClose = vpnInterface
		vpnInterface = null
		runCatching {
			interfaceToClose?.close()
		}.onFailure { error ->
			Log.w(Tag, "Failed to close VPN interface", error)
		}
        runCatching {
			val stopped = tun2SocksProcessManager.stop(
				gracePeriodMillis = Tun2proxyStopGracePeriodMillis,
				signalNative = true,
            )
            if (!stopped) {
                Log.w(Tag, "tun2proxy did not stop before VPN interface close")
            }
        }.onFailure { error ->
            Log.w(Tag, "Failed to stop tun2proxy", error)
        }
        runCatching {
            CottenDnsProcessManager.stop()
        }.onFailure { error ->
            Log.w(Tag, "Failed to stop CottenDns", error)
        }
        WhiteDnsRuntimeStateStore.markStopped(
            context = applicationContext,
            mode = WhiteDnsRuntimeStateStore.ModeVpn,
            sessionId = currentSessionId,
            message = "VPN service stopped",
        )
    }

    private fun startTrafficKeepalive(resolvedSettings: ResolvedWhiteDnsSettings) {
        stopTrafficKeepalive()
        if (!resolvedSettings.trafficWarmupEnabled) {
            return
        }
        keepaliveJob = serviceScope.launch {
            var successfulWarmupProbes = 0
            repeat(resolvedSettings.trafficWarmupProbeCount) { index ->
                if (!isActive || stopping) {
                    return@launch
                }
                if (WhiteDnsTrafficWarmup.runProbe(resolvedSettings)) {
                    successfulWarmupProbes += 1
                }
                if (index < resolvedSettings.trafficWarmupProbeCount - 1) {
                    delay(TrafficWarmupProbeSpacingMillis)
                }
            }
            if (successfulWarmupProbes > 0) {
                logInfo("Traffic warmup completed")
            }
            while (isActive && !stopping) {
                delay(resolvedSettings.trafficKeepaliveIntervalSeconds * 1_000L)
                WhiteDnsTrafficWarmup.runProbe(resolvedSettings)
            }
        }
    }

    private fun stopTrafficKeepalive() {
        keepaliveJob?.cancel()
        keepaliveJob = null
    }

    private fun selectVpnSocksHost(listenIp: String): String {
        val host = listenIp.trim().removeSurrounding("[", "]")
        return when (host) {
            "", "0.0.0.0" -> "127.0.0.1"
            "::" -> "::1"
            else -> host
        }
    }

    private fun Builder.configureSplitTunnelApplications(
        splitTunnelMode: String,
        splitTunnelPackages: List<String>,
    ) {
        val selectedPackages = splitTunnelPackages
            .asSequence()
            .map(String::trim)
            .filter { it.isNotEmpty() && it != packageName }
            .distinct()
            .toList()

        when (splitTunnelMode) {
            WhiteDnsOptions.SplitTunnelModeInclude -> {
                if (selectedPackages.isEmpty()) {
					throw IllegalStateException("Split tunnel include mode requires at least one selected app")
                }

				selectedPackages.forEach { appPackage ->
					requireAllowedApplication(appPackage)
                }
				logInfo("Split tunnel routes ${selectedPackages.size} selected app(s) through the VPN")
            }
            WhiteDnsOptions.SplitTunnelModeExclude -> {
                excludeWhiteDnsApp()
				selectedPackages.forEach { appPackage ->
					requireDisallowedApplication(appPackage, "Unable to bypass $appPackage")
                }
				logInfo("Split tunnel bypasses ${selectedPackages.size} selected app(s)")
            }
            else -> {
                excludeWhiteDnsApp()
            }
        }
    }

    private fun Builder.excludeWhiteDnsApp() {
        requireDisallowedApplication(packageName, "Unable to exclude WhiteDNS app from VPN")
        logInfo("WhiteDNS app traffic bypasses VPN routing")
    }

	private fun Builder.requireAllowedApplication(appPackage: String) {
		runCatching { addAllowedApplication(appPackage) }.onFailure { error ->
			throw IllegalStateException(
				"Unable to route $appPackage through VPN: ${error.message ?: error::class.java.simpleName}",
				error,
			)
		}
    }

    private fun Builder.requireDisallowedApplication(appPackage: String, message: String) {
        runCatching {
            addDisallowedApplication(appPackage)
        }.onFailure { error ->
            throw IllegalStateException(
                "$message: ${error.message ?: error::class.java.simpleName}",
                error,
            )
        }
    }

    private fun logInfo(message: String) {
        Log.i(Tag, message)
        updateTrafficNotification(message)
        WhiteDnsVpnEvents.log(currentSessionId, message)
        sendVpnEvent(BroadcastTypeLog, message)
    }

    private fun logWarning(message: String) {
        Log.w(Tag, message)
        updateTrafficNotification(message)
        WhiteDnsVpnEvents.log(currentSessionId, message)
        sendVpnEvent(BroadcastTypeLog, message)
    }

    private fun updateTrafficNotification(message: String) {
        if (!runtimeReady) {
            return
        }
        val stats = parseCottenDnsTrafficStatsLine(message) ?: return
        val now = System.currentTimeMillis()
        if (now - lastTrafficNotificationUpdateMillis < TrafficNotificationUpdateIntervalMillis) {
            return
        }
        lastTrafficNotificationUpdateMillis = now
        updateForegroundNotification(formatTrafficNotificationText(stats))
    }

    private fun logError(message: String, error: Throwable) {
        Log.e(Tag, message, error)
        reportFailure("$message: ${error.message ?: error::class.java.simpleName}")
    }

    private fun failAndStopVpn(message: String, error: Throwable? = null) {
        if (error == null) {
            Log.w(Tag, message)
        } else {
            Log.e(Tag, message, error)
        }
        runtimeReady = false
        lastTrafficNotificationUpdateMillis = 0L
        val failureMessage = if (error == null) {
            message
        } else {
            "$message: ${error.message ?: error::class.java.simpleName}"
        }
        WhiteDnsRuntimeStateStore.markFailed(
            context = applicationContext,
            mode = WhiteDnsRuntimeStateStore.ModeVpn,
            sessionId = currentSessionId,
            message = failureMessage,
        )
        updateForegroundNotification("VPN disconnected")
        reportFailure(failureMessage)
        stopVpn()
        exitForeground()
        stopSelf()
    }

    private fun reportFailure(message: String) {
        WhiteDnsVpnEvents.failed(currentSessionId, message)
        sendVpnEvent(BroadcastTypeFailed, message)
    }

    private fun reportReady(message: String) {
        Log.i(Tag, message)
        WhiteDnsVpnEvents.ready(currentSessionId, message)
        sendVpnEvent(BroadcastTypeReady, message)
    }

    private fun sendVpnEvent(type: String, message: String) {
        sendBroadcast(
            Intent(BroadcastAction)
                .setPackage(packageName)
                .putExtra(BroadcastExtraType, type)
                .putExtra(BroadcastExtraSessionId, currentSessionId)
                .putExtra(BroadcastExtraMessage, message),
        )
    }

    companion object {
        private const val Tag = "WhiteDnsVpnService"
        const val BroadcastAction = "shop.whitedns.client.vpn.EVENT"
        const val BroadcastExtraType = "shop.whitedns.client.vpn.extra.TYPE"
        const val BroadcastExtraSessionId = "shop.whitedns.client.vpn.extra.SESSION_ID"
        const val BroadcastExtraMessage = "shop.whitedns.client.vpn.extra.MESSAGE"
        const val BroadcastTypeLog = "log"
        const val BroadcastTypeReady = "ready"
        const val BroadcastTypeFailed = "failed"
        private const val ActionStart = "shop.whitedns.client.vpn.START"
        private const val ActionStop = "shop.whitedns.client.vpn.STOP"
        private const val ExtraSessionId = "shop.whitedns.client.vpn.extra.SESSION_ID"
        const val TunIpv4Address = "172.19.0.1"
        private const val TunIpv4PrefixLength = 30
        private const val TunIpv6Address = "fd00:2:fd00:1:1:1:1:1"
        private const val TunIpv6PrefixLength = 128
        private const val TunDnsServer = "172.19.0.2"
        private const val VpnMtu = 1500
        private const val Tun2proxyStopGracePeriodMillis = 5_000L
        private const val ProxyStartupIdleTimeoutMillis = 120_000L
        private const val PreviousRuntimeStopTimeoutMillis = 3_000L
        private const val PreviousRuntimeStopPollMillis = 100L
        private const val TrafficNotificationUpdateIntervalMillis = 1_000L
        private const val TrafficWarmupProbeSpacingMillis = 300L
		private const val Tun2proxyStartupTimeoutMillis = 1_500L
		private const val NetworkRecoveryGraceMillis = 15_000L
		private const val NetworkRecoveryProbeSpacingMillis = 2_000L
        private const val RestartInitialDelayMillis = 2_000L
        private const val RestartMaxDelayMillis = 30_000L
        private const val NotificationId = 3101
        private const val NotificationChannelId = "whitedns_vpn"

        fun start(
            context: Context,
            sessionId: String,
            serverProfile: CottenDnsServerProfile? = null,
            settings: WhiteDnsSettings? = null,
        ) {
            val launchSettings = settings ?: WhiteDnsSettingsStore(context).load()
            val launchServerProfile = serverProfile
                ?: selectServerProfile(launchSettings)
                ?: throw IllegalStateException("No CottenDns server profile configured")
            RuntimeLaunchRequestStore.save(
                context = context,
                requestId = sessionId,
                serverProfile = launchServerProfile,
                settings = launchSettings,
            )
            val intent = Intent(context, WhiteDnsVpnService::class.java)
                .setAction(ActionStart)
                .putExtra(ExtraSessionId, sessionId)
            ContextCompat.startForegroundService(context, intent)
        }

        private fun selectServerProfile(settings: WhiteDnsSettings): CottenDnsServerProfile? {
            val connectionProfile = settings.selectedConnectionProfile()
            val domain = connectionProfile.customServerDomain
                .trim()
                .trimEnd('.')
            val encryptionKey = connectionProfile.customServerEncryptionKey.trim()
            if (domain.isBlank() || encryptionKey.isBlank()) {
                return null
            }
            return CottenDnsServerProfile(
                id = "custom",
                label = "Custom CottenDns Server",
                domain = domain,
                encryptionKey = encryptionKey,
                encryptionMethod = connectionProfile.customServerEncryptionMethod.coerceIn(0, 5),
                serverType = ConnectionProfile.normalizeServerType(connectionProfile.serverType),
            )
        }

        fun stop(context: Context) {
            runCatching {
                context.startService(
                    Intent(context, WhiteDnsVpnService::class.java)
                        .setAction(ActionStop),
                )
            }.onFailure { error ->
                Log.w(Tag, "Failed to request VPN service stop", error)
                runCatching {
                    context.stopService(Intent(context, WhiteDnsVpnService::class.java))
                }.onFailure { stopError ->
                    Log.w(Tag, "Failed to stop VPN service", stopError)
                }
            }
        }

    }
}
