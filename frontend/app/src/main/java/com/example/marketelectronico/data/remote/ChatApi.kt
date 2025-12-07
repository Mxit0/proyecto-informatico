package com.example.marketelectronico.data.remote

import retrofit2.Response
import retrofit2.http.*

// Modelos de datos para la respuesta (Data Transfer Objects)
data class ChatResponse(val ok: Boolean, val chat: ChatDto?)
data class MessagesResponse(val ok: Boolean, val mensajes: List<MessageDto>)
data class ChatDto(val id: Int, val id_usuario1: Int, val id_usuario2: Int)
// En ChatApi.kt
data class MessageDto(val id: Int, val id_remitente: Int, val contenido: String, val leido: Boolean)

data class ChatListResponse(val ok: Boolean, val chats: List<ChatDto>)

interface ChatApi {
    // Obtener historial (coincide con chatRoutes.js)
    @GET("api/chat")
    suspend fun getMyChats(): Response<ChatListResponse>
    @GET("api/chat/{chatId}/mensajes")
    suspend fun getMessages(@Path("chatId") chatId: Int): Response<MessagesResponse>

    // Crear o recuperar chat (coincide con chatRoutes.js)
    @POST("api/chat/with-user/{userId}")
    suspend fun createChat(@Path("userId") userId: Int): Response<ChatResponse>
}