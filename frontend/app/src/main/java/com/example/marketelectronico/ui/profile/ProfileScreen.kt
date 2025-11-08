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
import androidx.compose.runtime.getValue
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
import androidx.navigation.compose.currentBackStackEntryAsState // <-- 1. IMPORTAR
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.marketelectronico.data.repository.OrderRepository
import com.example.marketelectronico.data.repository.Order
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.marketelectronico.data.model.allSampleProducts
import com.example.marketelectronico.data.repository.Review
import com.example.marketelectronico.data.repository.ReviewRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // --- LÓGICA DE LA BOTTOM BAR (DINÁMICA) ---
    val navItems = listOf("Inicio", "Categorías", "Vender", "Mensajes", "Perfil", "Foro")
    val navIcons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.List,
        Icons.Default.AddCircle,
        Icons.Default.Email,
        Icons.Default.Person,
        Icons.Default.Chat // Ícono para 'Foro'
    )
    val navRoutes = listOf("main", "categories", "publish", "chat_list", "profile", "forum")
    // --- FIN LÓGICA BOTTOM BAR ---

    Scaffold(
        modifier = modifier,
        topBar = {
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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                // --- 2. OBTENER RUTA ACTUAL ---
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItems.forEachIndexed { index, label ->
                    val route = navRoutes[index]
                    // --- 3. 'selected' AHORA ES DINÁMICO ---
                    val selected = currentRoute == route

                    NavigationBarItem(
                        icon = { Icon(navIcons[index], contentDescription = label, tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray) },
                        label = { Text(label, color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 9.sp) },
                        selected = selected, // <-- Usa el valor dinámico

                        // --- 4. LÓGICA DE NAVEGACIÓN CORREGIDA ---
                        onClick = {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserInfoSection()
            ProfileTabs(navController = navController)
        }
    }
}

// --- PÁGINAS DE CONTENIDO (Sin cambios) ---
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
    val orders = OrderRepository.orders

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Historial de Compras", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (orders.isEmpty()) {
            Text("Aún no tienes compras.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    OrderHistoryItem(order = order)
                }
            }
        }
    }
}
@Composable
private fun ReviewsHistoryPage() {
    // El autor "Asu" está hardcodeado, coincide con el autor en ReviewScreen
    val myReviews = ReviewRepository.getReviewsByUser("Asu")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Reviews que has Escrito", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (myReviews.isEmpty()) {
            Text("Aún no has escrito reviews.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myReviews) { review ->
                    MyReviewItem(review = review)
                }
            }
        }
    }
}

@Composable
private fun MyReviewItem(review: Review) {
    // Buscar el producto asociado a esta reseña
    val product = allSampleProducts.find { it.id == review.productId }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (product != null) {
                Text(
                    text = "Reseña para: ${product.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Usamos el RatingBar de ProductReviewScreen (si lo hiciste público)
            // o creamos uno simple aquí:
            Row(verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if (star <= review.rating) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = ReviewRepository.formatDate(review.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {

    MarketElectronicoTheme {
        ProfileScreen(navController = rememberNavController())
    }
}
@Composable
private fun OrderHistoryItem(order: Order) {
    // Formateador de fecha
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${order.totalAmount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = dateFormatter.format(order.date),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Mostrar los productos en esta orden
            order.items.forEach { product ->
                Row(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text("• ") // Viñeta
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}