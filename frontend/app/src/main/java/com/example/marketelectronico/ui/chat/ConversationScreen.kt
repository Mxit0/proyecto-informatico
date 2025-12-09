package com.example.marketelectronico.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // <-- Importante para inyectar el ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.base.BaseScreen
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import com.example.marketelectronico.data.model.Message
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.marketelectronico.utils.TokenManager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import com.example.marketelectronico.data.model.MessageStatus
import androidx.compose.foundation.lazy.itemsIndexed


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val chatIdStr = navBackStackEntry?.arguments?.getString("chatId")
    val chatId = chatIdStr?.toIntOrNull() ?: return
    val otherUserId = navBackStackEntry?.arguments?.getInt("otherUserId") ?: 0

    val myToken = TokenManager.getToken() ?: ""
    val rawUserId = TokenManager.getUserId()
    val myUserId = rawUserId?.toString()?.toIntOrNull() ?: 0

    LaunchedEffect(chatId) {
        if (myToken.isNotEmpty()) {
            viewModel.initChat(chatId, myUserId, myToken)
            viewModel.loadChatPartner(otherUserId)
        }
    }

    val messages = viewModel.messages
    val partner = viewModel.chatPartner

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = partner?.foto,
                            contentDescription = "Avatar",
                            placeholder = painterResource(id = android.R.drawable.ic_menu_camera),
                            error = painterResource(id = android.R.drawable.ic_menu_camera),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = partner?.nombre_usuario ?: "Cargando...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Verificamos quién está "detrás" en el historial
                        val previousRoute = navController.previousBackStackEntry?.destination?.route

                        // Si venimos de navegar normal (ej. desde chat_list), simplemente volvemos
                        // (Verificamos que no sea 'login' para evitar el problema actual)
                        if (previousRoute != null && previousRoute != "login") {
                            navController.popBackStack()
                        } else {
                            // Si venimos de una NOTIFICACIÓN (o no hay historial),
                            // forzamos la navegación a la lista de chats
                            navController.navigate("chat_list") {
                                // Limpiamos la pila para que 'Atrás' desde la lista no vuelva aquí
                                popUpTo("main") { saveState = true }
                                launchSingleTop = true
                            }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            ChatBottomBar(onSend = { text -> viewModel.sendMessage(text) })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            reverseLayout = true
        ) {
            // LÓGICA DE VISUALIZACIÓN
            // Usamos itemsIndexed para saber la posición.
            // Al usar reversed(), el índice 0 es el mensaje MÁS NUEVO (el de más abajo).
            val reversedList = messages.reversed()

            itemsIndexed(messages) { index, message ->
                // Condición: Mostrar solo si es el último mensaje (index 0) Y es mío.
                // Si el índice 0 es de la otra persona, isSentByMe será false y no se mostrará nada.
                val showStatus = (index == 0 && message.isSentByMe)

                MessageBubble(message = message, showStatus = showStatus)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, showStatus: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isSentByMe) 16.dp else 0.dp,
                bottomEnd = if (message.isSentByMe) 0.dp else 16.dp
            ),
            color = if (message.isSentByMe) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = if (message.isSentByMe) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        }

        if (showStatus) {
            val statusText = when (message.status) {
                MessageStatus.SENDING -> "Enviando..."
                MessageStatus.SENT -> "Enviado"
                MessageStatus.READ -> "Leído"
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatBottomBar(
    modifier: Modifier = Modifier,
    onSend: (String) -> Unit // Callback para enviar
) {
    var text by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text) // Llamamos al callback
                        text = ""    // Limpiamos el campo
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConversationScreenPreview() {
    MarketElectronicoTheme {
        // Nota: La preview puede fallar si intenta conectar el socket real,
        // pero es útil para ver el diseño.
        ConversationScreen(navController = rememberNavController())
    }
}