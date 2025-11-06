package com.example.marketelectronico.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme

/**
 * Pantalla de Login (Punto de entrada)
 * No usa BaseScreen para tener un control total del layout.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Pantalla Login")

            // Acción: navegar a "main" después del login
            Button(onClick = {
                navController.navigate("main") {
                    // Borra "login" del historial
                    popUpTo("login") { inclusive = true }
                }
            }) {
                Text(text = "Ingresar (Ir a Main)")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MarketElectronicoTheme {
        LoginScreen(navController = rememberNavController())
    }
}