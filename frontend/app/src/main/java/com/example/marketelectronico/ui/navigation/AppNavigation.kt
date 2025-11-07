package com.example.marketelectronico.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.auth.LoginScreen
// Asegúrate de que esta importación exista, o crea el archivo CartScreen.kt
// import com.example.marketelectronico.ui.cart.CartScreen
import com.example.marketelectronico.ui.main.MainScreen
import com.example.marketelectronico.ui.product.ProductScreen

/**
 * Gestiona la navegación para el prototipo.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login" // Empezamos en Login
    ) {

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("main") {
            MainScreen(navController = navController)
        }

        // --- RUTA DEL PRODUCTO CORREGIDA ---
        composable("product_detail/{productId}") { backStackEntry ->
            // 1. Obtenemos el ID del producto desde la ruta
            val productId = backStackEntry.arguments?.getString("productId")

            // 2. Pasamos el ID a la pantalla del producto
            ProductScreen(
                navController = navController,
                productId = productId
            )
        }

        // Esta ruta fallará si no tienes un composable 'CartScreen'
        // Coméntala si aún no la has creado.
        /*
        composable("cart") {
            CartScreen(navController = navController)
        }
        */
    }
}