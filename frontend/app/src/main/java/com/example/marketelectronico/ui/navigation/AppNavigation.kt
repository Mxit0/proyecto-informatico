package com.example.marketelectronico.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.marketelectronico.ui.auth.LoginScreen
import com.example.marketelectronico.ui.cart.CartScreen
import com.example.marketelectronico.ui.main.MainScreen
import com.example.marketelectronico.ui.product.ProductScreen

import com.example.marketelectronico.ui.profile.ProfileScreen
import com.example.marketelectronico.ui.chat.ChatListScreen
import com.example.marketelectronico.ui.chat.ConversationScreen
import com.example.marketelectronico.ui.forum.CreatePostScreen
import com.example.marketelectronico.ui.forum.ForumScreen
import com.example.marketelectronico.ui.forum.PostDetailScreen
import com.example.marketelectronico.ui.cart.PaymentScreen
import com.example.marketelectronico.ui.cart.AddPaymentMethodScreen
import com.example.marketelectronico.ui.product.ProductReviewScreen
import com.example.marketelectronico.ui.product.CategoriesScreen
import com.example.marketelectronico.ui.product.PublishScreen
import com.example.marketelectronico.ui.notifications.NotificationsScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login" // Empezamos en Login
    ) {

        // --- PANTALLAS BASE ---
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("main") {
            MainScreen(navController = navController)
        }
        composable("cart") {
            CartScreen(navController = navController)
        }
        composable("product_detail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductScreen(navController = navController, /* productId = productId */)
        }
        composable("product_reviews/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            // ProductReviewScreen(navController = navController, productId = productId)
        }
        composable("categories") {
            // CategoriesScreen(navController = navController)
        }
        composable("publish") {
            // PublishScreen(navController = navController)
        }

        composable("payment") {
            // PaymentScreen(navController = navController)
        }
        composable("add_payment_method") {
            // AddPaymentMethodScreen(navController = navController)
        }

        composable("profile") {
            ProfileScreen(navController = navController)
        }
        composable("chat_list") {
            ChatListScreen(navController = navController)
        }
        composable("conversation/{chatId}") {
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