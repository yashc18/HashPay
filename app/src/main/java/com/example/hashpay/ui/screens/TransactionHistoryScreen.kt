package com.example.hashpay.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hashpay.R
import com.example.hashpay.data.database.entities.Transaction
import com.example.hashpay.ui.viewmodels.TransactionHistoryViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: TransactionHistoryViewModel = viewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    val currentFilter = remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Ensure content extends fully
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Transaction History",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter),
                            contentDescription = "Filter",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                windowInsets = WindowInsets(0, 0, 0, 0) // No insets for topBar
            )
        }
    ) { innerPadding ->
        if (showFilterDialog) {
            FilterDialog(
                onDismiss = { showFilterDialog = false },
                onFilterSelected = { status ->
                    currentFilter.value = status
                    viewModel.filterTransactions(status)
                },
                currentFilter = currentFilter.value
            )
        }

        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isLoading),
            onRefresh = {
                Log.d("TransactionHistory", "Manual refresh triggered")
                viewModel.refreshTransactions()
            }
        ) {
            if (transactions.isEmpty()) {
                EmptyTransactionView(
                    onRefresh = { viewModel.refreshTransactions() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            start = 16.dp,
                            end = 16.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp) // Extended bottom padding to prevent cutoff
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTransactionView(
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_empty),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No transactions yet",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your transaction history will appear here",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onRefresh,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFB2FF59)
                ),
                border = BorderStroke(1.dp, Color(0xFFB2FF59))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh")
            }
        }
    }
}

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onFilterSelected: (String?) -> Unit,
    currentFilter: String?
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Filter Transactions",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                FilterOption(
                    text = "All Transactions",
                    icon = R.drawable.ic_exchange,
                    isSelected = currentFilter == null,
                    onClick = {
                        onFilterSelected(null)
                        onDismiss()
                    }
                )

                FilterOption(
                    text = "Completed",
                    icon = R.drawable.ic_check,
                    isSelected = currentFilter == "completed",
                    onClick = {
                        onFilterSelected("completed")
                        onDismiss()
                    }
                )

                FilterOption(
                    text = "Pending",
                    icon = R.drawable.ic_pending,
                    isSelected = currentFilter == "pending",
                    onClick = {
                        onFilterSelected("pending")
                        onDismiss()
                    }
                )

                FilterOption(
                    text = "Failed",
                    icon = R.drawable.ic_error,
                    isSelected = currentFilter == "failed",
                    onClick = {
                        onFilterSelected("failed")
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel", color = Color(0xFFB2FF59))
                }
            }
        }
    }
}

@Composable
fun FilterOption(
    text: String,
    icon: Int,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF353535) else Color(0xFF252525),
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) Color(0xFFB2FF59) else Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                color = if (isSelected) Color(0xFFB2FF59) else Color.White
            )

            if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFB2FF59)
                )
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    viewModel: TransactionHistoryViewModel
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Transaction type and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(
                            id = if (transaction.type == "send")
                                R.drawable.ic_send else R.drawable.ic_recieve
                        ),
                        contentDescription = null,
                        tint = if (transaction.type == "send") Color(0xFFFF5722) else Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (transaction.type == "send") "Sent" else "Received",
                        color = if (transaction.type == "send") Color(0xFFFF5722) else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                TransactionStatusChip(status = transaction.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount with larger, bolder text
            Text(
                text = "${if (transaction.type == "send") "-" else "+"} ${transaction.amountInEth} ETH",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFF333333))
            Spacer(modifier = Modifier.height(16.dp))

            // Address info with better formatting
            AddressRow("From", transaction.fromAddress)
            Spacer(modifier = Modifier.height(12.dp))
            AddressRow("To", transaction.toAddress)

            Spacer(modifier = Modifier.height(12.dp))

            // Message if available
            transaction.message?.let {
                if (it.isNotEmpty()) {
                    Surface(
                        color = Color(0xFF252525),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Message",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Date with icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatDate(transaction.timestamp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            // Transaction hash with verify button if completed
            transaction.txHash?.let {
                if (it.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Etherscan verification button with neon green outline
                    OutlinedButton(
                        onClick = { openTransactionOnEtherscan(context, viewModel.getEtherscanUrl(it)) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFB2FF59)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFB2FF59)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp) // More rounded for button
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_link),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify on Etherscan")
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionStatusChip(status: String) {
    val (backgroundColor, textColor, text) = when (status) {
        "completed" -> Triple(Color(0xFF0A3300), Color(0xFFB2FF59), "Completed")
        "failed" -> Triple(Color(0xFF330000), Color(0xFFFF9999), "Failed")
        else -> Triple(Color(0xFF333300), Color(0xFFFFFF99), "Pending")
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AddressRow(label: String, address: String) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatAddress(address),
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Copy button
        IconButton(
            onClick = {
                val clipboard = ContextCompat.getSystemService(
                    context,
                    ClipboardManager::class.java
                )
                val clip = ClipData.newPlainText("Address", address)
                clipboard?.setPrimaryClip(clip)

                Toast.makeText(
                    context,
                    "Address copied to clipboard",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_copy),
                contentDescription = "Copy address",
                tint = Color(0xFFB2FF59),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// Helper function to open transaction on Etherscan
fun openTransactionOnEtherscan(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

fun formatAddress(address: String): String {
    return if (address.length > 12) {
        "${address.take(6)}...${address.takeLast(4)}"
    } else {
        address
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun String.capitalize(locale: Locale): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}