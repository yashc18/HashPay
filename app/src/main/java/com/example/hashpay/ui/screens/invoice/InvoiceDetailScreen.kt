package com.example.hashpay.ui.screens.invoice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hashpay.data.database.entities.InvoiceStatus
import com.example.hashpay.ui.components.CustomTopAppBar
import com.example.hashpay.ui.components.LoadingView
import com.example.hashpay.ui.components.StatusChip
import com.example.hashpay.ui.viewmodels.InvoiceViewModel
import com.example.hashpay.ui.viewmodels.InvoiceViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    onNavigateBack: () -> Unit,
    onPayInvoice: (invoiceId: Long, amount: Double, receiverAddress: String) -> Unit,
    walletAddress: String
) {
    val context = LocalContext.current
    val viewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModelFactory(context))
    val clipboardManager = LocalClipboardManager.current

    val currentInvoice by viewModel.currentInvoice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    // Load invoice when the screen is first displayed
    LaunchedEffect(invoiceId) {
        viewModel.loadInvoiceById(invoiceId)
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage == "Invoice deleted") {
            onNavigateBack()
        }
    }

    val isUserReceiver = remember(currentInvoice, walletAddress) {
        currentInvoice.receiverAddress == walletAddress
    }

    val isUserSender = remember(currentInvoice, walletAddress) {
        currentInvoice.senderAddress == walletAddress
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
                title = "Invoice Details",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareText = """
                            Invoice #${currentInvoice.id}
                            Amount: ${currentInvoice.formattedAmount}
                            From: ${currentInvoice.senderAddress}
                            To: ${currentInvoice.receiverAddress}
                            Status: ${currentInvoice.status}
                            Created: ${currentInvoice.formattedDate}
                            Due: ${currentInvoice.formattedDueDate}
                            ${if (currentInvoice.description.isNotBlank()) "Description: ${currentInvoice.description}" else ""}
                        """.trimIndent()

                        clipboardManager.setText(AnnotatedString(shareText))
                        viewModel.setErrorMessage("Invoice details copied to clipboard")                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }

                    if (isUserSender && currentInvoice.status == InvoiceStatus.PENDING) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    }
                }
            )

            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { error ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = if (error.contains("successfully") || error.contains("copied"))
                            Color(0xFF003300) else Color(0xFF330000),
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

            if (isLoading) {
                LoadingView()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Invoice Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Invoice #${currentInvoice.id}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )

                        StatusChip(status = currentInvoice.status)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Amount
                    InvoiceDetailRow(label = "Amount", value = currentInvoice.formattedAmount)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sender Address
                    InvoiceDetailRow(
                        label = "From",
                        value = currentInvoice.senderAddress.ifBlank { "Unknown" },
                        isAddress = currentInvoice.senderAddress.isNotBlank(),
                        onCopy = { clipboardManager.setText(AnnotatedString(currentInvoice.senderAddress)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Receiver Address
                    InvoiceDetailRow(
                        label = "To",
                        value = currentInvoice.receiverAddress,
                        isAddress = true,
                        onCopy = { clipboardManager.setText(AnnotatedString(currentInvoice.receiverAddress)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dates
                    InvoiceDetailRow(label = "Created", value = currentInvoice.formattedDate)

                    if (currentInvoice.dueDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        InvoiceDetailRow(
                            label = "Due Date",
                            value = currentInvoice.formattedDueDate,
                            valueColor = if (currentInvoice.isOverdue) Color.Red else Color.White
                        )
                    }

                    if (currentInvoice.paidAt != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        InvoiceDetailRow(
                            label = "Paid On",
                            value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(Date(currentInvoice.paidAt!!))
                        )
                    }

                    // Transaction Hash if paid
                    if (currentInvoice.transactionHash != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        InvoiceDetailRow(
                            label = "Transaction",
                            value = currentInvoice.transactionHash!!,
                            isAddress = true,
                            onCopy = { clipboardManager.setText(AnnotatedString(currentInvoice.transactionHash!!)) }
                        )
                    }

                    // Description
                    if (currentInvoice.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Description",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF1E1E1E),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = currentInvoice.description,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Action buttons
                    if (currentInvoice.status == InvoiceStatus.PENDING) {
                        if (isUserSender) {
                            Button(
                                onClick = { showCancelConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF33002F)
                                )
                            ) {
                                Text("Cancel Invoice")
                            }
                        } else if (!isUserReceiver) {
                            Button(
                                onClick = {
                                    onPayInvoice(
                                        currentInvoice.id,
                                        currentInvoice.amount,
                                        currentInvoice.receiverAddress
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Pay Invoice")
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Invoice", color = Color.White) },
                text = { Text("Are you sure you want to delete this invoice? This action cannot be undone.", color = Color.White) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteInvoice(currentInvoice.id)
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }

        if (showCancelConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showCancelConfirmDialog = false },
                title = { Text("Cancel Invoice", color = Color.White) },
                text = { Text("Are you sure you want to cancel this invoice?", color = Color.White) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateInvoiceStatus(currentInvoice.id, InvoiceStatus.CANCELLED)
                            showCancelConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33002F))
                    ) {
                        Text("Cancel Invoice")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelConfirmDialog = false }) {
                        Text("Keep", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }
    }
}

@Composable
fun InvoiceDetailRow(
    label: String,
    value: String,
    isAddress: Boolean = false,
    onCopy: (() -> Unit)? = null,
    valueColor: Color = Color.White
) {
    Column {
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isAddress && value.length > 16)
                    "${value.take(8)}...${value.takeLast(8)}"
                else
                    value,
                color = valueColor,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            if (isAddress && onCopy != null) {
                TextButton(
                    onClick = { onCopy() },
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        text = "Copy",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}