package com.example.marketelectronico.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- 1. Interfaz ---
interface ProductApi {
    // GET /productos
    @GET("productos")
    suspend fun getAllProducts(): List<ProductResponse>

    // GET /productos/:id
    @GET("productos/{id}")
    suspend fun getProductById(@Path("id") id: String): ProductResponse

    // GET /productos/categorias/todas
    @GET("productos/categorias/todas")
    suspend fun getAllCategories(): List<CategoryResponse>

    // GET /productos/categoria/:categoryId
    @GET("productos/categoria/{categoryId}")
    suspend fun getProductsByCategory(
        @Path("categoryId") categoryId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): List<ProductResponse>
} // The interface ProductApi is correctly closed here.

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
    @SerializedName("producto_imagenes")
    val imagenes: List<ImageResponse>
)

data class ImageResponse(
    @SerializedName("id_im")
    val id_im: Int,
    @SerializedName("url_imagen")
    val urlImagen: String
)

data class CategoryResponse(
    val id: Int,
    val nombre: String
)

// --- 3. Servicio ---
object ProductService {
    val api: ProductApi by lazy {
        // Assuming ApiClient is defined elsewhere and provides a Retrofit instance
        ApiClient.retrofit.create(ProductApi::class.java)
    }
} // <-- FIX: Add this closing brace
