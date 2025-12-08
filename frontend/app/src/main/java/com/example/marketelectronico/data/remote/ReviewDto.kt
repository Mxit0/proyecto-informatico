package com.example.marketelectronico.data.remote

import com.google.gson.annotations.SerializedName

// Lo que recibimos del Backend (coincide con reviewRepository.js)
data class ReviewDto(
    val id: String,
    val productId: String,
    val productName: String,
    val productImageUrl: String?,
    val author: String,
    val authorId: String,
    val authorImageUrl: String?,
    val date: String, // Viene como String ISO
    val rating: Double,
    val comment: String,
    val likedByUserIds: List<String> = emptyList()
)

// Lo que enviamos para CREAR una reseña
data class CreateReviewRequest(
    val productId: String,
    val userId: String,
    val rating: Double,
    val comment: String
)

data class UpdateReviewRequest(
    val userId: String,
    val rating: Double,
    val comment: String
)