package com.example.marketelectronico.data.repository

import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.remote.ImageResponse
import com.example.marketelectronico.data.remote.ProductResponse
import com.example.marketelectronico.data.remote.ProductService

class ProductRepository {

    private val api = ProductService.api

    suspend fun getAllProducts(): List<Product> {
        return try {
            // --- ¡MÁS SIMPLE! ---
            // Llama a GET /productos y el backend ya incluye las imágenes
            api.getAllProducts().map { it.toProduct() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProductById(id: String): Product? {
        return try {
            // --- ¡MÁS SIMPLE! ---
            // Llama a GET /productos/:id y el backend ya incluye las imágenes
            api.getProductById(id).toProduct()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Función "Mapper" que convierte el DTO
 * al modelo de UI (Product).
 * Ahora SIEMPRE espera una respuesta con imágenes.
 */
private fun ProductResponse.toProduct(): Product {
    return Product(
        id = this.id.toString(),
        name = this.nombre,
        price = this.precio,

        // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
        // Usa la primera imagen si existe, si no, un placeholder
        imageUrl = this.imagenes.firstOrNull()?.urlImagen ?: "https://placehold.co/300x300/CCCCCC/FFFFFF?text=No+Imagen",
        // ----------------------------

        status = this.categoria,
        sellerName = "Vendedor #${this.idUsuario}",
        sellerRating = 4.5,
        sellerReviews = 10,
        description = this.descripcion,
        specifications = mapOf(
            "Stock" to this.stock.toString(),
            "Categoría" to this.categoria
        )
    )
}