package com.example.marketelectronico.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

// --- 1. Interfaz ---
interface ProductApi {
    // GET /productos
    @GET("api/productos")
    suspend fun getAllProducts(): List<ProductResponse> // <-- Esta respuesta ahora contendrá imágenes

    // GET /productos/:id
    @GET("api/productos/{id}")
    suspend fun getProductById(@Path("id") id: String): ProductResponse // <-- Esta respuesta ahora contendrá imágenes

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
    val categoria: String,
    @SerializedName("id_usuario")
    val idUsuario: Int,

    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
    // Le decimos a GSON que el backend enviará "producto_imagenes"
    @SerializedName("producto_imagenes")
    val imagenes: List<ImageResponse> // <-- ¡La lista de imágenes está de vuelta!
)

// Coincide con la función 'getProductImages' y el join manual
data class ImageResponse(
    @SerializedName("id_im") // <-- El nombre correcto
    val id_im: Int,
    @SerializedName("url_imagen")
    val urlImagen: String
)

// --- 3. Servicio ---
object ProductService {
    val api: ProductApi by lazy {
        ApiClient.retrofit.create(ProductApi::class.java)
    }
}