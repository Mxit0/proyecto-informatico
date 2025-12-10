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
    @SerializedName("userId") val userId: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("comment") val comment: String
)

data class UserReviewDto(
    val id: String,
    val targetUserId: String, // A quién se reseñó
    val authorId: String,     // Quién escribió
    val authorName: String,
    val authorPhoto: String?,
    val date: String,
    val rating: Double,
    val comment: String
)

data class CreateUserReviewRequest(
    val authorId: String,
    val targetUserId: String,
    val rating: Double,
    val comment: String
)

data class UserRatingResponse(
    val average: Double
)