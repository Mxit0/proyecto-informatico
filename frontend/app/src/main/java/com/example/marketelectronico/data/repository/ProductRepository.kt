package com.example.marketelectronico.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.marketelectronico.data.model.Category
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.remote.CreateProductRequest
import com.example.marketelectronico.data.remote.ProductResponse
import com.example.marketelectronico.data.remote.ProductService
import com.example.marketelectronico.data.remote.UserService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

class ProductRepository {

    private val api = ProductService.api
    // CONSERVADO (de master): Instancia del servicio de usuarios, necesaria para obtener datos del vendedor.
    private val userApi = UserService.api 

    suspend fun getAllProducts(): List<Product> {
        return try {
            // CONSERVADO (de tu stash): Logging más detallado y manejo de errores por producto.
            val response = api.getAllProducts()
            Log.d("ProductRepository", "Productos recibidos del API: ${response.size}")
            
            val mappedProducts = response.map { 
                try {
                    // Usa el mapper simple que no consulta datos del vendedor para no sobrecargar la API (problema N+1).
                    it.toProduct() 
                } catch (e: Exception) {
                    Log.e("ProductRepository", "Error al mapear producto: ${it.id} - ${e.message}", e)
                    null
                }
            }.filterNotNull()
            
            Log.d("ProductRepository", "Productos mapeados exitosamente: ${mappedProducts.size}")
            mappedProducts
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al obtener productos", e)
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProductById(id: String): Product? {
        return try {
            // CONSERVADO (de master): Lógica completa para obtener el producto y enriquecerlo con datos del vendedor.
            // 1. Obtener el producto.
            val productResponse = api.getProductById(id)

            // 2. Preparar variables para datos del vendedor.
            var sellerName = "Vendedor #${productResponse.idUsuario}"
            var sellerPhoto: String? = null
            var sellerReputation = 0.0

            // 3. Intentar obtener datos reales del vendedor.
            try {
                val userResponse = userApi.getUserById(productResponse.idUsuario.toLong())
                if (userResponse.ok && userResponse.user != null) {
                    sellerName = userResponse.user.nombre_usuario
                    sellerPhoto = userResponse.user.foto
                    sellerReputation = userResponse.user.reputacion ?: 0.0
                }
            } catch (e: Exception) {
                // Si falla la API de usuarios, solo logueamos y seguimos.
                Log.e("ProductRepository", "No se pudo cargar info del vendedor: ${e.message}")
            }

            // 4. Mapear usando los datos combinados con el mapper sobrecargado.
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

    // CONSERVADAS (de tu stash): Todas tus funciones para crear y gestionar productos.
suspend fun uploadProductImages(
    productId: String,
    imageUris: List<Uri>,
    context: Context
): Boolean {
    return try {
        val parts = imageUris.map { uri ->
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return@map null

            MultipartBody.Part.createFormData(
                "imagenes", // ← NOMBRE EXACTO QUE ESPERA MULTER
                "image_${System.currentTimeMillis()}.jpg",
                bytes.toRequestBody("image/*".toMediaType())
            )
        }.filterNotNull()

        if (parts.size < 3) return false

        val response = api.uploadProductImages(productId, parts)

        response.urls.isNotEmpty()
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

    suspend fun getAllCategories(): List<Category> {
        return try {
            api.getAllCategories().map { Category(it.id, it.nombre) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProductsByCategory(categoryId: Int): List<Product> {
        return try {
            api.getProductsByCategory(categoryId).map { it.toProduct() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createProduct(
        nombre: String,    precio: Double,
        descripcion: String,
        idUsuario: Long,
        categoria: Int,
        stock: Int = 1
    ): Product? {
        return try {
            val request = CreateProductRequest(
                nombre = nombre,
                precio = precio,
                descripcion = descripcion,
                idUsuario = idUsuario,
                categoria = categoria,
                stock = stock
            )
            val response = api.createProduct(request)
            Log.d("ProductRepository", "Producto creado en API: ${response.id}")

            response.toProduct()
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al crear producto: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }
    
    // CONSERVADA (de master): La función "mapper" sobrecargada y más completa.
    /**
     * Mapper que acepta datos opcionales del vendedor.
     */
    private fun ProductResponse.toProduct(
        resolvedSellerName: String? = null,
        resolvedSellerPhoto: String? = null,
        resolvedSellerRating: Double = 0.0
    ): Product {
        val mainImage = this.imagenes.firstOrNull()?.urlImagen
            ?: "https://placehold.co/300x300/CCCCCC/FFFFFF?text=No+Imagen"

        return Product(
            id = this.id.toString(),
            name = this.nombre,
            price = this.precio,
            imageUrl = mainImage,
            status = this.categoria,
            // --- DATOS DEL VENDEDOR ---
            sellerId = this.idUsuario,
            sellerName = resolvedSellerName ?: "Vendedor #${this.idUsuario}",
            sellerImageUrl = resolvedSellerPhoto,
            sellerRating = resolvedSellerRating,
            sellerReviews = 10, // Dato hardcodeado por ahora (API User no lo devuelve aún).
            // --------------------------
            description = this.descripcion,
            specifications = mapOf(
                "Stock" to this.stock.toString(),
                "Categoría" to this.categoria
            )
        )
    }
}

// CONSERVADA (de tu stash, pero podría ser redundante): Mapper simple.
// Lo mantengo por si `getAllProducts` o `createProduct` lo necesitan explícitamente.
/**
 * Función "Mapper" simple que convierte el DTO al modelo de UI (Product).
 */
private fun ProductResponse.toProduct(): Product {
    val imageUrl = this.imagenes?.firstOrNull()?.urlImagen
        ?: "https://placehold.co/300x300/CCCCCC/FFFFFF?text=No+Imagen"

    val productStatus = this.categoria ?: "Sin categoría"
    val productDescription = this.descripcion ?: "Sin descripción."

    return Product(
        id = this.id.toString(),
        name = this.nombre,
        price = this.precio,
        imageUrl = imageUrl,
        status = productStatus,
        // En este mapper simple, los datos del vendedor son genéricos.
        sellerId = this.idUsuario,
        sellerName = "Vendedor #${this.idUsuario}",
        sellerImageUrl = null,
        sellerRating = 0.0,
        sellerReviews = 0,
        description = productDescription,
        specifications = mapOf(
            "Stock" to this.stock.toString(),
            "Categoría" to productStatus
        )
    )
}
