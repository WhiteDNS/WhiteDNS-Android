package shop.whitedns.client.storm

import shop.whitedns.client.model.ConnectionProfile
import shop.whitedns.client.model.StormDnsServerProfile
import shop.whitedns.client.model.WhiteDnsSettings
import shop.whitedns.client.model.normalizeServerDomainText
import shop.whitedns.client.model.normalizeServerDomains
import shop.whitedns.client.model.normalizedResolverProfiles
import shop.whitedns.client.model.resolve
import shop.whitedns.client.model.runtimeConnectionSettings

object StormDnsConfigRenderer {

    fun renderClientToml(
        connectionProfile: ConnectionProfile,
        settings: WhiteDnsSettings,
    ): String {
        val resolverProfile = settings.normalizedResolverProfiles()
            .firstOrNull { it.id == connectionProfile.resolverProfileId }
        val exportSettings = settings.copy(
            selectedConnectionProfileId = connectionProfile.id,
            selectedResolverProfileId = resolverProfile?.id.orEmpty(),
            resolverText = resolverProfile?.resolverText ?: settings.resolverText,
            connectionMode = when (connectionProfile.connectionMode) {
                "proxy", "vpn" -> connectionProfile.connectionMode
                else -> settings.connectionMode
            },
        ).runtimeConnectionSettings()
        return renderClientToml(
            serverProfile = connectionProfile.toStormDnsServerProfile(),
            settings = exportSettings,
        )
    }

    fun renderClientToml(
        serverProfile: StormDnsServerProfile,
        settings: WhiteDnsSettings,
    ): String {
        val resolved = settings.resolve()
        val domains = normalizeServerDomains(serverProfile.domain)
        require(domains.isNotEmpty()) {
            "Custom server domain and encryption key are required to export TOML"
        }

        return buildString {
            appendLine("DOMAINS = [${domains.joinToString { "\"${escape(it)}\"" }}]")
            appendLine("DATA_ENCRYPTION_METHOD = ${serverProfile.encryptionMethod}")
            appendLine("ENCRYPTION_KEY = \"${escape(serverProfile.encryptionKey)}\"")
            appendLine("PROTOCOL_TYPE = \"${escape(resolved.protocolType)}\"")
            appendLine("LISTEN_IP = \"${escape(resolved.listenIp)}\"")
            appendLine("LISTEN_PORT = ${resolved.listenPort}")
            appendLine("SOCKS5_AUTH = ${resolved.socks5Authentication}")
            appendLine("SOCKS5_USER = \"${escape(resolved.socksUsername)}\"")
            appendLine("SOCKS5_PASS = \"${escape(resolved.socksPassword)}\"")
            appendLine("LOCAL_DNS_ENABLED = ${resolved.localDnsEnabled}")
            appendLine("LOCAL_DNS_IP = \"127.0.0.1\"")
            appendLine("LOCAL_DNS_PORT = ${resolved.localDnsPort}")
            appendLine("RESOLVER_BALANCING_STRATEGY = ${resolved.balancingStrategy}")
            appendLine("UPLOAD_PACKET_DUPLICATION_COUNT = ${resolved.uploadDuplication}")
            appendLine("DOWNLOAD_PACKET_DUPLICATION_COUNT = ${resolved.downloadDuplication}")
            appendLine("UPLOAD_COMPRESSION_TYPE = ${resolved.uploadCompression}")
            appendLine("DOWNLOAD_COMPRESSION_TYPE = ${resolved.downloadCompression}")
            appendLine("BASE_ENCODE_DATA = ${resolved.baseEncodeData}")
            appendLine("MIN_UPLOAD_MTU = ${resolved.minUploadMtu}")
            appendLine("MIN_DOWNLOAD_MTU = ${resolved.minDownloadMtu}")
            appendLine("MAX_UPLOAD_MTU = ${resolved.maxUploadMtu}")
            appendLine("MAX_DOWNLOAD_MTU = ${resolved.maxDownloadMtu}")
            appendLine("MTU_TEST_RETRIES_RESOLVERS = ${resolved.mtuTestRetriesResolvers}")
            appendLine("MTU_TEST_TIMEOUT_RESOLVERS = ${resolved.mtuTestTimeoutResolvers}")
            appendLine("MTU_TEST_PARALLELISM_RESOLVERS = ${resolved.mtuTestParallelismResolvers}")
            appendLine("MTU_TEST_RETRIES_LOGS = ${resolved.mtuTestRetriesLogs}")
            appendLine("MTU_TEST_TIMEOUT_LOGS = ${resolved.mtuTestTimeoutLogs}")
            appendLine("MTU_TEST_PARALLELISM_LOGS = ${resolved.mtuTestParallelismLogs}")
            appendLine("RX_TX_WORKERS = ${resolved.rxTxWorkers}")
            appendLine("TUNNEL_PROCESS_WORKERS = ${resolved.tunnelProcessWorkers}")
            appendLine("TUNNEL_PACKET_TIMEOUT_SECONDS = ${resolved.tunnelPacketTimeoutSeconds}")
            appendLine("DISPATCHER_IDLE_POLL_INTERVAL_SECONDS = ${resolved.dispatcherIdlePollIntervalSeconds}")
            appendLine("TX_CHANNEL_SIZE = ${resolved.txChannelSize}")
            appendLine("RX_CHANNEL_SIZE = ${resolved.rxChannelSize}")
            appendLine("RESOLVER_UDP_CONNECTION_POOL_SIZE = ${resolved.resolverUdpConnectionPoolSize}")
            appendLine("STREAM_QUEUE_INITIAL_CAPACITY = ${resolved.streamQueueInitialCapacity}")
            appendLine("ORPHAN_QUEUE_INITIAL_CAPACITY = ${resolved.orphanQueueInitialCapacity}")
            appendLine("DNS_RESPONSE_FRAGMENT_STORE_CAPACITY = ${resolved.dnsResponseFragmentStoreCapacity}")
            appendLine("MAX_ACTIVE_STREAMS = ${resolved.maxActiveStreams}")
            appendLine("LOCAL_HANDSHAKE_TIMEOUT_SECONDS = ${resolved.localHandshakeTimeoutSeconds}")
            appendLine("SOCKS_UDP_ASSOCIATE_READ_TIMEOUT_SECONDS = ${resolved.socksUdpAssociateReadTimeoutSeconds}")
            appendLine("CLIENT_TERMINAL_STREAM_RETENTION_SECONDS = ${resolved.clientTerminalStreamRetentionSeconds}")
            appendLine("CLIENT_CANCELLED_SETUP_RETENTION_SECONDS = ${resolved.clientCancelledSetupRetentionSeconds}")
            appendLine("SESSION_INIT_RETRY_BASE_SECONDS = ${resolved.sessionInitRetryBaseSeconds}")
            appendLine("SESSION_INIT_RETRY_STEP_SECONDS = ${resolved.sessionInitRetryStepSeconds}")
            appendLine("SESSION_INIT_RETRY_LINEAR_AFTER = ${resolved.sessionInitRetryLinearAfter}")
            appendLine("SESSION_INIT_RETRY_MAX_SECONDS = ${resolved.sessionInitRetryMaxSeconds}")
            appendLine("SESSION_INIT_BUSY_RETRY_INTERVAL_SECONDS = ${resolved.sessionInitBusyRetryIntervalSeconds}")
            appendLine("STARTUP_MODE = \"${escape(resolved.startupMode)}\"")
            appendLine("LOG_SCAN_MAX_DAYS = 14")
            appendLine("LOG_SCAN_MAX_RESOLVERS = 128")
            appendLine("LOG_BASED_MTU_VERIFY = true")
            appendLine("PING_WATCHDOG_TIMEOUT_SECONDS = ${resolved.pingWatchdogSeconds}")
            appendLine("LOG_LEVEL = \"${escape(resolved.logLevel)}\"")
            appendLine("LOG_TO_FILE = true")
            appendLine("LOG_DIR = \"logs\"")
        }.trimEnd()
    }

    fun renderResolvers(settings: WhiteDnsSettings): String {
        return settings.resolve().resolverEntries.joinToString(separator = "\n")
    }

    private fun escape(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }

    private fun ConnectionProfile.toStormDnsServerProfile(): StormDnsServerProfile {
        val domain = normalizeServerDomainText(customServerDomain)
        val encryptionKey = customServerEncryptionKey.trim()
        if (domain.isBlank() || encryptionKey.isBlank()) {
            throw IllegalArgumentException("Custom server domain and encryption key are required to export TOML")
        }
        return StormDnsServerProfile(
            id = id.ifBlank { "custom" },
            label = name.ifBlank { "Custom StormDNS Server" },
            domain = domain,
            encryptionKey = encryptionKey,
            encryptionMethod = customServerEncryptionMethod.coerceIn(0, 5),
        )
    }
}
