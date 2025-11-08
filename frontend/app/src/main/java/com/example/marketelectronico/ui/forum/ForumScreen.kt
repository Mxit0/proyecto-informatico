package com.example.marketelectronico.ui.forum

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
// No usamos BaseScreen
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import com.example.marketelectronico.data.model.ForumThread
import com.example.marketelectronico.data.model.sampleThreads

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // --- LÓGICA DE LA BOTTOM BAR ---
    val selectedItem = 5 // 5 = "Foro"
    val navItems = listOf("Inicio", "Categorías", "Vender", "Mensajes", "Perfil", "Foro")
    val navIcons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.List,
        Icons.Default.AddCircle,
        Icons.Default.Email,
        Icons.Default.Person,
        Icons.Default.Info // Ícono para 'Foro'
    )
    // --- FIN LÓGICA BOTTOM BAR ---

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Foro") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_post") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Hilo")
            }
        },
        bottomBar = {
            // --- IMPLEMENTACIÓN DE LA BOTTOM BAR ---
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navItems.forEachIndexed { index, label ->
                    NavigationBarItem(
                        icon = { Icon(navIcons[index], contentDescription = label, tint = if (selectedItem == index) MaterialTheme.colorScheme.primary else Color.Gray) },
                        label = { Text(label, color = if (selectedItem == index) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 9.sp) },
                        selected = selectedItem == index,
                        onClick = {
                            val route = when (label) {
                                "Inicio" -> "main"
                                "Categorías" -> "categories"
                                "Vender" -> "publish"
                                "Mensajes" -> "chat_list"
                                "Perfil" -> "profile"
                                "Foro" -> "forum"
                                else -> "main"
                            }
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
            // --- FIN BOTTOM BAR ---
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(sampleThreads) { thread ->
                ForumThreadItem(
                    thread = thread,
                    onClick = {
                        navController.navigate("post_detail/${thread.id}")
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForumThreadItem(
    thread: ForumThread,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = thread.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "por ${thread.author}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${thread.replies} respuestas",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForumScreenPreview() {
    MarketElectronicoTheme {
        ForumScreen(navController = rememberNavController())
    }
}