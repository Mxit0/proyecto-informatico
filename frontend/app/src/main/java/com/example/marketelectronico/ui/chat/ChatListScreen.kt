package com.example.marketelectronico.ui.chat

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
 * Pantalla que muestra la lista de conversaciones (chats)
 * Usa la plantilla BaseScreen.
 */
@Composable
fun ChatListScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    BaseScreen(
        title = "Mis Mensajes",
        navController = navController, // <-- CAMBIO AQUÍ
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Text(text = "Lista de todas las conversaciones")

            // Ejemplo de navegación a una conversación específica
            Button(onClick = { navController.navigate("conversation/1") }) {
                Text(text = "Abrir Chat con Vendedor 1")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    MarketElectronicoTheme {
        ChatListScreen(navController = rememberNavController())
    }
}