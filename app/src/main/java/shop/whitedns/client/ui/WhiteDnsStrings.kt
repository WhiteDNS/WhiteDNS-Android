package shop.whitedns.client.ui

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
    val connectionModeProxy: String
    val connectionModeVpn: String

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

    // Profile tabs
    val profileTabConnection: String
    val profileTabResolver: String
    val profileTabSetting: String

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

    override val connectionModeProxy = "Proxy"
    override val connectionModeVpn = "Full VPN"

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

    override val profileTabConnection = "Connection"
    override val profileTabResolver = "Resolver"
    override val profileTabSetting = "Setting"

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

    override val connectionModeProxy = "پروکسی"
    override val connectionModeVpn = "VPN کامل"

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

    override val profileTabConnection = "اتصال"
    override val profileTabResolver = "ریزالور"
    override val profileTabSetting = "تنظیمات"

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
}
