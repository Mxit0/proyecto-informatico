package com.example.marketelectronico.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.data.repository.CartRepository
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import java.text.NumberFormat
import java.util.Currency
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.repository.PaymentMethod
import com.example.marketelectronico.data.repository.PaymentRepository
import com.example.marketelectronico.data.repository.Order
import com.example.marketelectronico.data.repository.OrderRepository
import java.util.UUID
import com.example.marketelectronico.utils.TokenManager


// --- 2. ELIMINAR EL MODELO Y LOS DATOS DE MUESTRA LOCALES ---
// (Ya no se necesitan, ahora están en el PaymentRepository)
/*
data class PaymentMethod( ... )
val samplePaymentMethods = listOf( ... )
*/

// --- Constantes para el cálculo ---
private const val SHIPPING_COST = 5.00
private const val TAX_RATE = 0.19 // 19%

/**
 * Formatea un valor Double a un string de moneda (ej. $120.00)
 */
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance()
    format.currency = Currency.getInstance("USD") // Puedes cambiar "USD" si es necesario
    return format.format(amount)
}

/**
 * Pantalla de Pago
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController
) {
    // --- Lógica de Cálculo ---
    val subtotal = CartRepository.totalPrice.value
    val taxes = subtotal * TAX_RATE
    val total = subtotal + SHIPPING_COST + taxes

    // --- 3. LEER LA LISTA DESDE EL REPOSITORIO ---
    val paymentMethods = PaymentRepository.paymentMethods

    // Estado para la barra de navegación inferior
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

    // Estado para el método de pago seleccionado
    // Asegurarse de que el ID seleccionado sea válido
    var selectedMethodId by remember { mutableStateOf(paymentMethods.firstOrNull()?.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment", fontWeight = FontWeight.Bold) },
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
            // El BottomBar contiene el botón de pago Y la navegación
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                Button(
                    onClick = {
                        // 1. Obtener el ID del usuario actual
                        val currentUserId = TokenManager.getUserId()?.toString() ?: "usuario_anonimo"

                        val itemsToPurchase = CartRepository.cartItems.toList()
                        val orderTotal = total // Variable calculada más arriba en tu código

                        // 2. Crear la orden PASANDO EL userId
                        val newOrder = Order(
                            id = UUID.randomUUID().toString(),
                            userId = currentUserId,
                            items = itemsToPurchase,
                            totalAmount = orderTotal
                        )

                        OrderRepository.addOrder(newOrder)
                        CartRepository.clearCart()

                        // Navegar a la confirmación
                        navController.navigate("pay_confirm/${newOrder.id}") {
                            popUpTo("cart") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    enabled = selectedMethodId != null && CartRepository.cartItems.isNotEmpty()
                ) {
                    Text("Pay Now")
                }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // --- Sección de Métodos de Pago ---
            item {
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // --- 4. ACTUALIZAR EL items() PARA USAR LA LISTA DEL REPO ---
            items(paymentMethods.size) { index ->
                val method = paymentMethods[index]
                PaymentMethodItem(
                    method = method,
                    isSelected = method.id == selectedMethodId,
                    onClick = { selectedMethodId = method.id }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // --- 5. CONECTAR EL BOTÓN AL NAVIGATOR ---
                AddPaymentMethodItem(onClick = {
                    navController.navigate("add_payment_method")
                })
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Sección de Resumen del Pedido ---
            item {
                Text(
                    text = "Order Summary",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                SummaryRow(label = "Subtotal", amount = formatCurrency(subtotal))
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(label = "Shipping", amount = formatCurrency(SHIPPING_COST))
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(label = "Taxes", amount = formatCurrency(taxes))
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                SummaryRow(label = "Total", amount = formatCurrency(total), isTotal = true)
            }
        }
    }
}

/**
 * Un item para la lista de métodos de pago
 */
@Composable
fun PaymentMethodItem(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono de tarjeta genérico
            Icon(
                Icons.Default.CreditCard,
                contentDescription = method.type,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = 0.5f))
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = method.alias,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ending in ${method.lastFour}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        // Usamos RadioButton para la selección única
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/**
 * El botón para "Añadir Método de Pago"
 */
@Composable
fun AddPaymentMethodItem(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Add Payment Method",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Add Payment Method",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Una fila simple para el resumen del pedido
 */
@Composable
fun SummaryRow(label: String, amount: String, isTotal: Boolean = false) {
    val textStyle = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
    val fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
    val color = if (isTotal) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = textStyle,
            fontWeight = fontWeight,
            color = color
        )
        Text(
            text = amount,
            style = textStyle,
            fontWeight = fontWeight,
            color = color
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF1E1E2F)
@Composable
fun PaymentScreenPreview() {
    // Limpiamos carrito para evitar errores previos
    CartRepository.clearCart()

    // Usamos argumentos NOMBRADOS (name = "...", price = ...) para evitar errores de orden
    val testProduct = Product(
        id = "1",
        name = "Test Product",
        price = 120.0,
        imageUrl = "",
        status = "New",
        sellerId = 4, // <-- Aquí definimos explícitamente el ID
        sellerName = "Vendedor Test",
        sellerRating = 4.5,
        sellerReviews = 10,
        description = "Descripción de prueba",
        specifications = emptyMap()
    )

    CartRepository.addToCart(testProduct)

    MarketElectronicoTheme {
        PaymentScreen(navController = rememberNavController())
    }
}

