package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = SkyBlueDark,
    onPrimaryContainer = SkyBlueLight,
    secondary = CoralOrange,
    onSecondary = Color.White,
    secondaryContainer = CoralOrangeDark,
    onSecondaryContainer = CoralOrangeLight,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = DarkSurface,
    onBackground = NeutralGray100,
    surface = DarkSurfaceElevated,
    onSurface = NeutralGray100,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = NeutralGray400,
    outline = DarkBorder,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = SkyBlueSubtle,
    onPrimaryContainer = SkyBlueDark,
    secondary = CoralOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0E8),
    onSecondaryContainer = CoralOrangeDark,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = NeutralGray50,
    onBackground = NeutralGray900,
    surface = NeutralWhite,
    onSurface = NeutralGray900,
    surfaceVariant = NeutralGray100,
    onSurfaceVariant = NeutralGray600,
    outline = NeutralGray300,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (darkTheme) DarkColorScheme else LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}