package com.example.marketelectronico.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.auth.LoginScreen
// import com.example.marketelectronico.ui.cart.CartScreen // (Asegúrate de tener este archivo)
import com.example.marketelectronico.ui.main.MainScreen
import com.example.marketelectronico.ui.product.ProductScreen
// ***** ESTA ES LA LÍNEA QUE ARREGLA EL ERROR *****
import com.example.marketelectronico.ui.product.ProductReviewScreen

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

        composable("product_detail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductScreen(
                navController = navController,
                productId = productId
            )
        }

        // --- RUTA AÑADIDA PARA REVIEWS ---
        composable("product_reviews/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductReviewScreen( // <-- Esto ya no dará error
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