package com.example.marketelectronico.data.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val status: String,

    // Datos del vendedor
    val sellerId: Int,
    val sellerName: String,
    val sellerImageUrl: String?,
    val sellerRating: Double,
    val sellerReviews: Int,

    // Detalles del producto
    val description: String,
    val specifications: Map<String, String>,

    // 👇 Primer paso para soportar varias imágenes / miniaturas
    val imageUrls: List<String> = emptyList()
)
