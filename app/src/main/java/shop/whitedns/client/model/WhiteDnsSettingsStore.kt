package shop.whitedns.client.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class WhiteDnsSettingsStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    fun load(): WhiteDnsSettings {
        val defaults = WhiteDnsSettings()
        migrateAdvancedDefaultsIfNeeded()
        val resolverText = preferences.getString(KeyResolverText, defaults.resolverText) ?: defaults.resolverText
        val legacyServerMode = defaults.serverMode
        val legacyCustomServerDomain = preferences.getString(KeyCustomServerDomain, defaults.customServerDomain)
            ?: defaults.customServerDomain
        val legacyCustomServerEncryptionKey = preferences.getString(
            KeyCustomServerEncryptionKey,
            defaults.customServerEncryptionKey,
        ) ?: defaults.customServerEncryptionKey
        val legacyCustomServerEncryptionMethod = preferences.getInt(
            KeyCustomServerEncryptionMethod,
            defaults.customServerEncryptionMethod,
        )
        val legacyConnectionMode = preferences.getString(KeyConnectionMode, defaults.connectionMode) ?: defaults.connectionMode
        val legacyProfile = ConnectionProfile(
            id = ConnectionProfile.DefaultId,
            name = "Connection",
            serverMode = "custom",
            customServerDomain = legacyCustomServerDomain,
            customServerEncryptionKey = legacyCustomServerEncryptionKey,
            customServerEncryptionMethod = legacyCustomServerEncryptionMethod,
            connectionMode = legacyConnectionMode,
        )
        val connectionProfiles = decodeConnectionProfiles(
            raw = preferences.getString(KeyConnectionProfiles, null),
            fallbackProfile = legacyProfile,
        )
        val resolverProfiles = decodeResolverProfiles(
            raw = preferences.getString(KeyResolverProfiles, null),
        )
        val advancedProfiles = decodeAdvancedProfiles(
            raw = preferences.getString(KeyAdvancedProfiles, null),
        )
        return WhiteDnsSettings(
            selectedConnectionProfileId = preferences.getString(
                KeySelectedConnectionProfileId,
                connectionProfiles.first().id,
            ) ?: connectionProfiles.first().id,
            connectionProfiles = connectionProfiles,
            selectedResolverProfileId = preferences.getString(KeySelectedResolverProfileId, defaults.selectedResolverProfileId)
                ?: defaults.selectedResolverProfileId,
            resolverProfiles = resolverProfiles,
            selectedAdvancedProfileId = preferences.getString(
                KeySelectedAdvancedProfileId,
                defaults.selectedAdvancedProfileId,
            ) ?: defaults.selectedAdvancedProfileId,
            advancedProfiles = advancedProfiles,
            serverMode = legacyServerMode,
            customServerDomain = legacyCustomServerDomain,
            customServerEncryptionKey = legacyCustomServerEncryptionKey,
            customServerEncryptionMethod = legacyCustomServerEncryptionMethod,
            connectionMode = legacyConnectionMode,
            protocolType = preferences.getString(KeyProtocolType, defaults.protocolType) ?: defaults.protocolType,
            resolverText = if (resolverText == LegacyDefaultResolverText) defaults.resolverText else resolverText,
            listenIp = preferences.getString(KeyListenIp, defaults.listenIp) ?: defaults.listenIp,
            listenPort = preferences.getString(KeyListenPort, defaults.listenPort) ?: defaults.listenPort,
            httpProxyEnabled = preferences.getBoolean(KeyHttpProxyEnabled, defaults.httpProxyEnabled),
            httpProxyPort = preferences.getString(KeyHttpProxyPort, defaults.httpProxyPort) ?: defaults.httpProxyPort,
            socks5Authentication = preferences.getBoolean(KeySocks5Authentication, defaults.socks5Authentication),
            socksUsername = preferences.getString(KeySocksUsername, defaults.socksUsername) ?: defaults.socksUsername,
            socksPassword = preferences.getString(KeySocksPassword, defaults.socksPassword) ?: defaults.socksPassword,
            balancingStrategy = preferences.getInt(KeyBalancingStrategy, defaults.balancingStrategy),
            uploadDuplication = preferences.getString(KeyUploadDuplication, defaults.uploadDuplication) ?: defaults.uploadDuplication,
            downloadDuplication = preferences.getString(KeyDownloadDuplication, defaults.downloadDuplication) ?: defaults.downloadDuplication,
            uploadCompression = preferences.getInt(KeyUploadCompression, defaults.uploadCompression),
            downloadCompression = preferences.getInt(KeyDownloadCompression, defaults.downloadCompression),
            baseEncodeData = preferences.getBoolean(KeyBaseEncodeData, defaults.baseEncodeData),
            minUploadMtu = preferences.getString(KeyMinUploadMtu, defaults.minUploadMtu) ?: defaults.minUploadMtu,
            minDownloadMtu = preferences.getString(KeyMinDownloadMtu, defaults.minDownloadMtu) ?: defaults.minDownloadMtu,
            maxUploadMtu = preferences.getString(KeyMaxUploadMtu, defaults.maxUploadMtu) ?: defaults.maxUploadMtu,
            maxDownloadMtu = preferences.getString(KeyMaxDownloadMtu, defaults.maxDownloadMtu) ?: defaults.maxDownloadMtu,
            mtuTestRetriesResolvers = preferences.getString(KeyMtuTestRetriesResolvers, defaults.mtuTestRetriesResolvers)
                ?: defaults.mtuTestRetriesResolvers,
            mtuTestTimeoutResolvers = preferences.getString(KeyMtuTestTimeoutResolvers, defaults.mtuTestTimeoutResolvers)
                ?: defaults.mtuTestTimeoutResolvers,
            mtuTestParallelismResolvers = preferences.getString(KeyMtuTestParallelismResolvers, defaults.mtuTestParallelismResolvers)
                ?: defaults.mtuTestParallelismResolvers,
            mtuTestRetriesLogs = preferences.getString(KeyMtuTestRetriesLogs, defaults.mtuTestRetriesLogs)
                ?: defaults.mtuTestRetriesLogs,
            mtuTestTimeoutLogs = preferences.getString(KeyMtuTestTimeoutLogs, defaults.mtuTestTimeoutLogs)
                ?: defaults.mtuTestTimeoutLogs,
            mtuTestParallelismLogs = preferences.getString(KeyMtuTestParallelismLogs, defaults.mtuTestParallelismLogs)
                ?: defaults.mtuTestParallelismLogs,
            rxTxWorkers = preferences.getString(KeyRxTxWorkers, defaults.rxTxWorkers) ?: defaults.rxTxWorkers,
            tunnelProcessWorkers = preferences.getString(KeyTunnelProcessWorkers, defaults.tunnelProcessWorkers)
                ?: defaults.tunnelProcessWorkers,
            tunnelPacketTimeoutSeconds = preferences.getString(
                KeyTunnelPacketTimeoutSeconds,
                defaults.tunnelPacketTimeoutSeconds,
            ) ?: defaults.tunnelPacketTimeoutSeconds,
            dispatcherIdlePollIntervalSeconds = preferences.getString(
                KeyDispatcherIdlePollIntervalSeconds,
                defaults.dispatcherIdlePollIntervalSeconds,
            ) ?: defaults.dispatcherIdlePollIntervalSeconds,
            txChannelSize = preferences.getString(KeyTxChannelSize, defaults.txChannelSize) ?: defaults.txChannelSize,
            rxChannelSize = preferences.getString(KeyRxChannelSize, defaults.rxChannelSize) ?: defaults.rxChannelSize,
            resolverUdpConnectionPoolSize = preferences.getString(
                KeyResolverUdpConnectionPoolSize,
                defaults.resolverUdpConnectionPoolSize,
            ) ?: defaults.resolverUdpConnectionPoolSize,
            streamQueueInitialCapacity = preferences.getString(
                KeyStreamQueueInitialCapacity,
                defaults.streamQueueInitialCapacity,
            ) ?: defaults.streamQueueInitialCapacity,
            orphanQueueInitialCapacity = preferences.getString(
                KeyOrphanQueueInitialCapacity,
                defaults.orphanQueueInitialCapacity,
            ) ?: defaults.orphanQueueInitialCapacity,
            dnsResponseFragmentStoreCapacity = preferences.getString(
                KeyDnsResponseFragmentStoreCapacity,
                defaults.dnsResponseFragmentStoreCapacity,
            ) ?: defaults.dnsResponseFragmentStoreCapacity,
            maxActiveStreams = preferences.getString(KeyMaxActiveStreams, defaults.maxActiveStreams)
                ?: defaults.maxActiveStreams,
            localHandshakeTimeoutSeconds = preferences.getString(
                KeyLocalHandshakeTimeoutSeconds,
                defaults.localHandshakeTimeoutSeconds,
            ) ?: defaults.localHandshakeTimeoutSeconds,
            socksUdpAssociateReadTimeoutSeconds = preferences.getString(
                KeySocksUdpAssociateReadTimeoutSeconds,
                defaults.socksUdpAssociateReadTimeoutSeconds,
            ) ?: defaults.socksUdpAssociateReadTimeoutSeconds,
            clientTerminalStreamRetentionSeconds = preferences.getString(
                KeyClientTerminalStreamRetentionSeconds,
                defaults.clientTerminalStreamRetentionSeconds,
            ) ?: defaults.clientTerminalStreamRetentionSeconds,
            clientCancelledSetupRetentionSeconds = preferences.getString(
                KeyClientCancelledSetupRetentionSeconds,
                defaults.clientCancelledSetupRetentionSeconds,
            ) ?: defaults.clientCancelledSetupRetentionSeconds,
            sessionInitRetryBaseSeconds = preferences.getString(
                KeySessionInitRetryBaseSeconds,
                defaults.sessionInitRetryBaseSeconds,
            ) ?: defaults.sessionInitRetryBaseSeconds,
            sessionInitRetryStepSeconds = preferences.getString(
                KeySessionInitRetryStepSeconds,
                defaults.sessionInitRetryStepSeconds,
            ) ?: defaults.sessionInitRetryStepSeconds,
            sessionInitRetryLinearAfter = preferences.getString(
                KeySessionInitRetryLinearAfter,
                defaults.sessionInitRetryLinearAfter,
            ) ?: defaults.sessionInitRetryLinearAfter,
            sessionInitRetryMaxSeconds = preferences.getString(
                KeySessionInitRetryMaxSeconds,
                defaults.sessionInitRetryMaxSeconds,
            ) ?: defaults.sessionInitRetryMaxSeconds,
            sessionInitBusyRetryIntervalSeconds = preferences.getString(
                KeySessionInitBusyRetryIntervalSeconds,
                defaults.sessionInitBusyRetryIntervalSeconds,
            ) ?: defaults.sessionInitBusyRetryIntervalSeconds,
            localDnsEnabled = preferences.getBoolean(KeyLocalDnsEnabled, defaults.localDnsEnabled),
            localDnsPort = preferences.getString(KeyLocalDnsPort, defaults.localDnsPort) ?: defaults.localDnsPort,
            startupMode = preferences.getString(KeyStartupMode, defaults.startupMode) ?: defaults.startupMode,
            pingWatchdogSeconds = preferences.getString(KeyPingWatchdogSeconds, defaults.pingWatchdogSeconds)
                ?: defaults.pingWatchdogSeconds,
            trafficWarmupMode = preferences.getString(KeyTrafficWarmupMode, defaults.trafficWarmupMode)
                ?: defaults.trafficWarmupMode,
            trafficWarmupEnabled = preferences.getBoolean(KeyTrafficWarmupEnabled, defaults.trafficWarmupEnabled),
            trafficWarmupProbeCount = preferences.getString(
                KeyTrafficWarmupProbeCount,
                defaults.trafficWarmupProbeCount,
            ) ?: defaults.trafficWarmupProbeCount,
            trafficKeepaliveIntervalSeconds = preferences.getString(
                KeyTrafficKeepaliveIntervalSeconds,
                defaults.trafficKeepaliveIntervalSeconds,
            ) ?: defaults.trafficKeepaliveIntervalSeconds,
            vpnIpv6Strategy = preferences.getString(KeyVpnIpv6Strategy, defaults.vpnIpv6Strategy)
                ?: defaults.vpnIpv6Strategy,
            vpnMtuPreset = preferences.getString(KeyVpnMtuPreset, defaults.vpnMtuPreset)
                ?: defaults.vpnMtuPreset,
            vpnCustomMtu = preferences.getString(KeyVpnCustomMtu, defaults.vpnCustomMtu)
                ?: defaults.vpnCustomMtu,
            fullVpnPerformanceWarningDismissed = preferences.getBoolean(
                KeyFullVpnPerformanceWarningDismissed,
                defaults.fullVpnPerformanceWarningDismissed,
            ),
            splitTunnelMode = preferences.getString(KeySplitTunnelMode, defaults.splitTunnelMode)
                ?: defaults.splitTunnelMode,
            splitTunnelPackages = decodePackageNames(preferences.getString(KeySplitTunnelPackages, null)),
            logLevel = preferences.getString(KeyLogLevel, defaults.logLevel) ?: defaults.logLevel,
        ).syncSelectedConnectionProfileFields()
    }

    fun save(settings: WhiteDnsSettings) {
        val normalizedSettings = settings.syncSelectedConnectionProfileFields()
        preferences.edit()
            .putString(KeySelectedConnectionProfileId, normalizedSettings.selectedConnectionProfileId)
            .putString(KeyConnectionProfiles, encodeConnectionProfiles(normalizedSettings.connectionProfiles))
            .putString(KeySelectedResolverProfileId, normalizedSettings.selectedResolverProfileId)
            .putString(KeyResolverProfiles, encodeResolverProfiles(normalizedSettings.resolverProfiles))
            .putString(KeySelectedAdvancedProfileId, normalizedSettings.selectedAdvancedProfileId)
            .putString(KeyAdvancedProfiles, encodeAdvancedProfiles(normalizedSettings.advancedProfiles))
            .putString(KeyServerMode, normalizedSettings.serverMode)
            .putString(KeyCustomServerDomain, normalizedSettings.customServerDomain)
            .putString(KeyCustomServerEncryptionKey, normalizedSettings.customServerEncryptionKey)
            .putInt(KeyCustomServerEncryptionMethod, normalizedSettings.customServerEncryptionMethod)
            .putString(KeyConnectionMode, normalizedSettings.connectionMode)
            .putString(KeyProtocolType, normalizedSettings.protocolType)
            .putString(KeyResolverText, normalizedSettings.resolverText)
            .putString(KeyListenIp, normalizedSettings.listenIp)
            .putString(KeyListenPort, normalizedSettings.listenPort)
            .putBoolean(KeyHttpProxyEnabled, normalizedSettings.httpProxyEnabled)
            .putString(KeyHttpProxyPort, normalizedSettings.httpProxyPort)
            .putBoolean(KeySocks5Authentication, normalizedSettings.socks5Authentication)
            .putString(KeySocksUsername, normalizedSettings.socksUsername)
            .putString(KeySocksPassword, normalizedSettings.socksPassword)
            .putInt(KeyBalancingStrategy, normalizedSettings.balancingStrategy)
            .putString(KeyUploadDuplication, normalizedSettings.uploadDuplication)
            .putString(KeyDownloadDuplication, normalizedSettings.downloadDuplication)
            .putInt(KeyUploadCompression, normalizedSettings.uploadCompression)
            .putInt(KeyDownloadCompression, normalizedSettings.downloadCompression)
            .putBoolean(KeyBaseEncodeData, normalizedSettings.baseEncodeData)
            .putString(KeyMinUploadMtu, normalizedSettings.minUploadMtu)
            .putString(KeyMinDownloadMtu, normalizedSettings.minDownloadMtu)
            .putString(KeyMaxUploadMtu, normalizedSettings.maxUploadMtu)
            .putString(KeyMaxDownloadMtu, normalizedSettings.maxDownloadMtu)
            .putString(KeyMtuTestRetriesResolvers, normalizedSettings.mtuTestRetriesResolvers)
            .putString(KeyMtuTestTimeoutResolvers, normalizedSettings.mtuTestTimeoutResolvers)
            .putString(KeyMtuTestParallelismResolvers, normalizedSettings.mtuTestParallelismResolvers)
            .putString(KeyMtuTestRetriesLogs, normalizedSettings.mtuTestRetriesLogs)
            .putString(KeyMtuTestTimeoutLogs, normalizedSettings.mtuTestTimeoutLogs)
            .putString(KeyMtuTestParallelismLogs, normalizedSettings.mtuTestParallelismLogs)
            .putString(KeyRxTxWorkers, normalizedSettings.rxTxWorkers)
            .putString(KeyTunnelProcessWorkers, normalizedSettings.tunnelProcessWorkers)
            .putString(KeyTunnelPacketTimeoutSeconds, normalizedSettings.tunnelPacketTimeoutSeconds)
            .putString(KeyDispatcherIdlePollIntervalSeconds, normalizedSettings.dispatcherIdlePollIntervalSeconds)
            .putString(KeyTxChannelSize, normalizedSettings.txChannelSize)
            .putString(KeyRxChannelSize, normalizedSettings.rxChannelSize)
            .putString(KeyResolverUdpConnectionPoolSize, normalizedSettings.resolverUdpConnectionPoolSize)
            .putString(KeyStreamQueueInitialCapacity, normalizedSettings.streamQueueInitialCapacity)
            .putString(KeyOrphanQueueInitialCapacity, normalizedSettings.orphanQueueInitialCapacity)
            .putString(KeyDnsResponseFragmentStoreCapacity, normalizedSettings.dnsResponseFragmentStoreCapacity)
            .putString(KeyMaxActiveStreams, normalizedSettings.maxActiveStreams)
            .putString(KeyLocalHandshakeTimeoutSeconds, normalizedSettings.localHandshakeTimeoutSeconds)
            .putString(KeySocksUdpAssociateReadTimeoutSeconds, normalizedSettings.socksUdpAssociateReadTimeoutSeconds)
            .putString(KeyClientTerminalStreamRetentionSeconds, normalizedSettings.clientTerminalStreamRetentionSeconds)
            .putString(KeyClientCancelledSetupRetentionSeconds, normalizedSettings.clientCancelledSetupRetentionSeconds)
            .putString(KeySessionInitRetryBaseSeconds, normalizedSettings.sessionInitRetryBaseSeconds)
            .putString(KeySessionInitRetryStepSeconds, normalizedSettings.sessionInitRetryStepSeconds)
            .putString(KeySessionInitRetryLinearAfter, normalizedSettings.sessionInitRetryLinearAfter)
            .putString(KeySessionInitRetryMaxSeconds, normalizedSettings.sessionInitRetryMaxSeconds)
            .putString(KeySessionInitBusyRetryIntervalSeconds, normalizedSettings.sessionInitBusyRetryIntervalSeconds)
            .putBoolean(KeyLocalDnsEnabled, normalizedSettings.localDnsEnabled)
            .putString(KeyLocalDnsPort, normalizedSettings.localDnsPort)
            .putString(KeyStartupMode, normalizedSettings.startupMode)
            .putString(KeyPingWatchdogSeconds, normalizedSettings.pingWatchdogSeconds)
            .putString(KeyTrafficWarmupMode, normalizedSettings.trafficWarmupMode)
            .putBoolean(KeyTrafficWarmupEnabled, normalizedSettings.trafficWarmupEnabled)
            .putString(KeyTrafficWarmupProbeCount, normalizedSettings.trafficWarmupProbeCount)
            .putString(KeyTrafficKeepaliveIntervalSeconds, normalizedSettings.trafficKeepaliveIntervalSeconds)
            .putString(KeyVpnIpv6Strategy, normalizedSettings.vpnIpv6Strategy)
            .putString(KeyVpnMtuPreset, normalizedSettings.vpnMtuPreset)
            .putString(KeyVpnCustomMtu, normalizedSettings.vpnCustomMtu)
            .putBoolean(
                KeyFullVpnPerformanceWarningDismissed,
                normalizedSettings.fullVpnPerformanceWarningDismissed,
            )
            .putString(KeySplitTunnelMode, normalizedSettings.splitTunnelMode)
            .putString(KeySplitTunnelPackages, encodePackageNames(normalizedSettings.splitTunnelPackages))
            .putString(KeyLogLevel, normalizedSettings.logLevel)
            .apply()
    }

    private fun decodeConnectionProfiles(
        raw: String?,
        fallbackProfile: ConnectionProfile,
    ): List<ConnectionProfile> {
        if (raw.isNullOrBlank()) {
            return listOf(fallbackProfile)
        }
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                ConnectionProfile(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    serverMode = item.optString("serverMode", "custom"),
                    customServerDomain = item.optString("customServerDomain"),
                    customServerEncryptionKey = item.optString("customServerEncryptionKey"),
                    customServerEncryptionMethod = item.optInt("customServerEncryptionMethod", 1),
                    resolverProfileId = item.optString("resolverProfileId"),
                    connectionMode = item.optString("connectionMode", "proxy"),
                )
            }
                .filter { it.id.isNotBlank() }
                .ifEmpty { listOf(fallbackProfile) }
        }.getOrDefault(listOf(fallbackProfile))
    }

    private fun encodeConnectionProfiles(profiles: List<ConnectionProfile>): String {
        val array = JSONArray()
        profiles.forEach { profile ->
            array.put(
                JSONObject()
                    .put("id", profile.id)
                    .put("name", profile.name)
                    .put("serverMode", profile.serverMode)
                    .put("customServerDomain", profile.customServerDomain)
                    .put("customServerEncryptionKey", profile.customServerEncryptionKey)
                    .put("customServerEncryptionMethod", profile.customServerEncryptionMethod)
                    .put("resolverProfileId", profile.resolverProfileId)
                    .put("connectionMode", profile.connectionMode),
            )
        }
        return array.toString()
    }

    private fun decodeResolverProfiles(raw: String?): List<ResolverProfile> {
        if (raw.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                ResolverProfile(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    resolverText = item.optString("resolverText"),
                )
            }
                .filter { it.id.isNotBlank() && it.resolverText.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun encodeResolverProfiles(profiles: List<ResolverProfile>): String {
        val array = JSONArray()
        profiles.forEach { profile ->
            array.put(
                JSONObject()
                    .put("id", profile.id)
                    .put("name", profile.name)
                    .put("resolverText", profile.resolverText),
            )
        }
        return array.toString()
    }

    private fun decodeAdvancedProfiles(raw: String?): List<AdvancedSettingsProfile> {
        if (raw.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            val defaultProfile = AdvancedSettingsProfile.defaultProfile()
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                AdvancedSettingsProfile(
                    id = item.optString("id"),
                    name = item.optString("name", "Advanced Settings"),
                    listenIp = item.optString("listenIp", defaultProfile.listenIp),
                    listenPort = item.optString("listenPort", defaultProfile.listenPort),
                    httpProxyEnabled = item.optBoolean("httpProxyEnabled", defaultProfile.httpProxyEnabled),
                    httpProxyPort = item.optString("httpProxyPort", defaultProfile.httpProxyPort),
                    socks5Authentication = item.optBoolean(
                        "socks5Authentication",
                        defaultProfile.socks5Authentication,
                    ),
                    socksUsername = item.optString("socksUsername", defaultProfile.socksUsername),
                    socksPassword = item.optString("socksPassword", defaultProfile.socksPassword),
                    balancingStrategy = item.optInt("balancingStrategy", defaultProfile.balancingStrategy),
                    uploadDuplication = item.optString("uploadDuplication", defaultProfile.uploadDuplication),
                    downloadDuplication = item.optString("downloadDuplication", defaultProfile.downloadDuplication),
                    uploadCompression = item.optInt("uploadCompression", defaultProfile.uploadCompression),
                    downloadCompression = item.optInt("downloadCompression", defaultProfile.downloadCompression),
                    baseEncodeData = item.optBoolean("baseEncodeData", defaultProfile.baseEncodeData),
                    minUploadMtu = item.optString("minUploadMtu", defaultProfile.minUploadMtu),
                    minDownloadMtu = item.optString("minDownloadMtu", defaultProfile.minDownloadMtu),
                    maxUploadMtu = item.optString("maxUploadMtu", defaultProfile.maxUploadMtu),
                    maxDownloadMtu = item.optString("maxDownloadMtu", defaultProfile.maxDownloadMtu),
                    mtuTestRetriesResolvers = item.optString(
                        "mtuTestRetriesResolvers",
                        defaultProfile.mtuTestRetriesResolvers,
                    ),
                    mtuTestTimeoutResolvers = item.optString(
                        "mtuTestTimeoutResolvers",
                        defaultProfile.mtuTestTimeoutResolvers,
                    ),
                    mtuTestParallelismResolvers = item.optString(
                        "mtuTestParallelismResolvers",
                        defaultProfile.mtuTestParallelismResolvers,
                    ),
                    mtuTestRetriesLogs = item.optString(
                        "mtuTestRetriesLogs",
                        defaultProfile.mtuTestRetriesLogs,
                    ),
                    mtuTestTimeoutLogs = item.optString(
                        "mtuTestTimeoutLogs",
                        defaultProfile.mtuTestTimeoutLogs,
                    ),
                    mtuTestParallelismLogs = item.optString(
                        "mtuTestParallelismLogs",
                        defaultProfile.mtuTestParallelismLogs,
                    ),
                    rxTxWorkers = item.optString("rxTxWorkers", defaultProfile.rxTxWorkers),
                    tunnelProcessWorkers = item.optString(
                        "tunnelProcessWorkers",
                        defaultProfile.tunnelProcessWorkers,
                    ),
                    tunnelPacketTimeoutSeconds = item.optString(
                        "tunnelPacketTimeoutSeconds",
                        defaultProfile.tunnelPacketTimeoutSeconds,
                    ),
                    dispatcherIdlePollIntervalSeconds = item.optString(
                        "dispatcherIdlePollIntervalSeconds",
                        defaultProfile.dispatcherIdlePollIntervalSeconds,
                    ),
                    txChannelSize = item.optString("txChannelSize", defaultProfile.txChannelSize),
                    rxChannelSize = item.optString("rxChannelSize", defaultProfile.rxChannelSize),
                    resolverUdpConnectionPoolSize = item.optString(
                        "resolverUdpConnectionPoolSize",
                        defaultProfile.resolverUdpConnectionPoolSize,
                    ),
                    streamQueueInitialCapacity = item.optString(
                        "streamQueueInitialCapacity",
                        defaultProfile.streamQueueInitialCapacity,
                    ),
                    orphanQueueInitialCapacity = item.optString(
                        "orphanQueueInitialCapacity",
                        defaultProfile.orphanQueueInitialCapacity,
                    ),
                    dnsResponseFragmentStoreCapacity = item.optString(
                        "dnsResponseFragmentStoreCapacity",
                        defaultProfile.dnsResponseFragmentStoreCapacity,
                    ),
                    maxActiveStreams = item.optString("maxActiveStreams", defaultProfile.maxActiveStreams),
                    localHandshakeTimeoutSeconds = item.optString(
                        "localHandshakeTimeoutSeconds",
                        defaultProfile.localHandshakeTimeoutSeconds,
                    ),
                    socksUdpAssociateReadTimeoutSeconds = item.optString(
                        "socksUdpAssociateReadTimeoutSeconds",
                        defaultProfile.socksUdpAssociateReadTimeoutSeconds,
                    ),
                    clientTerminalStreamRetentionSeconds = item.optString(
                        "clientTerminalStreamRetentionSeconds",
                        defaultProfile.clientTerminalStreamRetentionSeconds,
                    ),
                    clientCancelledSetupRetentionSeconds = item.optString(
                        "clientCancelledSetupRetentionSeconds",
                        defaultProfile.clientCancelledSetupRetentionSeconds,
                    ),
                    sessionInitRetryBaseSeconds = item.optString(
                        "sessionInitRetryBaseSeconds",
                        defaultProfile.sessionInitRetryBaseSeconds,
                    ),
                    sessionInitRetryStepSeconds = item.optString(
                        "sessionInitRetryStepSeconds",
                        defaultProfile.sessionInitRetryStepSeconds,
                    ),
                    sessionInitRetryLinearAfter = item.optString(
                        "sessionInitRetryLinearAfter",
                        defaultProfile.sessionInitRetryLinearAfter,
                    ),
                    sessionInitRetryMaxSeconds = item.optString(
                        "sessionInitRetryMaxSeconds",
                        defaultProfile.sessionInitRetryMaxSeconds,
                    ),
                    sessionInitBusyRetryIntervalSeconds = item.optString(
                        "sessionInitBusyRetryIntervalSeconds",
                        defaultProfile.sessionInitBusyRetryIntervalSeconds,
                    ),
                    localDnsEnabled = item.optBoolean("localDnsEnabled", defaultProfile.localDnsEnabled),
                    localDnsPort = item.optString("localDnsPort", defaultProfile.localDnsPort),
                    startupMode = item.optString("startupMode", defaultProfile.startupMode),
                    pingWatchdogSeconds = item.optString(
                        "pingWatchdogSeconds",
                        defaultProfile.pingWatchdogSeconds,
                    ),
                    trafficWarmupMode = item.optString(
                        "trafficWarmupMode",
                        defaultProfile.trafficWarmupMode,
                    ),
                    trafficWarmupEnabled = item.optBoolean(
                        "trafficWarmupEnabled",
                        defaultProfile.trafficWarmupEnabled,
                    ),
                    trafficWarmupProbeCount = item.optString(
                        "trafficWarmupProbeCount",
                        defaultProfile.trafficWarmupProbeCount,
                    ),
                    trafficKeepaliveIntervalSeconds = item.optString(
                        "trafficKeepaliveIntervalSeconds",
                        defaultProfile.trafficKeepaliveIntervalSeconds,
                    ),
                    vpnIpv6Strategy = item.optString("vpnIpv6Strategy", defaultProfile.vpnIpv6Strategy),
                    vpnMtuPreset = item.optString("vpnMtuPreset", defaultProfile.vpnMtuPreset),
                    vpnCustomMtu = item.optString("vpnCustomMtu", defaultProfile.vpnCustomMtu),
                    logLevel = item.optString("logLevel", defaultProfile.logLevel),
                )
            }
                .filter { it.id.isNotBlank() && it.id != AdvancedSettingsProfile.DefaultId }
        }.getOrDefault(emptyList())
    }

    private fun encodeAdvancedProfiles(profiles: List<AdvancedSettingsProfile>): String {
        val array = JSONArray()
        profiles
            .filter { it.id.isNotBlank() && it.id != AdvancedSettingsProfile.DefaultId }
            .forEach { profile ->
                array.put(
                    JSONObject()
                        .put("id", profile.id)
                        .put("name", profile.name)
                        .put("listenIp", profile.listenIp)
                        .put("listenPort", profile.listenPort)
                        .put("httpProxyEnabled", profile.httpProxyEnabled)
                        .put("httpProxyPort", profile.httpProxyPort)
                        .put("socks5Authentication", profile.socks5Authentication)
                        .put("socksUsername", profile.socksUsername)
                        .put("socksPassword", profile.socksPassword)
                        .put("balancingStrategy", profile.balancingStrategy)
                        .put("uploadDuplication", profile.uploadDuplication)
                        .put("downloadDuplication", profile.downloadDuplication)
                        .put("uploadCompression", profile.uploadCompression)
                        .put("downloadCompression", profile.downloadCompression)
                        .put("baseEncodeData", profile.baseEncodeData)
                        .put("minUploadMtu", profile.minUploadMtu)
                        .put("minDownloadMtu", profile.minDownloadMtu)
                        .put("maxUploadMtu", profile.maxUploadMtu)
                        .put("maxDownloadMtu", profile.maxDownloadMtu)
                        .put("mtuTestRetriesResolvers", profile.mtuTestRetriesResolvers)
                        .put("mtuTestTimeoutResolvers", profile.mtuTestTimeoutResolvers)
                        .put("mtuTestParallelismResolvers", profile.mtuTestParallelismResolvers)
                        .put("mtuTestRetriesLogs", profile.mtuTestRetriesLogs)
                        .put("mtuTestTimeoutLogs", profile.mtuTestTimeoutLogs)
                        .put("mtuTestParallelismLogs", profile.mtuTestParallelismLogs)
                        .put("rxTxWorkers", profile.rxTxWorkers)
                        .put("tunnelProcessWorkers", profile.tunnelProcessWorkers)
                        .put("tunnelPacketTimeoutSeconds", profile.tunnelPacketTimeoutSeconds)
                        .put("dispatcherIdlePollIntervalSeconds", profile.dispatcherIdlePollIntervalSeconds)
                        .put("txChannelSize", profile.txChannelSize)
                        .put("rxChannelSize", profile.rxChannelSize)
                        .put("resolverUdpConnectionPoolSize", profile.resolverUdpConnectionPoolSize)
                        .put("streamQueueInitialCapacity", profile.streamQueueInitialCapacity)
                        .put("orphanQueueInitialCapacity", profile.orphanQueueInitialCapacity)
                        .put("dnsResponseFragmentStoreCapacity", profile.dnsResponseFragmentStoreCapacity)
                        .put("maxActiveStreams", profile.maxActiveStreams)
                        .put("localHandshakeTimeoutSeconds", profile.localHandshakeTimeoutSeconds)
                        .put("socksUdpAssociateReadTimeoutSeconds", profile.socksUdpAssociateReadTimeoutSeconds)
                        .put("clientTerminalStreamRetentionSeconds", profile.clientTerminalStreamRetentionSeconds)
                        .put("clientCancelledSetupRetentionSeconds", profile.clientCancelledSetupRetentionSeconds)
                        .put("sessionInitRetryBaseSeconds", profile.sessionInitRetryBaseSeconds)
                        .put("sessionInitRetryStepSeconds", profile.sessionInitRetryStepSeconds)
                        .put("sessionInitRetryLinearAfter", profile.sessionInitRetryLinearAfter)
                        .put("sessionInitRetryMaxSeconds", profile.sessionInitRetryMaxSeconds)
                        .put("sessionInitBusyRetryIntervalSeconds", profile.sessionInitBusyRetryIntervalSeconds)
                        .put("localDnsEnabled", profile.localDnsEnabled)
                        .put("localDnsPort", profile.localDnsPort)
                        .put("startupMode", profile.startupMode)
                        .put("pingWatchdogSeconds", profile.pingWatchdogSeconds)
                        .put("trafficWarmupMode", profile.trafficWarmupMode)
                        .put("trafficWarmupEnabled", profile.trafficWarmupEnabled)
                        .put("trafficWarmupProbeCount", profile.trafficWarmupProbeCount)
                        .put("trafficKeepaliveIntervalSeconds", profile.trafficKeepaliveIntervalSeconds)
                        .put("vpnIpv6Strategy", profile.vpnIpv6Strategy)
                        .put("vpnMtuPreset", profile.vpnMtuPreset)
                        .put("vpnCustomMtu", profile.vpnCustomMtu)
                        .put("logLevel", profile.logLevel),
                )
            }
        return array.toString()
    }

    private fun decodePackageNames(raw: String?): List<String> {
        if (raw.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                array.optString(index)
            }
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
        }.getOrDefault(emptyList())
    }

    private fun encodePackageNames(packageNames: List<String>): String {
        val array = JSONArray()
        packageNames.forEach { packageName ->
            array.put(packageName)
        }
        return array.toString()
    }

    private fun migrateAdvancedDefaultsIfNeeded() {
        if (preferences.getInt(KeyAdvancedDefaultsRevision, 0) >= AdvancedDefaultsRevision) {
            return
        }

        val editor = preferences.edit()
        fun replaceOldDefault(key: String, oldValue: String, newValue: String) {
            if (preferences.getString(key, null) == oldValue) {
                editor.putString(key, newValue)
            }
        }

        replaceOldDefault(KeyTunnelPacketTimeoutSeconds, oldValue = "12.0", newValue = "8.0")
        replaceOldDefault(KeyTxChannelSize, oldValue = "4096", newValue = "2048")
        replaceOldDefault(KeyRxChannelSize, oldValue = "4096", newValue = "2048")
        replaceOldDefault(KeyStreamQueueInitialCapacity, oldValue = "256", newValue = "128")
        replaceOldDefault(KeyOrphanQueueInitialCapacity, oldValue = "64", newValue = "32")
        replaceOldDefault(KeyDnsResponseFragmentStoreCapacity, oldValue = "1024", newValue = "256")
        replaceOldDefault(KeyClientCancelledSetupRetentionSeconds, oldValue = "90.0", newValue = "120.0")
        replaceOldDefault(KeySessionInitRetryMaxSeconds, oldValue = "30.0", newValue = "60.0")
        replaceOldDefault(KeyMinUploadMtu, oldValue = "100", newValue = "40")
        replaceOldDefault(KeyMinDownloadMtu, oldValue = "100", newValue = "300")
        replaceOldDefault(KeyMinDownloadMtu, oldValue = "1000", newValue = "300")
        replaceOldDefault(KeyMaxUploadMtu, oldValue = "64", newValue = "140")
        replaceOldDefault(KeyMaxUploadMtu, oldValue = "200", newValue = "140")
        replaceOldDefault(KeyMaxDownloadMtu, oldValue = "140", newValue = "3000")
        replaceOldDefault(KeyMaxDownloadMtu, oldValue = "4000", newValue = "3000")
        replaceOldDefault(KeyStartupMode, oldValue = "logs", newValue = "resolvers")
        editor.putInt(KeyAdvancedDefaultsRevision, AdvancedDefaultsRevision).apply()
    }

    private companion object {
        const val PreferencesName = "white_dns_settings"
        const val AdvancedDefaultsRevision = 4
        const val LegacyDefaultResolverText = "1.1.1.1\n8.8.8.8\n9.9.9.9"
        const val KeyAdvancedDefaultsRevision = "advanced_defaults_revision"
        const val KeySelectedConnectionProfileId = "selected_connection_profile_id"
        const val KeyConnectionProfiles = "connection_profiles"
        const val KeySelectedResolverProfileId = "selected_resolver_profile_id"
        const val KeyResolverProfiles = "resolver_profiles"
        const val KeySelectedAdvancedProfileId = "selected_advanced_profile_id"
        const val KeyAdvancedProfiles = "advanced_profiles"
        const val KeyServerMode = "server_mode"
        const val KeyCustomServerDomain = "custom_server_domain"
        const val KeyCustomServerEncryptionKey = "custom_server_encryption_key"
        const val KeyCustomServerEncryptionMethod = "custom_server_encryption_method"
        const val KeyConnectionMode = "connection_mode"
        const val KeyProtocolType = "protocol_type"
        const val KeyResolverText = "resolver_text"
        const val KeyListenIp = "listen_ip"
        const val KeyListenPort = "listen_port"
        const val KeyHttpProxyEnabled = "http_proxy_enabled"
        const val KeyHttpProxyPort = "http_proxy_port"
        const val KeySocks5Authentication = "socks5_authentication"
        const val KeySocksUsername = "socks_username"
        const val KeySocksPassword = "socks_password"
        const val KeyBalancingStrategy = "balancing_strategy"
        const val KeyUploadDuplication = "upload_duplication"
        const val KeyDownloadDuplication = "download_duplication"
        const val KeyUploadCompression = "upload_compression"
        const val KeyDownloadCompression = "download_compression"
        const val KeyBaseEncodeData = "base_encode_data"
        const val KeyMinUploadMtu = "min_upload_mtu"
        const val KeyMinDownloadMtu = "min_download_mtu"
        const val KeyMaxUploadMtu = "max_upload_mtu"
        const val KeyMaxDownloadMtu = "max_download_mtu"
        const val KeyMtuTestRetriesResolvers = "mtu_test_retries_resolvers"
        const val KeyMtuTestTimeoutResolvers = "mtu_test_timeout_resolvers"
        const val KeyMtuTestParallelismResolvers = "mtu_test_parallelism_resolvers"
        const val KeyMtuTestRetriesLogs = "mtu_test_retries_logs"
        const val KeyMtuTestTimeoutLogs = "mtu_test_timeout_logs"
        const val KeyMtuTestParallelismLogs = "mtu_test_parallelism_logs"
        const val KeyRxTxWorkers = "rx_tx_workers"
        const val KeyTunnelProcessWorkers = "tunnel_process_workers"
        const val KeyTunnelPacketTimeoutSeconds = "tunnel_packet_timeout_seconds"
        const val KeyDispatcherIdlePollIntervalSeconds = "dispatcher_idle_poll_interval_seconds"
        const val KeyTxChannelSize = "tx_channel_size"
        const val KeyRxChannelSize = "rx_channel_size"
        const val KeyResolverUdpConnectionPoolSize = "resolver_udp_connection_pool_size"
        const val KeyStreamQueueInitialCapacity = "stream_queue_initial_capacity"
        const val KeyOrphanQueueInitialCapacity = "orphan_queue_initial_capacity"
        const val KeyDnsResponseFragmentStoreCapacity = "dns_response_fragment_store_capacity"
        const val KeyMaxActiveStreams = "max_active_streams"
        const val KeyLocalHandshakeTimeoutSeconds = "local_handshake_timeout_seconds"
        const val KeySocksUdpAssociateReadTimeoutSeconds = "socks_udp_associate_read_timeout_seconds"
        const val KeyClientTerminalStreamRetentionSeconds = "client_terminal_stream_retention_seconds"
        const val KeyClientCancelledSetupRetentionSeconds = "client_cancelled_setup_retention_seconds"
        const val KeySessionInitRetryBaseSeconds = "session_init_retry_base_seconds"
        const val KeySessionInitRetryStepSeconds = "session_init_retry_step_seconds"
        const val KeySessionInitRetryLinearAfter = "session_init_retry_linear_after"
        const val KeySessionInitRetryMaxSeconds = "session_init_retry_max_seconds"
        const val KeySessionInitBusyRetryIntervalSeconds = "session_init_busy_retry_interval_seconds"
        const val KeyLocalDnsEnabled = "local_dns_enabled"
        const val KeyLocalDnsPort = "local_dns_port"
        const val KeyStartupMode = "startup_mode"
        const val KeyPingWatchdogSeconds = "ping_watchdog_seconds"
        const val KeyTrafficWarmupMode = "traffic_warmup_mode"
        const val KeyTrafficWarmupEnabled = "traffic_warmup_enabled"
        const val KeyTrafficWarmupProbeCount = "traffic_warmup_probe_count"
        const val KeyTrafficKeepaliveIntervalSeconds = "traffic_keepalive_interval_seconds"
        const val KeyVpnIpv6Strategy = "vpn_ipv6_strategy"
        const val KeyVpnMtuPreset = "vpn_mtu_preset"
        const val KeyVpnCustomMtu = "vpn_custom_mtu"
        const val KeyFullVpnPerformanceWarningDismissed = "full_vpn_performance_warning_dismissed"
        const val KeySplitTunnelMode = "split_tunnel_mode"
        const val KeySplitTunnelPackages = "split_tunnel_packages"
        const val KeyLogLevel = "log_level"
    }
}
