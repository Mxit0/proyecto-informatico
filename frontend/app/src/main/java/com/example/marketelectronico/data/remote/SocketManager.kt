package com.example.marketelectronico.data.remote

import android.util.Log
import com.example.marketelectronico.data.model.Message
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException
import com.example.marketelectronico.data.model.MessageStatus
import io.socket.client.Ack

object SocketManager {
    private var socket: Socket? = null
    private const val URL = "http://10.0.2.2:3000"

    fun init(token: String) {
        try {
            val opts = IO.Options()
            opts.forceNew = true
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

    fun joinChat(chatId: Int) {
        val json = JSONObject()
        json.put("chatId", chatId)
        socket?.emit("join_chat", json)
    }

    fun sendMessage(chatId: Int, content: String, onAck: (Boolean) -> Unit) {
        val json = JSONObject()
        json.put("chatId", chatId)
        json.put("contenido", content)

        socket?.emit("send_message", json, Ack { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as JSONObject
                val ok = response.optBoolean("ok")
                onAck(ok)
            } else {
                onAck(false)
            }
        })
    }

    fun onMessageReceived(callback: (Message) -> Unit) {
        socket?.on("new_message") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                try {
                    val msg = Message(
                        id = data.optString("id"),
                        text = data.optString("contenido"),
                        isSentByMe = false,
                        senderId = data.optString("id_remitente"),
                        status = MessageStatus.SENT
                    )
                    callback(msg)
                } catch (e: Exception) {
                    Log.e("SocketManager", "Error parsing message", e)
                }
            }
        }
    }

    fun markMessagesAsRead(chatId: Int) {
        val json = JSONObject()
        json.put("chatId", chatId)
        socket?.emit("mark_messages_read", json)
    }

    fun onMessagesReadUpdate(callback: () -> Unit) {
        socket?.on("messages_read_update") {
            callback()
        }
    }
    fun getSocket(): Socket? {
        return socket
    }
}
