package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light first — modern travel app aesthetic
private val LightColorScheme = lightColorScheme(
    primary             = EmeraldGreen,
    onPrimary           = PureWhite,
    primaryContainer    = MintGreen,
    onPrimaryContainer  = DarkGreen,
    secondary           = CrimsonRed,
    onSecondary         = PureWhite,
    secondaryContainer  = WarmPink,
    onSecondaryContainer= Color(0xFF7F1D1D),
    tertiary            = RoyalGold,
    onTertiary          = PureWhite,
    tertiaryContainer   = PaleGold,
    onTertiaryContainer = DeepGold,
    background          = SurfaceBg,
    onBackground        = TextPrimary,
    surface             = SurfaceCard,
    onSurface           = TextPrimary,
    surfaceVariant      = SurfaceMuted,
    onSurfaceVariant    = TextSecondary,
    outline             = Border,
    outlineVariant      = Divider,
    error               = StatusError,
    onError             = PureWhite
)

private val DarkColorScheme = darkColorScheme(
    primary             = LightGreen,
    onPrimary           = DarkGreen,
    primaryContainer    = ForestGreen,
    onPrimaryContainer  = MintGreen,
    secondary           = SoftRed,
    onSecondary         = Color(0xFF7F1D1D),
    secondaryContainer  = Color(0xFF7F1D1D),
    onSecondaryContainer= WarmPink,
    tertiary            = LiquidGold,
    onTertiary          = DeepGold,
    tertiaryContainer   = DeepGold,
    onTertiaryContainer = SoftGold,
    background          = DarkNavy,
    onBackground        = IvoryWhite,
    surface             = DarkSurface,
    onSurface           = IvoryWhite,
    surfaceVariant      = DarkCard,
    onSurfaceVariant    = SilverGray,
    outline             = DarkBorder,
    outlineVariant      = DarkBorder,
    error               = BrightRed,
    onError             = PureWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
}

val LightColors = LightColorScheme
val DarkColors  = DarkColorScheme
