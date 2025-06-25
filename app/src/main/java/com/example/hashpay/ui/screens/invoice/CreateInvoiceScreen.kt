package com.example.hashpay.ui.screens.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hashpay.R
import com.example.hashpay.ui.components.DatePickerDialog
import com.example.hashpay.ui.theme.SpaceGrotesk
import com.example.hashpay.ui.viewmodels.InvoiceViewModel
import com.example.hashpay.ui.viewmodels.InvoiceViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    onNavigateBack: () -> Unit,
    onContactSelect: (suspend (String, String) -> Unit) -> Unit,
    walletAddress: String,
    viewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Observe ViewModel states
    val receiverAddress by viewModel.receiverAddress.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val description by viewModel.description.collectAsState()
    val dueDateState by viewModel.dueDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Local UI state
    var recipientName by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Calculate a default due date (7 days from now)
    val defaultDueDate = remember { System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) }

    // Monitor success message to navigate back on success
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            viewModel.clearSuccessMessage()
            onNavigateBack()
        }
    }

    // Show date picker if requested
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = {
                viewModel.setDueDate(it)
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Invoice",
                        color = Color.White,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F0F)
                ),
                windowInsets = WindowInsets(0)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Recipient Input Field
                Text(
                    "Recipient",
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = receiverAddress,
                    onValueChange = { viewModel.setReceiverAddress(it) },
                    placeholder = {
                        Text(
                            "Enter wallet address or ENS",
                            color = Color(0xFF6B7280),
                            fontFamily = SpaceGrotesk
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                onContactSelect { address, name ->
                                    viewModel.setReceiverAddress(address)
                                    recipientName = name
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = "Select Contact",
                                tint = Color(0xFF9E9E9E)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF9FE870),
                        unfocusedBorderColor = Color(0xFF2A2A2A),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1F1F1F),
                        unfocusedContainerColor = Color(0xFF1F1F1F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = SpaceGrotesk
                    )
                )

                // Amount Input Field
                Text(
                    "Amount",
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { viewModel.setAmount(it) },
                    placeholder = {
                        Text(
                            "Enter amount in ETH",
                            color = Color(0xFF6B7280),
                            fontFamily = SpaceGrotesk
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F3A1F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Ξ",
                                color = Color(0xFF9FE870),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF9FE870),
                        unfocusedBorderColor = Color(0xFF2A2A2A),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1F1F1F),
                        unfocusedContainerColor = Color(0xFF1F1F1F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = SpaceGrotesk
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )

                // Description Input Field (Optional)
                Text(
                    "Description (Optional)",
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.setDescription(it) },
                    placeholder = {
                        Text(
                            "Enter description",
                            color = Color(0xFF6B7280),
                            fontFamily = SpaceGrotesk
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF9FE870),
                        unfocusedBorderColor = Color(0xFF2A2A2A),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1F1F1F),
                        unfocusedContainerColor = Color(0xFF1F1F1F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = SpaceGrotesk
                    )
                )

// Due Date Selector
                Text(
                    "Due Date",
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = dueDateState?.let { dateFormatter.format(Date(it)) } ?: dateFormatter.format(Date(defaultDueDate)),
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_calendar),
                                contentDescription = "Select Date",
                                tint = Color(0xFF9E9E9E)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9FE870),
                            unfocusedBorderColor = Color(0xFF2A2A2A),
                            disabledBorderColor = Color(0xFF2A2A2A),
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1F1F1F),
                            unfocusedContainerColor = Color(0xFF1F1F1F),
                            disabledContainerColor = Color(0xFF1F1F1F)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = SpaceGrotesk
                        )
                    )

                    // Add this transparent clickable box on top of the TextField
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                            .background(Color.Transparent)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        // Set the due date if not set already
                        if (dueDateState == null) {
                            viewModel.setDueDate(defaultDueDate)
                        }

                        viewModel.createInvoice(walletAddress)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9FE870),
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFF424242)
                    ),
                    enabled = receiverAddress.isNotBlank() && amount.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Create Invoice",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// Helper function
private fun formatAddress(address: String): String {
    return if (address.length > 10) {
        "${address.take(6)}...${address.takeLast(4)}"
    } else {
        address
    }
}