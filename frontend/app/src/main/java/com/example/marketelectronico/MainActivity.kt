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

/**
 * Es el anfitrión de la aplicación de Jetpack Compose.
 * Carga el Tema y el controlador de Navegación.
 *
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Inicializar TokenManager
        TokenManager.init(this)
        
        setContent {
            MarketElectronicoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}