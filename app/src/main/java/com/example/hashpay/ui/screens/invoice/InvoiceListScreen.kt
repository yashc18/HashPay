package com.example.hashpay.ui.screens.invoice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hashpay.R
import com.example.hashpay.data.database.entities.InvoiceStatus
import com.example.hashpay.ui.components.CustomTopAppBar
import com.example.hashpay.ui.components.EmptyStateView
import com.example.hashpay.ui.components.InvoiceItem
import com.example.hashpay.ui.components.LoadingView
import com.example.hashpay.ui.states.InvoiceUIState
import com.example.hashpay.ui.theme.neonGreen
import com.example.hashpay.ui.viewmodels.InvoiceViewModel
import com.example.hashpay.ui.viewmodels.InvoiceViewModelFactory

@Composable
fun InvoiceListScreen(
    onCreateInvoice: () -> Unit,
    onInvoiceClick: (Long) -> Unit,
    walletAddress: String
) {
    val context = LocalContext.current
    val viewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModelFactory(context))

    val invoices by viewModel.invoices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }

    val filteredInvoices = remember(invoices, walletAddress) {
        invoices.filter { invoice ->
            invoice.receiverAddress == walletAddress || invoice.senderAddress == walletAddress
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CustomTopAppBar(
                title = "Invoices",
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter),
                            contentDescription = "Filter Invoices",
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
                        color = Color(0xFF330000),
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
            } else if (filteredInvoices.isEmpty()) {
                EmptyStateView(
                    message = "No invoices found",
                    buttonText = "Create Invoice",
                    onButtonClick = onCreateInvoice
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(filteredInvoices) { invoice ->
                        InvoiceItem(
                            invoice = invoice,
                            onClick = onInvoiceClick
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateInvoice,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = neonGreen,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Invoice"
            )
        }

        if (showFilterDialog) {
            FilterDialog(
                currentFilter = filterStatus,
                onFilterSelected = { viewModel.setFilterStatus(it) },
                onDismiss = { showFilterDialog = false }
            )
        }
    }
}

@Composable
fun FilterDialog(
    currentFilter: InvoiceStatus?,
    onFilterSelected: (InvoiceStatus?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Invoices",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                FilterOption(
                    title = "All Invoices",
                    selected = currentFilter == null,
                    onClick = { onFilterSelected(null); onDismiss() }
                )
                FilterOption(
                    title = "Pending",
                    selected = currentFilter == InvoiceStatus.PENDING,
                    onClick = { onFilterSelected(InvoiceStatus.PENDING); onDismiss() }
                )
                FilterOption(
                    title = "Paid",
                    selected = currentFilter == InvoiceStatus.PAID,
                    onClick = { onFilterSelected(InvoiceStatus.PAID); onDismiss() }
                )
                FilterOption(
                    title = "Overdue",
                    selected = currentFilter == InvoiceStatus.OVERDUE,
                    onClick = { onFilterSelected(InvoiceStatus.OVERDUE); onDismiss() }
                )
                FilterOption(
                    title = "Cancelled",
                    selected = currentFilter == InvoiceStatus.CANCELLED,
                    onClick = { onFilterSelected(InvoiceStatus.CANCELLED); onDismiss() }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        textContentColor = Color.White
    )
}

@Composable
fun FilterOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = Color.White)
    }
}