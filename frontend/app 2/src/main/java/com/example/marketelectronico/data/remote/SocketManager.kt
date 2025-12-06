package com.example.marketelectronico.data.remote

import android.util.Log
import com.example.marketelectronico.data.model.Message
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

object SocketManager {
    private var socket: Socket? = null
    private const val URL = "http://10.0.2.2:4000" // La misma IP que tu API

    fun init() {
        try {
            // Configuración básica
            val opts = IO.Options()
            opts.forceNew = true
            socket = IO.socket(URL, opts)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun connect() {
        socket?.connect()
        Log.d("SocketManager", "Intentando conectar...")
    }

    fun disconnect() {
        socket?.disconnect()
    }

    // Enviar un mensaje al servidor
    fun sendMessage(message: String, senderId: String) {
        val json = JSONObject()
        json.put("text", message)
        json.put("senderId", senderId)
        // IMPORTANTE: Pregúntale a Joaquín cómo se llama el evento.
        // Por defecto suelo ser "chat message" o "message".
        socket?.emit("chat message", json)
    }

    // Escuchar mensajes nuevos
    fun onMessageReceived(callback: (Message) -> Unit) {
        socket?.on("chat message") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                // Convertimos el JSON a nuestro objeto Message
                val text = data.getString("text")
                val senderId = data.getString("senderId")
                // Aquí asumimos una lógica simple para "isSentByMe"
                // En un app real, compararías senderId con tu ID de usuario actual
                val isMe = senderId == "1" // TODO: Usar ID real del usuario

                val message = Message(
                    id = System.currentTimeMillis().toString(),
                    text = text,
                    isSentByMe = isMe
                )
                callback(message)
            }
        }
    }
}