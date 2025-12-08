package com.example.marketelectronico.data.repository


import android.util.Log
import com.example.marketelectronico.data.model.Message
import com.example.marketelectronico.data.remote.ChatApi
import com.example.marketelectronico.data.remote.ChatDto
import com.example.marketelectronico.data.model.ChatPreview
import com.example.marketelectronico.data.remote.ChatListResponse
import com.example.marketelectronico.utils.TokenManager
import com.example.marketelectronico.data.remote.UserService
// Asegúrate de tener una instancia de Retrofit accesible, por ejemplo en un objeto ApiClient
// import com.example.marketelectronico.data.remote.ApiClient


class ChatRepository(
    // Idealmente inyectas la API. Si no usas inyección de dependencias,
    // puedes instanciarla aquí directamente o pasarla al construir.
    private val api: ChatApi
) {

    /**
     * Obtiene el historial de mensajes de un chat específico.
     * Retorna una lista vacía si hay error, o la lista de mensajes si es exitoso.
     */
    suspend fun getMessages(chatId: Int): List<Message>? {
        return try {
            val response = api.getMessages(chatId)
            if (response.isSuccessful && response.body()?.ok == true) {
                // Mapeamos de MessageDto (backend) a Message (modelo UI)
                response.body()?.mensajes?.map { dto ->
                    Message(
                        id = dto.id.toString(),
                        text = dto.contenido,
                        isSentByMe = false, // Se ajusta en el ViewModel
                        senderId = dto.id_remitente.toString(),
                        status = if (dto.leido) com.example.marketelectronico.data.model.MessageStatus.READ
                        else com.example.marketelectronico.data.model.MessageStatus.SENT
                    )
                } ?: emptyList()
            } else {
                Log.e("ChatRepository", "Error al obtener mensajes: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Excepción en getMessages", e)
            null
        }
    }

    /**
     * Crea un chat con un usuario (vendedor) o devuelve el existente.
     * Retorna el objeto ChatDto con el ID del chat, o null si falla.
     */
    suspend fun createOrGetChat(otherUserId: Int): ChatDto? {
        return try {
            val response = api.createChat(otherUserId)
            if (response.isSuccessful && response.body()?.ok == true) {
                response.body()?.chat
            } else {
                Log.e("ChatRepository", "Error al crear chat: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Excepción en createOrGetChat", e)
            null
        }
    }
    suspend fun getMyChats(): List<ChatPreview> {
        return try {
            val response = api.getMyChats()

            if (response.isSuccessful && response.body()?.ok == true) {
                // Mapeo directo: DTO del Backend -> Modelo de UI
                response.body()!!.chats.map { dto ->
                    ChatPreview(
                        id = dto.id,
                        name = dto.name,                 // Ya viene del backend
                        photoUrl = dto.photoUrl,         // Ya viene del backend
                        lastMessage = dto.lastMessage,   // Ya viene del backend
                        otherUserId = dto.otherUserId,
                        lastMessageDate = dto.lastMessageDate,
                        timestamp = "" // Campo legacy, no se usa si tenemos lastMessageDate
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}