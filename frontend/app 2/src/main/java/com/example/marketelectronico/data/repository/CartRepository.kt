package com.example.marketelectronico.data.repository

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import com.example.marketelectronico.data.model.Product

/**
 * Un repositorio simple (singleton) para manejar el estado del carrito de compras.
 * Usa mutableStateListOf para que Compose pueda reaccionar a los cambios.
 */
object CartRepository {

    // Lista observable de productos en el carrito
    val cartItems = mutableStateListOf<Product>()

    // --- ¡NUEVO! ---
    // Un estado derivado que calcula el total automáticamente cuando cartItems cambia.
    val totalPrice: State<Double> = derivedStateOf {
        cartItems.sumOf { it.price }
    }
    // -------------

    fun addToCart(product: Product) {
        if (!cartItems.contains(product)) {
            cartItems.add(product)
        }
    }

    fun removeFromCart(product: Product) {
        cartItems.remove(product)
    }

    fun clearCart() {
        cartItems.clear()
    }
}