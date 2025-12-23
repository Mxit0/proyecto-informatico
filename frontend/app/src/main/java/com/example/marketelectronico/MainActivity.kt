package com.example.marketelectronico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.marketelectronico.ui.navigation.AppNavigation
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import com.example.marketelectronico.utils.TokenManager
import com.example.marketelectronico.data.remote.SocketManager
import android.Manifest
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.example.marketelectronico.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
/**
 * Es el anfitrión de la aplicación de Jetpack Compose.
 * Carga el Tema y el controlador de Navegación.
 *
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:1086510416472:android:8f867a6905fe5c105cc02d") // Tu Mobile SDK App ID
                    .setApiKey("AIzaSyBpdB4Nw07RPHdvrodwo4VKqBwBcyehtA0") // Tu Current Key
                    .setProjectId("console-firebase-d4f08") // Tu Project ID
                    .build()

                FirebaseApp.initializeApp(this, options)
                Log.d("FCM", "Firebase inicializado manualmente con éxito")
            }
        } catch (e: Exception) {
            Log.e("FCM", "Error inicializando Firebase manualmente", e)
        }

        TokenManager.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 99)
        }

        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("FCM", "Token actual: $token")

                CoroutineScope(Dispatchers.IO).launch {
                    UserRepository.getInstance().updateFcmToken(token)
                }
            }
        } catch (e: Exception) {
            Log.e("FCM", "Error obteniendo instancia de Messaging", e)
        }

        val token = TokenManager.getToken() ?: ""
        SocketManager.init(token)

        setContent {
            MarketElectronicoTheme {
                val navController = rememberNavController()

                val chatId = intent.getStringExtra("chatId")
                val otherUserId = intent.getStringExtra("otherUserId") // El backend manda "remitenteId"

                LaunchedEffect(chatId) {
                    if (chatId != null && otherUserId != null) {
                        navController.navigate("conversation/$chatId/$otherUserId")
                    }
                }

                AppNavigation(navController = navController) // Pasamos el controller que creamos
            }
        }
    }
}