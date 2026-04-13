package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FreshGreen,
    onPrimary = PureWhite,
    primaryContainer = EmeraldGreen,
    onPrimaryContainer = LightGreen,
    secondary = CrimsonRed,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF5C0015),
    onSecondaryContainer = SoftRed,
    tertiary = RoyalGold,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF5C4A10),
    onTertiaryContainer = SoftGold,
    background = DarkNavy,
    onBackground = IvoryWhite,
    surface = DarkSurface,
    onSurface = IvoryWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = SilverGray,
    outline = DarkBorder,
    error = StatusError,
    onError = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = FreshGreen,
    onPrimary = PureWhite,
    primaryContainer = MintGreen,
    onPrimaryContainer = DarkGreen,
    secondary = CrimsonRed,
    onSecondary = PureWhite,
    secondaryContainer = WarmPink,
    onSecondaryContainer = Color(0xFF5C0015),
    tertiary = RoyalGold,
    onTertiary = PureWhite,
    tertiaryContainer = PaleGold,
    onTertiaryContainer = Color(0xFF5C4A10),
    background = WarmWhite,
    onBackground = DeepCharcoal,
    surface = PureWhite,
    onSurface = DeepCharcoal,
    surfaceVariant = IvoryWhite,
    onSurfaceVariant = SteelGray,
    outline = CloudGray,
    error = StatusError,
    onError = PureWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

// Expose for external use
val LightColors = LightColorScheme
val DarkColors = DarkColorScheme
