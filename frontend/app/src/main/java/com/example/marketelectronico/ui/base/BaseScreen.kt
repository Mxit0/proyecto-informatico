package com.example.marketelectronico.ui.base

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme

/**
 * Plantilla base reutilizable (Scaffold) para la mayoría de las pantallas.
 * Proporciona una TopAppBar consistente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseScreen(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable (padding: PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Preview(showBackground = true)
@Composable
fun BaseScreenPreview() {
    MarketElectronicoTheme {
        BaseScreen(title = "Vista Previa") { padding ->
            Text(
                text = "El contenido de la pantalla va aquí.",
                modifier = Modifier.padding(padding)
            )
        }
    }
}