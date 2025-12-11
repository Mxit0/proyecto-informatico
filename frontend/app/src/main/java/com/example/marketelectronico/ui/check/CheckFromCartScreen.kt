package com.example.marketelectronico.ui.check

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue as getValueCompose
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import com.example.marketelectronico.data.repository.CartRepository
import com.example.marketelectronico.data.repository.CompatibilityRepository
import com.example.marketelectronico.data.model.Product
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckFromCartScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = ""
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        // Cargar carrito al entrar
        CartRepository.loadCart()
    }

    val cartItems = CartRepository.cartItems
    val hasItems = cartItems.isNotEmpty()
    val scope = rememberCoroutineScope()
    val statusMap = remember { mutableStateMapOf<String, String>() }
    var checking by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificar desde Carrito") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp)
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (!hasItems) {
                    Text(
                        "Aún no has añadido productos a tu carrito.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(cartItems) { product: Product ->
                            val pid = product.id.toString()
                            val status = statusMap[pid]
                            var cardModifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)

                            if (status == "COMPATIBLE") {
                                cardModifier = cardModifier.border(
                                    BorderStroke(2.dp, Color(0xFF10B981)), RoundedCornerShape(8.dp)
                                )
                            } else if (status == "INCOMPATIBLE") {
                                cardModifier = cardModifier.border(
                                    BorderStroke(2.dp, Color(0xFFEF4444)), RoundedCornerShape(8.dp)
                                )
                            }

                            androidx.compose.material3.Card(
                                modifier = cardModifier
                            ) {
                                androidx.compose.foundation.layout.Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = product.name,
                                        modifier = Modifier
                                            .height(56.dp)
                                            .padding(end = 12.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                                        Text("Precio: \$${product.price}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    scope.launch {
                        // Realizar la comprobación de compatibilidad
                        checking = true
                        try {
                            val response = CompatibilityRepository.check(cartItems.toList())
                            if (response.isSuccessful) {
                                val wrapper = response.body()
                                if (wrapper != null && wrapper.success && wrapper.data != null) {
                                    val data = wrapper.data
                                    if (data.compatible) {
                                        cartItems.forEach { statusMap[it.id.toString()] = "COMPATIBLE" }
                                        dialogMessage = "Todos los productos parecen compatibles."
                                        showDialog = true
                                    } else {
                                        // Default: mark all compatible then mark incompatible pairs
                                        cartItems.forEach { statusMap[it.id.toString()] = "COMPATIBLE" }
                                        if (!data.pairs.isNullOrEmpty()) {
                                            data.pairs.forEach { p ->
                                                if (!p.compatible) {
                                                    val aId = cartItems.getOrNull(p.a)?.id?.toString()
                                                    val bId = cartItems.getOrNull(p.b)?.id?.toString()
                                                    if (aId != null) statusMap[aId] = "INCOMPATIBLE"
                                                    if (bId != null) statusMap[bId] = "INCOMPATIBLE"
                                                }
                                            }
                                        } else {
                                            cartItems.forEach { statusMap[it.id.toString()] = "INCOMPATIBLE" }
                                        }
                                        dialogMessage = data.explanation ?: data.issues?.joinToString("\n") ?: "Incompatibilidades encontradas."
                                        showDialog = true
                                    }
                                } else {
                                    dialogMessage = wrapper?.error ?: "Respuesta inválida del servidor"
                                    showDialog = true
                                }
                            } else {
                                dialogMessage = "Error de red: ${response.code()}"
                                showDialog = true
                            }
                        } catch (e: Exception) {
                            dialogMessage = "Error: ${e.message}"
                            showDialog = true
                        } finally {
                            checking = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = hasItems && !checking
            ) {
                if (checking) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Verificar Todos", fontWeight = FontWeight.Bold)
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Aceptar") }
                    },
                    title = { Text("Resultado de la comprobación") },
                    text = { Text(dialogMessage ?: "") }
                )
            }
        }
    }
}