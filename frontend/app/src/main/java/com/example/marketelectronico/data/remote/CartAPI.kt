package com.example.marketelectronico.data.remote

import retrofit2.Response
import retrofit2.http.*

interface CartAPI {

    // Obtener el carrito del usuario
    @GET("api/carro/{userId}")
    suspend fun getCart(@Path("userId") userId: String): Response<CartResponseDto>

    // Agregar item al carrito
    @POST("api/carro")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<Any>

    // Eliminar un item específico
    @DELETE("api/carro/{userId}/item/{productId}")
    suspend fun removeFromCart(
        @Path("userId") userId: String,
        @Path("productId") productId: String
    ): Response<Any>

    // Vaciar todo el carrito
    @DELETE("api/carro/{userId}")
    suspend fun clearCart(@Path("userId") userId: String): Response<Any>
}