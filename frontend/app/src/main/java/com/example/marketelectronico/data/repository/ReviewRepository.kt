package com.example.marketelectronico.data.repository

import android.util.Log
import com.example.marketelectronico.data.remote.ApiClient
import com.example.marketelectronico.data.remote.CreateReviewRequest
import com.example.marketelectronico.data.remote.ReviewDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.marketelectronico.data.remote.CreateUserReviewRequest

// Mantenemos tu modelo 'Review' para la UI (Mappearemos el DTO a este)
data class Review(
    val id: String,
    val productId: String,
    val productName: String,
    val productImageUrl: String,
    val author: String,
    val authorId: String,
    val authorImageUrl: String? = null,
    val date: Date,
    val rating: Double,
    val comment: String,
    val likedByUserIds: List<String> = emptyList()
) {
    val likesCount: Int get() = likedByUserIds.size
}

object ReviewRepository {
    private val api = ApiClient.reviewApi

    // Función auxiliar para convertir DTO -> Modelo UI
    private fun mapDtoToReview(dto: ReviewDto): Review {
        // Parsear fecha ISO 8601
        val date = try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.parse(dto.date) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        return Review(
            id = dto.id,
            productId = dto.productId,
            productName = dto.productName,
            productImageUrl = dto.productImageUrl ?: "",
            author = dto.author,
            authorId = dto.authorId,
            authorImageUrl = dto.authorImageUrl,
            date = date,
            rating = dto.rating,
            comment = dto.comment,
            likedByUserIds = dto.likedByUserIds
        )
    }

    // 1. Obtener reseñas de un producto (ASÍNCRONO)
    suspend fun getReviewsForProduct(productId: String): List<Review> {
        return try {
            val response = api.getReviewsByProduct(productId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { mapDtoToReview(it) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error fetching product reviews", e)
            emptyList()
        }
    }

    // 2. Obtener historial del usuario (ASÍNCRONO)
    suspend fun getReviewsByUser(userId: String): List<Review> {
        return try {
            val response = api.getUserReviewHistory(userId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { mapDtoToReview(it) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error fetching user history", e)
            emptyList()
        }
    }

    // 3. Crear reseña (ASÍNCRONO)
    suspend fun addReview(productId: String, userId: String, rating: Double, comment: String): Boolean { // <--- rating: Double
        return try {
            val request = CreateReviewRequest(productId, userId, rating, comment)
            val response = api.addReview(request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error creating review", e)
            false
        }
    }

    // Auxiliar para formatear fecha visualmente (ej. "Hace 2 días")
    fun formatDate(date: Date): String {
        val diff = Date().time - date.time
        val days = diff / (1000 * 60 * 60 * 24)
        return when {
            days == 0L -> "Hoy"
            days == 1L -> "Ayer"
            days < 30 -> "Hace $days días"
            else -> "Hace meses"
        }
    }


    suspend fun hasUserReviewedProduct(productId: String, userId: String): Boolean {
        // Obtenemos las reseñas frescas y verificamos
        val reviews = getReviewsForProduct(productId)
        return reviews.any { it.authorId == userId }
    }

    suspend fun toggleLike(reviewId: String, userId: String): Boolean {
        return try {
            // El backend espera un JSON { "userId": "..." }
            val body = mapOf("userId" to userId)
            val response = api.toggleLike(reviewId, body)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error toggleLike", e)
            false
        }
    }

    suspend fun deleteReview(reviewId: String, userId: String): Boolean {
        return try {
            // El backend espera un JSON { "userId": "..." } para verificar dueño
            val body = mapOf("userId" to userId)
            val response = api.deleteReview(reviewId, body)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error deleteReview", e)
            false
        }
    }

    suspend fun updateReview(reviewId: String, userId: String, comment: String, rating: Double): Boolean {
        return try {
            val request = com.example.marketelectronico.data.remote.UpdateReviewRequest(
                userId = userId,
                rating = rating,
                comment = comment
            )
            val response = api.updateReview(reviewId, request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error updateReview", e)
            false
        }
    }

    suspend fun addUserReview(authorId: String, targetUserId: String, rating: Double, comment: String): Boolean {
        return try {
            val request = CreateUserReviewRequest(authorId, targetUserId, rating, comment)
            val response = api.addUserReview(request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error creando reseña de usuario", e)
            false
        }
    }

    suspend fun getUserReviews(userId: String): List<Review> {
        return try {
            val response = api.getUserReviews(userId)
            if (response.isSuccessful && response.body() != null) {
                // Mapeamos el DTO de usuario al modelo Review genérico de la UI
                response.body()!!.map { dto ->
                    Review(
                        id = dto.id,
                        productId = "", // No aplica
                        productName = "Vendedor", // Indicativo
                        productImageUrl = "",
                        author = dto.authorName,
                        authorId = dto.authorId,
                        authorImageUrl = dto.authorPhoto,
                        date = try {
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dto.date) ?: Date()
                        } catch (e: Exception) { Date() },
                        rating = dto.rating,
                        comment = dto.comment,
                        likedByUserIds = emptyList()
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error obteniendo reseñas de usuario", e)
            emptyList()
        }
    }

    suspend fun deleteUserReview(reviewId: String, userId: String): Boolean {
        return try {
            // Ya no creamos un mapa/body, pasamos el String directo
            val response = api.deleteUserReview(reviewId, userId)

            if (!response.isSuccessful) {
                Log.e("ReviewRepo", "Fallo al borrar: ${response.code()} - ${response.errorBody()?.string()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Excepción al borrar", e)
            false
        }
    }

    suspend fun updateUserReview(reviewId: String, userId: String, rating: Double, comment: String): Boolean {
        return try {
            val request = com.example.marketelectronico.data.remote.UpdateReviewRequest(
                userId = userId,
                rating = rating,
                comment = comment
            )
            val response = api.updateUserReview(reviewId, request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Error updateUserReview", e)
            false
        }
    }

    suspend fun checkUserReview(authorId: String, targetId: String): Review? {
        return try {
            val response = api.checkUserReview(authorId, targetId)
            if (response.isSuccessful && response.body()?.exists == true) {
                val dto = response.body()!!.review
                // Mapeo rápido manual (o usa tu mapDtoToReview si es compatible)
                if (dto != null) {
                    Review(
                        id = dto.id,
                        productId = "",
                        productName = "Vendedor",
                        productImageUrl = "",
                        author = dto.authorName,
                        authorId = dto.authorId,
                        authorImageUrl = dto.authorPhoto,
                        date = try { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dto.date) ?: Date() } catch (e: Exception) { Date() },
                        rating = dto.rating,
                        comment = dto.comment,
                        likedByUserIds = emptyList()
                    )
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
