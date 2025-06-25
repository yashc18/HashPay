package com.example.hashpay.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hashpay.R
import com.example.hashpay.ui.theme.SpaceGrotesk
import com.example.hashpay.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onViewTransactions: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    // Collect states
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val walletAddress by viewModel.walletAddress.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val isWalletConnected by viewModel.isWalletConnected.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditEmailDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share profile */ }) {
                        Icon(Icons.Default.Share, "Share", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F0F)),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Profile Header
            ProfileHeader(
                userName = userName,
                userEmail = userEmail,
                onEditName = { showEditNameDialog = true },
                onEditEmail = { showEditEmailDialog = true }
            )

            // Wallet Card
            WalletCard(
                isWalletConnected = isWalletConnected,
                walletAddress = walletAddress,
                walletBalance = walletBalance,
                onConnectionChange = { viewModel.updateWalletConnection(it) }
            )

            // Stats Card
            StatsCard(
                stats = stats,
                onViewTransactions = onViewTransactions
            )

            // Settings Card
            SettingsCard(
                settings = settings,
                onToggleSetting = { key -> viewModel.toggleSetting(key) },
                onLogout = { showLogoutDialog = true }
            )
        }
    }

    // Dialogs
    if (showEditNameDialog) {
        EditDialog(
            title = "Edit Profile Name",
            initialValue = userName,
            onConfirm = { viewModel.updateUserName(it) },
            onDismiss = { showEditNameDialog = false }
        )
    }

    if (showEditEmailDialog) {
        EditDialog(
            title = "Edit Email Address",
            initialValue = userEmail,
            onConfirm = { viewModel.updateUserEmail(it) },
            onDismiss = { showEditEmailDialog = false }
        )
    }

    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Confirm Logout",
            message = "Are you sure you want to logout from your HashPay account?",
            confirmText = "Logout",
            confirmColor = Color(0xFFFF6B6B),
            onConfirm = onLogout,
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    onEditName: () -> Unit,
    onEditEmail: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF9FE870))
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F1F1F)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).uppercase(),
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userName,
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                IconButton(
                    onClick = onEditName,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "Edit name",
                        tint = Color(0xFF9FE870),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userEmail,
                    color = Color(0xFF9E9E9E),
                    fontFamily = SpaceGrotesk,
                    fontSize = 14.sp
                )
                IconButton(
                    onClick = onEditEmail,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "Edit email",
                        tint = Color(0xFF9FE870),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WalletCard(
    isWalletConnected: Boolean,
    walletAddress: String,
    walletBalance: java.math.BigDecimal,
    onConnectionChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_metamask),
                        contentDescription = "Wallet",
                        tint = Color(0xFFF6851B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "MetaMask Wallet",
                        color = Color.White,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Medium
                    )
                }

                Switch(
                    checked = isWalletConnected,
                    onCheckedChange = onConnectionChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color(0xFF9E9E9E),
                        uncheckedTrackColor = Color(0xFF424242)
                    )
                )
            }

            if (isWalletConnected) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFF3A3A3A))
                Spacer(modifier = Modifier.height(12.dp))

                // Wallet Address
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Address", color = Color(0xFF9E9E9E), fontFamily = SpaceGrotesk, fontSize = 12.sp)
                        Text(
                            shortenAddress(walletAddress),
                            color = Color.White,
                            fontFamily = SpaceGrotesk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = {
                        val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(ClipData.newPlainText("Wallet Address", walletAddress))
                        Toast.makeText(context, "Address copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(painterResource(R.drawable.ic_copy), "Copy", tint = Color(0xFF9FE870))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Balance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Balance", color = Color(0xFF9E9E9E), fontFamily = SpaceGrotesk, fontSize = 12.sp)
                        Text(
                            "$walletBalance ETH",
                            color = Color.White,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { /* Refresh balance */ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9FE870)),
                        border = BorderStroke(1.dp, Color(0xFF9FE870)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, "Refresh", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh", fontFamily = SpaceGrotesk, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    stats: Map<String, java.math.BigDecimal>,
    onViewTransactions: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Statistics",
                color = Color.White,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    icon = R.drawable.ic_send,
                    label = "Sent",
                    value = "${stats["sent"]} ETH",
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    icon = R.drawable.ic_recieve,
                    label = "Received",
                    value = "${stats["received"]} ETH",
                    tint = Color(0xFF4ECDC4),
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    icon = R.drawable.baseline_history_24,
                    label = "Transactions",
                    value = stats["transactions"]?.toInt()?.toString() ?: "0",
                    tint = Color(0xFF9FE870),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onViewTransactions,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "View All Transactions",
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    settings: Map<String, Boolean>,
    onToggleSetting: (String) -> Unit,
    onLogout: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Settings",
                color = Color.White,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsItem(
                iconRes = R.drawable.ic_darkmode,
                title = "Dark Mode",
                isToggled = settings["darkMode"] ?: true,
                onToggle = { onToggleSetting("darkMode") }
            )

            SettingsItem(
                iconRes = R.drawable.ic_notifications,
                title = "Notifications",
                isToggled = settings["notifications"] ?: true,
                onToggle = { onToggleSetting("notifications") }
            )

            SettingsItem(
                iconRes = R.drawable.ic_fingerprint,
                title = "Biometric Authentication",
                isToggled = settings["biometric"] ?: false,
                onToggle = { onToggleSetting("biometric") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A1F1F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = "Logout"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Logout",
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatItem(icon: Int, label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(icon), label, tint = tint, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(value, color = Color.White, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFF9E9E9E), fontFamily = SpaceGrotesk, fontSize = 12.sp)
    }
}

@Composable
fun SettingsItem(iconRes: Int, title: String, isToggled: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color.White, fontFamily = SpaceGrotesk)
        }

        Switch(
            checked = isToggled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color(0xFF9E9E9E),
                uncheckedTrackColor = Color(0xFF424242)
            )
        )
    }
}

@Composable
fun EditDialog(title: String, initialValue: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var inputValue by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF9FE870),
                    unfocusedBorderColor = Color(0xFF444444),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(fontFamily = SpaceGrotesk)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputValue.isNotBlank()) onConfirm(inputValue)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9FE870))
            ) {
                Text("Save", color = Color.Black, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = SpaceGrotesk)
            }
        },
        containerColor = Color(0xFF1F1F1F),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) },
        text = { Text(message, fontFamily = SpaceGrotesk) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) {
                Text(confirmText, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = SpaceGrotesk)
            }
        },
        containerColor = Color(0xFF1F1F1F),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

private fun shortenAddress(address: String): String {
    return if (address.length > 12) {
        "${address.take(6)}...${address.takeLast(4)}"
    } else {
        address
    }
}