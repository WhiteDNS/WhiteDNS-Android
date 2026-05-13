package shop.whitedns.client.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.OsConstants
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.net.InetSocketAddress
import java.net.Socket
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
import shop.whitedns.client.model.ResolvedWhiteDnsSettings
import shop.whitedns.client.model.StormDnsServerProfile
import shop.whitedns.client.model.WhiteDnsOptions
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.WhiteDnsSettingsStore
import shop.whitedns.client.model.resolve
import shop.whitedns.client.model.runtimeConnectionSettings
import shop.whitedns.client.model.selectedConnectionProfile
import shop.whitedns.client.proxy.WhiteDnsProxyService
import shop.whitedns.client.runtime.RuntimeLaunchRequestStore
import shop.whitedns.client.runtime.RuntimeReconnectGuard
import shop.whitedns.client.runtime.WhiteDnsRuntimeStateStore
import shop.whitedns.client.runtime.WhiteDnsTrafficWarmup
import shop.whitedns.client.runtime.formatTrafficNotificationText
import shop.whitedns.client.runtime.parseStormDnsTrafficStatsLine
import shop.whitedns.client.storm.StormDnsProcessManager

class WhiteDnsVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var foregroundStarted = false
    private var startJob: Job? = null
    private var keepaliveJob: Job? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var networkRecoveryJob: Job? = null
    private var runtimeReady = false
    private var lastTrafficNotificationUpdateMillis = 0L
    private var currentSessionId = ""
    @Volatile
    private var stopping = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val stormDnsProcessManager by lazy {
        StormDnsProcessManager(applicationContext)
    }
    private val tun2SocksProcessManager by lazy {
        Tun2SocksProcessManager(applicationContext)
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ActionStop -> {
                startJob?.cancel()
                WhiteDnsRuntimeStateStore.markDisconnecting(
                    context = applicationContext,
                    mode = WhiteDnsRuntimeStateStore.ModeVpn,
                    sessionId = currentSessionId,
                    message = "VPN service stopping",
                )
                stopVpn()
                exitForeground()
                stopSelf()
                START_NOT_STICKY
            }
            else -> {
                try {
                    enterForeground("Preparing StormDNS")
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
        startJob?.cancel()
        WhiteDnsRuntimeStateStore.markDisconnecting(
            context = applicationContext,
            mode = WhiteDnsRuntimeStateStore.ModeVpn,
            sessionId = currentSessionId,
            message = "VPN service stopping",
        )
        stopVpn()
        exitForeground()
        serviceScope.cancel()
        super.onDestroy()
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
            var startedOnce = false
            var restartDelayMillis = VpnRestartInitialDelayMillis
            var runtimeStartedAtMillis = 0L
            val reconnectGuard = RuntimeReconnectGuard(
                maxFailures = VpnRestartMaxFailures,
                windowMillis = VpnRestartFailureWindowMillis,
            )
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
                    val runtimeFailure = AtomicReference<String?>(null)

                    stopVpn()
                    WhiteDnsProxyService.stop(applicationContext)
                    waitForLocalPortToClose(resolvedSettings.listenPort)
                    stopping = false
                    runtimeReady = false
                    lastTrafficNotificationUpdateMillis = 0L
                    WhiteDnsRuntimeStateStore.markStarting(
                        context = applicationContext,
                        settings = settings,
                        sessionId = sessionId,
                        message = if (startedOnce) "Recovering full-device VPN" else "Starting full-device VPN",
                    )
                    logInfo("Using custom StormDNS server")
                    logInfo("Starting internal SOCKS bridge")
                    startStormDnsAndVpn(sessionId, serverProfile, settings, resolvedSettings, runtimeFailure)
                    startedOnce = true
                    restartDelayMillis = VpnRestartInitialDelayMillis
                    runtimeStartedAtMillis = System.currentTimeMillis()
                    monitorRuntimeProcesses(runtimeFailure)
                } catch (error: CancellationException) {
                    stopVpn()
                    throw error
                } catch (error: Exception) {
                    stopVpn()
                    runtimeReady = false
                    lastTrafficNotificationUpdateMillis = 0L
                    if (!startedOnce) {
                        failAndStopVpn("Failed to start WhiteDNS VPN", error)
                        return@launch
                    }
                    if (stopping || !isActive) {
                        return@launch
                    }
                    if (runtimeStartedAtMillis > 0L &&
                        System.currentTimeMillis() - runtimeStartedAtMillis >= VpnRestartHealthyResetMillis
                    ) {
                        reconnectGuard.reset()
                    }
                    val reconnectAllowed = reconnectGuard.recordFailure()
                    val failureCount = reconnectGuard.failureCount()
                    if (!reconnectAllowed) {
                        failAndStopVpn("WhiteDNS VPN stopped after repeated runtime failures", error)
                        return@launch
                    }
                    val failureMessage = "WhiteDNS VPN runtime stopped: ${error.message ?: error::class.java.simpleName}"
                    WhiteDnsRuntimeStateStore.markFailed(
                        context = applicationContext,
                        mode = WhiteDnsRuntimeStateStore.ModeVpn,
                        sessionId = sessionId,
                        message = "VPN reconnect attempt $failureCount/$VpnRestartMaxFailures after: " +
                            (error.message ?: error::class.java.simpleName),
                    )
                    updateForegroundNotification("VPN reconnecting")
                    logWarning("$failureMessage. Restarting in ${restartDelayMillis / 1_000}s")
                    delay(restartDelayMillis)
                    restartDelayMillis = (restartDelayMillis * 2).coerceAtMost(VpnRestartMaxDelayMillis)
                }
            }
        }
    }

    private suspend fun startStormDnsAndVpn(
        sessionId: String,
        serverProfile: StormDnsServerProfile,
        settings: WhiteDnsSettings,
        resolvedSettings: ResolvedWhiteDnsSettings,
        runtimeFailure: AtomicReference<String?>,
    ) {
        val startupFailure = AtomicReference<String?>(null)
        stormDnsProcessManager.start(serverProfile, settings) { line ->
            logInfo(line)
            detectStormDnsStartupFailure(line)?.let { failure ->
                startupFailure.compareAndSet(null, failure)
            }
        }
        waitForProxyPort(
            listenPort = resolvedSettings.listenPort,
            startupFailure = { startupFailure.get() },
        )
        logInfo("SOCKS proxy is ready")
        startVpnRouting(sessionId, settings, resolvedSettings, runtimeFailure)
    }

    private suspend fun waitForProxyPort(
        listenPort: Int,
        startupFailure: () -> String?,
    ) {
        while (true) {
            startupFailure()?.let { failure ->
                throw IllegalStateException("StormDNS startup failed: $failure")
            }
            if (!stormDnsProcessManager.isRunning()) {
                val exitCode = stormDnsProcessManager.exitCodeOrNull()
                throw IllegalStateException(
                    "StormDNS process exited before SOCKS was ready${exitCode?.let { " (exit code $it)" }.orEmpty()}",
                )
            }
            if (canConnectToLocalPort(listenPort)) {
                return
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

    private fun detectStormDnsStartupFailure(line: String): String? {
        val normalized = line.lowercase()
        return when {
            "no valid connections found after mtu testing" in normalized ||
                "mtu tests failed: no valid connections" in normalized ||
                "no valid connections after mtu testing" in normalized ->
                "No DNS resolver passed MTU testing"
            else -> null
        }
    }

    private suspend fun monitorRuntimeProcesses(runtimeFailure: AtomicReference<String?>) {
        while (true) {
            runtimeFailure.get()?.let { failure ->
                throw IllegalStateException(failure)
            }
            if (!stormDnsProcessManager.isRunning()) {
                val exitCode = stormDnsProcessManager.exitCodeOrNull()
                throw IllegalStateException(
                    "StormDNS process exited while VPN was active${exitCode?.let { " (exit code $it)" }.orEmpty()}",
                )
            }
            delay(1_000)
        }
    }

    private fun startVpnRouting(
        sessionId: String,
        settings: WhiteDnsSettings,
        resolvedSettings: ResolvedWhiteDnsSettings,
        runtimeFailure: AtomicReference<String?>,
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
                .setMtu(resolvedSettings.vpnMtu)
                .addAddress(TunIpv4Address, TunIpv4PrefixLength)
                .addDnsServer(TunDnsServer)
                .addRoute(TunDnsServer, 32)
                .addRoute("0.0.0.0", 0)
                .apply {
                    configureIpv6Strategy(resolvedSettings.vpnIpv6Strategy)
                    configureSplitTunnelApplications(
                        splitTunnelMode = resolvedSettings.splitTunnelMode,
                        splitTunnelPackages = resolvedSettings.splitTunnelPackages,
                    )
                }
                .establish()
                ?: throw IllegalStateException("Failed to establish WhiteDNS VPN interface")

            vpnInterface = tun
            clearCloseOnExec(tun)
            val tunFd = tun.fd
            logInfo("Routing device traffic to SOCKS $socksHost:$socksPort")
            logInfo(
                "VPN interface ready with MTU ${resolvedSettings.vpnMtu} and " +
                    WhiteDnsOptions.vpnIpv6StrategyLabel(resolvedSettings.vpnIpv6Strategy),
            )
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
                        runtimeFailure.compareAndSet(null, "tun2proxy exited with code $exitCode")
                    }
                },
            )
            updateForegroundNotification("Full-device VPN is active")
            runtimeReady = true
            registerNetworkChangeCallback(sessionId, runtimeFailure)
            WhiteDnsRuntimeStateStore.markReady(
                context = applicationContext,
                settings = settings,
                sessionId = sessionId,
                message = "Full-device VPN routing started",
            )
            reportReady("Full-device VPN routing started")
            startTrafficKeepalive(resolvedSettings)
        } catch (error: Exception) {
            failAndStopVpn("Failed to start WhiteDNS VPN", error)
        }
    }

    private fun stopVpn() {
        stopping = true
        runtimeReady = false
        lastTrafficNotificationUpdateMillis = 0L
        stopTrafficKeepalive()
        unregisterNetworkChangeCallback()
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
        val interfaceToClose = vpnInterface
        vpnInterface = null
        runCatching {
            interfaceToClose?.close()
        }.onFailure { error ->
            Log.w(Tag, "Failed to close VPN interface", error)
        }
        runCatching {
            stormDnsProcessManager.stop()
        }.onFailure { error ->
            Log.w(Tag, "Failed to stop StormDNS", error)
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
                    excludeWhiteDnsApp()
                    logWarning("No split tunnel apps selected; using full-device VPN routing")
                    return
                }

                val allowedCount = selectedPackages.count { appPackage ->
                    tryAddAllowedApplication(appPackage)
                }
                if (allowedCount == 0) {
                    throw IllegalStateException("No selected split tunnel apps could be routed through the VPN")
                }
                logInfo("Split tunnel routes $allowedCount selected app(s) through the VPN")
            }
            WhiteDnsOptions.SplitTunnelModeExclude -> {
                excludeWhiteDnsApp()
                val excludedCount = selectedPackages.count { appPackage ->
                    tryAddDisallowedApplication(appPackage, "Unable to bypass $appPackage")
                }
                logInfo("Split tunnel bypasses $excludedCount selected app(s)")
            }
            else -> {
                excludeWhiteDnsApp()
            }
        }
    }

    private fun Builder.configureIpv6Strategy(ipv6Strategy: String) {
        when (ipv6Strategy) {
            WhiteDnsOptions.VpnIpv6StrategyBypass -> {
                logWarning("IPv6 bypass is enabled; IPv6 traffic will not be routed through WhiteDNS")
            }
            WhiteDnsOptions.VpnIpv6StrategyExperimentalRoute -> {
                addAddress(TunIpv6Address, TunIpv6PrefixLength)
                addRoute("::", 0)
                logWarning("Experimental IPv6 routing is enabled")
            }
            else -> {
                addAddress(TunIpv6Address, TunIpv6PrefixLength)
                addRoute("::", 0)
                logInfo("IPv6 traffic is routed into WhiteDNS and blocked unless supported by the VPN bridge")
            }
        }
    }

    private fun registerNetworkChangeCallback(
        sessionId: String,
        runtimeFailure: AtomicReference<String?>,
    ) {
        unregisterNetworkChangeCallback()
        val connectivityManager = getSystemService(ConnectivityManager::class.java) ?: return
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val ignoreEventsBeforeMillis = System.currentTimeMillis() + NetworkChangeInitialQuietMillis
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (System.currentTimeMillis() < ignoreEventsBeforeMillis) return
                scheduleNetworkRecovery(sessionId, runtimeFailure, "Network became available")
            }

            override fun onLost(network: Network) {
                if (System.currentTimeMillis() < ignoreEventsBeforeMillis) return
                scheduleNetworkRecovery(sessionId, runtimeFailure, "Network changed")
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                if (System.currentTimeMillis() < ignoreEventsBeforeMillis) return
                scheduleNetworkRecovery(sessionId, runtimeFailure, "Network capabilities changed")
            }
        }
        runCatching {
            connectivityManager.registerNetworkCallback(request, callback)
            networkCallback = callback
            logInfo("Watching network changes for same-session VPN recovery")
        }.onFailure { error ->
            logWarning("Unable to watch network changes: ${error.message ?: error::class.java.simpleName}")
        }
    }

    private fun scheduleNetworkRecovery(
        sessionId: String,
        runtimeFailure: AtomicReference<String?>,
        reason: String,
    ) {
        if (sessionId != currentSessionId || stopping) {
            return
        }
        networkRecoveryJob?.cancel()
        networkRecoveryJob = serviceScope.launch {
            delay(NetworkChangeDebounceMillis)
            if (sessionId == currentSessionId && runtimeReady && !stopping) {
                runtimeFailure.compareAndSet(null, "$reason; recovering VPN session")
            }
        }
    }

    private fun unregisterNetworkChangeCallback() {
        networkRecoveryJob?.cancel()
        networkRecoveryJob = null
        val callback = networkCallback ?: return
        networkCallback = null
        runCatching {
            getSystemService(ConnectivityManager::class.java)?.unregisterNetworkCallback(callback)
        }
    }

    private fun Builder.excludeWhiteDnsApp() {
        tryAddDisallowedApplication(packageName, "Unable to exclude WhiteDNS app from VPN")
    }

    private fun Builder.tryAddAllowedApplication(appPackage: String): Boolean {
        return runCatching {
            addAllowedApplication(appPackage)
            true
        }.getOrElse { error ->
            logWarning("Unable to route $appPackage through VPN: ${error.message ?: error::class.java.simpleName}")
            false
        }
    }

    private fun Builder.tryAddDisallowedApplication(appPackage: String, message: String): Boolean {
        return runCatching {
            addDisallowedApplication(appPackage)
            true
        }.getOrElse { error ->
            logWarning("$message: ${error.message ?: error::class.java.simpleName}")
            false
        }
    }

    @SuppressLint("NewApi")
    private fun clearCloseOnExec(tun: ParcelFileDescriptor) {
        val flags = Os.fcntlInt(tun.fileDescriptor, OsConstants.F_GETFD, 0)
        Os.fcntlInt(
            tun.fileDescriptor,
            OsConstants.F_SETFD,
            flags and OsConstants.FD_CLOEXEC.inv(),
        )
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
        val stats = parseStormDnsTrafficStatsLine(message) ?: return
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
        private const val TunIpv6Address = "fd00:7764:6e73::1"
        private const val TunIpv6PrefixLength = 126
        private const val TunDnsServer = "172.19.0.2"
        private const val VpnRestartInitialDelayMillis = 2_000L
        private const val VpnRestartMaxDelayMillis = 30_000L
        private const val VpnRestartMaxFailures = 3
        private const val VpnRestartFailureWindowMillis = 2 * 60 * 1_000L
        private const val VpnRestartHealthyResetMillis = 5 * 60 * 1_000L
        private const val Tun2proxyStopGracePeriodMillis = 5_000L
        private const val PreviousRuntimeStopTimeoutMillis = 3_000L
        private const val PreviousRuntimeStopPollMillis = 100L
        private const val TrafficNotificationUpdateIntervalMillis = 1_000L
        private const val TrafficWarmupProbeSpacingMillis = 300L
        private const val NetworkChangeInitialQuietMillis = 2_000L
        private const val NetworkChangeDebounceMillis = 1_500L
        private const val NotificationId = 3101
        private const val NotificationChannelId = "whitedns_vpn"

        fun start(
            context: Context,
            sessionId: String,
            serverProfile: StormDnsServerProfile? = null,
            settings: WhiteDnsSettings? = null,
        ) {
            val launchSettings = settings ?: WhiteDnsSettingsStore(context).load()
            val launchServerProfile = serverProfile
                ?: selectServerProfile(launchSettings)
                ?: throw IllegalStateException("No StormDNS server profile configured")
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

        private fun selectServerProfile(settings: WhiteDnsSettings): StormDnsServerProfile? {
            val connectionProfile = settings.selectedConnectionProfile()
            val domain = connectionProfile.customServerDomain
                .trim()
                .trimEnd('.')
            val encryptionKey = connectionProfile.customServerEncryptionKey.trim()
            if (domain.isBlank() || encryptionKey.isBlank()) {
                return null
            }
            return StormDnsServerProfile(
                id = "custom",
                label = "Custom StormDNS Server",
                domain = domain,
                encryptionKey = encryptionKey,
                encryptionMethod = connectionProfile.customServerEncryptionMethod.coerceIn(0, 5),
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
