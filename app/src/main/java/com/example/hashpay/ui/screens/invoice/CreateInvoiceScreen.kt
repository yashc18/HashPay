package com.example.hashpay.ui.screens.invoice

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hashpay.R
import com.example.hashpay.ui.components.CustomTopAppBar
import com.example.hashpay.ui.components.DatePickerDialog
import com.example.hashpay.ui.viewmodels.InvoiceViewModel
import com.example.hashpay.ui.viewmodels.InvoiceViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreateInvoiceScreen(
    onNavigateBack: () -> Unit,
    onContactSelect: (onAddressSelected: (String) -> Unit) -> Unit,
    walletAddress: String
) {
    val context = LocalContext.current
    val viewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModelFactory(context))

    val receiverAddress by viewModel.receiverAddress.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val description by viewModel.description.collectAsState()
    val dueDate by viewModel.dueDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val successMessage by viewModel.successMessage.collectAsState(null)

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDueDate = dueDate?.let { dateFormat.format(Date(it)) } ?: "No due date"

    val isFormValid = remember(receiverAddress, amount) {
        receiverAddress.isNotBlank() &&
                receiverAddress.startsWith("0x") &&
                amount.isNotBlank() &&
                amount.toDoubleOrNull() != null &&
                amount.toDoubleOrNull()!! > 0
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSuccessMessage()
            onNavigateBack()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CustomTopAppBar(
                title = "Create Invoice",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )

            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { error ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = if (error.contains("successfully")) Color(0xFF003300) else Color(0xFF330000),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.clearErrorMessage() }) {
                                Text("Dismiss", color = Color.White)
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Invoice Details",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Receiver Address
                OutlinedTextField(
                    value = receiverAddress,
                    onValueChange = { viewModel.setReceiverAddress(it) },
                    label = { Text("Receiver Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            onContactSelect { address ->
                                viewModel.setReceiverAddress(address)
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = "Select Contact",
                                tint = Color.White
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { viewModel.setAmount(it) },
                    label = { Text("Amount (ETH)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.setDescription(it) },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Due Date
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "Due Date",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Due Date",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formattedDueDate,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        Log.d("InvoiceDebug", "Create Invoice button clicked")
                        Log.d("InvoiceDebug", "Wallet address: $walletAddress")
                        viewModel.createInvoice(walletAddress) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create Invoice")
                    }
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDateSelected = { viewModel.setDueDate(it) },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}