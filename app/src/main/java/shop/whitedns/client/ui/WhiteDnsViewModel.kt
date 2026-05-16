package shop.whitedns.client.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.TrafficStats
import android.os.PowerManager
import android.provider.OpenableColumns
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import shop.whitedns.client.model.AdvancedSettingsProfile
import shop.whitedns.client.model.AutoTuneTrialResult
import shop.whitedns.client.model.ConnectionProfile
import shop.whitedns.client.model.ConnectionProgressState
import shop.whitedns.client.model.ConnectionStats
import shop.whitedns.client.model.ConnectionStatus
import shop.whitedns.client.model.ConnectionVerificationState
import shop.whitedns.client.model.ConnectionVerificationStatus
import shop.whitedns.client.model.ResolverProfile
import shop.whitedns.client.model.ResolverRuntimeState
import shop.whitedns.client.model.StormDnsServerProfile
import shop.whitedns.client.model.WhiteDnsScanDefaults
import shop.whitedns.client.model.WhiteDnsScanState
import shop.whitedns.client.model.WhiteDnsScanStatus
import shop.whitedns.client.model.WhiteDnsRuntimeProxy
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.WhiteDnsSettingsStore
import shop.whitedns.client.model.WhiteDnsUiState
import shop.whitedns.client.model.WhiteDnsAutoTunePresets
import shop.whitedns.client.model.WhiteDnsParallelTest
import shop.whitedns.client.model.applyAdvancedProfile
import shop.whitedns.client.model.applyAutoTunePreset
import shop.whitedns.client.model.importStormDnsProfileLink
import shop.whitedns.client.model.normalizeServerDomains
import shop.whitedns.client.model.normalizedAdvancedProfiles
import shop.whitedns.client.model.normalizedConnectionProfiles
import shop.whitedns.client.model.normalizedResolverProfiles
import shop.whitedns.client.model.resolve
import shop.whitedns.client.model.runtimeConnectionSettings
import shop.whitedns.client.model.selectedConnectionProfile
import shop.whitedns.client.model.syncSelectedConnectionProfileFields
import shop.whitedns.client.model.validateConnectionSettings
import shop.whitedns.client.proxy.WhiteDnsProxyEvent
import shop.whitedns.client.proxy.WhiteDnsProxyEvents
import shop.whitedns.client.proxy.WhiteDnsProxyService
import shop.whitedns.client.runtime.StormDnsTrafficAccounting
import shop.whitedns.client.runtime.WhiteDnsRuntimeState
import shop.whitedns.client.runtime.WhiteDnsRuntimeStateStore
import shop.whitedns.client.runtime.WhiteDnsTrafficWarmup
import shop.whitedns.client.runtime.RuntimeLaunchRequestStore
import shop.whitedns.client.runtime.estimateDeduplicatedTraffic
import shop.whitedns.client.runtime.formatTrafficSpeed
import shop.whitedns.client.runtime.parseStormDnsConnectionProgressLine
import shop.whitedns.client.runtime.parseStormDnsResolverStateLine
import shop.whitedns.client.runtime.parseStormDnsTrafficStatsLine
import shop.whitedns.client.security.RedactionSecrets
import shop.whitedns.client.security.SecretRedactor
import shop.whitedns.client.scan.WhiteDnsScanLaunchRequest
import shop.whitedns.client.scan.WhiteDnsScanRequestStore
import shop.whitedns.client.scan.WhiteDnsScanService
import shop.whitedns.client.scan.WhiteDnsScanSettingsStore
import shop.whitedns.client.scan.WhiteDnsScanStateStore
import shop.whitedns.client.scan.WhiteDnsScannerResultStore
import shop.whitedns.client.storm.StormDnsBuiltInPool
import shop.whitedns.client.storm.StormDnsProcessManager
import shop.whitedns.client.vpn.WhiteDnsVpnService
import shop.whitedns.client.vpn.WhiteDnsVpnEvent
import shop.whitedns.client.vpn.WhiteDnsVpnEvents

class WhiteDnsViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val settingsStore = WhiteDnsSettingsStore(appContext)
    private val scanSettingsStore = WhiteDnsScanSettingsStore(appContext)
    private val initialSettings = settingsStore.load()
    private val initialPersistedScanState = WhiteDnsScanStateStore.read(appContext)
    private val initialScanState = initialPersistedScanState
        .recoverIfStale(
            nowMillis = System.currentTimeMillis(),
            staleAfterMillis = StaleScanStateTimeoutMillis,
        )

    var uiState by mutableStateOf(
        WhiteDnsUiState(
            settings = initialSettings,
            serverPool = StormDnsBuiltInPool.profiles,
            networkIpAddress = findDeviceNetworkIpAddress(),
            batteryOptimizationIgnored = isIgnoringBatteryOptimizations(appContext),
            notificationsEnabled = areNotificationsEnabled(appContext),
            scanState = initialScanState,
            scanWorkerCount = scanSettingsStore.loadWorkerCount().toString(),
            scanConnectionProfileId = resolveScanConnectionProfileId(
                settings = initialSettings,
                requestedProfileId = scanSettingsStore.loadConnectionProfileId(),
            ),
        ),
    )
        private set

    private var connectJob: Job? = null
    private var statsJob: Job? = null
    private var runtimeRefreshJob: Job? = null
    private var batteryOptimizationRefreshJob: Job? = null
    private var verificationJob: Job? = null
    private var readinessJob: Job? = null
    private var scanLaunchJob: Job? = null
    private var lastScannerResultProfileText = ""
    private var activeServerProfile: StormDnsServerProfile? = null
    private var activeRuntimeSessionId: String = ""
    private var activeProxyListenPort: Int = WhiteDnsRuntimeProxy.ListenPortInt
    private var trafficBaseline = TrafficSnapshot.empty()
    private var lastTrafficSnapshot = TrafficSnapshot.empty()
    private var activeVpnTrafficInterfaceName: String? = null
    private val stormDnsTrafficAccounting = StormDnsTrafficAccounting()
    private val autoTuneTrialManagersLock = Any()
    private var autoTuneTrialManagers: List<StormDnsProcessManager> = emptyList()
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
    private val scanBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != WhiteDnsScanService.BroadcastAction) {
                return
            }
            refreshScanState()
        }
    }

    init {
        if (initialScanState != initialPersistedScanState) {
            WhiteDnsScanStateStore.write(appContext, initialScanState)
        }
        WhiteDnsProxyEvents.addListener(proxyEventListener)
        WhiteDnsVpnEvents.addListener(vpnEventListener)
        registerRuntimeBroadcastReceivers()
        refreshRuntimeConnectionStatus()
        refreshScanState()
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

        val syncedSettings = settings.syncSelectedConnectionProfileFields()
        val normalizedSettings = syncedSettings
        val scanConnectionProfileId = resolveScanConnectionProfileId(
            settings = normalizedSettings,
            requestedProfileId = uiState.scanConnectionProfileId,
        )
        if (normalizedSettings != previousSettings) {
            readinessJob?.cancel()
        }
        if (scanConnectionProfileId != uiState.scanConnectionProfileId) {
            scanSettingsStore.saveConnectionProfileId(scanConnectionProfileId)
        }
        settingsStore.save(normalizedSettings)
        uiState = uiState.copy(
            settings = normalizedSettings,
            networkIpAddress = findDeviceNetworkIpAddress(),
            scanConnectionProfileId = scanConnectionProfileId,
            profileReadiness = if (normalizedSettings != previousSettings) {
                ConnectionVerificationState()
            } else {
                uiState.profileReadiness
            },
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
            readinessJob?.cancel()
            settingsStore.save(importedSettings)
            uiState = uiState.copy(
                settings = importedSettings,
                networkIpAddress = findDeviceNetworkIpAddress(),
                profileReadiness = ConnectionVerificationState(),
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

    fun testCurrentProfileReadiness() {
        if (uiState.connectionStatus != ConnectionStatus.DISCONNECTED) {
            return
        }
        val settingsSnapshot = uiState.settings.syncSelectedConnectionProfileFields()
        readinessJob?.cancel()
        uiState = uiState.copy(
            profileReadiness = ConnectionVerificationState(
                status = ConnectionVerificationStatus.Checking,
                message = "Checking setup",
            ),
        )
        readinessJob = viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                verifyProfileReadiness(settingsSnapshot)
            }
            if (uiState.connectionStatus != ConnectionStatus.DISCONNECTED) {
                return@launch
            }
            uiState = uiState.copy(profileReadiness = result)
        }
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
        readinessJob?.cancel()
        val sessionId = UUID.randomUUID().toString()
        activeRuntimeSessionId = sessionId
        uiState = uiState.copy(
            connectionStatus = ConnectionStatus.CONNECTING,
            connectionStats = ConnectionStats(),
            resolverRuntimeState = ResolverRuntimeState(),
            connectionProgress = ConnectionProgressState(phase = "preparing", percent = 3),
            profileReadiness = ConnectionVerificationState(),
            connectionVerification = ConnectionVerificationState(),
            autoTuneTrialResults = emptyList(),
            connectionLogs = listOf("Starting StormDNS"),
        )
        activeVpnTrafficInterfaceName = null
        resetTrafficAccounting()
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
                    autoTuneTrialResults = emptyList(),
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
                    autoTuneTrialResults = emptyList(),
                )
                return@launch
            }

            activeServerProfile = serverProfile
            val runtimeSettings = settings.runtimeConnectionSettings()
            val resolvedRuntimeSettings = runtimeSettings.resolve()
            val useParallelTest = settings.autoTuneEnabled &&
                resolvedRuntimeSettings.connectionMode in ParallelTestConnectionModes
            uiState = uiState.copy(
                settings = settings,
                activeConnectionProfileId = connectionProfile.id,
            )
            val started = if (useParallelTest) {
                runParallelTestConnection(
                    sessionId = sessionId,
                    baseSettings = settings,
                    connectionProfile = connectionProfile,
                    serverProfile = serverProfile,
                )
            } else {
                launchRuntime(
                    sessionId = sessionId,
                    connectionProfile = connectionProfile,
                    serverProfile = serverProfile,
                    runtimeSettings = runtimeSettings,
                )
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
                resetTrafficAccounting()
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

    private suspend fun launchRuntime(
        sessionId: String,
        connectionProfile: ConnectionProfile,
        serverProfile: StormDnsServerProfile,
        runtimeSettings: WhiteDnsSettings,
    ): Boolean {
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
                } else {
                    appendLog("Starting local proxy service")
                    WhiteDnsProxyService.start(
                        context = getApplication<Application>().applicationContext,
                        sessionId = sessionId,
                        serverProfile = serverProfile,
                        settings = runtimeSettings,
                    )
                }
                true
            }
        }

        return result.getOrElse { error ->
            appendLog("Launch failed: ${error.message ?: error::class.java.simpleName}")
            false
        }
    }

    private suspend fun runParallelTestConnection(
        sessionId: String,
        baseSettings: WhiteDnsSettings,
        connectionProfile: ConnectionProfile,
        serverProfile: StormDnsServerProfile,
    ): Boolean {
        return runParallelProxyTestConnection(
            sessionId = sessionId,
            baseSettings = baseSettings,
            connectionProfile = connectionProfile,
            serverProfile = serverProfile,
        )
    }

    private suspend fun runParallelProxyTestConnection(
        sessionId: String,
        baseSettings: WhiteDnsSettings,
        connectionProfile: ConnectionProfile,
        serverProfile: StormDnsServerProfile,
    ): Boolean = coroutineScope {
        val finalConnectionMode = baseSettings.runtimeConnectionSettings().resolve().connectionMode
        val finalModeLabel = if (finalConnectionMode == WhiteDnsRuntimeStateStore.ModeVpn) {
            "Full VPN"
        } else {
            "Proxy Mode"
        }
        val selectedConfigs = buildParallelTestConfigs(baseSettings)
        if (selectedConfigs.isEmpty()) {
            appendLog("Parallel Test: no configuration selected")
            uiState = uiState.copy(
                connectionProgress = ConnectionProgressState(),
                connectionVerification = ConnectionVerificationState(
                    status = ConnectionVerificationStatus.Failed,
                    message = "Parallel Test failed: no configuration selected",
                    checkedAtMillis = System.currentTimeMillis(),
                ),
            )
            return@coroutineScope false
        }

        val ports = allocateRandomLocalPorts(selectedConfigs.size)
        val trialPlans = selectedConfigs.mapIndexed { index, config ->
            AutoTuneTrialPlan(
                config = config,
                settings = config.userSettings
                    .copy(
                        connectionMode = WhiteDnsRuntimeStateStore.ModeProxy,
                        listenIp = WhiteDnsRuntimeProxy.ListenIp,
                        listenPort = ports[index].toString(),
                        httpProxyEnabled = false,
                        localDnsEnabled = false,
                        trafficWarmupEnabled = false,
                        autoTuneEnabled = true,
                    )
                    .syncSelectedConnectionProfileFields(),
                result = AutoTuneTrialResult(
                    configId = config.id,
                    label = config.label,
                    listenIp = WhiteDnsRuntimeProxy.ListenIp,
                    listenPort = ports[index],
                    status = "starting",
                    message = "Starting",
                ),
            )
        }
        val trialManagers = trialPlans.associate { plan ->
            plan.config.id to StormDnsProcessManager(appContext)
        }

        setAutoTuneTrialManagers(trialManagers.values.toList())
        uiState = uiState.copy(
            settings = baseSettings,
            connectionStatus = ConnectionStatus.CONNECTING,
            connectionStats = ConnectionStats(),
            resolverRuntimeState = ResolverRuntimeState(),
            connectionProgress = ConnectionProgressState(
                phase = "autotune",
                percent = 5,
                completed = 0,
                total = trialPlans.size,
            ),
            connectionVerification = ConnectionVerificationState(
                status = ConnectionVerificationStatus.Checking,
                message = "Parallel Test: testing ${trialPlans.size} SOCKS configurations before $finalModeLabel",
            ),
            autoTuneTrialResults = trialPlans.map { it.result },
            activeConnectionProfileId = connectionProfile.id,
        )
        appendLog("Parallel Test: testing ${trialPlans.size} SOCKS configurations in parallel before $finalModeLabel")

        try {
            val startups = trialPlans.map { plan ->
                async(Dispatchers.IO) {
                    startParallelAutoTuneTrial(
                        plan = plan,
                        manager = trialManagers.getValue(plan.config.id),
                        serverProfile = serverProfile,
                    )
                }
            }.awaitAll()
            val readyStartups = startups.filter { it.ready }
            if (readyStartups.isNotEmpty()) {
                withContext(Dispatchers.Main.immediate) {
                    uiState = uiState.copy(
                        connectionVerification = ConnectionVerificationState(
                            status = ConnectionVerificationStatus.Checking,
                            message = "Parallel Test: running tests on ${readyStartups.size} SOCKS configurations",
                        ),
                    )
                }
                delay(AutoTuneMeasurementSettleMillis)
            }
            val measuredResults = readyStartups.map { startup ->
                async(Dispatchers.IO) {
                    measureParallelAutoTuneTrial(startup)
                }
            }.awaitAll()
            val trialResults = measuredResults + startups
                .filterNot { it.ready }
                .map { it.result }
            val selectedResult = trialResults
                .filter { it.scoreBytesPerSecond > 0L }
                .sortedWith(
                    compareByDescending<AutoTuneResult> { it.scoreBytesPerSecond }
                        .thenBy { it.pingMillis ?: Long.MAX_VALUE },
                )
                .firstOrNull()
                ?: trialResults
                    .filter { it.ready }
                    .maxByOrNull { it.scoreBytesPerSecond }
                ?: run {
                    appendLog("Parallel Test: no SOCKS configuration became ready")
                    uiState = uiState.copy(
                        connectionProgress = ConnectionProgressState(),
                        connectionVerification = ConnectionVerificationState(
                            status = ConnectionVerificationStatus.Failed,
                            message = "Parallel Test failed: no SOCKS configuration became ready",
                            checkedAtMillis = System.currentTimeMillis(),
                        ),
                    )
                    return@coroutineScope false
                }

            val selectedUserSettings = selectedResult.config.userSettings
                .copy(
                    connectionMode = finalConnectionMode,
                    autoTuneEnabled = true,
                )
                .syncSelectedConnectionProfileFields()
            val selectedRuntimeSettings = selectedUserSettings.runtimeConnectionSettings()
            activeRuntimeSessionId = sessionId
            activeProxyListenPort = selectedRuntimeSettings.resolve().listenPort
            uiState = uiState.copy(
                settings = baseSettings,
                connectionStatus = ConnectionStatus.CONNECTING,
                connectionStats = ConnectionStats(),
                resolverRuntimeState = ResolverRuntimeState(),
                connectionProgress = ConnectionProgressState(phase = "preparing", percent = 3),
                connectionVerification = ConnectionVerificationState(),
                autoTuneTrialResults = uiState.autoTuneTrialResults.map { result ->
                    result.copy(selected = result.configId == selectedResult.config.id)
                },
                activeConnectionProfileId = connectionProfile.id,
            )
            appendLog(
                "Parallel Test: selected ${selectedResult.config.label} for this connection " +
                    "(${formatTrafficSpeed(selectedResult.scoreBytesPerSecond)}, " +
                    "ping ${formatAutoTuneLatency(selectedResult.pingMillis)}); starting $finalModeLabel",
            )
            launchRuntime(
                sessionId = sessionId,
                connectionProfile = connectionProfile,
                serverProfile = serverProfile,
                runtimeSettings = selectedRuntimeSettings,
            )
        } finally {
            withContext(Dispatchers.IO) {
                stopAutoTuneTrialManagers()
            }
        }
    }

    private fun buildParallelTestConfigs(baseSettings: WhiteDnsSettings): List<AutoTuneTrialConfig> {
        val advancedProfiles = baseSettings.normalizedAdvancedProfiles()
        val selectedConfigIds = WhiteDnsParallelTest.normalizeConfigIds(
            configIds = baseSettings.parallelTestSelectedConfigIds,
            advancedProfiles = advancedProfiles,
        )
        return selectedConfigIds.mapNotNull { configId ->
            WhiteDnsParallelTest.presetIdFromConfigId(configId)?.let { presetId ->
                val preset = WhiteDnsAutoTunePresets.all.firstOrNull { it.id == presetId } ?: return@mapNotNull null
                return@mapNotNull AutoTuneTrialConfig(
                    id = configId,
                    label = preset.label,
                    userSettings = baseSettings
                        .applyAutoTunePreset(preset)
                        .copy(
                            autoTuneEnabled = true,
                            parallelTestSelectedConfigIds = selectedConfigIds,
                        )
                        .syncSelectedConnectionProfileFields(),
                )
            }

            WhiteDnsParallelTest.settingProfileIdFromConfigId(configId)?.let { profileId ->
                val profile = advancedProfiles.firstOrNull { it.id == profileId } ?: return@mapNotNull null
                return@mapNotNull AutoTuneTrialConfig(
                    id = configId,
                    label = profile.name.ifBlank { "Setting" },
                    userSettings = baseSettings
                        .applyAdvancedProfile(profile)
                        .copy(
                            autoTuneEnabled = true,
                            parallelTestSelectedConfigIds = selectedConfigIds,
                        )
                        .syncSelectedConnectionProfileFields(),
                )
            }

            null
        }.take(WhiteDnsParallelTest.MaxSelectedConfigs)
    }

    private suspend fun startParallelAutoTuneTrial(
        plan: AutoTuneTrialPlan,
        manager: StormDnsProcessManager,
        serverProfile: StormDnsServerProfile,
    ): AutoTuneTrialStartup {
        val startupFailure = AtomicReference<String?>(null)
        return try {
            withContext(Dispatchers.Main.immediate) {
                updateAutoTuneTrialResult(plan.result.copy(status = "starting", message = "Starting SOCKS proxy"))
            }
            manager.start(serverProfile, plan.settings) { line ->
                detectStormDnsStartupFailure(line)?.let { failure ->
                    startupFailure.compareAndSet(null, failure)
                }
            }
            val ready = waitForAutoTuneTrialReady(
                manager = manager,
                listenPort = plan.result.listenPort,
                startupFailure = startupFailure,
            )
            if (!ready) {
                val failureMessage = startupFailure.get() ?: "SOCKS proxy did not become ready"
                val failedResult = plan.result.copy(status = "failed", message = failureMessage)
                withContext(Dispatchers.Main.immediate) {
                    updateAutoTuneTrialResult(failedResult)
                    updateAutoTuneProgress()
                }
                return AutoTuneTrialStartup(
                    plan = plan,
                    manager = manager,
                    ready = false,
                    result = autoTuneResultForPlan(plan, ready = false),
                )
            }

            withContext(Dispatchers.Main.immediate) {
                updateAutoTuneTrialResult(plan.result.copy(status = "listening", message = "SOCKS proxy ready"))
            }
            AutoTuneTrialStartup(
                plan = plan,
                manager = manager,
                ready = true,
                result = autoTuneResultForPlan(plan, ready = true),
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            val message = error.message ?: error::class.java.simpleName
            withContext(Dispatchers.Main.immediate) {
                updateAutoTuneTrialResult(plan.result.copy(status = "failed", message = message))
                updateAutoTuneProgress()
            }
            AutoTuneTrialStartup(
                plan = plan,
                manager = manager,
                ready = false,
                result = autoTuneResultForPlan(plan, ready = false),
            )
        }
    }

    private suspend fun measureParallelAutoTuneTrial(startup: AutoTuneTrialStartup): AutoTuneResult {
        val plan = startup.plan
        withContext(Dispatchers.Main.immediate) {
            updateAutoTuneTrialResult(plan.result.copy(status = "measuring", message = "Measuring speed"))
        }
        val probeResult = WhiteDnsTrafficWarmup.measureDownloadThroughput(plan.settings.resolve())
        val score = probeResult?.bytesPerSecond ?: 0L
        val pingMillis = probeResult?.latencyMillis
        val completedResult = plan.result.copy(
            status = "ready",
            speedBytesPerSecond = score,
            pingMillis = pingMillis,
            message = if (score > 0L) "Measured" else "No speed result",
        )
        withContext(Dispatchers.Main.immediate) {
            updateAutoTuneTrialResult(completedResult)
            updateAutoTuneProgress()
        }
        appendLog(
            "Parallel Test: ${plan.config.label} ${plan.result.listenIp}:${plan.result.listenPort} " +
                "${formatTrafficSpeed(score)}, ping ${formatAutoTuneLatency(pingMillis)}",
        )
        return AutoTuneResult(
            config = plan.config,
            listenIp = plan.result.listenIp,
            listenPort = plan.result.listenPort,
            scoreBytesPerSecond = score,
            pingMillis = pingMillis,
            ready = true,
        )
    }

    private suspend fun waitForAutoTuneTrialReady(
        manager: StormDnsProcessManager,
        listenPort: Int,
        startupFailure: AtomicReference<String?>,
    ): Boolean {
        val deadline = System.currentTimeMillis() + AutoTuneReadyTimeoutMillis
        while (System.currentTimeMillis() < deadline) {
            if (startupFailure.get() != null) {
                return false
            }
            if (!manager.isRunning()) {
                return false
            }
            if (canConnectToLocalPort(listenPort)) {
                return true
            }
            delay(AutoTuneSignalPollMillis)
        }
        return false
    }

    private fun updateAutoTuneTrialResult(result: AutoTuneTrialResult) {
        val currentResults = uiState.autoTuneTrialResults
        val resultIndex = currentResults.indexOfFirst { it.configId == result.configId }
        val nextResults = if (resultIndex >= 0) {
            currentResults.toMutableList().also { results ->
                results[resultIndex] = result
            }
        } else {
            currentResults + result
        }
        uiState = uiState.copy(autoTuneTrialResults = nextResults)
    }

    private fun updateAutoTuneProgress() {
        val results = uiState.autoTuneTrialResults
        val completed = results.count { it.status == "ready" || it.status == "failed" }
        val total = results.size.coerceAtLeast(1)
        uiState = uiState.copy(
            connectionProgress = ConnectionProgressState(
                phase = "autotune",
                percent = ((completed * 100) / total).coerceIn(5, 99),
                completed = completed,
                total = results.size,
            ),
            connectionVerification = ConnectionVerificationState(
                status = ConnectionVerificationStatus.Checking,
                message = "Parallel Test: measured $completed/${results.size} SOCKS configurations",
            ),
        )
    }

    private fun autoTuneResultForPlan(
        plan: AutoTuneTrialPlan,
        ready: Boolean,
    ): AutoTuneResult {
        return AutoTuneResult(
            config = plan.config,
            listenIp = plan.result.listenIp,
            listenPort = plan.result.listenPort,
            scoreBytesPerSecond = 0L,
            pingMillis = null,
            ready = ready,
        )
    }

    private fun allocateRandomLocalPorts(count: Int): List<Int> {
        val ports = linkedSetOf<Int>()
        val blockedPorts = setOf(
            WhiteDnsRuntimeProxy.ListenPortInt,
            WhiteDnsRuntimeProxy.HttpProxyPortInt,
            WhiteDnsRuntimeProxy.LocalDnsPortInt,
        )
        while (ports.size < count) {
            val port = ServerSocket(0).use { socket ->
                socket.reuseAddress = true
                socket.localPort
            }
            if (port !in blockedPorts) {
                ports += port
            }
        }
        return ports.toList()
    }

    private fun setAutoTuneTrialManagers(managers: List<StormDnsProcessManager>) {
        synchronized(autoTuneTrialManagersLock) {
            autoTuneTrialManagers = managers
        }
    }

    private fun stopAutoTuneTrialManagers() {
        val managers = synchronized(autoTuneTrialManagersLock) {
            autoTuneTrialManagers.also {
                autoTuneTrialManagers = emptyList()
            }
        }
        managers.forEach { manager ->
            runCatching {
                manager.stop()
            }
        }
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

    private fun formatAutoTuneLatency(pingMillis: Long?): String {
        return pingMillis?.let { "${it}ms" } ?: "n/a"
    }

    fun disconnect() {
        connectJob?.cancel()
        statsJob?.cancel()
        runtimeRefreshJob?.cancel()
        verificationJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            stopAutoTuneTrialManagers()
            stopAllRuntimeServices()
            if (uiState.settings.resolve().connectionMode == "vpn") {
                delay(VpnStopBeforeStormDnsStopDelayMillis)
            }
        }
        activeProxyListenPort = WhiteDnsRuntimeProxy.ListenPortInt
        activeVpnTrafficInterfaceName = null
        activeRuntimeSessionId = ""
        resetTrafficAccounting()
        resetSocksStreamTracker()
        resetRuntimeUiThrottles()
        appendLog("Disconnected")
        uiState = uiState.copy(
            connectionStatus = ConnectionStatus.DISCONNECTED,
            connectionStats = ConnectionStats(),
            resolverRuntimeState = ResolverRuntimeState(),
            connectionProgress = ConnectionProgressState(),
            connectionVerification = ConnectionVerificationState(),
            autoTuneTrialResults = emptyList(),
            activeConnectionProfileId = null,
        )
    }

    fun updateScanWorkerCount(rawValue: String) {
        val filtered = rawValue.filter(Char::isDigit).take(MaxScanWorkerDigits)
        val workerCount = filtered.toIntOrNull()
        if (workerCount != null && workerCount > 0) {
            scanSettingsStore.saveWorkerCount(workerCount)
        }
        uiState = uiState.copy(scanWorkerCount = filtered)
    }

    fun updateScanConnectionProfile(profileId: String) {
        if (uiState.scanState.isRunning) {
            return
        }
        val scanConnectionProfileId = resolveScanConnectionProfileId(
            settings = uiState.settings,
            requestedProfileId = profileId,
        )
        scanSettingsStore.saveConnectionProfileId(scanConnectionProfileId)
        uiState = uiState.copy(scanConnectionProfileId = scanConnectionProfileId)
    }

    fun beginScanFromFile(uri: Uri) {
        beginScanFromSource(sourceName = "Resolver file") { sessionId ->
            importScanResolverFile(uri, sessionId)
        }
    }

    fun beginScanFromDefaultResolvers() {
        beginScanFromSource(sourceName = "Default resolver list") { sessionId ->
            importDefaultScanResolverFile(sessionId)
        }
    }

    private fun beginScanFromSource(
        sourceName: String,
        importSource: (String) -> ImportedScanResolverFile,
    ) {
        if (uiState.scanState.isRunning) {
            return
        }
        scanLaunchJob?.cancel()
        scanLaunchJob = viewModelScope.launch {
            val sessionId = UUID.randomUUID().toString()
            try {
                val baseSettings = uiState.settings.syncSelectedConnectionProfileFields()
                val scanConnectionProfileId = resolveScanConnectionProfileId(
                    settings = baseSettings,
                    requestedProfileId = uiState.scanConnectionProfileId,
                )
                if (scanConnectionProfileId != uiState.scanConnectionProfileId) {
                    scanSettingsStore.saveConnectionProfileId(scanConnectionProfileId)
                    uiState = uiState.copy(scanConnectionProfileId = scanConnectionProfileId)
                }
                val workerCount = uiState.scanWorkerCount.toIntOrNull()
                    ?.coerceAtLeast(1)
                    ?: WhiteDnsScanDefaults.DefaultWorkerCount
                val importingState = WhiteDnsScanState(
                    sessionId = sessionId,
                    status = WhiteDnsScanStatus.Starting,
                    sourceName = sourceName,
                    workerCount = workerCount,
                    updatedAtMillis = System.currentTimeMillis(),
                    message = "Importing $sourceName",
                )
                withContext(Dispatchers.IO) {
                    WhiteDnsScanStateStore.write(appContext, importingState)
                }
                uiState = uiState.copy(scanState = importingState)

                val imported = withContext(Dispatchers.IO) {
                    importSource(sessionId)
                }

                withContext(Dispatchers.IO) {
                    WhiteDnsScanRequestStore.save(
                        context = appContext,
                        request = WhiteDnsScanLaunchRequest(
                            id = sessionId,
                            sourceName = imported.sourceName,
                            resolverFilePath = imported.file.absolutePath,
                            workerCount = workerCount.coerceAtLeast(1),
                            initialValidResolvers = emptyList(),
                            initialCompletedResolvers = 0,
                            totalResolvers = imported.pendingResolverCount,
                        ),
                    )
                }

                val readyStatus = if (imported.pendingResolverCount > 0) {
                    WhiteDnsScanStatus.Ready
                } else {
                    WhiteDnsScanStatus.Completed
                }
                val readyState = WhiteDnsScanState(
                    sessionId = sessionId,
                    status = readyStatus,
                    sourceName = imported.sourceName,
                    totalResolvers = imported.displayResolverCount,
                    completedResolvers = if (imported.pendingResolverCount > 0) 0 else imported.displayResolverCount,
                    validResolvers = if (imported.pendingResolverCount > 0) 0 else imported.alreadyValidResolverCount,
                    rejectedResolvers = 0,
                    workerCount = workerCount.coerceAtMost(imported.pendingResolverCount.coerceAtLeast(1)),
                    updatedAtMillis = System.currentTimeMillis(),
                    message = buildPreparedScanMessage(imported),
                    validResolverEntries = emptyList(),
                    rejectedResolverEntries = emptyList(),
                )
                withContext(Dispatchers.IO) {
                    WhiteDnsScanStateStore.write(appContext, readyState)
                }
                uiState = uiState.copy(scanState = readyState)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setScanFailure(sessionId, "Resolver import failed: ${error.message ?: error::class.java.simpleName}")
            }
        }
    }

    fun startPreparedScan() {
        if (uiState.scanState.isRunning) {
            return
        }
        scanLaunchJob?.cancel()
        scanLaunchJob = viewModelScope.launch {
            val previousState = uiState.scanState
            val sessionId = previousState.sessionId
            if (
                sessionId.isBlank() ||
                previousState.status != WhiteDnsScanStatus.Ready ||
                previousState.completedResolvers >= previousState.totalResolvers
            ) {
                return@launch
            }
            val scanRequest = withContext(Dispatchers.IO) {
                WhiteDnsScanRequestStore.load(appContext, sessionId)
            } ?: run {
                setScanFailure(sessionId, "Scan start failed: resolver file is missing")
                return@launch
            }
            val baseSettings = uiState.settings.syncSelectedConnectionProfileFields()
            val scanConnectionProfileId = resolveScanConnectionProfileId(
                settings = baseSettings,
                requestedProfileId = uiState.scanConnectionProfileId,
            )
            if (scanConnectionProfileId != uiState.scanConnectionProfileId) {
                scanSettingsStore.saveConnectionProfileId(scanConnectionProfileId)
                uiState = uiState.copy(scanConnectionProfileId = scanConnectionProfileId)
            }
            val settings = baseSettings.copy(
                selectedConnectionProfileId = scanConnectionProfileId,
            ).syncSelectedConnectionProfileFields()
            val serverProfile = selectServerProfile(settings)
            if (serverProfile == null) {
                val profileName = settings.selectedConnectionProfile().name.ifBlank { "selected scan profile" }
                setScanFailure(sessionId, "$profileName requires a StormDNS domain and encryption key")
                return@launch
            }
            val workerCount = uiState.scanWorkerCount.toIntOrNull()
                ?.coerceAtLeast(1)
                ?: scanRequest.workerCount.coerceAtLeast(1)
            val resolverFileExists = withContext(Dispatchers.IO) {
                File(scanRequest.resolverFilePath).isFile
            }
            if (!resolverFileExists) {
                setScanFailure(sessionId, "Scan start failed: resolver file is missing")
                return@launch
            }
            val pendingResolverCount = previousState.totalResolvers
                .takeIf { it > 0 }
                ?: scanRequest.totalResolvers
            if (pendingResolverCount == 0) {
                val completedState = previousState.copy(
                    status = WhiteDnsScanStatus.Completed,
                    updatedAtMillis = System.currentTimeMillis(),
                    message = "No new resolvers to scan; Scanner result is up to date",
                )
                WhiteDnsScanStateStore.write(appContext, completedState)
                uiState = uiState.copy(scanState = completedState)
                return@launch
            }
            val nowMillis = System.currentTimeMillis()
            val startingState = previousState.copy(
                status = WhiteDnsScanStatus.Starting,
                workerCount = workerCount.coerceAtMost(pendingResolverCount.coerceAtLeast(1)),
                startedAtMillis = nowMillis,
                updatedAtMillis = nowMillis,
                message = "Starting scan",
            )
            withContext(Dispatchers.IO) {
                RuntimeLaunchRequestStore.save(
                    context = appContext,
                    requestId = sessionId,
                    serverProfile = serverProfile,
                    settings = settings,
                )
                WhiteDnsScanRequestStore.save(
                    context = appContext,
                    request = scanRequest.copy(
                        workerCount = workerCount,
                        initialValidResolvers = previousState.validResolverEntries,
                        initialRejectedResolvers = previousState.rejectedResolverEntries,
                        initialCompletedResolvers = previousState.completedResolvers,
                        totalResolvers = previousState.totalResolvers,
                    ),
                )
            }
            WhiteDnsScanStateStore.write(appContext, startingState)
            uiState = uiState.copy(scanState = startingState)

            withContext(Dispatchers.IO) {
                WhiteDnsScanService.startPrepared(appContext, sessionId)
            }
        }
    }

    fun stopScan() {
        scanLaunchJob?.cancel()
        if (uiState.scanState.isRunning) {
            val stoppedState = uiState.scanState.copy(
                status = WhiteDnsScanStatus.Stopped,
                updatedAtMillis = System.currentTimeMillis(),
                message = "Scan stopped",
            )
            WhiteDnsScanStateStore.write(appContext, stoppedState)
            uiState = uiState.copy(scanState = stoppedState)
        }
        viewModelScope.launch(Dispatchers.IO) {
            WhiteDnsScanService.stop(appContext)
        }
    }

    fun refreshScanState() {
        val persistedState = WhiteDnsScanStateStore.read(appContext)
        val scanState = persistedState.recoverIfStale(
            nowMillis = System.currentTimeMillis(),
            staleAfterMillis = StaleScanStateTimeoutMillis,
        )
        if (scanState != persistedState) {
            WhiteDnsScanStateStore.write(appContext, scanState)
        }
        val updatedSettings = syncScannerResultProfile()
        uiState = uiState.copy(
            settings = updatedSettings ?: uiState.settings,
            scanState = scanState,
        )
    }

    fun resumeScan() {
        if (uiState.scanState.isRunning) {
            return
        }
        scanLaunchJob?.cancel()
        scanLaunchJob = viewModelScope.launch {
            val previousState = uiState.scanState
            val previousSessionId = previousState.sessionId
            if (previousSessionId.isBlank()) {
                return@launch
            }
            val runtimeRequest = withContext(Dispatchers.IO) {
                RuntimeLaunchRequestStore.load(appContext, previousSessionId)
            } ?: run {
                setScanFailure(previousSessionId, "Scan resume failed: launch settings are missing")
                return@launch
            }
            val scanRequest = withContext(Dispatchers.IO) {
                WhiteDnsScanRequestStore.load(appContext, previousSessionId)
            } ?: run {
                setScanFailure(previousSessionId, "Scan resume failed: resolver file is missing")
                return@launch
            }
            val validEntries = WhiteDnsScannerResultStore.normalizeResolverEntries(previousState.validResolverEntries)
            val rejectedEntries = WhiteDnsScannerResultStore.normalizeResolverEntries(previousState.rejectedResolverEntries)
                .filterNot(validEntries::contains)
            val processed = (validEntries + rejectedEntries).toSet()
            val sessionId = UUID.randomUUID().toString()
            val resolverFile = File(
                File(File(appContext.noBackupFilesDir, "stormdns/scan"), sessionId).apply { mkdirs() },
                "resume.resolvers",
            )
            val remainingResolverCount = withContext(Dispatchers.IO) {
                runCatching {
                    val excludedResolvers = processed + WhiteDnsScannerResultStore.readValidResolverSet(appContext)
                    File(scanRequest.resolverFilePath).bufferedReader(Charsets.UTF_8).useLines { lines ->
                        WhiteDnsScannerResultStore.writePendingScanResolverFile(
                            lines = lines,
                            outputFile = resolverFile,
                            excludedResolvers = excludedResolvers,
                        )
                    }.pendingResolverCount
                }
            }.getOrElse { error ->
                setScanFailure(previousSessionId, "Scan resume failed: ${error.message ?: error::class.java.simpleName}")
                return@launch
            }
            if (remainingResolverCount == 0) {
                val updatedState = previousState.copy(
                    updatedAtMillis = System.currentTimeMillis(),
                    message = "No remaining resolvers to resume",
                )
                WhiteDnsScanStateStore.write(appContext, updatedState)
                uiState = uiState.copy(scanState = updatedState)
                return@launch
            }

            val workerCount = uiState.scanWorkerCount.toIntOrNull()
                ?.coerceAtLeast(1)
                ?: scanRequest.workerCount.coerceAtLeast(1)
            val startingState = WhiteDnsScanState(
                sessionId = sessionId,
                status = WhiteDnsScanStatus.Starting,
                sourceName = previousState.sourceName.ifBlank { scanRequest.sourceName },
                totalResolvers = remainingResolverCount + processed.size,
                completedResolvers = processed.size,
                validResolvers = validEntries.size,
                rejectedResolvers = rejectedEntries.size,
                workerCount = workerCount.coerceAtMost(remainingResolverCount.coerceAtLeast(1)),
                startedAtMillis = System.currentTimeMillis(),
                updatedAtMillis = System.currentTimeMillis(),
                message = "Resuming scan",
                validResolverEntries = validEntries,
                rejectedResolverEntries = rejectedEntries,
            )
            WhiteDnsScanStateStore.write(appContext, startingState)
            uiState = uiState.copy(scanState = startingState)

            withContext(Dispatchers.IO) {
                WhiteDnsScanService.start(
                    context = appContext,
                    sessionId = sessionId,
                    serverProfile = runtimeRequest.serverProfile,
                    settings = runtimeRequest.settings,
                    sourceName = startingState.sourceName,
                    resolverFile = resolverFile,
                    workerCount = workerCount,
                    initialValidResolvers = validEntries,
                    initialRejectedResolvers = rejectedEntries,
                )
            }
        }
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
        readinessJob?.cancel()
        scanLaunchJob?.cancel()
        stopAutoTuneTrialManagers()
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
        ContextCompat.registerReceiver(
            appContext,
            scanBroadcastReceiver,
            IntentFilter(WhiteDnsScanService.BroadcastAction),
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
        runCatching {
            appContext.unregisterReceiver(scanBroadcastReceiver)
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
            stormDnsTrafficAccounting.record(trafficStats)
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
            resetTrafficAccounting()
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
            resetTrafficAccounting()
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
        resetTrafficAccounting()
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
        resetTrafficAccounting()
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
        val cleanMessage = redactRuntimeText(message).trim()
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

    private fun setScanFailure(sessionId: String, message: String) {
        val failedState = WhiteDnsScanState(
            sessionId = sessionId,
            status = WhiteDnsScanStatus.Failed,
            updatedAtMillis = System.currentTimeMillis(),
            message = message,
        )
        WhiteDnsScanStateStore.write(appContext, failedState)
        uiState = uiState.copy(scanState = failedState)
    }

    private fun importScanResolverFile(uri: Uri, sessionId: String): ImportedScanResolverFile {
        val scanDir = File(File(appContext.noBackupFilesDir, "stormdns/scan"), sessionId).apply {
            mkdirs()
        }
        val resolverFile = File(scanDir, "input.resolvers")
        val summary = appContext.contentResolver.openInputStream(uri)
            ?.bufferedReader(Charsets.UTF_8)
            ?.useLines { lines ->
                WhiteDnsScannerResultStore.writePendingScanResolverFile(
                    lines = lines,
                    outputFile = resolverFile,
                )
            }
            ?: throw IllegalArgumentException("Unable to open resolver file")
        if (summary.totalResolverCount == 0) {
            throw IllegalArgumentException("No valid resolver entries found in file")
        }
        return ImportedScanResolverFile(
            file = resolverFile,
            sourceName = displayNameForUri(uri),
            pendingResolverCount = summary.pendingResolverCount,
            totalResolverCount = summary.totalResolverCount,
            alreadyValidResolverCount = summary.alreadyValidResolverCount,
            invalidEntryCount = summary.invalidEntryCount,
        )
    }

    private fun importDefaultScanResolverFile(sessionId: String): ImportedScanResolverFile {
        val scanDir = File(File(appContext.noBackupFilesDir, "stormdns/scan"), sessionId).apply {
            mkdirs()
        }
        val resolverFile = File(scanDir, "default.resolvers")
        val summary = appContext.assets.open(DefaultScanResolverAssetName)
            .bufferedReader(Charsets.UTF_8)
            .useLines { lines ->
                WhiteDnsScannerResultStore.writePendingScanResolverFile(
                    lines = lines,
                    outputFile = resolverFile,
                )
            }
        if (summary.totalResolverCount == 0) {
            throw IllegalArgumentException("No valid resolver entries found in default list")
        }
        return ImportedScanResolverFile(
            file = resolverFile,
            sourceName = "Default resolver list",
            pendingResolverCount = summary.pendingResolverCount,
            totalResolverCount = summary.totalResolverCount,
            alreadyValidResolverCount = summary.alreadyValidResolverCount,
            invalidEntryCount = summary.invalidEntryCount,
        )
    }

    private fun buildPreparedScanMessage(imported: ImportedScanResolverFile): String {
        val parts = mutableListOf<String>()
        parts += "Imported ${imported.totalResolverCount} resolver${if (imported.totalResolverCount == 1) "" else "s"}"
        if (imported.pendingResolverCount > 0) {
            parts += "Ready to scan ${imported.pendingResolverCount} entr${if (imported.pendingResolverCount == 1) "y" else "ies"}"
        } else {
            parts += "No new resolvers to scan"
        }
        if (imported.alreadyValidResolverCount > 0) {
            parts += "skipped ${imported.alreadyValidResolverCount} already in Scanner result"
        }
        if (imported.invalidEntryCount > 0) {
            parts += "ignored ${imported.invalidEntryCount} invalid entries"
        }
        return parts.joinToString(separator = "\n")
    }

    private fun syncScannerResultProfile(): WhiteDnsSettings? {
        val scannerResultText = WhiteDnsScannerResultStore.readValidResolvers(appContext)
            .joinToString(separator = "\n")
        if (scannerResultText.isBlank() || scannerResultText == lastScannerResultProfileText) {
            return null
        }

        val currentSettings = uiState.settings.syncSelectedConnectionProfileFields()
        val resolverProfiles = currentSettings.normalizedResolverProfiles()
        val existingIndex = resolverProfiles.indexOfFirst { it.name == ScannerResultProfileName }
        if (existingIndex >= 0 && resolverProfiles[existingIndex].resolverText == scannerResultText) {
            lastScannerResultProfileText = scannerResultText
            return null
        }

        val scannerResultProfile = if (existingIndex >= 0) {
            resolverProfiles[existingIndex].copy(resolverText = scannerResultText)
        } else {
            ResolverProfile(
                id = ResolverProfile.newId(),
                name = ScannerResultProfileName,
                resolverText = scannerResultText,
            )
        }
        val updatedProfiles = if (existingIndex >= 0) {
            resolverProfiles.toMutableList().also { profiles ->
                profiles[existingIndex] = scannerResultProfile
            }
        } else {
            resolverProfiles + scannerResultProfile
        }
        val updatedSettings = currentSettings.copy(
            resolverProfiles = updatedProfiles,
        ).syncSelectedConnectionProfileFields()
        settingsStore.save(updatedSettings)
        lastScannerResultProfileText = scannerResultText
        return updatedSettings
    }

    private fun displayNameForUri(uri: Uri): String {
        return runCatching {
            appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index >= 0) {
                            cursor.getString(index)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
        }.getOrNull()
            ?.takeIf(String::isNotBlank)
            ?: uri.lastPathSegment?.substringAfterLast('/')?.takeIf(String::isNotBlank)
            ?: "Resolver file"
    }

    private fun resolveScanConnectionProfileId(
        settings: WhiteDnsSettings,
        requestedProfileId: String,
    ): String {
        val normalizedSettings = settings.syncSelectedConnectionProfileFields()
        val profiles = normalizedSettings.normalizedConnectionProfiles()
        return profiles.firstOrNull { it.id == requestedProfileId }?.id
            ?: normalizedSettings.selectedConnectionProfile().id
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

    private fun verifyProfileReadiness(settings: WhiteDnsSettings): ConnectionVerificationState {
        val normalizedSettings = settings.syncSelectedConnectionProfileFields()
        val validation = validateConnectionSettings(normalizedSettings)
        if (!validation.canConnect) {
            return failedVerification("Setup needs attention: ${validation.fatalIssues.first().message}")
        }

        val runtimeSettings = normalizedSettings.runtimeConnectionSettings()
        val resolvedSettings = runtimeSettings.resolve()
        val connectionProfile = normalizedSettings.selectedConnectionProfile()
        val serverDomains = normalizeServerDomains(connectionProfile.customServerDomain)
        val resolvedDomainCount = serverDomains.count { domain -> hasServerAddressRecord(domain) }

        val busyPorts = buildList {
            add("SOCKS ${resolvedSettings.listenPort}" to resolvedSettings.listenPort)
            if (resolvedSettings.httpProxyEnabled) {
                add("HTTP ${resolvedSettings.httpProxyPort}" to resolvedSettings.httpProxyPort)
            }
            if (resolvedSettings.localDnsEnabled) {
                add("DNS ${resolvedSettings.localDnsPort}" to resolvedSettings.localDnsPort)
            }
        }.filter { (_, port) -> canConnectToLocalPort(port) }
        if (busyPorts.isNotEmpty()) {
            return failedVerification(
                "Setup needs attention: local ${busyPorts.joinToString { it.first }} already appears to be in use",
            )
        }

        val domainSummary = if (serverDomains.size <= 1) {
            if (resolvedDomainCount == 1) {
                "server address lookup is confirmed"
            } else {
                "server domain is configured"
            }
        } else {
            "$resolvedDomainCount/${serverDomains.size} server address lookups confirmed"
        }
        val resolverCount = resolvedSettings.resolverEntries.size
        val warnings = buildList {
            addAll(validation.warnings.take(2).map { issue -> issue.message })
            if (serverDomains.isNotEmpty() && resolvedDomainCount == 0) {
                add("normal server address lookup was not confirmed; the route check will prove the live tunnel after connect")
            }
        }.joinToString("; ")
        val warningSuffix = if (warnings.isBlank()) "" else " Warning: $warnings."
        return ConnectionVerificationState(
            status = ConnectionVerificationStatus.Verified,
            message = "Setup ready: $domainSummary, $resolverCount resolver${if (resolverCount == 1) "" else "s"}, and local ports are free. Connect next for the route check.$warningSuffix",
            checkedAtMillis = System.currentTimeMillis(),
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

    private fun hasServerAddressRecord(domain: String): Boolean {
        val lookupDomain = domain.trim().removeSurrounding("[", "]")
        if (lookupDomain.isBlank()) {
            return false
        }
        return runCatching {
            InetAddress.getAllByName(lookupDomain).isNotEmpty()
        }.getOrDefault(false)
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
        stormDnsTrafficAccounting.latest()?.let { stats ->
            val resolvedSettings = uiState.settings.resolve()
            val estimatedPayloadStats = stats.estimateDeduplicatedTraffic(
                uploadDuplication = resolvedSettings.uploadDuplication,
                downloadDuplication = resolvedSettings.downloadDuplication,
            )
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
                estimatedPayloadDownloadBytes = estimatedPayloadStats.downloadBytes,
                estimatedPayloadUploadBytes = estimatedPayloadStats.uploadBytes,
                estimatedPayloadTotalBytes = estimatedPayloadStats.downloadBytes + estimatedPayloadStats.uploadBytes,
                estimatedPayloadDownloadSpeedBytesPerSecond = estimatedPayloadStats.downloadSpeedBytesPerSecond,
                estimatedPayloadUploadSpeedBytesPerSecond = estimatedPayloadStats.uploadSpeedBytesPerSecond,
                hasEstimatedPayloadTraffic = resolvedSettings.uploadDuplication > 1 ||
                    resolvedSettings.downloadDuplication > 1,
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
            estimatedPayloadDownloadBytes = downloadBytes,
            estimatedPayloadUploadBytes = uploadBytes,
            estimatedPayloadTotalBytes = downloadBytes + uploadBytes,
            estimatedPayloadDownloadSpeedBytesPerSecond = downloadSpeed,
            estimatedPayloadUploadSpeedBytesPerSecond = uploadSpeed,
            hasEstimatedPayloadTraffic = false,
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
        val currentState = uiState.resolverRuntimeState
        val nextState = resolverState.withMergedValidResolvers(currentState.validResolvers)
        if (nextState == currentState) {
            return
        }
        val now = System.currentTimeMillis()
        val firstVisibleState = currentState == ResolverRuntimeState()
        if (!firstVisibleState && now - lastResolverUiUpdateMillis < RuntimeResolverUiUpdateIntervalMillis) {
            return
        }
        lastResolverUiUpdateMillis = now
        uiState = uiState.copy(resolverRuntimeState = nextState)
    }

    private fun ResolverRuntimeState.withMergedValidResolvers(
        currentValidResolvers: List<String>,
    ): ResolverRuntimeState {
        if (currentValidResolvers.isEmpty()) {
            return this
        }
        val mergedValidResolvers = (currentValidResolvers + validResolvers).distinct()
        return copy(validResolvers = mergedValidResolvers)
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

    private fun resetTrafficAccounting() {
        stormDnsTrafficAccounting.reset()
    }

    private fun pruneTrackedSocksStreamsLocked(now: Long) {
        socksStreamLastSeenMillis.entries.removeAll { (_, lastSeenMillis) ->
            now - lastSeenMillis > SocksStreamTrackingTtlMillis
        }
    }

    private fun Long.normalizeTrafficCounter(): Long {
        return if (this == TrafficStats.UNSUPPORTED.toLong()) 0 else coerceAtLeast(0)
    }

    private data class ImportedScanResolverFile(
        val file: File,
        val sourceName: String,
        val pendingResolverCount: Int,
        val totalResolverCount: Int,
        val alreadyValidResolverCount: Int,
        val invalidEntryCount: Int,
    ) {
        val displayResolverCount: Int
            get() = if (pendingResolverCount > 0) pendingResolverCount else totalResolverCount
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
        val cleanMessage = redactRuntimeText(message).trim()
        if (cleanMessage.isEmpty()) {
            return
        }
        val nextLogs = (listOf(cleanMessage) + uiState.connectionLogs).take(MaxConnectionLogs)
        uiState = uiState.copy(connectionLogs = nextLogs)
    }

    private fun redactRuntimeText(message: String): String {
        val settings = uiState.settings
        val profiles = settings.normalizedConnectionProfiles()
        val resolvedSettings = settings.resolve()
        return SecretRedactor.redact(
            source = message,
            secrets = RedactionSecrets(
                serverDomains = profiles.map { it.customServerDomain },
                encryptionKeys = profiles.map { it.customServerEncryptionKey },
                socksUsernames = listOf(resolvedSettings.socksUsername),
                socksPasswords = listOf(resolvedSettings.socksPassword),
            ),
        )
    }

    private companion object {
        const val MaxConnectionLogs = 100
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
        const val AutoTuneReadyTimeoutMillis = 90_000L
        const val AutoTuneSignalPollMillis = 100L
        const val AutoTuneMeasurementSettleMillis = 1_500L
        const val MaxScanWorkerDigits = 3
        const val StaleScanStateTimeoutMillis = 15_000L
        const val ScannerResultProfileName = "Scanner result"
        const val DefaultScanResolverAssetName = "default_resolvers.txt"
        const val UidTrafficSourcePrefix = "uid:"
        const val VpnTrafficSourcePrefix = "vpn:"
        val ParallelTestConnectionModes = setOf(
            WhiteDnsRuntimeStateStore.ModeProxy,
            WhiteDnsRuntimeStateStore.ModeVpn,
        )
        val socksStreamOpenedRegex = Regex("""New SOCKS\d TCP CONNECT .*Stream ID:\s*(\d+)""")
        val socksStreamClosedRegex = Regex("""ARQ Stream Closed .*Stream:\s*(\d+)""")
        val SafeNetworkInterfaceNameRegex = Regex("""[A-Za-z0-9_.:-]+""")
    }

    private data class AutoTuneTrialConfig(
        val id: String,
        val label: String,
        val userSettings: WhiteDnsSettings,
    )

    private data class AutoTuneTrialPlan(
        val config: AutoTuneTrialConfig,
        val settings: WhiteDnsSettings,
        val result: AutoTuneTrialResult,
    )

    private data class AutoTuneTrialStartup(
        val plan: AutoTuneTrialPlan,
        val manager: StormDnsProcessManager,
        val ready: Boolean,
        val result: AutoTuneResult,
    )

    private data class AutoTuneResult(
        val config: AutoTuneTrialConfig,
        val listenIp: String,
        val listenPort: Int,
        val scoreBytesPerSecond: Long,
        val pingMillis: Long?,
        val ready: Boolean,
    )

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
