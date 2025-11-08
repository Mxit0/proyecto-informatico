package com.example.marketelectronico.ui.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination // <-- IMPORTAR
import androidx.navigation.compose.currentBackStackEntryAsState // <-- IMPORTAR
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.repository.CartRepository
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import java.text.NumberFormat
import java.util.Currency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController
) {
    val cartProducts = CartRepository.cartItems
    val totalPrice = CartRepository.totalPrice.value
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // --- LÓGICA DE LA BOTTOM BAR (DINÁMICA) ---
    val navItems = listOf("Inicio", "Categorías", "Vender", "Mensajes", "Perfil", "Foro")
    val navIcons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.List,
        Icons.Default.AddCircle,
        Icons.Default.Email,
        Icons.Default.Person,
        Icons.Default.Info
    )
    val navRoutes = listOf("main", "categories", "publish", "chat_list", "profile", "forum")
    // --- FIN LÓGICA BOTTOM BAR ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                // --- Sección de Total y Botón de Pago ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatCurrency(totalPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { navController.navigate("payment") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = cartProducts.isNotEmpty()
                    ) {
                        Text("Continue to payment")
                    }
                }

                // --- Barra de Navegación Inferior (CORREGIDA) ---
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    // --- OBTENER RUTA ACTUAL ---
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    navItems.forEachIndexed { index, label ->
                        val route = navRoutes[index]
                        // 'selected' es dinámico
                        val selected = currentRoute == route

                        NavigationBarItem(
                            icon = { Icon(navIcons[index], contentDescription = label, tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray) },
                            label = { Text(label, color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 9.sp) },
                            selected = selected,
                            // --- LÓGICA DE NAVEGACIÓN COMPLETA ---
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        productToDelete?.let { product ->
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text(text = "Eliminar Producto") },
                text = { Text(text = "¿Estás seguro de que quieres eliminar \"${product.name}\" de tu carrito?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            CartRepository.removeFromCart(product)
                            productToDelete = null
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { productToDelete = null }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (cartProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tu carrito está vacío",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                item {
                    Text(
                        text = "Items Selected",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(cartProducts) { product ->
                    CartItem(
                        product = product,
                        onRemoveClick = {
                            productToDelete = product
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun CartItem(product: Product, onRemoveClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Placeholder
            contentDescription = product.name,
            modifier = Modifier
                .size(60.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surface),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Used - ${product.status}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        IconButton(onClick = onRemoveClick) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Eliminar producto",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance()
    format.currency = Currency.getInstance("USD")
    return format.format(amount)
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E2F)
@Composable
fun CartScreenPreview() {
    val previewProduct = Product(
        id = "1",
        name = "Intel Core i7-9700K Processor",
        price = 250.0,
        imageUrl = "",
        status = "Good",
        sellerName = "TechTrader",
        sellerRating = 4.8,
        sellerReviews = 120,
        description = "...",
        specifications = emptyMap()
    )

    CartRepository.clearCart()
    CartRepository.addToCart(previewProduct)

    MarketElectronicoTheme {
        CartScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E2F)
@Composable
fun CartScreenEmptyPreview() {
    CartRepository.clearCart()

    MarketElectronicoTheme {
        CartScreen(navController = rememberNavController())
    }
}