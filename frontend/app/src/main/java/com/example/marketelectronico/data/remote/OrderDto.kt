package com.example.marketelectronico.data.remote

import com.google.gson.annotations.SerializedName

// 1. DTO para la lista de órdenes (GET /api/orders/user/:id)
// Coincide con la tabla 'compras' del backend
data class OrderSummaryDto(
    @SerializedName("id") val id: String,
    @SerializedName("id_usuario") val userId: String,
    @SerializedName("fecha_compra") val fechaCompra: String?,
    @SerializedName("total") val total: Double,
    @SerializedName("estado") val estado: String? // Opcional
)

// 2. DTO para el detalle de items (parte de GET /api/orders/detail/:id)
// Coincide con lo que devuelve 'getOrderById' en tu backend
data class OrderItemDto(
    @SerializedName("id_producto") val idProducto: String,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: Double,
    @SerializedName("nombre_producto") val nombreProducto: String,
    @SerializedName("imagen_producto") val imagenProducto: String?
)

// 3. Respuesta completa del detalle
data class OrderDetailResponse(
    @SerializedName("compra") val compra: OrderSummaryDto,
    @SerializedName("items") val items: List<OrderItemDto>
)