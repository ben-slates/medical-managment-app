package com.medical.management.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Color(0xFF006A67),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9DF2EC),
    onPrimaryContainer = Color(0xFF00201F),
    secondary = Color(0xFF47617A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDE5FF),
    tertiary = Color(0xFF6D5E00),
    tertiaryContainer = Color(0xFFFFE16A),
    background = Color(0xFFF7FAFA),
    surface = Color(0xFFF7FAFA),
    surfaceContainer = Color(0xFFEAF0EF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171D1C),
    onSurfaceVariant = Color(0xFF3F4948),
    error = Color(0xFFBA1A1A)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF81D5D0),
    onPrimary = Color(0xFF003735),
    primaryContainer = Color(0xFF00504E),
    onPrimaryContainer = Color(0xFF9DF2EC),
    secondary = Color(0xFFAFC9E5),
    onSecondary = Color(0xFF17324A),
    secondaryContainer = Color(0xFF2F4961),
    tertiary = Color(0xFFE4C44D),
    tertiaryContainer = Color(0xFF524600),
    background = Color(0xFF0F1414),
    surface = Color(0xFF0F1414),
    surfaceContainer = Color(0xFF1B211F),
    surfaceContainerLow = Color(0xFF171D1C),
    onSurface = Color(0xFFDEE4E2),
    onSurfaceVariant = Color(0xFFBEC9C7),
    error = Color(0xFFFFB4AB)
)

@Composable
fun MedicalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
