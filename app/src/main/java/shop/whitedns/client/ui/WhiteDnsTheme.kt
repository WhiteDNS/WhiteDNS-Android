package shop.whitedns.client.ui

import android.app.Activity
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import shop.whitedns.client.R
import shop.whitedns.client.model.WhiteDnsLanguage
import shop.whitedns.client.model.WhiteDnsThemeMode

object WhiteDnsSpacing {
    val xs = 4.dp       // Extra small spacing
    val sm = 8.dp       // Small spacing
    val md = 12.dp      // Medium spacing
    val lg = 16.dp      // Large spacing
    val xl = 20.dp      // Extra large spacing
    val xxl = 24.dp     // Double extra large spacing
    val xxxl = 32.dp    // Triple extra large spacing

    // Component-specific spacing
    val cardPadding = 16.dp
    val sectionSpacing = 24.dp
    val inputSpacing = 10.dp
    val listItemSpacing = 8.dp
    val iconSpacing = 6.dp
}

object WhiteDnsAnimations {
    // Duration constants (in milliseconds)
    const val DURATION_INSTANT = 120
    const val DURATION_FAST = 180
    const val DURATION_NORMAL = 300
    const val DURATION_SLOW = 400
    const val DURATION_VERY_SLOW = 600

    // Easing functions for smooth animations
    val easingStandard = FastOutSlowInEasing
    val easingEmphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val easingDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

    // Reusable animation specs
    fun <T> standardTween(duration: Int = DURATION_NORMAL) =
        tween<T>(durationMillis = duration, easing = easingStandard)

    fun <T> fastFade() =
        tween<T>(durationMillis = DURATION_FAST, easing = easingStandard)

    fun <T> slowTransition() =
        tween<T>(durationMillis = DURATION_SLOW, easing = easingEmphasized)
}

interface WhiteDnsPaletteColors {
    val Background: Color
    val Surface: Color
    val SurfaceAlt: Color
    val DropdownSurface: Color
    val Input: Color
    val Border: Color
    val Divider: Color
    val ControlBorder: Color
    val Accent: Color
    val AccentPressed: Color
    val AccentText: Color
    val AccentDim: Color
    val OnAccent: Color
    val Success: Color
    val Error: Color
    val Warning: Color
    val WarningText: Color
    val Ink: Color
    val Muted: Color
    val Pale: Color
    val SectionTitle: Color
    val FieldLabel: Color
    val Description: Color
    val Placeholder: Color
    val Disabled: Color
    val SurfaceHover: Color
    val AccentSurface: Color
    val SuccessSurface: Color
    val WarningSurface: Color
    val ErrorSurface: Color
}

object WhiteDnsPaletteDark : WhiteDnsPaletteColors {
    // Core backgrounds - deeper, richer blacks with subtle blue undertones
    override val Background = Color(0xFF0A0C10)           // Deeper pure dark
    override val Surface = Color(0xFF13161D)              // Elevated surface with better contrast
    override val SurfaceAlt = Color(0xFF0E1117)           // Subtle variation
    override val DropdownSurface = Color(0xFF1A1E28)      // Clearer dropdown distinction
    override val Input = Color(0xFF0E1117)                // Matches SurfaceAlt for consistency

    // Borders and dividers - improved hierarchy
    override val Border = Color(0xFF1C2028)               // Subtle border
    override val Divider = Color(0xFF22273A)              // More visible dividers
    override val ControlBorder = Color(0xFF2D3448)        // Clearer control borders

    // Brand colors - vibrant and modern
    override val Accent = Color(0xFF7C6FEA)               // Brighter, more vibrant purple
    override val AccentPressed = Color(0xFF6456D6)        // Deeper pressed state
    override val AccentText = Color(0xFF8A7FED)           // Lighter for text
    override val AccentDim = Color(0xFF5547C2)            // Dimmed accent
    override val OnAccent = Color(0xFFFFFFFF)             // Pure white on accent

    // Status colors - more vibrant and clear
    override val Success = Color(0xFF10D98E)              // Brighter, more energetic green
    override val Error = Color(0xFFFF5757)                // Vivid red with better visibility
    override val Warning = Color(0xFFFFC043)              // Warmer, more noticeable amber
    override val WarningText = Color(0xFFFFC043)          // Consistent warning text

    // Text colors - optimized contrast and hierarchy
    override val Ink = Color(0xFFF2F3F7)                  // Brighter white for primary text
    override val Muted = Color(0xFFB8BED6)                // Softer muted text
    override val Pale = Color(0xFF9BA3C4)                 // Subtle pale text
    override val SectionTitle = Color(0xFFD4D7E3)         // Clearer section headers
    override val FieldLabel = Color(0xFFBFC4D8)           // Better field label visibility
    override val Description = Color(0xFF9FA6C0)          // Optimized description text
    override val Placeholder = Color(0xFF7A8299)          // Subtle placeholders
    override val Disabled = Color(0xFF5A6178)             // Clear disabled state

    // Interactive states
    override val SurfaceHover = Color(0xFF181D27)         // Subtle hover effect

    // Surface tints - refined for better visual feedback
    override val AccentSurface = Color(0xFF1A1640)        // Deeper purple tint
    override val SuccessSurface = Color(0xFF0A2D20)       // Richer green tint
    override val WarningSurface = Color(0xFF2A2310)       // Warmer amber tint
    override val ErrorSurface = Color(0xFF331818)         // Deeper red tint
}

object WhiteDnsPaletteLight : WhiteDnsPaletteColors {
    override val Background = Color(0xFFF5F6FA)
    override val Surface = Color(0xFFFFFFFF)
    override val SurfaceAlt = Color(0xFFF8F9FC)
    override val DropdownSurface = Color(0xFFFAFBFD)
    override val Border = Color(0xFFE5E8F0)
    override val Divider = Color(0xFFDCE0EB)
    override val ControlBorder = Color(0xFFD1D6E4)
    override val Accent = Color(0xFF6C5CE7)
    override val AccentPressed = Color(0xFF5A4BD1)
    override val AccentText = Color(0xFF5546C8)
    override val OnAccent = Color(0xFFFFFFFF)
    override val Success = Color(0xFF00B87C)
    override val Error = Color(0xFFE63946)
    override val Warning = Color(0xFFF59E0B)
    override val WarningText = Color(0xFFD97706)
    override val Ink = Color(0xFF0F1419)
    override val Muted = Color(0xFF4B5563)
    override val Pale = Color(0xFF6B7280)
    override val SectionTitle = Color(0xFF374151)
    override val FieldLabel = Color(0xFF4B5563)
    override val Description = Color(0xFF6B7280)
    override val Placeholder = Color(0xFF9CA3AF)
    override val Disabled = Color(0xFFD1D5DB)
    override val Input = Color(0xFFFAFBFC)
    override val AccentDim = Color(0xFF9F93E8)
    override val SurfaceHover = Color(0xFFF3F4F8)
    override val AccentSurface = Color(0xFFF0EDFC)
    override val SuccessSurface = Color(0xFFE6F7F1)
    override val WarningSurface = Color(0xFFFEF3E2)
    override val ErrorSurface = Color(0xFFFEE8E9)
}

private val LocalWhiteDnsPalette = staticCompositionLocalOf<WhiteDnsPaletteColors> { WhiteDnsPaletteDark }

internal val LocalWhiteDnsStrings = staticCompositionLocalOf<WhiteDnsStrings> { EnglishStrings }

object WhiteDnsL10n {
    val tabProfiles: String @Composable get() = LocalWhiteDnsStrings.current.tabProfiles
    val tabConnect: String @Composable get() = LocalWhiteDnsStrings.current.tabConnect
    val tabScan: String @Composable get() = LocalWhiteDnsStrings.current.tabScan
    val tabLogs: String @Composable get() = LocalWhiteDnsStrings.current.tabLogs
    val btnConnect: String @Composable get() = LocalWhiteDnsStrings.current.btnConnect
    val btnConnecting: String @Composable get() = LocalWhiteDnsStrings.current.btnConnecting
    val btnStop: String @Composable get() = LocalWhiteDnsStrings.current.btnStop
    val btnClose: String @Composable get() = LocalWhiteDnsStrings.current.btnClose
    val btnSave: String @Composable get() = LocalWhiteDnsStrings.current.btnSave
    val btnCancel: String @Composable get() = LocalWhiteDnsStrings.current.btnCancel
    val btnCreate: String @Composable get() = LocalWhiteDnsStrings.current.btnCreate
    val btnImport: String @Composable get() = LocalWhiteDnsStrings.current.btnImport
    val btnDelete: String @Composable get() = LocalWhiteDnsStrings.current.btnDelete
    val btnCopy: String @Composable get() = LocalWhiteDnsStrings.current.btnCopy
    val btnShare: String @Composable get() = LocalWhiteDnsStrings.current.btnShare
    val appSettingsTitle: String @Composable get() = LocalWhiteDnsStrings.current.appSettingsTitle
    val fieldTheme: String @Composable get() = LocalWhiteDnsStrings.current.fieldTheme
    val fieldLanguage: String @Composable get() = LocalWhiteDnsStrings.current.fieldLanguage
    val themeModeAuto: String @Composable get() = LocalWhiteDnsStrings.current.themeModeAuto
    val themeModeLight: String @Composable get() = LocalWhiteDnsStrings.current.themeModeLight
    val themeModeDark: String @Composable get() = LocalWhiteDnsStrings.current.themeModeDark
    val fieldMode: String @Composable get() = LocalWhiteDnsStrings.current.fieldMode
    val connectionModeProxy: String @Composable get() = LocalWhiteDnsStrings.current.connectionModeProxy
    val connectionModeVpn: String @Composable get() = LocalWhiteDnsStrings.current.connectionModeVpn
    val sectionConnection: String @Composable get() = LocalWhiteDnsStrings.current.sectionConnection
    val sectionResolver: String @Composable get() = LocalWhiteDnsStrings.current.sectionResolver
    val bannerBatteryTitle: String @Composable get() = LocalWhiteDnsStrings.current.bannerBatteryTitle
    val bannerBatteryBody: String @Composable get() = LocalWhiteDnsStrings.current.bannerBatteryBody
    val bannerAllowBackground: String @Composable get() = LocalWhiteDnsStrings.current.bannerAllowBackground
    val bannerNotificationTitle: String @Composable get() = LocalWhiteDnsStrings.current.bannerNotificationTitle
    val bannerNotificationBody: String @Composable get() = LocalWhiteDnsStrings.current.bannerNotificationBody
    val bannerEnableNotification: String @Composable get() = LocalWhiteDnsStrings.current.bannerEnableNotification
    val bannerVpnWarningTitle: String @Composable get() = LocalWhiteDnsStrings.current.bannerVpnWarningTitle
    val bannerVpnWarningBody: String @Composable get() = LocalWhiteDnsStrings.current.bannerVpnWarningBody
    val parallelTest: String @Composable get() = LocalWhiteDnsStrings.current.parallelTest
    val profileTabConnection: String @Composable get() = LocalWhiteDnsStrings.current.profileTabConnection
    val profileTabResolver: String @Composable get() = LocalWhiteDnsStrings.current.profileTabResolver
    val profileTabSetting: String @Composable get() = LocalWhiteDnsStrings.current.profileTabSetting
    val settingGuideTitle: String @Composable get() = LocalWhiteDnsStrings.current.settingGuideTitle
    val settingGuideIntro: String @Composable get() = LocalWhiteDnsStrings.current.settingGuideIntro
    val settingGuideSource: String @Composable get() = LocalWhiteDnsStrings.current.settingGuideSource
    val settingGuideEffectLabel: String @Composable get() = LocalWhiteDnsStrings.current.settingGuideEffectLabel
    val settingGuideSections: List<SettingsGuideSection> @Composable get() = LocalWhiteDnsStrings.current.settingGuideSections
    val cdSettingGuide: String @Composable get() = LocalWhiteDnsStrings.current.cdSettingGuide
    val menuAppSettings: String @Composable get() = LocalWhiteDnsStrings.current.menuAppSettings
    val menuDonate: String @Composable get() = LocalWhiteDnsStrings.current.menuDonate
    val logsTitle: String @Composable get() = LocalWhiteDnsStrings.current.logsTitle
    val logsClear: String @Composable get() = LocalWhiteDnsStrings.current.logsClear
    val logsCopy: String @Composable get() = LocalWhiteDnsStrings.current.logsCopy
    val scanBtnStart: String @Composable get() = LocalWhiteDnsStrings.current.scanBtnStart
    val scanBtnStop: String @Composable get() = LocalWhiteDnsStrings.current.scanBtnStop
    val scanBtnSaveAs: String @Composable get() = LocalWhiteDnsStrings.current.scanBtnSaveAs
    val scanBtnResume: String @Composable get() = LocalWhiteDnsStrings.current.scanBtnResume
    val scanStatusTitle: String @Composable get() = LocalWhiteDnsStrings.current.scanStatusTitle
    val scanLabelTotal: String @Composable get() = LocalWhiteDnsStrings.current.scanLabelTotal
    val scanLabelValid: String @Composable get() = LocalWhiteDnsStrings.current.scanLabelValid
    val scanLabelRejected: String @Composable get() = LocalWhiteDnsStrings.current.scanLabelRejected
    val scanLabelStatus: String @Composable get() = LocalWhiteDnsStrings.current.scanLabelStatus
    val scanLabelSource: String @Composable get() = LocalWhiteDnsStrings.current.scanLabelSource
    val scanLabelWorkers: String @Composable get() = LocalWhiteDnsStrings.current.scanLabelWorkers
    val scanLabelProgress: String @Composable get() = LocalWhiteDnsStrings.current.scanLabelProgress
    val scanAutoSave: String @Composable get() = LocalWhiteDnsStrings.current.scanAutoSave
    val supportTitle: String @Composable get() = LocalWhiteDnsStrings.current.supportTitle
    val supportBody: String @Composable get() = LocalWhiteDnsStrings.current.supportBody
    val resolverRequired: String @Composable get() = LocalWhiteDnsStrings.current.resolverRequired
    val serverRouteMissing: String @Composable get() = LocalWhiteDnsStrings.current.serverRouteMissing
    val selectorConnectionProfiles: String @Composable get() = LocalWhiteDnsStrings.current.selectorConnectionProfiles
    val selectorResolverProfiles: String @Composable get() = LocalWhiteDnsStrings.current.selectorResolverProfiles
    val selectorSettingProfiles: String @Composable get() = LocalWhiteDnsStrings.current.selectorSettingProfiles
    val languageEn: String @Composable get() = LocalWhiteDnsStrings.current.languageEn
    val languageFa: String @Composable get() = LocalWhiteDnsStrings.current.languageFa
    val serverTestTitle: String @Composable get() = LocalWhiteDnsStrings.current.serverTestTitle
    val serverTestButton: String @Composable get() = LocalWhiteDnsStrings.current.serverTestButton
    val serverTestSingleButton: String @Composable get() = LocalWhiteDnsStrings.current.serverTestSingleButton
    val serverTestRunning: String @Composable get() = LocalWhiteDnsStrings.current.serverTestRunning
    val serverTestIdle: String @Composable get() = LocalWhiteDnsStrings.current.serverTestIdle
    val serverTestReady: String @Composable get() = LocalWhiteDnsStrings.current.serverTestReady
    val serverTestFailed: String @Composable get() = LocalWhiteDnsStrings.current.serverTestFailed
    val serverTestMeasuring: String @Composable get() = LocalWhiteDnsStrings.current.serverTestMeasuring
    val serverTestStarting: String @Composable get() = LocalWhiteDnsStrings.current.serverTestStarting
    val serverTestPending: String @Composable get() = LocalWhiteDnsStrings.current.serverTestPending
    val serverTestProgressTemplate: String @Composable get() = LocalWhiteDnsStrings.current.serverTestProgressTemplate
    val serverTestSummaryTemplate: String @Composable get() = LocalWhiteDnsStrings.current.serverTestSummaryTemplate
    val serverTestNoSavedServers: String @Composable get() = LocalWhiteDnsStrings.current.serverTestNoSavedServers
    val serverTestNoConnectedResolvers: String @Composable get() = LocalWhiteDnsStrings.current.serverTestNoConnectedResolvers
    val serverTestFailedTemplate: String @Composable get() = LocalWhiteDnsStrings.current.serverTestFailedTemplate
    val serverTestConnectionRequired: String @Composable get() = LocalWhiteDnsStrings.current.serverTestConnectionRequired
    val serverTestServiceConnected: String @Composable get() = LocalWhiteDnsStrings.current.serverTestServiceConnected
    val serverTestServiceTesting: String @Composable get() = LocalWhiteDnsStrings.current.serverTestServiceTesting
    val serverTestServiceGood: String @Composable get() = LocalWhiteDnsStrings.current.serverTestServiceGood
    val serverTestServiceFair: String @Composable get() = LocalWhiteDnsStrings.current.serverTestServiceFair
    val serverTestServicePoor: String @Composable get() = LocalWhiteDnsStrings.current.serverTestServicePoor
    val serverTestScoreGood: String @Composable get() = LocalWhiteDnsStrings.current.serverTestScoreGood
    val serverTestScoreFair: String @Composable get() = LocalWhiteDnsStrings.current.serverTestScoreFair
    val serverTestScorePoor: String @Composable get() = LocalWhiteDnsStrings.current.serverTestScorePoor
    val serverTestScoreUnavailable: String @Composable get() = LocalWhiteDnsStrings.current.serverTestScoreUnavailable

    // Scan tab additional
    val scanDefaultList: String @Composable get() = LocalWhiteDnsStrings.current.scanDefaultList
    val scanSelectFile: String @Composable get() = LocalWhiteDnsStrings.current.scanSelectFile
    val scanProfileLabel: String @Composable get() = LocalWhiteDnsStrings.current.scanProfileLabel
    val scanAutoSaveTitle: String @Composable get() = LocalWhiteDnsStrings.current.scanAutoSaveTitle
    val scanSaveAsTitle: String @Composable get() = LocalWhiteDnsStrings.current.scanSaveAsTitle
    val scanSaveAsName: String @Composable get() = LocalWhiteDnsStrings.current.scanSaveAsName
    val scanSaveAsPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.scanSaveAsPlaceholder
    val scanNoFileSelected: String @Composable get() = LocalWhiteDnsStrings.current.scanNoFileSelected
    val scanMessageLabel: String @Composable get() = LocalWhiteDnsStrings.current.scanMessageLabel

    // Setup card
    val setupResolversLabel: String @Composable get() = LocalWhiteDnsStrings.current.setupResolversLabel
    val setupAddConnectionSupportingText: String @Composable get() = LocalWhiteDnsStrings.current.setupAddConnectionSupportingText
    val setupAddResolverSupportingText: String @Composable get() = LocalWhiteDnsStrings.current.setupAddResolverSupportingText
    val setupManualResolvers: String @Composable get() = LocalWhiteDnsStrings.current.setupManualResolvers
    val setupSectionSetup: String @Composable get() = LocalWhiteDnsStrings.current.setupSectionSetup

    // Connection info card
    val infoCardConnection: String @Composable get() = LocalWhiteDnsStrings.current.infoCardConnection
    val infoLabelMode: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelMode
    val infoLabelSocks5Proxy: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelSocks5Proxy
    val infoLabelHttpProxy: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelHttpProxy
    val infoLabelAuth: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelAuth
    val infoLabelUser: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelUser
    val infoLabelPass: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelPass
    val infoLabelSplitTunnel: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelSplitTunnel
    val infoLabelApps: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelApps
    val infoLabelConnectionProfile: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelConnectionProfile
    val infoLabelResolverProfile: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelResolverProfile
    val infoLabelSettingProfile: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelSettingProfile
    val infoLabelProtocol: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelProtocol
    val infoLabelAuthOn: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelAuthOn
    val infoLabelAuthOff: String @Composable get() = LocalWhiteDnsStrings.current.infoLabelAuthOff

    // Speed indicators
    val speedDown: String @Composable get() = LocalWhiteDnsStrings.current.speedDown
    val speedUp: String @Composable get() = LocalWhiteDnsStrings.current.speedUp
    val speedTotalUsage: String @Composable get() = LocalWhiteDnsStrings.current.speedTotalUsage

    // Resolver runtime
    val resolverActiveResolvers: String @Composable get() = LocalWhiteDnsStrings.current.resolverActiveResolvers
    val resolverValidResolvers: String @Composable get() = LocalWhiteDnsStrings.current.resolverValidResolvers
    val resolverPending: String @Composable get() = LocalWhiteDnsStrings.current.resolverPending
    val resolverNoResolvers: String @Composable get() = LocalWhiteDnsStrings.current.resolverNoResolvers
    val backgroundScanningInProgress: String @Composable get() = LocalWhiteDnsStrings.current.backgroundScanningInProgress

    // Profile dialogs
    val profileDialogCreateSetting: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogCreateSetting
    val profileDialogEditSetting: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogEditSetting
    val profileDialogCreateResolver: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogCreateResolver
    val profileDialogEditResolver: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogEditResolver
    val profileDialogCreateConnection: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogCreateConnection
    val profileDialogEditConnection: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogEditConnection
    val profileDialogImportSettings: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogImportSettings
    val profileDialogImportConnection: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogImportConnection
    val profileDialogExportConnection: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogExportConnection
    val profileDialogExportAllConnections: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogExportAllConnections
    val profileDialogExportAllResolvers: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogExportAllResolvers
    val profileDialogExportSettings: String @Composable get() = LocalWhiteDnsStrings.current.profileDialogExportSettings
    val profileExportResolverTotalTemplate: String @Composable get() = LocalWhiteDnsStrings.current.profileExportResolverTotalTemplate
    val profileExportSavingFile: String @Composable get() = LocalWhiteDnsStrings.current.profileExportSavingFile
    val profileExportSavedToTemplate: String @Composable get() = LocalWhiteDnsStrings.current.profileExportSavedToTemplate
    val profileFieldName: String @Composable get() = LocalWhiteDnsStrings.current.profileFieldName
    val profileFieldResolvers: String @Composable get() = LocalWhiteDnsStrings.current.profileFieldResolvers
    val profileFieldProfileLinks: String @Composable get() = LocalWhiteDnsStrings.current.profileFieldProfileLinks
    val profileFieldToml: String @Composable get() = LocalWhiteDnsStrings.current.profileFieldToml
    val profileNamePlaceholderFastTunnel: String @Composable get() = LocalWhiteDnsStrings.current.profileNamePlaceholderFastTunnel
    val profileNamePlaceholderHomeResolvers: String @Composable get() = LocalWhiteDnsStrings.current.profileNamePlaceholderHomeResolvers
    val profileNamePlaceholderImportedSettings: String @Composable get() = LocalWhiteDnsStrings.current.profileNamePlaceholderImportedSettings
    val profileNamePlaceholderConnection: String @Composable get() = LocalWhiteDnsStrings.current.profileNamePlaceholderConnection
    val profileResolverPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.profileResolverPlaceholder

    // Profile menu actions
    val profileMenuExport: String @Composable get() = LocalWhiteDnsStrings.current.profileMenuExport
    val profileMenuEdit: String @Composable get() = LocalWhiteDnsStrings.current.profileMenuEdit
    val profileMenuDelete: String @Composable get() = LocalWhiteDnsStrings.current.profileMenuDelete
    val profileMenuUse: String @Composable get() = LocalWhiteDnsStrings.current.profileMenuUse
    val profileMenuUseSelected: String @Composable get() = LocalWhiteDnsStrings.current.profileMenuUseSelected

    // Profile list empty states
    val profileNoResolverLists: String @Composable get() = LocalWhiteDnsStrings.current.profileNoResolverLists
    val profileNoSettingProfiles: String @Composable get() = LocalWhiteDnsStrings.current.profileNoSettingProfiles
    val profileQrUnavailable: String @Composable get() = LocalWhiteDnsStrings.current.profileQrUnavailable

    // Profile action buttons
    val profileBtnCreate: String @Composable get() = LocalWhiteDnsStrings.current.profileBtnCreate
    val profileBtnImport: String @Composable get() = LocalWhiteDnsStrings.current.profileBtnImport
    val profileBtnDeleteDups: String @Composable get() = LocalWhiteDnsStrings.current.profileBtnDeleteDups
    val profileBtnExportAll: String @Composable get() = LocalWhiteDnsStrings.current.profileBtnExportAll
    val profileBtnSaveCurrent: String @Composable get() = LocalWhiteDnsStrings.current.profileBtnSaveCurrent
    val profileBtnImportFile: String @Composable get() = LocalWhiteDnsStrings.current.profileBtnImportFile
    val profileBtnClear: String @Composable get() = LocalWhiteDnsStrings.current.profileBtnClear

    // Delete confirmation dialogs
    val deleteConnectionTitle: String @Composable get() = LocalWhiteDnsStrings.current.deleteConnectionTitle
    val deleteResolverTitle: String @Composable get() = LocalWhiteDnsStrings.current.deleteResolverTitle
    val deleteSettingTitle: String @Composable get() = LocalWhiteDnsStrings.current.deleteSettingTitle
    val deleteDupsTitle: String @Composable get() = LocalWhiteDnsStrings.current.deleteDupsTitle

    // GroupLabels
    val groupMtu: String @Composable get() = LocalWhiteDnsStrings.current.groupMtu
    val groupRuntimeWorkers: String @Composable get() = LocalWhiteDnsStrings.current.groupRuntimeWorkers
    val groupLocalProxy: String @Composable get() = LocalWhiteDnsStrings.current.groupLocalProxy
    val groupNetworkTuning: String @Composable get() = LocalWhiteDnsStrings.current.groupNetworkTuning
    val groupReliability: String @Composable get() = LocalWhiteDnsStrings.current.groupReliability
    val groupDefault: String @Composable get() = LocalWhiteDnsStrings.current.groupDefault
    val groupCustomSettings: String @Composable get() = LocalWhiteDnsStrings.current.groupCustomSettings
    val groupParallelTestResults: String @Composable get() = LocalWhiteDnsStrings.current.groupParallelTestResults

    // Advanced settings field labels
    val settingListenIp: String @Composable get() = LocalWhiteDnsStrings.current.settingListenIp
    val settingListenPort: String @Composable get() = LocalWhiteDnsStrings.current.settingListenPort
    val settingHttpProxy: String @Composable get() = LocalWhiteDnsStrings.current.settingHttpProxy
    val settingHttpPort: String @Composable get() = LocalWhiteDnsStrings.current.settingHttpPort
    val settingSocks5Auth: String @Composable get() = LocalWhiteDnsStrings.current.settingSocks5Auth
    val settingUsername: String @Composable get() = LocalWhiteDnsStrings.current.settingUsername
    val settingPassword: String @Composable get() = LocalWhiteDnsStrings.current.settingPassword
    val settingBalancingStrategy: String @Composable get() = LocalWhiteDnsStrings.current.settingBalancingStrategy
    val settingUploadDup: String @Composable get() = LocalWhiteDnsStrings.current.settingUploadDup
    val settingDownloadDup: String @Composable get() = LocalWhiteDnsStrings.current.settingDownloadDup
    val settingUploadCompress: String @Composable get() = LocalWhiteDnsStrings.current.settingUploadCompress
    val settingDownloadCompress: String @Composable get() = LocalWhiteDnsStrings.current.settingDownloadCompress
    val settingBaseEncodeData: String @Composable get() = LocalWhiteDnsStrings.current.settingBaseEncodeData
    val settingPingWatchdog: String @Composable get() = LocalWhiteDnsStrings.current.settingPingWatchdog
    val settingTrafficWarmup: String @Composable get() = LocalWhiteDnsStrings.current.settingTrafficWarmup
    val settingWarmupProbes: String @Composable get() = LocalWhiteDnsStrings.current.settingWarmupProbes
    val settingKeepalive: String @Composable get() = LocalWhiteDnsStrings.current.settingKeepalive
    val settingLogLevel: String @Composable get() = LocalWhiteDnsStrings.current.settingLogLevel
    val settingSearch: String @Composable get() = LocalWhiteDnsStrings.current.settingSearch
    val settingMinUpload: String @Composable get() = LocalWhiteDnsStrings.current.settingMinUpload
    val settingMinDownload: String @Composable get() = LocalWhiteDnsStrings.current.settingMinDownload
    val settingMaxUpload: String @Composable get() = LocalWhiteDnsStrings.current.settingMaxUpload
    val settingMaxDownload: String @Composable get() = LocalWhiteDnsStrings.current.settingMaxDownload
    val settingResolverRetries: String @Composable get() = LocalWhiteDnsStrings.current.settingResolverRetries
    val settingResolverTimeout: String @Composable get() = LocalWhiteDnsStrings.current.settingResolverTimeout
    val settingResolverParallel: String @Composable get() = LocalWhiteDnsStrings.current.settingResolverParallel
    val settingResolverParallelNote: String @Composable get() = LocalWhiteDnsStrings.current.settingResolverParallelNote
    val settingLogsRetries: String @Composable get() = LocalWhiteDnsStrings.current.settingLogsRetries
    val settingLogsTimeout: String @Composable get() = LocalWhiteDnsStrings.current.settingLogsTimeout
    val settingLogsParallel: String @Composable get() = LocalWhiteDnsStrings.current.settingLogsParallel
    val settingRxTxWorkers: String @Composable get() = LocalWhiteDnsStrings.current.settingRxTxWorkers
    val settingProcessWorkers: String @Composable get() = LocalWhiteDnsStrings.current.settingProcessWorkers
    val settingTunnelPacketTimeout: String @Composable get() = LocalWhiteDnsStrings.current.settingTunnelPacketTimeout
    val settingIdlePoll: String @Composable get() = LocalWhiteDnsStrings.current.settingIdlePoll
    val settingTxChannel: String @Composable get() = LocalWhiteDnsStrings.current.settingTxChannel
    val settingRxChannel: String @Composable get() = LocalWhiteDnsStrings.current.settingRxChannel
    val settingUdpPool: String @Composable get() = LocalWhiteDnsStrings.current.settingUdpPool
    val settingStreamQueue: String @Composable get() = LocalWhiteDnsStrings.current.settingStreamQueue
    val settingOrphanQueue: String @Composable get() = LocalWhiteDnsStrings.current.settingOrphanQueue
    val settingDnsFragments: String @Composable get() = LocalWhiteDnsStrings.current.settingDnsFragments
    val settingSocksUdpTimeout: String @Composable get() = LocalWhiteDnsStrings.current.settingSocksUdpTimeout
    val settingTerminalRetain: String @Composable get() = LocalWhiteDnsStrings.current.settingTerminalRetain
    val settingCancelledRetain: String @Composable get() = LocalWhiteDnsStrings.current.settingCancelledRetain
    val settingRetryBase: String @Composable get() = LocalWhiteDnsStrings.current.settingRetryBase
    val settingRetryStep: String @Composable get() = LocalWhiteDnsStrings.current.settingRetryStep
    val settingRetryLinear: String @Composable get() = LocalWhiteDnsStrings.current.settingRetryLinear
    val settingRetryMax: String @Composable get() = LocalWhiteDnsStrings.current.settingRetryMax
    val settingBusyRetry: String @Composable get() = LocalWhiteDnsStrings.current.settingBusyRetry
    val settingSettingLabel: String @Composable get() = LocalWhiteDnsStrings.current.settingSettingLabel

    // Split Tunnel
    val splitTunnelTitle: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelTitle
    val splitTunnelAppRouting: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelAppRouting
    val splitTunnelSelected: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelSelected
    val splitTunnelSelectApps: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelSelectApps
    val splitTunnelNoAppsFound: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelNoAppsFound
    val splitTunnelSearchPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelSearchPlaceholder
    val splitTunnelDialogTitle: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelDialogTitle
    val splitTunnelSearchLabel: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelSearchLabel
    val splitTunnelPackageNameLabel: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelPackageNameLabel
    val splitTunnelPackageNamePlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelPackageNamePlaceholder
    val splitTunnelAddPackage: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelAddPackage
    val splitTunnelShowSystemApps: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelShowSystemApps
    val splitTunnelSelectedCountTemplate: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelSelectedCountTemplate

    // Connection logs (inline panel)
    val logsInlineTitle: String @Composable get() = LocalWhiteDnsStrings.current.logsInlineTitle
    val logsDiagnostics: String @Composable get() = LocalWhiteDnsStrings.current.logsDiagnostics

    // Notification / VPN banners
    val bannerNotificationBlockedTitle: String @Composable get() = LocalWhiteDnsStrings.current.bannerNotificationBlockedTitle
    val bannerNotificationBlockedBody: String @Composable get() = LocalWhiteDnsStrings.current.bannerNotificationBlockedBody
    val bannerFullVpnWarningTitle: String @Composable get() = LocalWhiteDnsStrings.current.bannerFullVpnWarningTitle
    val bannerFullVpnWarningBody: String @Composable get() = LocalWhiteDnsStrings.current.bannerFullVpnWarningBody

    // Parallel test UI
    val parallelTestOpenLabel: String @Composable get() = LocalWhiteDnsStrings.current.parallelTestOpenLabel
    val parallelTestClosedLabel: String @Composable get() = LocalWhiteDnsStrings.current.parallelTestClosedLabel
    val parallelTestDescription: String @Composable get() = LocalWhiteDnsStrings.current.parallelTestDescription
    val parallelTestYourConfigs: String @Composable get() = LocalWhiteDnsStrings.current.parallelTestYourConfigs

    // Connect tab messages
    val connectNeedResolvers: String @Composable get() = LocalWhiteDnsStrings.current.connectNeedResolvers
    val connectSelectedCount: String @Composable get() = LocalWhiteDnsStrings.current.connectSelectedCount

    // Auto-tune / parallel test results
    val autoTuneSaveSettingAs: String @Composable get() = LocalWhiteDnsStrings.current.autoTuneSaveSettingAs
    val autoTuneMtuFail: String @Composable get() = LocalWhiteDnsStrings.current.autoTuneMtuFail
    val autoTuneMtuPass: String @Composable get() = LocalWhiteDnsStrings.current.autoTuneMtuPass
    val autoTuneMtuTest: String @Composable get() = LocalWhiteDnsStrings.current.autoTuneMtuTest
    val autoTuneSpeedLabel: String @Composable get() = LocalWhiteDnsStrings.current.autoTuneSpeedLabel
    val autoTunePingLabel: String @Composable get() = LocalWhiteDnsStrings.current.autoTunePingLabel
    val autoTuneStatusStarting: String @Composable get() = LocalWhiteDnsStrings.current.autoTuneStatusStarting
    val autoTuneMeasuringSpeed: String @Composable get() = LocalWhiteDnsStrings.current.autoTuneMeasuringSpeed
    val cdParallelTestSpeed: String @Composable get() = LocalWhiteDnsStrings.current.cdParallelTestSpeed
    val cdParallelTestPing: String @Composable get() = LocalWhiteDnsStrings.current.cdParallelTestPing
    val cdConnectButtonDisconnected: String @Composable get() = LocalWhiteDnsStrings.current.cdConnectButtonDisconnected
    val cdConnectButtonConnecting: String @Composable get() = LocalWhiteDnsStrings.current.cdConnectButtonConnecting
    val cdConnectButtonConnected: String @Composable get() = LocalWhiteDnsStrings.current.cdConnectButtonConnected
    val cdAutoTuneMtuFailed: String @Composable get() = LocalWhiteDnsStrings.current.cdAutoTuneMtuFailed
    val cdAutoTuneMtuPassed: String @Composable get() = LocalWhiteDnsStrings.current.cdAutoTuneMtuPassed
    val cdAutoTuneMtuTesting: String @Composable get() = LocalWhiteDnsStrings.current.cdAutoTuneMtuTesting

    val shareSubjectProfile: String @Composable get() = LocalWhiteDnsStrings.current.shareSubjectProfile
    val shareChooserProfile: String @Composable get() = LocalWhiteDnsStrings.current.shareChooserProfile
    val shareChooserClientConfig: String @Composable get() = LocalWhiteDnsStrings.current.shareChooserClientConfig
    val shareChooserAdvancedSettings: String @Composable get() = LocalWhiteDnsStrings.current.shareChooserAdvancedSettings
    val shareChooserResolvers: String @Composable get() = LocalWhiteDnsStrings.current.shareChooserResolvers

    val errorUnableToOpenResolverFile: String @Composable get() = LocalWhiteDnsStrings.current.errorUnableToOpenResolverFile
    val errorInvalidResolverIpTemplate: String @Composable get() = LocalWhiteDnsStrings.current.errorInvalidResolverIpTemplate
    val errorNoResolverEntries: String @Composable get() = LocalWhiteDnsStrings.current.errorNoResolverEntries
    val errorEnterValidResolverIp: String @Composable get() = LocalWhiteDnsStrings.current.errorEnterValidResolverIp
    val errorEnterProfileNameToSave: String @Composable get() = LocalWhiteDnsStrings.current.errorEnterProfileNameToSave
    val resolverValidSingularTemplate: String @Composable get() = LocalWhiteDnsStrings.current.resolverValidSingularTemplate
    val resolverValidPluralTemplate: String @Composable get() = LocalWhiteDnsStrings.current.resolverValidPluralTemplate
    val advancedProfileModifiedSuffix: String @Composable get() = LocalWhiteDnsStrings.current.advancedProfileModifiedSuffix
    val cdEditPrefix: String @Composable get() = LocalWhiteDnsStrings.current.cdEditPrefix
    val resolverFieldPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.resolverFieldPlaceholder
    val dropdownPlaceholderSelect: String @Composable get() = LocalWhiteDnsStrings.current.dropdownPlaceholderSelect
    val setupDefaultResolver: String @Composable get() = LocalWhiteDnsStrings.current.setupDefaultResolver
    val setupDefaultConnection: String @Composable get() = LocalWhiteDnsStrings.current.setupDefaultConnection
    val setupDefaultAdvanced: String @Composable get() = LocalWhiteDnsStrings.current.setupDefaultAdvanced

    val balancingStrategyRandom: String @Composable get() = LocalWhiteDnsStrings.current.balancingStrategyRandom
    val balancingStrategyRoundRobin: String @Composable get() = LocalWhiteDnsStrings.current.balancingStrategyRoundRobin
    val balancingStrategyLeastLoss: String @Composable get() = LocalWhiteDnsStrings.current.balancingStrategyLeastLoss
    val balancingStrategyLowestLatency: String @Composable get() = LocalWhiteDnsStrings.current.balancingStrategyLowestLatency
    val compressionOff: String @Composable get() = LocalWhiteDnsStrings.current.compressionOff
    val compressionZstd: String @Composable get() = LocalWhiteDnsStrings.current.compressionZstd
    val compressionLz4: String @Composable get() = LocalWhiteDnsStrings.current.compressionLz4
    val compressionZlib: String @Composable get() = LocalWhiteDnsStrings.current.compressionZlib
    val splitTunnelAllAppsChoice: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelAllAppsChoice
    val splitTunnelOnlySelectedChoice: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelOnlySelectedChoice
    val splitTunnelBypassSelectedChoice: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelBypassSelectedChoice
    val encryptionMethodNone: String @Composable get() = LocalWhiteDnsStrings.current.encryptionMethodNone
    val encryptionMethodXor: String @Composable get() = LocalWhiteDnsStrings.current.encryptionMethodXor
    val encryptionMethodChacha20: String @Composable get() = LocalWhiteDnsStrings.current.encryptionMethodChacha20
    val encryptionMethodAes128: String @Composable get() = LocalWhiteDnsStrings.current.encryptionMethodAes128
    val encryptionMethodAes192: String @Composable get() = LocalWhiteDnsStrings.current.encryptionMethodAes192
    val encryptionMethodAes256: String @Composable get() = LocalWhiteDnsStrings.current.encryptionMethodAes256

    // Download TOML dialog
    val downloadTomlTitle: String @Composable get() = LocalWhiteDnsStrings.current.downloadTomlTitle
    val downloadTomlBtn: String @Composable get() = LocalWhiteDnsStrings.current.downloadTomlBtn

    // Save setting as button
    val saveSettingAs: String @Composable get() = LocalWhiteDnsStrings.current.saveSettingAs

    // Validation messages
    val validationEnterResolverIp: String @Composable get() = LocalWhiteDnsStrings.current.validationEnterResolverIp
    val validationEnterProfileName: String @Composable get() = LocalWhiteDnsStrings.current.validationEnterProfileName

    // HomeSelectorCard
    val homeSelectorSettingLabel: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorSettingLabel
    val homeSelectorUnsavedChanges: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorUnsavedChanges

    // Existing (menu items kept for completeness)
    val menuVersion: String @Composable get() = LocalWhiteDnsStrings.current.menuVersion
    val setupTitle: String @Composable get() = LocalWhiteDnsStrings.current.setupTitle
    val setupAddConnection: String @Composable get() = LocalWhiteDnsStrings.current.setupAddConnection
    val setupAddResolver: String @Composable get() = LocalWhiteDnsStrings.current.setupAddResolver
    val resolverNotSelected: String @Composable get() = LocalWhiteDnsStrings.current.resolverNotSelected
    val connectProgressConnected: String @Composable get() = LocalWhiteDnsStrings.current.connectProgressConnected
    val scanWorkerWarning: String @Composable get() = LocalWhiteDnsStrings.current.scanWorkerWarning

    // Newly added getters for remaining English strings translation
    val homeSelectorNoSavedLists: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorNoSavedLists
    val homeSelectorNotSelected: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorNotSelected
    val homeSelectorResolverProfileFallback: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorResolverProfileFallback
    val homeSelectorSearchConnections: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorSearchConnections
    val homeSelectorSearchResolvers: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorSearchResolvers
    val homeSelectorSearchSettings: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorSearchSettings
    val homeSelectorCustomAdvanced: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorCustomAdvanced
    val profileNameCopySuffix: String @Composable get() = LocalWhiteDnsStrings.current.profileNameCopySuffix
    val settingProfileFastTunnelPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.settingProfileFastTunnelPlaceholder
    val resolverProfileHomeResolversPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.resolverProfileHomeResolversPlaceholder
    val settingProfileImportedSettingsPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.settingProfileImportedSettingsPlaceholder
    val homeSelectorNoConnectionProfiles: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorNoConnectionProfiles
    val homeSelectorNoResolverProfiles: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorNoResolverProfiles
    val homeSelectorNoSettingProfiles: String @Composable get() = LocalWhiteDnsStrings.current.homeSelectorNoSettingProfiles

    val setupNoResolversConfigured: String @Composable get() = LocalWhiteDnsStrings.current.setupNoResolversConfigured
    val setupInvalidResolverIp: String @Composable get() = LocalWhiteDnsStrings.current.setupInvalidResolverIp
    val setupServerRouteAndKey: String @Composable get() = LocalWhiteDnsStrings.current.setupServerRouteAndKey
    val setupEncryptionKeyMissing: String @Composable get() = LocalWhiteDnsStrings.current.setupEncryptionKeyMissing

    val scanProfileNeedsServer: String @Composable get() = LocalWhiteDnsStrings.current.scanProfileNeedsServer
    val scanProfileFallback: String @Composable get() = LocalWhiteDnsStrings.current.scanProfileFallback
    val scanResultsTitle: String @Composable get() = LocalWhiteDnsStrings.current.scanResultsTitle
    val scanCurrentScan: String @Composable get() = LocalWhiteDnsStrings.current.scanCurrentScan
    val scanFieldWorkers: String @Composable get() = LocalWhiteDnsStrings.current.scanFieldWorkers
    val scanSaveBodyTemplate: String @Composable get() = LocalWhiteDnsStrings.current.scanSaveBodyTemplate
    val scanStatusReady: String @Composable get() = LocalWhiteDnsStrings.current.scanStatusReady
    val scanStatusStarting: String @Composable get() = LocalWhiteDnsStrings.current.scanStatusStarting
    val scanStatusRunning: String @Composable get() = LocalWhiteDnsStrings.current.scanStatusRunning
    val scanStatusCompleted: String @Composable get() = LocalWhiteDnsStrings.current.scanStatusCompleted
    val scanStatusFailed: String @Composable get() = LocalWhiteDnsStrings.current.scanStatusFailed
    val scanStatusStopped: String @Composable get() = LocalWhiteDnsStrings.current.scanStatusStopped
    val scanStatusIdle: String @Composable get() = LocalWhiteDnsStrings.current.scanStatusIdle

    val groupCustomConnections: String @Composable get() = LocalWhiteDnsStrings.current.groupCustomConnections
    val customConnectionsEmpty: String @Composable get() = LocalWhiteDnsStrings.current.customConnectionsEmpty
    val profileFieldDomain: String @Composable get() = LocalWhiteDnsStrings.current.profileFieldDomain
    val profileFieldEncryptionKey: String @Composable get() = LocalWhiteDnsStrings.current.profileFieldEncryptionKey
    val profileFieldEncryptionMethod: String @Composable get() = LocalWhiteDnsStrings.current.profileFieldEncryptionMethod
    val profileDomainPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.profileDomainPlaceholder
    val profileEncryptionKeyPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.profileEncryptionKeyPlaceholder
    val profileMyStormDnsPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.profileMyStormDnsPlaceholder
    val profileDomainFallback: String @Composable get() = LocalWhiteDnsStrings.current.profileDomainFallback
    val profileStatusActive: String @Composable get() = LocalWhiteDnsStrings.current.profileStatusActive
    val profileStatusSelected: String @Composable get() = LocalWhiteDnsStrings.current.profileStatusSelected
    val profileStatusModified: String @Composable get() = LocalWhiteDnsStrings.current.profileStatusModified
    @Composable fun resolverProfileSummary(count: Int): String = LocalWhiteDnsStrings.current.resolverProfileSummary(count)
    val profileFieldProfileLinkSingle: String @Composable get() = LocalWhiteDnsStrings.current.profileFieldProfileLinkSingle
    val dialogDeleteConfirm: String @Composable get() = LocalWhiteDnsStrings.current.dialogDeleteConfirm
    val deleteConnectionMessageTemplate: String @Composable get() = LocalWhiteDnsStrings.current.deleteConnectionMessageTemplate
    val deleteResolverMessageTemplate: String @Composable get() = LocalWhiteDnsStrings.current.deleteResolverMessageTemplate
    val deleteSettingMessageTemplate: String @Composable get() = LocalWhiteDnsStrings.current.deleteSettingMessageTemplate
    val deleteDupsMessageSingleConnection: String @Composable get() = LocalWhiteDnsStrings.current.deleteDupsMessageSingleConnection
    val deleteDupsMessageManyConnection: String @Composable get() = LocalWhiteDnsStrings.current.deleteDupsMessageManyConnection

    val footerPoweredBy: String @Composable get() = LocalWhiteDnsStrings.current.footerPoweredBy

    val verificationVerifying: String @Composable get() = LocalWhiteDnsStrings.current.verificationVerifying
    val verificationVerified: String @Composable get() = LocalWhiteDnsStrings.current.verificationVerified
    val verificationNeedsAttention: String @Composable get() = LocalWhiteDnsStrings.current.verificationNeedsAttention
    val verificationPending: String @Composable get() = LocalWhiteDnsStrings.current.verificationPending
    val verificationNotRunYet: String @Composable get() = LocalWhiteDnsStrings.current.verificationNotRunYet
    val verificationCheckingRoute: String @Composable get() = LocalWhiteDnsStrings.current.verificationCheckingRoute
    val verificationProxyReachable: String @Composable get() = LocalWhiteDnsStrings.current.verificationProxyReachable
    val verificationVpnReachable: String @Composable get() = LocalWhiteDnsStrings.current.verificationVpnReachable
    val verificationProxyWarming: String @Composable get() = LocalWhiteDnsStrings.current.verificationProxyWarming
    val verificationVpnWarming: String @Composable get() = LocalWhiteDnsStrings.current.verificationVpnWarming
    val verificationModeChanged: String @Composable get() = LocalWhiteDnsStrings.current.verificationModeChanged
    val verificationSocksNotReachable: String @Composable get() = LocalWhiteDnsStrings.current.verificationSocksNotReachable
    val verificationVpnInterfaceInactive: String @Composable get() = LocalWhiteDnsStrings.current.verificationVpnInterfaceInactive

    val noResolversPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.noResolversPlaceholder
    val whiteDnsResolversLabel: String @Composable get() = LocalWhiteDnsStrings.current.whiteDnsResolversLabel
    val whiteDnsConfigsLabel: String @Composable get() = LocalWhiteDnsStrings.current.whiteDnsConfigsLabel
    val whiteDnsConfigsDescription: String @Composable get() = LocalWhiteDnsStrings.current.whiteDnsConfigsDescription
    val whiteDnsAggressiveConfigsLabel: String @Composable get() = LocalWhiteDnsStrings.current.whiteDnsAggressiveConfigsLabel
    val whiteDnsAggressiveConfigsDescription: String @Composable get() = LocalWhiteDnsStrings.current.whiteDnsAggressiveConfigsDescription
    val whiteDnsLogsLabel: String @Composable get() = LocalWhiteDnsStrings.current.whiteDnsLogsLabel
    val whiteDnsDiagnosticsLabel: String @Composable get() = LocalWhiteDnsStrings.current.whiteDnsDiagnosticsLabel
    val parallelTestCollapseDescription: String @Composable get() = LocalWhiteDnsStrings.current.parallelTestCollapseDescription
    val parallelTestExpandDescription: String @Composable get() = LocalWhiteDnsStrings.current.parallelTestExpandDescription

    val autoTuneStartingTest: String @Composable get() = LocalWhiteDnsStrings.current.autoTuneStartingTest
    val autoTuneFailedFallback: String @Composable get() = LocalWhiteDnsStrings.current.autoTuneFailedFallback
    val autoTuneMeasuredKeyword: String @Composable get() = LocalWhiteDnsStrings.current.autoTuneMeasuredKeyword

    val dropdownSelectFallback: String @Composable get() = LocalWhiteDnsStrings.current.dropdownSelectFallback

    val splitTunnelAllApps: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelAllApps
    val splitTunnelNoApps: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelNoApps
    val splitTunnelOnlyPrefix: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelOnlyPrefix
    val splitTunnelBypassPrefix: String @Composable get() = LocalWhiteDnsStrings.current.splitTunnelBypassPrefix

    val tapToCollapse: String @Composable get() = LocalWhiteDnsStrings.current.tapToCollapse
    val tapToConfigure: String @Composable get() = LocalWhiteDnsStrings.current.tapToConfigure
    val parallelTestOpen: String @Composable get() = LocalWhiteDnsStrings.current.parallelTestOpen
    val parallelTestClosed: String @Composable get() = LocalWhiteDnsStrings.current.parallelTestClosed

    val appsSearchPlaceholder: String @Composable get() = LocalWhiteDnsStrings.current.appsSearchPlaceholder

    val profileFieldProfileLinksLabel: String @Composable get() = LocalWhiteDnsStrings.current.profileFieldProfileLinksLabel

    val cdCloseSelector: String @Composable get() = LocalWhiteDnsStrings.current.cdCloseSelector
    val cdSelected: String @Composable get() = LocalWhiteDnsStrings.current.cdSelected
    val cdDismissScannerInfo: String @Composable get() = LocalWhiteDnsStrings.current.cdDismissScannerInfo
    val cdEditField: String @Composable get() = LocalWhiteDnsStrings.current.cdEditField
    val cdDragToReorder: String @Composable get() = LocalWhiteDnsStrings.current.cdDragToReorder
    val cdProfileQrCode: String @Composable get() = LocalWhiteDnsStrings.current.cdProfileQrCode
    val cdAppMenu: String @Composable get() = LocalWhiteDnsStrings.current.cdAppMenu
    val cdAppSettings: String @Composable get() = LocalWhiteDnsStrings.current.cdAppSettings
    val cdDonate: String @Composable get() = LocalWhiteDnsStrings.current.cdDonate
    val cdDismissBatteryWarning: String @Composable get() = LocalWhiteDnsStrings.current.cdDismissBatteryWarning
    val cdDismissVpnWarning: String @Composable get() = LocalWhiteDnsStrings.current.cdDismissVpnWarning

    val resolverCountTemplate: String @Composable get() = LocalWhiteDnsStrings.current.resolverCountTemplate
    val resolverCountOneTemplate: String @Composable get() = LocalWhiteDnsStrings.current.resolverCountOneTemplate

    val genericConnectionFallback: String @Composable get() = LocalWhiteDnsStrings.current.genericConnectionFallback
    val genericResolverFallback: String @Composable get() = LocalWhiteDnsStrings.current.genericResolverFallback
    val genericSettingFallback: String @Composable get() = LocalWhiteDnsStrings.current.genericSettingFallback
    val scanProfileMenuActions: String @Composable get() = LocalWhiteDnsStrings.current.scanProfileMenuActions
    val settingProfileMenuActions: String @Composable get() = LocalWhiteDnsStrings.current.settingProfileMenuActions
    val connectionProfileMenuActions: String @Composable get() = LocalWhiteDnsStrings.current.connectionProfileMenuActions
    val resolverProfileMenuActions: String @Composable get() = LocalWhiteDnsStrings.current.resolverProfileMenuActions
    val useSettingProfile: String @Composable get() = LocalWhiteDnsStrings.current.useSettingProfile
    val useResolverProfile: String @Composable get() = LocalWhiteDnsStrings.current.useResolverProfile
    val exportConnectionProfileAction: String @Composable get() = LocalWhiteDnsStrings.current.exportConnectionProfileAction
    val editConnectionProfileAction: String @Composable get() = LocalWhiteDnsStrings.current.editConnectionProfileAction
    val deleteConnectionProfileAction: String @Composable get() = LocalWhiteDnsStrings.current.deleteConnectionProfileAction
    val deleteConnectionProfileBlockedAction: String @Composable get() = LocalWhiteDnsStrings.current.deleteConnectionProfileBlockedAction
    val exportSettingProfileAction: String @Composable get() = LocalWhiteDnsStrings.current.exportSettingProfileAction
    val editSettingProfileAction: String @Composable get() = LocalWhiteDnsStrings.current.editSettingProfileAction
    val deleteSettingProfileAction: String @Composable get() = LocalWhiteDnsStrings.current.deleteSettingProfileAction
    val editResolverProfileAction: String @Composable get() = LocalWhiteDnsStrings.current.editResolverProfileAction
    val deleteResolverProfileAction: String @Composable get() = LocalWhiteDnsStrings.current.deleteResolverProfileAction
    val brandWhiteDns: String @Composable get() = LocalWhiteDnsStrings.current.brandWhiteDns

    val errorImportSettingsFile: String @Composable get() = LocalWhiteDnsStrings.current.errorImportSettingsFile
    val errorImportSettings: String @Composable get() = LocalWhiteDnsStrings.current.errorImportSettings
    val errorImportResolver: String @Composable get() = LocalWhiteDnsStrings.current.errorImportResolver
    val errorImportProfile: String @Composable get() = LocalWhiteDnsStrings.current.errorImportProfile
    val errorExportProfile: String @Composable get() = LocalWhiteDnsStrings.current.errorExportProfile

    val resolverScanResults: String @Composable get() = LocalWhiteDnsStrings.current.resolverScanResults
    val scanResultsSuffix: String @Composable get() = LocalWhiteDnsStrings.current.scanResultsSuffix
    val noResolverEntriesError: String @Composable get() = LocalWhiteDnsStrings.current.noResolverEntriesError

    val profileImportSuccess: String @Composable get() = LocalWhiteDnsStrings.current.profileImportSuccess
    val profileBtnScanQr: String @Composable get() = LocalWhiteDnsStrings.current.profileBtnScanQr
    val qrScanNoCode: String @Composable get() = LocalWhiteDnsStrings.current.qrScanNoCode
    val qrScanCancelled: String @Composable get() = LocalWhiteDnsStrings.current.qrScanCancelled

}

object WhiteDnsPalette {
    val Background: Color
        @Composable get() = LocalWhiteDnsPalette.current.Background
    val Surface: Color
        @Composable get() = LocalWhiteDnsPalette.current.Surface
    val SurfaceAlt: Color
        @Composable get() = LocalWhiteDnsPalette.current.SurfaceAlt
    val DropdownSurface: Color
        @Composable get() = LocalWhiteDnsPalette.current.DropdownSurface
    val Border: Color
        @Composable get() = LocalWhiteDnsPalette.current.Border
    val Divider: Color
        @Composable get() = LocalWhiteDnsPalette.current.Divider
    val ControlBorder: Color
        @Composable get() = LocalWhiteDnsPalette.current.ControlBorder
    val Accent: Color
        @Composable get() = LocalWhiteDnsPalette.current.Accent
    val AccentPressed: Color
        @Composable get() = LocalWhiteDnsPalette.current.AccentPressed
    val AccentText: Color
        @Composable get() = LocalWhiteDnsPalette.current.AccentText
    val OnAccent: Color
        @Composable get() = LocalWhiteDnsPalette.current.OnAccent
    val Success: Color
        @Composable get() = LocalWhiteDnsPalette.current.Success
    val Error: Color
        @Composable get() = LocalWhiteDnsPalette.current.Error
    val Warning: Color
        @Composable get() = LocalWhiteDnsPalette.current.Warning
    val WarningText: Color
        @Composable get() = LocalWhiteDnsPalette.current.WarningText
    val Ink: Color
        @Composable get() = LocalWhiteDnsPalette.current.Ink
    val Muted: Color
        @Composable get() = LocalWhiteDnsPalette.current.Muted
    val Pale: Color
        @Composable get() = LocalWhiteDnsPalette.current.Pale
    val SectionTitle: Color
        @Composable get() = LocalWhiteDnsPalette.current.SectionTitle
    val FieldLabel: Color
        @Composable get() = LocalWhiteDnsPalette.current.FieldLabel
    val Description: Color
        @Composable get() = LocalWhiteDnsPalette.current.Description
    val Placeholder: Color
        @Composable get() = LocalWhiteDnsPalette.current.Placeholder
    val Disabled: Color
        @Composable get() = LocalWhiteDnsPalette.current.Disabled
    val Input: Color
        @Composable get() = LocalWhiteDnsPalette.current.Input
    val AccentDim: Color
        @Composable get() = LocalWhiteDnsPalette.current.AccentDim
    val SurfaceHover: Color
        @Composable get() = LocalWhiteDnsPalette.current.SurfaceHover
    val AccentSurface: Color
        @Composable get() = LocalWhiteDnsPalette.current.AccentSurface
    val SuccessSurface: Color
        @Composable get() = LocalWhiteDnsPalette.current.SuccessSurface
    val WarningSurface: Color
        @Composable get() = LocalWhiteDnsPalette.current.WarningSurface
    val ErrorSurface: Color
        @Composable get() = LocalWhiteDnsPalette.current.ErrorSurface
}

@Composable
fun currentPalette(themeMode: String = WhiteDnsThemeMode.System): WhiteDnsPaletteColors {
    return if (shouldUseDarkTheme(themeMode)) WhiteDnsPaletteDark else WhiteDnsPaletteLight
}

private val WhiteDnsColorSchemeDark = darkColorScheme(
    primary = WhiteDnsPaletteDark.Accent,
    onPrimary = WhiteDnsPaletteDark.OnAccent,
    primaryContainer = WhiteDnsPaletteDark.AccentPressed,
    onPrimaryContainer = WhiteDnsPaletteDark.OnAccent,
    secondary = WhiteDnsPaletteDark.Pale,
    onSecondary = WhiteDnsPaletteDark.Background,
    secondaryContainer = WhiteDnsPaletteDark.DropdownSurface,
    onSecondaryContainer = WhiteDnsPaletteDark.Ink,
    tertiary = WhiteDnsPaletteDark.Success,
    onTertiary = WhiteDnsPaletteDark.Background,
    tertiaryContainer = WhiteDnsPaletteDark.SuccessSurface,
    onTertiaryContainer = WhiteDnsPaletteDark.Success,
    background = WhiteDnsPaletteDark.Background,
    onBackground = WhiteDnsPaletteDark.Ink,
    surface = WhiteDnsPaletteDark.Surface,
    onSurface = WhiteDnsPaletteDark.Ink,
    surfaceVariant = WhiteDnsPaletteDark.SurfaceAlt,
    onSurfaceVariant = WhiteDnsPaletteDark.Muted,
    surfaceTint = WhiteDnsPaletteDark.Accent,
    outline = WhiteDnsPaletteDark.ControlBorder,
    outlineVariant = WhiteDnsPaletteDark.Border,
    inverseSurface = WhiteDnsPaletteDark.Ink,
    inverseOnSurface = WhiteDnsPaletteDark.Background,
    inversePrimary = WhiteDnsPaletteDark.AccentPressed,
    error = WhiteDnsPaletteDark.Error,
    onError = WhiteDnsPaletteDark.OnAccent,
    errorContainer = WhiteDnsPaletteDark.ErrorSurface,
    onErrorContainer = WhiteDnsPaletteDark.Error,
    scrim = Color(0xFF000000),
    surfaceBright = WhiteDnsPaletteDark.Divider,
    surfaceDim = WhiteDnsPaletteDark.Background,
    surfaceContainerLowest = WhiteDnsPaletteDark.Background,
    surfaceContainerLow = WhiteDnsPaletteDark.SurfaceAlt,
    surfaceContainer = WhiteDnsPaletteDark.Surface,
    surfaceContainerHigh = WhiteDnsPaletteDark.DropdownSurface,
    surfaceContainerHighest = WhiteDnsPaletteDark.Divider,
    primaryFixed = WhiteDnsPaletteDark.AccentSurface,
    primaryFixedDim = WhiteDnsPaletteDark.AccentSurface,
    onPrimaryFixed = WhiteDnsPaletteDark.Ink,
    onPrimaryFixedVariant = WhiteDnsPaletteDark.Muted,
    secondaryFixed = WhiteDnsPaletteDark.DropdownSurface,
    secondaryFixedDim = WhiteDnsPaletteDark.DropdownSurface,
    onSecondaryFixed = WhiteDnsPaletteDark.Ink,
    onSecondaryFixedVariant = WhiteDnsPaletteDark.Muted,
    tertiaryFixed = WhiteDnsPaletteDark.SuccessSurface,
    tertiaryFixedDim = WhiteDnsPaletteDark.SuccessSurface,
    onTertiaryFixed = WhiteDnsPaletteDark.Ink,
    onTertiaryFixedVariant = WhiteDnsPaletteDark.Success,
)

private val WhiteDnsColorSchemeLight = lightColorScheme(
    primary = WhiteDnsPaletteLight.Accent,
    onPrimary = WhiteDnsPaletteLight.OnAccent,
    primaryContainer = WhiteDnsPaletteLight.AccentSurface,
    onPrimaryContainer = WhiteDnsPaletteLight.AccentText,
    secondary = WhiteDnsPaletteLight.Pale,
    onSecondary = WhiteDnsPaletteLight.Surface,
    secondaryContainer = WhiteDnsPaletteLight.SurfaceAlt,
    onSecondaryContainer = WhiteDnsPaletteLight.Ink,
    tertiary = WhiteDnsPaletteLight.Success,
    onTertiary = WhiteDnsPaletteLight.OnAccent,
    tertiaryContainer = WhiteDnsPaletteLight.SuccessSurface,
    onTertiaryContainer = WhiteDnsPaletteLight.Success,
    background = WhiteDnsPaletteLight.Background,
    onBackground = WhiteDnsPaletteLight.Ink,
    surface = WhiteDnsPaletteLight.Surface,
    onSurface = WhiteDnsPaletteLight.Ink,
    surfaceVariant = WhiteDnsPaletteLight.SurfaceAlt,
    onSurfaceVariant = WhiteDnsPaletteLight.Muted,
    surfaceTint = WhiteDnsPaletteLight.Accent,
    outline = WhiteDnsPaletteLight.ControlBorder,
    outlineVariant = WhiteDnsPaletteLight.Border,
    inverseSurface = WhiteDnsPaletteLight.Ink,
    inverseOnSurface = WhiteDnsPaletteLight.Background,
    inversePrimary = WhiteDnsPaletteLight.AccentPressed,
    error = WhiteDnsPaletteLight.Error,
    onError = WhiteDnsPaletteLight.OnAccent,
    errorContainer = WhiteDnsPaletteLight.ErrorSurface,
    onErrorContainer = WhiteDnsPaletteLight.Error,
    scrim = Color(0x77000000),
    surfaceBright = WhiteDnsPaletteLight.Surface,
    surfaceDim = WhiteDnsPaletteLight.SurfaceAlt,
    surfaceContainerLowest = WhiteDnsPaletteLight.Background,
    surfaceContainerLow = WhiteDnsPaletteLight.SurfaceAlt,
    surfaceContainer = WhiteDnsPaletteLight.Surface,
    surfaceContainerHigh = WhiteDnsPaletteLight.DropdownSurface,
    surfaceContainerHighest = WhiteDnsPaletteLight.Surface,
    primaryFixed = WhiteDnsPaletteLight.AccentSurface,
    primaryFixedDim = WhiteDnsPaletteLight.AccentSurface,
    onPrimaryFixed = WhiteDnsPaletteLight.Ink,
    onPrimaryFixedVariant = WhiteDnsPaletteLight.Muted,
    secondaryFixed = WhiteDnsPaletteLight.SurfaceAlt,
    secondaryFixedDim = WhiteDnsPaletteLight.SurfaceAlt,
    onSecondaryFixed = WhiteDnsPaletteLight.Ink,
    onSecondaryFixedVariant = WhiteDnsPaletteLight.Muted,
    tertiaryFixed = WhiteDnsPaletteLight.SuccessSurface,
    tertiaryFixedDim = WhiteDnsPaletteLight.SuccessSurface,
    onTertiaryFixed = WhiteDnsPaletteLight.Ink,
    onTertiaryFixedVariant = WhiteDnsPaletteLight.Success,
)

private val WhiteDnsTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,
    ),
)

private val WhiteDnsPersianFontFamily = FontFamily(
    Font(R.font.vazirmatn_ui_fd_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_ui_fd_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_ui_fd_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_ui_fd_bold, FontWeight.Bold),
)

private fun Typography.withFontFamily(fontFamily: FontFamily): Typography {
    return copy(
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily),
    )
}

@Suppress("DEPRECATION")
@Composable
fun WhiteDnsTheme(
    themeMode: String = WhiteDnsThemeMode.System,
    languageCode: String = WhiteDnsLanguage.En,
    content: @Composable () -> Unit,
) {
    val darkTheme = shouldUseDarkTheme(themeMode)
    val palette = if (darkTheme) WhiteDnsPaletteDark else WhiteDnsPaletteLight
    val colorScheme = if (darkTheme) WhiteDnsColorSchemeDark else WhiteDnsColorSchemeLight
    val isPersian = languageCode == WhiteDnsLanguage.Fa
    val layoutDirection = if (isPersian) LayoutDirection.Rtl else LayoutDirection.Ltr
    val typography = if (isPersian) {
        WhiteDnsTypography.withFontFamily(WhiteDnsPersianFontFamily)
    } else {
        WhiteDnsTypography
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = palette.Background.toArgb()
            window.navigationBarColor = palette.Surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val strings = if (isPersian) PersianStrings else EnglishStrings
    CompositionLocalProvider(
        LocalWhiteDnsPalette provides palette,
        LocalWhiteDnsStrings provides strings,
        androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}

@Composable
private fun shouldUseDarkTheme(themeMode: String): Boolean {
    val systemDarkTheme = isSystemInDarkTheme()
    return when (themeMode) {
        WhiteDnsThemeMode.Light -> false
        WhiteDnsThemeMode.Dark -> true
        else -> systemDarkTheme
    }
}
