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
 * Esta pantalla NO usa BaseScreen para tener un control total.
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

            // Acción de ejemplo: navegar a "main" (homepage) después del login
            Button(onClick = {
                navController.navigate("main") {
                    // Borra "login" del historial para que el usuario no pueda "volver"
                    popUpTo("login") { inclusive = true }
                }
            }) {
                Text(text = "Ingresar")
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