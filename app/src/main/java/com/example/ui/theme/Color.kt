package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Reactive theme mode token so all components, cards, charts, borders, and text adapt seamlessly
var activeDarkThemeMode by mutableStateOf(true)

// Deep Dark Navy Cybersecurity Palette & Complete High-Contrast Light Mode Counterparts
val CyberNavyBackground: Color
    get() = if (activeDarkThemeMode) Color(0xFF0A1124) else Color(0xFFF1F5F9)
val CyberNavySurface: Color
    get() = if (activeDarkThemeMode) Color(0xFF111C38) else Color(0xFFFFFFFF)
val CyberNavyCard: Color
    get() = if (activeDarkThemeMode) Color(0xFF162447) else Color(0xFFFFFFFF)
val CyberNavyElevated: Color
    get() = if (activeDarkThemeMode) Color(0xFF1E315C) else Color(0xFFE2E8F0)
val CyberNavyBorder: Color
    get() = if (activeDarkThemeMode) Color(0xFF28417A) else Color(0xFFCBD5E1)

// Cyan & Tech Accents (Electric Cyan + Restrained Violet)
val CyberCyanPrimary: Color
    get() = if (activeDarkThemeMode) Color(0xFF00E5FF) else Color(0xFF0284C7)
val CyberCyanSecondary: Color
    get() = if (activeDarkThemeMode) Color(0xFF38BDF8) else Color(0xFF0369A1)
val CyberVioletAccent: Color
    get() = if (activeDarkThemeMode) Color(0xFF818CF8) else Color(0xFF4F46E5)
val CyberCyanDim: Color
    get() = if (activeDarkThemeMode) Color(0xFF0B4F6C) else Color(0xFFBAE6FD)
val CyberCyanContainer: Color
    get() = if (activeDarkThemeMode) Color(0xFF09314B) else Color(0xFFE0F2FE)

// Semantic Status Colors (Accessible in both Dark and Light themes)
val StatusPresentGreen: Color
    get() = if (activeDarkThemeMode) Color(0xFF10B981) else Color(0xFF059669)
val StatusPartialAmber: Color
    get() = if (activeDarkThemeMode) Color(0xFFF59E0B) else Color(0xFFD97706)
val StatusAbsentRed: Color
    get() = if (activeDarkThemeMode) Color(0xFFEF4444) else Color(0xFFDC2626)
val StatusRestIndigo: Color
    get() = if (activeDarkThemeMode) Color(0xFF818CF8) else Color(0xFF4F46E5)

// Text Colors
val TextPrimaryWhite: Color
    get() = if (activeDarkThemeMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
val TextSecondarySlate: Color
    get() = if (activeDarkThemeMode) Color(0xFF94A3B8) else Color(0xFF475569)
val TextMutedSlate: Color
    get() = if (activeDarkThemeMode) Color(0xFF64748B) else Color(0xFF64748B)

