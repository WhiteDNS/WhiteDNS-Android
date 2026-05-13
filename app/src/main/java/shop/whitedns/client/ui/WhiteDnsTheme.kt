package shop.whitedns.client.ui

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

object WhiteDnsPalette {
    val Background = Color(0xFF0D0F14)
    val Surface = Color(0xFF161A23)
    val SurfaceAlt = Color(0xFF111420)
    val DropdownSurface = Color(0xFF1C2030)
    val Border = Color(0xFF1E2330)
    val Divider = Color(0xFF252B3D)
    val ControlBorder = Color(0xFF2A3048)
    val Accent = Color(0xFF6C5CE7)
    val AccentPressed = Color(0xFF5A4BD1)
    val AccentText = Color(0xFF7A6BE1)
    val OnAccent = Color(0xFFFFFFFF)
    val Success = Color(0xFF00D68F)
    val Error = Color(0xFFFF6B6B)
    val Warning = Color(0xFFFBBF24)
    val WarningText = Color(0xFFFBBF24)
    val Ink = Color(0xFFEDEEF2)
    val TextPrimary = Ink
    val Muted = Color(0xFFC2C8E1)
    val TextSecondary = Muted
    val Pale = Color(0xFFADB5D3)
    val SectionTitle = Color(0xFFC1C1C2)
    val FieldLabel = Color(0xFFC1C1C2)
    val Description = Color(0xFFADADAD)
    val Placeholder = Color(0xFFA8B0CC)
    val Disabled = Color(0xFF717A9E)
    val Input = Color(0xFF111420)
    val AccentDim = Color(0xFF4A3FB0)
    val SurfaceHover = Color(0xFF1A1F2C)
    val AccentSurface = Color(0xFF1C1835)
    val SuccessSurface = Color(0xFF0D2E22)
    val WarningSurface = Color(0xFF2B2410)
    val ErrorSurface = Color(0xFF3D1C1C)
}

private val WhiteDnsColorScheme = darkColorScheme(
    primary = WhiteDnsPalette.Accent,
    onPrimary = WhiteDnsPalette.OnAccent,
    primaryContainer = WhiteDnsPalette.AccentPressed,
    onPrimaryContainer = WhiteDnsPalette.OnAccent,
    secondary = WhiteDnsPalette.Pale,
    onSecondary = WhiteDnsPalette.Background,
    secondaryContainer = WhiteDnsPalette.DropdownSurface,
    onSecondaryContainer = WhiteDnsPalette.Ink,
    tertiary = WhiteDnsPalette.Success,
    onTertiary = WhiteDnsPalette.Background,
    tertiaryContainer = WhiteDnsPalette.SuccessSurface,
    onTertiaryContainer = WhiteDnsPalette.Success,
    background = WhiteDnsPalette.Background,
    onBackground = WhiteDnsPalette.Ink,
    surface = WhiteDnsPalette.Surface,
    onSurface = WhiteDnsPalette.Ink,
    surfaceVariant = WhiteDnsPalette.SurfaceAlt,
    onSurfaceVariant = WhiteDnsPalette.Muted,
    surfaceTint = WhiteDnsPalette.Accent,
    outline = WhiteDnsPalette.ControlBorder,
    outlineVariant = WhiteDnsPalette.Border,
    inverseSurface = WhiteDnsPalette.Ink,
    inverseOnSurface = WhiteDnsPalette.Background,
    inversePrimary = WhiteDnsPalette.AccentPressed,
    error = WhiteDnsPalette.Error,
    onError = WhiteDnsPalette.OnAccent,
    errorContainer = WhiteDnsPalette.ErrorSurface,
    onErrorContainer = WhiteDnsPalette.Error,
    scrim = Color(0xFF000000),
    surfaceBright = WhiteDnsPalette.Divider,
    surfaceDim = WhiteDnsPalette.Background,
    surfaceContainerLowest = WhiteDnsPalette.Background,
    surfaceContainerLow = WhiteDnsPalette.SurfaceAlt,
    surfaceContainer = WhiteDnsPalette.Surface,
    surfaceContainerHigh = WhiteDnsPalette.DropdownSurface,
    surfaceContainerHighest = WhiteDnsPalette.Divider,
    primaryFixed = WhiteDnsPalette.AccentSurface,
    primaryFixedDim = WhiteDnsPalette.AccentSurface,
    onPrimaryFixed = WhiteDnsPalette.Ink,
    onPrimaryFixedVariant = WhiteDnsPalette.Muted,
    secondaryFixed = WhiteDnsPalette.DropdownSurface,
    secondaryFixedDim = WhiteDnsPalette.DropdownSurface,
    onSecondaryFixed = WhiteDnsPalette.Ink,
    onSecondaryFixedVariant = WhiteDnsPalette.Muted,
    tertiaryFixed = WhiteDnsPalette.SuccessSurface,
    tertiaryFixedDim = WhiteDnsPalette.SuccessSurface,
    onTertiaryFixed = WhiteDnsPalette.Ink,
    onTertiaryFixedVariant = WhiteDnsPalette.Success,
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
fun WhiteDnsTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = WhiteDnsPalette.Background.toArgb()
            window.navigationBarColor = WhiteDnsPalette.Surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = WhiteDnsColorScheme,
        typography = WhiteDnsTypography,
        content = content,
    )
}
