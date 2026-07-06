package com.engineeringood.athena.composeruntime

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Shared Athena shell theme with one strong skin and light/dark variants.
 */
@Composable
fun AthenaComposeTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (isDark) athenaDarkColors() else athenaLightColors(),
        typography = athenaTypography(),
        content = content,
    )
}

private fun athenaDarkColors(): ColorScheme {
    return darkColorScheme(
        primary = Color(0xFF4C8DFF),
        onPrimary = Color(0xFF07111D),
        primaryContainer = Color(0xFF203554),
        onPrimaryContainer = Color(0xFFDCEBFF),
        secondary = Color(0xFF8CB8FF),
        onSecondary = Color(0xFF0C1624),
        background = Color(0xFF0F1722),
        onBackground = Color(0xFFE7EEF8),
        surface = Color(0xFF111D2C),
        onSurface = Color(0xFFE7EEF8),
        surfaceVariant = Color(0xFF18263A),
        onSurfaceVariant = Color(0xFFA7B6CB),
        surfaceContainer = Color(0xFF162234),
        surfaceContainerHigh = Color(0xFF1B2B42),
        surfaceContainerHighest = Color(0xFF203554),
        surfaceContainerLow = Color(0xFF132031),
        surfaceContainerLowest = Color(0xFF0D1622),
        surfaceBright = Color(0xFF22344F),
        outline = Color(0xFF3A4E6A),
        outlineVariant = Color(0xFF2A3C56),
        error = Color(0xFFF07D7D),
        onError = Color(0xFF24090B),
        errorContainer = Color(0xFF542126),
        onErrorContainer = Color(0xFFFFDADB),
    )
}

private fun athenaLightColors(): ColorScheme {
    return lightColorScheme(
        primary = Color(0xFF2D62C7),
        onPrimary = Color(0xFFF8FBFF),
        primaryContainer = Color(0xFFDCE6F4),
        onPrimaryContainer = Color(0xFF13233A),
        secondary = Color(0xFF4C8DFF),
        onSecondary = Color(0xFFF5F9FF),
        background = Color(0xFFF3F6FB),
        onBackground = Color(0xFF102032),
        surface = Color(0xFFFAFBFD),
        onSurface = Color(0xFF102032),
        surfaceVariant = Color(0xFFEAF0F8),
        onSurfaceVariant = Color(0xFF53657C),
        surfaceContainer = Color(0xFFF4F7FB),
        surfaceContainerHigh = Color(0xFFE8EEF7),
        surfaceContainerHighest = Color(0xFFDCE6F4),
        surfaceContainerLow = Color(0xFFF8FBFD),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceBright = Color(0xFFFFFFFF),
        outline = Color(0xFFB7C4D6),
        outlineVariant = Color(0xFFD0DAE8),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF410E0B),
    )
}

private fun athenaTypography(): Typography {
    return Typography(
        headlineMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 20.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 18.sp,
        ),
    )
}
