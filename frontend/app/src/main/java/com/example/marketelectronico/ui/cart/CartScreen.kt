package com.example.marketelectronico.ui.cart

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
 * Pantalla del Carrito de Compras
 * Usa la plantilla BaseScreen.
 */
@Composable
fun CartScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    BaseScreen(title = "Pantalla Carrito", modifier = modifier) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Text(text = "Contenido de Pantalla Carrito")

            Button(onClick = { navController.navigate("main") }) {
                Text("Volver a la Tienda")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    MarketElectronicoTheme {
        CartScreen(navController = rememberNavController())
    }
}