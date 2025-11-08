package com.example.marketelectronico.ui.review

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.R // Para la imagen de placeholder
import com.example.marketelectronico.data.model.allSampleProducts
import com.example.marketelectronico.data.repository.Review
import com.example.marketelectronico.data.repository.ReviewRepository
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    navController: NavController,
    productId: String?
) {
    // 1. Encontrar el producto que se está reseñando
    val product = remember { allSampleProducts.find { it.id == productId } }

    // 2. Estados para guardar la entrada del usuario
    var rating by remember { mutableDoubleStateOf(0.0) }
    var comment by remember { mutableStateOf("") }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (product == null) {
                Text("Producto no encontrado.")
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                // --- Información del Producto ---
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background), // Placeholder
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(24.dp))

                // --- Selector de Rating ---
                Text(
                    text = "Your Rating",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                RatingInput(
                    currentRating = rating,
                    onRatingChanged = { newRating -> rating = newRating }
                )
                Spacer(modifier = Modifier.height(24.dp))

                // --- Campo de Comentario ---
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your Review") },
                    placeholder = { Text("Share your thoughts about this product...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // --- Botón de Enviar ---
                Button(
                    onClick = {
                        // 3. Crear y guardar la reseña
                        val newReview = Review(
                            productId = product.id,
                            author = "Asu", // Hardcodeado del ProfileScreen
                            rating = rating,
                            comment = comment
                        )
                        ReviewRepository.addReview(newReview)

                        // 4. Volver a la pantalla anterior
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = rating > 0 && comment.isNotBlank() // Habilitado solo si hay rating y comentario
                ) {
                    Text("Submit Review")
                }
            }
        }
    }
}

@Composable
fun RatingInput(
    currentRating: Double,
    onRatingChanged: (Double) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        (1..5).forEach { star ->
            Icon(
                imageVector = if (star <= currentRating) Icons.Default.Star else Icons.Default.StarOutline,
                contentDescription = "Star $star",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
                    .clickable { onRatingChanged(star.toDouble()) }
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF1E1E2F)
@Composable
fun ReviewScreenPreview() {
    MarketElectronicoTheme {
        ReviewScreen(
            navController = rememberNavController(),
            productId = "1" // ID de uno de tus productos de muestra
        )
    }
}

