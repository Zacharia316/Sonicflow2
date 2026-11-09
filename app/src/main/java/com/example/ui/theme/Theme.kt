package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioLightColorScheme = lightColorScheme(
    primary = StudioPrimary,
    onPrimary = Color.White,
    primaryContainer = StudioPillInactive,
    onPrimaryContainer = StudioTextPrimary,
    secondary = StudioPrimaryLight,
    onSecondary = Color.White,
    tertiary = StudioRecordRed,
    background = StudioBgGradientStart,
    onBackground = StudioTextPrimary,
    surface = StudioSurface,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioCardBg,
    onSurfaceVariant = StudioTextSecondary,
    outline = StudioCardBorder
)

private val StudioDarkColorScheme = darkColorScheme(
    primary = StudioPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF382A40),
    onPrimaryContainer = Color(0xFFF1EBF4),
    secondary = StudioPrimary,
    onSecondary = Color.White,
    tertiary = StudioRecordRed,
    background = Color(0xFF1B1420),
    onBackground = Color(0xFFEDE5F0),
    surface = Color(0xFF281F2F),
    onSurface = Color(0xFFEDE5F0),
    surfaceVariant = Color(0xFF362B3F),
    onSurfaceVariant = Color(0xFFBCAFC4),
    outline = Color(0xFF493A54)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent pastel branding from user reference
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StudioDarkColorScheme else StudioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
