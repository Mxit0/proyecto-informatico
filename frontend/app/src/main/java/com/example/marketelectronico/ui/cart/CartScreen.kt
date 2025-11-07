package com.example.marketelectronico.ui.cart

import androidx.compose.foundation.Image
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
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.repository.CartRepository
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import androidx.compose.foundation.background
import com.example.marketelectronico.data.model.sampleProduct1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController
) {
    val cartProducts = CartRepository.cartItems
    var productToRemove by remember { mutableStateOf<Product?>(null) }
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

                // --- ¡NUEVO! SECCIÓN DE TOTAL ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    // Formateamos el precio para que muestre 2 decimales
                    Text(
                        text = "$${String.format("%.2f", CartRepository.totalPrice.value)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                // ---------------------------------

                // Botón de Pago
                Button(
                    onClick = { /* TODO: Ir a la pantalla de pago */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp) // Reducido el padding superior
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Continue to payment")
                }

                // Barra de Navegación
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
                                    "Inicio" -> navController.navigate("main") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                    // ... (otras navegaciones)
                                }
                            }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (cartProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp) // Padding interno para la lista
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
                                productToRemove = product
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    // Espacio al final para que no se pegue al botón de total
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Diálogo de confirmación (sin cambios)
        if (productToRemove != null) {
            AlertDialog(
                onDismissRequest = { productToRemove = null },
                title = { Text(text = "¿Eliminar Producto?") },
                text = { Text(text = "¿Estás seguro de que quieres eliminar \"${productToRemove!!.name}\" del carrito?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            CartRepository.removeFromCart(productToRemove!!)
                            productToRemove = null
                        }
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { productToRemove = null }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// CartItem (sin cambios)
@Composable
fun CartItem(product: Product, onRemoveClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
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
                tint = Color.Gray
            )
        }
    }
}

// Preview (sin cambios)
@Preview(showBackground = true, backgroundColor = 0xFF1E1E2F)
@Composable
fun CartScreenPreview() {
    CartRepository.clearCart()
    CartRepository.addToCart(sampleProduct1)

    MarketElectronicoTheme {
        CartScreen(navController = rememberNavController())
    }
}