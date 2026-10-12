package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StreamPrimary,
    onPrimary = Color.Black,
    primaryContainer = StreamPrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = StreamSecondary,
    onSecondary = Color.Black,
    secondaryContainer = StreamDarkSurfaceVariant,
    onSecondaryContainer = StreamTextPrimary,
    tertiary = StreamGold,
    onTertiary = Color.Black,
    background = StreamDarkBackground,
    onBackground = StreamTextPrimary,
    surface = StreamDarkSurface,
    onSurface = StreamTextPrimary,
    surfaceVariant = StreamDarkSurfaceVariant,
    onSurfaceVariant = StreamTextSecondary,
    outline = StreamDarkBorder,
    error = StreamAccent,
    onError = Color.White
)

@Composable
fun CloudStreamTheme(
    darkTheme: Boolean = true, // Streaming app defaults to immersive dark mode
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
