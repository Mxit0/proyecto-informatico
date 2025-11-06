package com.example.marketelectronico.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.auth.LoginScreen
import com.example.marketelectronico.ui.cart.CartScreen
import com.example.marketelectronico.ui.main.MainScreen
import com.example.marketelectronico.ui.product.ProductScreen

/**
 * Gestiona la navegación para el prototipo.
 * Define las 4 rutas principales.
 *
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

        // Ruta con argumento (para ver un producto específico)
        composable("product_detail/{productId}") { backStackEntry ->
            // En un futuro, obtendrías el ID así:
            // val productId = backStackEntry.arguments?.getString("productId")
            ProductScreen(navController = navController)
        }

        composable("cart") {
            CartScreen(navController = navController)
        }
    }
}