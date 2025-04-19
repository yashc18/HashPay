package com.example.hashpay.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hashpay.R
import com.example.hashpay.ui.viewmodels.SendMoneyViewModel
import com.example.hashpay.ui.viewmodels.TransactionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyScreen(
    viewModel: SendMoneyViewModel,
    onBackClick: () -> Unit = {},
    onScanQrCode: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isWalletConnected by viewModel.isWalletConnected.collectAsState()
    val walletAddress by viewModel.walletAddress.collectAsState()
    val showConnectionDialog by viewModel.showConnectionDialog.collectAsState()
    val recipientAddress by viewModel.recipientAddress.collectAsState()
    val amountEth by viewModel.amountEth.collectAsState()
    val transactionResult by viewModel.transactionResult.collectAsState()
    val isTransactionInProgress by viewModel.isTransactionInProgress.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()

    val scrollState = rememberScrollState()

    // MetaMask wallet connection dialog
    if (showConnectionDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isConnecting) viewModel.setShowConnectionDialog(false)
            },
            title = { Text("Connect to MetaMask") },
            text = {
                Column {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_wallet),
                        contentDescription = null,
                        tint = Color(0xFFB2FF59),
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    )
                    Text("You need to connect your MetaMask wallet before sending ETH.")

                    if (isConnecting) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFB2FF59)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Connecting to MetaMask...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.connectWallet() },
                    enabled = !isConnecting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB2FF59))
                ) {
                    Text("Connect", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.setShowConnectionDialog(false) },
                    enabled = !isConnecting
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF222222),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    // Transaction state listener effect
    LaunchedEffect(transactionResult) {
        when (val result = transactionResult) {
            is TransactionState.Success -> {
                if (!result.message.startsWith("Connected:")) {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            is TransactionState.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Send ETH",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onScanQrCode) {
                        Icon(
                            painter = painterResource(id = R.drawable.qr_scanner),
                            contentDescription = "Scan QR Code",
                            tint = Color(0xFFB2FF59)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFF121212),
                shadowElevation = 8.dp,
            ) {
                Button(
                    onClick = { viewModel.sendTransaction() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(isWalletConnected) Color(0xFFB2FF59) else Color(0xFF666666)
                    )
                ) {
                    if (isTransactionInProgress) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isWalletConnected) "Send ETH" else "Connect Wallet First",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                )
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Wallet status card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_wallet),
                                contentDescription = null,
                                tint = if (isWalletConnected) Color(0xFFB2FF59) else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isWalletConnected) "MetaMask Connected" else "MetaMask Wallet",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                if (isWalletConnected && walletAddress.isNotBlank()) {
                                    Text(
                                        text = formatWalletAddress(walletAddress),
                                        color = Color(0xFFB2FF59),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (!isWalletConnected) {
                                    viewModel.setShowConnectionDialog(true)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWalletConnected) Color(0xFF66BB6A) else Color(0xFFB2FF59)
                            ),
                            modifier = Modifier.width(IntrinsicSize.Min)
                        ) {
                            Text(
                                text = if (isWalletConnected) "Connected" else "Connect",
                                color = if (isWalletConnected) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            // Contacts section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Contacts",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

                TextButton(onClick = { /* View all contacts */ }) {
                    Text(
                        "View All",
                        color = Color(0xFFB2FF59),
                        fontSize = 14.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.width(4.dp))
                ContactItem(name = "Add New", isAddNew = true, onClick = { /* TODO */ })
                listOf("Mahesh", "Dinesh", "Roshan").forEach {
                    ContactItem(
                        name = it,
                        onClick = { viewModel.setRecipientAddress("0x" + it.hashCode().toString(16)) }
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Transaction Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Transaction Details",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = recipientAddress,
                        onValueChange = { viewModel.setRecipientAddress(it) },
                        label = { Text("Recipient Address", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB2FF59),
                            unfocusedBorderColor = Color(0xFF444444),
                            cursorColor = Color.White,
                            focusedLabelColor = Color(0xFFB2FF59)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = onScanQrCode) {
                                Icon(
                                    painter = painterResource(id = R.drawable.qr_scanner),
                                    contentDescription = "Scan QR Code",
                                    tint = Color(0xFFB2FF59)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amountEth,
                        onValueChange = { viewModel.setAmountEth(it) },
                        label = { Text("Amount (ETH)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB2FF59),
                            unfocusedBorderColor = Color(0xFF444444),
                            cursorColor = Color.White,
                            focusedLabelColor = Color(0xFFB2FF59)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Text(
                                "ETH",
                                modifier = Modifier.padding(end = 12.dp),
                                color = Color(0xFFB2FF59),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // Quick Amount Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Quick Amounts",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("0.01", "0.05", "0.1", "0.5", "1.0").forEach { amount ->
                    OutlinedButton(
                        onClick = { viewModel.setAmountEth(amount) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFB2FF59),
                            containerColor = if (amountEth == amount) Color(0xFF2A2A2A) else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, SolidColor(Color(0xFF444444)))
                    ) {
                        Text(
                            text = "$amount ETH",
                            color = if (amountEth == amount) Color(0xFFB2FF59) else Color.White
                        )
                    }
                }
            }

            // Transaction Result
            AnimatedVisibility(
                visible = transactionResult is TransactionState.Success || transactionResult is TransactionState.Error,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                when (val state = transactionResult) {
                    is TransactionState.Success -> {
                        if (!state.message.startsWith("Connected:")) {  // Don't show connection success here
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A3300))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check),
                                        contentDescription = "Success",
                                        tint = Color(0xFFB2FF59),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(state.message, color = Color.White)
                                }
                            }
                        }
                    }
                    is TransactionState.Error -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF330000))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_error),
                                    contentDescription = "Error",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(state.message, color = Color.White)
                            }
                        }
                    }
                    else -> {}
                }
            }

            // Add extra space at the bottom
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ContactItem(name: String, isAddNew: Boolean = false, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isAddNew) Color(0xFF333333) else getGradientColorForName(name)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isAddNew) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add New",
                    tint = Color.White
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = name,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isAddNew) "Add New" else name,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(68.dp)
        )
    }
}

// Helper function to format wallet address
private fun formatWalletAddress(address: String): String {
    if (address.isBlank()) return "Not connected"
    if (address.length < 10) return address
    return "${address.take(6)}...${address.takeLast(4)}"
}

// Helper function to generate consistent colors for contacts
private fun getGradientColorForName(name: String): Color {
    return when (name.lowercase().first()) {
        in 'a'..'f' -> Color(0xFF5E35B1) // Purple
        in 'g'..'l' -> Color(0xFF1E88E5) // Blue
        in 'm'..'r' -> Color(0xFF43A047) // Green
        else -> Color(0xFFE53935) // Red
    }
}