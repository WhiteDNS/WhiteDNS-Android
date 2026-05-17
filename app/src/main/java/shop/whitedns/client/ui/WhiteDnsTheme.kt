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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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
    val layoutDirection = if (languageCode == WhiteDnsLanguage.Fa) LayoutDirection.Rtl else LayoutDirection.Ltr

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

    val strings = if (languageCode == WhiteDnsLanguage.Fa) PersianStrings else EnglishStrings
    CompositionLocalProvider(
        LocalWhiteDnsPalette provides palette,
        LocalWhiteDnsStrings provides strings,
        androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WhiteDnsTypography,
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
