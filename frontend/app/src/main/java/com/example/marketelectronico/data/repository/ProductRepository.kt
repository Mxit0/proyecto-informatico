package com.example.marketelectronico.data.repository

import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.remote.ProductResponse
import com.example.marketelectronico.data.remote.ProductService

/**
 * Repositorio de Frontend para obtener productos.
 * Llama a la API (ProductService) y mapea los datos al modelo de UI (Product).
 */
class ProductRepository {

    private val api = ProductService.api

    suspend fun getAllProducts(): List<Product> {
        return try {
            api.getAllProducts().map { it.toProduct() }
        } catch (e: Exception) {
            e.printStackTrace() // Revisa el Logcat aquí si sigue fallando
            emptyList()
        }
    }

    suspend fun getProductById(id: String): Product? {
        return try {
            api.getProductById(id).toProduct()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Función "Mapper" que convierte el DTO de red (ProductResponse)
 * al modelo de UI que tu app ya usa (Product).
 */
private fun ProductResponse.toProduct(): Product {
    return Product(
        id = this.id.toString(),
        name = this.nombre,
        price = this.precio,

        // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
        // Asignamos un placeholder fijo ya que no recibimos imágenes
        imageUrl = "https://placehold.co/300x300/CCCCCC/FFFFFF?text=No+Imagen",
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