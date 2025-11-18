package com.example.marketelectronico.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.marketelectronico.data.model.allSampleProducts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Modelo de datos global para una Reseña.
 * Lo movimos aquí desde ProductReviewScreen.kt y le añadimos productId.
 */
data class Review(
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val author: String,
    val date: Date = Date(),
    val rating: Double,
    val comment: String
)

/**
 * Repositorio (Singleton) para gestionar todas las reseñas de productos.
 */
object ReviewRepository {

    // Lista de reseñas de muestra (movida desde ProductReviewScreen)
    private val sampleReviews = listOf(
        Review(productId = "1", author = "Liam Carter", date = Date(System.currentTimeMillis() - 1209600000), rating = 5.0, comment = "This processor is a game-changer! It significantly boosted my computer's performance..."),
        Review(productId = "1", author = "Sophia Bennett", date = Date(System.currentTimeMillis() - 2592000000), rating = 3.5, comment = "The processor works well... however, I encountered some minor issues during installation."),
        Review(productId = "2", author = "Ethan Walker", date = Date(System.currentTimeMillis() - 5184000000), rating = 3.0, comment = "The processor is okay for basic tasks, but it didn't meet my expectations for more demanding applications.")
    )

    // Lista observable de todas las reseñas
    val allReviews = mutableStateListOf<Review>()

    init {
        // Carga las reseñas de muestra al iniciar
        allReviews.addAll(sampleReviews)
    }

    /**
     * Añade una nueva reseña al repositorio.
     */
    fun addReview(review: Review) {
        // La añade al principio de la lista
        allReviews.add(0, review)
    }

    /**
     * Obtiene todas las reseñas para un producto específico.
     */
    fun getReviewsForProduct(productId: String?): List<Review> {
        return allReviews.filter { it.productId == productId }.sortedByDescending { it.date }
    }

    /**
     * Obtiene todas las reseñas escritas por un autor (usuario) específico.
     */
    fun getReviewsByUser(authorName: String): List<Review> {
        return allReviews.filter { it.author == authorName }.sortedByDescending { it.date }
    }

    /**
     * Formatea una fecha para mostrarla (ej. "2 weeks ago")
     * (Esta es una implementación simple, puedes mejorarla)
     */
    fun formatDate(date: Date): String {
        val diff = Date().time - date.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30

        return when {
            months > 0 -> if (months == 1L) "1 month ago" else "$months months ago"
            weeks > 0 -> if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
            days > 0 -> if (days == 1L) "1 day ago" else "$days days ago"
            hours > 0 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
            minutes > 0 -> if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
            else -> "Just now"
        }
    }
}
