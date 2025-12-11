package com.example.marketelectronico.data.remote

import retrofit2.Response
import retrofit2.http.*
import com.google.gson.annotations.SerializedName

// Modelos de datos para la respuesta (Data Transfer Objects)
data class ChatResponse(val ok: Boolean, val chat: ChatDto?)
data class MessagesResponse(val ok: Boolean, val mensajes: List<MessageDto>)
data class ChatDto(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val otherUserId: Int,
    val lastMessage: String,
    val lastMessageDate: String?
)
// En ChatApi.kt
data class MessageDto(val id: Int, val id_remitente: Int, val contenido: String, val leido: Boolean)

data class ChatListResponse(val ok: Boolean, val chats: List<ChatDto>)

interface ChatApi {
    // Obtener historial (trae la lista enriquecida)
    @GET("api/chat")
    suspend fun getMyChats(): Response<ChatListResponse>

    @GET("api/chat/{chatId}/mensajes")
    suspend fun getMessages(@Path("chatId") chatId: Int): Response<MessagesResponse>

    // Crear o recuperar chat
    @POST("api/chat/with-user/{userId}")
    suspend fun createChat(@Path("userId") userId: Int): Response<ChatResponse>
}