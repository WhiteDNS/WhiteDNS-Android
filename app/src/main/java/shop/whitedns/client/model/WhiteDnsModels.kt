package shop.whitedns.client.model

import java.io.Serializable
import java.net.InetAddress

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
}

data class Choice<T>(
    val value: T,
    val label: String,
)

data class StormDnsServerProfile(
    val id: String,
    val label: String,
    val domain: String,
    val encryptionKey: String,
    val encryptionMethod: Int,
)

data class ConnectionProfile(
    val id: String,
    val name: String,
    val serverMode: String = "custom",
    val customServerDomain: String = "",
    val customServerEncryptionKey: String = "",
    val customServerEncryptionMethod: Int = 1,
    val resolverProfileId: String = "",
    val connectionMode: String = "proxy",
) : Serializable {
    companion object {
        const val DefaultId = "default"

        fun defaultProfile(): ConnectionProfile {
            return ConnectionProfile(
                id = DefaultId,
                name = "Connection",
                serverMode = "custom",
            )
        }

        fun fromSettings(settings: WhiteDnsSettings): ConnectionProfile {
            return ConnectionProfile(
                id = DefaultId,
                name = "Connection",
                serverMode = "custom",
                customServerDomain = settings.customServerDomain,
                customServerEncryptionKey = settings.customServerEncryptionKey,
                customServerEncryptionMethod = settings.customServerEncryptionMethod,
                resolverProfileId = settings.selectedResolverProfileId,
                connectionMode = settings.connectionMode,
            )
        }
    }
}

data class ResolverProfile(
    val id: String,
    val name: String,
    val resolverText: String,
) : Serializable {
    companion object {
        fun newId(): String = "resolver-${System.currentTimeMillis()}"
    }
}

data class AdvancedSettingsProfile(
    val id: String,
    val name: String,
    val listenIp: String,
    val listenPort: String,
    val httpProxyEnabled: Boolean,
    val httpProxyPort: String,
    val socks5Authentication: Boolean,
    val socksUsername: String,
    val socksPassword: String,
    val balancingStrategy: Int,
    val uploadDuplication: String,
    val downloadDuplication: String,
    val uploadCompression: Int,
    val downloadCompression: Int,
    val baseEncodeData: Boolean,
    val minUploadMtu: String,
    val minDownloadMtu: String,
    val maxUploadMtu: String,
    val maxDownloadMtu: String,
    val mtuTestRetriesResolvers: String,
    val mtuTestTimeoutResolvers: String,
    val mtuTestParallelismResolvers: String,
    val mtuTestRetriesLogs: String,
    val mtuTestTimeoutLogs: String,
    val mtuTestParallelismLogs: String,
    val rxTxWorkers: String,
    val tunnelProcessWorkers: String,
    val tunnelPacketTimeoutSeconds: String,
    val dispatcherIdlePollIntervalSeconds: String,
    val txChannelSize: String,
    val rxChannelSize: String,
    val resolverUdpConnectionPoolSize: String,
    val streamQueueInitialCapacity: String,
    val orphanQueueInitialCapacity: String,
    val dnsResponseFragmentStoreCapacity: String,
    val maxActiveStreams: String,
    val localHandshakeTimeoutSeconds: String,
    val socksUdpAssociateReadTimeoutSeconds: String,
    val clientTerminalStreamRetentionSeconds: String,
    val clientCancelledSetupRetentionSeconds: String,
    val sessionInitRetryBaseSeconds: String,
    val sessionInitRetryStepSeconds: String,
    val sessionInitRetryLinearAfter: String,
    val sessionInitRetryMaxSeconds: String,
    val sessionInitBusyRetryIntervalSeconds: String,
    val localDnsEnabled: Boolean,
    val localDnsPort: String,
    val startupMode: String,
    val pingWatchdogSeconds: String,
    val trafficWarmupMode: String,
    val trafficWarmupEnabled: Boolean,
    val trafficWarmupProbeCount: String,
    val trafficKeepaliveIntervalSeconds: String,
    val vpnIpv6Strategy: String,
    val vpnMtuPreset: String,
    val vpnCustomMtu: String,
    val logLevel: String,
) : Serializable {
    companion object {
        const val DefaultId = "advanced-default"

        fun defaultProfile(): AdvancedSettingsProfile {
            return fromSettings(
                settings = WhiteDnsSettings(),
                id = DefaultId,
                name = "Default",
            )
        }

        fun newId(): String = "advanced-${System.currentTimeMillis()}"

        fun fromSettings(
            settings: WhiteDnsSettings,
            id: String = "",
            name: String = "Advanced Settings",
        ): AdvancedSettingsProfile {
            return AdvancedSettingsProfile(
                id = id,
                name = name,
                listenIp = settings.listenIp,
                listenPort = settings.listenPort,
                httpProxyEnabled = settings.httpProxyEnabled,
                httpProxyPort = settings.httpProxyPort,
                socks5Authentication = settings.socks5Authentication,
                socksUsername = settings.socksUsername,
                socksPassword = settings.socksPassword,
                balancingStrategy = settings.balancingStrategy,
                uploadDuplication = settings.uploadDuplication,
                downloadDuplication = settings.downloadDuplication,
                uploadCompression = settings.uploadCompression,
                downloadCompression = settings.downloadCompression,
                baseEncodeData = settings.baseEncodeData,
                minUploadMtu = settings.minUploadMtu,
                minDownloadMtu = settings.minDownloadMtu,
                maxUploadMtu = settings.maxUploadMtu,
                maxDownloadMtu = settings.maxDownloadMtu,
                mtuTestRetriesResolvers = settings.mtuTestRetriesResolvers,
                mtuTestTimeoutResolvers = settings.mtuTestTimeoutResolvers,
                mtuTestParallelismResolvers = settings.mtuTestParallelismResolvers,
                mtuTestRetriesLogs = settings.mtuTestRetriesLogs,
                mtuTestTimeoutLogs = settings.mtuTestTimeoutLogs,
                mtuTestParallelismLogs = settings.mtuTestParallelismLogs,
                rxTxWorkers = settings.rxTxWorkers,
                tunnelProcessWorkers = settings.tunnelProcessWorkers,
                tunnelPacketTimeoutSeconds = settings.tunnelPacketTimeoutSeconds,
                dispatcherIdlePollIntervalSeconds = settings.dispatcherIdlePollIntervalSeconds,
                txChannelSize = settings.txChannelSize,
                rxChannelSize = settings.rxChannelSize,
                resolverUdpConnectionPoolSize = settings.resolverUdpConnectionPoolSize,
                streamQueueInitialCapacity = settings.streamQueueInitialCapacity,
                orphanQueueInitialCapacity = settings.orphanQueueInitialCapacity,
                dnsResponseFragmentStoreCapacity = settings.dnsResponseFragmentStoreCapacity,
                maxActiveStreams = settings.maxActiveStreams,
                localHandshakeTimeoutSeconds = settings.localHandshakeTimeoutSeconds,
                socksUdpAssociateReadTimeoutSeconds = settings.socksUdpAssociateReadTimeoutSeconds,
                clientTerminalStreamRetentionSeconds = settings.clientTerminalStreamRetentionSeconds,
                clientCancelledSetupRetentionSeconds = settings.clientCancelledSetupRetentionSeconds,
                sessionInitRetryBaseSeconds = settings.sessionInitRetryBaseSeconds,
                sessionInitRetryStepSeconds = settings.sessionInitRetryStepSeconds,
                sessionInitRetryLinearAfter = settings.sessionInitRetryLinearAfter,
                sessionInitRetryMaxSeconds = settings.sessionInitRetryMaxSeconds,
                sessionInitBusyRetryIntervalSeconds = settings.sessionInitBusyRetryIntervalSeconds,
                localDnsEnabled = settings.localDnsEnabled,
                localDnsPort = settings.localDnsPort,
                startupMode = settings.startupMode,
                pingWatchdogSeconds = settings.pingWatchdogSeconds,
                trafficWarmupMode = settings.trafficWarmupMode,
                trafficWarmupEnabled = settings.trafficWarmupEnabled,
                trafficWarmupProbeCount = settings.trafficWarmupProbeCount,
                trafficKeepaliveIntervalSeconds = settings.trafficKeepaliveIntervalSeconds,
                vpnIpv6Strategy = settings.vpnIpv6Strategy,
                vpnMtuPreset = settings.vpnMtuPreset,
                vpnCustomMtu = settings.vpnCustomMtu,
                logLevel = settings.logLevel,
            )
        }
    }
}

data class ResolverTextValidation(
    val normalizedResolvers: List<String>,
    val invalidEntries: List<String>,
) {
    val normalizedText: String
        get() = normalizedResolvers.joinToString("\n")

    val isValid: Boolean
        get() = normalizedResolvers.isNotEmpty() && invalidEntries.isEmpty()
}

data class WhiteDnsSettings(
    val selectedConnectionProfileId: String = ConnectionProfile.DefaultId,
    val connectionProfiles: List<ConnectionProfile> = listOf(ConnectionProfile.defaultProfile()),
    val selectedResolverProfileId: String = "",
    val resolverProfiles: List<ResolverProfile> = emptyList(),
    val selectedAdvancedProfileId: String = AdvancedSettingsProfile.DefaultId,
    val advancedProfiles: List<AdvancedSettingsProfile> = emptyList(),
    val serverMode: String = "custom",
    val customServerDomain: String = "",
    val customServerEncryptionKey: String = "",
    val customServerEncryptionMethod: Int = 1,
    val connectionMode: String = "proxy",
    val protocolType: String = "SOCKS5",
    val resolverText: String = "",
    val listenIp: String = "127.0.0.1",
    val listenPort: String = "10886",
    val httpProxyEnabled: Boolean = true,
    val httpProxyPort: String = "10887",
    val socks5Authentication: Boolean = false,
    val socksUsername: String = "master_dns_vpn",
    val socksPassword: String = "master_dns_vpn",
    val balancingStrategy: Int = 3,
    val uploadDuplication: String = "3",
    val downloadDuplication: String = "7",
    val uploadCompression: Int = 2,
    val downloadCompression: Int = 2,
    val baseEncodeData: Boolean = false,
    val minUploadMtu: String = "40",
    val minDownloadMtu: String = "300",
    val maxUploadMtu: String = "140",
    val maxDownloadMtu: String = "3000",
    val mtuTestRetriesResolvers: String = "3",
    val mtuTestTimeoutResolvers: String = "2.0",
    val mtuTestParallelismResolvers: String = "100",
    val mtuTestRetriesLogs: String = "5",
    val mtuTestTimeoutLogs: String = "2.0",
    val mtuTestParallelismLogs: String = "32",
    val rxTxWorkers: String = "4",
    val tunnelProcessWorkers: String = "4",
    val tunnelPacketTimeoutSeconds: String = "8.0",
    val dispatcherIdlePollIntervalSeconds: String = "0.020",
    val txChannelSize: String = "2048",
    val rxChannelSize: String = "2048",
    val resolverUdpConnectionPoolSize: String = "64",
    val streamQueueInitialCapacity: String = "128",
    val orphanQueueInitialCapacity: String = "32",
    val dnsResponseFragmentStoreCapacity: String = "256",
    val maxActiveStreams: String = "2048",
    val localHandshakeTimeoutSeconds: String = "5.0",
    val socksUdpAssociateReadTimeoutSeconds: String = "30.0",
    val clientTerminalStreamRetentionSeconds: String = "45.0",
    val clientCancelledSetupRetentionSeconds: String = "120.0",
    val sessionInitRetryBaseSeconds: String = "1.0",
    val sessionInitRetryStepSeconds: String = "1.0",
    val sessionInitRetryLinearAfter: String = "5",
    val sessionInitRetryMaxSeconds: String = "60.0",
    val sessionInitBusyRetryIntervalSeconds: String = "60.0",
    val localDnsEnabled: Boolean = false,
    val localDnsPort: String = "53",
    val startupMode: String = "resolvers",
    val pingWatchdogSeconds: String = "300",
    val trafficWarmupMode: String = WhiteDnsOptions.TrafficWarmupBalanced,
    val trafficWarmupEnabled: Boolean = true,
    val trafficWarmupProbeCount: String = "4",
    val trafficKeepaliveIntervalSeconds: String = "5",
    val vpnIpv6Strategy: String = WhiteDnsOptions.VpnIpv6StrategyBlock,
    val vpnMtuPreset: String = WhiteDnsOptions.VpnMtuPreset1500,
    val vpnCustomMtu: String = "1500",
    val fullVpnPerformanceWarningDismissed: Boolean = false,
    val splitTunnelMode: String = WhiteDnsOptions.SplitTunnelModeOff,
    val splitTunnelPackages: List<String> = emptyList(),
    val logLevel: String = "WARN",
) : Serializable

data class ResolvedWhiteDnsSettings(
    val connectionMode: String,
    val protocolType: String,
    val resolverEntries: List<String>,
    val listenIp: String,
    val listenPort: Int,
    val httpProxyEnabled: Boolean,
    val httpProxyPort: Int,
    val socks5Authentication: Boolean,
    val socksUsername: String,
    val socksPassword: String,
    val balancingStrategy: Int,
    val uploadDuplication: Int,
    val downloadDuplication: Int,
    val uploadCompression: Int,
    val downloadCompression: Int,
    val baseEncodeData: Boolean,
    val minUploadMtu: Int,
    val minDownloadMtu: Int,
    val maxUploadMtu: Int,
    val maxDownloadMtu: Int,
    val mtuTestRetriesResolvers: Int,
    val mtuTestTimeoutResolvers: Double,
    val mtuTestParallelismResolvers: Int,
    val mtuTestRetriesLogs: Int,
    val mtuTestTimeoutLogs: Double,
    val mtuTestParallelismLogs: Int,
    val rxTxWorkers: Int,
    val tunnelProcessWorkers: Int,
    val tunnelPacketTimeoutSeconds: Double,
    val dispatcherIdlePollIntervalSeconds: Double,
    val txChannelSize: Int,
    val rxChannelSize: Int,
    val resolverUdpConnectionPoolSize: Int,
    val streamQueueInitialCapacity: Int,
    val orphanQueueInitialCapacity: Int,
    val dnsResponseFragmentStoreCapacity: Int,
    val maxActiveStreams: Int,
    val localHandshakeTimeoutSeconds: Double,
    val socksUdpAssociateReadTimeoutSeconds: Double,
    val clientTerminalStreamRetentionSeconds: Double,
    val clientCancelledSetupRetentionSeconds: Double,
    val sessionInitRetryBaseSeconds: Double,
    val sessionInitRetryStepSeconds: Double,
    val sessionInitRetryLinearAfter: Int,
    val sessionInitRetryMaxSeconds: Double,
    val sessionInitBusyRetryIntervalSeconds: Double,
    val localDnsEnabled: Boolean,
    val localDnsPort: Int,
    val startupMode: String,
    val pingWatchdogSeconds: Int,
    val trafficWarmupMode: String,
    val trafficWarmupEnabled: Boolean,
    val trafficWarmupProbeCount: Int,
    val trafficKeepaliveIntervalSeconds: Int,
    val vpnIpv6Strategy: String,
    val vpnMtuPreset: String,
    val vpnMtu: Int,
    val splitTunnelMode: String,
    val splitTunnelPackages: List<String>,
    val logLevel: String,
)

data class ConnectionStats(
    val downloadBytes: Long = 0,
    val uploadBytes: Long = 0,
    val totalDataUsageBytes: Long = 0,
    val downloadSpeedBytesPerSecond: Long = 0,
    val uploadSpeedBytesPerSecond: Long = 0,
    val peakSpeedBytesPerSecond: Long = 0,
    val connectedApps: Int = 0,
)

data class ResolverRuntimeState(
    val activeResolvers: List<String> = emptyList(),
    val standbyResolvers: List<String> = emptyList(),
    val validResolvers: List<String> = emptyList(),
)

data class ConnectionProgressState(
    val phase: String = "idle",
    val percent: Int = 0,
    val completed: Int = 0,
    val total: Int = 0,
    val valid: Int = 0,
    val rejected: Int = 0,
) {
    val fraction: Float
        get() = percent.coerceIn(0, 100) / 100f

    val label: String
        get() = when (phase.lowercase()) {
            "preparing" -> "Preparing"
            "starting" -> "Starting"
            "mtu" -> if (total > 0) {
                "Scanning $completed/$total"
            } else {
                "Scanning"
            }
            "selecting" -> "Selecting resolver"
            "session" -> "Starting session"
            "runtime" -> "Starting runtime"
            "retry" -> "Retrying"
            "connected" -> "Connected"
            else -> "Preparing"
        }
}

object ConnectionVerificationStatus {
    const val Idle = "idle"
    const val Checking = "checking"
    const val Verified = "verified"
    const val Failed = "failed"
}

data class ConnectionVerificationState(
    val status: String = ConnectionVerificationStatus.Idle,
    val message: String = "",
    val checkedAtMillis: Long = 0,
)

data class SplitTunnelAppInfo(
    val packageName: String,
    val label: String,
)

data class WhiteDnsUiState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val settings: WhiteDnsSettings = WhiteDnsSettings(),
    val serverPool: List<StormDnsServerProfile> = emptyList(),
    val networkIpAddress: String = "127.0.0.1",
    val batteryOptimizationIgnored: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val activeConnectionProfileId: String? = null,
    val connectionLogs: List<String> = listOf("Idle"),
    val connectionStats: ConnectionStats = ConnectionStats(),
    val resolverRuntimeState: ResolverRuntimeState = ResolverRuntimeState(),
    val connectionProgress: ConnectionProgressState = ConnectionProgressState(),
    val connectionVerification: ConnectionVerificationState = ConnectionVerificationState(),
    val splitTunnelApps: List<SplitTunnelAppInfo> = emptyList(),
    val splitTunnelAppsLoading: Boolean = true,
)

object WhiteDnsRuntimeProxy {
    const val ListenIp = "127.0.0.1"
    const val ListenPort = "10886"
    const val ListenPortInt = 10886
    const val HttpProxyPort = "10887"
    const val HttpProxyPortInt = 10887
    const val LocalDnsPort = "10888"
    const val LocalDnsPortInt = 10888
}

object WhiteDnsOptions {
    const val SplitTunnelModeOff = "off"
    const val SplitTunnelModeInclude = "include"
    const val SplitTunnelModeExclude = "exclude"
    const val VpnIpv6StrategyBlock = "block"
    const val VpnIpv6StrategyBypass = "bypass"
    const val VpnIpv6StrategyExperimentalRoute = "experimental_route"
    const val VpnMtuPreset1280 = "1280"
    const val VpnMtuPreset1400 = "1400"
    const val VpnMtuPreset1500 = "1500"
    const val VpnMtuPresetCustom = "custom"
    const val TrafficWarmupOff = "off"
    const val TrafficWarmupStartupOnly = "startup_only"
    const val TrafficWarmupBalanced = "balanced"
    const val TrafficWarmupAggressive = "aggressive"
    const val TrafficWarmupCustom = "custom"

    val connectionModes = listOf(
        Choice("proxy", "Proxy Mode"),
        Choice("vpn", "Full VPN"),
    )

    val splitTunnelModes = listOf(
        Choice(SplitTunnelModeOff, "All Apps"),
        Choice(SplitTunnelModeInclude, "Only Selected"),
        Choice(SplitTunnelModeExclude, "Bypass Selected"),
    )

    val vpnIpv6Strategies = listOf(
        Choice(VpnIpv6StrategyBlock, "Block IPv6"),
        Choice(VpnIpv6StrategyBypass, "Bypass IPv6"),
        Choice(VpnIpv6StrategyExperimentalRoute, "Experimental Route"),
    )

    val vpnMtuPresets = listOf(
        Choice(VpnMtuPreset1280, "1280"),
        Choice(VpnMtuPreset1400, "1400"),
        Choice(VpnMtuPreset1500, "1500"),
        Choice(VpnMtuPresetCustom, "Custom"),
    )

    val trafficWarmupModes = listOf(
        Choice(TrafficWarmupOff, "Off"),
        Choice(TrafficWarmupStartupOnly, "Startup Only"),
        Choice(TrafficWarmupBalanced, "Balanced"),
        Choice(TrafficWarmupAggressive, "Aggressive"),
        Choice(TrafficWarmupCustom, "Custom"),
    )

    val encryptionMethods = listOf(
        Choice(0, "None"),
        Choice(1, "XOR"),
        Choice(2, "ChaCha20"),
        Choice(3, "AES-128-GCM"),
        Choice(4, "AES-192-GCM"),
        Choice(5, "AES-256-GCM"),
    )

    val balancingStrategies = listOf(
        Choice(1, "Random"),
        Choice(2, "Round Robin"),
        Choice(3, "Least Loss"),
        Choice(4, "Lowest Latency"),
    )

    val compressionTypes = listOf(
        Choice(0, "OFF"),
        Choice(1, "ZSTD"),
        Choice(2, "LZ4"),
        Choice(3, "ZLIB"),
    )

    val startupModes = listOf(
        Choice("ask", "Ask each time"),
        Choice("resolvers", "Full scan"),
        Choice("logs", "From logs (fast)"),
    )

    val logLevels = listOf(
        Choice("DEBUG", "DEBUG"),
        Choice("INFO", "INFO"),
        Choice("WARN", "WARN"),
        Choice("ERROR", "ERROR"),
    )

    fun encryptionMethodLabel(methodId: Int): String {
        return encryptionMethods.firstOrNull { it.value == methodId }?.label ?: "Unknown"
    }

    fun connectionModeLabel(mode: String): String {
        return connectionModes.firstOrNull { it.value == mode }?.label ?: "Proxy Mode"
    }

    fun splitTunnelModeLabel(mode: String): String {
        return splitTunnelModes.firstOrNull { it.value == mode }?.label ?: "All Apps"
    }

    fun vpnIpv6StrategyLabel(strategy: String): String {
        return vpnIpv6Strategies.firstOrNull { it.value == strategy }?.label ?: "Block IPv6"
    }
}

fun WhiteDnsSettings.normalizedConnectionProfiles(): List<ConnectionProfile> {
    val resolverIds = resolverProfiles.map { it.id }.toSet()
    val source = connectionProfiles.ifEmpty {
        listOf(ConnectionProfile.fromSettings(this))
    }
    val normalizedProfiles = source
        .filter { it.id.isNotBlank() }
        .distinctBy { it.id }
        .mapIndexed { index, profile ->
            profile.copy(
                name = profile.name.ifBlank { "Connection ${index + 1}" },
                serverMode = "custom",
                customServerEncryptionMethod = profile.customServerEncryptionMethod.coerceIn(0, 5),
                resolverProfileId = profile.resolverProfileId.takeIf { it in resolverIds }.orEmpty(),
                connectionMode = when (profile.connectionMode) {
                    "proxy", "vpn" -> profile.connectionMode
                    else -> "proxy"
                },
            )
        }

    val customProfiles = normalizedProfiles
        .mapIndexed { index, profile ->
            profile.copy(
                id = profile.id,
                name = profile.name.ifBlank { "Connection ${index + 1}" },
                serverMode = "custom",
            )
        }
        .distinctBy { it.id }

    return customProfiles.ifEmpty {
        listOf(ConnectionProfile.defaultProfile())
    }
}

fun WhiteDnsSettings.normalizedResolverProfiles(): List<ResolverProfile> {
    return resolverProfiles
        .filter { it.id.isNotBlank() }
        .distinctBy { it.id }
        .mapIndexed { index, profile ->
            profile.copy(
                name = profile.name.ifBlank { "Resolvers ${index + 1}" },
                resolverText = normalizeResolverText(profile.resolverText),
            )
        }
        .filter { it.resolverText.isNotBlank() }
}

fun WhiteDnsSettings.normalizedAdvancedProfiles(): List<AdvancedSettingsProfile> {
    val customProfiles = advancedProfiles
        .filter { it.id.isNotBlank() && it.id != AdvancedSettingsProfile.DefaultId }
        .distinctBy { it.id }
        .mapIndexed { index, profile ->
            profile.copy(name = profile.name.ifBlank { "Advanced ${index + 1}" })
        }
    return listOf(AdvancedSettingsProfile.defaultProfile()) + customProfiles
}

fun WhiteDnsSettings.selectedConnectionProfile(): ConnectionProfile {
    val profiles = normalizedConnectionProfiles()
    return profiles.firstOrNull { it.id == selectedConnectionProfileId } ?: profiles.first()
}

fun WhiteDnsSettings.selectedResolverProfile(): ResolverProfile? {
    return normalizedResolverProfiles().firstOrNull { it.id == selectedResolverProfileId }
}

fun WhiteDnsSettings.selectedAdvancedProfile(): AdvancedSettingsProfile {
    val profiles = normalizedAdvancedProfiles()
    return profiles.firstOrNull { it.id == selectedAdvancedProfileId } ?: profiles.first()
}

fun WhiteDnsSettings.matchesAdvancedProfile(profile: AdvancedSettingsProfile): Boolean {
    return AdvancedSettingsProfile.fromSettings(this, id = profile.id, name = profile.name) == profile
}

fun WhiteDnsSettings.syncSelectedConnectionProfileFields(): WhiteDnsSettings {
    val resolverProfiles = normalizedResolverProfiles()
    val resolverIds = resolverProfiles.map { it.id }.toSet()
    val profiles = normalizedConnectionProfiles()
    val advancedProfiles = normalizedAdvancedProfiles()
    val selected = profiles.firstOrNull { it.id == selectedConnectionProfileId } ?: profiles.first()
    val selectedConnectionMode = normalizeConnectionMode(connectionMode)
    val modeSyncedProfiles = profiles.map { profile ->
        if (profile.id == selected.id) {
            profile.copy(connectionMode = selectedConnectionMode)
        } else {
            profile
        }
    }
    val selectedResolverId = selected.resolverProfileId
        .takeIf { it in resolverIds }
        ?: selectedResolverProfileId.takeIf { it in resolverIds }
        ?: ""
    val selectedResolver = resolverProfiles.firstOrNull { it.id == selectedResolverId }
    val selectedAdvancedId = selectedAdvancedProfileId
        .takeIf { profileId -> advancedProfiles.any { it.id == profileId } }
        ?: AdvancedSettingsProfile.DefaultId
    return copy(
        selectedConnectionProfileId = selected.id,
        connectionProfiles = modeSyncedProfiles,
        selectedResolverProfileId = selectedResolverId,
        resolverProfiles = resolverProfiles,
        selectedAdvancedProfileId = selectedAdvancedId,
        advancedProfiles = advancedProfiles.filter { it.id != AdvancedSettingsProfile.DefaultId },
        resolverText = selectedResolver?.resolverText ?: resolverText,
        serverMode = selected.serverMode,
        customServerDomain = selected.customServerDomain,
        customServerEncryptionKey = selected.customServerEncryptionKey,
        customServerEncryptionMethod = selected.customServerEncryptionMethod,
        connectionMode = selectedConnectionMode,
        splitTunnelMode = normalizeSplitTunnelMode(splitTunnelMode),
        splitTunnelPackages = normalizePackageNames(splitTunnelPackages),
    )
}

fun WhiteDnsSettings.runtimeConnectionSettings(): WhiteDnsSettings {
    val settings = syncSelectedConnectionProfileFields()
    return if (settings.connectionMode == "vpn") {
        settings.copy(
            listenIp = WhiteDnsRuntimeProxy.ListenIp,
            listenPort = WhiteDnsRuntimeProxy.ListenPort,
            httpProxyEnabled = false,
            httpProxyPort = WhiteDnsRuntimeProxy.HttpProxyPort,
            socks5Authentication = false,
            socksUsername = "",
            socksPassword = "",
            localDnsEnabled = false,
            localDnsPort = WhiteDnsRuntimeProxy.LocalDnsPort,
        )
    } else {
        settings.copy(
            localDnsEnabled = true,
            localDnsPort = WhiteDnsRuntimeProxy.LocalDnsPort,
        )
    }
}

fun WhiteDnsSettings.applyAdvancedProfile(profile: AdvancedSettingsProfile): WhiteDnsSettings {
    return copy(
        selectedAdvancedProfileId = profile.id,
        listenIp = profile.listenIp,
        listenPort = profile.listenPort,
        httpProxyEnabled = profile.httpProxyEnabled,
        httpProxyPort = profile.httpProxyPort,
        socks5Authentication = profile.socks5Authentication,
        socksUsername = profile.socksUsername,
        socksPassword = profile.socksPassword,
        balancingStrategy = profile.balancingStrategy,
        uploadDuplication = profile.uploadDuplication,
        downloadDuplication = profile.downloadDuplication,
        uploadCompression = profile.uploadCompression,
        downloadCompression = profile.downloadCompression,
        baseEncodeData = profile.baseEncodeData,
        minUploadMtu = profile.minUploadMtu,
        minDownloadMtu = profile.minDownloadMtu,
        maxUploadMtu = profile.maxUploadMtu,
        maxDownloadMtu = profile.maxDownloadMtu,
        mtuTestRetriesResolvers = profile.mtuTestRetriesResolvers,
        mtuTestTimeoutResolvers = profile.mtuTestTimeoutResolvers,
        mtuTestParallelismResolvers = profile.mtuTestParallelismResolvers,
        mtuTestRetriesLogs = profile.mtuTestRetriesLogs,
        mtuTestTimeoutLogs = profile.mtuTestTimeoutLogs,
        mtuTestParallelismLogs = profile.mtuTestParallelismLogs,
        rxTxWorkers = profile.rxTxWorkers,
        tunnelProcessWorkers = profile.tunnelProcessWorkers,
        tunnelPacketTimeoutSeconds = profile.tunnelPacketTimeoutSeconds,
        dispatcherIdlePollIntervalSeconds = profile.dispatcherIdlePollIntervalSeconds,
        txChannelSize = profile.txChannelSize,
        rxChannelSize = profile.rxChannelSize,
        resolverUdpConnectionPoolSize = profile.resolverUdpConnectionPoolSize,
        streamQueueInitialCapacity = profile.streamQueueInitialCapacity,
        orphanQueueInitialCapacity = profile.orphanQueueInitialCapacity,
        dnsResponseFragmentStoreCapacity = profile.dnsResponseFragmentStoreCapacity,
        maxActiveStreams = profile.maxActiveStreams,
        localHandshakeTimeoutSeconds = profile.localHandshakeTimeoutSeconds,
        socksUdpAssociateReadTimeoutSeconds = profile.socksUdpAssociateReadTimeoutSeconds,
        clientTerminalStreamRetentionSeconds = profile.clientTerminalStreamRetentionSeconds,
        clientCancelledSetupRetentionSeconds = profile.clientCancelledSetupRetentionSeconds,
        sessionInitRetryBaseSeconds = profile.sessionInitRetryBaseSeconds,
        sessionInitRetryStepSeconds = profile.sessionInitRetryStepSeconds,
        sessionInitRetryLinearAfter = profile.sessionInitRetryLinearAfter,
        sessionInitRetryMaxSeconds = profile.sessionInitRetryMaxSeconds,
        sessionInitBusyRetryIntervalSeconds = profile.sessionInitBusyRetryIntervalSeconds,
        localDnsEnabled = profile.localDnsEnabled,
        localDnsPort = profile.localDnsPort,
        startupMode = profile.startupMode,
        pingWatchdogSeconds = profile.pingWatchdogSeconds,
        trafficWarmupMode = profile.trafficWarmupMode,
        trafficWarmupEnabled = profile.trafficWarmupEnabled,
        trafficWarmupProbeCount = profile.trafficWarmupProbeCount,
        trafficKeepaliveIntervalSeconds = profile.trafficKeepaliveIntervalSeconds,
        vpnIpv6Strategy = profile.vpnIpv6Strategy,
        vpnMtuPreset = profile.vpnMtuPreset,
        vpnCustomMtu = profile.vpnCustomMtu,
        logLevel = profile.logLevel,
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.selectAdvancedProfile(profileId: String): WhiteDnsSettings {
    val selectedProfile = normalizedAdvancedProfiles().firstOrNull { it.id == profileId }
        ?: AdvancedSettingsProfile.defaultProfile()
    return applyAdvancedProfile(selectedProfile)
}

fun WhiteDnsSettings.saveSelectedAdvancedProfile(): WhiteDnsSettings {
    if (selectedAdvancedProfileId == AdvancedSettingsProfile.DefaultId) {
        return syncSelectedConnectionProfileFields()
    }
    val customProfiles = normalizedAdvancedProfiles().filter { it.id != AdvancedSettingsProfile.DefaultId }
    if (customProfiles.none { it.id == selectedAdvancedProfileId }) {
        return syncSelectedConnectionProfileFields()
    }
    val currentProfile = selectedAdvancedProfile()
    val updatedProfile = AdvancedSettingsProfile.fromSettings(
        settings = this,
        id = currentProfile.id,
        name = currentProfile.name,
    )
    return copy(
        advancedProfiles = customProfiles.map { profile ->
            if (profile.id == updatedProfile.id) updatedProfile else profile
        },
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.saveCurrentAdvancedProfileAs(name: String): WhiteDnsSettings {
    val profileName = name.trim().ifBlank { "Advanced Settings" }
    val profileId = AdvancedSettingsProfile.newId()
    val profile = AdvancedSettingsProfile.fromSettings(
        settings = this,
        id = profileId,
        name = profileName,
    )
    return copy(
        selectedAdvancedProfileId = profileId,
        advancedProfiles = normalizedAdvancedProfiles().filter { it.id != AdvancedSettingsProfile.DefaultId } + profile,
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.selectConnectionProfile(profileId: String): WhiteDnsSettings {
    val profiles = normalizedConnectionProfiles()
    val resolverProfiles = normalizedResolverProfiles()
    val selected = profiles.firstOrNull { it.id == profileId } ?: profiles.first()
    val resolverProfile = resolverProfiles.firstOrNull { it.id == selected.resolverProfileId }
    return copy(
        selectedConnectionProfileId = selected.id,
        connectionProfiles = profiles,
        selectedResolverProfileId = resolverProfile?.id.orEmpty(),
        resolverProfiles = resolverProfiles,
        resolverText = resolverProfile?.resolverText ?: resolverText,
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.upsertConnectionProfile(profile: ConnectionProfile): WhiteDnsSettings {
    val resolverIds = normalizedResolverProfiles().map { it.id }.toSet()
    val normalizedProfile = profile.copy(
        id = profile.id.ifBlank { "profile-${System.currentTimeMillis()}" },
        name = profile.name.ifBlank { "Connection" },
        serverMode = "custom",
        customServerEncryptionMethod = profile.customServerEncryptionMethod.coerceIn(0, 5),
        resolverProfileId = profile.resolverProfileId.takeIf { it in resolverIds }.orEmpty(),
        connectionMode = when (profile.connectionMode) {
            "proxy", "vpn" -> profile.connectionMode
            else -> "proxy"
        },
    )
    val profiles = normalizedConnectionProfiles()
    val updatedProfiles = if (profiles.any { it.id == normalizedProfile.id }) {
        profiles.map { existing ->
            if (existing.id == normalizedProfile.id) normalizedProfile else existing
        }
    } else {
        profiles + normalizedProfile
    }
    return copy(
        connectionProfiles = updatedProfiles,
        selectedConnectionProfileId = if (selectedConnectionProfileId.isBlank()) {
            normalizedProfile.id
        } else {
            selectedConnectionProfileId
        },
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.upsertResolverProfile(profile: ResolverProfile): WhiteDnsSettings {
    val normalizedProfile = profile.copy(
        id = profile.id.ifBlank { ResolverProfile.newId() },
        name = profile.name.ifBlank { "Resolvers" },
        resolverText = normalizeResolverText(profile.resolverText),
    )
    if (normalizedProfile.resolverText.isBlank()) {
        return syncSelectedConnectionProfileFields()
    }
    val profiles = normalizedResolverProfiles()
    val updatedProfiles = if (profiles.any { it.id == normalizedProfile.id }) {
        profiles.map { existing ->
            if (existing.id == normalizedProfile.id) normalizedProfile else existing
        }
    } else {
        profiles + normalizedProfile
    }
    return copy(
        resolverProfiles = updatedProfiles,
        selectedResolverProfileId = normalizedProfile.id,
        resolverText = normalizedProfile.resolverText,
    ).applyResolverProfileToSelectedConnection(normalizedProfile.id)
}

fun WhiteDnsSettings.moveConnectionProfile(profileId: String, direction: Int): WhiteDnsSettings {
    if (direction == 0) {
        return syncSelectedConnectionProfileFields()
    }
    val profiles = normalizedConnectionProfiles()
    val customProfiles = profiles.filter { it.serverMode == "custom" }
    val fromIndex = customProfiles.indexOfFirst { it.id == profileId }
    if (fromIndex == -1) {
        return copy(connectionProfiles = profiles).syncSelectedConnectionProfileFields()
    }
    return moveConnectionProfileToIndex(profileId, fromIndex + direction)
}

fun WhiteDnsSettings.moveConnectionProfileToIndex(profileId: String, targetIndex: Int): WhiteDnsSettings {
    val profiles = normalizedConnectionProfiles()
    val customProfiles = profiles.filter { it.serverMode == "custom" }
    val fromIndex = customProfiles.indexOfFirst { it.id == profileId }
    if (fromIndex == -1) {
        return copy(connectionProfiles = profiles).syncSelectedConnectionProfileFields()
    }
    val toIndex = targetIndex.coerceIn(0, customProfiles.lastIndex)
    if (fromIndex == toIndex) {
        return copy(connectionProfiles = profiles).syncSelectedConnectionProfileFields()
    }
    return copy(
        connectionProfiles = customProfiles.moved(fromIndex, toIndex),
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.moveResolverProfile(profileId: String, direction: Int): WhiteDnsSettings {
    if (direction == 0) {
        return syncSelectedConnectionProfileFields()
    }
    val profiles = normalizedResolverProfiles()
    val fromIndex = profiles.indexOfFirst { it.id == profileId }
    if (fromIndex == -1) {
        return copy(resolverProfiles = profiles).syncSelectedConnectionProfileFields()
    }
    return moveResolverProfileToIndex(profileId, fromIndex + direction)
}

fun WhiteDnsSettings.moveResolverProfileToIndex(profileId: String, targetIndex: Int): WhiteDnsSettings {
    val profiles = normalizedResolverProfiles()
    val fromIndex = profiles.indexOfFirst { it.id == profileId }
    if (fromIndex == -1) {
        return copy(resolverProfiles = profiles).syncSelectedConnectionProfileFields()
    }
    val toIndex = targetIndex.coerceIn(0, profiles.lastIndex)
    if (fromIndex == toIndex) {
        return copy(resolverProfiles = profiles).syncSelectedConnectionProfileFields()
    }
    return copy(
        resolverProfiles = profiles.moved(fromIndex, toIndex),
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.applyResolverProfileToSelectedConnection(profileId: String): WhiteDnsSettings {
    val resolverProfiles = normalizedResolverProfiles()
    val resolverProfile = resolverProfiles.firstOrNull { it.id == profileId }
        ?: return copy(selectedResolverProfileId = "").syncSelectedConnectionProfileFields()
    val connectionProfiles = normalizedConnectionProfiles()
    val selectedConnection = connectionProfiles.firstOrNull { it.id == selectedConnectionProfileId }
        ?: connectionProfiles.first()
    val updatedConnectionProfiles = connectionProfiles.map { profile ->
        if (profile.id == selectedConnection.id) {
            profile.copy(resolverProfileId = resolverProfile.id)
        } else {
            profile
        }
    }
    return copy(
        connectionProfiles = updatedConnectionProfiles,
        resolverProfiles = resolverProfiles,
        selectedResolverProfileId = resolverProfile.id,
        resolverText = resolverProfile.resolverText,
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.clearSelectedResolverProfile(): WhiteDnsSettings {
    val connectionProfiles = normalizedConnectionProfiles()
    val selectedConnection = connectionProfiles.firstOrNull { it.id == selectedConnectionProfileId }
        ?: connectionProfiles.first()
    val updatedConnectionProfiles = connectionProfiles.map { profile ->
        if (profile.id == selectedConnection.id) {
            profile.copy(resolverProfileId = "")
        } else {
            profile
        }
    }
    return copy(
        connectionProfiles = updatedConnectionProfiles,
        selectedConnectionProfileId = selectedConnection.id,
        selectedResolverProfileId = "",
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.updateManualResolverText(resolverText: String): WhiteDnsSettings {
    return clearSelectedResolverProfile()
        .copy(resolverText = resolverText)
        .syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.deleteResolverProfile(profileId: String): WhiteDnsSettings {
    val profiles = normalizedResolverProfiles()
    if (profiles.none { it.id == profileId }) {
        return syncSelectedConnectionProfileFields()
    }
    val remainingProfiles = profiles.filterNot { it.id == profileId }
    val updatedConnectionProfiles = normalizedConnectionProfiles().map { profile ->
        if (profile.resolverProfileId == profileId) {
            profile.copy(resolverProfileId = "")
        } else {
            profile
        }
    }
    return copy(
        resolverProfiles = remainingProfiles,
        connectionProfiles = updatedConnectionProfiles,
        selectedResolverProfileId = if (selectedResolverProfileId == profileId) "" else selectedResolverProfileId,
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.deleteConnectionProfile(profileId: String): WhiteDnsSettings {
    val profiles = normalizedConnectionProfiles()
    if (profiles.size <= 1 || profiles.none { it.id == profileId }) {
        return syncSelectedConnectionProfileFields()
    }
    val remainingProfiles = profiles.filterNot { it.id == profileId }
    val nextSelectedId = if (selectedConnectionProfileId == profileId) {
        remainingProfiles.first().id
    } else {
        selectedConnectionProfileId
    }
    return copy(
        connectionProfiles = remainingProfiles,
        selectedConnectionProfileId = nextSelectedId,
    ).syncSelectedConnectionProfileFields()
}

private fun <T> List<T>.moved(fromIndex: Int, toIndex: Int): List<T> {
    val reordered = toMutableList()
    val item = reordered.removeAt(fromIndex)
    reordered.add(toIndex, item)
    return reordered
}

fun WhiteDnsSettings.resetAdvancedSettings(): WhiteDnsSettings {
    val defaults = WhiteDnsSettings()
    return copy(
        selectedAdvancedProfileId = AdvancedSettingsProfile.DefaultId,
        listenIp = defaults.listenIp,
        listenPort = defaults.listenPort,
        httpProxyEnabled = defaults.httpProxyEnabled,
        httpProxyPort = defaults.httpProxyPort,
        socks5Authentication = defaults.socks5Authentication,
        socksUsername = defaults.socksUsername,
        socksPassword = defaults.socksPassword,
        balancingStrategy = defaults.balancingStrategy,
        uploadDuplication = defaults.uploadDuplication,
        downloadDuplication = defaults.downloadDuplication,
        uploadCompression = defaults.uploadCompression,
        downloadCompression = defaults.downloadCompression,
        baseEncodeData = defaults.baseEncodeData,
        minUploadMtu = defaults.minUploadMtu,
        minDownloadMtu = defaults.minDownloadMtu,
        maxUploadMtu = defaults.maxUploadMtu,
        maxDownloadMtu = defaults.maxDownloadMtu,
        mtuTestRetriesResolvers = defaults.mtuTestRetriesResolvers,
        mtuTestTimeoutResolvers = defaults.mtuTestTimeoutResolvers,
        mtuTestParallelismResolvers = defaults.mtuTestParallelismResolvers,
        mtuTestRetriesLogs = defaults.mtuTestRetriesLogs,
        mtuTestTimeoutLogs = defaults.mtuTestTimeoutLogs,
        mtuTestParallelismLogs = defaults.mtuTestParallelismLogs,
        rxTxWorkers = defaults.rxTxWorkers,
        tunnelProcessWorkers = defaults.tunnelProcessWorkers,
        tunnelPacketTimeoutSeconds = defaults.tunnelPacketTimeoutSeconds,
        dispatcherIdlePollIntervalSeconds = defaults.dispatcherIdlePollIntervalSeconds,
        txChannelSize = defaults.txChannelSize,
        rxChannelSize = defaults.rxChannelSize,
        resolverUdpConnectionPoolSize = defaults.resolverUdpConnectionPoolSize,
        streamQueueInitialCapacity = defaults.streamQueueInitialCapacity,
        orphanQueueInitialCapacity = defaults.orphanQueueInitialCapacity,
        dnsResponseFragmentStoreCapacity = defaults.dnsResponseFragmentStoreCapacity,
        maxActiveStreams = defaults.maxActiveStreams,
        localHandshakeTimeoutSeconds = defaults.localHandshakeTimeoutSeconds,
        socksUdpAssociateReadTimeoutSeconds = defaults.socksUdpAssociateReadTimeoutSeconds,
        clientTerminalStreamRetentionSeconds = defaults.clientTerminalStreamRetentionSeconds,
        clientCancelledSetupRetentionSeconds = defaults.clientCancelledSetupRetentionSeconds,
        sessionInitRetryBaseSeconds = defaults.sessionInitRetryBaseSeconds,
        sessionInitRetryStepSeconds = defaults.sessionInitRetryStepSeconds,
        sessionInitRetryLinearAfter = defaults.sessionInitRetryLinearAfter,
        sessionInitRetryMaxSeconds = defaults.sessionInitRetryMaxSeconds,
        sessionInitBusyRetryIntervalSeconds = defaults.sessionInitBusyRetryIntervalSeconds,
        localDnsEnabled = defaults.localDnsEnabled,
        localDnsPort = defaults.localDnsPort,
        startupMode = defaults.startupMode,
        pingWatchdogSeconds = defaults.pingWatchdogSeconds,
        trafficWarmupMode = defaults.trafficWarmupMode,
        trafficWarmupEnabled = defaults.trafficWarmupEnabled,
        trafficWarmupProbeCount = defaults.trafficWarmupProbeCount,
        trafficKeepaliveIntervalSeconds = defaults.trafficKeepaliveIntervalSeconds,
        vpnIpv6Strategy = defaults.vpnIpv6Strategy,
        vpnMtuPreset = defaults.vpnMtuPreset,
        vpnCustomMtu = defaults.vpnCustomMtu,
        logLevel = defaults.logLevel,
    ).syncSelectedConnectionProfileFields()
}

fun validateResolverText(raw: String): ResolverTextValidation {
    val normalizedResolvers = mutableListOf<String>()
    val invalidEntries = mutableListOf<String>()
    val seen = mutableSetOf<String>()

    resolverTextTokens(raw).forEach { entry ->
        val normalized = normalizeResolverEntry(entry)
        if (normalized == null) {
            invalidEntries += entry
            return@forEach
        }
        if (seen.add(normalized)) {
            normalizedResolvers += normalized
        }
    }

    return ResolverTextValidation(
        normalizedResolvers = normalizedResolvers,
        invalidEntries = invalidEntries.distinct(),
    )
}

private fun normalizeResolverText(raw: String): String {
    return validateResolverText(raw).normalizedText
}

private fun resolverTextTokens(raw: String): Sequence<String> {
    val lines = raw
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .lineSequence()
        .map(String::trim)
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .toList()

    val separators = if (detectResolverTextEntryType(lines) == ResolverTextEntryType.CommaSeparated) {
        charArrayOf(',', ';')
    } else {
        charArrayOf()
    }

    return lines
        .asSequence()
        .flatMap { line ->
            if (separators.isEmpty()) {
                sequenceOf(line)
            } else {
                line.split(*separators).asSequence()
            }
        }
        .map(String::trim)
        .filter { it.isNotEmpty() && !it.startsWith("#") }
}

private fun normalizeResolverEntry(entry: String): String? {
    normalizeResolverTarget(entry)?.let { return it }

    val hostPort = splitResolverHostPort(entry) ?: return null
    val target = normalizeResolverTarget(hostPort.first) ?: return null
    val port = hostPort.second.toIntOrNull()?.takeIf { it in 1..65535 } ?: return null
    if (port == DefaultResolverPort) {
        return target
    }
    return if (resolverTargetNeedsBrackets(target)) {
        "[$target]:$port"
    } else {
        "$target:$port"
    }
}

private fun splitResolverHostPort(entry: String): Pair<String, String>? {
    val text = entry.trim()
    if (text.startsWith("[")) {
        val end = text.indexOf(']')
        if (end <= 1) {
            return null
        }
        val hostPart = text.substring(1, end).trim()
        val remainder = text.substring(end + 1).trim()
        if (!remainder.startsWith(":")) {
            return null
        }
        val portPart = remainder.substring(1).trim()
        return if (hostPart.isNotEmpty() && portPart.isNotEmpty()) hostPart to portPart else null
    }

    if (text.count { it == ':' } != 1) {
        return null
    }
    val separator = text.indexOf(':')
    val hostPart = text.substring(0, separator).trim()
    val portPart = text.substring(separator + 1).trim()
    return if (hostPart.isNotEmpty() && portPart.isNotEmpty()) hostPart to portPart else null
}

private fun normalizeResolverTarget(target: String): String? {
    val text = target.trim()
    if (text.isEmpty()) {
        return null
    }

    val slashIndex = text.indexOf('/')
    if (slashIndex == -1) {
        return normalizeIpAddress(text)
    }
    if (slashIndex != text.lastIndexOf('/')) {
        return null
    }

    val ip = normalizeIpAddress(text.substring(0, slashIndex).trim()) ?: return null
    val prefixBits = text.substring(slashIndex + 1).trim().toIntOrNull() ?: return null
    val maxBits = if (ip.contains(':')) 128 else 32
    if (prefixBits !in 0..maxBits) {
        return null
    }
    val hostBits = maxBits - prefixBits
    if (hostBits > 16) {
        return null
    }
    return "$ip/$prefixBits"
}

private fun normalizeIpAddress(raw: String): String? {
    val text = raw.trim()
    if (text.isEmpty()) {
        return null
    }

    if (!text.contains(':')) {
        return normalizeIpv4Address(text)
    }

    if (!ResolverIpv6Chars.matches(text)) {
        return null
    }
    return runCatching {
        InetAddress.getByName(text)
    }.getOrNull()?.hostAddress?.takeIf { it.contains(':') }
}

private fun normalizeIpv4Address(raw: String): String? {
    val parts = raw.split('.')
    if (parts.size != 4) {
        return null
    }
    return parts
        .map { part ->
            if (part.isEmpty() || part.any { !it.isDigit() }) {
                return null
            }
            part.toIntOrNull()?.takeIf { it in 0..255 } ?: return null
        }
        .joinToString(".")
}

private fun resolverTargetNeedsBrackets(target: String): Boolean {
    return target.substringBefore('/').contains(':')
}

private fun detectResolverTextEntryType(lines: List<String>): ResolverTextEntryType {
    return if (lines.any { it.contains(',') || it.contains(';') }) {
        ResolverTextEntryType.CommaSeparated
    } else {
        ResolverTextEntryType.LineSeparated
    }
}

private enum class ResolverTextEntryType {
    LineSeparated,
    CommaSeparated,
}

private const val DefaultResolverPort = 53

private val ResolverIpv6Chars = Regex("^[0-9A-Fa-f:.]+$")

private fun normalizeSplitTunnelMode(raw: String): String {
    return when (raw) {
        WhiteDnsOptions.SplitTunnelModeInclude -> raw
        WhiteDnsOptions.SplitTunnelModeExclude -> raw
        WhiteDnsOptions.SplitTunnelModeOff -> raw
        else -> WhiteDnsOptions.SplitTunnelModeOff
    }
}

private fun normalizeConnectionMode(raw: String): String {
    return when (raw) {
        "proxy", "vpn" -> raw
        else -> "proxy"
    }
}

private fun normalizeTrafficWarmupMode(raw: String, legacyEnabled: Boolean): String {
    if (!legacyEnabled) {
        return WhiteDnsOptions.TrafficWarmupOff
    }
    return when (raw) {
        WhiteDnsOptions.TrafficWarmupOff,
        WhiteDnsOptions.TrafficWarmupStartupOnly,
        WhiteDnsOptions.TrafficWarmupBalanced,
        WhiteDnsOptions.TrafficWarmupAggressive,
        WhiteDnsOptions.TrafficWarmupCustom -> raw
        else -> WhiteDnsOptions.TrafficWarmupBalanced
    }
}

private fun normalizePackageNames(raw: List<String>): List<String> {
    return raw
        .asSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
        .sorted()
        .toList()
}

fun WhiteDnsSettings.resolve(): ResolvedWhiteDnsSettings {
    fun boundedInt(raw: String, defaultValue: Int, minValue: Int, maxValue: Int): Int {
        return raw.trim().toIntOrNull()?.coerceIn(minValue, maxValue) ?: defaultValue
    }

    fun positiveDouble(raw: String, defaultValue: Double): Double {
        val value = raw.trim().toDoubleOrNull() ?: return defaultValue
        return if (value > 0.0) value else defaultValue
    }

    fun boundedDouble(raw: String, defaultValue: Double, minValue: Double, maxValue: Double): Double {
        val value = raw.trim().toDoubleOrNull() ?: return defaultValue
        return value.coerceIn(minValue, maxValue)
    }

    fun boundedPositiveDouble(raw: String, defaultValue: Double, minValue: Double, maxValue: Double): Double {
        val value = raw.trim().toDoubleOrNull() ?: return defaultValue
        if (value <= 0.0) {
            return defaultValue
        }
        return value.coerceIn(minValue, maxValue)
    }

    val resolvers = validateResolverText(resolverText).normalizedResolvers

    val resolvedRxTxWorkers = boundedInt(rxTxWorkers, defaultValue = 4, minValue = 1, maxValue = 64)
    val resolvedTunnelProcessWorkers = boundedInt(
        tunnelProcessWorkers,
        defaultValue = 4,
        minValue = 1,
        maxValue = 64,
    ).coerceAtLeast(resolvedRxTxWorkers)
    val resolvedSessionRetryBaseSeconds = boundedDouble(
        sessionInitRetryBaseSeconds,
        defaultValue = 1.0,
        minValue = 0.1,
        maxValue = 60.0,
    )
    val resolvedMinUploadMtu = boundedInt(minUploadMtu, defaultValue = 40, minValue = 1, maxValue = 65535)
    val resolvedMinDownloadMtu = boundedInt(minDownloadMtu, defaultValue = 300, minValue = 1, maxValue = 65535)
    val resolvedMaxUploadMtu = boundedInt(maxUploadMtu, defaultValue = 140, minValue = 1, maxValue = 65535)
        .coerceAtLeast(resolvedMinUploadMtu)
    val resolvedMaxDownloadMtu = boundedInt(maxDownloadMtu, defaultValue = 3000, minValue = 1, maxValue = 65535)
        .coerceAtLeast(resolvedMinDownloadMtu)
    val resolvedVpnIpv6Strategy = when (vpnIpv6Strategy) {
        WhiteDnsOptions.VpnIpv6StrategyBlock,
        WhiteDnsOptions.VpnIpv6StrategyBypass,
        WhiteDnsOptions.VpnIpv6StrategyExperimentalRoute -> vpnIpv6Strategy
        else -> WhiteDnsOptions.VpnIpv6StrategyBlock
    }
    val resolvedVpnMtuPreset = when (vpnMtuPreset) {
        WhiteDnsOptions.VpnMtuPreset1280,
        WhiteDnsOptions.VpnMtuPreset1400,
        WhiteDnsOptions.VpnMtuPreset1500,
        WhiteDnsOptions.VpnMtuPresetCustom -> vpnMtuPreset
        else -> WhiteDnsOptions.VpnMtuPreset1500
    }
    val resolvedVpnMtu = when (resolvedVpnMtuPreset) {
        WhiteDnsOptions.VpnMtuPreset1280 -> 1280
        WhiteDnsOptions.VpnMtuPreset1400 -> 1400
        WhiteDnsOptions.VpnMtuPresetCustom -> boundedInt(
            vpnCustomMtu,
            defaultValue = 1500,
            minValue = 1280,
            maxValue = 9000,
        )
        else -> 1500
    }
    val resolvedSocksUsername = socksUsername.take(255)
    val resolvedSocksPassword = socksPassword.take(255)
    val resolvedSocks5Authentication = socks5Authentication &&
        resolvedSocksUsername.isNotBlank() &&
        resolvedSocksPassword.isNotBlank()
    val resolvedTrafficWarmupMode = normalizeTrafficWarmupMode(trafficWarmupMode, trafficWarmupEnabled)
    val customWarmupProbeCount = boundedInt(trafficWarmupProbeCount, defaultValue = 4, minValue = 0, maxValue = 10)
    val customKeepaliveIntervalSeconds = boundedInt(
        trafficKeepaliveIntervalSeconds,
        defaultValue = 30,
        minValue = 2,
        maxValue = 300,
    )
    val resolvedTrafficWarmupProbeCount = when (resolvedTrafficWarmupMode) {
        WhiteDnsOptions.TrafficWarmupOff -> 0
        WhiteDnsOptions.TrafficWarmupStartupOnly -> 2
        WhiteDnsOptions.TrafficWarmupBalanced -> 3
        WhiteDnsOptions.TrafficWarmupAggressive -> 6
        else -> customWarmupProbeCount
    }
    val resolvedTrafficKeepaliveIntervalSeconds = when (resolvedTrafficWarmupMode) {
        WhiteDnsOptions.TrafficWarmupOff,
        WhiteDnsOptions.TrafficWarmupStartupOnly -> 0
        WhiteDnsOptions.TrafficWarmupBalanced -> 30
        WhiteDnsOptions.TrafficWarmupAggressive -> 5
        else -> customKeepaliveIntervalSeconds
    }

    return ResolvedWhiteDnsSettings(
        connectionMode = when (connectionMode) {
            "proxy", "vpn" -> connectionMode
            else -> "proxy"
        },
        protocolType = protocolType,
        resolverEntries = resolvers,
        listenIp = listenIp.trim().ifEmpty { "127.0.0.1" },
        listenPort = boundedInt(listenPort, defaultValue = 10886, minValue = 1, maxValue = 65535),
        httpProxyEnabled = httpProxyEnabled,
        httpProxyPort = boundedInt(httpProxyPort, defaultValue = 10887, minValue = 1, maxValue = 65535),
        socks5Authentication = resolvedSocks5Authentication,
        socksUsername = resolvedSocksUsername,
        socksPassword = resolvedSocksPassword,
        balancingStrategy = listOf(1, 2, 3, 4).firstOrNull { it == balancingStrategy } ?: 3,
        uploadDuplication = boundedInt(uploadDuplication, defaultValue = 3, minValue = 1, maxValue = 8),
        downloadDuplication = boundedInt(downloadDuplication, defaultValue = 7, minValue = 1, maxValue = 8),
        uploadCompression = uploadCompression.coerceIn(0, 3),
        downloadCompression = downloadCompression.coerceIn(0, 3),
        baseEncodeData = baseEncodeData,
        minUploadMtu = resolvedMinUploadMtu,
        minDownloadMtu = resolvedMinDownloadMtu,
        maxUploadMtu = resolvedMaxUploadMtu,
        maxDownloadMtu = resolvedMaxDownloadMtu,
        mtuTestRetriesResolvers = boundedInt(mtuTestRetriesResolvers, defaultValue = 3, minValue = 1, maxValue = 100),
        mtuTestTimeoutResolvers = positiveDouble(mtuTestTimeoutResolvers, defaultValue = 2.0),
        mtuTestParallelismResolvers = boundedInt(mtuTestParallelismResolvers, defaultValue = 100, minValue = 1, maxValue = 1024),
        mtuTestRetriesLogs = boundedInt(mtuTestRetriesLogs, defaultValue = 5, minValue = 1, maxValue = 100),
        mtuTestTimeoutLogs = positiveDouble(mtuTestTimeoutLogs, defaultValue = 2.0),
        mtuTestParallelismLogs = boundedInt(mtuTestParallelismLogs, defaultValue = 32, minValue = 1, maxValue = 1024),
        rxTxWorkers = resolvedRxTxWorkers,
        tunnelProcessWorkers = resolvedTunnelProcessWorkers,
        tunnelPacketTimeoutSeconds = boundedDouble(
            tunnelPacketTimeoutSeconds,
            defaultValue = 8.0,
            minValue = 0.5,
            maxValue = 120.0,
        ),
        dispatcherIdlePollIntervalSeconds = boundedDouble(
            dispatcherIdlePollIntervalSeconds,
            defaultValue = 0.020,
            minValue = 0.001,
            maxValue = 1.0,
        ),
        txChannelSize = boundedInt(txChannelSize, defaultValue = 2048, minValue = 64, maxValue = 65536),
        rxChannelSize = boundedInt(rxChannelSize, defaultValue = 2048, minValue = 64, maxValue = 65536),
        resolverUdpConnectionPoolSize = boundedInt(
            resolverUdpConnectionPoolSize,
            defaultValue = 64,
            minValue = 1,
            maxValue = 1024,
        ),
        streamQueueInitialCapacity = boundedInt(
            streamQueueInitialCapacity,
            defaultValue = 128,
            minValue = 8,
            maxValue = 65536,
        ),
        orphanQueueInitialCapacity = boundedInt(
            orphanQueueInitialCapacity,
            defaultValue = 32,
            minValue = 4,
            maxValue = 4096,
        ),
        dnsResponseFragmentStoreCapacity = boundedInt(
            dnsResponseFragmentStoreCapacity,
            defaultValue = 256,
            minValue = 16,
            maxValue = 16384,
        ),
        maxActiveStreams = boundedInt(
            maxActiveStreams,
            defaultValue = 2048,
            minValue = 1,
            maxValue = 65535,
        ),
        localHandshakeTimeoutSeconds = boundedPositiveDouble(
            localHandshakeTimeoutSeconds,
            defaultValue = 5.0,
            minValue = 0.5,
            maxValue = 60.0,
        ),
        socksUdpAssociateReadTimeoutSeconds = boundedDouble(
            socksUdpAssociateReadTimeoutSeconds,
            defaultValue = 30.0,
            minValue = 1.0,
            maxValue = 3600.0,
        ),
        clientTerminalStreamRetentionSeconds = boundedDouble(
            clientTerminalStreamRetentionSeconds,
            defaultValue = 45.0,
            minValue = 1.0,
            maxValue = 3600.0,
        ),
        clientCancelledSetupRetentionSeconds = boundedDouble(
            clientCancelledSetupRetentionSeconds,
            defaultValue = 120.0,
            minValue = 1.0,
            maxValue = 3600.0,
        ),
        sessionInitRetryBaseSeconds = resolvedSessionRetryBaseSeconds,
        sessionInitRetryStepSeconds = boundedDouble(
            sessionInitRetryStepSeconds,
            defaultValue = 1.0,
            minValue = 0.0,
            maxValue = 60.0,
        ),
        sessionInitRetryLinearAfter = boundedInt(
            sessionInitRetryLinearAfter,
            defaultValue = 5,
            minValue = 0,
            maxValue = 1000,
        ),
        sessionInitRetryMaxSeconds = boundedDouble(
            sessionInitRetryMaxSeconds,
            defaultValue = 60.0,
            minValue = resolvedSessionRetryBaseSeconds,
            maxValue = 3600.0,
        ),
        sessionInitBusyRetryIntervalSeconds = boundedDouble(
            sessionInitBusyRetryIntervalSeconds,
            defaultValue = 60.0,
            minValue = 1.0,
            maxValue = 3600.0,
        ),
        localDnsEnabled = localDnsEnabled,
        localDnsPort = boundedInt(localDnsPort, defaultValue = 53, minValue = 1, maxValue = 65535),
        startupMode = when (startupMode) {
            "ask", "resolvers", "logs" -> startupMode
            else -> "resolvers"
        },
        pingWatchdogSeconds = boundedInt(pingWatchdogSeconds, defaultValue = 300, minValue = 0, maxValue = 3600),
        trafficWarmupMode = resolvedTrafficWarmupMode,
        trafficWarmupEnabled = resolvedTrafficWarmupMode != WhiteDnsOptions.TrafficWarmupOff,
        trafficWarmupProbeCount = resolvedTrafficWarmupProbeCount,
        trafficKeepaliveIntervalSeconds = resolvedTrafficKeepaliveIntervalSeconds,
        vpnIpv6Strategy = resolvedVpnIpv6Strategy,
        vpnMtuPreset = resolvedVpnMtuPreset,
        vpnMtu = resolvedVpnMtu,
        splitTunnelMode = normalizeSplitTunnelMode(splitTunnelMode),
        splitTunnelPackages = normalizePackageNames(splitTunnelPackages),
        logLevel = when (logLevel) {
            "DEBUG", "INFO", "WARN", "ERROR" -> logLevel
            else -> "WARN"
        },
    )
}
