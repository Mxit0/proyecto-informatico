package com.example.marketelectronico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.marketelectronico.ui.navigation.AppNavigation
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import com.example.marketelectronico.utils.TokenManager
import com.example.marketelectronico.data.remote.SocketManager
/**
 * Es el anfitrión de la aplicación de Jetpack Compose.
 * Carga el Tema y el controlador de Navegación.
 *
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TokenManager.init(this)

        // Inicializar Socket con el token (si ya hay uno guardado)
        val token = TokenManager.getToken() ?: ""
        SocketManager.init(token) // <-- Pasa el token si tu init lo requiere

        setContent {
            MarketElectronicoTheme {
                AppNavigation()
            }
        }
    }
}