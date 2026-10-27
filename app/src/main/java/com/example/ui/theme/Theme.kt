package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalReducedMotion = staticCompositionLocalOf { false }

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberCyanPrimary,
    onPrimary = CyberNavyBackground,
    primaryContainer = CyberCyanContainer,
    onPrimaryContainer = CyberCyanPrimary,
    secondary = CyberCyanSecondary,
    onSecondary = CyberNavyBackground,
    secondaryContainer = CyberNavyElevated,
    onSecondaryContainer = TextPrimaryWhite,
    tertiary = StatusPresentGreen,
    onTertiary = CyberNavyBackground,
    background = CyberNavyBackground,
    onBackground = TextPrimaryWhite,
    surface = CyberNavySurface,
    onSurface = TextPrimaryWhite,
    surfaceVariant = CyberNavyCard,
    onSurfaceVariant = TextSecondarySlate,
    outline = CyberNavyBorder,
    outlineVariant = Color(0xFF1E293B),
    error = StatusAbsentRed,
    onError = Color.White
)

private val CyberLightColorScheme = lightColorScheme(
    primary = Color(0xFF007799),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF003647),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    activeDarkThemeMode = darkTheme
    val scheme = if (darkTheme) CyberDarkColorScheme else CyberLightColorScheme
    CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}
