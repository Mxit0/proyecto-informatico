package com.example.marketelectronico.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PATCH
import retrofit2.http.DELETE
interface ProductApi {
    @GET("api/productos")
    suspend fun getAllProducts(): List<ProductResponse>

    @GET("api/productos/{id}")
    suspend fun getProductById(@Path("id") id: String): ProductResponse

    @GET("api/productos/categorias")
    suspend fun getAllCategories(): List<CategoryResponse>

    @GET("api/productos/categoria/{categoryId}")
    suspend fun getProductsByCategory(@Path("categoryId") categoryId: Int): List<ProductResponse>

    @POST("api/productos")
    suspend fun createProduct(@Body product: CreateProductRequest): ProductResponse

    @GET("api/productos/componentes/categoria/{categoryId}")
    suspend fun getComponentsByCategory(@Path("categoryId") categoryId: Int): List<ComponentResponse>

    @Multipart
    @POST("api/productos/{id}/imagenes")
    suspend fun uploadProductImages(
        @Path("id") productId: String,
        @Part images: List<MultipartBody.Part>
    ): ImageUploadResponse

    @PATCH("api/productos/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body updates: UpdateProductRequest
    ): ProductResponse

    @DELETE("api/productos/{id}")
    suspend fun deleteProduct(@Path("id") id: String): retrofit2.Response<Unit>

    @GET("api/productos/usuario/{userId}")
    suspend fun getProductsByUser(@Path("userId") userId: Long): List<ProductResponse>

    @DELETE("api/productos/imagenes/{id}")
    suspend fun deleteProductImage(@Path("id") imageId: Int): retrofit2.Response<Unit>
}

data class CategoryResponse(
    val id: Int,
    val nombre: String
)

data class ImageUploadResponse(
    val message: String,
    val urls: List<String>
)

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
    val imagenes: List<ImageResponse> = emptyList(),

    @SerializedName("activo")
    val activo: Boolean? = true
)

data class ImageResponse(
    @SerializedName("id_im")
    val id_im: Int,
    @SerializedName("url_imagen")
    val urlImagen: String
)

data class CreateProductRequest(
    val nombre: String,
    val precio: Double,
    val descripcion: String,
    @SerializedName("id_usuario")
    val idUsuario: Long,
    val stock: Int = 1,
    val categoria: Int,
    @SerializedName("id_componente_maestro")
    val idComponenteMaestro: String?
)

data class ComponentResponse(
    val id: String,
    val nombre_componente: String,
    val categoria: Int,
    val especificaciones: Map<String, Any>? = null
)

data class UpdateProductRequest(
    val nombre: String? = null,

    @SerializedName("descripcion")
    val description: String? = null,

    val precio: Double? = null,
    val stock: Int? = null
)

object ProductService {
    val api: ProductApi by lazy {
        ApiClient.retrofit.create(ProductApi::class.java)
    }
}
