package com.example.marketelectronico.data.remote

import com.google.gson.annotations.SerializedName

// --- Modelos de Lectura (Con usuario anidado) ---
data class UsuarioResumen(
    @SerializedName("nombre_usuario") val nombre: String?,
    val foto: String?
)

data class Foro(
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    @SerializedName("id_creador") val idCreador: Int,
    val usuario: UsuarioResumen? = null
)

data class Publicacion(
    val id: Int,
    @SerializedName("id_foro") val idForo: Int,
    @SerializedName("id_usuario") val idUsuario: Int,
    val contenido: String,
    @SerializedName("fecha_publicacion") val fechaPublicacion: String,
    @SerializedName("id_respuesta_a") val idRespuestaA: Int? = null,
    val usuario: UsuarioResumen? = null
)

// --- Respuestas ---
data class ForosListResponse(val ok: Boolean, val foros: List<Foro>?, val error: String?)
data class ForoDetailResponse(val ok: Boolean, val foro: Foro?, val error: String?)
data class PublicacionesListResponse(val ok: Boolean, val publicaciones: List<Publicacion>?, val error: String?)
data class CreatePublicacionResponse(val ok: Boolean, val publicacion: Publicacion?, val error: String?)
data class GenericResponse(val ok: Boolean, val error: String?)

// --- Requests (AQUÍ ESTABA EL ERROR) ---

// Corregido: Ahora acepta el idCreador
data class CreateForoRequest(
    val titulo: String,
    val descripcion: String,
    @SerializedName("id_creador") val idCreador: Int
)

// Corregido: Ahora acepta el idUsuario
data class CreatePublicacionRequest(
    val contenido: String,
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("id_respuesta_a") val idRespuestaA: Int? = null
)

data class UpdateForoRequest(
    val titulo: String,
    val descripcion: String
)

data class UpdatePublicacionRequest(
    val contenido: String
)