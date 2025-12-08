package com.example.marketelectronico.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel // <-- 1. IMPORTAR
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.marketelectronico.ui.auth.LoginScreen
import com.example.marketelectronico.ui.cart.AddPaymentMethodScreen
import com.example.marketelectronico.ui.cart.CartScreen
import com.example.marketelectronico.ui.cart.PayConfirmScreen
import com.example.marketelectronico.ui.cart.PaymentScreen
import com.example.marketelectronico.ui.chat.ChatListScreen
import com.example.marketelectronico.ui.chat.ConversationScreen
import com.example.marketelectronico.ui.forum.CreatePostScreen
import com.example.marketelectronico.ui.forum.ForumScreen
import com.example.marketelectronico.ui.forum.PostDetailScreen
import com.example.marketelectronico.ui.main.MainScreen
import com.example.marketelectronico.ui.main.MainViewModel // <-- 2. IMPORTAR
import com.example.marketelectronico.ui.product.ProductReviewScreen
import com.example.marketelectronico.ui.product.ProductScreen
import com.example.marketelectronico.ui.product.ProductViewModel // <-- 3. IMPORTAR
import com.example.marketelectronico.ui.product.PublishScreen
import com.example.marketelectronico.ui.publish.PublishViewModel
import com.example.marketelectronico.ui.profile.ProfileScreen
import com.example.marketelectronico.ui.review.ReviewScreen
import com.example.marketelectronico.ui.category.CategoryProductsScreen
import com.example.marketelectronico.ui.category.CategoryProductsViewModel
import android.net.Uri
import androidx.navigation.NavHostController

/**
 * Gestiona TODA la navegación de la aplicación.
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    //val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // --- PANTALLAS BASE ---
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("main") {
            // --- 4. INYECTAR MAINVIEWMODEL ---
            MainScreen(
                navController = navController,
                viewModel = viewModel<MainViewModel>()
            )
        }
        composable("cart") {
            CartScreen(navController = navController)
        }

        // --- PANTALLAS DE PRODUCTO (Fusionadas) ---
        composable(
            route = "product_detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            // --- 5. INYECTAR PRODUCTVIEWMODEL ---
            ProductScreen(
                navController = navController,
                productId = backStackEntry.arguments?.getString("productId"),
                viewModel = viewModel<ProductViewModel>()
            )
        }

        // --- RUTA DESCOMENTADA ---
        composable("product_reviews/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductReviewScreen(navController = navController, productId = productId)
        }

        composable(
            route = "add_review/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            ReviewScreen(
                navController = navController,
                productId = backStackEntry.arguments?.getString("productId")
            )
        }

        composable(
            route = "category_products/{categoryId}/{categoryName}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { entry ->
            val categoryId = entry.arguments?.getInt("categoryId") ?: 0
            val rawName = entry.arguments?.getString("categoryName") ?: "Categoría"
            val categoryName = Uri.decode(rawName)

            CategoryProductsScreen(
                navController = navController,
                categoryId = categoryId,
                categoryName = categoryName,
                viewModel = viewModel<CategoryProductsViewModel>()
            )
        }

        composable("categories") {
            // CategoriesScreen(navController = navController)
        }
        composable("publish") {
            PublishScreen(
                navController = navController,
                viewModel = viewModel<PublishViewModel>()
            )
        }

        // --- PANTALLAS DE PAGO (De 'master' - DESCOMENTADAS) ---
        composable("payment") {
            PaymentScreen(navController = navController)
        }

        composable(
            route = "pay_confirm/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            PayConfirmScreen(
                navController = navController,
                orderId = backStackEntry.arguments?.getString("orderId")
            )
        }

        composable("add_payment_method") {
            AddPaymentMethodScreen(navController = navController)
        }

        composable(
            route = "order_detail/{orderId}",
            arguments = listOf(androidx.navigation.navArgument("orderId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")
            // Importa tu nueva pantalla OrderDetailScreen
            com.example.marketelectronico.ui.cart.OrderDetailScreen(
                navController = navController,
                orderId = orderId
            )
        }

        // --- TUS PANTALLAS (De 'max') ---
        composable("profile") {
            ProfileScreen(navController = navController)
        }
        composable("chat_list") {
            ChatListScreen(navController = navController)
        }
        composable(
            route = "conversation/{chatId}/{otherUserId}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("otherUserId") { type = NavType.IntType } // Nuevo parámetro
            )
        ) {
            ConversationScreen(navController = navController)
        }
        composable("forum") {
            ForumScreen(navController = navController)
        }
        composable("post_detail/{postId}") {
            PostDetailScreen(navController = navController)
        }
        composable("create_post") {
            CreatePostScreen(navController = navController)
        }

        // --- OTRAS PANTALLAS ---
        composable("notifications") {
            // NotificationsScreen(navController = navController)
        }
    }
}