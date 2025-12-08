package com.example.marketelectronico.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.repository.Order
import com.example.marketelectronico.data.repository.OrderRepository
import com.example.marketelectronico.data.repository.ReviewRepository
import com.example.marketelectronico.data.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.mutableIntStateOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    navController: NavController,
    orderId: String?
) {
    var orderState by remember { mutableStateOf<Order?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar la orden asíncronamente
    LaunchedEffect(orderId) {
        if (orderId != null) {
            orderState = OrderRepository.getOrderById(orderId)
        }
        isLoading = false
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var refreshTrigger by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val currentUserId = com.example.marketelectronico.utils.TokenManager.getUserId()?.toString() ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Compra") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val currentOrder = orderState

        if (currentOrder == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No se pudo cargar la información de la orden.")
            }
            return@Scaffold
        }

        val dateFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        val currentUser by UserRepository.getInstance().currentUser.collectAsState()
        val userName = currentUser?.nombre_usuario ?: ""

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Encabezado
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Resumen del Pedido", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Text(
                    text = "Fecha: ${dateFormatter.format(currentOrder.date)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Datos generales
            item {
                DetailItem(Icons.Default.Tag, "ID Orden", "#${currentOrder.id.take(8)}")
            }
            item {
                DetailItem(Icons.Default.AttachMoney, "Total Pagado", "$${currentOrder.totalAmount}")
            }
            item {
                DetailItem(Icons.Default.LocalShipping, "Estado", "Entregado") // Puedes ajustar esto si tienes estado en el backend
            }

            // Lista de productos
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Productos (${currentOrder.items.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            items(currentOrder.items) { product ->
                //Pasamos 'currentUserId'
                val hasReviewed by produceState(initialValue = false, product.id, currentUserId, refreshTrigger) {
                    if (currentUserId.isNotEmpty()) {
                        value = ReviewRepository.hasUserReviewedProduct(product.id, currentUserId)
                    } else {
                        value = false
                    }
                }

                ProductDetailRow(
                    product = product,
                    showReviewButton = !hasReviewed,
                    onAddReviewClick = { navController.navigate("add_review/${product.id}") }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ProductDetailRow(product: Product, showReviewButton: Boolean, onAddReviewClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$${product.price}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (showReviewButton) {
                OutlinedButton(
                    onClick = onAddReviewClick,
                    modifier = Modifier.height(35.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Review", fontSize = 12.sp)
                }
            }
        }
    }
}