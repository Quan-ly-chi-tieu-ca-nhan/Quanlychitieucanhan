package com.example.personalexpensemanagementapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

// App-specific color tokens (light)
private val LightPrimary = Color(0xFF1565C0) // deeper blue
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFD9E8FF)
private val LightOnPrimaryContainer = Color(0xFF001E36)

private val LightBackground = Color(0xFFF6F9FF)
private val LightOnBackground = Color(0xFF1C1B1F)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF1C1B1F)
private val LightSurfaceVariant = Color(0xFFE9F4FF)
private val LightOutline = Color(0xFFBDBDBD)

private val LightError = Color(0xFFB00020)
private val LightSuccess = Color(0xFF2E7D32)

// App-specific color tokens (dark)
private val DarkPrimary = Color(0xFF90CAF9) // lighter blue for dark
private val DarkOnPrimary = Color(0xFF0B2944)
private val DarkPrimaryContainer = Color(0xFF0D3A66)
private val DarkOnPrimaryContainer = Color(0xFFD9E8FF)

private val DarkBackground = Color(0xFF0F1720)
private val DarkOnBackground = Color(0xFFE6EEF5)
private val DarkSurface = Color(0xFF111827)
private val DarkOnSurface = Color(0xFFE6EEF5)
private val DarkSurfaceVariant = Color(0xFF0C293F)
private val DarkOutline = Color(0xFF374151)

private val DarkError = Color(0xFFCF6679)
private val DarkSuccess = Color(0xFF66BB6A)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,

    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    outline = DarkOutline,

    error = DarkError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,

    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    outline = LightOutline,

    error = LightError,
    onError = Color.White
)

@Composable
fun PersonalExpenseManagementApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}