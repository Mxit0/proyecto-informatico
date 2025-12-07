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
                        // El ViewModel decidirá si "isSentByMe" es true comparando IDs
                        // Aquí lo dejamos en false por defecto o pasamos el senderId
                        isSentByMe = false
                    ).apply {
                        // Es importante guardar el senderId en el modelo Message
                        // para que el ViewModel sepa de quién es.
                        senderId = dto.id_remitente.toString()
                    }
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
            // 1. Obtenemos la lista de chats (IDs)
            val response = api.getMyChats()

            val rawId = TokenManager.getUserId()
            val myId = rawId?.toInt() ?: -1

            if (response.isSuccessful && response.body()?.ok == true) {
                val rawChats = response.body()?.chats ?: emptyList()

                val resultList = mutableListOf<ChatPreview>()

                for (dto in rawChats) {
                    // Calculamos el ID del otro usuario
                    val otherUserId = if (dto.id_usuario1 == myId) dto.id_usuario2 else dto.id_usuario1

                    var displayName = "Usuario #$otherUserId"
                    var photoUrl: String? = null

                    // 2. USAMOS TU UserService EXISTENTE
                    try {
                        // Tu UserApi espera un Long, así que convertimos con .toLong()
                        val userResponse = UserService.api.getUserById(otherUserId.toLong())

                        // Verificamos usando tu estructura UserResponse (campo 'ok' y 'user')
                        if (userResponse.ok && userResponse.user != null) {
                            displayName = userResponse.user.nombre_usuario
                            photoUrl = userResponse.user.foto
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Si falla, se queda con "Usuario #ID" por defecto
                    }

                    resultList.add(
                        ChatPreview(
                            id = dto.id.toString(),
                            name = displayName,
                            lastMessage = "Toca para ver mensajes",
                            timestamp = "",
                            otherUserId = otherUserId,
                            photoUrl = photoUrl
                        )
                    )
                }

                return resultList
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}