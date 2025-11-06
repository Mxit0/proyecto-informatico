package com.example.marketelectronico.ui.product

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
 * Pantalla de Detalle de Producto
 * Usa la plantilla BaseScreen.
 *
 */
@Composable
fun ProductScreen(
    navController: NavController,
    modifier: Modifier = Modifier
    // En el futuro, querrás recibir el ID del producto:
    // productId: String?
) {
    BaseScreen(title = "Pantalla Producto", modifier = modifier) { padding ->
        // El padding asegura que tu contenido no quede oculto por la TopAppBar
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Text(text = "Contenido de Pantalla Producto")
            // Aquí mostrarías los detalles del producto usando el productId

            Button(onClick = { navController.navigate("cart") }) {
                Text("Añadir al Carrito e ir")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductScreenPreview() {
    MarketElectronicoTheme {
        ProductScreen(navController = rememberNavController())
    }
}