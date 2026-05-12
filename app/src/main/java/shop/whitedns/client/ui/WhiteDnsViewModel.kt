package shop.whitedns.client.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections
import java.util.UUID
import shop.whitedns.client.model.ConnectionProgressState
import shop.whitedns.client.model.ConnectionStats
import shop.whitedns.client.model.ConnectionStatus
import shop.whitedns.client.model.ConnectionVerificationState
import shop.whitedns.client.model.ConnectionVerificationStatus
import shop.whitedns.client.model.ResolverRuntimeState
import shop.whitedns.client.model.StormDnsServerProfile
import shop.whitedns.client.model.WhiteDnsRuntimeProxy
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.WhiteDnsSettingsStore
import shop.whitedns.client.model.WhiteDnsUiState
import shop.whitedns.client.model.importStormDnsProfileLink
import shop.whitedns.client.model.normalizedConnectionProfiles
import shop.whitedns.client.model.resolve
import shop.whitedns.client.model.runtimeConnectionSettings
import shop.whitedns.client.model.selectedConnectionProfile
import shop.whitedns.client.model.syncSelectedConnectionProfileFields
import shop.whitedns.client.proxy.WhiteDnsProxyEvent
import shop.whitedns.client.proxy.WhiteDnsProxyEvents
import shop.whitedns.client.proxy.WhiteDnsProxyService
import shop.whitedns.client.runtime.StormDnsTrafficStats
import shop.whitedns.client.runtime.WhiteDnsRuntimeState
import shop.whitedns.client.runtime.WhiteDnsRuntimeStateStore
import shop.whitedns.client.runtime.WhiteDnsTrafficWarmup
import shop.whitedns.client.runtime.parseStormDnsConnectionProgressLine
import shop.whitedns.client.runtime.parseStormDnsResolverStateLine
import shop.whitedns.client.runtime.parseStormDnsTrafficStatsLine
import shop.whitedns.client.storm.StormDnsBuiltInPool
import shop.whitedns.client.vpn.WhiteDnsVpnService
import shop.whitedns.client.vpn.WhiteDnsVpnEvent
import shop.whitedns.client.vpn.WhiteDnsVpnEvents

class WhiteDnsViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val settingsStore = WhiteDnsSettingsStore(appContext)

    var uiState by mutableStateOf(
        WhiteDnsUiState(
            settings = settingsStore.load(),
            serverPool = StormDnsBuiltInPool.profiles,
            networkIpAddress = findDeviceNetworkIpAddress(),
            batteryOptimizationIgnored = isIgnoringBatteryOptimizations(appContext),
            notificationsEnabled = areNotificationsEnabled(appContext),
        ),
    )
        private set

    private var connectJob: Job? = null
    private var statsJob: Job? = null
    private var runtimeRefreshJob: Job? = null
    private var batteryOptimizationRefreshJob: Job? = null
    private var verificationJob: Job? = null
    private var activeServerProfile: StormDnsServerProfile? = null
    private var activeRuntimeSessionId: String = ""
    private var activeProxyListenPort: Int = WhiteDnsRuntimeProxy.ListenPortInt
    private var trafficBaseline = TrafficSnapshot.empty()
    private var lastTrafficSnapshot = TrafficSnapshot.empty()
    private var activeVpnTrafficInterfaceName: String? = null
    @Volatile
    private var latestStormDnsTrafficStats: StormDnsTrafficStats? = null
    private var lastProgressUiUpdateMillis = 0L
    private var lastResolverUiUpdateMillis = 0L
    private val socksStreamTrackerLock = Any()
    private val socksStreamLastSeenMillis = mutableMapOf<Int, Long>()
    private val proxyEventListener: (WhiteDnsProxyEvent) -> Unit = { event ->
        when (event) {
            is WhiteDnsProxyEvent.Log -> handleRuntimeLog(event.sessionId, event.message)
            is WhiteDnsProxyEvent.Ready -> handleRuntimeReady(event.sessionId, event.message, expectedConnectionMode = "proxy")
            is WhiteDnsProxyEvent.Failed -> handleProxyFailure(event.sessionId, event.message)
        }
    }
    private val vpnEventListener: (WhiteDnsVpnEvent) -> Unit = { event ->
        when (event) {
            is WhiteDnsVpnEvent.Log -> handleRuntimeLog(event.sessionId, event.message)
            is WhiteDnsVpnEvent.Ready -> handleRuntimeReady(event.sessionId, event.message, expectedConnectionMode = "vpn")
            is WhiteDnsVpnEvent.Failed -> handleVpnFailure(event.sessionId, event.message)
        }
    }
    private val proxyBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != WhiteDnsProxyService.BroadcastAction) {
                return
            }
            val message = intent.getStringExtra(WhiteDnsProxyService.BroadcastExtraMessage).orEmpty()
            val sessionId = intent.getStringExtra(WhiteDnsProxyService.BroadcastExtraSessionId).orEmpty()
            when (intent.getStringExtra(WhiteDnsProxyService.BroadcastExtraType)) {
                WhiteDnsProxyService.BroadcastTypeLog -> handleRuntimeLog(sessionId, message)
                WhiteDnsProxyService.BroadcastTypeReady -> handleRuntimeReady(sessionId, message, expectedConnectionMode = "proxy")
                WhiteDnsProxyService.BroadcastTypeFailed -> handleProxyFailure(sessionId, message)
            }
        }
    }
    private val vpnBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != WhiteDnsVpnService.BroadcastAction) {
                return
            }
            val message = intent.getStringExtra(WhiteDnsVpnService.BroadcastExtraMessage).orEmpty()
            val sessionId = intent.getStringExtra(WhiteDnsVpnService.BroadcastExtraSessionId).orEmpty()
            when (intent.getStringExtra(WhiteDnsVpnService.BroadcastExtraType)) {
                WhiteDnsVpnService.BroadcastTypeLog -> handleRuntimeLog(sessionId, message)
                WhiteDnsVpnService.BroadcastTypeReady -> handleRuntimeReady(sessionId, message, expectedConnectionMode = "vpn")
                WhiteDnsVpnService.BroadcastTypeFailed -> handleVpnFailure(sessionId, message)
            }
        }
    }

    init {
        WhiteDnsProxyEvents.addListener(proxyEventListener)
        WhiteDnsVpnEvents.addListener(vpnEventListener)
        registerRuntimeBroadcastReceivers()
        refreshRuntimeConnectionStatus()
    }

    fun updateSettings(settings: WhiteDnsSettings) {
        val activeProfileId = uiState.activeConnectionProfileId
        val previousSettings = uiState.settings.syncSelectedConnectionProfileFields()
        if (
            activeProfileId != null &&
            uiState.connectionStatus != ConnectionStatus.DISCONNECTED &&
            uiState.settings.normalizedConnectionProfiles().any { it.id == activeProfileId } &&
            settings.normalizedConnectionProfiles().none { it.id == activeProfileId }
        ) {
            appendLog("Cannot delete the active connection profile")
            return
        }

        val normalizedSettings = settings.syncSelectedConnectionProfileFields()
        settingsStore.save(normalizedSettings)
        uiState = uiState.copy(
            settings = normalizedSettings,
            networkIpAddress = findDeviceNetworkIpAddress(),
        )
        if (shouldReconfigureActiveVpn(previousSettings, normalizedSettings)) {
            reconfigureActiveVpnSplitTunnel(normalizedSettings)
        }
    }

    fun importProfileLink(rawLink: String) {
        runCatching {
            uiState.settings
                .importStormDnsProfileLink(rawLink)
                .syncSelectedConnectionProfileFields()
        }.onSuccess { importedSettings ->
            settingsStore.save(importedSettings)
            uiState = uiState.copy(
                settings = importedSettings,
                networkIpAddress = findDeviceNetworkIpAddress(),
            )
            appendLog("Imported connection profile")
        }.onFailure { error ->
            appendLog("Profile import failed: ${error.message ?: error::class.java.simpleName}")
        }
    }

    fun refreshBatteryOptimizationStatus() {
        uiState = uiState.copy(
            batteryOptimizationIgnored = isIgnoringBatteryOptimizations(appContext),
        )
    }

    fun refreshBatteryOptimizationStatusWithRetry() {
        batteryOptimizationRefreshJob?.cancel()
        batteryOptimizationRefreshJob = viewModelScope.launch {
            repeat(BatteryOptimizationRefreshAttempts) { attempt ->
                refreshBatteryOptimizationStatus()
                if (uiState.batteryOptimizationIgnored) {
                    return@launch
                }
                if (attempt < BatteryOptimizationRefreshAttempts - 1) {
                    delay(BatteryOptimizationRefreshRetryDelayMillis)
                }
            }
        }
    }

    fun refreshNotificationStatus() {
        uiState = uiState.copy(
            notificationsEnabled = areNotificationsEnabled(appContext),
        )
    }

    fun refreshRuntimeConnectionStatus() {
        runtimeRefreshJob?.cancel()
        runtimeRefreshJob = viewModelScope.launch {
            if (uiState.connectionStatus == ConnectionStatus.CONNECTING) {
                return@launch
            }
            val activeRuntimeState = withContext(Dispatchers.IO) {
                findActiveRuntimeState()
            }
            if (activeRuntimeState != null) {
                if (!isSameConnectedRuntime(activeRuntimeState)) {
                    restoreRuntimeConnection(activeRuntimeState)
                }
                return@launch
            }
            if (uiState.connectionStatus == ConnectionStatus.CONNECTED) {
                val currentRuntimeHealthy = withContext(Dispatchers.IO) {
                    isCurrentRuntimeHealthy()
                }
                if (!currentRuntimeHealthy) {
                    markRuntimeDisconnected("Connection stopped")
                }
            }
        }
    }

    fun beginConnection() {
        if (uiState.connectionStatus != ConnectionStatus.DISCONNECTED) {
            return
        }

        connectJob?.cancel()
        statsJob?.cancel()
        runtimeRefreshJob?.cancel()
        verificationJob?.cancel()
        val sessionId = UUID.randomUUID().toString()
        activeRuntimeSessionId = sessionId
        uiState = uiState.copy(
            connectionStatus = ConnectionStatus.CONNECTING,
            connectionStats = ConnectionStats(),
            resolverRuntimeState = ResolverRuntimeState(),
            connectionProgress = ConnectionProgressState(phase = "preparing", percent = 3),
            connectionVerification = ConnectionVerificationState(),
            connectionLogs = listOf("Starting StormDNS"),
        )
        activeVpnTrafficInterfaceName = null
        latestStormDnsTrafficStats = null
        trafficBaseline = currentTrafficSnapshot()
        lastTrafficSnapshot = trafficBaseline
        resetSocksStreamTracker()
        resetRuntimeUiThrottles()

        connectJob = viewModelScope.launch {
            val settings = uiState.settings.syncSelectedConnectionProfileFields()
            if (settings.resolve().resolverEntries.isEmpty()) {
                appendLog("Resolvers are required to connect")
                activeRuntimeSessionId = ""
                uiState = uiState.copy(
                    connectionStatus = ConnectionStatus.DISCONNECTED,
                    resolverRuntimeState = ResolverRuntimeState(),
                    connectionProgress = ConnectionProgressState(),
                    connectionVerification = ConnectionVerificationState(),
                )
                return@launch
            }
            val connectionProfile = settings.selectedConnectionProfile()
            val serverProfile = selectServerProfile(settings)
            if (serverProfile == null) {
                appendLog(
                    if (connectionProfile.serverMode == "custom") {
                        "Custom StormDNS domain and encryption key are required"
                    } else {
                        "No StormDNS server profile configured"
                    },
                )
                activeRuntimeSessionId = ""
                uiState = uiState.copy(
                    connectionStatus = ConnectionStatus.DISCONNECTED,
                    resolverRuntimeState = ResolverRuntimeState(),
                    connectionProgress = ConnectionProgressState(),
                    connectionVerification = ConnectionVerificationState(),
                )
                return@launch
            }

            activeServerProfile = serverProfile
            val runtimeSettings = settings.runtimeConnectionSettings()
            uiState = uiState.copy(
                settings = settings,
                activeConnectionProfileId = connectionProfile.id,
            )
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val resolvedSettings = runtimeSettings.resolve()
                    activeProxyListenPort = resolvedSettings.listenPort
                    val modeLabel = if (resolvedSettings.connectionMode == "vpn") {
                        "Full System VPN"
                    } else {
                        "Proxy Only"
                    }
                    appendLog(
                        if (connectionProfile.serverMode == "custom") {
                            "Using custom StormDNS server"
                        } else {
                            "Using configured StormDNS server"
                        },
                    )
                    appendLog("Connection mode: $modeLabel")
                    if (resolvedSettings.connectionMode == "vpn") {
                        appendLog("Starting full-device VPN service")
                        WhiteDnsVpnService.start(
                            context = getApplication<Application>().applicationContext,
                            sessionId = sessionId,
                            serverProfile = serverProfile,
                            settings = runtimeSettings,
                        )
                        true
                    } else {
                        appendLog("Starting local proxy service")
                        WhiteDnsProxyService.start(
                            context = getApplication<Application>().applicationContext,
                            sessionId = sessionId,
                            serverProfile = serverProfile,
                            settings = runtimeSettings,
                        )
                        true
                    }
                }
            }

            val started = result.getOrElse { error ->
                appendLog("Launch failed: ${error.message ?: error::class.java.simpleName}")
                false
            }

            if (started) {
                uiState = uiState.copy(
                    networkIpAddress = findDeviceNetworkIpAddress(),
                    activeConnectionProfileId = connectionProfile.id,
                )
            } else {
                withContext(Dispatchers.IO) {
                    stopAllRuntimeServices()
                }
                activeProxyListenPort = WhiteDnsRuntimeProxy.ListenPortInt
                activeRuntimeSessionId = ""
                latestStormDnsTrafficStats = null
                resetSocksStreamTracker()
                resetRuntimeUiThrottles()
                appendLog("Connection failed")
                uiState = uiState.copy(
                    connectionStatus = ConnectionStatus.DISCONNECTED,
                    connectionStats = ConnectionStats(),
                    resolverRuntimeState = ResolverRuntimeState(),
                    connectionProgress = ConnectionProgressState(),
                    connectionVerification = ConnectionVerificationState(),
                    networkIpAddress = findDeviceNetworkIpAddress(),
                    activeConnectionProfileId = null,
                )
            }
        }
    }

    fun disconnect() {
        connectJob?.cancel()
        statsJob?.cancel()
        runtimeRefreshJob?.cancel()
        verificationJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            stopAllRuntimeServices()
            if (uiState.settings.resolve().connectionMode == "vpn") {
                delay(VpnStopBeforeStormDnsStopDelayMillis)
            }
        }
        activeProxyListenPort = WhiteDnsRuntimeProxy.ListenPortInt
        activeVpnTrafficInterfaceName = null
        activeRuntimeSessionId = ""
        latestStormDnsTrafficStats = null
        resetSocksStreamTracker()
        resetRuntimeUiThrottles()
        appendLog("Disconnected")
        uiState = uiState.copy(
            connectionStatus = ConnectionStatus.DISCONNECTED,
            connectionStats = ConnectionStats(),
            resolverRuntimeState = ResolverRuntimeState(),
            connectionProgress = ConnectionProgressState(),
            connectionVerification = ConnectionVerificationState(),
            activeConnectionProfileId = null,
        )
    }

    private fun startStatsMonitor() {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            while (isActive && uiState.connectionStatus == ConnectionStatus.CONNECTED) {
                delay(1_000)
                val listenPort = activeProxyListenPort
                val stats = withContext(Dispatchers.IO) {
                    buildConnectionStats(listenPort = listenPort)
                }
                uiState = uiState.copy(
                    connectionStats = stats,
                )
            }
        }
    }

    override fun onCleared() {
        connectJob?.cancel()
        statsJob?.cancel()
        runtimeRefreshJob?.cancel()
        verificationJob?.cancel()
        WhiteDnsProxyEvents.removeListener(proxyEventListener)
        WhiteDnsVpnEvents.removeListener(vpnEventListener)
        unregisterRuntimeBroadcastReceivers()
        super.onCleared()
    }

    private fun registerRuntimeBroadcastReceivers() {
        ContextCompat.registerReceiver(
            appContext,
            proxyBroadcastReceiver,
            IntentFilter(WhiteDnsProxyService.BroadcastAction),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        ContextCompat.registerReceiver(
            appContext,
            vpnBroadcastReceiver,
            IntentFilter(WhiteDnsVpnService.BroadcastAction),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    private fun unregisterRuntimeBroadcastReceivers() {
        runCatching {
            appContext.unregisterReceiver(proxyBroadcastReceiver)
        }
        runCatching {
            appContext.unregisterReceiver(vpnBroadcastReceiver)
        }
    }

    private fun handleRuntimeLog(sessionId: String, message: String) {
        if (isStaleRuntimeEvent(sessionId)) {
            return
        }
        val trafficStats = parseStormDnsTrafficStatsLine(message)
        val progressState = parseStormDnsConnectionProgressLine(message)
        val resolverState = parseStormDnsResolverStateLine(message)
        if (trafficStats != null) {
            latestStormDnsTrafficStats = trafficStats
        }
        trackSocksStreamLogLine(message)
        val isTelemetry = trafficStats != null ||
            progressState != null ||
            resolverState != null ||
            message.contains("WD_PROGRESS") ||
            message.contains("WD_RESOLVERS")
        if (progressState == null && resolverState == null && isTelemetry) {
            return
        }
        viewModelScope.launch(Dispatchers.Main.immediate) {
            progressState?.let(::updateConnectionProgressOnMain)
            resolverState?.let(::updateResolverStateOnMain)
            if (!isTelemetry) {
                appendLogOnMain(message)
            }
        }
    }

    private fun handleRuntimeReady(sessionId: String, message: String, expectedConnectionMode: String) {
        if (isStaleRuntimeEvent(sessionId)) {
            return
        }
        viewModelScope.launch(Dispatchers.Main.immediate) {
            if (uiState.connectionStatus == ConnectionStatus.DISCONNECTED) {
                val activeRuntimeState = withContext(Dispatchers.IO) {
                    findActiveRuntimeState()?.takeIf { it.mode == expectedConnectionMode }
                }
                if (activeRuntimeState != null) {
                    restoreRuntimeConnection(activeRuntimeState)
                }
                return@launch
            }
            if (uiState.connectionStatus != ConnectionStatus.CONNECTING) {
                return@launch
            }
            if (uiState.settings.resolve().connectionMode != expectedConnectionMode) {
                return@launch
            }
            appendLogOnMain(message)
            uiState = uiState.copy(
                connectionStatus = ConnectionStatus.CONNECTED,
                connectionStats = ConnectionStats(),
                connectionProgress = ConnectionProgressState(phase = "connected", percent = 100),
                networkIpAddress = findDeviceNetworkIpAddress(),
            )
            trafficBaseline = currentTrafficSnapshot()
            lastTrafficSnapshot = trafficBaseline
            startStatsMonitor()
            startConnectionVerification(expectedConnectionMode)
        }
    }

    private fun handleProxyFailure(sessionId: String, message: String) {
        if (isStaleRuntimeEvent(sessionId)) {
            return
        }
        viewModelScope.launch(Dispatchers.Main.immediate) {
            if (!shouldHandleRuntimeEvent(WhiteDnsRuntimeStateStore.ModeProxy)) {
                return@launch
            }
            appendLogOnMain(message)
            connectJob?.cancel()
            statsJob?.cancel()
            verificationJob?.cancel()
            withContext(Dispatchers.IO) {
                stopAllRuntimeServices()
            }
            activeProxyListenPort = WhiteDnsRuntimeProxy.ListenPortInt
            activeVpnTrafficInterfaceName = null
            activeRuntimeSessionId = ""
            latestStormDnsTrafficStats = null
            resetSocksStreamTracker()
            resetRuntimeUiThrottles()
            uiState = uiState.copy(
                connectionStatus = ConnectionStatus.DISCONNECTED,
                connectionStats = ConnectionStats(),
                resolverRuntimeState = ResolverRuntimeState(),
                connectionProgress = ConnectionProgressState(),
                connectionVerification = ConnectionVerificationState(),
                networkIpAddress = findDeviceNetworkIpAddress(),
                activeConnectionProfileId = null,
            )
        }
    }

    private fun handleVpnFailure(sessionId: String, message: String) {
        if (isStaleRuntimeEvent(sessionId)) {
            return
        }
        viewModelScope.launch(Dispatchers.Main.immediate) {
            if (!shouldHandleRuntimeEvent(WhiteDnsRuntimeStateStore.ModeVpn)) {
                return@launch
            }
            appendLogOnMain(message)
            connectJob?.cancel()
            statsJob?.cancel()
            verificationJob?.cancel()
            withContext(Dispatchers.IO) {
                stopAllRuntimeServices()
            }
            activeProxyListenPort = WhiteDnsRuntimeProxy.ListenPortInt
            activeVpnTrafficInterfaceName = null
            activeRuntimeSessionId = ""
            latestStormDnsTrafficStats = null
            resetSocksStreamTracker()
            resetRuntimeUiThrottles()
            uiState = uiState.copy(
                connectionStatus = ConnectionStatus.DISCONNECTED,
                connectionStats = ConnectionStats(),
                resolverRuntimeState = ResolverRuntimeState(),
                connectionProgress = ConnectionProgressState(),
                connectionVerification = ConnectionVerificationState(),
                networkIpAddress = findDeviceNetworkIpAddress(),
                activeConnectionProfileId = null,
            )
        }
    }

    private fun shouldHandleRuntimeEvent(expectedConnectionMode: String): Boolean {
        return uiState.connectionStatus != ConnectionStatus.DISCONNECTED &&
            uiState.settings.resolve().connectionMode == expectedConnectionMode
    }

    private fun isStaleRuntimeEvent(sessionId: String): Boolean {
        return activeRuntimeSessionId.isNotBlank() && sessionId != activeRuntimeSessionId
    }

    private fun findActiveRuntimeState(): WhiteDnsRuntimeState? {
        return WhiteDnsRuntimeStateStore.readAll(appContext)
            .asSequence()
            .filter { state ->
                state.status == WhiteDnsRuntimeStateStore.StatusReady ||
                    state.status == WhiteDnsRuntimeStateStore.StatusStarting
            }
            .sortedByDescending { it.updatedAtMillis }
            .firstOrNull(::isRuntimeStateHealthy)
    }

    private fun isRuntimeStateHealthy(state: WhiteDnsRuntimeState): Boolean {
        return when (state.mode) {
            WhiteDnsRuntimeStateStore.ModeProxy -> state.listenPort > 0 && canConnectToLocalPort(state.listenPort)
            WhiteDnsRuntimeStateStore.ModeVpn -> state.listenPort > 0 &&
                findVpnTrafficInterfaceName() != null &&
                canConnectToLocalPort(state.listenPort)
            else -> false
        }
    }

    private fun isCurrentRuntimeHealthy(): Boolean {
        return when (uiState.settings.resolve().connectionMode) {
            WhiteDnsRuntimeStateStore.ModeProxy -> canConnectToLocalPort(activeProxyListenPort)
            WhiteDnsRuntimeStateStore.ModeVpn -> findVpnTrafficInterfaceName() != null &&
                canConnectToLocalPort(activeProxyListenPort)
            else -> false
        }
    }

    private fun isSameConnectedRuntime(state: WhiteDnsRuntimeState): Boolean {
        val activeProfileId = state.connectionProfileId.takeIf(String::isNotBlank)
        return uiState.connectionStatus == ConnectionStatus.CONNECTED &&
            (state.sessionId.isBlank() || activeRuntimeSessionId == state.sessionId) &&
            uiState.settings.resolve().connectionMode == state.mode &&
            (activeProfileId == null || uiState.activeConnectionProfileId == activeProfileId)
    }

    private fun restoreRuntimeConnection(state: WhiteDnsRuntimeState) {
        val profileId = state.connectionProfileId.takeIf(String::isNotBlank)
        activeRuntimeSessionId = state.sessionId
        val restoredSettings = uiState.settings
            .copy(
                selectedConnectionProfileId = profileId ?: uiState.settings.selectedConnectionProfileId,
                connectionMode = state.mode,
            )
            .syncSelectedConnectionProfileFields()
        activeProxyListenPort = state.listenPort.takeIf { it > 0 }
            ?: restoredSettings.runtimeConnectionSettings().resolve().listenPort
        activeVpnTrafficInterfaceName = null
        latestStormDnsTrafficStats = null
        resetSocksStreamTracker()
        resetRuntimeUiThrottles()
        val modeLabel = if (state.mode == WhiteDnsRuntimeStateStore.ModeVpn) {
            "VPN"
        } else {
            "proxy"
        }
        uiState = uiState.copy(
            settings = restoredSettings,
            connectionStatus = ConnectionStatus.CONNECTED,
            connectionStats = ConnectionStats(),
            resolverRuntimeState = ResolverRuntimeState(),
            connectionProgress = ConnectionProgressState(phase = "connected", percent = 100),
            connectionVerification = ConnectionVerificationState(),
            networkIpAddress = findDeviceNetworkIpAddress(),
            activeConnectionProfileId = restoredSettings.selectedConnectionProfile().id,
            connectionLogs = prependConnectionLog("Restored active $modeLabel connection"),
        )
        trafficBaseline = currentTrafficSnapshot()
        lastTrafficSnapshot = trafficBaseline
        startStatsMonitor()
        startConnectionVerification(state.mode)
    }

    private fun markRuntimeDisconnected(message: String) {
        connectJob?.cancel()
        statsJob?.cancel()
        verificationJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            stopAllRuntimeServices()
        }
        activeProxyListenPort = WhiteDnsRuntimeProxy.ListenPortInt
        activeVpnTrafficInterfaceName = null
        latestStormDnsTrafficStats = null
        resetSocksStreamTracker()
        resetRuntimeUiThrottles()
        uiState = uiState.copy(
            connectionStatus = ConnectionStatus.DISCONNECTED,
            connectionStats = ConnectionStats(),
            resolverRuntimeState = ResolverRuntimeState(),
            connectionProgress = ConnectionProgressState(),
            connectionVerification = ConnectionVerificationState(),
            networkIpAddress = findDeviceNetworkIpAddress(),
            activeConnectionProfileId = null,
            connectionLogs = prependConnectionLog(message),
        )
    }

    private fun prependConnectionLog(message: String): List<String> {
        val cleanMessage = message
            .replace(Regex("\\u001B\\[[;\\d]*m"), "")
            .trim()
            .redactRouteDetails()
        if (cleanMessage.isEmpty()) {
            return uiState.connectionLogs
        }
        return (listOf(cleanMessage) + uiState.connectionLogs).take(MaxConnectionLogs)
    }

    private fun shouldReconfigureActiveVpn(
        previousSettings: WhiteDnsSettings,
        nextSettings: WhiteDnsSettings,
    ): Boolean {
        if (uiState.connectionStatus != ConnectionStatus.CONNECTED) {
            return false
        }
        if (previousSettings.resolve().connectionMode != "vpn" || nextSettings.resolve().connectionMode != "vpn") {
            return false
        }
        return previousSettings.splitTunnelMode != nextSettings.splitTunnelMode ||
            previousSettings.splitTunnelPackages != nextSettings.splitTunnelPackages
    }

    private fun reconfigureActiveVpnSplitTunnel(settings: WhiteDnsSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolvedSettings = settings.runtimeConnectionSettings().resolve()
            if (resolvedSettings.connectionMode != "vpn") {
                return@launch
            }
            runCatching {
                WhiteDnsVpnService.start(
                    context = getApplication<Application>().applicationContext,
                    sessionId = activeRuntimeSessionId,
                    serverProfile = activeServerProfile,
                    settings = settings.runtimeConnectionSettings(),
                )
            }.onSuccess {
                appendLog("Updated VPN split tunnel apps")
            }.onFailure { error ->
                handleVpnFailure(
                    activeRuntimeSessionId,
                    "Failed to update split tunnel: ${error.message ?: error::class.java.simpleName}",
                )
            }
        }
    }

    private fun stopAllRuntimeServices() {
        WhiteDnsVpnService.stop(appContext)
        WhiteDnsProxyService.stop(appContext)
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

    private fun startConnectionVerification(expectedConnectionMode: String) {
        verificationJob?.cancel()
        uiState = uiState.copy(
            connectionVerification = ConnectionVerificationState(
                status = ConnectionVerificationStatus.Checking,
                message = "Checking tunnel route",
            ),
        )
        verificationJob = viewModelScope.launch {
            delay(VerificationStartDelayMillis)
            val result = withContext(Dispatchers.IO) {
                verifyActiveConnection(expectedConnectionMode)
            }
            if (
                uiState.connectionStatus != ConnectionStatus.CONNECTED ||
                uiState.settings.resolve().connectionMode != expectedConnectionMode
            ) {
                return@launch
            }
            uiState = uiState.copy(connectionVerification = result)
            appendLog(result.message)
        }
    }

    private suspend fun verifyActiveConnection(expectedConnectionMode: String): ConnectionVerificationState {
        val resolvedSettings = uiState.settings
            .runtimeConnectionSettings()
            .resolve()
            .copy(listenPort = activeProxyListenPort)
        if (resolvedSettings.connectionMode != expectedConnectionMode) {
            return failedVerification("Connection mode changed before verification finished")
        }
        if (!canConnectToLocalPort(activeProxyListenPort)) {
            return failedVerification("Connection verification failed: local SOCKS listener is not reachable")
        }
        if (expectedConnectionMode == WhiteDnsRuntimeStateStore.ModeVpn && findVpnTrafficInterfaceName() == null) {
            return failedVerification("Connection verification failed: VPN interface is not active")
        }

        val probePassed = repeatBooleanAttempt(VerificationProbeAttempts) {
            WhiteDnsTrafficWarmup.verifySocksRoute(resolvedSettings)
        }
        return ConnectionVerificationState(
            status = ConnectionVerificationStatus.Verified,
            message = if (probePassed) {
                if (expectedConnectionMode == WhiteDnsRuntimeStateStore.ModeVpn) {
                    "Connection verified: VPN tunnel can reach the internet"
                } else {
                    "Connection verified: proxy tunnel can reach the internet"
                }
            } else {
                if (expectedConnectionMode == WhiteDnsRuntimeStateStore.ModeVpn) {
                    "Connection ready: VPN tunnel is active; outbound probe is still warming up"
                } else {
                    "Connection ready: proxy tunnel is active; outbound probe is still warming up"
                }
            },
            checkedAtMillis = System.currentTimeMillis(),
        )
    }

    private fun failedVerification(message: String): ConnectionVerificationState {
        return ConnectionVerificationState(
            status = ConnectionVerificationStatus.Failed,
            message = message,
            checkedAtMillis = System.currentTimeMillis(),
        )
    }

    private suspend fun repeatBooleanAttempt(
        attempts: Int,
        block: () -> Boolean,
    ): Boolean {
        repeat(attempts.coerceAtLeast(1)) { attempt ->
            if (block()) {
                return true
            }
            if (attempt < attempts - 1) {
                delay(VerificationProbeRetryDelayMillis)
            }
        }
        return false
    }

    private fun buildConnectionStats(listenPort: Int): ConnectionStats {
        val connectedApps = maxOf(
            countActiveProxyClients(listenPort),
            countTrackedSocksStreams(),
        )
        latestStormDnsTrafficStats?.let { stats ->
            val peakSpeed = maxOf(
                uiState.connectionStats.peakSpeedBytesPerSecond,
                stats.downloadSpeedBytesPerSecond + stats.uploadSpeedBytesPerSecond,
            )
            return ConnectionStats(
                downloadBytes = stats.downloadBytes,
                uploadBytes = stats.uploadBytes,
                totalDataUsageBytes = stats.downloadBytes + stats.uploadBytes,
                downloadSpeedBytesPerSecond = stats.downloadSpeedBytesPerSecond,
                uploadSpeedBytesPerSecond = stats.uploadSpeedBytesPerSecond,
                peakSpeedBytesPerSecond = peakSpeed,
                connectedApps = connectedApps,
            )
        }

        val previous = lastTrafficSnapshot
        val current = currentTrafficSnapshot()
        if (
            current.sourceKey != previous.sourceKey ||
            current.sourceKey != trafficBaseline.sourceKey ||
            current.rxBytes < previous.rxBytes ||
            current.txBytes < previous.txBytes ||
            current.rxBytes < trafficBaseline.rxBytes ||
            current.txBytes < trafficBaseline.txBytes
        ) {
            trafficBaseline = current
            lastTrafficSnapshot = current
            return ConnectionStats(
                connectedApps = connectedApps,
            )
        }
        lastTrafficSnapshot = current

        val elapsedMillis = (current.timestampMillis - previous.timestampMillis).coerceAtLeast(1)
        val downloadBytes = (current.rxBytes - trafficBaseline.rxBytes).coerceAtLeast(0)
        val uploadBytes = (current.txBytes - trafficBaseline.txBytes).coerceAtLeast(0)
        val downloadSpeed = (((current.rxBytes - previous.rxBytes).coerceAtLeast(0)) * 1_000) / elapsedMillis
        val uploadSpeed = (((current.txBytes - previous.txBytes).coerceAtLeast(0)) * 1_000) / elapsedMillis
        val peakSpeed = maxOf(
            uiState.connectionStats.peakSpeedBytesPerSecond,
            downloadSpeed + uploadSpeed,
        )

        return ConnectionStats(
            downloadBytes = downloadBytes,
            uploadBytes = uploadBytes,
            totalDataUsageBytes = downloadBytes + uploadBytes,
            downloadSpeedBytesPerSecond = downloadSpeed,
            uploadSpeedBytesPerSecond = uploadSpeed,
            peakSpeedBytesPerSecond = peakSpeed,
            connectedApps = connectedApps,
        )
    }

    private fun currentTrafficSnapshot(): TrafficSnapshot {
        if (uiState.settings.resolve().connectionMode == "vpn") {
            currentVpnTrafficSnapshot()?.let { snapshot ->
                return snapshot
            }
        }
        return currentUidTrafficSnapshot()
    }

    private fun currentUidTrafficSnapshot(): TrafficSnapshot {
        val uid = getApplication<Application>().applicationInfo.uid
        val rxBytes = TrafficStats.getUidRxBytes(uid).normalizeTrafficCounter()
        val txBytes = TrafficStats.getUidTxBytes(uid).normalizeTrafficCounter()
        return TrafficSnapshot(
            rxBytes = rxBytes,
            txBytes = txBytes,
            timestampMillis = System.currentTimeMillis(),
            sourceKey = "$UidTrafficSourcePrefix$uid",
        )
    }

    private fun currentVpnTrafficSnapshot(): TrafficSnapshot? {
        val cachedName = activeVpnTrafficInterfaceName
        if (cachedName != null) {
            val cachedCounters = readNetworkInterfaceCounters(cachedName)
            if (cachedCounters != null) {
                return cachedCounters.toTrafficSnapshot(cachedName)
            }
            activeVpnTrafficInterfaceName = null
        }

        val interfaceName = findVpnTrafficInterfaceName() ?: return null
        val counters = readNetworkInterfaceCounters(interfaceName) ?: return null
        activeVpnTrafficInterfaceName = interfaceName
        return counters.toTrafficSnapshot(interfaceName)
    }

    private fun Pair<Long, Long>.toTrafficSnapshot(interfaceName: String): TrafficSnapshot {
        return TrafficSnapshot(
            rxBytes = first,
            txBytes = second,
            timestampMillis = System.currentTimeMillis(),
            sourceKey = "$VpnTrafficSourcePrefix$interfaceName",
        )
    }

    private fun findVpnTrafficInterfaceName(): String? {
        return runCatching {
            NetworkInterface.getNetworkInterfaces()
                .asSequence()
                .firstOrNull { networkInterface ->
                    networkInterface.isUp &&
                        networkInterface.inetAddresses
                            .asSequence()
                            .any { address ->
                                address.hostAddress?.substringBefore('%') == WhiteDnsVpnService.TunIpv4Address
                            }
                }
                ?.name
        }.getOrNull()
    }

    private fun canConnectToLocalPort(port: Int): Boolean {
        if (port !in 1..65535) {
            return false
        }
        return runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("127.0.0.1", port), 300)
            }
            true
        }.getOrDefault(false)
    }

    private fun readNetworkInterfaceCounters(interfaceName: String): Pair<Long, Long>? {
        if (!SafeNetworkInterfaceNameRegex.matches(interfaceName)) {
            return null
        }
        val statisticsDir = File(File(File("/sys/class/net"), interfaceName), "statistics")
        val rxBytes = readTrafficCounterFile(File(statisticsDir, "rx_bytes")) ?: return null
        val txBytes = readTrafficCounterFile(File(statisticsDir, "tx_bytes")) ?: return null
        return rxBytes to txBytes
    }

    private fun readTrafficCounterFile(file: File): Long? {
        return runCatching {
            file.readText()
                .trim()
                .toLongOrNull()
                ?.coerceAtLeast(0)
        }.getOrNull()
    }

    private fun updateConnectionProgressOnMain(progressState: ConnectionProgressState) {
        val currentProgress = uiState.connectionProgress
        if (progressState == currentProgress) {
            return
        }
        val now = System.currentTimeMillis()
        val phaseOrPercentChanged = progressState.phase != currentProgress.phase ||
            progressState.percent != currentProgress.percent
        val shouldUpdate = phaseOrPercentChanged ||
            now - lastProgressUiUpdateMillis >= RuntimeProgressUiUpdateIntervalMillis
        if (!shouldUpdate) {
            return
        }
        lastProgressUiUpdateMillis = now
        uiState = uiState.copy(connectionProgress = progressState)
    }

    private fun updateResolverStateOnMain(resolverState: ResolverRuntimeState) {
        if (resolverState == uiState.resolverRuntimeState) {
            return
        }
        val now = System.currentTimeMillis()
        val firstVisibleState = uiState.resolverRuntimeState == ResolverRuntimeState()
        if (!firstVisibleState && now - lastResolverUiUpdateMillis < RuntimeResolverUiUpdateIntervalMillis) {
            return
        }
        lastResolverUiUpdateMillis = now
        uiState = uiState.copy(resolverRuntimeState = resolverState)
    }

    private fun countActiveProxyClients(listenPort: Int): Int {
        val tcpPaths = listOf(
            "/proc/self/net/tcp",
            "/proc/self/net/tcp6",
            "/proc/net/tcp",
            "/proc/net/tcp6",
        )
        val localMatches = tcpPaths
            .flatMap { path -> activeTcpClientKeys(path, listenPort, matchLocalPort = true) }
            .distinct()
        if (localMatches.isNotEmpty()) {
            return localMatches.size
        }

        return tcpPaths
            .flatMap { path -> activeTcpClientKeys(path, listenPort, matchLocalPort = false) }
            .distinct()
            .size
    }

    private fun activeTcpClientKeys(
        path: String,
        listenPort: Int,
        matchLocalPort: Boolean,
    ): List<String> {
        return runCatching {
            java.io.File(path)
                .readLines()
                .drop(1)
                .mapNotNull { line ->
                    val columns = line.trim().split(Regex("\\s+"))
                    val localAddress = columns.getOrNull(1) ?: return@mapNotNull null
                    val remoteAddress = columns.getOrNull(2) ?: return@mapNotNull null
                    val state = columns.getOrNull(3) ?: return@mapNotNull null
                    val addressToMatch = if (matchLocalPort) localAddress else remoteAddress
                    val portHex = addressToMatch.substringAfterLast(':', missingDelimiterValue = "")
                    val port = portHex.toIntOrNull(radix = 16)
                    if (port == listenPort && state == EstablishedTcpState) {
                        "$localAddress-$remoteAddress-$state"
                    } else {
                        null
                    }
                }
        }.getOrDefault(emptyList())
    }

    private fun trackSocksStreamLogLine(line: String) {
        val now = System.currentTimeMillis()
        socksStreamOpenedRegex.find(line)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { streamId ->
            synchronized(socksStreamTrackerLock) {
                socksStreamLastSeenMillis[streamId] = now
                pruneTrackedSocksStreamsLocked(now)
            }
            return
        }

        val closeMatch = socksStreamClosedRegex.find(line)
        val streamId = closeMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: return
        synchronized(socksStreamTrackerLock) {
            socksStreamLastSeenMillis.remove(streamId)
        }
    }

    private fun countTrackedSocksStreams(): Int {
        val now = System.currentTimeMillis()
        return synchronized(socksStreamTrackerLock) {
            pruneTrackedSocksStreamsLocked(now)
            socksStreamLastSeenMillis.size
        }
    }

    private fun resetSocksStreamTracker() {
        synchronized(socksStreamTrackerLock) {
            socksStreamLastSeenMillis.clear()
        }
    }

    private fun resetRuntimeUiThrottles() {
        lastProgressUiUpdateMillis = 0L
        lastResolverUiUpdateMillis = 0L
    }

    private fun pruneTrackedSocksStreamsLocked(now: Long) {
        socksStreamLastSeenMillis.entries.removeAll { (_, lastSeenMillis) ->
            now - lastSeenMillis > SocksStreamTrackingTtlMillis
        }
    }

    private fun Long.normalizeTrafficCounter(): Long {
        return if (this == TrafficStats.UNSUPPORTED.toLong()) 0 else coerceAtLeast(0)
    }

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(PowerManager::class.java) ?: return true
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun findDeviceNetworkIpAddress(): String {
        return runCatching {
            NetworkInterface.getNetworkInterfaces()
                .asSequence()
                .filter { it.isUp && !it.isLoopback && !it.isVirtual }
                .flatMap { it.inetAddresses.asSequence() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { !it.isLoopbackAddress && !it.isLinkLocalAddress }
                ?.hostAddress
        }.getOrNull() ?: "127.0.0.1"
    }

    private fun <T> java.util.Enumeration<T>.asSequence(): Sequence<T> {
        return Collections.list(this).asSequence()
    }

    private fun appendLog(message: String) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            appendLogOnMain(message)
        }
    }

    private fun appendLogOnMain(message: String) {
        val cleanMessage = message
            .replace(Regex("\\u001B\\[[;\\d]*m"), "")
            .trim()
            .redactRouteDetails()
        if (cleanMessage.isEmpty()) {
            return
        }
        val nextLogs = (listOf(cleanMessage) + uiState.connectionLogs).take(MaxConnectionLogs)
        uiState = uiState.copy(connectionLogs = nextLogs)
    }

    private companion object {
        const val MaxConnectionLogs = 50
        const val RuntimeProgressUiUpdateIntervalMillis = 250L
        const val RuntimeResolverUiUpdateIntervalMillis = 500L
        const val EstablishedTcpState = "01"
        const val VpnStopBeforeStormDnsStopDelayMillis = 1_500L
        const val SocksStreamTrackingTtlMillis = 120_000L
        const val EmptyTrafficSource = "none"
        const val BatteryOptimizationRefreshAttempts = 8
        const val BatteryOptimizationRefreshRetryDelayMillis = 500L
        const val VerificationStartDelayMillis = 700L
        const val VerificationProbeAttempts = 2
        const val VerificationProbeRetryDelayMillis = 750L
        const val UidTrafficSourcePrefix = "uid:"
        const val VpnTrafficSourcePrefix = "vpn:"
        val socksStreamOpenedRegex = Regex("""New SOCKS\d TCP CONNECT .*Stream ID:\s*(\d+)""")
        val socksStreamClosedRegex = Regex("""ARQ Stream Closed .*Stream:\s*(\d+)""")
        val SafeNetworkInterfaceNameRegex = Regex("""[A-Za-z0-9_.:-]+""")
    }

    private fun String.redactRouteDetails(): String {
        val profiles = activeServerProfile
            ?.let { uiState.serverPool + it }
            ?: uiState.serverPool
        return profiles.fold(this) { message, profile ->
            message
                .replace(profile.domain, "[server route]")
        }
    }

    private data class TrafficSnapshot(
        val rxBytes: Long,
        val txBytes: Long,
        val timestampMillis: Long,
        val sourceKey: String,
    ) {
        companion object {
            fun empty(): TrafficSnapshot {
                return TrafficSnapshot(
                    rxBytes = 0,
                    txBytes = 0,
                    timestampMillis = System.currentTimeMillis(),
                    sourceKey = EmptyTrafficSource,
                )
            }
        }
    }

}
