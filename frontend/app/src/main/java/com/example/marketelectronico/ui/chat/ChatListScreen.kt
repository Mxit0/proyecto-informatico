package com.example.marketelectronico.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState // <-- 1. IMPORTAR
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import com.example.marketelectronico.data.model.ChatPreview
import com.example.marketelectronico.data.model.sampleChats
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel = viewModel()
) {
    LaunchedEffect(true) {
        viewModel.loadChats()
    }
    val chatList = viewModel.chats
    // --- LÓGICA DE LA BOTTOM BAR (DINÁMICA) ---
    val navItems = listOf("Inicio", "Categorías", "Vender", "Mensajes", "Perfil", "Foro")
    val navIcons = listOf(Icons.Default.Home, Icons.AutoMirrored.Filled.List, Icons.Default.AddCircle, Icons.Default.Email, Icons.Default.Person, Icons.Default.Info)
    val navRoutes = listOf("main", "categories", "publish", "chat_list", "profile", "forum")
    // --- FIN LÓGICA BOTTOM BAR ---

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mis Mensajes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                navItems.forEachIndexed { index, label ->
                    val route = navRoutes[index]
                    val selected = currentRoute == route
                    NavigationBarItem(
                        icon = { Icon(navIcons[index], contentDescription = label, tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray) },
                        label = { Text(label, color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 9.sp) },
                        selected = selected,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        if (chatList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tienes chats activos", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // USAMOS LA LISTA REAL (chatList) EN VEZ DE sampleChats
                items(chatList) { chat ->
                    ChatListItem(
                        chat = chat,
                        onClick = {
                            // AHORA PASAMOS TAMBIÉN EL ID DEL OTRO USUARIO
                            navController.navigate("conversation/${chat.id}/${chat.otherUserId}")
                        }
                    )
                }
            }
        }
    }
}

// --- Ítem de Lista (Sin cambios) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListItem(
    chat: ChatPreview,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // FOTO DE PERFIL
            AsyncImage(
                model = chat.photoUrl,
                contentDescription = "Foto de ${chat.name}",
                placeholder = painterResource(id = android.R.drawable.ic_menu_camera),
                error = painterResource(id = android.R.drawable.ic_menu_camera),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // COLUMNA CENTRAL (Nombre y Mensaje)
            Column(modifier = Modifier.weight(1f)) {

                // FILA SUPERIOR: Nombre + Hora
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // --- AQUÍ MOSTRAMOS LA HORA ---
                    Text(
                        text = formatChatTime(chat.lastMessageDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                // FILA INFERIOR: Último mensaje
                Text(
                    text = chat.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun formatChatTime(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""

    try {
        // Formato de entrada (Lo que viene de Supabase/Backend)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        // inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Descomentar si la hora sale corrida

        val date = inputFormat.parse(dateString) ?: return ""
        val now = Date()

        // Lógica simple: Si es hoy, mostrar hora. Si no, mostrar fecha.
        val diff = now.time - date.time
        val oneDay = 24 * 60 * 60 * 1000

        return if (diff < oneDay && date.date == now.date) {
            // Es hoy: Mostrar hora (ej: 15:30)
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        } else {
            // Es otro día: Mostrar fecha (ej: 08/12)
            SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        return ""
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatListScreenPreview() {
    MarketElectronicoTheme {
        ChatListScreen(navController = rememberNavController())
    }
}