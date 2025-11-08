package com.example.marketelectronico.ui.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.R
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.model.allSampleProducts
import com.example.marketelectronico.data.repository.CartRepository
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.marketelectronico.data.repository.OrderRepository
import com.example.marketelectronico.data.repository.Order
import com.example.marketelectronico.data.repository.ReviewRepository
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Pantalla de confirmación de pago exitoso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayConfirmScreen(
    navController: NavController,
    orderId: String?
) {
    // Obtenemos los items del carrito para mostrarlos en el resumen
    val order = OrderRepository.findOrderById(orderId)
    if (order == null) {
        // ... (puedes mostrar un mensaje de error o simplemente volver)
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }
    val purchasedItems = order.items
    val totalItems = purchasedItems.size
    val userReviews by remember {
        derivedStateOf { ReviewRepository.getReviewsByUser("Asu") }
    }
    // --- Lógica para la barra de navegación inferior ---
    // Copiada de ProductScreen.kt para consistencia
    var selectedItem by remember { mutableIntStateOf(-1) }
    val navItems = listOf("Inicio", "Categorías", "Vender", "Mensajes", "Perfil", "Foro")
    val navIcons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.List,
        Icons.Default.AddCircle,
        Icons.Default.Email,
        Icons.Default.Person,
        Icons.Default.Info
    )

    // --- Lógica de Navegación al Salir ---
    // Esta función se llamará al presionar "Continue Shopping" o la flecha de "Atrás".
    // Limpia el carrito y vuelve a la pantalla principal.
    val onDoneShopping: () -> Unit = {
        navController.navigate("main") {
            popUpTo("main") { inclusive = true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Successful") },
                navigationIcon = {
                    IconButton(onClick = onDoneShopping) { // Al volver, se completa la compra
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            // Barra de navegación inferior, copiada de ProductScreen.kt
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navItems.forEachIndexed { index, label ->
                    NavigationBarItem(
                        icon = { Icon(navIcons[index], contentDescription = label, tint = if (selectedItem == index) MaterialTheme.colorScheme.primary else Color.Gray) },
                        label = { Text(label, color = if (selectedItem == index) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 9.sp) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            when (label) {
                                "Inicio" -> onDoneShopping() // "Inicio" también limpia el carrito
                                "Categorías" -> { /* TODO: navController.navigate("categories") */ }
                                "Vender" -> { /* TODO: navController.navigate("publish") */ }
                                "Mensajes" -> { /* TODO: navController.navigate("messages") */ }
                                "Perfil" -> { /* TODO: navController.navigate("profile") */ }
                                "Foro" -> { /* TODO: navController.navigate("forum") */ }
                            }
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Encabezado ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Payment Successful!",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your payment has been processed successfully. Thank you for your purchase!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Detalles del Pedido ---
            item {
                PaymentDetailItem(
                    icon = Icons.Default.Tag,
                    label = "Order Number",
                    value = "Order #${order.id.take(8)}" // Muestra parte del ID
                )
            }
            item {
                PaymentDetailItem(
                    icon = Icons.Default.Inventory2,
                    label = "Items Purchased",
                    value = "$totalItems Items"
                )
            }
            item {
                PaymentDetailItem(
                    icon = Icons.Default.LocalShipping, // Icono de camión
                    label = "Delivery Information",
                    value = "Estimated Delivery: July 20-22" // Valor de ejemplo
                )
            }

            // --- Resumen de Orden ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Order Summary",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // --- Lista de Items Comprados ---
            items(purchasedItems) { product ->
                val hasBeenReviewed = userReviews.any { it.productId == product.id }
                ProductSummaryItem(
                    product = product,
                    hasBeenReviewed = hasBeenReviewed,
                    onAddReviewClick = {
                        navController.navigate("add_review/${product.id}")
                    }
                )
            }

            // --- Botones de Acción ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO: Navegar a la pantalla de detalles de orden */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("View Order Details", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDoneShopping, // Usa la función de limpiar y navegar
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Text("Continue Shopping", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Un Composable reutilizable para los items de detalle (Order Number, Items, Delivery).
 */
@Composable
private fun PaymentDetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Un Composable reutilizable para los productos en el resumen de orden.
 */
@Composable
private fun ProductSummaryItem(
    product: Product,
    hasBeenReviewed: Boolean,
    onAddReviewClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Placeholder - Usa tus imágenes reales si están disponibles
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background), // CAMBIA ESTO
                contentDescription = product.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "1 x ${product.status}", // Muestra el estado (ej. "Used")
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (!hasBeenReviewed) {
                Button(
                    onClick = onAddReviewClick,
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Add Review")
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF1E1E2F)
@Composable
fun PayConfirmScreenPreview() {
    val previewOrder = Order(
        id = "preview123",
        items = com.example.marketelectronico.data.model.allSampleProducts.take(2),
        totalAmount = 250.0
    )
    OrderRepository.addOrder(previewOrder)

    MarketElectronicoTheme {
        PayConfirmScreen(
            navController = rememberNavController(),
            orderId = "preview123"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E2F)
@Composable
fun PayConfirmScreenEmptyPreview() {
    OrderRepository.orders.clear() // Limpiamos el repo de órdenes para el preview
    MarketElectronicoTheme {
        // Ahora pasamos 'null' para probar el caso de "orden no encontrada"
        PayConfirmScreen(
            navController = rememberNavController(),
            orderId = null
        )
    }
}