package com.example.marketelectronico.data.remote
import com.google.gson.annotations.SerializedName

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Body

data class CreateOrderResponse(
    @SerializedName("message") val message: String,
    @SerializedName("orderId") val orderId: String
)

data class CreateOrderRequest(
    @SerializedName("userId") val userId: String
)
interface OrderAPI {

    // Obtener historial del usuario
    @GET("api/orders/user/{userId}")
    suspend fun getOrdersByUser(@Path("userId") userId: String): Response<List<OrderSummaryDto>>

    // Obtener detalle de una orden específica
    @GET("api/orders/detail/{orderId}")
    suspend fun getOrderDetail(@Path("orderId") orderId: String): Response<OrderDetailResponse>

    @POST("api/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<CreateOrderResponse>
}