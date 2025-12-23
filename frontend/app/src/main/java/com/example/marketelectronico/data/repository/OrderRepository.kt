package com.example.marketelectronico.data.repository

import android.util.Log
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.remote.ApiClient
import com.example.marketelectronico.utils.TokenManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.marketelectronico.data.remote.CreateOrderRequest

data class Order(
    val id: String,
    val userId: String,
    val items: List<Product>,
    val date: Date,
    val totalAmount: Double
)

object OrderRepository {
    private val api = ApiClient.orderApi

    private fun parseDate(dateString: String?): Date {
        if (dateString.isNullOrEmpty()) return Date()
        return try {
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

                // Mapeo: DTO -> Modelo UI
                dtos.map { dto ->
                    Order(
                        id = dto.id,
                        userId = dto.userId,
                        items = emptyList(),
                        date = parseDate(dto.fechaCompra),
                        totalAmount = dto.total ?: 0.0
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
                val compraDto = detailDto.compra

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
                    id = compraDto.id,
                    userId = compraDto.userId,
                    items = productList,
                    date = parseDate(compraDto.fechaCompra),
                    totalAmount = compraDto.total ?: 0.0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error getting order detail", e)
            null
        }
    }

    suspend fun createOrder(userId: String): String? {
        return try {
            val response = api.createOrder(CreateOrderRequest(userId))
            if (response.isSuccessful && response.body() != null) {
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