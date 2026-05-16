package shop.whitedns.client.model

data class WhiteDnsAutoTunePreset(
    val id: String,
    val label: String,
    val listenIp: String? = null,
    val listenPort: String? = null,
    val httpProxyEnabled: Boolean? = null,
    val httpProxyPort: String? = null,
    val socks5Authentication: Boolean? = null,
    val socksUsername: String? = null,
    val socksPassword: String? = null,
    val localDnsEnabled: Boolean? = null,
    val localDnsPort: String? = null,
    val balancingStrategy: Int? = null,
    val minUploadMtu: String,
    val maxUploadMtu: String,
    val minDownloadMtu: String,
    val maxDownloadMtu: String,
    val resolverTimeoutSeconds: String,
    val resolverRetries: String? = null,
    val resolverParallelism: String? = null,
    val logRetries: String? = null,
    val logTimeoutSeconds: String? = null,
    val logParallelism: String? = null,
    val dnsResponseFragmentStoreCapacity: String,
    val uploadDuplication: String,
    val downloadDuplication: String,
    val uploadCompression: Int,
    val downloadCompression: Int,
    val baseEncodeData: Boolean? = null,
    val rxTxWorkers: String? = null,
    val tunnelProcessWorkers: String? = null,
    val tunnelPacketTimeoutSeconds: String? = null,
    val dispatcherIdlePollIntervalSeconds: String? = null,
    val txChannelSize: String? = null,
    val rxChannelSize: String? = null,
    val resolverUdpConnectionPoolSize: String? = null,
    val streamQueueInitialCapacity: String? = null,
    val orphanQueueInitialCapacity: String? = null,
    val maxActiveStreams: String? = null,
    val localHandshakeTimeoutSeconds: String? = null,
    val socksUdpAssociateReadTimeoutSeconds: String? = null,
    val clientTerminalStreamRetentionSeconds: String? = null,
    val clientCancelledSetupRetentionSeconds: String? = null,
    val sessionInitRetryBaseSeconds: String? = null,
    val sessionInitRetryStepSeconds: String? = null,
    val sessionInitRetryLinearAfter: String? = null,
    val sessionInitRetryMaxSeconds: String? = null,
    val sessionInitBusyRetryIntervalSeconds: String? = null,
    val startupMode: String? = null,
    val pingWatchdogSeconds: String? = null,
    val trafficWarmupEnabled: Boolean? = null,
    val trafficWarmupProbeCount: String? = null,
    val trafficKeepaliveIntervalSeconds: String? = null,
    val logLevel: String? = null,
)

object WhiteDnsAutoTunePresets {
    val all: List<WhiteDnsAutoTunePreset> = listOf(
        WhiteDnsAutoTunePreset(
            id = "auto-1",
            label = "WhiteDNS Config 1",
            minUploadMtu = "100",
            maxUploadMtu = "1000",
            minDownloadMtu = "200",
            maxDownloadMtu = "4000",
            resolverTimeoutSeconds = "0.5",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "15",
            downloadDuplication = "30",
            uploadCompression = 2,
            downloadCompression = 3,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-2",
            label = "WhiteDNS Config 2",
            minUploadMtu = "100",
            maxUploadMtu = "500",
            minDownloadMtu = "100",
            maxDownloadMtu = "1325",
            resolverTimeoutSeconds = "0.5",
            dnsResponseFragmentStoreCapacity = "250",
            uploadDuplication = "5",
            downloadDuplication = "15",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-3",
            label = "WhiteDNS Config 3",
            minUploadMtu = "50",
            maxUploadMtu = "1500",
            minDownloadMtu = "50",
            maxDownloadMtu = "500",
            resolverTimeoutSeconds = "0.2",
            dnsResponseFragmentStoreCapacity = "200",
            uploadDuplication = "1",
            downloadDuplication = "1",
            uploadCompression = 0,
            downloadCompression = 0,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-4",
            label = "WhiteDNS Config 4",
            minUploadMtu = "20",
            maxUploadMtu = "325",
            minDownloadMtu = "100",
            maxDownloadMtu = "1270",
            resolverTimeoutSeconds = "0.5",
            dnsResponseFragmentStoreCapacity = "100",
            uploadDuplication = "1",
            downloadDuplication = "5",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-5",
            label = "WhiteDNS Config 5",
            minUploadMtu = "50",
            maxUploadMtu = "500",
            minDownloadMtu = "100",
            maxDownloadMtu = "9000",
            resolverTimeoutSeconds = "0.3",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "1",
            downloadDuplication = "1",
            uploadCompression = 0,
            downloadCompression = 0,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-6",
            label = "WhiteDNS Config 6",
            minUploadMtu = "40",
            maxUploadMtu = "200",
            minDownloadMtu = "100",
            maxDownloadMtu = "1500",
            resolverTimeoutSeconds = "0.5",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "5",
            downloadDuplication = "5",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "auto-7",
            label = "WhiteDNS Config 7",
            minUploadMtu = "64",
            maxUploadMtu = "64",
            minDownloadMtu = "756",
            maxDownloadMtu = "756",
            resolverTimeoutSeconds = "0.7",
            dnsResponseFragmentStoreCapacity = "256",
            uploadDuplication = "15",
            downloadDuplication = "30",
            uploadCompression = 2,
            downloadCompression = 2,
        ),
        WhiteDnsAutoTunePreset(
            id = "field-milad-telegram",
            label = "Milad Telegram Proxy",
            listenIp = "127.0.0.1",
            listenPort = "10886",
            httpProxyEnabled = true,
            httpProxyPort = "10887",
            socks5Authentication = false,
            socksUsername = "master_dns_vpn",
            socksPassword = "master_dns_vpn",
            localDnsEnabled = false,
            localDnsPort = "53",
            balancingStrategy = 3,
            minUploadMtu = "5",
            maxUploadMtu = "30",
            minDownloadMtu = "300",
            maxDownloadMtu = "300",
            resolverTimeoutSeconds = "3.0",
            resolverRetries = "3",
            resolverParallelism = "100",
            logRetries = "5",
            logTimeoutSeconds = "2.0",
            logParallelism = "32",
            dnsResponseFragmentStoreCapacity = "64",
            uploadDuplication = "3",
            downloadDuplication = "4",
            uploadCompression = 2,
            downloadCompression = 2,
            baseEncodeData = false,
            rxTxWorkers = "4",
            tunnelProcessWorkers = "4",
            tunnelPacketTimeoutSeconds = "10.0",
            dispatcherIdlePollIntervalSeconds = "0.02",
            txChannelSize = "2048",
            rxChannelSize = "2048",
            resolverUdpConnectionPoolSize = "64",
            streamQueueInitialCapacity = "128",
            orphanQueueInitialCapacity = "32",
            maxActiveStreams = "2048",
            localHandshakeTimeoutSeconds = "5.0",
            socksUdpAssociateReadTimeoutSeconds = "30.0",
            clientTerminalStreamRetentionSeconds = "45.0",
            clientCancelledSetupRetentionSeconds = "120.0",
            sessionInitRetryBaseSeconds = "1.0",
            sessionInitRetryStepSeconds = "1.0",
            sessionInitRetryLinearAfter = "5",
            sessionInitRetryMaxSeconds = "60.0",
            sessionInitBusyRetryIntervalSeconds = "60.0",
            startupMode = "resolvers",
            pingWatchdogSeconds = "30",
            trafficWarmupEnabled = true,
            trafficWarmupProbeCount = "4",
            trafficKeepaliveIntervalSeconds = "5",
            logLevel = "WARN",
        ),
    )
}

object WhiteDnsParallelTest {
    const val EnabledByDefault = false
    const val MaxSelectedConfigs = 10
    private const val WhiteDnsConfigPrefix = "whitedns:"
    private const val SettingConfigPrefix = "setting:"

    val defaultConfigIds: List<String>
        get() = WhiteDnsAutoTunePresets.all.map { whiteDnsConfigId(it.id) }

    fun whiteDnsConfigId(presetId: String): String = "$WhiteDnsConfigPrefix$presetId"

    fun settingConfigId(profileId: String): String = "$SettingConfigPrefix$profileId"

    fun presetIdFromConfigId(configId: String): String? {
        val normalized = normalizeLegacyConfigId(configId)
        return normalized.removePrefix(WhiteDnsConfigPrefix)
            .takeIf { normalized.startsWith(WhiteDnsConfigPrefix) && it.isNotBlank() }
    }

    fun settingProfileIdFromConfigId(configId: String): String? {
        return configId.removePrefix(SettingConfigPrefix)
            .takeIf { configId.startsWith(SettingConfigPrefix) && it.isNotBlank() }
    }

    fun normalizeConfigIds(
        configIds: List<String>,
        advancedProfiles: List<AdvancedSettingsProfile>,
        defaultIfEmpty: Boolean = true,
    ): List<String> {
        val whiteDnsIds = defaultConfigIds
        val settingIds = advancedProfiles
            .filter { it.id.isNotBlank() && it.id != AdvancedSettingsProfile.DefaultId }
            .map { settingConfigId(it.id) }
        val availableIds = (whiteDnsIds + settingIds).toSet()
        val requestedIds = if (configIds.isEmpty() && defaultIfEmpty) {
            whiteDnsIds
        } else {
            configIds
        }
        return requestedIds
            .map(::normalizeLegacyConfigId)
            .distinct()
            .filter { it in availableIds }
            .take(MaxSelectedConfigs)
            .ifEmpty {
                if (defaultIfEmpty) {
                    whiteDnsIds.take(MaxSelectedConfigs)
                } else {
                    emptyList()
                }
            }
    }

    private fun normalizeLegacyConfigId(configId: String): String {
        return if (WhiteDnsAutoTunePresets.all.any { it.id == configId }) {
            whiteDnsConfigId(configId)
        } else {
            configId
        }
    }
}

fun WhiteDnsSettings.applyAutoTunePreset(preset: WhiteDnsAutoTunePreset): WhiteDnsSettings {
    return copy(
        listenIp = preset.listenIp ?: listenIp,
        listenPort = preset.listenPort ?: listenPort,
        httpProxyEnabled = preset.httpProxyEnabled ?: httpProxyEnabled,
        httpProxyPort = preset.httpProxyPort ?: httpProxyPort,
        socks5Authentication = preset.socks5Authentication ?: socks5Authentication,
        socksUsername = preset.socksUsername ?: socksUsername,
        socksPassword = preset.socksPassword ?: socksPassword,
        localDnsEnabled = preset.localDnsEnabled ?: localDnsEnabled,
        localDnsPort = preset.localDnsPort ?: localDnsPort,
        balancingStrategy = preset.balancingStrategy ?: balancingStrategy,
        minUploadMtu = preset.minUploadMtu,
        maxUploadMtu = preset.maxUploadMtu,
        minDownloadMtu = preset.minDownloadMtu,
        maxDownloadMtu = preset.maxDownloadMtu,
        mtuTestTimeoutResolvers = preset.resolverTimeoutSeconds,
        mtuTestRetriesResolvers = preset.resolverRetries ?: mtuTestRetriesResolvers,
        mtuTestParallelismResolvers = preset.resolverParallelism ?: mtuTestParallelismResolvers,
        mtuTestRetriesLogs = preset.logRetries ?: mtuTestRetriesLogs,
        mtuTestTimeoutLogs = preset.logTimeoutSeconds ?: preset.resolverTimeoutSeconds,
        mtuTestParallelismLogs = preset.logParallelism ?: mtuTestParallelismLogs,
        dnsResponseFragmentStoreCapacity = preset.dnsResponseFragmentStoreCapacity,
        uploadDuplication = preset.uploadDuplication,
        downloadDuplication = preset.downloadDuplication,
        uploadCompression = preset.uploadCompression,
        downloadCompression = preset.downloadCompression,
        baseEncodeData = preset.baseEncodeData ?: baseEncodeData,
        rxTxWorkers = preset.rxTxWorkers ?: rxTxWorkers,
        tunnelProcessWorkers = preset.tunnelProcessWorkers ?: tunnelProcessWorkers,
        tunnelPacketTimeoutSeconds = preset.tunnelPacketTimeoutSeconds ?: tunnelPacketTimeoutSeconds,
        dispatcherIdlePollIntervalSeconds = preset.dispatcherIdlePollIntervalSeconds ?: dispatcherIdlePollIntervalSeconds,
        txChannelSize = preset.txChannelSize ?: txChannelSize,
        rxChannelSize = preset.rxChannelSize ?: rxChannelSize,
        resolverUdpConnectionPoolSize = preset.resolverUdpConnectionPoolSize ?: resolverUdpConnectionPoolSize,
        streamQueueInitialCapacity = preset.streamQueueInitialCapacity ?: streamQueueInitialCapacity,
        orphanQueueInitialCapacity = preset.orphanQueueInitialCapacity ?: orphanQueueInitialCapacity,
        maxActiveStreams = preset.maxActiveStreams ?: maxActiveStreams,
        localHandshakeTimeoutSeconds = preset.localHandshakeTimeoutSeconds ?: localHandshakeTimeoutSeconds,
        socksUdpAssociateReadTimeoutSeconds = preset.socksUdpAssociateReadTimeoutSeconds
            ?: socksUdpAssociateReadTimeoutSeconds,
        clientTerminalStreamRetentionSeconds = preset.clientTerminalStreamRetentionSeconds
            ?: clientTerminalStreamRetentionSeconds,
        clientCancelledSetupRetentionSeconds = preset.clientCancelledSetupRetentionSeconds
            ?: clientCancelledSetupRetentionSeconds,
        sessionInitRetryBaseSeconds = preset.sessionInitRetryBaseSeconds ?: sessionInitRetryBaseSeconds,
        sessionInitRetryStepSeconds = preset.sessionInitRetryStepSeconds ?: sessionInitRetryStepSeconds,
        sessionInitRetryLinearAfter = preset.sessionInitRetryLinearAfter ?: sessionInitRetryLinearAfter,
        sessionInitRetryMaxSeconds = preset.sessionInitRetryMaxSeconds ?: sessionInitRetryMaxSeconds,
        sessionInitBusyRetryIntervalSeconds = preset.sessionInitBusyRetryIntervalSeconds
            ?: sessionInitBusyRetryIntervalSeconds,
        startupMode = preset.startupMode ?: startupMode,
        pingWatchdogSeconds = preset.pingWatchdogSeconds ?: pingWatchdogSeconds,
        trafficWarmupEnabled = preset.trafficWarmupEnabled ?: trafficWarmupEnabled,
        trafficWarmupProbeCount = preset.trafficWarmupProbeCount ?: trafficWarmupProbeCount,
        trafficKeepaliveIntervalSeconds = preset.trafficKeepaliveIntervalSeconds
            ?: trafficKeepaliveIntervalSeconds,
        logLevel = preset.logLevel ?: logLevel,
    ).syncSelectedConnectionProfileFields()
}

fun WhiteDnsSettings.recoverPersistedParallelTestPreset(): WhiteDnsSettings {
    val settings = syncSelectedConnectionProfileFields()
    if (!settings.matchesAutoTunePresetFields()) {
        return settings
    }

    val selectedProfile = settings.selectedAdvancedProfile()
    return when {
        selectedProfile.id != AdvancedSettingsProfile.DefaultId &&
            settings.matchesAdvancedProfile(selectedProfile) -> settings

        selectedProfile.id != AdvancedSettingsProfile.DefaultId -> settings
            .applyAdvancedProfile(selectedProfile)
            .copy(
                autoTuneEnabled = settings.autoTuneEnabled,
                parallelTestSelectedConfigIds = settings.parallelTestSelectedConfigIds,
            )
            .syncSelectedConnectionProfileFields()

        else -> settings
            .copyAutoTunePresetFieldsFrom(WhiteDnsSettings())
            .syncSelectedConnectionProfileFields()
    }
}

private fun WhiteDnsSettings.matchesAutoTunePresetFields(): Boolean {
    return WhiteDnsAutoTunePresets.all.any { preset ->
        minUploadMtu == preset.minUploadMtu &&
            maxUploadMtu == preset.maxUploadMtu &&
            minDownloadMtu == preset.minDownloadMtu &&
            maxDownloadMtu == preset.maxDownloadMtu &&
            mtuTestTimeoutResolvers == preset.resolverTimeoutSeconds &&
            mtuTestTimeoutLogs == preset.resolverTimeoutSeconds &&
            dnsResponseFragmentStoreCapacity == preset.dnsResponseFragmentStoreCapacity &&
            uploadDuplication == preset.uploadDuplication &&
            downloadDuplication == preset.downloadDuplication &&
            uploadCompression == preset.uploadCompression &&
            downloadCompression == preset.downloadCompression
    }
}

private fun WhiteDnsSettings.copyAutoTunePresetFieldsFrom(source: WhiteDnsSettings): WhiteDnsSettings {
    return copy(
        minUploadMtu = source.minUploadMtu,
        maxUploadMtu = source.maxUploadMtu,
        minDownloadMtu = source.minDownloadMtu,
        maxDownloadMtu = source.maxDownloadMtu,
        mtuTestTimeoutResolvers = source.mtuTestTimeoutResolvers,
        mtuTestTimeoutLogs = source.mtuTestTimeoutLogs,
        dnsResponseFragmentStoreCapacity = source.dnsResponseFragmentStoreCapacity,
        uploadDuplication = source.uploadDuplication,
        downloadDuplication = source.downloadDuplication,
        uploadCompression = source.uploadCompression,
        downloadCompression = source.downloadCompression,
    )
}
