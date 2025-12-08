package com.example.marketelectronico.data.repository

import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.remote.ProductResponse
import com.example.marketelectronico.data.remote.ProductService
import com.example.marketelectronico.data.remote.UserService // <-- Importante: Importar UserService
import android.util.Log

class ProductRepository {

    private val api = ProductService.api
    private val userApi = UserService.api // <-- Instancia del servicio de usuarios

    suspend fun getAllProducts(): List<Product> {
        return try {
            // En la lista general, por rendimiento, no cargamos los datos de cada usuario (N+1 problem)
            // Usamos los valores por defecto del mapper.
            api.getAllProducts().map { it.toProduct() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProductById(id: String): Product? {
        return try {
            // 1. Obtener el producto (incluye imágenes del producto)
            val productResponse = api.getProductById(id)

            // 2. Preparar variables para datos del vendedor
            var sellerName = "Vendedor #${productResponse.idUsuario}"
            var sellerPhoto: String? = null
            var sellerReputation = 0.0

            // 3. Intentar obtener datos reales del vendedor
            try {
                val userResponse = userApi.getUserById(productResponse.idUsuario.toLong())
                if (userResponse.ok && userResponse.user != null) {
                    sellerName = userResponse.user.nombre_usuario
                    sellerPhoto = userResponse.user.foto
                    sellerReputation = userResponse.user.reputacion ?: 0.0
                }
            } catch (e: Exception) {
                // Si falla la API de usuarios, solo logueamos y seguimos mostrando el producto
                Log.e("ProductRepository", "No se pudo cargar info del vendedor: ${e.message}")
            }

            // 4. Mapear usando los datos combinados
            productResponse.toProduct(
                resolvedSellerName = sellerName,
                resolvedSellerPhoto = sellerPhoto,
                resolvedSellerRating = sellerReputation
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Mapper actualizado: Ahora acepta parámetros opcionales para los datos del vendedor.
     * Si no se pasan (ej. en getAllProducts), usa valores por defecto.
     */
    private fun ProductResponse.toProduct(
        resolvedSellerName: String? = null,
        resolvedSellerPhoto: String? = null,
        resolvedSellerRating: Double = 0.0
    ): Product {
        // Lógica para la imagen del producto (prioridad: backend > placeholder)
        val mainImage = this.imagenes.firstOrNull()?.urlImagen
            ?: "https://placehold.co/300x300/CCCCCC/FFFFFF?text=No+Imagen"

        return Product(
            id = this.id.toString(),
            name = this.nombre,
            price = this.precio,
            imageUrl = mainImage,
            status = this.categoria, // O mapear stock > 0 a "Disponible"

            // --- DATOS DEL VENDEDOR ---
            sellerId = this.idUsuario,
            sellerName = resolvedSellerName ?: "Vendedor #${this.idUsuario}",
            sellerImageUrl = resolvedSellerPhoto, // <-- Aquí va la foto del vendedor
            sellerRating = resolvedSellerRating,
            sellerReviews = 10, // Dato hardcodeado por ahora (API User no lo devuelve aún)
            // --------------------------

            description = this.descripcion,
            specifications = mapOf(
                "Stock" to this.stock.toString(),
                "Categoría" to this.categoria
            )
        )
    }
}