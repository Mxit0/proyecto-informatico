package com.example.marketelectronico.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.marketelectronico.data.repository.Review
import com.example.marketelectronico.data.repository.ReviewRepository
import com.example.marketelectronico.data.repository.UserRepository // <-- Tu repo existente
import com.example.marketelectronico.ui.product.ProductDetailUiState
import com.example.marketelectronico.ui.product.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    navController: NavController,
    productId: String?,
    viewModel: ProductViewModel = viewModel()
) {
    var rating by remember { mutableDoubleStateOf(0.0) }
    var comment by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    // --- 1. OBTENER USUARIO ACTUAL ---
    // Observamos el StateFlow que añadimos a tu UserRepository
    val currentUser by UserRepository.getInstance().currentUser.collectAsState()
    // ---------------------------------

    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.fetchProduct(productId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Write a Review") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        when (val state = uiState) {
            is ProductDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProductDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}")
                }
            }
            is ProductDetailUiState.Success -> {
                val product = state.product

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Imagen del producto
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                        error = painterResource(id = android.R.drawable.ic_menu_gallery)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- 2. MOSTRAR USUARIO REAL EN PANTALLA ---
                    if (currentUser != null) {
                        Text(
                            text = "Escribiendo como: ${currentUser?.nombre_usuario}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    // ------------------------------------------

                    Text("Your Rating", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    RatingInput(currentRating = rating, onRatingChanged = { rating = it })
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Your Review") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            // --- 3. GUARDAR LA RESEÑA CON EL USUARIO REAL ---
                            val authorName = currentUser?.nombre_usuario ?: "Usuario Anónimo"
                            val authorPhoto = currentUser?.foto

                            val newReview = Review(
                                productId = product.id,
                                productName = product.name,
                                productImageUrl = product.imageUrl,
                                author = authorName, // <-- ¡Nombre correcto!
                                authorImageUrl = authorPhoto,
                                rating = rating,
                                comment = comment
                            )
                            ReviewRepository.addReview(newReview)
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = rating > 0
                    ) {
                        Text("Submit Review")
                    }
                }
            }
        }
    }
}

// ... (RatingInput se queda igual) ...
@Composable
fun RatingInput(currentRating: Double, onRatingChanged: (Double) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        (1..5).forEach { starIndex ->
            val icon = when {
                currentRating >= starIndex -> Icons.Default.Star
                currentRating >= starIndex - 0.5 -> Icons.Default.StarHalf
                else -> Icons.Default.StarOutline
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
                    .pointerInput(starIndex) {
                        detectTapGestures { offset ->
                            if (offset.x < this.size.width / 2) onRatingChanged(starIndex - 0.5)
                            else onRatingChanged(starIndex.toDouble())
                        }
                    }
            )
        }
    }
}