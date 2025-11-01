package com.example.bd_finance.ui.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bd_finance.data.portfolio.PortfolioHolding
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHoldingDialog(
    initialTicker: String = "",
    initialCompanyName: String? = null,
    initialPrice: Double? = null,
    onDismiss: () -> Unit,
    onConfirm: (PortfolioHolding) -> Unit
) {
    var ticker by remember { mutableStateOf(initialTicker) }
    var quantity by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf(initialPrice?.toString() ?: "") }
    var purchaseDate by remember { mutableStateOf(LocalDate.now()) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    var tickerError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Portfolio") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ticker,
                    onValueChange = {
                        ticker = it.uppercase()
                        tickerError = false
                    },
                    label = { Text("Ticker *") },
                    placeholder = { Text("e.g., AAPL") },
                    isError = tickerError,
                    supportingText = if (tickerError) {
                        { Text("Ticker is required") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        quantityError = false
                    },
                    label = { Text("Quantity *") },
                    placeholder = { Text("Number of shares") },
                    isError = quantityError,
                    supportingText = if (quantityError) {
                        { Text("Enter a valid quantity") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = {
                        purchasePrice = it
                        priceError = false
                    },
                    label = { Text("Purchase Price *") },
                    placeholder = { Text("Price per share") },
                    isError = priceError,
                    supportingText = if (priceError) {
                        { Text("Enter a valid price") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = purchaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    onValueChange = { },
                    label = { Text("Purchase Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, "Select date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Add notes about this investment") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "* Required fields",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate inputs
                    var isValid = true

                    if (ticker.isBlank()) {
                        tickerError = true
                        isValid = false
                    }

                    val quantityValue = quantity.toDoubleOrNull()
                    if (quantityValue == null || quantityValue <= 0) {
                        quantityError = true
                        isValid = false
                    }

                    val priceValue = purchasePrice.toDoubleOrNull()
                    if (priceValue == null || priceValue <= 0) {
                        priceError = true
                        isValid = false
                    }

                    if (isValid && quantityValue != null && priceValue != null) {
                        val holding = PortfolioHolding(
                            ticker = ticker,
                            companyName = initialCompanyName,
                            quantity = quantityValue,
                            purchasePrice = priceValue,
                            purchaseDate = purchaseDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                            notes = notes.ifBlank { null },
                            lastPrice = null,
                            lastRecommendation = null,
                            lastUpdated = null
                        )
                        onConfirm(holding)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = purchaseDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        purchaseDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = content
    )
}
