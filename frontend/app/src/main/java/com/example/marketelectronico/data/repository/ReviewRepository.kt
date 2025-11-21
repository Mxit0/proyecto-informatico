package com.example.marketelectronico.data.repository

import androidx.compose.runtime.mutableStateListOf
import java.util.Date
import java.util.UUID

/**
 * Modelo de datos para una Reseña.
 * AHORA INCLUYE el nombre y la imagen del producto para facilitar su visualización en el perfil.
 */
data class Review(
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val productName: String, // <-- NUEVO CAMPO
    val productImageUrl: String, // <-- NUEVO CAMPO
    val author: String,
    val date: Date = Date(),
    val rating: Double,
    val comment: String
)

/**
 * Repositorio (Singleton) para gestionar todas las reseñas.
 */
object ReviewRepository {

    // Datos de muestra actualizados con los nuevos campos
    private val sampleReviews = listOf(
        Review(
            productId = "1",
            productName = "CPU Intel Core i7",
            productImageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=CPU",
            author = "Liam Carter",
            date = Date(System.currentTimeMillis() - 1209600000),
            rating = 5.0,
            comment = "This processor is a game-changer!"
        ),
        Review(
            productId = "1",
            productName = "CPU Intel Core i7",
            productImageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=CPU",
            author = "Sophia Bennett",
            date = Date(System.currentTimeMillis() - 2592000000),
            rating = 3.5,
            comment = "Works well but had installation issues."
        )
    )

    val allReviews = mutableStateListOf<Review>()

    init {
        allReviews.addAll(sampleReviews)
    }

    fun addReview(review: Review) {
        allReviews.add(0, review)
    }

    fun getReviewsForProduct(productId: String?): List<Review> {
        return allReviews.filter { it.productId == productId }.sortedByDescending { it.date }
    }

    fun getReviewsByUser(authorName: String): List<Review> {
        return allReviews.filter { it.author == authorName }.sortedByDescending { it.date }
    }

    fun formatDate(date: Date): String {
        val diff = Date().time - date.time
        val days = diff / (1000 * 60 * 60 * 24)
        return when {
            days == 0L -> "Just now"
            days == 1L -> "1 day ago"
            days < 30 -> "$days days ago"
            else -> "Months ago"
        }
    }
}
