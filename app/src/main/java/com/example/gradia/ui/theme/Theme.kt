package com.example.gradia.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PurpleGradia,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3EDF7),
    onPrimaryContainer = Color(0xFF2D0F40),
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Pink40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1F1F1F),
    surface = Color.White,
    onSurface = Color(0xFF4A4A4A),
    surfaceVariant = Color(0xFFF3EDF7),
    onSurfaceVariant = Color(0xFF6B6B6B),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFE0D4EE),
    inverseSurface = Color(0xFF2B2930),
    inverseOnSurface = Color(0xFFE6E1E5),
    inversePrimary = Color(0xFFD0BCFF)
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleGradia,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B1A5E),
    onPrimaryContainer = Color(0xFFF3EDF7),
    secondary = PurpleGrey80,
    onSecondary = Color(0xFF1D192B),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Pink80,
    onTertiary = Color(0xFF31111D),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = Color(0xFF4A4458),
    inverseSurface = Color(0xFFFBF8FF),
    inverseOnSurface = Color(0xFF4A4A4A),
    inversePrimary = PurpleGradia
)

@Composable
fun GradiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
