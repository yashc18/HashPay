package com.example.hashpay.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.hashpay.R
import com.example.hashpay.ui.theme.SpaceGrotesk
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
    val scrollState = rememberScrollState()

    // Collect states
    val isWalletConnected by viewModel.isWalletConnected.collectAsState()
    val walletAddress by viewModel.walletAddress.collectAsState()
    val showConnectionDialog by viewModel.showConnectionDialog.collectAsState()
    val recipientAddress by viewModel.recipientAddress.collectAsState()
    val amountEth by viewModel.amountEth.collectAsState()
    val transactionResult by viewModel.transactionResult.collectAsState()
    val isTransactionInProgress by viewModel.isTransactionInProgress.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()

    // Local state for toggles
    var hasInvoiceAttached by remember { mutableStateOf(false) }

    // Remember contacts list with profile images
    val contactsList = remember {
        listOf(
            "Mahesh" to R.drawable.ic_person,
            "Dinesh" to R.drawable.ic_person,
            "Roshan" to R.drawable.ic_person,
            "Sarah" to R.drawable.ic_person,
            "John" to R.drawable.ic_person
        )
    }

    // Transaction result listener
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
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Send Money",
                        color = Color.White,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                windowInsets = WindowInsets(0, 0, 0, 0) // Fixed Zero error
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // Fixed Zero error
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Recent Contacts Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Contacts",
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                TextButton(onClick = { /* View all */ }) {
                    Text(
                        "View All",
                        color = Color(0xFF9E9E9E),
                        fontFamily = SpaceGrotesk,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contacts Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ContactItem(
                        name = "Add New",
                        isAddNew = true,
                        onClick = { /* Add new contact */ }
                    )
                }

                items(contactsList) { (name, _) ->
                    ContactItem(
                        name = name,
                        onClick = {
                            viewModel.setRecipientAddress("0x" + name.hashCode().toString(16))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recipient Address Input
            Surface(
                color = Color(0xFF1F1F1F),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = recipientAddress,
                    onValueChange = { viewModel.setRecipientAddress(it) },
                    placeholder = {
                        Text(
                            "Recipient Address",
                            color = Color(0xFF6B7280),
                            fontFamily = SpaceGrotesk
                        )
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
                    ),
                    trailingIcon = {
                        IconButton(onClick = onScanQrCode) {
                            Icon(
                                painter = painterResource(id = R.drawable.qr_scanner),
                                contentDescription = "Scan QR",
                                tint = Color(0xFF9E9E9E)
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input with dropdown
            Surface(
                color = Color(0xFF1F1F1F),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = amountEth,
                    onValueChange = { viewModel.setAmountEth(it) },
                    placeholder = {
                        Text(
                            "Amount (ETH)",
                            color = Color(0xFF6B7280),
                            fontFamily = SpaceGrotesk
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    ),
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                "ETH",
                                color = Color.White,
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color(0xFF9E9E9E)
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Amount Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val amounts = listOf("0.01", "0.05", "0.1", "0.5")
                amounts.forEach { amount ->
                    QuickAmountButton(
                        amount = amount,
                        isSelected = amountEth == amount,
                        onClick = { viewModel.setAmountEth(amount) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Transaction Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Estimated Gas Fee: 0.001 ETH",
                    color = Color(0xFF6B7280),
                    fontFamily = SpaceGrotesk,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Current Balance: 1.234 ETH",
                    color = Color(0xFF6B7280),
                    fontFamily = SpaceGrotesk,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MetaMask Toggle - FIXED SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_metamask),
                        contentDescription = "MetaMask",
                        tint = Color(0xFFF6851B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Use MetaMask",
                        color = Color.White,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }

                Switch(
                    checked = isWalletConnected,
                    onCheckedChange = { isEnabled ->
                        if (isEnabled && !isWalletConnected) {
                            viewModel.setShowConnectionDialog(true)
                        } else if (!isEnabled && isWalletConnected) {
                            // You might need to add a disconnectWallet method to your ViewModel
                            // viewModel.disconnectWallet()
                            // For now, just show a message
                            Toast.makeText(context, "Wallet disconnected", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color(0xFF9E9E9E),
                        uncheckedTrackColor = Color(0xFF424242)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Invoice Attachment Option
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hasInvoiceAttached = !hasInvoiceAttached },
                colors = CardDefaults.cardColors(
                    containerColor = if (hasInvoiceAttached) Color(0xFF1F3A1F) else Color(0xFF1F1F1F)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_link),
                        contentDescription = "Attach Invoice",
                        tint = if (hasInvoiceAttached) Color(0xFF4CAF50) else Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (hasInvoiceAttached) "Invoice Attached" else "Attach Invoice",
                            color = Color.White,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Medium
                        )
                        if (hasInvoiceAttached) {
                            Text(
                                "Invoice #12345",
                                color = Color(0xFF6B7280),
                                fontFamily = SpaceGrotesk,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (hasInvoiceAttached) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Attached",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Transaction Result Feedback
            AnimatedVisibility(
                visible = transactionResult is TransactionState.Success ||
                        transactionResult is TransactionState.Error,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (transactionResult) {
                            is TransactionState.Success -> Color(0xFF1F3A1F)
                            is TransactionState.Error -> Color(0xFF3A1F1F)
                            else -> Color(0xFF1F1F1F)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = when (transactionResult) {
                                        is TransactionState.Success -> R.drawable.ic_check
                                        else -> R.drawable.ic_error
                                    }
                                ),
                                contentDescription = null,
                                tint = when (transactionResult) {
                                    is TransactionState.Success -> Color(0xFF9FE870)
                                    else -> Color(0xFFFF6B6B)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (transactionResult) {
                                        is TransactionState.Success -> "Transaction Successful"
                                        is TransactionState.Error -> "Transaction Failed"
                                        else -> ""
                                    },
                                    color = Color.White,
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Medium
                                )

                                when (val result = transactionResult) {
                                    is TransactionState.Success -> {
                                        if (!result.message.startsWith("Connected:")) {
                                            Text(
                                                text = result.message,
                                                color = Color(0xFF6B7280),
                                                fontFamily = SpaceGrotesk,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    is TransactionState.Error -> {
                                        Text(
                                            text = result.message,
                                            color = Color(0xFF6B7280),
                                            fontFamily = SpaceGrotesk,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }

                        // Only show transaction hash for successful transactions
                        if (transactionResult is TransactionState.Success) {
                            val hash = (transactionResult as? TransactionState.Success)?.message
                            if (!hash.isNullOrEmpty() && !hash.startsWith("Connected:")) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Hash: ${shortenAddress(hash)}",
                                        color = Color(0xFF6B7280),
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = {
                                            val clipboard = ContextCompat.getSystemService(
                                                context,
                                                ClipboardManager::class.java
                                            )
                                            val clip = ClipData.newPlainText("Hash", hash)
                                            clipboard?.setPrimaryClip(clip)
                                            Toast.makeText(
                                                context,
                                                "Hash copied to clipboard",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_copy),
                                            contentDescription = "Copy hash",
                                            tint = Color(0xFF9FE870)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Send Button
            Button(
                onClick = {
                    if (isWalletConnected) {
                        viewModel.sendTransaction()
                    } else {
                        viewModel.setShowConnectionDialog(true)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9FE870),
                    disabledContainerColor = Color(0xFF424242)
                ),
                enabled = !isTransactionInProgress
            ) {
                if (isTransactionInProgress) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Next",
                        color = Color.Black,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Connection Dialog
    if (showConnectionDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isConnecting) viewModel.setShowConnectionDialog(false)
            },
            title = {
                Text(
                    "Connect to MetaMask",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_metamask),
                        contentDescription = null,
                        tint = Color(0xFFF6851B),
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    )
                    Text(
                        "You need to connect your MetaMask wallet before sending ETH.",
                        fontFamily = SpaceGrotesk
                    )

                    if (isConnecting) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF9FE870)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Connecting to MetaMask...",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontFamily = SpaceGrotesk
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.connectWallet() },
                    enabled = !isConnecting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9FE870))
                ) {
                    Text(
                        "Connect",
                        color = Color.Black,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.setShowConnectionDialog(false) },
                    enabled = !isConnecting
                ) {
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
}

@Composable
fun ContactItem(
    name: String,
    isAddNew: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable(onClick = onClick)
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val scale = animateFloatAsState(
            targetValue = if (isPressed) 0.9f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "scaleAnimation"
        )

        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(scale.value)
                .clip(CircleShape)
                .background(
                    if (isAddNew) Color(0xFF2A2A2A) else getContactColor(name)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isAddNew) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add New",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = name.first().uppercase(),
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isAddNew) "Add New" else name,
            color = Color.White,
            fontFamily = SpaceGrotesk,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun QuickAmountButton(
    amount: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF2A2A2A) else Color(0xFF1A1A1A),
            contentColor = if (isSelected) Color(0xFF9FE870) else Color(0xFF6B7280)
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(
            "$amount ETH",
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

private fun getContactColor(name: String): Color {
    return when (name.lowercase().first()) {
        in 'a'..'f' -> Color(0xFF6366F1) // Indigo
        in 'g'..'l' -> Color(0xFF8B5CF6) // Purple
        in 'm'..'r' -> Color(0xFF06B6D4) // Cyan
        else -> Color(0xFF10B981) // Emerald
    }
}

private fun shortenAddress(address: String): String {
    return if (address.length > 12) {
        "${address.take(6)}...${address.takeLast(4)}"
    } else {
        address
    }
}

@Preview(showBackground = true)
@Composable
fun SendMoneyScreenPreview() {
    val context = LocalContext.current
    val viewModel = remember { SendMoneyViewModel(context) }

    SendMoneyScreen(
        viewModel = viewModel,
        onBackClick = {},
        onScanQrCode = {}
    )
}