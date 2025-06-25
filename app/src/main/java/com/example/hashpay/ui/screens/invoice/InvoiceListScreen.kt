package com.example.hashpay.ui.screens.invoice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hashpay.R
import com.example.hashpay.data.database.entities.Invoice
import com.example.hashpay.data.database.entities.InvoiceStatus
import com.example.hashpay.ui.states.InvoiceUIState
import com.example.hashpay.ui.theme.SpaceGrotesk
import com.example.hashpay.ui.viewmodels.InvoiceViewModel
import com.example.hashpay.ui.viewmodels.InvoiceViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    onCreateInvoice: () -> Unit,
    onInvoiceClick: (Long) -> Unit,
    walletAddress: String,
    viewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModelFactory(LocalContext.current))
) {
    val invoices by viewModel.invoices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()

    var showFilterOptions by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Track selected filters
    val selectedFilters = remember { mutableStateListOf<String>() }

    LaunchedEffect(walletAddress) {
        viewModel.loadInvoices()
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Invoices",
                        color = Color.White,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F0F)
                ),
                actions = {
                    IconButton(onClick = { showFilterOptions = !showFilterOptions }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter),
                            contentDescription = "Filter",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = onCreateInvoice) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create Invoice",
                            tint = Color.White
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateInvoice,
                containerColor = Color(0xFF9FE870),
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Invoice")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
                    Surface(
                color = Color(0xFF1F1F1F),
                shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { newValue ->
                        searchQuery = newValue
                        // Note: Add search implementation in ViewModel
                    },
                    placeholder = {
                        Text(
                            "Search invoices...",
                            color = Color(0xFF6B7280),
                            fontFamily = SpaceGrotesk
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF9E9E9E)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    // Note: Clear search in ViewModel
                                }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1F1F1F),
                        unfocusedContainerColor = Color(0xFF1F1F1F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = SpaceGrotesk,
                        fontSize = 16.sp
                    )
                )
            }

            // Filter Options
            AnimatedVisibility(
                visible = showFilterOptions,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FilterOptions(
                    currentFilters = selectedFilters,
                    onFilterSelected = { filter ->
                        if (selectedFilters.contains(filter)) {
                            selectedFilters.remove(filter)
                        } else {
                            selectedFilters.add(filter)
                        }

                        // Apply filters based on selection
                        when (filter) {
                            "Paid" -> viewModel.setFilterStatus(InvoiceStatus.PAID)
                            "Unpaid" -> viewModel.setFilterStatus(InvoiceStatus.PENDING)
                            "Expired" -> viewModel.setFilterStatus(InvoiceStatus.OVERDUE)
                            else -> viewModel.setFilterStatus(null)
                        }
                    }
                )
            }

            // Content based on state
            when {
                isLoading -> {
                    LoadingIndicator(message = "Loading invoices...")
                }
                errorMessage != null -> {
                    ErrorState(
                        message = errorMessage ?: "Unknown error",
                        onRetry = {
                            viewModel.clearErrorMessage()
                            viewModel.loadInvoices()
                        }
                    )
                }
                invoices.isEmpty() -> {
                    EmptyState(
                    message = "No invoices found",
                        description = "Create your first invoice by tapping the + button",
                        onActionClick = onCreateInvoice
                )
                }
                else -> {
                LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(invoices) { invoiceUiState ->
                            SafeInvoiceItem(
                                invoiceUiState = invoiceUiState,
                                onInvoiceClick = onInvoiceClick,
                                onError = { error -> viewModel.setErrorMessage(error) }
                            )
                        }

                        // Add some space at the bottom for the FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SafeInvoiceItem(
    invoiceUiState: InvoiceUIState,
    onInvoiceClick: (Long) -> Unit,
    onError: (String) -> Unit
) {
    val invoice = try {
        invoiceUiState.toInvoice()
    } catch (e: Exception) {
        onError("Error processing invoice: ${e.message}")
        return
    }

    Card(
            modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    if (invoiceUiState.id > 0) {
                        onInvoiceClick(invoiceUiState.id)
                    } else {
                        onError("Invalid invoice ID")
                    }
                } catch (e: Exception) {
                    onError("Error navigating to invoice: ${e.message}")
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${invoice.id}",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )

                StatusChip(status = invoice.status.toString())
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            Text(
                text = "${invoice.amount} ETH",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            invoice.description?.let {
                if (it.isNotEmpty()) {
                    Text(
                        text = it,
                        fontFamily = SpaceGrotesk,
                        color = Color(0xFF9E9E9E),
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Divider(color = Color(0xFF2A2A2A))

            Spacer(modifier = Modifier.height(8.dp))

            // Footer with date and recipient
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(invoice.createdAt),
                        fontFamily = SpaceGrotesk,
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatAddress(invoice.receiverAddress),
                        fontFamily = SpaceGrotesk,
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Rest of the composables remain the same

@Composable
fun LoadingIndicator(message: String) {
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
                color = Color.White,
                fontFamily = SpaceGrotesk,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    description: String,
    onActionClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_empty),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.White,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = Color(0xFF9E9E9E),
                fontFamily = SpaceGrotesk,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9FE870)
                )
            ) {
                Text(
                    "Create Invoice",
                    color = Color.Black,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_error),
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error: $message",
                color = Color.White,
                fontFamily = SpaceGrotesk,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9FE870)
                )
            ) {
                Text(
                    "Retry",
                    color = Color.Black,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FilterOptions(
    currentFilters: List<String>,
    onFilterSelected: (String) -> Unit
) {
    val filterOptions = listOf("Paid", "Unpaid", "Expired", "Recent")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Filter By:",
            color = Color.White,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            filterOptions.forEach { filter ->
                val isSelected = currentFilters.contains(filter)
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterSelected(filter) },
                    label = {
                        Text(
                            filter,
                            fontFamily = SpaceGrotesk,
                            fontSize = 14.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF9FE870),
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0xFF1F1F1F),
                        labelColor = Color(0xFF9E9E9E)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color(0xFF2A2A2A),
                        selectedBorderColor = Color.Transparent,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "paid" -> Triple(Color(0xFF1F3A1F), Color(0xFF9FE870), "Paid")
        "expired" -> Triple(Color(0xFF3A1F1F), Color(0xFFFF6B6B), "Expired")
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

// Helper functions
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatAddress(address: String): String {
    return if (address.length > 10) {
        "${address.take(6)}...${address.takeLast(4)}"
    } else {
        address
    }
}

// Extension function to convert InvoiceUIState to Invoice
private fun InvoiceUIState.toInvoice(): Invoice {
    return Invoice(
        id = this.id,
        senderAddress = this.senderAddress,
        receiverAddress = this.receiverAddress,
        amount = this.amount,
        description = this.description,
        status = this.status,
        createdAt = this.createdAt,
        paidAt = this.paidAt,
        dueDate = this.dueDate,
        transactionHash = this.transactionHash
    )
}