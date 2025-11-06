package com.example.marketelectronico.ui.base

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
// Importa el tema desde el paquete hermano 'theme'
import com.example.marketelectronico.theme.MarketElectronicoTheme

/**
 * Plantilla base reutilizable (Scaffold) para la mayoría de las pantallas.
 * Proporciona una TopAppBar consistente.
 * [cite: alias(libs.plugins.kotlin.compose)]
 */
@OptIn(ExperimentalMaterial3Api::class) // Requerido para Scaffold y TopAppBar en Material 3
@Composable
fun BaseScreen(
    title: String,
    modifier: Modifier = Modifier,
    // Slot para el contenido específico de cada pantalla
    content: @Composable (padding: PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            // Esta es la barra de navegación superior que será igual en todas las pantallas
            TopAppBar(
                title = { Text(text = title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    // Usa los colores definidos en tu /ui/theme/Color.kt
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        // Aquí llamamos al "content" que nos pasó la pantalla específica
        // (ej. HomeScreen) y le pasamos el padding de la barra superior.
        content(innerPadding)
    }
}

/**
 * Esta función es solo para previsualizar tu plantilla BaseScreen
 * en la pestaña "Split" o "Design" de Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun BaseScreenPreview() {
    // Es importante envolver tu vista previa en el tema de tu app
    // para que los colores (MaterialTheme.colorScheme.primary) funcionen.
    MarketElectronicoTheme {
        BaseScreen(title = "Título de Ejemplo") { padding ->
            // Contenido de ejemplo para la vista previa
            Text(
                text = "El contenido de la pantalla va aquí.",
                modifier = Modifier.padding(padding) // Aplicamos el padding
            )
        }
    }
}