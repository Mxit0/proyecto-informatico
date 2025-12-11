package com.example.marketelectronico.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.marketelectronico.data.model.Category
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.remote.CreateProductRequest
import com.example.marketelectronico.data.remote.ProductResponse
import com.example.marketelectronico.data.remote.ProductService
import com.example.marketelectronico.data.remote.ComponentResponse
import com.example.marketelectronico.data.model.ComponenteMaestro
import com.example.marketelectronico.data.remote.UserService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import com.example.marketelectronico.data.remote.UpdateProductRequest
import com.example.marketelectronico.data.model.ProductImage

class ProductRepository {

    private val api = ProductService.api
    private val userApi = UserService.api

    suspend fun getAllProducts(): List<Product> {
        return try {
            val response = api.getAllProducts()
            Log.d("ProductRepository", "Productos recibidos del API: ${response.size}")

            val mappedProducts = response.map {
                try {
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
            val productResponse = api.getProductById(id)

            var sellerName = "Vendedor #${productResponse.idUsuario}"
            var sellerPhoto: String? = null
            var sellerReputation = 0.0
            var sellerReviewsCount = 0

            try {
                val userResponse = userApi.getUserById(productResponse.idUsuario.toLong())
                if (userResponse.ok && userResponse.user != null) {
                    sellerName = userResponse.user.nombre_usuario
                    sellerPhoto = userResponse.user.foto
                    sellerReputation = userResponse.user.reputacion ?: 0.0
                    sellerReviewsCount = userResponse.user.totalResenas ?: 0
                }
            } catch (e: Exception) {
                Log.e("ProductRepository", "No se pudo cargar info del vendedor: ${e.message}")
            }

            productResponse.toProduct(
                resolvedSellerName = sellerName,
                resolvedSellerPhoto = sellerPhoto,
                resolvedSellerRating = sellerReputation,
                resolvedSellerReviews = sellerReviewsCount
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadProductImages(
        productId: String,
        imageUris: List<Uri>,
        context: Context
    ): Boolean {
        return try {
            val parts = imageUris.map { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@map null

                MultipartBody.Part.createFormData(
                    "imagenes",
                    "image_${System.currentTimeMillis()}.jpg",
                    bytes.toRequestBody("image/*".toMediaType())
                )
            }.filterNotNull()

            // VALIDACIÓN: Permitir subir aunque sea 1 foto si es edición.
            // La validación de máximo 10 está en el backend.
            if (parts.isEmpty()) return false

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

    suspend fun getComponentsByCategory(categoryId: Int): List<ComponenteMaestro> {
        return try {
            val response: List<ComponentResponse> = api.getComponentsByCategory(categoryId)
            response.map { cr ->
                ComponenteMaestro(
                    id = cr.id,
                    nombre_componente = cr.nombre_componente,
                    categoria = cr.categoria,
                    especificaciones = cr.especificaciones ?: emptyMap()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createProduct(
        nombre: String,
        precio: Double,
        descripcion: String,
        idUsuario: Long,
        categoria: Int,
        stock: Int = 1,
        idComponenteMaestro: String?
    ): Product? {
        return try {
            val request = CreateProductRequest(
                nombre = nombre,
                precio = precio,
                descripcion = descripcion,
                idUsuario = idUsuario,
                categoria = categoria,
                stock = stock,
                idComponenteMaestro = idComponenteMaestro
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

    private fun ProductResponse.toProduct(
        resolvedSellerName: String? = null,
        resolvedSellerPhoto: String? = null,
        resolvedSellerRating: Double = 0.0,
        resolvedSellerReviews: Int = 0
    ): Product {
        // Mapear la lista de objetos imagen usando ProductImage
        val mappedImages = this.imagenes?.map {
            ProductImage(it.id_im, it.urlImagen)
        } ?: emptyList()

        // Para compatibilidad con código viejo que usa List<String>
        val allImagesUrls = mappedImages.map { it.url }

        val mainImage = allImagesUrls.firstOrNull()
            ?: "https://placehold.co/300x300/CCCCCC/FFFFFF?text=No+Imagen"

        return Product(
            id = this.id.toString(),
            name = this.nombre,
            price = this.precio,
            imageUrl = mainImage,
            status = this.categoria,
            sellerId = this.idUsuario,
            sellerName = resolvedSellerName ?: "Vendedor #${this.idUsuario}",
            sellerImageUrl = resolvedSellerPhoto,
            sellerRating = resolvedSellerRating,
            sellerReviews = resolvedSellerReviews,
            description = this.descripcion,
            specifications = mapOf(
                "Stock" to this.stock.toString(),
                "Categoría" to this.categoria
            ),
            imageUrls = allImagesUrls,
            images = mappedImages, // <--- Nueva lista con IDs
            active = this.activo ?: true
        )
    }

    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            val response = api.deleteProduct(productId)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ProductRepo", "Error borrando: ${e.message}")
            false
        }
    }

    suspend fun updateProduct(productId: String, request: UpdateProductRequest): Boolean {
        return try {
            val response = api.updateProduct(productId, request)
            true
        } catch (e: Exception) {
            Log.e("ProductRepo", "Error actualizando: ${e.message}")
            false
        }
    }

    suspend fun getProductsByUser(userId: Long): List<Product> {
        return try {
            val response = api.getProductsByUser(userId)
            response.map { it.toProduct() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteImage(imageId: Int): Boolean {
        return try {
            val response = api.deleteProductImage(imageId)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e("ProductRepo", "Error borrando imagen: $errorBody")
                throw Exception(errorBody ?: "Error desconocido")
            }
            true
        } catch (e: Exception) {
            throw e
        }
    }
}

// CONSERVADA (de tu stash, pero podría ser redundante): Mapper simple.
// Lo mantengo por si `getAllProducts` o `createProduct` lo necesitan explícitamente.
/**
 * Función "Mapper" simple que convierte el DTO al modelo de UI (Product).
 */
private fun ProductResponse.toProduct(): Product {
    val allImages: List<String> =
        this.imagenes?.mapNotNull { it.urlImagen } ?: emptyList()

    val imageUrl = allImages.firstOrNull()
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
        ),
        imageUrls = allImages,
        active = this.activo ?: true
    )
}
