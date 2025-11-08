package com.example.marketelectronico.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
// No usamos BaseScreen, implementamos el Scaffold completo
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // --- LÓGICA DE LA BOTTOM BAR ---
    val selectedItem = 4 // 4 = "Perfil"
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
            // TopBar con flecha de "Atrás"
            TopAppBar(
                title = { Text("Mi Perfil") },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding), // Usa el padding del Scaffold
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserInfoSection()
            ProfileTabs(navController = navController) // Pasa el NavController para el Logout
        }
    }
}

// --- 1. SECCIÓN DE INFO (Foto, Nombre, Email) ---
@Composable
private fun UserInfoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = android.R.drawable.ic_menu_camera), // Placeholder
            contentDescription = "Foto de Perfil",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Asu", // Mock data
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "asu@mail.com", // Mock data
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// --- 2. PESTAÑAS Y CONTENIDO DESLIZABLE ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileTabs(navController: NavController) {
    val pagerState = rememberPagerState { 3 } // 3 pestañas
    val coroutineScope = rememberCoroutineScope()
    val tabTitles = listOf("Mi Nota", "Compras", "Reviews")

    Column(modifier = Modifier.fillMaxHeight()) {

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f) // Ocupa el espacio intermedio
        ) { pageIndex ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (pageIndex) {
                    0 -> MyRatingPage()
                    1 -> PurchasesHistoryPage()
                    2 -> ReviewsHistoryPage()
                }
            }
        }

        Button(
            onClick = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerrar Sesión")
        }
    }
}

@Composable
private fun MyRatingPage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tu Reputación como Vendedor", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("4.8", style = MaterialTheme.typography.headlineLarge)
        }
        Text("(120 Reviews)", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PurchasesHistoryPage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Historial de Compras", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Aún no tienes compras.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ReviewsHistoryPage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Reviews que has Escrito", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Aún no has escrito reviews.", style = MaterialTheme.typography.bodyMedium)
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MarketElectronicoTheme {
        ProfileScreen(navController = rememberNavController())
    }
}