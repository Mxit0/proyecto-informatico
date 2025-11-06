package com.example.marketelectronico.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

// Usamos el darkColorScheme con los colores de tu amigo
private val DarkColorScheme = darkColorScheme(
    primary = LightPurple,
    background = DarkBlueBg,
    surface = DarkBlueSurface,
    onBackground = White,
    onSurface = White,
    onPrimary = Color.Black
)

@Composable
fun MarketElectronicoTheme(
    // Forzamos el tema oscuro ya que el diseño está pensado para eso
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Usamos siempre el esquema oscuro

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Color de fondo
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}