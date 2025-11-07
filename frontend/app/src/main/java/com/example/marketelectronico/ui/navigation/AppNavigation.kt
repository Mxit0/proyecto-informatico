package com.example.marketelectronico.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.auth.LoginScreen
import com.example.marketelectronico.ui.cart.CartScreen
import com.example.marketelectronico.ui.cart.PaymentScreen
import com.example.marketelectronico.ui.cart.AddPaymentMethodScreen
import com.example.marketelectronico.ui.main.MainScreen
import com.example.marketelectronico.ui.product.ProductReviewScreen
import com.example.marketelectronico.ui.product.ProductScreen

/**
 * Gestiona la navegación para el prototipo.
 * Define las 7 rutas principales.
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

        composable("product_detail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductScreen(navController = navController, productId = productId)
        }

        composable("product_reviews/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductReviewScreen(navController = navController, productId = productId)
        }

        composable("cart") {
            CartScreen(navController = navController)
        }

        composable("payment") {
            PaymentScreen(navController = navController)
        }

        // --- 2. AÑADIR NUEVA RUTA ---
        composable("add_payment_method") {
            AddPaymentMethodScreen(navController = navController)
        }
    }
}