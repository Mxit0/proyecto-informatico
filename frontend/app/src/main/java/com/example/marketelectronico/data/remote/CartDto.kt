package com.example.marketelectronico.data.remote

import com.google.gson.annotations.SerializedName

// Lo que responde el backend al hacer GET /api/carro/:userId
data class CartResponseDto(
    @SerializedName("cart_id") val cartId: String,
    @SerializedName("items") val items: List<CartItemDto>,
    @SerializedName("total") val total: Double
)

// Los items individuales dentro del carrito
data class CartItemDto(
    @SerializedName("id_item_lista") val idItemLista: String,
    @SerializedName("id_producto") val idProducto: String,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("precio_unitario") val precioUnitario: Double,
    @SerializedName("stock_disponible") val stock: Int,
    @SerializedName("imagen") val imagenUrl: String?,
    @SerializedName("subtotal") val subtotal: Double
)

// Lo que enviamos al hacer POST /api/carro
data class AddToCartRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int
)