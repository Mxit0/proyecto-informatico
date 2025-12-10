package com.example.marketelectronico.utils

import android.util.Log
import com.example.marketelectronico.data.model.Message
import com.example.marketelectronico.data.model.MessageStatus
import com.example.marketelectronico.utils.TokenManager
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import android.os.Handler
import android.os.Looper

object SocketChatManager {

    private lateinit var socket: Socket
    private const val TAG = "SocketChat"

    // Asegúrate de que este puerto coincida con tu server.js (4000 o 3000)
    private const val SOCKET_URL = "http://10.0.2.2:3000"

    fun connect() {
        val token = TokenManager.getToken()

        if (token == null) {
            Log.e(TAG, "No token disponible para conectar el socket")
            return
        }

        val opts = IO.Options().apply {
            // CORRECCIÓN 1: Usar 'auth' es más directo y compatible con tu server.js
            // que busca socket.handshake.auth.token
            auth = mapOf("token" to token)

            // Si prefieres mantener extraHeaders, descomenta esto, pero auth es mejor:
            // extraHeaders = mapOf("Authorization" to listOf("Bearer $token"))
        }

        try {
            socket = IO.socket(SOCKET_URL, opts)
            socket.connect()

            socket.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket conectado correctamente")
            }

            socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
                // CORRECCIÓN 2: Especificar el tipo de args explícitamente
                val error = if (args.isNotEmpty()) args[0] else "Unknown"
                Log.e(TAG, "Error de conexión: $error")
            }

            socket.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket desconectado")
            }

            socket.on("new_message") { args ->
                // CORRECCIÓN 3: Especificar tipo explícito Array<Any>
                val arguments = args as Array<Any>
                if (arguments.isNotEmpty()) {
                    val mensaje = arguments[0] as JSONObject
                    Log.d(TAG, "Nuevo mensaje recibido: $mensaje")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando socket: ${e.message}")
        }
    }

    fun openChatWithUser(otherUserId: Long, callback: (JSONObject?) -> Unit) {
        val payload = JSONObject().apply {
            put("otherUserId", otherUserId)
        }

        // CORRECCIÓN 4: Usar la interfaz Ack explícita para el callback
        // y especificar el tipo de args (Array<Any>)
        socket.emit("open_chat_with_user", payload, Ack { args ->
            val arguments = args as Array<Any>
            if (arguments.isNotEmpty()) {
                val response = arguments[0] as? JSONObject
                callback(response)
            } else {
                callback(null)
            }
        })
    }

    fun joinChat(chatId: Long) {
        val payload = JSONObject().apply {
            put("chatId", chatId)
        }
        socket.emit("join_chat", payload)
    }

    fun sendMessage(chatId: Long, contenido: String) {
        val payload = JSONObject().apply {
            put("chatId", chatId)
            put("contenido", contenido)
        }
        // Nota: Si quieres confirmar que se envió, agrega un Ack aquí también
        socket.emit("send_message", payload)
    }

    fun disconnect() {
        if (::socket.isInitialized) {
            socket.disconnect()
        }
    }

    // Helper por si lo necesitas en ViewModels
    fun getSocket(): Socket? {
        return if (::socket.isInitialized) socket else null
    }

    fun onMessageReceived(callback: (Message) -> Unit) {
        socket?.on("new_message") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                try {
                    // Mapeamos el JSON del backend (server.js) al objeto Message de Android
                    val message = Message(
                        id = data.optString("id"),
                        text = data.optString("contenido"),
                        senderId = data.optString("id_remitente"),
                        isSentByMe = false, // El ViewModel decidirá esto
                        status = MessageStatus.SENT
                    )

                    // IMPORTANTE: Ejecutar en el Hilo Principal (UI Thread)
                    Handler(Looper.getMainLooper()).post {
                        callback(message)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parseando mensaje", e)
                }
            }
        }
    }

    fun markMessagesAsRead(chatId: Int) {
        val payload = JSONObject().apply { put("chatId", chatId) }
        socket?.emit("mark_messages_read", payload)
    }

    fun onMessagesReadUpdate(callback: () -> Unit) {
        socket?.on("messages_read_update") {
            Handler(Looper.getMainLooper()).post { callback() }
        }
    }
}