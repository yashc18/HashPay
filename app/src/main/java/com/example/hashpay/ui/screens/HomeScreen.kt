package com.example.hashpay.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hashpay.R
import com.example.hashpay.ui.theme.HashPayTheme
import com.example.hashpay.ui.theme.SpaceGrotesk

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(com.example.hashpay.ui.viewmodels.HomeScreenViewModel::class.java)) {
            return com.example.hashpay.ui.viewmodels.HomeScreenViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: com.example.hashpay.ui.viewmodels.HomeScreenViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current)),
    onNavigate: (String) -> Unit
) {
    // Collect states from ViewModel
    val isWalletConnected by viewModel.isWalletConnected.collectAsState()
    val walletAddress by viewModel.walletAddress.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isWalletToggleOn by viewModel.isWalletToggleOn.collectAsState()

    // Show error if present
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Handle error display
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = innerPadding.calculateBottomPadding() + 80.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header - HashPay Logo and Welcome text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_hashpaylogo),
                    contentDescription = "HashPay Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .height(50.dp)
                        .width(116.dp)
                )
            }

            Text(
                text = "Welcome Back 👋",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = SpaceGrotesk
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet Section
            Text(
                text = "Your Wallet",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceGrotesk
            )

            // Wallet Card with updated design
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0A1D17),
                                    Color(0xFF143029),
                                    Color(0xFF1E4439)
                                )
                            )
                        )
                ) {
                    // Loading indicator
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF9FE870)
                        )
                    }

                    // Card content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        // Top row - MetaMask connection and logo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_metamask),
                                    contentDescription = "MetaMask",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MetaMask",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = SpaceGrotesk
                                )
                            }

                            Switch(
                                checked = isWalletToggleOn,
                                onCheckedChange = { viewModel.toggleWalletConnection(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4CAF50),
                                    uncheckedThumbColor = Color(0xFF9E9E9E),
                                    uncheckedTrackColor = Color(0xFF424242)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chip icon and balance
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_chip),
                                contentDescription = "Card Chip",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "ETH",
                                color = Color(0xFF9FE870),
                                fontSize = 18.sp,
                                fontFamily = SpaceGrotesk
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Balance amount - Use data from ViewModel
                        Text(
                            text = if (isWalletConnected) balance else "0.0",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGrotesk
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Card number and owner info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = if (isWalletConnected) viewModel.formatWalletAddress(walletAddress) else "Not connected",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    letterSpacing = 2.sp,
                                    fontFamily = SpaceGrotesk
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Wallet Owner",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontFamily = SpaceGrotesk
                                )
                            }
                        }
                    }
                }
            }

            // Action Cards Section
            Text(
                text = "Quick Actions",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceGrotesk,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            // Action Cards with updated design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionCard(
                    title = "Send",
                    backgroundColor = Color(0xFF1F1F1F),
                    iconColor = Color(0xFFFF6B6B),
                    icon = R.drawable.ic_send,
                    onClick = { onNavigate("send_money") }
                )

                ActionCard(
                    title = "Deposit",
                    backgroundColor = Color(0xFF1F1F1F),
                    iconColor = Color(0xFF9FE870),
                    icon = R.drawable.ic_desposit,
                    onClick = { onNavigate("deposit") }
                )

                ActionCard(
                    title = "Exchange",
                    backgroundColor = Color(0xFF1F1F1F),
                    iconColor = Color(0xFF06B6D4),
                    icon = R.drawable.ic_exchange,
                    onClick = { onNavigate("exchange") }
                )

                ActionCard(
                    title = "Request",
                    backgroundColor = Color(0xFF1F1F1F),
                    iconColor = Color(0xFFFFE066),
                    icon = R.drawable.ic_recieve,
                    onClick = { onNavigate("create_invoice") }
                )
            }

            // History Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGrotesk
                )

                TextButton(onClick = { onNavigate("history") }) {
                    Text(
                        "View All",
                        color = Color(0xFF9E9E9E),
                        fontFamily = SpaceGrotesk,
                        fontSize = 14.sp
                    )
                }
            }

            // Placeholder for transactions
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Empty state or sample transactions would go here
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isWalletConnected) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_empty),
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Connect your wallet to see transactions",
                                    color = Color(0xFF6B7280),
                                    fontFamily = SpaceGrotesk,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // This would be replaced with actual transactions
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color(0xFF9FE870),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Loading transactions...",
                                    color = Color(0xFF6B7280),
                                    fontFamily = SpaceGrotesk
                                )
                            }
                        }
                    }
                }
            }

            // Invoices Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Invoices",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGrotesk
                )

                TextButton(onClick = { onNavigate("invoice") }) {
                    Text(
                        "View All",
                        color = Color(0xFF9E9E9E),
                        fontFamily = SpaceGrotesk,
                        fontSize = 14.sp
                    )
                }
            }

            // Placeholder for invoices
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Empty state or sample transactions would go here
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_money),
                                contentDescription = null,
                                tint = Color(0xFFFFE066),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Create your first invoice",
                                color = Color(0xFF6B7280),
                                fontFamily = SpaceGrotesk,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onNavigate("create_invoice") },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9FE870))
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
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    backgroundColor: Color,
    iconColor: Color,
    icon: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            modifier = Modifier.size(70.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = SpaceGrotesk,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HashPayTheme {
        PreviewHomeScreen()
    }
}

@Composable
fun PreviewHomeScreen() {
    val previewContext = LocalContext.current
    val viewModelFactory = HomeViewModelFactory(previewContext)
    val previewViewModel: com.example.hashpay.ui.viewmodels.HomeScreenViewModel = viewModel(factory = viewModelFactory)

    HomeScreen(
        viewModel = previewViewModel,
        onNavigate = { /* Preview only - no navigation */ }
    )
}