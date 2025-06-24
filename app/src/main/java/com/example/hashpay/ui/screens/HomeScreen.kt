package com.example.hashpay.ui.screens

import android.content.Context
import android.graphics.Paint.Style
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Added missing import
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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
import com.example.hashpay.ui.viewmodels.HomeScreenViewModel

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            return HomeScreenViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current)),
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
        containerColor = colorResource(id = R.color.GreenishBlack)
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
                        .clip(RoundedCornerShape(0.dp))
                )
            }

            Text(
                text = "Welcome, Back 👋",
                style = TextStyle(
                    fontFamily = SpaceGrotesk,
                    color = Color.White,
                    fontSize = 14.sp,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet Section
            Text(
                text = "Your Wallet",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    fontFamily = SpaceGrotesk,
                )
            )

            // Wallet Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF004D40),
                                    Color(0xFF00796B),
                                    Color(0xFF009688)
                                )
                            )
                        )
                ) {
                    // Loading indicator
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = colorResource(id = R.color.NeonGreen)
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
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Switch(
                                checked = isWalletToggleOn,
                                onCheckedChange = { viewModel.toggleWalletConnection(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFB2FF59),
                                    checkedTrackColor = Color(0xFF004D40),
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color(0xFF004D40).copy(alpha = 0.5f)
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
                                color = Color(0xFFB2FF59),
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Balance amount - Use data from ViewModel
                        Text(
                            text = if (isWalletConnected) balance else "0.0",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
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
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Wallet Owner",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Action Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionCard(
                    title = "Send",
                    backgroundColor = Color(0xFFFF9999), // Light Red
                    icon = R.drawable.ic_send,
                    onClick = { onNavigate("send_money") }
                )

                ActionCard(
                    title = "Deposit",
                    backgroundColor = Color(0xFF99FF99), // Light Green
                    icon = R.drawable.ic_desposit,
                    onClick = { onNavigate("deposit") }
                )

                ActionCard(
                    title = "Exchange",
                    backgroundColor = Color(0xFF99FFFF), // Light Cyan
                    icon = R.drawable.ic_exchange,
                    onClick = { onNavigate("exchange") }
                )

                ActionCard(
                    title = "Request",
                    backgroundColor = Color(0xFFFFFF99), // Light Yellow
                    icon = R.drawable.ic_recieve,
                    onClick = { onNavigate("request") }
                )
            }

            // History Section
            Text(
                text = "History",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Empty space for history items
            Spacer(modifier = Modifier.weight(1f))
        }
    }
} // Fixed: Properly closed HomeScreen composable

@Composable
fun ActionCard(
    title: String,
    backgroundColor: Color,
    icon: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(90.dp)
                .width(78.dp)
                .background(backgroundColor, shape = RoundedCornerShape(1.dp))
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
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
    val previewViewModel: HomeScreenViewModel = viewModel(factory = viewModelFactory)

    HomeScreen(
        viewModel = previewViewModel,
        onNavigate = { /* Preview only - no navigation */ }
    )
}