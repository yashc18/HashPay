package com.example.hashpay.ui.screens.invoice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hashpay.R
import com.example.hashpay.data.database.entities.InvoiceStatus
import com.example.hashpay.ui.theme.SpaceGrotesk
import com.example.hashpay.ui.viewmodels.InvoiceViewModel
import com.example.hashpay.ui.viewmodels.InvoiceViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
        currentInvoice.receiverAddress.equals(walletAddress, ignoreCase = true)
    }

    val isUserSender = remember(currentInvoice, walletAddress) {
        currentInvoice.senderAddress.equals(walletAddress, ignoreCase = true)
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Invoice Details",
                        color = Color.White,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
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
                        viewModel.setErrorMessage("Invoice details copied to clipboard")
                    }) {
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F0F)
                ),
                windowInsets = WindowInsets(0)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Status message
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (error.contains("successfully") || error.contains("copied"))
                                Color(0xFF1F3A1F) else Color(0xFF3A1F1F)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = if (error.contains("successfully") || error.contains("copied"))
                                    painterResource(id = R.drawable.ic_check)
                                else
                                    painterResource(id = R.drawable.ic_error),
                                contentDescription = null,
                                tint = if (error.contains("successfully") || error.contains("copied"))
                                    Color(0xFF9FE870) else Color(0xFFFF6B6B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = error,
                                color = Color.White,
                                fontFamily = SpaceGrotesk,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.clearErrorMessage() }) {
                                Text(
                                    "Dismiss",
                                    color = if (error.contains("successfully") || error.contains("copied"))
                                        Color(0xFF9FE870) else Color(0xFFFF6B6B),
                                    fontFamily = SpaceGrotesk
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                LoadingView(
                    message = "Loading invoice details...",
                    textColor = Color.White,
                    fontFamily = SpaceGrotesk
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Invoice Header Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // Title and status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Invoice #${currentInvoice.id}",
                                    color = Color.White,
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )

                                InvoiceStatusChip(status = currentInvoice.status.name)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Amount
                            Text(
                                text = currentInvoice.formattedAmount,
                                color = Color.White,
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Dates
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calendar),
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Created: ${currentInvoice.formattedDate}",
                                    color = Color(0xFF6B7280),
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 14.sp
                                )
                            }

                            if (currentInvoice.dueDate != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_calendar),
                                        contentDescription = null,
                                        tint = if (currentInvoice.isOverdue) Color(0xFFFF6B6B) else Color(0xFF6B7280),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Due: ${currentInvoice.formattedDueDate}",
                                        color = if (currentInvoice.isOverdue) Color(0xFFFF6B6B) else Color(0xFF6B7280),
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // Parties Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Parties",
                                color = Color.White,
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sender Address
                            DetailRow(
                                label = "From",
                                value = currentInvoice.senderAddress.ifBlank { "Unknown" },
                                isAddress = currentInvoice.senderAddress.isNotBlank(),
                                onCopy = if (currentInvoice.senderAddress.isNotBlank()) {
                                    {
                                        clipboardManager.setText(AnnotatedString(currentInvoice.senderAddress))
                                        viewModel.setErrorMessage("Sender address copied to clipboard")
                                    }
                                } else null
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color(0xFF2A2A2A))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Receiver Address
                            DetailRow(
                                label = "To",
                                value = currentInvoice.receiverAddress,
                                isAddress = true,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(currentInvoice.receiverAddress))
                                    viewModel.setErrorMessage("Receiver address copied to clipboard")
                                }
                            )
                        }
                    }

                    // Payment Details Card
                    if (currentInvoice.paidAt != null || currentInvoice.transactionHash != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Payment Details",
                                    color = Color.White,
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (currentInvoice.paidAt != null) {
                                    DetailRow(
                                        label = "Paid On",
                                        value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                            .format(Date(currentInvoice.paidAt ?: 0))
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                if (currentInvoice.transactionHash != null) {
                                    DetailRow(
                                        label = "Transaction Hash",
                                        value = currentInvoice.transactionHash ?: "",
                                        isAddress = true,
                                        onCopy = {
                                            clipboardManager.setText(AnnotatedString(currentInvoice.transactionHash ?: ""))
                                            viewModel.setErrorMessage("Transaction hash copied to clipboard")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Description Card
                    if (currentInvoice.description.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Description",
                                    color = Color.White,
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = currentInvoice.description,
                                    color = Color(0xFF9E9E9E),
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    if (currentInvoice.status == InvoiceStatus.PENDING) {
                        if (isUserSender) {
                            Button(
                                onClick = { showCancelConfirmDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3A1F1F)
                                )
                            ) {
                                Text(
                                    "Cancel Invoice",
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF9FE870)
                                )
                            ) {
                                Text(
                                    "Pay Invoice",
                                    color = Color.Black,
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Invoice", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete this invoice? This action cannot be undone.",
                    fontFamily = SpaceGrotesk
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInvoice(currentInvoice.id)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
                ) {
                    Text(
                        "Delete",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(
                        "Cancel",
                        fontFamily = SpaceGrotesk
                    )
                }
            },
            containerColor = Color(0xFF1F1F1F),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    // Cancel Confirmation Dialog
    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text("Cancel Invoice", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to cancel this invoice?",
                    fontFamily = SpaceGrotesk
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateInvoiceStatus(currentInvoice.id, InvoiceStatus.CANCELLED)
                        showCancelConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A1F1F))
                ) {
                    Text(
                        "Cancel Invoice",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text(
                        "Keep",
                        fontFamily = SpaceGrotesk
                    )
                }
            },
            containerColor = Color(0xFF1F1F1F),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}

@Composable
fun LoadingView(
    message: String,
    textColor: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color(0xFF9FE870))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = textColor,
                fontFamily = fontFamily,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    isAddress: Boolean = false,
    onCopy: (() -> Unit)? = null,
    valueColor: Color = Color.White
) {
    Column {
        Text(
            text = label,
            color = Color(0xFF6B7280),
            fontFamily = SpaceGrotesk,
            fontSize = 14.sp
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
                fontFamily = SpaceGrotesk,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (isAddress && onCopy != null) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_copy),
                        contentDescription = "Copy",
                        tint = Color(0xFF9FE870),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceStatusChip(status: String) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "PAID" -> Triple(Color(0xFF1F3A1F), Color(0xFF9FE870), "Paid")
        "OVERDUE" -> Triple(Color(0xFF3A1F1F), Color(0xFFFF6B6B), "Expired")
        "EXPIRED" -> Triple(Color(0xFF3A1F1F), Color(0xFFFF6B6B), "Expired")
        "CANCELLED" -> Triple(Color(0xFF3A1F3A), Color(0xFFE882FC), "Cancelled")
        else -> Triple(Color(0xFF3A3A1F), Color(0xFFFFE066), "Pending")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = textColor,
            fontFamily = SpaceGrotesk,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}