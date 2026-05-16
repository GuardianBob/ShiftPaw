package com.example.shiftpaw.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Violet80,
    onPrimaryContainer = PurpleDark,
    secondary = Gold40,
    onSecondary = Color.White,
    secondaryContainer = Gold80,
    onSecondaryContainer = Color(0xFF241A00),
    tertiary = Violet40,
    onTertiary = Color.White,
    tertiaryContainer = Violet80,
    background = SurfaceLight,
    surface = SurfaceLight,
    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = PurpleDark,
    primaryContainer = Color(0xFF4F378A),
    onPrimaryContainer = Violet80,
    secondary = Gold80,
    onSecondary = Color(0xFF241A00),
    secondaryContainer = Gold40,
    onSecondaryContainer = Gold80,
    tertiary = Violet80,
    onTertiary = Color(0xFF1F1635),
    background = SurfaceDark,
    surface = SurfaceDark,
    error = Color(0xFFFFB4AB)
)

@Composable
fun ShiftPawTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
