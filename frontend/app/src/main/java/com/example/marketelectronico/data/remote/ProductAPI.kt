package com.example.marketelectronico.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.POST

// --- 1. Interfaz ---
interface ProductApi {
    // GET /productos
    @GET("productos")
    suspend fun getAllProducts(): List<ProductResponse> // <-- Esta respuesta ahora contendrá imágenes

    // GET /productos/:id
    @GET("productos/{id}")
    suspend fun getProductById(@Path("id") id: String): ProductResponse // <-- Esta respuesta ahora contendrá imágenes

    // GET /productos/categorias -> devuelve { ok: true, categories: ["cat1", ...] }
    @GET("productos/categorias")
    suspend fun getCategories(): CategoriesResponse

    // Crear producto
    @POST("productos")
    suspend fun createProduct(@Body request: CreateProductRequest): ProductResponse

    // --- Esta ruta ya no es necesaria para la lista/detalle ---
    // GET /productos/:id/imagenes
    // suspend fun getProductImages(@Path("id") id: String): List<ImageResponse>
}

// --- 2. DTOs (Data Transfer Objects) ---
data class ProductResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val categoria: Int,
    @SerializedName("id_usuario")
    val idUsuario: Int,

    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
    // Le decimos a GSON que el backend enviará "producto_imagenes"
    @SerializedName("producto_imagenes")
    val imagenes: List<ImageResponse>? // <-- Puede venir ausente/null desde el backend
)

// Coincide con la función 'getProductImages' y el join manual
data class ImageResponse(
    @SerializedName("id_im") // <-- El nombre correcto
    val id_im: Int,
    @SerializedName("url_imagen")
    val urlImagen: String
)

// Respuesta para GET /productos/categorias
data class CategoryResponse(
    val id: Int,
    val nombre: String
)

data class CategoriesResponse(
    val ok: Boolean,
    val categories: List<CategoryResponse>
)

data class CreateProductRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val id_usuario: Int,
    val stock: Int,
    val categoria: Int
)

// --- 3. Servicio ---
object ProductService {
    val api: ProductApi by lazy {
        ApiClient.retrofit.create(ProductApi::class.java)
    }
}