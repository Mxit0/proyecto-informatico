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
 * Pantalla con el formulario para crear un nuevo hilo en el foro.
 * Usa la plantilla BaseScreen.
 */
@Composable
fun CreatePostScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    BaseScreen(
        title = "Crear Nuevo Hilo",
        navController = navController, // <-- CAMBIO AQUÍ
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Text(text = "Formulario para crear un hilo")
            // Aquí irían los TextField (título, contenido) y un botón de "Publicar"

            Button(onClick = { navController.popBackStack() }) {
                Text(text = "Publicar (y volver al foro)")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePostScreenPreview() {
    MarketElectronicoTheme {
        CreatePostScreen(navController = rememberNavController())
    }
}