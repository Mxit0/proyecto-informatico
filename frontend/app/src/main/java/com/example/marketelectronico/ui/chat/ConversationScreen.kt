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

@Composable
fun ConversationScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    // Inyectamos el ViewModel que maneja el Socket
    viewModel: ChatViewModel = viewModel()
) {
    // Observamos la lista de mensajes del ViewModel
    val messages = viewModel.messages

    BaseScreen(
        title = "Chat en Vivo", // Puedes hacerlo dinámico si pasas el nombre del vendedor
        navController = navController,
        modifier = modifier
    ) { padding ->

        Scaffold(
            modifier = Modifier.padding(padding),
            // Pasamos la acción de enviar al BottomBar
            bottomBar = {
                ChatBottomBar(
                    onSend = { text -> viewModel.sendMessage(text) }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                reverseLayout = true // Para que los mensajes nuevos aparezcan abajo
            ) {
                // Usamos la lista dinámica 'messages' en lugar de 'sampleMessages'
                // Invertimos la lista si el reverseLayout es true, o el ViewModel la gestiona
                // (Generalmente con reverseLayout=true, el índice 0 es el último mensaje)
                items(messages.reversed()) { message ->
                    MessageBubble(message = message)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
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