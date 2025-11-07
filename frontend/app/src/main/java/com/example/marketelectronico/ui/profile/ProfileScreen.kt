package com.example.marketelectronico.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.base.BaseScreen
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme

/**
 * Pantalla de Perfil de Usuario
 * Usa la plantilla BaseScreen.
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // AHORA PASAMOS EL NAVCONTROLLER A LA PLANTILLA
    BaseScreen(
        title = "Mi Perfil",
        navController = navController, // <-- CAMBIO AQUÍ
        modifier = modifier
    ) { padding ->
        // El padding asegura que tu contenido no quede oculto por la TopAppBar
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Text(text = "Contenido de la Pantalla de Perfil")
            // Aquí irían los datos del usuario, botones para cerrar sesión, etc.
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MarketElectronicoTheme {
        ProfileScreen(navController = rememberNavController())
    }
}