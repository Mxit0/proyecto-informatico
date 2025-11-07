package com.example.marketelectronico.ui.chat

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
 * Pantalla que muestra los mensajes dentro de un chat específico.
 * Usa la plantilla BaseScreen.
 */
@Composable
fun ConversationScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    BaseScreen(
        title = "Conversación",
        navController = navController, // <-- CAMBIO AQUÍ
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Text(text = "Mensajes de la conversación (ID: ...)")
            // Aquí iría la lista de mensajes (LazyColumn) y un TextField para escribir
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConversationScreenPreview() {
    MarketElectronicoTheme {
        ConversationScreen(navController = rememberNavController())
    }
}