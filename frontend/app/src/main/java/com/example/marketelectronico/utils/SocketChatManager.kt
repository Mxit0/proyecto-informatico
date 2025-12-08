package com.example.marketelectronico.utils

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketChatManager {

    private lateinit var socket: Socket
    private const val TAG = "SocketChat"

    fun connect() {
        val token = TokenManager.getToken()

        if (token == null) {
            Log.e(TAG, "No token disponible para conectar el socket")
            return
        }

        val opts = IO.Options().apply {
            extraHeaders = mapOf(
                "Authorization" to listOf("Bearer $token")
            )
        }

        socket = IO.socket("http://10.0.2.2:3000", opts) // Cambiar dominio si es necesario
        socket.connect()

        socket.on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "Socket conectado correctamente")
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d(TAG, "Socket desconectado")
        }

        socket.on("new_message") { args ->
            if (args.isNotEmpty()) {
                val mensaje = args[0] as JSONObject
                Log.d(TAG, "Nuevo mensaje: $mensaje")
            }
        }
    }

    fun openChatWithUser(otherUserId: Long, callback: (JSONObject?) -> Unit) {
        val payload = JSONObject().apply {
            put("otherUserId", otherUserId)
        }

        socket.emit("open_chat_with_user", payload) { args ->
            if (args.isNotEmpty()) {
                callback(args[0] as? JSONObject)
            } else {
                callback(null)
            }
        }
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
        socket.emit("send_message", payload)
    }

    fun disconnect() {
        socket.disconnect()
    }
}
