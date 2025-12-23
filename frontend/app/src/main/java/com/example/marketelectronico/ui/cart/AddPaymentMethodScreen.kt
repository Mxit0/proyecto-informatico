package com.example.marketelectronico.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.data.repository.PaymentMethod
import com.example.marketelectronico.data.repository.PaymentRepository
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import java.util.UUID
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentMethodScreen(
    navController: NavController
) {
    
    var alias by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Card") },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), 
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Alias de la Tarjeta (ej. Tarjeta Principal)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp)) 
                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it },
                    label = { Text("Cardholder Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )


                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { if (it.length <= 16) cardNumber = it }, 
                    label = { Text("Card Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    val isExpiryDateError = expiryDate.length == 4 && !isExpiryDateValid(expiryDate)
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { if (it.length <= 4) expiryDate = it.filter { char -> char.isDigit() } }, 
                        label = { Text("Expiry Date (MM/YY)") }, 
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        
                        visualTransformation = DateVisualTransformation(),
                        isError = isExpiryDateError,
                        supportingText = { if (isExpiryDateError) Text("Fecha inválida o expirada") }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { if (it.length <= 3) cvv = it }, 
                        label = { Text("CVV") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }

            
            Button(
                onClick = {
                    
                    val newMethod = PaymentMethod(
                        id = UUID.randomUUID().toString(), 
                        alias = alias,
                        type = if (cardNumber.startsWith("4")) "Visa" else "Mastercard", 
                        lastFour = cardNumber.takeLast(4),
                        cardholderName = cardholderName
                    )

                    
                    PaymentRepository.addMethod(newMethod)

                    
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp),
                
                enabled = alias.isNotBlank() && cardholderName.isNotBlank() && cardNumber.length == 16 && isExpiryDateValid(expiryDate) && cvv.length == 3
            ) {
                Text("Save Card")
            }
        }
    }

}


private class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 && i < trimmed.length) out += "/"
        }

        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 1
                return 5
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }

        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}

private fun isExpiryDateValid(expiryDate: String): Boolean {
    if (expiryDate.length != 4) return false

    return try {
        val month = expiryDate.substring(0, 2).toInt()
        val year = expiryDate.substring(2, 4).toInt() + 2000 

        if (month < 1 || month > 12) return false

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 

        when {
            year < currentYear -> false 
            year == currentYear && month < currentMonth -> false 
            else -> true
        }
    } catch (e: NumberFormatException) {
        false
    }
}
@Preview(showBackground = true, backgroundColor = 0xFF1E1E2F)
@Composable
fun AddPaymentMethodScreenPreview() {
    MarketElectronicoTheme {
        AddPaymentMethodScreen(navController = rememberNavController())
    }
}