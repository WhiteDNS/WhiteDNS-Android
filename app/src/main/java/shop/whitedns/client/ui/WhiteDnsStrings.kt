package shop.whitedns.client.ui

data class SettingsGuideEntry(
    val title: String,
    val body: String,
    val effect: String,
)

data class SettingsGuideSection(
    val title: String,
    val entries: List<SettingsGuideEntry>,
)

interface WhiteDnsStrings {
    // Tabs
    val tabProfiles: String
    val tabConnect: String
    val tabScan: String
    val tabLogs: String

    // Connect button
    val btnConnect: String
    val btnConnecting: String
    val btnStop: String

    // Common buttons
    val btnClose: String
    val btnSave: String
    val btnCancel: String
    val btnCreate: String
    val btnImport: String
    val btnDelete: String
    val btnCopy: String
    val btnShare: String

    // App Settings dialog
    val appSettingsTitle: String
    val fieldTheme: String
    val fieldLanguage: String
    val themeModeAuto: String
    val themeModeLight: String
    val themeModeDark: String
    val languageEn: String
    val languageFa: String

    // Connection mode
    val fieldMode: String
    val connectionModeProxy: String
    val connectionModeVpn: String

    // Section labels
    val sectionConnection: String
    val sectionResolver: String

    // Banners
    val bannerBatteryTitle: String
    val bannerBatteryBody: String
    val bannerAllowBackground: String
    val bannerNotificationTitle: String
    val bannerNotificationBody: String
    val bannerEnableNotification: String
    val bannerVpnWarningTitle: String
    val bannerVpnWarningBody: String

    // Connect tab labels
    val parallelTest: String
    val connectProgressConnected: String
    val serverTestTitle: String
    val serverTestButton: String
    val serverTestSingleButton: String
    val serverTestRunning: String
    val serverTestIdle: String
    val serverTestReady: String
    val serverTestFailed: String
    val serverTestMeasuring: String
    val serverTestStarting: String
    val serverTestPending: String
    val serverTestProgressTemplate: String
    val serverTestSummaryTemplate: String
    val serverTestNoSavedServers: String
    val serverTestNoConnectedResolvers: String
    val serverTestFailedTemplate: String
    val serverTestConnectionRequired: String
    val serverTestServiceConnected: String
    val serverTestServiceTesting: String
    val serverTestServiceGood: String
    val serverTestServiceFair: String
    val serverTestServicePoor: String
    val serverTestScoreGood: String
    val serverTestScoreFair: String
    val serverTestScorePoor: String
    val serverTestScoreUnavailable: String

    // Profile tabs
    val profileTabConnection: String
    val profileTabResolver: String
    val profileTabSetting: String

    // Setting profile guide
    val settingGuideTitle: String
    val settingGuideIntro: String
    val settingGuideSource: String
    val settingGuideEffectLabel: String
    val settingGuideSections: List<SettingsGuideSection>
    val cdSettingGuide: String

    // Header menu
    val menuAppSettings: String
    val menuDonate: String
    val menuVersion: String

    // Setup card
    val setupTitle: String
    val setupAddConnection: String
    val setupAddResolver: String

    // Connection selectors
    val selectorConnectionProfiles: String
    val selectorResolverProfiles: String
    val selectorSettingProfiles: String
    val resolverNotSelected: String
    val resolverRequired: String

    // Logs tab
    val logsTitle: String
    val logsClear: String
    val logsCopy: String

    // Scan tab
    val scanBtnStart: String
    val scanBtnStop: String
    val scanBtnSaveAs: String
    val scanBtnResume: String
    val scanWorkerWarning: String
    val scanStatusTitle: String
    val scanLabelTotal: String
    val scanLabelValid: String
    val scanLabelRejected: String
    val scanLabelStatus: String
    val scanLabelSource: String
    val scanLabelWorkers: String
    val scanLabelProgress: String
    val scanAutoSave: String

    // Validation / error
    val serverRouteMissing: String

    // Donate dialog
    val supportTitle: String
    val supportBody: String

    // Scan tab additional
    val scanDefaultList: String
    val scanSelectFile: String
    val scanProfileLabel: String
    val scanAutoSaveTitle: String
    val scanSaveAsTitle: String
    val scanSaveAsName: String
    val scanSaveAsPlaceholder: String
    val scanNoFileSelected: String
    val scanMessageLabel: String

    // Setup card
    val setupResolversLabel: String
    val setupAddConnectionSupportingText: String
    val setupAddResolverSupportingText: String
    val setupManualResolvers: String
    val setupSectionSetup: String

    // Connection info card
    val infoCardConnection: String
    val infoLabelMode: String
    val infoLabelSocks5Proxy: String
    val infoLabelHttpProxy: String
    val infoLabelAuth: String
    val infoLabelUser: String
    val infoLabelPass: String
    val infoLabelSplitTunnel: String
    val infoLabelApps: String
    val infoLabelConnectionProfile: String
    val infoLabelResolverProfile: String
    val infoLabelSettingProfile: String
    val infoLabelProtocol: String
    val infoLabelAuthOn: String
    val infoLabelAuthOff: String

    // Speed indicators
    val speedDown: String
    val speedUp: String
    val speedTotalUsage: String

    // Resolver runtime
    val resolverActiveResolvers: String
    val resolverValidResolvers: String
    val resolverPending: String
    val resolverNoResolvers: String
    val backgroundScanningInProgress: String

    // Profile dialogs
    val profileDialogCreateSetting: String
    val profileDialogEditSetting: String
    val profileDialogCreateResolver: String
    val profileDialogEditResolver: String
    val profileDialogCreateConnection: String
    val profileDialogEditConnection: String
    val profileDialogImportSettings: String
    val profileDialogImportConnection: String
    val profileDialogExportConnection: String
    val profileDialogExportAllConnections: String
    val profileDialogExportAllResolvers: String
    val profileDialogExportSettings: String
    val profileExportResolverTotalTemplate: String
    val profileExportSavingFile: String
    val profileExportSavedToTemplate: String
    val profileFieldName: String
    val profileFieldResolvers: String
    val profileFieldProfileLinks: String
    val profileFieldToml: String
    val profileNamePlaceholderFastTunnel: String
    val profileNamePlaceholderHomeResolvers: String
    val profileNamePlaceholderImportedSettings: String
    val profileNamePlaceholderConnection: String
    val profileResolverPlaceholder: String

    // Profile menu actions
    val profileMenuExport: String
    val profileMenuEdit: String
    val profileMenuDelete: String
    val profileMenuUse: String
    val profileMenuUseSelected: String

    // Profile list empty states
    val profileNoResolverLists: String
    val profileNoSettingProfiles: String
    val profileQrUnavailable: String

    // Profile action buttons
    val profileBtnCreate: String
    val profileBtnImport: String
    val profileBtnDeleteDups: String
    val profileBtnExportAll: String
    val profileBtnSaveCurrent: String
    val profileBtnImportFile: String
    val profileBtnClear: String

    // Delete confirmation dialogs
    val deleteConnectionTitle: String
    val deleteResolverTitle: String
    val deleteSettingTitle: String
    val deleteDupsTitle: String

    // GroupLabels
    val groupMtu: String
    val groupRuntimeWorkers: String
    val groupLocalProxy: String
    val groupNetworkTuning: String
    val groupReliability: String
    val groupDefault: String
    val groupCustomSettings: String
    val groupParallelTestResults: String

    // Advanced settings field labels
    val settingListenIp: String
    val settingListenPort: String
    val settingHttpProxy: String
    val settingHttpPort: String
    val settingSocks5Auth: String
    val settingUsername: String
    val settingPassword: String
    val settingBalancingStrategy: String
    val settingUploadDup: String
    val settingDownloadDup: String
    val settingUploadCompress: String
    val settingDownloadCompress: String
    val settingBaseEncodeData: String
    val settingPingWatchdog: String
    val settingTrafficWarmup: String
    val settingWarmupProbes: String
    val settingKeepalive: String
    val settingLogLevel: String
    val settingSearch: String
    val settingMinUpload: String
    val settingMinDownload: String
    val settingMaxUpload: String
    val settingMaxDownload: String
    val settingResolverRetries: String
    val settingResolverTimeout: String
    val settingResolverParallel: String
    val settingResolverParallelNote: String
    val settingLogsRetries: String
    val settingLogsTimeout: String
    val settingLogsParallel: String
    val settingRxTxWorkers: String
    val settingProcessWorkers: String
    val settingTunnelPacketTimeout: String
    val settingIdlePoll: String
    val settingTxChannel: String
    val settingRxChannel: String
    val settingUdpPool: String
    val settingStreamQueue: String
    val settingOrphanQueue: String
    val settingDnsFragments: String
    val settingSocksUdpTimeout: String
    val settingTerminalRetain: String
    val settingCancelledRetain: String
    val settingRetryBase: String
    val settingRetryStep: String
    val settingRetryLinear: String
    val settingRetryMax: String
    val settingBusyRetry: String
    val settingSettingLabel: String

    // Split Tunnel
    val splitTunnelTitle: String
    val splitTunnelAppRouting: String
    val splitTunnelSelected: String
    val splitTunnelSelectApps: String
    val splitTunnelNoAppsFound: String
    val splitTunnelSearchPlaceholder: String
    val splitTunnelDialogTitle: String
    val splitTunnelSearchLabel: String

    // Connection logs (inline panel)
    val logsInlineTitle: String
    val logsDiagnostics: String

    // Notification permission banner (runtime permission variant)
    val bannerNotificationBlockedTitle: String
    val bannerNotificationBlockedBody: String

    // Full VPN warning banner (inline)
    val bannerFullVpnWarningTitle: String
    val bannerFullVpnWarningBody: String

    // Parallel test UI
    val parallelTestOpenLabel: String
    val parallelTestClosedLabel: String
    val parallelTestDescription: String
    val parallelTestYourConfigs: String

    // Connect tab messages
    val connectNeedResolvers: String
    val connectSelectedCount: String

    // Auto-tune / parallel test results
    val autoTuneSaveSettingAs: String
    val autoTuneMtuFail: String
    val autoTuneMtuPass: String
    val autoTuneMtuTest: String
    val autoTuneSpeedLabel: String
    val autoTunePingLabel: String
    val autoTuneStatusStarting: String
    val autoTuneMeasuringSpeed: String
    val cdParallelTestSpeed: String
    val cdParallelTestPing: String
    val cdConnectButtonDisconnected: String
    val cdConnectButtonConnecting: String
    val cdConnectButtonConnected: String
    val cdAutoTuneMtuFailed: String
    val cdAutoTuneMtuPassed: String
    val cdAutoTuneMtuTesting: String

    // Share intents
    val shareSubjectProfile: String
    val shareChooserProfile: String
    val shareChooserClientConfig: String
    val shareChooserAdvancedSettings: String
    val shareChooserResolvers: String

    // File import / validation errors
    val errorUnableToOpenResolverFile: String
    val errorInvalidResolverIpTemplate: String
    val errorNoResolverEntries: String
    val errorEnterValidResolverIp: String
    val errorEnterProfileNameToSave: String
    val resolverValidSingularTemplate: String
    val resolverValidPluralTemplate: String
    val advancedProfileModifiedSuffix: String
    val cdEditPrefix: String
    val resolverFieldPlaceholder: String
    val dropdownPlaceholderSelect: String
    val setupDefaultResolver: String
    val setupDefaultConnection: String
    val setupDefaultAdvanced: String

    // Localized choice labels (for dropdowns)
    val balancingStrategyRandom: String
    val balancingStrategyRoundRobin: String
    val balancingStrategyLeastLoss: String
    val balancingStrategyLowestLatency: String
    val compressionOff: String
    val compressionZstd: String
    val compressionLz4: String
    val compressionZlib: String
    val splitTunnelAllAppsChoice: String
    val splitTunnelOnlySelectedChoice: String
    val splitTunnelBypassSelectedChoice: String
    val encryptionMethodNone: String
    val encryptionMethodXor: String
    val encryptionMethodChacha20: String
    val encryptionMethodAes128: String
    val encryptionMethodAes192: String
    val encryptionMethodAes256: String

    // Download TOML dialog
    val downloadTomlTitle: String
    val downloadTomlBtn: String

    // Save setting as button
    val saveSettingAs: String

    // Validation messages
    val validationEnterResolverIp: String
    val validationEnterProfileName: String

    // HomeSelectorCard
    val homeSelectorSettingLabel: String
    val homeSelectorUnsavedChanges: String

    // Newly added (in-app translation of remaining English strings)
    val homeSelectorNoSavedLists: String
    val homeSelectorNotSelected: String
    val homeSelectorResolverProfileFallback: String
    val homeSelectorSearchConnections: String
    val homeSelectorSearchResolvers: String
    val homeSelectorSearchSettings: String
    val homeSelectorCustomAdvanced: String
    val profileNameCopySuffix: String
    val settingProfileFastTunnelPlaceholder: String
    val resolverProfileHomeResolversPlaceholder: String
    val settingProfileImportedSettingsPlaceholder: String
    val homeSelectorNoConnectionProfiles: String
    val homeSelectorNoResolverProfiles: String
    val homeSelectorNoSettingProfiles: String

    // Setup card
    val setupNoResolversConfigured: String
    val setupInvalidResolverIp: String
    val setupServerRouteAndKey: String
    val setupEncryptionKeyMissing: String

    // Scan tab
    val scanProfileNeedsServer: String
    val scanProfileFallback: String
    val scanResultsTitle: String
    val scanCurrentScan: String
    val scanFieldWorkers: String
    val scanSaveBodyTemplate: String
    val scanStatusReady: String
    val scanStatusStarting: String
    val scanStatusRunning: String
    val scanStatusCompleted: String
    val scanStatusFailed: String
    val scanStatusStopped: String
    val scanStatusIdle: String

    // Connection profiles list / dialogs
    val groupCustomConnections: String
    val customConnectionsEmpty: String
    val profileFieldDomain: String
    val profileFieldEncryptionKey: String
    val profileFieldEncryptionMethod: String
    val profileDomainPlaceholder: String
    val profileEncryptionKeyPlaceholder: String
    val profileMyStormDnsPlaceholder: String
    val profileDomainFallback: String
    val profileStatusActive: String
    val profileStatusSelected: String
    val profileStatusModified: String
    fun resolverProfileSummary(count: Int): String
    val profileFieldProfileLinkSingle: String
    val dialogDeleteConfirm: String
    val deleteConnectionMessageTemplate: String
    val deleteResolverMessageTemplate: String
    val deleteSettingMessageTemplate: String
    val deleteDupsMessageSingleConnection: String
    val deleteDupsMessageManyConnection: String

    // Footer / branding
    val footerPoweredBy: String

    // Verification
    val verificationVerifying: String
    val verificationVerified: String
    val verificationNeedsAttention: String
    val verificationPending: String
    val verificationNotRunYet: String
    val verificationCheckingRoute: String
    val verificationProxyReachable: String
    val verificationVpnReachable: String
    val verificationProxyWarming: String
    val verificationVpnWarming: String
    val verificationModeChanged: String
    val verificationSocksNotReachable: String
    val verificationVpnInterfaceInactive: String

    // Lists / placeholders
    val noResolversPlaceholder: String
    val whiteDnsResolversLabel: String
    val whiteDnsConfigsLabel: String
    val whiteDnsConfigsDescription: String
    val whiteDnsAggressiveConfigsLabel: String
    val whiteDnsAggressiveConfigsDescription: String
    val whiteDnsLogsLabel: String
    val whiteDnsDiagnosticsLabel: String
    val parallelTestCollapseDescription: String
    val parallelTestExpandDescription: String

    // Auto-tune
    val autoTuneStartingTest: String
    val autoTuneFailedFallback: String
    val autoTuneMeasuredKeyword: String

    // Dropdown / select
    val dropdownSelectFallback: String

    // Split tunnel
    val splitTunnelAllApps: String
    val splitTunnelNoApps: String
    val splitTunnelOnlyPrefix: String
    val splitTunnelBypassPrefix: String

    // Setting profile dropdown labels
    val tapToCollapse: String
    val tapToConfigure: String
    val parallelTestOpen: String
    val parallelTestClosed: String

    // Apps search
    val appsSearchPlaceholder: String

    // Connection profile related
    val profileFieldProfileLinksLabel: String

    // Open/close selector content description
    val cdCloseSelector: String
    val cdSelected: String
    val cdDismissScannerInfo: String
    val cdEditField: String
    val cdDragToReorder: String
    val cdProfileQrCode: String
    val cdAppMenu: String
    val cdAppSettings: String
    val cdDonate: String
    val cdDismissBatteryWarning: String
    val cdDismissVpnWarning: String

    // Resolver count label - function returns String
    val resolverCountTemplate: String
    val resolverCountOneTemplate: String

    // Generic
    val genericConnectionFallback: String
    val genericResolverFallback: String
    val genericSettingFallback: String
    val scanProfileMenuActions: String
    val settingProfileMenuActions: String
    val connectionProfileMenuActions: String
    val resolverProfileMenuActions: String
    val useSettingProfile: String
    val useResolverProfile: String
    val exportConnectionProfileAction: String
    val editConnectionProfileAction: String
    val deleteConnectionProfileAction: String
    val deleteConnectionProfileBlockedAction: String
    val exportSettingProfileAction: String
    val editSettingProfileAction: String
    val deleteSettingProfileAction: String
    val editResolverProfileAction: String
    val deleteResolverProfileAction: String
    val brandWhiteDns: String

    // Import error generic
    val errorImportSettingsFile: String
    val errorImportSettings: String
    val errorImportResolver: String
    val errorImportProfile: String
    val errorExportProfile: String

    // Resolver placeholder for scan results
    val resolverScanResults: String
    val scanResultsSuffix: String
    val noResolverEntriesError: String

    // QR import
    val profileImportSuccess: String
    val profileBtnScanQr: String
    val qrScanNoCode: String
    val qrScanCancelled: String

}

object EnglishStrings : WhiteDnsStrings {
    override val tabProfiles = "Profiles"
    override val tabConnect = "Connect"
    override val tabScan = "Scan"
    override val tabLogs = "Logs"

    override val btnConnect = "CONNECT"
    override val btnConnecting = "CONNECTING"
    override val btnStop = "STOP"

    override val btnClose = "CLOSE"
    override val btnSave = "SAVE"
    override val btnCancel = "CANCEL"
    override val btnCreate = "CREATE"
    override val btnImport = "IMPORT"
    override val btnDelete = "DELETE"
    override val btnCopy = "COPY"
    override val btnShare = "SHARE"

    override val appSettingsTitle = "APP SETTINGS"
    override val fieldTheme = "Theme"
    override val fieldLanguage = "Language"
    override val themeModeAuto = "Auto"
    override val themeModeLight = "Light"
    override val themeModeDark = "Dark"
    override val languageEn = "English"
    override val languageFa = "فارسی"

    override val fieldMode = "Mode"
    override val connectionModeProxy = "Proxy Mode"
    override val connectionModeVpn = "Full VPN"
    override val sectionConnection = "Connection"
    override val sectionResolver = "Resolver"

    override val bannerBatteryTitle = "BACKGROUND VPN MAY STOP"
    override val bannerBatteryBody = "Allow WhiteDNS to ignore battery optimization so the VPN keeps running after you leave the app."
    override val bannerAllowBackground = "ALLOW BACKGROUND VPN"
    override val bannerNotificationTitle = "VPN NOTIFICATION DISABLED"
    override val bannerNotificationBody = "Enable VPN notification to keep the connection stable in the background."
    override val bannerEnableNotification = "ENABLE VPN NOTIFICATION"
    override val bannerVpnWarningTitle = "FULL VPN PERFORMANCE WARNING"
    override val bannerVpnWarningBody = "Full VPN mode may affect performance. Proxy mode is recommended for most users."

    override val parallelTest = "Parallel Test"
    override val connectProgressConnected = "Connected"
    override val serverTestTitle = "SERVER TEST"
    override val serverTestButton = "TEST SERVERS"
    override val serverTestSingleButton = "Test server"
    override val serverTestRunning = "TESTING"
    override val serverTestIdle = "Ready to test saved servers"
    override val serverTestReady = "Ready"
    override val serverTestFailed = "Failed"
    override val serverTestMeasuring = "Measuring"
    override val serverTestStarting = "Starting"
    override val serverTestPending = "Pending"
    override val serverTestProgressTemplate = "Server Test: measured %d/%d servers"
    override val serverTestSummaryTemplate = "Server Test complete: %d ready, %d failed"
    override val serverTestNoSavedServers = "Server Test: no saved server profiles are configured"
    override val serverTestNoConnectedResolvers = "Server Test: no connected resolvers are available"
    override val serverTestFailedTemplate = "Server Test failed: %s"
    override val serverTestConnectionRequired = "Connection Required"
    override val serverTestServiceConnected = "Connected"
    override val serverTestServiceTesting = "Testing"
    override val serverTestServiceGood = "Healthy"
    override val serverTestServiceFair = "Mixed"
    override val serverTestServicePoor = "Needs Attention"
    override val serverTestScoreGood = "Good"
    override val serverTestScoreFair = "Fair"
    override val serverTestScorePoor = "Poor"
    override val serverTestScoreUnavailable = "Not available"

    override val profileTabConnection = "Connection"
    override val profileTabResolver = "Resolver"
    override val profileTabSetting = "Setting"

    override val settingGuideTitle = "SETTING PROFILE GUIDE"
    override val settingGuideIntro = "Setting profiles change how the MasterDNS/StormDNS client behaves after you choose a connection and resolver list. Most values are MasterDNS/StormDNS client_config.toml runtime knobs; Android-only helpers are marked by WhiteDNS behavior."
    override val settingGuideSource = "Based on MasterDNS/StormDNS client_config.toml and WhiteDNS runtime behavior."
    override val settingGuideEffectLabel = "Effect"
    override val cdSettingGuide = "Open setting profile guide"
    override val settingGuideSections = listOf(
        SettingsGuideSection(
            title = "MTU discovery",
            entries = listOf(
                SettingsGuideEntry(
                    title = "Min Upload / Min Download",
                    body = "Smallest upload and download payload sizes accepted during resolver MTU testing. Upload is your phone to the tunnel server; download is server to your phone.",
                    effect = "Higher minimums reject weak resolvers faster, but setting them too high can leave no usable resolver on strict networks.",
                ),
                SettingsGuideEntry(
                    title = "Max Upload / Max Download",
                    body = "Upper limits for the largest payload StormDNS will try in each direction during MTU search.",
                    effect = "Higher values can improve throughput when DNS paths allow large packets, but scans take longer and may fail more often on filtered resolvers.",
                ),
                SettingsGuideEntry(
                    title = "Resolver Retries / Timeout / Parallelism",
                    body = "Used during a full startup scan from the resolver list. Retries repeat failed probes, timeout waits for each probe, and parallelism decides how many resolvers are tested at once.",
                    effect = "More retries or longer timeouts improve accuracy on bad links but delay connection. More parallelism starts faster, while using more CPU, battery, and network burst.",
                ),
                SettingsGuideEntry(
                    title = "Logs Retries / Timeout / Parallel",
                    body = "Used when StormDNS starts from cached resolver log entries instead of scanning the whole resolver list.",
                    effect = "Higher values make cached startup safer. Lower values reconnect quicker when you trust the previous working resolvers.",
                ),
            ),
        ),
        SettingsGuideSection(
            title = "Runtime workers, queues, and timers",
            entries = listOf(
                SettingsGuideEntry(
                    title = "RX/TX Workers / Process Workers",
                    body = "Background workers for UDP tunnel reads, writes, and packet processing.",
                    effect = "More workers can help heavy traffic on fast phones. Too many workers waste battery and CPU; WhiteDNS keeps process workers at least as high as RX/TX workers.",
                ),
                SettingsGuideEntry(
                    title = "Tunnel Packet Timeout / Idle Poll",
                    body = "Packet timeout limits how long async tunnel work may wait. Idle poll controls how often the dispatcher wakes up when there is nothing to send.",
                    effect = "Shorter values react faster but can burn CPU or drop slow packets. Longer values are calmer but may feel less responsive.",
                ),
                SettingsGuideEntry(
                    title = "TX Channel / RX Channel / UDP Pool",
                    body = "Buffer sizes for outgoing and incoming tunnel packets, plus the number of reusable UDP sockets per resolver.",
                    effect = "Larger values handle bursts better and reduce socket churn, but they use more memory and can hide congestion.",
                ),
                SettingsGuideEntry(
                    title = "Stream Queue / Orphan Queue / DNS Fragments",
                    body = "Initial storage for stream data, unmatched control packets, and split DNS response fragments.",
                    effect = "Larger queues tolerate busy or fragmented sessions better. Smaller queues save memory but can drop work under load.",
                ),
                SettingsGuideEntry(
                    title = "SOCKS UDP Timeout / Terminal Retain / Cancelled Retain",
                    body = "Cleanup timers for SOCKS UDP sessions, finished streams, and setup attempts that were cancelled.",
                    effect = "Longer retention helps late packets finish cleanly. Shorter retention frees memory sooner but can break delayed cleanup paths.",
                ),
                SettingsGuideEntry(
                    title = "Retry Base / Step / Linear / Max / Busy Retry",
                    body = "Session initialization retry schedule after setup failure, reset, or a server busy response.",
                    effect = "Aggressive retries reconnect sooner but create more traffic. Slower retries are gentler on unstable or crowded servers.",
                ),
            ),
        ),
        SettingsGuideSection(
            title = "Local proxy",
            entries = listOf(
                SettingsGuideEntry(
                    title = "Listen IP / Listen Port",
                    body = "Address and port where the local SOCKS5 proxy listens. 127.0.0.1 keeps it local to your phone.",
                    effect = "Changing the port requires apps to use the new proxy address. Binding broadly, such as 0.0.0.0, can expose the proxy to your network.",
                ),
                SettingsGuideEntry(
                    title = "HTTP Proxy / HTTP Port",
                    body = "WhiteDNS can expose an HTTP proxy bridge next to the SOCKS5 listener for apps that do not support SOCKS5.",
                    effect = "Useful for compatibility, but it opens another local listener and should use a port that is not already taken.",
                ),
                SettingsGuideEntry(
                    title = "SOCKS5 Authentication / Username / Password",
                    body = "Optional username and password for the local SOCKS5 proxy. This protects the local proxy, not the remote StormDNS server.",
                    effect = "Turn it on if the proxy is reachable beyond the phone. Apps must then be configured with the same username and password.",
                ),
            ),
        ),
        SettingsGuideSection(
            title = "Network tuning",
            entries = listOf(
                SettingsGuideEntry(
                    title = "Balancing Strategy",
                    body = "Resolver selection mode: Random, Round Robin, Least Loss, or Lowest Latency. Loss and latency modes use runtime feedback after traffic starts.",
                    effect = "Least Loss is usually stable on bad networks. Lowest Latency can feel faster, but may switch toward paths that are quick yet less reliable.",
                ),
                SettingsGuideEntry(
                    title = "Upload Dup / Download Dup",
                    body = "How many copies StormDNS sends for upload data and download-support packets. Download duplicates are mostly ACK/NACK support packets.",
                    effect = "Higher upload duplication multiplies real upload usage. Higher download duplication costs little upload and can improve download reliability on lossy links.",
                ),
                SettingsGuideEntry(
                    title = "Upload Compress / Download Compress",
                    body = "Compression method negotiated for each direction: OFF, ZSTD, LZ4, or ZLIB.",
                    effect = "Compression can reduce traffic for text-like data, but adds CPU work and may not help already-compressed media or encrypted app traffic.",
                ),
                SettingsGuideEntry(
                    title = "Base Encode Data",
                    body = "Encodes payload labels into a more DNS-safe form before tunneling.",
                    effect = "Usually keep it off for efficiency. Turn it on only if a resolver path behaves better with safer labels.",
                ),
            ),
        ),
        SettingsGuideSection(
            title = "Reliability and diagnostics",
            entries = listOf(
                SettingsGuideEntry(
                    title = "Ping Watchdog",
                    body = "If non-ping traffic is active but no server response arrives for this many seconds, StormDNS restarts the session. Zero disables it.",
                    effect = "Helps recover a silent zombie tunnel. Too short can restart healthy but slow sessions.",
                ),
                SettingsGuideEntry(
                    title = "Traffic Warmup / Warmup Probes / Keepalive",
                    body = "WhiteDNS can send small test requests after connect and then keep sending occasional probes.",
                    effect = "Can make VPN/proxy verification and first traffic feel smoother, but uses a small amount of data and battery.",
                ),
                SettingsGuideEntry(
                    title = "Log Level",
                    body = "Controls how much diagnostic information StormDNS prints: DEBUG, INFO, WARN, or ERROR.",
                    effect = "DEBUG and INFO help troubleshooting but create noisier logs. WARN or ERROR are quieter for normal use.",
                ),
            ),
        ),
    )

    override val menuAppSettings = "App Settings"
    override val menuDonate = "Donate"
    override val menuVersion = "Version"

    override val setupTitle = "Setup Connection"
    override val setupAddConnection = "Add Connection Profile"
    override val setupAddResolver = "Add Resolver Profile"

    override val selectorConnectionProfiles = "Connection Profiles"
    override val selectorResolverProfiles = "Resolver Profiles"
    override val selectorSettingProfiles = "Setting Profiles"
    override val resolverNotSelected = "No resolver selected"
    override val resolverRequired = "A resolver is required to connect."

    override val logsTitle = "CONNECTION LOGS"
    override val logsClear = "CLEAR"
    override val logsCopy = "COPY"

    override val scanBtnStart = "START"
    override val scanBtnStop = "STOP"
    override val scanBtnSaveAs = "SAVE AS"
    override val scanBtnResume = "RESUME"
    override val scanWorkerWarning = "High worker count may affect device performance."
    override val scanStatusTitle = "SCAN STATUS"
    override val scanLabelTotal = "Total"
    override val scanLabelValid = "Valid"
    override val scanLabelRejected = "Rejected"
    override val scanLabelStatus = "Status"
    override val scanLabelSource = "Source"
    override val scanLabelWorkers = "Workers"
    override val scanLabelProgress = "Progress"
    override val scanAutoSave = "Auto-save results"

    override val serverRouteMissing = "Server route not available"

    override val supportTitle = "SUPPORT WHITEDNS"
    override val supportBody = "WhiteDNS is free and open source. If you find it useful, consider supporting development."

    // Scan tab additional
    override val scanDefaultList = "DEFAULT LIST"
    override val scanSelectFile = "SELECT FILE"
    override val scanProfileLabel = "Scan Profile"
    override val scanAutoSaveTitle = "SCAN AUTO SAVE"
    override val scanSaveAsTitle = "SAVE SCAN RESULTS"
    override val scanSaveAsName = "Name"
    override val scanSaveAsPlaceholder = "Fast Tunnel"
    override val scanNoFileSelected = "No file selected"
    override val scanMessageLabel = "Message"

    // Setup card
    override val setupResolversLabel = "Resolvers"
    override val setupAddConnectionSupportingText = "Server domain and key"
    override val setupAddResolverSupportingText = "DNS resolver list"
    override val setupManualResolvers = "Manual resolvers"
    override val setupSectionSetup = "SETUP"

    // Connection info card
    override val infoCardConnection = "CONNECTION INFO"
    override val infoLabelMode = "Mode"
    override val infoLabelSocks5Proxy = "SOCKS5 Proxy"
    override val infoLabelHttpProxy = "HTTP Proxy"
    override val infoLabelAuth = "Auth"
    override val infoLabelUser = "User"
    override val infoLabelPass = "Pass"
    override val infoLabelSplitTunnel = "Split Tunnel"
    override val infoLabelApps = "Apps"
    override val infoLabelConnectionProfile = "Connection"
    override val infoLabelResolverProfile = "Resolver"
    override val infoLabelSettingProfile = "Setting"
    override val infoLabelProtocol = "Protocol"
    override val infoLabelAuthOn = "On"
    override val infoLabelAuthOff = "Off"

    // Speed indicators
    override val speedDown = "Down"
    override val speedUp = "Up"
    override val speedTotalUsage = "Total Usage"

    // Resolver runtime
    override val resolverActiveResolvers = "Active Resolvers"
    override val resolverValidResolvers = "Valid Resolvers"
    override val resolverPending = "Pending"
    override val resolverNoResolvers = "You need resolvers to connect."
    override val backgroundScanningInProgress = "Background scanning in progress"

    // Profile dialogs
    override val profileDialogCreateSetting = "CREATE SETTING PROFILE"
    override val profileDialogEditSetting = "EDIT SETTING PROFILE"
    override val profileDialogCreateResolver = "CREATE RESOLVER PROFILE"
    override val profileDialogEditResolver = "EDIT RESOLVER PROFILE"
    override val profileDialogCreateConnection = "CREATE NEW CONNECTION"
    override val profileDialogEditConnection = "EDIT CONNECTION"
    override val profileDialogImportSettings = "IMPORT SETTINGS PROFILE"
    override val profileDialogImportConnection = "IMPORT CONNECTION"
    override val profileDialogExportConnection = "EXPORT CONNECTION"
    override val profileDialogExportAllConnections = "EXPORT ALL CONNECTIONS"
    override val profileDialogExportAllResolvers = "EXPORT ALL RESOLVERS"
    override val profileDialogExportSettings = "EXPORT SETTINGS"
    override val profileExportResolverTotalTemplate = "Total: %s"
    override val profileExportSavingFile = "Saving client_resolvers.txt..."
    override val profileExportSavedToTemplate = "Saved to %s"
    override val profileFieldName = "Name"
    override val profileFieldResolvers = "Resolvers"
    override val profileFieldProfileLinks = "Profile Links"
    override val profileFieldToml = "TOML"
    override val profileNamePlaceholderFastTunnel = "Fast Tunnel"
    override val profileNamePlaceholderHomeResolvers = "Home Resolvers"
    override val profileNamePlaceholderImportedSettings = "Imported Settings"
    override val profileNamePlaceholderConnection = "My Connection"
    override val profileResolverPlaceholder = "1.1.1.1, 8.8.8.8"

    // Profile menu actions
    override val profileMenuExport = "Export profile"
    override val profileMenuEdit = "Edit profile"
    override val profileMenuDelete = "Delete profile"
    override val profileMenuUse = "Use profile"
    override val profileMenuUseSelected = "Use profile (selected)"

    // Profile list empty states
    override val profileNoResolverLists = "No saved resolver lists yet."
    override val profileNoSettingProfiles = "No saved setting profiles yet."
    override val profileQrUnavailable = "QR code unavailable for this profile link."

    // Profile action buttons
    override val profileBtnCreate = "CREATE"
    override val profileBtnImport = "IMPORT"
    override val profileBtnDeleteDups = "DELETE DUPS"
    override val profileBtnExportAll = "EXPORT ALL"
    override val profileBtnSaveCurrent = "SAVE CURRENT"
    override val profileBtnImportFile = "IMPORT FILE"
    override val profileBtnClear = "CLEAR"

    // Delete confirmation dialogs
    override val deleteConnectionTitle = "DELETE CONNECTION"
    override val deleteResolverTitle = "DELETE RESOLVER PROFILE"
    override val deleteSettingTitle = "DELETE SETTING PROFILE"
    override val deleteDupsTitle = "DELETE DUPLICATE RESOLVERS"

    // GroupLabels
    override val groupMtu = "MTU"
    override val groupRuntimeWorkers = "Runtime Workers, Queues, and Timers"
    override val groupLocalProxy = "Local Proxy"
    override val groupNetworkTuning = "Network Tuning"
    override val groupReliability = "Reliability"
    override val groupDefault = "Default"
    override val groupCustomSettings = "Custom Settings"
    override val groupParallelTestResults = "Parallel Test Results"

    // Advanced settings field labels
    override val settingListenIp = "Listen IP"
    override val settingListenPort = "Listen Port"
    override val settingHttpProxy = "HTTP Proxy"
    override val settingHttpPort = "HTTP Port"
    override val settingSocks5Auth = "SOCKS5 Authentication"
    override val settingUsername = "Username"
    override val settingPassword = "Password"
    override val settingBalancingStrategy = "Balancing Strategy"
    override val settingUploadDup = "Upload Dup"
    override val settingDownloadDup = "Download Dup"
    override val settingUploadCompress = "Upload Compress"
    override val settingDownloadCompress = "Download Compress"
    override val settingBaseEncodeData = "Base Encode Data"
    override val settingPingWatchdog = "Ping Watchdog"
    override val settingTrafficWarmup = "Traffic Warmup"
    override val settingWarmupProbes = "Warmup Probes"
    override val settingKeepalive = "Keepalive"
    override val settingLogLevel = "Log Level"
    override val settingSearch = "Search"
    override val settingMinUpload = "Min Upload"
    override val settingMinDownload = "Min Download"
    override val settingMaxUpload = "Max Upload"
    override val settingMaxDownload = "Max Download"
    override val settingResolverRetries = "Resolver Retries"
    override val settingResolverTimeout = "Resolver Timeout"
    override val settingResolverParallel = "Resolver MTU Parallelism"
    override val settingResolverParallelNote = "More parallel MTU tests can make the first connection faster, but may put pressure on the phone."
    override val settingLogsRetries = "Logs Retries"
    override val settingLogsTimeout = "Logs Timeout"
    override val settingLogsParallel = "Logs Parallel"
    override val settingRxTxWorkers = "RX/TX Workers"
    override val settingProcessWorkers = "Process Workers"
    override val settingTunnelPacketTimeout = "Tunnel Packet Timeout"
    override val settingIdlePoll = "Idle Poll"
    override val settingTxChannel = "TX Channel"
    override val settingRxChannel = "RX Channel"
    override val settingUdpPool = "UDP Pool"
    override val settingStreamQueue = "Stream Queue"
    override val settingOrphanQueue = "Orphan Queue"
    override val settingDnsFragments = "DNS Fragments"
    override val settingSocksUdpTimeout = "SOCKS UDP Timeout"
    override val settingTerminalRetain = "Terminal Retain"
    override val settingCancelledRetain = "Cancelled Retain"
    override val settingRetryBase = "Retry Base"
    override val settingRetryStep = "Retry Step"
    override val settingRetryLinear = "Retry Linear"
    override val settingRetryMax = "Retry Max"
    override val settingBusyRetry = "Busy Retry"
    override val settingSettingLabel = "Setting"

    // Split Tunnel
    override val splitTunnelTitle = "SPLIT TUNNEL"
    override val splitTunnelAppRouting = "App Routing"
    override val splitTunnelSelected = "Selected"
    override val splitTunnelSelectApps = "SELECT APPS"
    override val splitTunnelNoAppsFound = "No apps found."
    override val splitTunnelSearchPlaceholder = "Search apps"
    override val splitTunnelDialogTitle = "SELECT APPS"
    override val splitTunnelSearchLabel = "Search"

    // Connection logs (inline panel)
    override val logsInlineTitle = "CONNECTION LOGS"
    override val logsDiagnostics = "DIAGNOSTICS"

    // Notification permission banner
    override val bannerNotificationBlockedTitle = "VPN NOTIFICATION BLOCKED"
    override val bannerNotificationBlockedBody = "WhiteDNS needs permission to show notifications so the VPN stays running. Tap below to grant permission."
    override val bannerFullVpnWarningTitle = "FULL VPN PERFORMANCE WARNING"
    override val bannerFullVpnWarningBody = "Full VPN mode routes all device traffic through WhiteDNS. This may increase latency and battery usage compared to Proxy mode."

    // Parallel test UI
    override val parallelTestOpenLabel = "Parallel Test ▲"
    override val parallelTestClosedLabel = "Parallel Test ▼"
    override val parallelTestDescription = "Select configs to test in parallel. The fastest config will be selected automatically."
    override val parallelTestYourConfigs = "Your configs"

    // Connect tab messages
    override val connectNeedResolvers = "You need resolvers to connect."
    override val connectSelectedCount = "Selected"

    // Auto-tune / parallel test results
    override val autoTuneSaveSettingAs = "SAVE SETTING AS"
    override val autoTuneMtuFail = "Fail"
    override val autoTuneMtuPass = "Pass"
    override val autoTuneMtuTest = "Test"
    override val autoTuneSpeedLabel = "Speed"
    override val autoTunePingLabel = "Ping"
    override val autoTuneStatusStarting = "Starting"
    override val autoTuneMeasuringSpeed = "Measuring speed"
    override val cdParallelTestSpeed = "Parallel Test speed"
    override val cdParallelTestPing = "Parallel Test ping"
    override val cdConnectButtonDisconnected = "Connect button - tap to start VPN"
    override val cdConnectButtonConnecting = "Connecting - establishing VPN connection"
    override val cdConnectButtonConnected = "Stop button - tap to disconnect VPN"
    override val cdAutoTuneMtuFailed = "MTU failed"
    override val cdAutoTuneMtuPassed = "MTU passed"
    override val cdAutoTuneMtuTesting = "MTU testing"

    override val shareSubjectProfile = "WhiteDNS profile"
    override val shareChooserProfile = "Export WhiteDNS profile"
    override val shareChooserClientConfig = "Export client_config.toml"
    override val shareChooserAdvancedSettings = "Export advanced_settings.toml"
    override val shareChooserResolvers = "Export client_resolvers.txt"

    override val errorUnableToOpenResolverFile = "Unable to open resolver file"
    override val errorInvalidResolverIpTemplate = "Invalid resolver IP: %s"
    override val errorNoResolverEntries = "No resolver entries found in file"
    override val errorEnterValidResolverIp = "Enter at least one valid resolver IP."
    override val errorEnterProfileNameToSave = "Enter a profile name to save."
    override val resolverValidSingularTemplate = "%d valid resolver."
    override val resolverValidPluralTemplate = "%d valid resolvers."
    override val advancedProfileModifiedSuffix = "(modified)"
    override val cdEditPrefix = "Edit"
    override val resolverFieldPlaceholder = "1.1.1.1, 8.8.8.8 or one per line"
    override val dropdownPlaceholderSelect = "Select"
    override val setupDefaultResolver = "Default Resolver"
    override val setupDefaultConnection = "Connection"
    override val setupDefaultAdvanced = "Default"

    override val balancingStrategyRandom = "Random"
    override val balancingStrategyRoundRobin = "Round Robin"
    override val balancingStrategyLeastLoss = "Least Loss"
    override val balancingStrategyLowestLatency = "Lowest Latency"
    override val compressionOff = "OFF"
    override val compressionZstd = "ZSTD"
    override val compressionLz4 = "LZ4"
    override val compressionZlib = "ZLIB"
    override val splitTunnelAllAppsChoice = "All Apps"
    override val splitTunnelOnlySelectedChoice = "Only Selected"
    override val splitTunnelBypassSelectedChoice = "Bypass Selected"
    override val encryptionMethodNone = "None"
    override val encryptionMethodXor = "XOR"
    override val encryptionMethodChacha20 = "ChaCha20"
    override val encryptionMethodAes128 = "AES-128-GCM"
    override val encryptionMethodAes192 = "AES-192-GCM"
    override val encryptionMethodAes256 = "AES-256-GCM"

    // Download TOML dialog
    override val downloadTomlTitle = "DOWNLOAD TOML"
    override val downloadTomlBtn = "DOWNLOAD TOML"

    // Save setting as button
    override val saveSettingAs = "SAVE SETTING AS"

    // Validation messages
    override val validationEnterResolverIp = "Enter resolver IP or domain"
    override val validationEnterProfileName = "Enter a profile name"

    // HomeSelectorCard
    override val homeSelectorSettingLabel = "Setting"
    override val homeSelectorUnsavedChanges = "Unsaved changes"

    override val homeSelectorNoSavedLists = "No saved lists"
    override val homeSelectorNotSelected = "Not selected"
    override val homeSelectorResolverProfileFallback = "Resolver Profile"
    override val homeSelectorSearchConnections = "Search connections"
    override val homeSelectorSearchResolvers = "Search resolvers"
    override val homeSelectorSearchSettings = "Search setting profiles"
    override val homeSelectorCustomAdvanced = "Custom Advanced"
    override val profileNameCopySuffix = "Copy"
    override val settingProfileFastTunnelPlaceholder = "Fast tunnel"
    override val resolverProfileHomeResolversPlaceholder = "Home resolvers"
    override val settingProfileImportedSettingsPlaceholder = "Imported settings"
    override val homeSelectorNoConnectionProfiles = "No connection profiles found."
    override val homeSelectorNoResolverProfiles = "No resolver profiles found."
    override val homeSelectorNoSettingProfiles = "No setting profiles found."

    override val setupNoResolversConfigured = "No resolvers configured"
    override val setupInvalidResolverIp = "Invalid resolver IP"
    override val setupServerRouteAndKey = "Server route and key missing"
    override val setupEncryptionKeyMissing = "Encryption key missing"

    override val scanProfileNeedsServer = "needs a server route and key."
    override val scanProfileFallback = "Scan profile"
    override val scanResultsTitle = "Scan Results"
    override val scanCurrentScan = "Current scan"
    override val scanFieldWorkers = "Workers"
    override val scanSaveBodyTemplate = "valid resolvers will be saved as a new resolver profile."
    override val scanStatusReady = "Ready"
    override val scanStatusStarting = "Starting"
    override val scanStatusRunning = "Running"
    override val scanStatusCompleted = "Completed"
    override val scanStatusFailed = "Failed"
    override val scanStatusStopped = "Stopped"
    override val scanStatusIdle = "Idle"

    override val groupCustomConnections = "Custom Connections"
    override val customConnectionsEmpty = "No custom StormDNS connections yet."
    override val profileFieldDomain = "Domain"
    override val profileFieldEncryptionKey = "Encryption Key"
    override val profileFieldEncryptionMethod = "Encryption Method"
    override val profileDomainPlaceholder = "v.example.com"
    override val profileEncryptionKeyPlaceholder = "32-character key"
    override val profileMyStormDnsPlaceholder = "My StormDNS"
    override val profileDomainFallback = "Custom StormDNS"
    override val profileStatusActive = "ACTIVE"
    override val profileStatusSelected = "SELECTED"
    override val profileStatusModified = "MODIFIED"
    override fun resolverProfileSummary(count: Int): String =
        "$count resolver${if (count == 1) "" else "s"}"
    override val profileFieldProfileLinkSingle = "Profile Link"
    override val dialogDeleteConfirm = "DELETE"
    override val deleteConnectionMessageTemplate = "Delete this connection profile? This cannot be undone."
    override val deleteResolverMessageTemplate = "Delete this resolver profile? This cannot be undone."
    override val deleteSettingMessageTemplate = "Delete this setting profile? This cannot be undone."
    override val deleteDupsMessageSingleConnection = "Delete duplicate connection profile? Duplicates are matched by server domain and encryption key. The active or selected profile is kept when possible."
    override val deleteDupsMessageManyConnection = "Delete duplicate connection profiles? Duplicates are matched by server domain and encryption key. The active or selected profile is kept when possible."

    override val footerPoweredBy = "Powered by WhiteDNS"

    override val verificationVerifying = "Verifying"
    override val verificationVerified = "Verified"
    override val verificationNeedsAttention = "Needs Attention"
    override val verificationPending = "Pending"
    override val verificationNotRunYet = "Connection verification has not run yet"
    override val verificationCheckingRoute = "Checking tunnel route"
    override val verificationProxyReachable = "Connection verified: proxy tunnel can reach the internet"
    override val verificationVpnReachable = "Connection verified: VPN tunnel can reach the internet"
    override val verificationProxyWarming = "Connection ready: proxy tunnel is active; outbound probe is still warming up"
    override val verificationVpnWarming = "Connection ready: VPN tunnel is active; outbound probe is still warming up"
    override val verificationModeChanged = "Connection mode changed before verification finished"
    override val verificationSocksNotReachable = "Connection verification failed: local SOCKS listener is not reachable"
    override val verificationVpnInterfaceInactive = "Connection verification failed: VPN interface is not active"

    override val noResolversPlaceholder = "No resolvers"
    override val whiteDnsResolversLabel = "WhiteDNS resolvers"
    override val whiteDnsConfigsLabel = "WhiteDNS configs"
    override val whiteDnsConfigsDescription = "Conservative: lower data usage, recommended for most users"
    override val whiteDnsAggressiveConfigsLabel = "High usage configs"
    override val whiteDnsAggressiveConfigsDescription = "High usage: use when internet usage is not a concern"
    override val whiteDnsLogsLabel = "WhiteDNS logs"
    override val whiteDnsDiagnosticsLabel = "WhiteDNS diagnostics"
    override val parallelTestCollapseDescription = "Collapse Parallel Test configs"
    override val parallelTestExpandDescription = "Expand Parallel Test configs"

    override val autoTuneStartingTest = "Starting test"
    override val autoTuneFailedFallback = "Failed"
    override val autoTuneMeasuredKeyword = "Measured"

    override val dropdownSelectFallback = "Select"

    override val splitTunnelAllApps = "All apps"
    override val splitTunnelNoApps = "No apps"
    override val splitTunnelOnlyPrefix = "Only"
    override val splitTunnelBypassPrefix = "Bypass"

    override val tapToCollapse = "TAP TO COLLAPSE"
    override val tapToConfigure = "TAP TO CONFIGURE"
    override val parallelTestOpen = "OPEN"
    override val parallelTestClosed = "CLOSED"

    override val appsSearchPlaceholder = "App name or package"

    override val profileFieldProfileLinksLabel = "Profile Links"

    override val cdCloseSelector = "Close selector"
    override val cdSelected = "Selected"
    override val cdDismissScannerInfo = "Dismiss scanner info"
    override val cdEditField = "Edit"
    override val cdDragToReorder = "Drag to reorder profile"
    override val cdProfileQrCode = "Profile QR code"
    override val cdAppMenu = "App menu"
    override val cdAppSettings = "App settings"
    override val cdDonate = "Donate"
    override val cdDismissBatteryWarning = "Dismiss battery optimization warning"
    override val cdDismissVpnWarning = "Dismiss full VPN warning"

    override val resolverCountTemplate = "resolvers configured"
    override val resolverCountOneTemplate = "resolver configured"

    override val genericConnectionFallback = "Connection"
    override val genericResolverFallback = "Resolver"
    override val genericSettingFallback = "Setting"
    override val scanProfileMenuActions = "Scan profile actions"
    override val settingProfileMenuActions = "Setting profile actions"
    override val connectionProfileMenuActions = "Connection profile actions"
    override val resolverProfileMenuActions = "Resolver profile actions"
    override val useSettingProfile = "Use setting profile"
    override val useResolverProfile = "Use resolver profile"
    override val exportConnectionProfileAction = "Export connection profile"
    override val editConnectionProfileAction = "Edit connection profile"
    override val deleteConnectionProfileAction = "Delete connection profile"
    override val deleteConnectionProfileBlockedAction = "Connected profile cannot be deleted"
    override val exportSettingProfileAction = "Export setting profile"
    override val editSettingProfileAction = "Edit setting profile"
    override val deleteSettingProfileAction = "Delete setting profile"
    override val editResolverProfileAction = "Edit resolver profile"
    override val deleteResolverProfileAction = "Delete resolver profile"
    override val brandWhiteDns = "WhiteDNS"

    override val errorImportSettingsFile = "Unable to open settings file"
    override val errorImportSettings = "Unable to import settings"
    override val errorImportResolver = "Unable to import resolver file"
    override val errorImportProfile = "Unable to import profile"
    override val errorExportProfile = "Unable to export profile"

    override val resolverScanResults = "Scan Results"
    override val scanResultsSuffix = "Results"
    override val noResolverEntriesError = "No resolver entries found in file"

    override val profileImportSuccess = "Profile imported"
    override val profileBtnScanQr = "SCAN QR"
    override val qrScanNoCode = "No WhiteDNS QR profile found"
    override val qrScanCancelled = "QR scan cancelled"

}

object PersianStrings : WhiteDnsStrings {
    override val tabProfiles = "پروفایل‌ها"
    override val tabConnect = "اتصال"
    override val tabScan = "اسکن"
    override val tabLogs = "لاگ‌ها"

    override val btnConnect = "اتصال"
    override val btnConnecting = "در حال اتصال"
    override val btnStop = "قطع"

    override val btnClose = "بستن"
    override val btnSave = "ذخیره"
    override val btnCancel = "لغو"
    override val btnCreate = "ایجاد"
    override val btnImport = "وارد کردن"
    override val btnDelete = "حذف"
    override val btnCopy = "کپی"
    override val btnShare = "اشتراک‌گذاری"

    override val appSettingsTitle = "تنظیمات برنامه"
    override val fieldTheme = "قالب"
    override val fieldLanguage = "زبان"
    override val themeModeAuto = "خودکار"
    override val themeModeLight = "روشن"
    override val themeModeDark = "تاریک"
    override val languageEn = "English"
    override val languageFa = "فارسی"

    override val fieldMode = "حالت"
    override val connectionModeProxy = "حالت پروکسی"
    override val connectionModeVpn = "VPN کامل"
    override val sectionConnection = "اتصال"
    override val sectionResolver = "ریزالور"

    override val bannerBatteryTitle = "VPN پس‌زمینه ممکن است متوقف شود"
    override val bannerBatteryBody = "به WhiteDNS اجازه دهید بهینه‌سازی باتری را نادیده بگیرد تا VPN بعد از خروج از برنامه فعال بماند."
    override val bannerAllowBackground = "اجازه VPN پس‌زمینه"
    override val bannerNotificationTitle = "اعلان VPN غیرفعال است"
    override val bannerNotificationBody = "اعلان VPN را فعال کنید تا اتصال در پس‌زمینه پایدار بماند."
    override val bannerEnableNotification = "فعال‌سازی اعلان VPN"
    override val bannerVpnWarningTitle = "هشدار عملکرد VPN کامل"
    override val bannerVpnWarningBody = "حالت VPN کامل ممکن است عملکرد را تحت‌تأثیر قرار دهد. حالت پروکسی برای اکثر کاربران توصیه می‌شود."

    override val parallelTest = "تست موازی"
    override val connectProgressConnected = "متصل شد"
    override val serverTestTitle = "تست سرور"
    override val serverTestButton = "تست سرورها"
    override val serverTestSingleButton = "تست این سرور"
    override val serverTestRunning = "در حال تست"
    override val serverTestIdle = "آماده تست سرورهای ذخیره‌شده"
    override val serverTestReady = "آماده"
    override val serverTestFailed = "ناموفق"
    override val serverTestMeasuring = "در حال اندازه‌گیری"
    override val serverTestStarting = "در حال شروع"
    override val serverTestPending = "در انتظار"
    override val serverTestProgressTemplate = "تست سرور: %d/%d سرور اندازه‌گیری شد"
    override val serverTestSummaryTemplate = "تست سرور تکمیل شد: %d آماده، %d ناموفق"
    override val serverTestNoSavedServers = "تست سرور: هیچ پروفایل سروری ذخیره نشده است"
    override val serverTestNoConnectedResolvers = "تست سرور: ریزالور متصل در دسترس نیست"
    override val serverTestFailedTemplate = "تست سرور ناموفق بود: %s"
    override val serverTestConnectionRequired = "نیاز به اتصال"
    override val serverTestServiceConnected = "متصل"
    override val serverTestServiceTesting = "در حال تست"
    override val serverTestServiceGood = "سالم"
    override val serverTestServiceFair = "ترکیبی"
    override val serverTestServicePoor = "نیاز به بررسی"
    override val serverTestScoreGood = "خوب"
    override val serverTestScoreFair = "متوسط"
    override val serverTestScorePoor = "ضعیف"
    override val serverTestScoreUnavailable = "در دسترس نیست"

    override val profileTabConnection = "اتصال"
    override val profileTabResolver = "ریزالور"
    override val profileTabSetting = "تنظیمات"

    override val settingGuideTitle = "راهنمای پروفایل تنظیمات"
    override val settingGuideIntro = "پروفایل تنظیمات مشخص می‌کند کلاینت MasterDNS/StormDNS بعد از انتخاب اتصال و لیست ریزالورها چطور کار کند. بیشتر گزینه‌ها همان تنظیمات runtime در client_config.toml مربوط به MasterDNS/StormDNS هستند؛ بخش‌های مخصوص اندروید بر اساس رفتار WhiteDNS توضیح داده شده‌اند."
    override val settingGuideSource = "بر اساس client_config.toml در MasterDNS/StormDNS و رفتار اجرایی WhiteDNS."
    override val settingGuideEffectLabel = "اثر"
    override val cdSettingGuide = "باز کردن راهنمای پروفایل تنظیمات"
    override val settingGuideSections = listOf(
        SettingsGuideSection(
            title = "کشف MTU",
            entries = listOf(
                SettingsGuideEntry(
                    title = "حداقل آپلود / حداقل دانلود",
                    body = "کمترین اندازه payload آپلود و دانلود که در تست MTU ریزالورها قبول می‌شود. آپلود یعنی از گوشی شما به سرور تونل، دانلود یعنی از سرور به گوشی شما.",
                    effect = "حداقل بالاتر ریزالورهای ضعیف را زودتر حذف می‌کند، اما اگر زیادی بالا باشد ممکن است در شبکه‌های سخت‌گیر هیچ ریزالور قابل استفاده‌ای باقی نماند.",
                ),
                SettingsGuideEntry(
                    title = "حداکثر آپلود / حداکثر دانلود",
                    body = "بالاترین اندازه‌ای که StormDNS در هر جهت برای پیدا کردن MTU امتحان می‌کند.",
                    effect = "عدد بالاتر می‌تواند روی مسیرهای DNS آزادتر سرعت بهتری بدهد، ولی اسکن طولانی‌تر می‌شود و روی ریزالورهای محدود احتمال خطا بیشتر است.",
                ),
                SettingsGuideEntry(
                    title = "تلاش مجدد / تایم‌اوت / موازی‌سازی ریزالور",
                    body = "برای اسکن کامل از لیست ریزالورها استفاده می‌شود. تلاش مجدد پروب ناموفق را تکرار می‌کند، تایم‌اوت مدت انتظار هر پروب است، و موازی‌سازی تعداد ریزالورهایی است که همزمان تست می‌شوند.",
                    effect = "تلاش مجدد یا تایم‌اوت بیشتر دقت را در لینک بد بالا می‌برد، اما اتصال دیرتر شروع می‌شود. موازی‌سازی بیشتر شروع را سریع‌تر می‌کند ولی CPU، باتری و burst شبکه بیشتری مصرف می‌کند.",
                ),
                SettingsGuideEntry(
                    title = "تلاش مجدد / تایم‌اوت / موازی‌سازی لاگ‌ها",
                    body = "وقتی StormDNS به‌جای اسکن کامل از ریزالورهای ذخیره‌شده در لاگ شروع می‌کند، این مقادیر استفاده می‌شوند.",
                    effect = "عدد بالاتر شروع از کش را مطمئن‌تر می‌کند. عدد پایین‌تر وقتی به ریزالورهای قبلی اعتماد دارید اتصال مجدد را سریع‌تر می‌کند.",
                ),
            ),
        ),
        SettingsGuideSection(
            title = "ورکرها، صف‌ها و تایمرهای اجرا",
            entries = listOf(
                SettingsGuideEntry(
                    title = "ورکرهای RX/TX / ورکرهای پردازش",
                    body = "ورکرهای پس‌زمینه برای خواندن و نوشتن UDP تونل و پردازش بسته‌ها.",
                    effect = "ورکر بیشتر روی گوشی سریع و ترافیک سنگین کمک می‌کند. ورکر زیاد باتری و CPU را هدر می‌دهد؛ WhiteDNS تعداد ورکر پردازش را حداقل برابر RX/TX نگه می‌دارد.",
                ),
                SettingsGuideEntry(
                    title = "تایم‌اوت بسته تونل / نظرسنجی بیکار",
                    body = "تایم‌اوت بسته مشخص می‌کند کار async تونل چقدر منتظر بماند. نظرسنجی بیکار تعیین می‌کند وقتی چیزی برای ارسال نیست dispatcher هر چند وقت بیدار شود.",
                    effect = "عدد کوتاه‌تر واکنش را سریع‌تر می‌کند ولی ممکن است CPU بسوزاند یا بسته کند را حذف کند. عدد بلندتر آرام‌تر است ولی حس پاسخ‌گویی را کمتر می‌کند.",
                ),
                SettingsGuideEntry(
                    title = "کانال TX / کانال RX / استخر UDP",
                    body = "اندازه buffer برای بسته‌های خروجی و ورودی تونل، و تعداد سوکت‌های UDP قابل استفاده دوباره برای هر ریزالور.",
                    effect = "عدد بزرگ‌تر burst را بهتر تحمل می‌کند و ساخت سوکت را کم می‌کند، اما حافظه بیشتری مصرف می‌کند و ممکن است ازدحام را پنهان کند.",
                ),
                SettingsGuideEntry(
                    title = "صف استریم / صف یتیم / قطعات DNS",
                    body = "فضای اولیه برای داده استریم، بسته‌های کنترلی بدون جفت، و پاسخ‌های DNS چندتکه.",
                    effect = "صف بزرگ‌تر برای session شلوغ یا fragment شده بهتر است. صف کوچک‌تر حافظه را کم می‌کند ولی زیر بار ممکن است کارها حذف شوند.",
                ),
                SettingsGuideEntry(
                    title = "تایم‌اوت UDP برای SOCKS / نگهداری پایانه‌ای / نگهداری لغو شده",
                    body = "تایمرهای پاکسازی برای sessionهای SOCKS UDP، استریم‌های تمام‌شده، و تلاش‌های راه‌اندازی که لغو شده‌اند.",
                    effect = "نگهداری طولانی‌تر به بسته‌های دیررس فرصت می‌دهد تمیز تمام شوند. نگهداری کوتاه‌تر حافظه را زودتر آزاد می‌کند ولی مسیرهای cleanup کند را خراب می‌کند.",
                ),
                SettingsGuideEntry(
                    title = "پایه / گام / خطی / حداکثر / تلاش مجدد مشغول",
                    body = "برنامه تلاش مجدد برای شروع session بعد از خطای setup، reset، یا پاسخ SESSION_BUSY از سرور.",
                    effect = "تلاش مجدد تهاجمی سریع‌تر وصل می‌کند ولی ترافیک بیشتری می‌سازد. تلاش کندتر برای سرور شلوغ یا شبکه ناپایدار ملایم‌تر است.",
                ),
            ),
        ),
        SettingsGuideSection(
            title = "پروکسی محلی",
            entries = listOf(
                SettingsGuideEntry(
                    title = "IP شنیداری / پورت شنیداری",
                    body = "آدرس و پورتی که پروکسی SOCKS5 محلی روی آن گوش می‌دهد. 127.0.0.1 آن را فقط داخل گوشی نگه می‌دارد.",
                    effect = "با تغییر پورت، برنامه‌ها باید آدرس پروکسی جدید را استفاده کنند. bind گسترده مثل 0.0.0.0 می‌تواند پروکسی را در شبکه در دسترس کند.",
                ),
                SettingsGuideEntry(
                    title = "پروکسی HTTP / پورت HTTP",
                    body = "WhiteDNS می‌تواند کنار SOCKS5 یک پل HTTP proxy هم باز کند تا برنامه‌هایی که SOCKS5 ندارند کار کنند.",
                    effect = "برای سازگاری مفید است، اما یک listener محلی دیگر باز می‌کند و باید پورتی بگیرد که قبلا اشغال نشده باشد.",
                ),
                SettingsGuideEntry(
                    title = "احراز هویت SOCKS5 / نام کاربری / رمز عبور",
                    body = "نام کاربری و رمز عبور اختیاری برای پروکسی SOCKS5 محلی. این فقط پروکسی محلی را محافظت می‌کند، نه سرور StormDNS را.",
                    effect = "اگر پروکسی از خارج گوشی قابل دسترسی است آن را روشن کنید. برنامه‌ها باید همان نام کاربری و رمز عبور را داشته باشند.",
                ),
            ),
        ),
        SettingsGuideSection(
            title = "تنظیم شبکه",
            entries = listOf(
                SettingsGuideEntry(
                    title = "استراتژی توازن",
                    body = "روش انتخاب ریزالور: تصادفی، نوبتی، کمترین افت، یا کمترین تاخیر. حالت‌های افت و تاخیر بعد از شروع ترافیک از آمار runtime استفاده می‌کنند.",
                    effect = "کمترین افت معمولا در شبکه بد پایدارتر است. کمترین تاخیر می‌تواند سریع‌تر حس شود، اما شاید مسیرهای سریع‌تر ولی کم‌اعتمادتر را انتخاب کند.",
                ),
                SettingsGuideEntry(
                    title = "تکرار آپلود / تکرار دانلود",
                    body = "تعداد کپی‌هایی که StormDNS برای داده آپلود و بسته‌های پشتیبان دانلود می‌فرستد. تکرار دانلود بیشتر ACK/NACKهای کوچک است.",
                    effect = "تکرار آپلود مصرف واقعی آپلود را چندبرابر می‌کند. تکرار دانلود هزینه آپلود کمی دارد و روی لینک packet loss دار دانلود را پایدارتر می‌کند.",
                ),
                SettingsGuideEntry(
                    title = "فشرده‌سازی آپلود / فشرده‌سازی دانلود",
                    body = "روش فشرده‌سازی هر جهت: خاموش، ZSTD، LZ4 یا ZLIB.",
                    effect = "برای داده‌های متنی می‌تواند مصرف را کم کند، اما CPU مصرف می‌کند و روی مدیا یا ترافیک از قبل رمزنگاری/فشرده معمولا کمک زیادی نمی‌کند.",
                ),
                SettingsGuideEntry(
                    title = "رمزگذاری پایه داده",
                    body = "payload labelها را قبل از تونل به شکل امن‌تر برای DNS تبدیل می‌کند.",
                    effect = "معمولا برای بازدهی بهتر خاموش بماند. فقط وقتی روشن کنید که یک مسیر ریزالور با labelهای امن‌تر بهتر کار می‌کند.",
                ),
            ),
        ),
        SettingsGuideSection(
            title = "پایداری و تشخیص",
            entries = listOf(
                SettingsGuideEntry(
                    title = "نظارت Ping",
                    body = "اگر ترافیک غیر ping فعال باشد ولی در این تعداد ثانیه هیچ پاسخی از سرور نیاید، StormDNS session را ری‌استارت می‌کند. صفر آن را خاموش می‌کند.",
                    effect = "برای بیرون آمدن از حالت تونل خاموش و بی‌پاسخ مفید است. عدد خیلی کوتاه ممکن است session سالم ولی کند را بی‌دلیل ری‌استارت کند.",
                ),
                SettingsGuideEntry(
                    title = "گرم‌کردن ترافیک / پروب‌ها / Keepalive",
                    body = "WhiteDNS می‌تواند بعد از اتصال چند درخواست کوچک تستی بفرستد و سپس هر چند وقت یک پروب سبک ارسال کند.",
                    effect = "ممکن است تایید اتصال و اولین ترافیک را روان‌تر کند، اما مقدار کمی دیتا و باتری مصرف می‌کند.",
                ),
                SettingsGuideEntry(
                    title = "سطح لاگ",
                    body = "میزان اطلاعات تشخیصی StormDNS را کنترل می‌کند: DEBUG، INFO، WARN یا ERROR.",
                    effect = "DEBUG و INFO برای عیب‌یابی بهترند ولی لاگ‌ها را شلوغ می‌کنند. WARN یا ERROR برای استفاده عادی آرام‌ترند.",
                ),
            ),
        ),
    )

    override val menuAppSettings = "تنظیمات برنامه"
    override val menuDonate = "حمایت مالی"
    override val menuVersion = "نسخه"

    override val setupTitle = "راه‌اندازی اتصال"
    override val setupAddConnection = "افزودن پروفایل اتصال"
    override val setupAddResolver = "افزودن پروفایل ریزالور"

    override val selectorConnectionProfiles = "پروفایل‌های اتصال"
    override val selectorResolverProfiles = "پروفایل‌های ریزالور"
    override val selectorSettingProfiles = "پروفایل‌های تنظیمات"
    override val resolverNotSelected = "هیچ ریزالوری انتخاب نشده"
    override val resolverRequired = "برای اتصال به ریزالور نیاز دارید."

    override val logsTitle = "لاگ‌های اتصال"
    override val logsClear = "پاک کردن"
    override val logsCopy = "کپی"

    override val scanBtnStart = "شروع"
    override val scanBtnStop = "توقف"
    override val scanBtnSaveAs = "ذخیره به عنوان"
    override val scanBtnResume = "ادامه"
    override val scanWorkerWarning = "تعداد زیاد ورکر ممکن است عملکرد دستگاه را تحت‌تأثیر قرار دهد."
    override val scanStatusTitle = "وضعیت اسکن"
    override val scanLabelTotal = "کل"
    override val scanLabelValid = "معتبر"
    override val scanLabelRejected = "رد شده"
    override val scanLabelStatus = "وضعیت"
    override val scanLabelSource = "منبع"
    override val scanLabelWorkers = "ورکرها"
    override val scanLabelProgress = "پیشرفت"
    override val scanAutoSave = "ذخیره خودکار نتایج"

    override val serverRouteMissing = "مسیر سرور در دسترس نیست"

    override val supportTitle = "حمایت از WHITEDNS"
    override val supportBody = "WhiteDNS رایگان و متن‌باز است. اگر مفید بود، از توسعه آن حمایت کنید."

    // Scan tab additional
    override val scanDefaultList = "لیست پیش‌فرض"
    override val scanSelectFile = "انتخاب فایل"
    override val scanProfileLabel = "پروفایل اسکن"
    override val scanAutoSaveTitle = "ذخیره خودکار اسکن"
    override val scanSaveAsTitle = "ذخیره نتایج اسکن"
    override val scanSaveAsName = "نام"
    override val scanSaveAsPlaceholder = "تونل سریع"
    override val scanNoFileSelected = "فایلی انتخاب نشده"
    override val scanMessageLabel = "پیام"

    // Setup card
    override val setupResolversLabel = "ریزالورها"
    override val setupAddConnectionSupportingText = "دامنه و کلید سرور"
    override val setupAddResolverSupportingText = "لیست DNS ریزالور"
    override val setupManualResolvers = "ریزالورهای دستی"
    override val setupSectionSetup = "راه‌اندازی"

    // Connection info card
    override val infoCardConnection = "اطلاعات اتصال"
    override val infoLabelMode = "حالت"
    override val infoLabelSocks5Proxy = "پروکسی SOCKS5"
    override val infoLabelHttpProxy = "پروکسی HTTP"
    override val infoLabelAuth = "احراز هویت"
    override val infoLabelUser = "کاربر"
    override val infoLabelPass = "رمز عبور"
    override val infoLabelSplitTunnel = "تونل تقسیم‌شده"
    override val infoLabelApps = "برنامه‌ها"
    override val infoLabelConnectionProfile = "اتصال"
    override val infoLabelResolverProfile = "ریزالور"
    override val infoLabelSettingProfile = "تنظیمات"
    override val infoLabelProtocol = "پروتکل"
    override val infoLabelAuthOn = "فعال"
    override val infoLabelAuthOff = "غیرفعال"

    // Speed indicators
    override val speedDown = "دریافت"
    override val speedUp = "ارسال"
    override val speedTotalUsage = "مصرف کل"

    // Resolver runtime
    override val resolverActiveResolvers = "ریزالورهای فعال"
    override val resolverValidResolvers = "ریزالورهای معتبر"
    override val resolverPending = "در انتظار"
    override val resolverNoResolvers = "برای اتصال به ریزالور نیاز دارید."
    override val backgroundScanningInProgress = "اسکن پس‌زمینه در حال انجام است"

    // Profile dialogs
    override val profileDialogCreateSetting = "ایجاد پروفایل تنظیمات"
    override val profileDialogEditSetting = "ویرایش پروفایل تنظیمات"
    override val profileDialogCreateResolver = "ایجاد پروفایل ریزالور"
    override val profileDialogEditResolver = "ویرایش پروفایل ریزالور"
    override val profileDialogCreateConnection = "ایجاد اتصال جدید"
    override val profileDialogEditConnection = "ویرایش اتصال"
    override val profileDialogImportSettings = "وارد کردن پروفایل تنظیمات"
    override val profileDialogImportConnection = "وارد کردن اتصال"
    override val profileDialogExportConnection = "خروجی اتصال"
    override val profileDialogExportAllConnections = "خروجی همه اتصال‌ها"
    override val profileDialogExportAllResolvers = "خروجی همه ریزالورها"
    override val profileDialogExportSettings = "خروجی تنظیمات"
    override val profileExportResolverTotalTemplate = "مجموع: %s"
    override val profileExportSavingFile = "در حال ذخیره client_resolvers.txt..."
    override val profileExportSavedToTemplate = "ذخیره شد در %s"
    override val profileFieldName = "نام"
    override val profileFieldResolvers = "ریزالورها"
    override val profileFieldProfileLinks = "لینک‌های پروفایل"
    override val profileFieldToml = "TOML"
    override val profileNamePlaceholderFastTunnel = "تونل سریع"
    override val profileNamePlaceholderHomeResolvers = "ریزالورهای خانه"
    override val profileNamePlaceholderImportedSettings = "تنظیمات وارد شده"
    override val profileNamePlaceholderConnection = "اتصال من"
    override val profileResolverPlaceholder = "1.1.1.1, 8.8.8.8"

    // Profile menu actions
    override val profileMenuExport = "خروجی پروفایل"
    override val profileMenuEdit = "ویرایش پروفایل"
    override val profileMenuDelete = "حذف پروفایل"
    override val profileMenuUse = "استفاده از پروفایل"
    override val profileMenuUseSelected = "استفاده از پروفایل (انتخاب شده)"

    // Profile list empty states
    override val profileNoResolverLists = "هنوز لیست ریزالوری ذخیره نشده."
    override val profileNoSettingProfiles = "هنوز پروفایل تنظیماتی ذخیره نشده."
    override val profileQrUnavailable = "کد QR برای این لینک پروفایل در دسترس نیست."

    // Profile action buttons
    override val profileBtnCreate = "ایجاد"
    override val profileBtnImport = "وارد کردن"
    override val profileBtnDeleteDups = "حذف تکراری‌ها"
    override val profileBtnExportAll = "خروجی همه"
    override val profileBtnSaveCurrent = "ذخیره فعلی"
    override val profileBtnImportFile = "وارد کردن فایل"
    override val profileBtnClear = "پاک کردن"

    // Delete confirmation dialogs
    override val deleteConnectionTitle = "حذف اتصال"
    override val deleteResolverTitle = "حذف پروفایل ریزالور"
    override val deleteSettingTitle = "حذف پروفایل تنظیمات"
    override val deleteDupsTitle = "حذف ریزالورهای تکراری"

    // GroupLabels
    override val groupMtu = "MTU"
    override val groupRuntimeWorkers = "ورکرها، صف‌ها و تایمرهای اجرا"
    override val groupLocalProxy = "پروکسی محلی"
    override val groupNetworkTuning = "تنظیم شبکه"
    override val groupReliability = "پایداری"
    override val groupDefault = "پیش‌فرض"
    override val groupCustomSettings = "تنظیمات سفارشی"
    override val groupParallelTestResults = "نتایج تست موازی"

    // Advanced settings field labels
    override val settingListenIp = "IP شنیداری"
    override val settingListenPort = "پورت شنیداری"
    override val settingHttpProxy = "پروکسی HTTP"
    override val settingHttpPort = "پورت HTTP"
    override val settingSocks5Auth = "احراز هویت SOCKS5"
    override val settingUsername = "نام کاربری"
    override val settingPassword = "رمز عبور"
    override val settingBalancingStrategy = "استراتژی توازن"
    override val settingUploadDup = "تکرار آپلود"
    override val settingDownloadDup = "تکرار دانلود"
    override val settingUploadCompress = "فشرده‌سازی آپلود"
    override val settingDownloadCompress = "فشرده‌سازی دانلود"
    override val settingBaseEncodeData = "رمزگذاری پایه داده"
    override val settingPingWatchdog = "نظارت Ping"
    override val settingTrafficWarmup = "گرم‌کردن ترافیک"
    override val settingWarmupProbes = "پروب‌های گرم‌کردن"
    override val settingKeepalive = "Keepalive"
    override val settingLogLevel = "سطح لاگ"
    override val settingSearch = "جستجو"
    override val settingMinUpload = "حداقل آپلود"
    override val settingMinDownload = "حداقل دانلود"
    override val settingMaxUpload = "حداکثر آپلود"
    override val settingMaxDownload = "حداکثر دانلود"
    override val settingResolverRetries = "تلاش مجدد ریزالور"
    override val settingResolverTimeout = "تایم‌اوت ریزالور"
    override val settingResolverParallel = "موازی‌سازی MTU ریزالور"
    override val settingResolverParallelNote = "تست‌های موازی بیشتر می‌تواند اتصال اول را سریع‌تر کند، اما ممکن است به گوشی فشار وارد کند."
    override val settingLogsRetries = "تلاش مجدد لاگ‌ها"
    override val settingLogsTimeout = "تایم‌اوت لاگ‌ها"
    override val settingLogsParallel = "موازی لاگ‌ها"
    override val settingRxTxWorkers = "ورکرهای RX/TX"
    override val settingProcessWorkers = "ورکرهای پردازش"
    override val settingTunnelPacketTimeout = "تایم‌اوت بسته تونل"
    override val settingIdlePoll = "نظرسنجی بیکار"
    override val settingTxChannel = "کانال TX"
    override val settingRxChannel = "کانال RX"
    override val settingUdpPool = "استخر UDP"
    override val settingStreamQueue = "صف استریم"
    override val settingOrphanQueue = "صف یتیم"
    override val settingDnsFragments = "قطعات DNS"
    override val settingSocksUdpTimeout = "تایم‌اوت UDP برای SOCKS"
    override val settingTerminalRetain = "نگهداری پایانه‌ای"
    override val settingCancelledRetain = "نگهداری لغو شده"
    override val settingRetryBase = "پایه تلاش مجدد"
    override val settingRetryStep = "گام تلاش مجدد"
    override val settingRetryLinear = "تلاش مجدد خطی"
    override val settingRetryMax = "حداکثر تلاش مجدد"
    override val settingBusyRetry = "تلاش مجدد مشغول"
    override val settingSettingLabel = "تنظیمات"

    // Split Tunnel
    override val splitTunnelTitle = "تونل تقسیم‌شده"
    override val splitTunnelAppRouting = "مسیریابی برنامه"
    override val splitTunnelSelected = "انتخاب شده"
    override val splitTunnelSelectApps = "انتخاب برنامه‌ها"
    override val splitTunnelNoAppsFound = "برنامه‌ای یافت نشد."
    override val splitTunnelSearchPlaceholder = "جستجوی برنامه‌ها"
    override val splitTunnelDialogTitle = "انتخاب برنامه‌ها"
    override val splitTunnelSearchLabel = "جستجو"

    // Connection logs (inline panel)
    override val logsInlineTitle = "لاگ‌های اتصال"
    override val logsDiagnostics = "تشخیص"

    // Notification permission banner
    override val bannerNotificationBlockedTitle = "اعلان VPN مسدود شده"
    override val bannerNotificationBlockedBody = "WhiteDNS برای نمایش اعلان نیاز به مجوز دارد تا VPN فعال بماند. برای اعطای مجوز ضربه بزنید."
    override val bannerFullVpnWarningTitle = "هشدار عملکرد VPN کامل"
    override val bannerFullVpnWarningBody = "حالت VPN کامل تمام ترافیک دستگاه را از طریق WhiteDNS هدایت می‌کند. این ممکن است در مقایسه با حالت پروکسی تأخیر و مصرف باتری را افزایش دهد."

    // Parallel test UI
    override val parallelTestOpenLabel = "تست موازی ▲"
    override val parallelTestClosedLabel = "تست موازی ▼"
    override val parallelTestDescription = "تنظیمات را برای تست موازی انتخاب کنید. سریع‌ترین تنظیم به‌طور خودکار انتخاب می‌شود."
    override val parallelTestYourConfigs = "تنظیمات شما"

    // Connect tab messages
    override val connectNeedResolvers = "برای اتصال به ریزالور نیاز دارید."
    override val connectSelectedCount = "انتخاب شده"

    // Auto-tune / parallel test results
    override val autoTuneSaveSettingAs = "ذخیره تنظیمات به عنوان"
    override val autoTuneMtuFail = "ناموفق"
    override val autoTuneMtuPass = "موفق"
    override val autoTuneMtuTest = "آزمایش"
    override val autoTuneSpeedLabel = "سرعت"
    override val autoTunePingLabel = "پینگ"
    override val autoTuneStatusStarting = "در حال شروع"
    override val autoTuneMeasuringSpeed = "در حال اندازه‌گیری سرعت"
    override val cdParallelTestSpeed = "سرعت تست موازی"
    override val cdParallelTestPing = "پینگ تست موازی"
    override val cdConnectButtonDisconnected = "دکمه اتصال - ضربه بزنید برای شروع VPN"
    override val cdConnectButtonConnecting = "در حال اتصال - برقراری اتصال VPN"
    override val cdConnectButtonConnected = "دکمه قطع - ضربه بزنید برای قطع VPN"
    override val cdAutoTuneMtuFailed = "MTU ناموفق"
    override val cdAutoTuneMtuPassed = "MTU موفق"
    override val cdAutoTuneMtuTesting = "در حال تست MTU"

    override val shareSubjectProfile = "پروفایل WhiteDNS"
    override val shareChooserProfile = "اشتراک پروفایل WhiteDNS"
    override val shareChooserClientConfig = "اشتراک client_config.toml"
    override val shareChooserAdvancedSettings = "اشتراک advanced_settings.toml"
    override val shareChooserResolvers = "اشتراک client_resolvers.txt"

    override val errorUnableToOpenResolverFile = "باز کردن فایل ریزالور ممکن نیست"
    override val errorInvalidResolverIpTemplate = "IP ریزالور نامعتبر: %s"
    override val errorNoResolverEntries = "هیچ ریزالوری در فایل یافت نشد"
    override val errorEnterValidResolverIp = "حداقل یک IP ریزالور معتبر وارد کنید."
    override val errorEnterProfileNameToSave = "نام پروفایل را برای ذخیره وارد کنید."
    override val resolverValidSingularTemplate = "%d ریزالور معتبر."
    override val resolverValidPluralTemplate = "%d ریزالور معتبر."
    override val advancedProfileModifiedSuffix = "(تغییر یافته)"
    override val cdEditPrefix = "ویرایش"
    override val resolverFieldPlaceholder = "1.1.1.1، 8.8.8.8 یا یکی در هر خط"
    override val dropdownPlaceholderSelect = "انتخاب کنید"
    override val setupDefaultResolver = "ریزالور پیش‌فرض"
    override val setupDefaultConnection = "اتصال"
    override val setupDefaultAdvanced = "پیش‌فرض"

    override val balancingStrategyRandom = "تصادفی"
    override val balancingStrategyRoundRobin = "نوبتی"
    override val balancingStrategyLeastLoss = "کمترین افت"
    override val balancingStrategyLowestLatency = "کمترین تأخیر"
    override val compressionOff = "خاموش"
    override val compressionZstd = "ZSTD"
    override val compressionLz4 = "LZ4"
    override val compressionZlib = "ZLIB"
    override val splitTunnelAllAppsChoice = "همه برنامه‌ها"
    override val splitTunnelOnlySelectedChoice = "فقط انتخاب‌شده‌ها"
    override val splitTunnelBypassSelectedChoice = "نادیده گرفتن انتخاب‌شده‌ها"
    override val encryptionMethodNone = "هیچ‌کدام"
    override val encryptionMethodXor = "XOR"
    override val encryptionMethodChacha20 = "ChaCha20"
    override val encryptionMethodAes128 = "AES-128-GCM"
    override val encryptionMethodAes192 = "AES-192-GCM"
    override val encryptionMethodAes256 = "AES-256-GCM"

    // Download TOML dialog
    override val downloadTomlTitle = "دانلود TOML"
    override val downloadTomlBtn = "دانلود TOML"

    // Save setting as button
    override val saveSettingAs = "ذخیره تنظیمات به عنوان"

    // Validation messages
    override val validationEnterResolverIp = "IP یا دامنه ریزالور را وارد کنید"
    override val validationEnterProfileName = "نام پروفایل را وارد کنید"

    // HomeSelectorCard
    override val homeSelectorSettingLabel = "تنظیمات"
    override val homeSelectorUnsavedChanges = "تغییرات ذخیره نشده"

    override val homeSelectorNoSavedLists = "لیستی ذخیره نشده"
    override val homeSelectorNotSelected = "انتخاب نشده"
    override val homeSelectorResolverProfileFallback = "پروفایل ریزالور"
    override val homeSelectorSearchConnections = "جستجوی اتصال‌ها"
    override val homeSelectorSearchResolvers = "جستجوی ریزالورها"
    override val homeSelectorSearchSettings = "جستجوی پروفایل‌های تنظیمات"
    override val homeSelectorCustomAdvanced = "پیشرفته سفارشی"
    override val profileNameCopySuffix = "کپی"
    override val settingProfileFastTunnelPlaceholder = "تونل سریع"
    override val resolverProfileHomeResolversPlaceholder = "ریزالورهای خانگی"
    override val settingProfileImportedSettingsPlaceholder = "تنظیمات وارد شده"
    override val homeSelectorNoConnectionProfiles = "پروفایل اتصالی یافت نشد."
    override val homeSelectorNoResolverProfiles = "پروفایل ریزالوری یافت نشد."
    override val homeSelectorNoSettingProfiles = "پروفایل تنظیماتی یافت نشد."

    override val setupNoResolversConfigured = "هیچ ریزالوری پیکربندی نشده"
    override val setupInvalidResolverIp = "آی‌پی ریزالور نامعتبر"
    override val setupServerRouteAndKey = "مسیر سرور و کلید موجود نیست"
    override val setupEncryptionKeyMissing = "کلید رمزنگاری موجود نیست"

    override val scanProfileNeedsServer = "به مسیر و کلید سرور نیاز دارد."
    override val scanProfileFallback = "پروفایل اسکن"
    override val scanResultsTitle = "نتایج اسکن"
    override val scanCurrentScan = "اسکن فعلی"
    override val scanFieldWorkers = "ورکرها"
    override val scanSaveBodyTemplate = "ریزالور معتبر به عنوان پروفایل ریزالور جدید ذخیره می‌شود."
    override val scanStatusReady = "آماده"
    override val scanStatusStarting = "در حال شروع"
    override val scanStatusRunning = "در حال اجرا"
    override val scanStatusCompleted = "تکمیل شد"
    override val scanStatusFailed = "ناموفق"
    override val scanStatusStopped = "متوقف شد"
    override val scanStatusIdle = "بیکار"

    override val groupCustomConnections = "اتصال‌های سفارشی"
    override val customConnectionsEmpty = "هنوز اتصال StormDNS سفارشی وجود ندارد."
    override val profileFieldDomain = "دامنه"
    override val profileFieldEncryptionKey = "کلید رمزنگاری"
    override val profileFieldEncryptionMethod = "روش رمزنگاری"
    override val profileDomainPlaceholder = "v.example.com"
    override val profileEncryptionKeyPlaceholder = "کلید ۳۲ کاراکتری"
    override val profileMyStormDnsPlaceholder = "StormDNS من"
    override val profileDomainFallback = "StormDNS سفارشی"
    override val profileStatusActive = "فعال"
    override val profileStatusSelected = "انتخاب شده"
    override val profileStatusModified = "تغییر یافته"
    override fun resolverProfileSummary(count: Int): String = "$count ریزالور"
    override val profileFieldProfileLinkSingle = "لینک پروفایل"
    override val dialogDeleteConfirm = "حذف"
    override val deleteConnectionMessageTemplate = "این پروفایل اتصال حذف شود؟ این عمل قابل بازگشت نیست."
    override val deleteResolverMessageTemplate = "این پروفایل ریزالور حذف شود؟ این عمل قابل بازگشت نیست."
    override val deleteSettingMessageTemplate = "این پروفایل تنظیمات حذف شود؟ این عمل قابل بازگشت نیست."
    override val deleteDupsMessageSingleConnection = "پروفایل اتصال تکراری حذف شود؟ تکراری‌ها بر اساس دامنه سرور و کلید رمزنگاری شناسایی می‌شوند. در صورت امکان پروفایل فعال یا انتخاب‌شده حفظ می‌شود."
    override val deleteDupsMessageManyConnection = "پروفایل‌های اتصال تکراری حذف شوند؟ تکراری‌ها بر اساس دامنه سرور و کلید رمزنگاری شناسایی می‌شوند. در صورت امکان پروفایل فعال یا انتخاب‌شده حفظ می‌شود."

    override val footerPoweredBy = "قدرت گرفته از WhiteDNS"

    override val verificationVerifying = "در حال تأیید"
    override val verificationVerified = "تأیید شد"
    override val verificationNeedsAttention = "نیاز به بررسی"
    override val verificationPending = "در انتظار"
    override val verificationNotRunYet = "تأیید اتصال هنوز اجرا نشده است"
    override val verificationCheckingRoute = "در حال بررسی مسیر تونل"
    override val verificationProxyReachable = "اتصال تأیید شد: تونل پروکسی به اینترنت دسترسی دارد"
    override val verificationVpnReachable = "اتصال تأیید شد: تونل VPN به اینترنت دسترسی دارد"
    override val verificationProxyWarming = "اتصال آماده است: تونل پروکسی فعال است؛ پروب خروجی هنوز در حال آماده‌سازی است"
    override val verificationVpnWarming = "اتصال آماده است: تونل VPN فعال است؛ پروب خروجی هنوز در حال آماده‌سازی است"
    override val verificationModeChanged = "حالت اتصال پیش از پایان تأیید تغییر کرد"
    override val verificationSocksNotReachable = "تأیید اتصال ناموفق بود: شنونده محلی SOCKS در دسترس نیست"
    override val verificationVpnInterfaceInactive = "تأیید اتصال ناموفق بود: رابط VPN فعال نیست"

    override val noResolversPlaceholder = "بدون ریزالور"
    override val whiteDnsResolversLabel = "ریزالورهای WhiteDNS"
    override val whiteDnsConfigsLabel = "تنظیمات WhiteDNS"
    override val whiteDnsConfigsDescription = "محافظه‌کارانه: مصرف اینترنت کمتر، مناسب بیشتر کاربران"
    override val whiteDnsAggressiveConfigsLabel = "تنظیمات پرمصرف"
    override val whiteDnsAggressiveConfigsDescription = "پرمصرف: وقتی مصرف اینترنت برایتان مهم نیست استفاده کنید"
    override val whiteDnsLogsLabel = "لاگ‌های WhiteDNS"
    override val whiteDnsDiagnosticsLabel = "تشخیص WhiteDNS"
    override val parallelTestCollapseDescription = "بستن تنظیمات تست موازی"
    override val parallelTestExpandDescription = "باز کردن تنظیمات تست موازی"

    override val autoTuneStartingTest = "در حال شروع تست"
    override val autoTuneFailedFallback = "ناموفق"
    override val autoTuneMeasuredKeyword = "Measured"

    override val dropdownSelectFallback = "انتخاب"

    override val splitTunnelAllApps = "همه برنامه‌ها"
    override val splitTunnelNoApps = "بدون برنامه"
    override val splitTunnelOnlyPrefix = "فقط"
    override val splitTunnelBypassPrefix = "نادیده گرفتن"

    override val tapToCollapse = "ضربه برای بستن"
    override val tapToConfigure = "ضربه برای پیکربندی"
    override val parallelTestOpen = "باز"
    override val parallelTestClosed = "بسته"

    override val appsSearchPlaceholder = "نام برنامه یا بسته"

    override val profileFieldProfileLinksLabel = "لینک‌های پروفایل"

    override val cdCloseSelector = "بستن انتخابگر"
    override val cdSelected = "انتخاب شده"
    override val cdDismissScannerInfo = "بستن اطلاعات اسکنر"
    override val cdEditField = "ویرایش"
    override val cdDragToReorder = "بکشید تا پروفایل را مرتب کنید"
    override val cdProfileQrCode = "کد QR پروفایل"
    override val cdAppMenu = "منوی برنامه"
    override val cdAppSettings = "تنظیمات برنامه"
    override val cdDonate = "حمایت مالی"
    override val cdDismissBatteryWarning = "بستن هشدار بهینه‌سازی باتری"
    override val cdDismissVpnWarning = "بستن هشدار VPN کامل"

    override val resolverCountTemplate = "ریزالور پیکربندی شده"
    override val resolverCountOneTemplate = "ریزالور پیکربندی شده"

    override val genericConnectionFallback = "اتصال"
    override val genericResolverFallback = "ریزالور"
    override val genericSettingFallback = "تنظیمات"
    override val scanProfileMenuActions = "عملیات پروفایل اسکن"
    override val settingProfileMenuActions = "عملیات پروفایل تنظیمات"
    override val connectionProfileMenuActions = "عملیات پروفایل اتصال"
    override val resolverProfileMenuActions = "عملیات پروفایل ریزالور"
    override val useSettingProfile = "استفاده از پروفایل تنظیمات"
    override val useResolverProfile = "استفاده از پروفایل ریزالور"
    override val exportConnectionProfileAction = "خروجی پروفایل اتصال"
    override val editConnectionProfileAction = "ویرایش پروفایل اتصال"
    override val deleteConnectionProfileAction = "حذف پروفایل اتصال"
    override val deleteConnectionProfileBlockedAction = "پروفایل متصل قابل حذف نیست"
    override val exportSettingProfileAction = "خروجی پروفایل تنظیمات"
    override val editSettingProfileAction = "ویرایش پروفایل تنظیمات"
    override val deleteSettingProfileAction = "حذف پروفایل تنظیمات"
    override val editResolverProfileAction = "ویرایش پروفایل ریزالور"
    override val deleteResolverProfileAction = "حذف پروفایل ریزالور"
    override val brandWhiteDns = "WhiteDNS"

    override val errorImportSettingsFile = "امکان باز کردن فایل تنظیمات وجود ندارد"
    override val errorImportSettings = "امکان وارد کردن تنظیمات وجود ندارد"
    override val errorImportResolver = "امکان وارد کردن فایل ریزالور وجود ندارد"
    override val errorImportProfile = "امکان وارد کردن پروفایل وجود ندارد"
    override val errorExportProfile = "امکان خروجی گرفتن از پروفایل وجود ندارد"

    override val resolverScanResults = "نتایج اسکن"
    override val scanResultsSuffix = "نتایج"
    override val noResolverEntriesError = "هیچ ورودی ریزالوری در فایل یافت نشد"

    override val profileImportSuccess = "پروفایل وارد شد"
    override val profileBtnScanQr = "اسکن QR"
    override val qrScanNoCode = "هیچ پروفایل QR برای WhiteDNS پیدا نشد"
    override val qrScanCancelled = "اسکن QR لغو شد"

}
