package com.example.marketelectronico.data.remote

import com.google.gson.annotations.SerializedName

// --- Modelos Base ---

data class Foro(
    val id: Int, // O String, depende de tu DB (Supabase suele ser Int o UUID)
    val titulo: String,
    val descripcion: String?,
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    @SerializedName("id_creador") val idCreador: Int
)

data class Publicacion(
    val id: Int,
    @SerializedName("id_foro") val idForo: Int,
    @SerializedName("id_usuario") val idUsuario: Int,
    val contenido: String,
    @SerializedName("fecha_publicacion") val fechaPublicacion: String,
    @SerializedName("id_respuesta_a") val idRespuestaA: Int?
)

// --- Respuestas de la API (Wrappers) ---

data class ForosListResponse(
    val ok: Boolean,
    val foros: List<Foro>?,
    val error: String?
)

data class ForoDetailResponse(
    val ok: Boolean,
    val foro: Foro?,
    val error: String?
)

data class PublicacionesListResponse(
    val ok: Boolean,
    val publicaciones: List<Publicacion>?,
    val error: String?
)

data class CreatePublicacionResponse(
    val ok: Boolean,
    val publicacion: Publicacion?,
    val error: String?
)

// --- Requests (Cuerpos de envío) ---

data class CreateForoRequest(
    val titulo: String,
    val descripcion: String
)

data class CreatePublicacionRequest(
    val contenido: String,
    @SerializedName("id_respuesta_a") val idRespuestaA: Int? = null
)