package shop.whitedns.client.runtime

import android.content.Context
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import shop.whitedns.client.model.ConnectionProfile
import shop.whitedns.client.model.StormDnsServerProfile
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.runtimeConnectionSettings
import shop.whitedns.client.model.syncSelectedConnectionProfileFields

data class RuntimeLaunchRequest(
    val id: String,
    val serverProfile: StormDnsServerProfile,
    val settings: WhiteDnsSettings,
)

object RuntimeLaunchRequestStore {
    private const val DirectoryName = "runtime-launch"
    private const val Extension = ".json"
    private val SafeIdRegex = Regex("[A-Za-z0-9._-]+")

    fun save(
        context: Context,
        requestId: String,
        serverProfile: StormDnsServerProfile,
        settings: WhiteDnsSettings,
    ): RuntimeLaunchRequest {
        require(requestId.isSafeRequestId()) { "Invalid runtime launch request ID" }
        val request = RuntimeLaunchRequest(
            id = requestId,
            serverProfile = serverProfile,
            settings = settings.runtimeConnectionSettings().syncSelectedConnectionProfileFields(),
        )
        launchDirectory(context).mkdirs()
        requestFile(context, requestId).writeText(encode(request).toString(), Charsets.UTF_8)
        return request
    }

    fun load(context: Context, requestId: String): RuntimeLaunchRequest? {
        if (!requestId.isSafeRequestId()) {
            return null
        }
        val file = requestFile(context, requestId)
        if (!file.isFile) {
            return null
        }
        return runCatching {
            decode(JSONObject(file.readText(Charsets.UTF_8)))
        }.getOrNull()
    }

    fun delete(context: Context, requestId: String) {
        if (requestId.isSafeRequestId()) {
            requestFile(context, requestId).delete()
        }
    }

    private fun launchDirectory(context: Context): File {
        return File(context.noBackupFilesDir, DirectoryName)
    }

    private fun requestFile(context: Context, requestId: String): File {
        return File(launchDirectory(context), "$requestId$Extension")
    }

    private fun encode(request: RuntimeLaunchRequest): JSONObject {
        return JSONObject()
            .put("id", request.id)
            .put("serverProfile", encodeServerProfile(request.serverProfile))
            .put("settings", encodeSettings(request.settings))
    }

    private fun decode(json: JSONObject): RuntimeLaunchRequest {
        return RuntimeLaunchRequest(
            id = json.optString("id"),
            serverProfile = decodeServerProfile(json.getJSONObject("serverProfile")),
            settings = decodeSettings(json.getJSONObject("settings")),
        )
    }

    private fun encodeServerProfile(profile: StormDnsServerProfile): JSONObject {
        return JSONObject()
            .put("id", profile.id)
            .put("label", profile.label)
            .put("domain", profile.domain)
            .put("encryptionKey", profile.encryptionKey)
            .put("encryptionMethod", profile.encryptionMethod)
    }

    private fun decodeServerProfile(json: JSONObject): StormDnsServerProfile {
        return StormDnsServerProfile(
            id = json.optString("id"),
            label = json.optString("label"),
            domain = json.optString("domain"),
            encryptionKey = json.optString("encryptionKey"),
            encryptionMethod = json.optInt("encryptionMethod", 1),
        )
    }

    private fun encodeSettings(settings: WhiteDnsSettings): JSONObject {
        val splitTunnelPackages = JSONArray()
        settings.splitTunnelPackages.forEach { packageName ->
            splitTunnelPackages.put(packageName)
        }
        return JSONObject()
            .put("selectedConnectionProfileId", settings.selectedConnectionProfileId)
            .put("serverMode", settings.serverMode)
            .put("customServerDomain", settings.customServerDomain)
            .put("customServerEncryptionKey", settings.customServerEncryptionKey)
            .put("customServerEncryptionMethod", settings.customServerEncryptionMethod)
            .put("connectionMode", settings.connectionMode)
            .put("protocolType", settings.protocolType)
            .put("resolverText", settings.resolverText)
            .put("listenIp", settings.listenIp)
            .put("listenPort", settings.listenPort)
            .put("httpProxyEnabled", settings.httpProxyEnabled)
            .put("httpProxyPort", settings.httpProxyPort)
            .put("socks5Authentication", settings.socks5Authentication)
            .put("socksUsername", settings.socksUsername)
            .put("socksPassword", settings.socksPassword)
            .put("balancingStrategy", settings.balancingStrategy)
            .put("uploadDuplication", settings.uploadDuplication)
            .put("downloadDuplication", settings.downloadDuplication)
            .put("uploadCompression", settings.uploadCompression)
            .put("downloadCompression", settings.downloadCompression)
            .put("baseEncodeData", settings.baseEncodeData)
            .put("minUploadMtu", settings.minUploadMtu)
            .put("minDownloadMtu", settings.minDownloadMtu)
            .put("maxUploadMtu", settings.maxUploadMtu)
            .put("maxDownloadMtu", settings.maxDownloadMtu)
            .put("mtuTestRetriesResolvers", settings.mtuTestRetriesResolvers)
            .put("mtuTestTimeoutResolvers", settings.mtuTestTimeoutResolvers)
            .put("mtuTestParallelismResolvers", settings.mtuTestParallelismResolvers)
            .put("mtuTestRetriesLogs", settings.mtuTestRetriesLogs)
            .put("mtuTestTimeoutLogs", settings.mtuTestTimeoutLogs)
            .put("mtuTestParallelismLogs", settings.mtuTestParallelismLogs)
            .put("rxTxWorkers", settings.rxTxWorkers)
            .put("tunnelProcessWorkers", settings.tunnelProcessWorkers)
            .put("tunnelPacketTimeoutSeconds", settings.tunnelPacketTimeoutSeconds)
            .put("dispatcherIdlePollIntervalSeconds", settings.dispatcherIdlePollIntervalSeconds)
            .put("txChannelSize", settings.txChannelSize)
            .put("rxChannelSize", settings.rxChannelSize)
            .put("resolverUdpConnectionPoolSize", settings.resolverUdpConnectionPoolSize)
            .put("streamQueueInitialCapacity", settings.streamQueueInitialCapacity)
            .put("orphanQueueInitialCapacity", settings.orphanQueueInitialCapacity)
            .put("dnsResponseFragmentStoreCapacity", settings.dnsResponseFragmentStoreCapacity)
            .put("maxActiveStreams", settings.maxActiveStreams)
            .put("localHandshakeTimeoutSeconds", settings.localHandshakeTimeoutSeconds)
            .put("socksUdpAssociateReadTimeoutSeconds", settings.socksUdpAssociateReadTimeoutSeconds)
            .put("clientTerminalStreamRetentionSeconds", settings.clientTerminalStreamRetentionSeconds)
            .put("clientCancelledSetupRetentionSeconds", settings.clientCancelledSetupRetentionSeconds)
            .put("sessionInitRetryBaseSeconds", settings.sessionInitRetryBaseSeconds)
            .put("sessionInitRetryStepSeconds", settings.sessionInitRetryStepSeconds)
            .put("sessionInitRetryLinearAfter", settings.sessionInitRetryLinearAfter)
            .put("sessionInitRetryMaxSeconds", settings.sessionInitRetryMaxSeconds)
            .put("sessionInitBusyRetryIntervalSeconds", settings.sessionInitBusyRetryIntervalSeconds)
            .put("localDnsEnabled", settings.localDnsEnabled)
            .put("localDnsPort", settings.localDnsPort)
            .put("startupMode", settings.startupMode)
            .put("pingWatchdogSeconds", settings.pingWatchdogSeconds)
            .put("trafficWarmupEnabled", settings.trafficWarmupEnabled)
            .put("trafficWarmupProbeCount", settings.trafficWarmupProbeCount)
            .put("trafficKeepaliveIntervalSeconds", settings.trafficKeepaliveIntervalSeconds)
            .put("fullVpnPerformanceWarningDismissed", settings.fullVpnPerformanceWarningDismissed)
            .put("splitTunnelMode", settings.splitTunnelMode)
            .put("splitTunnelPackages", splitTunnelPackages)
            .put("logLevel", settings.logLevel)
    }

    private fun decodeSettings(json: JSONObject): WhiteDnsSettings {
        val selectedConnectionProfileId = json.optString("selectedConnectionProfileId", ConnectionProfile.DefaultId)
        val settings = WhiteDnsSettings(
            selectedConnectionProfileId = selectedConnectionProfileId,
            connectionProfiles = listOf(
                ConnectionProfile(
                    id = selectedConnectionProfileId.ifBlank { ConnectionProfile.DefaultId },
                    name = "Connection",
                    serverMode = json.optString("serverMode", "custom"),
                    customServerDomain = json.optString("customServerDomain"),
                    customServerEncryptionKey = json.optString("customServerEncryptionKey"),
                    customServerEncryptionMethod = json.optInt("customServerEncryptionMethod", 1),
                    connectionMode = json.optString("connectionMode", "proxy"),
                ),
            ),
            serverMode = json.optString("serverMode", "custom"),
            customServerDomain = json.optString("customServerDomain"),
            customServerEncryptionKey = json.optString("customServerEncryptionKey"),
            customServerEncryptionMethod = json.optInt("customServerEncryptionMethod", 1),
            connectionMode = json.optString("connectionMode", "proxy"),
            protocolType = json.optString("protocolType", "SOCKS5"),
            resolverText = json.optString("resolverText"),
            listenIp = json.optString("listenIp", "127.0.0.1"),
            listenPort = json.optString("listenPort", "10886"),
            httpProxyEnabled = json.optBoolean("httpProxyEnabled", true),
            httpProxyPort = json.optString("httpProxyPort", "10887"),
            socks5Authentication = json.optBoolean("socks5Authentication", false),
            socksUsername = json.optString("socksUsername"),
            socksPassword = json.optString("socksPassword"),
            balancingStrategy = json.optInt("balancingStrategy", 3),
            uploadDuplication = json.optString("uploadDuplication", "3"),
            downloadDuplication = json.optString("downloadDuplication", "7"),
            uploadCompression = json.optInt("uploadCompression", 2),
            downloadCompression = json.optInt("downloadCompression", 2),
            baseEncodeData = json.optBoolean("baseEncodeData", false),
            minUploadMtu = json.optString("minUploadMtu", "40"),
            minDownloadMtu = json.optString("minDownloadMtu", "300"),
            maxUploadMtu = json.optString("maxUploadMtu", "140"),
            maxDownloadMtu = json.optString("maxDownloadMtu", "3000"),
            mtuTestRetriesResolvers = json.optString("mtuTestRetriesResolvers", "3"),
            mtuTestTimeoutResolvers = json.optString("mtuTestTimeoutResolvers", "2.0"),
            mtuTestParallelismResolvers = json.optString("mtuTestParallelismResolvers", "100"),
            mtuTestRetriesLogs = json.optString("mtuTestRetriesLogs", "5"),
            mtuTestTimeoutLogs = json.optString("mtuTestTimeoutLogs", "2.0"),
            mtuTestParallelismLogs = json.optString("mtuTestParallelismLogs", "32"),
            rxTxWorkers = json.optString("rxTxWorkers", "4"),
            tunnelProcessWorkers = json.optString("tunnelProcessWorkers", "4"),
            tunnelPacketTimeoutSeconds = json.optString("tunnelPacketTimeoutSeconds", "8.0"),
            dispatcherIdlePollIntervalSeconds = json.optString("dispatcherIdlePollIntervalSeconds", "0.020"),
            txChannelSize = json.optString("txChannelSize", "2048"),
            rxChannelSize = json.optString("rxChannelSize", "2048"),
            resolverUdpConnectionPoolSize = json.optString("resolverUdpConnectionPoolSize", "64"),
            streamQueueInitialCapacity = json.optString("streamQueueInitialCapacity", "128"),
            orphanQueueInitialCapacity = json.optString("orphanQueueInitialCapacity", "32"),
            dnsResponseFragmentStoreCapacity = json.optString("dnsResponseFragmentStoreCapacity", "256"),
            maxActiveStreams = json.optString("maxActiveStreams", "2048"),
            localHandshakeTimeoutSeconds = json.optString("localHandshakeTimeoutSeconds", "5.0"),
            socksUdpAssociateReadTimeoutSeconds = json.optString("socksUdpAssociateReadTimeoutSeconds", "30.0"),
            clientTerminalStreamRetentionSeconds = json.optString("clientTerminalStreamRetentionSeconds", "45.0"),
            clientCancelledSetupRetentionSeconds = json.optString("clientCancelledSetupRetentionSeconds", "120.0"),
            sessionInitRetryBaseSeconds = json.optString("sessionInitRetryBaseSeconds", "1.0"),
            sessionInitRetryStepSeconds = json.optString("sessionInitRetryStepSeconds", "1.0"),
            sessionInitRetryLinearAfter = json.optString("sessionInitRetryLinearAfter", "5"),
            sessionInitRetryMaxSeconds = json.optString("sessionInitRetryMaxSeconds", "60.0"),
            sessionInitBusyRetryIntervalSeconds = json.optString("sessionInitBusyRetryIntervalSeconds", "60.0"),
            localDnsEnabled = json.optBoolean("localDnsEnabled", false),
            localDnsPort = json.optString("localDnsPort", "10888"),
            startupMode = json.optString("startupMode", "resolvers"),
            pingWatchdogSeconds = json.optString("pingWatchdogSeconds", "300"),
            trafficWarmupEnabled = json.optBoolean("trafficWarmupEnabled", true),
            trafficWarmupProbeCount = json.optString("trafficWarmupProbeCount", "4"),
            trafficKeepaliveIntervalSeconds = json.optString("trafficKeepaliveIntervalSeconds", "5"),
            fullVpnPerformanceWarningDismissed = json.optBoolean("fullVpnPerformanceWarningDismissed", false),
            splitTunnelMode = json.optString("splitTunnelMode", "off"),
            splitTunnelPackages = decodeStringArray(json.optJSONArray("splitTunnelPackages")),
            logLevel = json.optString("logLevel", "WARN"),
        )
        return settings.syncSelectedConnectionProfileFields()
    }

    private fun decodeStringArray(array: JSONArray?): List<String> {
        if (array == null) {
            return emptyList()
        }
        return List(array.length()) { index ->
            array.optString(index)
        }.filter(String::isNotBlank)
    }

    private fun String.isSafeRequestId(): Boolean {
        return isNotBlank() && length <= 128 && SafeIdRegex.matches(this)
    }
}
