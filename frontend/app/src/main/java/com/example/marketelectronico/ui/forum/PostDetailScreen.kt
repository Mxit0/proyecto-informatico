package com.example.marketelectronico.ui.forum

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
 * Pantalla que muestra un hilo (post) específico y sus respuestas.
 * Usa la plantilla BaseScreen.
 */
@Composable
fun PostDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier
    // En el futuro, recibirás el ID del post:
    // postId: String?
) {
    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
    // Ahora le pasamos el navController a la plantilla BaseScreen.
    BaseScreen(
        title = "Detalle de Hilo",
        navController = navController, // <-- Esta línea es la que faltaba
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Text(text = "Contenido del Hilo (ID: ...)")
            Text(text = "Aquí irían las respuestas (LazyColumn)...")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostDetailScreenPreview() {
    MarketElectronicoTheme {
        PostDetailScreen(navController = rememberNavController())
    }
}