package com.example.marketelectronico.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.marketelectronico.MainActivity 
import com.example.marketelectronico.data.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class MyFirebaseMessagingService : FirebaseMessagingService() {

    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            UserRepository.getInstance().updateFcmToken(token)
        }
    }

    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM_DEBUG", "¡Mensaje recibido del servidor! Payload: ${remoteMessage.data}")

        val title = remoteMessage.notification?.title ?: "Nuevo mensaje"
        val body = remoteMessage.notification?.body ?: ""

        
        val chatId = remoteMessage.data["chatId"]
        val remitenteId = remoteMessage.data["remitenteId"]

        showNotification(title, body, chatId, remitenteId)
    }

    private fun showNotification(title: String, body: String, chatId: String?, remitenteId: String?) {
        val channelId = "chat_channel"

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("chatId", chatId)
            putExtra("otherUserId", remitenteId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email) 
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "Mensajes de Chat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true) 
                enableLights(true)
            }
            manager.createNotificationChannel(channel)
        }

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}