package com.example.marketelectronico.data.repository

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.remote.ApiClient
import com.example.marketelectronico.data.remote.AddToCartRequest
import com.example.marketelectronico.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CartRepository {

    private val api = ApiClient.cartApi

    val cartItems = mutableStateListOf<Product>()

    private val _totalPrice = mutableDoubleStateOf(0.0)
    val totalPrice: State<Double> = _totalPrice

    private val repoScope = CoroutineScope(Dispatchers.IO)

    /**
     * Carga el carrito desde el backend y actualiza la lista local.
     */
    fun loadCart() {
        val userId = TokenManager.getUserId()?.toString()
        if (userId == null) {
            cartItems.clear()
            _totalPrice.doubleValue = 0.0
            return
        }

        repoScope.launch {
            try {
                val response = api.getCart(userId)
                if (response.isSuccessful && response.body() != null) {
                    val cartData = response.body()!!

                    val mappedProducts = cartData.items.map { dto ->
                        Product(
                            id = dto.idProducto,
                            name = dto.nombre,
                            price = dto.precioUnitario,
                            imageUrl = dto.imagenUrl ?: "",

                            description = "",
                            status = "En stock: ${dto.stock}",
                            sellerId = 0,
                            sellerName = "Tienda",
                            sellerRating = 0.0,
                            sellerReviews = 0,
                            specifications = emptyMap()
                        )
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        cartItems.clear()
                        cartItems.addAll(mappedProducts)
                        _totalPrice.doubleValue = cartData.total
                    }
                }
            } catch (e: Exception) {
                Log.e("CartRepository", "Error cargando carrito", e)
            }
        }
    }

    fun addToCart(product: Product) {
        val userId = TokenManager.getUserId()?.toString() ?: return

        repoScope.launch {
            try {
                val request = AddToCartRequest(
                    userId = userId,
                    productId = product.id,
                    quantity = 1
                )
                val response = api.addToCart(request)
                if (response.isSuccessful) {
                    loadCart()
                }
            } catch (e: Exception) {
                Log.e("CartRepository", "Error agregando al carrito", e)
            }
        }
    }

    fun removeFromCart(product: Product) {
        val userId = TokenManager.getUserId()?.toString() ?: return

        repoScope.launch {
            try {
                val response = api.removeFromCart(userId, product.id)
                if (response.isSuccessful) {
                    loadCart()
                }
            } catch (e: Exception) {
                Log.e("CartRepository", "Error eliminando del carrito", e)
            }
        }
    }

    fun clearCart() {
        val userId = TokenManager.getUserId()?.toString() ?: return

        repoScope.launch {
            try {
                val response = api.clearCart(userId)
                if (response.isSuccessful) {
                    CoroutineScope(Dispatchers.Main).launch {
                        cartItems.clear()
                        _totalPrice.doubleValue = 0.0
                    }
                }
            } catch (e: Exception) {
                Log.e("CartRepository", "Error vaciando carrito", e)
            }
        }
    }
}