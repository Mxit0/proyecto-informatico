package com.example.marketelectronico.ui.forum

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.base.BaseScreen
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme

/**
 * Pantalla que muestra la lista de hilos (posts) del foro.
 * Usa la plantilla BaseScreen.
 */
@Composable
fun ForumScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    BaseScreen(
        title = "Foro",
        navController = navController, // <-- CAMBIO AQUÍ
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Text(text = "Lista de todos los hilos del foro")

            // Ejemplo de navegación a un post específico
            Button(onClick = { navController.navigate("post_detail/1") }) {
                Text(text = "Abrir Hilo 1")
            }
            // Ejemplo de navegación para crear un post
            Button(onClick = { navController.navigate("create_post") }) {
                Text(text = "Crear Nuevo Hilo")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForumScreenPreview() {
    MarketElectronicoTheme {
        ForumScreen(navController = rememberNavController())
    }
}