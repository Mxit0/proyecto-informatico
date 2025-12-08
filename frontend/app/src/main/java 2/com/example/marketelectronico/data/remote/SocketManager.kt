package com.example.marketelectronico.data.remote

import android.util.Log
import com.example.marketelectronico.data.model.Message
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

object SocketManager {
    private var socket: Socket? = null
    // Usa la IP de tu máquina (10.0.2.2 para emulador)
    private const val URL = "http://10.0.2.2:3000"

    // Inicializar CON el token del usuario (lo obtienes tras el Login)
    fun init(token: String) {
        try {
            val opts = IO.Options()
            opts.forceNew = true
            // ESTO ES CLAVE: Tu server.js busca el token aquí
            opts.auth = mapOf("token" to token)

            socket = IO.socket(URL, opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("SocketManager", "Conectado al servidor")
            }
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("SocketManager", "Error conexión: ${args[0]}")
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun connect() {
        if (socket?.connected() == false) {
            socket?.connect()
        }
    }

    fun disconnect() {
        socket?.disconnect()
    }

    // Unirse a la sala: coincidiendo con server.js "join_chat"
    fun joinChat(chatId: Int) {
        val json = JSONObject()
        json.put("chatId", chatId)
        socket?.emit("join_chat", json)
    }

    // Enviar mensaje: coincidiendo con server.js "send_message"
    fun sendMessage(chatId: Int, content: String) {
        val json = JSONObject()
        json.put("chatId", chatId)
        json.put("contenido", content) // Tu server espera "contenido", no "text"

        // El callback lo manejaremos escuchando "new_message" o confirmación
        socket?.emit("send_message", json)
    }

    // Escuchar: server.js emite "new_message"
    fun onMessageReceived(callback: (Message) -> Unit) {
        socket?.on("new_message") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                try {
                    // Mapeo del JSON de Supabase a tu modelo Message
                    val msg = Message(
                        id = data.optString("id"),
                        text = data.optString("contenido"),
                        // Comparar con ID del usuario actual en el ViewModel
                        isSentByMe = false // Lo ajustaremos en el ViewModel
                    )
                    // Importante: extraemos el senderId para saber de quién es
                    msg.senderId = data.optString("id_remitente")

                    callback(msg)
                } catch (e: Exception) {
                    Log.e("SocketManager", "Error parsing message", e)
                }
            }
        }
    }
}