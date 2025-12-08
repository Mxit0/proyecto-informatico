package com.example.marketelectronico.data.repository

import android.util.Log
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.remote.ApiClient
import com.example.marketelectronico.utils.TokenManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.marketelectronico.data.remote.CreateOrderRequest

// Tu modelo de UI existente
data class Order(
    val id: String,
    val userId: String,
    val items: List<Product>, // En el historial vendrá vacío por eficiencia
    val date: Date,
    val totalAmount: Double
)

object OrderRepository {
    private val api = ApiClient.orderApi

    // Función para parsear la fecha de Postgres/Node a Date de Java
    private fun parseDate(dateString: String): Date {
        return try {
            // Ajusta este formato según cómo venga de tu backend (ej. ISO 8601)
            // Supabase suele enviar: "2025-12-06T23:47:18.930878"
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    /**
     * Obtiene el historial de órdenes desde el Backend
     */
    suspend fun getUserOrders(): List<Order> {
        val userId = TokenManager.getUserId()?.toString() ?: return emptyList()

        return try {
            val response = api.getOrdersByUser(userId)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!

                // Convertimos DTO a tu modelo Order
                dtos.map { dto ->
                    Order(
                        id = dto.id,
                        userId = dto.userId,
                        items = emptyList(), // El endpoint de lista NO trae items
                        date = parseDate(dto.fechaCompra),
                        totalAmount = dto.total
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error fetching orders", e)
            emptyList()
        }
    }

    suspend fun getOrderById(orderId: String): Order? {
        return try {
            val response = api.getOrderDetail(orderId)
            if (response.isSuccessful && response.body() != null) {
                val detailDto = response.body()!!

                // Mapeamos los items del DTO a tu modelo Product
                val productList = detailDto.items.map { itemDto ->
                    Product(
                        id = itemDto.idProducto,
                        name = itemDto.nombreProducto,
                        price = itemDto.precioUnitario,
                        imageUrl = itemDto.imagenProducto ?: "",
                        status = "Comprado",
                        sellerId = 0, sellerName = "", sellerRating = 0.0, sellerReviews = 0, description = "", specifications = emptyMap()
                    )
                }

                Order(
                    id = detailDto.compra.id,
                    userId = detailDto.compra.userId,
                    items = productList, // ¡Aquí sí tenemos los productos!
                    date = parseDate(detailDto.compra.fechaCompra),
                    totalAmount = detailDto.compra.total
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createOrder(userId: String): String? {
        return try {
            val response = api.createOrder(CreateOrderRequest(userId))
            if (response.isSuccessful && response.body() != null) {
                // Devolvemos el ID REAL que creó la base de datos
                response.body()!!.orderId
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}