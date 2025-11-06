package com.example.marketelectronico.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// Importa los iconos AutoMirrored para RTL (Right-to-Left)
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.marketelectronico.data.model.Product

// --- Datos de muestra (movidos aquí) ---
val sampleRecommendations = listOf(
    Product("1", "CPU Intel Core i7", 250.0, "..."),
    Product("2", "GPU NVIDIA RTX 3080", 700.0, "..."),
    Product("3", "RAM 16GB DDR4", 80.0, "...")
)
val sampleNews = listOf(
    Product("4", "SSD Samsung 980 Pro 1TB", 180.0, "..."),
    Product("5", "Motherboard ASUS ROG", 200.0, "..."),
    Product("6", "Power Supply 750W", 120.0, "...")
)

/**
 * Pantalla Principal (Anteriormente 'TechTradeApp')
 * Ahora es un Composable que recibe el NavController.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // --- CORRECCIÓN 1: Usar mutableIntStateOf para primitivos ---
    var selectedItem by remember { mutableIntStateOf(0) }

    val navItems = listOf("Inicio", "Categorías", "Vender", "Mensajes", "Perfil", "Foro")
    val navIcons = listOf(
        Icons.Default.Home,
        // --- CORRECCIÓN 2: Usar el icono AutoMirrored ---
        Icons.AutoMirrored.Filled.List,
        Icons.Default.AddCircle,
        Icons.Default.Email,
        Icons.Default.Person,
        // --- CORRECCIÓN 3: Reemplazar 'Forum' por 'Chat' (que sí existe) ---
        Icons.Default.Info// (Cambiado de 'Forum' a 'Chat')
    )

    Scaffold(
// ... (el resto del archivo es idéntico) ...
        modifier = modifier,
        topBar = {
// ... (existing code) ...
            TechTradeTopBar(
                onCartClick = { navController.navigate("cart") } // Navega al carrito
            )
        },
        bottomBar = {
// ... (existing code) ...
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
// ... (existing code) ...
                navItems.forEachIndexed { index, label ->
                    NavigationBarItem(
// ... (existing code) ...
                        icon = { Icon(navIcons[index], contentDescription = label, tint = if (selectedItem == index) MaterialTheme.colorScheme.primary else Color.Gray) },
                        label = { Text(label, color = if (selectedItem == index) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 9.sp) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                        // TODO: Conectar esto al NavController (ej. if (label == "Perfil") navController.navigate("profile"))
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
// ... (existing code) ...
        MainScreenContent(
            modifier = Modifier.padding(innerPadding),
            navController = navController
        )
    }
}

// --- Barra Superior ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechTradeTopBar(onCartClick: () -> Unit) {
// ... (existing code) ...
    TopAppBar(
        title = { Text("TechTrade", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
        navigationIcon = {
// ... (existing code) ...
            IconButton(onClick = { /* TODO: Acción Notificaciones */ }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        actions = {
            IconButton(onClick = onCartClick) { // Acción del carrito
// ... (existing code) ...
                Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// --- Contenido de la Pantalla (LazyColumn) ---
@Composable
fun MainScreenContent(modifier: Modifier = Modifier, navController: NavController) {
// ... (existing code) ...
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
// ... (existing code) ...
        item { SearchBar(modifier = Modifier.padding(vertical = 8.dp)) }
        item { SectionTitle("Recomendaciones para ti") }
        item {
            ProductRow(
// ... (existing code) ...
                products = sampleRecommendations,
                onProductClick = { productId ->
                    navController.navigate("product_detail/$productId")
                }
            )
        }
        item { SectionTitle("Novedades") }
        item {
            ProductRow(
// ... (existing code) ...
                products = sampleNews,
                onProductClick = { productId ->
                    navController.navigate("product_detail/$productId")
                }
            )
        }
    }
}

// --- Barra de Búsqueda ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(modifier: Modifier = Modifier) {
// ... (existing code) ...
    var text by remember { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text("Buscar productos", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray) },
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
        colors = TextFieldDefaults.colors(
// ... (existing code) ...
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

// --- Título de Sección ---
@Composable
fun SectionTitle(title: String) {
// ... (existing code) ...
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
    )
}

// --- Fila Horizontal de Productos (LazyRow) ---
@Composable
fun ProductRow(products: List<Product>, onProductClick: (String) -> Unit) {
// ... (existing code) ...
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
// ... (existing code) ...
            ProductCard(
                product = product,
                onClick = { onProductClick(product.id) }
            )
        }
    }
}

// --- Tarjeta de Producto ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
// ... (existing code) ...
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.width(150.dp)
    ) {
        Column {
            Image(
// ... (existing code) ...
                painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Placeholder
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .align(Alignment.CenterHorizontally)
            )
            Column(modifier = Modifier.padding(12.dp)) {
// ... (existing code) ...
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
// ... (existing code) ...
                    text = "$${product.price}",
                    color = MaterialTheme.typography.bodyMedium.color,
                    fontSize = 14.sp
                )
            }
        }
    }
}