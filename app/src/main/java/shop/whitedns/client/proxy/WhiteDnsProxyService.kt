package shop.whitedns.client.proxy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicReference
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
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.WhiteDnsSettingsStore
import shop.whitedns.client.model.resolve
import shop.whitedns.client.model.runtimeConnectionSettings
import shop.whitedns.client.model.selectedConnectionProfile
import shop.whitedns.client.runtime.WhiteDnsRuntimeStateStore
import shop.whitedns.client.runtime.WhiteDnsTrafficWarmup
import shop.whitedns.client.runtime.formatTrafficNotificationText
import shop.whitedns.client.runtime.parseStormDnsTrafficStatsLine
import shop.whitedns.client.storm.StormDnsProcessManager
import shop.whitedns.client.vpn.WhiteDnsVpnService

class WhiteDnsProxyService : Service() {

    private var foregroundStarted = false
    private var startJob: Job? = null
    private var keepaliveJob: Job? = null
    private var runtimeReady = false
    private var lastTrafficNotificationUpdateMillis = 0L
    private var currentSessionId = ""
    @Volatile
    private var stopping = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val settingsStore by lazy {
        WhiteDnsSettingsStore(applicationContext)
    }
    private val stormDnsProcessManager by lazy {
        StormDnsProcessManager(applicationContext)
    }
    private val httpProxyBridge by lazy {
        HttpProxyBridge()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ActionStop -> {
                stopping = true
                startJob?.cancel()
                stopProxyRuntime()
                runtimeReady = false
                lastTrafficNotificationUpdateMillis = 0L
                WhiteDnsRuntimeStateStore.markStopped(
                    context = applicationContext,
                    mode = WhiteDnsRuntimeStateStore.ModeProxy,
                    sessionId = currentSessionId,
                    message = "Proxy service stopped",
                )
                exitForeground()
                stopSelf()
                START_NOT_STICKY
            }
            else -> {
                try {
                    enterForeground("Starting local proxy")
                    startProxy(intent)
                    START_STICKY
                } catch (error: Exception) {
                    logError("Failed to start proxy service", error)
                    stopProxyRuntime()
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
        stopProxyRuntime()
        runtimeReady = false
        lastTrafficNotificationUpdateMillis = 0L
        WhiteDnsRuntimeStateStore.markStopped(
            context = applicationContext,
            mode = WhiteDnsRuntimeStateStore.ModeProxy,
            sessionId = currentSessionId,
            message = "Proxy service stopped",
        )
        exitForeground()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startProxy(intent: Intent?) {
        val previousJob = startJob
        val sessionId = intent?.getStringExtra(ExtraSessionId).orEmpty()
        val requestedServerProfile = intent?.serverProfileExtra()
        val requestedSettings = intent?.settingsExtra()?.runtimeConnectionSettings()
        startJob = serviceScope.launch {
            previousJob?.cancelAndJoin()
            currentSessionId = sessionId
            stopping = false
            var startedOnce = false
            var restartDelayMillis = RestartInitialDelayMillis
            while (isActive && !stopping) {
                try {
                    val settings = requestedSettings ?: settingsStore.load().runtimeConnectionSettings()
                    val resolvedSettings = settings.resolve()
                    if (resolvedSettings.connectionMode != "proxy") {
                        throw IllegalStateException("Proxy mode is not enabled")
                    }
                    if (resolvedSettings.resolverEntries.isEmpty()) {
                        throw IllegalStateException("Resolvers are required to connect")
                    }
                    val serverProfile = requestedServerProfile
                        ?: selectServerProfile(settings)
                        ?: throw IllegalStateException("No StormDNS server profile configured")

                    stopProxyRuntime()
                    WhiteDnsVpnService.stop(applicationContext)
                    waitForLocalPortToClose(resolvedSettings.listenPort)
                    runtimeReady = false
                    lastTrafficNotificationUpdateMillis = 0L
                    WhiteDnsRuntimeStateStore.markStarting(
                        context = applicationContext,
                        settings = settings,
                        sessionId = sessionId,
                        message = "Starting local proxy",
                    )
                    logInfo("Using custom StormDNS server")
                    logInfo("Starting SOCKS listener on ${resolvedSettings.listenIp}:${resolvedSettings.listenPort}")
                    if (resolvedSettings.localDnsEnabled) {
                        logInfo("Starting tunneled DNS listener on 127.0.0.1:${resolvedSettings.localDnsPort}")
                    }
                    startStormDns(sessionId, serverProfile, settings, resolvedSettings)
                    startedOnce = true
                    restartDelayMillis = RestartInitialDelayMillis
                    runtimeReady = true
                    startTrafficKeepalive(resolvedSettings)
                    updateForegroundNotification("Local proxy is active")
                    monitorStormDnsProcess()
                } catch (error: CancellationException) {
                    stopProxyRuntime()
                    throw error
                } catch (error: Exception) {
                    stopProxyRuntime()
                    runtimeReady = false
                    lastTrafficNotificationUpdateMillis = 0L
                    updateForegroundNotification("Local proxy reconnecting")
                    val failureMessage = "Failed to start WhiteDNS proxy: ${error.message ?: error::class.java.simpleName}"
                    WhiteDnsRuntimeStateStore.markFailed(
                        context = applicationContext,
                        mode = WhiteDnsRuntimeStateStore.ModeProxy,
                        sessionId = sessionId,
                        message = failureMessage,
                    )
                    if (!startedOnce) {
                        logError("Failed to start WhiteDNS proxy", error)
                        exitForeground()
                        stopSelf()
                        return@launch
                    }
                    if (stopping || !isActive) {
                        return@launch
                    }
                    logWarning(
                        "StormDNS stopped unexpectedly: ${error.message ?: error::class.java.simpleName}. " +
                            "Restarting in ${restartDelayMillis / 1_000}s",
                    )
                    delay(restartDelayMillis)
                    restartDelayMillis = (restartDelayMillis * 2).coerceAtMost(RestartMaxDelayMillis)
                }
            }
        }
    }

    private suspend fun startStormDns(
        sessionId: String,
        serverProfile: StormDnsServerProfile,
        settings: WhiteDnsSettings,
        resolvedSettings: ResolvedWhiteDnsSettings,
    ) {
        val startupFailure = AtomicReference<String?>(null)
        try {
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
            startHttpProxyBridge(resolvedSettings)
            WhiteDnsRuntimeStateStore.markReady(
                context = applicationContext,
                settings = settings,
                sessionId = sessionId,
                message = "SOCKS proxy is ready",
            )
            reportReady("SOCKS proxy is ready")
        } finally {
            stormDnsProcessManager.cleanupLaunchFiles()
        }
    }

    private fun startHttpProxyBridge(resolvedSettings: ResolvedWhiteDnsSettings) {
        if (!resolvedSettings.httpProxyEnabled) {
            httpProxyBridge.stop()
            return
        }
        runCatching {
            httpProxyBridge.start(
                listenHost = resolvedSettings.listenIp,
                listenPort = resolvedSettings.httpProxyPort,
                socksHost = selectLocalSocksHost(resolvedSettings.listenIp),
                socksPort = resolvedSettings.listenPort,
                socksUsername = if (resolvedSettings.socks5Authentication) resolvedSettings.socksUsername else null,
                socksPassword = if (resolvedSettings.socks5Authentication) resolvedSettings.socksPassword else null,
                onOutput = ::logInfo,
            )
        }.onFailure { error ->
            logWarning("HTTP proxy bridge was not started: ${error.message ?: error::class.java.simpleName}")
        }
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

    private suspend fun monitorStormDnsProcess() {
        while (true) {
            if (!stormDnsProcessManager.isRunning()) {
                val exitCode = stormDnsProcessManager.exitCodeOrNull()
                throw IllegalStateException(
                    "StormDNS process exited${exitCode?.let { " (exit code $it)" }.orEmpty()}",
                )
            }
            delay(1_000)
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

    private fun stopProxyRuntime() {
        stopTrafficKeepalive()
        httpProxyBridge.stop()
        runCatching {
            stormDnsProcessManager.stop()
        }.onFailure { error ->
            Log.w(Tag, "Failed to stop StormDNS", error)
        }
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

    private fun selectLocalSocksHost(listenIp: String): String {
        return when (listenIp.trim().removeSurrounding("[", "]")) {
            "", "0.0.0.0" -> "127.0.0.1"
            "::" -> "::1"
            else -> listenIp.trim().removeSurrounding("[", "]")
        }
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
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
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
            "WhiteDNS Proxy",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows the active WhiteDNS proxy connection"
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
            Intent(this, WhiteDnsProxyService::class.java).setAction(ActionStop),
            pendingIntentFlags,
        )

        return NotificationCompat.Builder(this, NotificationChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("WhiteDNS Proxy")
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

    private fun Intent.serverProfileExtra(): StormDnsServerProfile? {
        val domain = getStringExtra(ExtraServerDomain)
            ?.trim()
            ?.trimEnd('.')
            ?.takeIf(String::isNotBlank)
            ?: return null
        val encryptionKey = getStringExtra(ExtraServerEncryptionKey)
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: return null
        return StormDnsServerProfile(
            id = getStringExtra(ExtraServerId)?.takeIf(String::isNotBlank) ?: "custom",
            label = getStringExtra(ExtraServerLabel)?.takeIf(String::isNotBlank) ?: "StormDNS Server",
            domain = domain,
            encryptionKey = encryptionKey,
            encryptionMethod = getIntExtra(ExtraServerEncryptionMethod, 1).coerceIn(0, 5),
        )
    }

    private fun Intent.settingsExtra(): WhiteDnsSettings? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializableExtra(ExtraSettings, WhiteDnsSettings::class.java)
        } else {
            @Suppress("DEPRECATION")
            getSerializableExtra(ExtraSettings) as? WhiteDnsSettings
        }
    }

    private fun logInfo(message: String) {
        Log.i(Tag, message)
        updateTrafficNotification(message)
        WhiteDnsProxyEvents.log(currentSessionId, message)
        sendProxyEvent(BroadcastTypeLog, message)
    }

    private fun logWarning(message: String) {
        Log.w(Tag, message)
        updateTrafficNotification(message)
        WhiteDnsProxyEvents.log(currentSessionId, message)
        sendProxyEvent(BroadcastTypeLog, message)
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

    private fun reportFailure(message: String) {
        WhiteDnsProxyEvents.failed(currentSessionId, message)
        sendProxyEvent(BroadcastTypeFailed, message)
    }

    private fun reportReady(message: String) {
        Log.i(Tag, message)
        WhiteDnsProxyEvents.ready(currentSessionId, message)
        sendProxyEvent(BroadcastTypeReady, message)
    }

    private fun sendProxyEvent(type: String, message: String) {
        sendBroadcast(
            Intent(BroadcastAction)
                .setPackage(packageName)
                .putExtra(BroadcastExtraType, type)
                .putExtra(BroadcastExtraSessionId, currentSessionId)
                .putExtra(BroadcastExtraMessage, message),
        )
    }

    companion object {
        private const val Tag = "WhiteDnsProxyService"
        const val BroadcastAction = "shop.whitedns.client.proxy.EVENT"
        const val BroadcastExtraType = "shop.whitedns.client.proxy.extra.TYPE"
        const val BroadcastExtraSessionId = "shop.whitedns.client.proxy.extra.SESSION_ID"
        const val BroadcastExtraMessage = "shop.whitedns.client.proxy.extra.MESSAGE"
        const val BroadcastTypeLog = "log"
        const val BroadcastTypeReady = "ready"
        const val BroadcastTypeFailed = "failed"
        private const val ActionStart = "shop.whitedns.client.proxy.START"
        private const val ActionStop = "shop.whitedns.client.proxy.STOP"
        private const val ExtraServerId = "shop.whitedns.client.proxy.extra.SERVER_ID"
        private const val ExtraServerLabel = "shop.whitedns.client.proxy.extra.SERVER_LABEL"
        private const val ExtraServerDomain = "shop.whitedns.client.proxy.extra.SERVER_DOMAIN"
        private const val ExtraServerEncryptionKey = "shop.whitedns.client.proxy.extra.SERVER_ENCRYPTION_KEY"
        private const val ExtraServerEncryptionMethod = "shop.whitedns.client.proxy.extra.SERVER_ENCRYPTION_METHOD"
        private const val ExtraSessionId = "shop.whitedns.client.proxy.extra.SESSION_ID"
        private const val ExtraSettings = "shop.whitedns.client.proxy.extra.SETTINGS"
        private const val RestartInitialDelayMillis = 2_000L
        private const val RestartMaxDelayMillis = 30_000L
        private const val PreviousRuntimeStopTimeoutMillis = 3_000L
        private const val PreviousRuntimeStopPollMillis = 100L
        private const val TrafficNotificationUpdateIntervalMillis = 1_000L
        private const val TrafficWarmupProbeSpacingMillis = 300L
        private const val NotificationId = 3201
        private const val NotificationChannelId = "whitedns_proxy"

        fun start(
            context: Context,
            sessionId: String,
            serverProfile: StormDnsServerProfile? = null,
            settings: WhiteDnsSettings? = null,
        ) {
            val intent = Intent(context, WhiteDnsProxyService::class.java)
                .setAction(ActionStart)
                .putExtra(ExtraSessionId, sessionId)
            if (settings != null) {
                intent.putExtra(ExtraSettings, settings)
            }
            if (serverProfile != null) {
                intent
                    .putExtra(ExtraServerId, serverProfile.id)
                    .putExtra(ExtraServerLabel, serverProfile.label)
                    .putExtra(ExtraServerDomain, serverProfile.domain)
                    .putExtra(ExtraServerEncryptionKey, serverProfile.encryptionKey)
                    .putExtra(ExtraServerEncryptionMethod, serverProfile.encryptionMethod)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            runCatching {
                context.startService(
                    Intent(context, WhiteDnsProxyService::class.java)
                        .setAction(ActionStop),
                )
            }.onFailure { error ->
                Log.w(Tag, "Failed to request proxy service stop", error)
                runCatching {
                    context.stopService(Intent(context, WhiteDnsProxyService::class.java))
                }.onFailure { stopError ->
                    Log.w(Tag, "Failed to stop proxy service", stopError)
                }
            }
        }

    }
}
