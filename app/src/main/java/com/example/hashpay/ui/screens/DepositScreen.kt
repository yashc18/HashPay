package com.example.hashpay.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.hashpay.R
import com.example.hashpay.ui.theme.SpaceGrotesk
import com.example.hashpay.ui.viewmodels.DepositViewModel
import com.example.hashpay.ui.viewmodels.DepositViewModelFactory
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    viewModel: DepositViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = DepositViewModelFactory(LocalContext.current)
    ),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val walletAddress by viewModel.walletAddress.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Deposit Funds",
                        color = Color.White,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F0F)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Scan QR Code to Deposit",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceGrotesk
            )

            Spacer(modifier = Modifier.height(24.dp))

            // QR Code display with improved styling
            qrCodeBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .background(Color(0xFF1F1F1F), RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Deposit QR Code",
                        modifier = Modifier
                            .size(250.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Or copy your wallet address:",
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = SpaceGrotesk
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Wallet address display with copy button
            Surface(
                color = Color(0xFF1F1F1F),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = walletAddress,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        fontFamily = SpaceGrotesk
                    )

                    IconButton(
                        onClick = {
                            viewModel.copyAddressToClipboard()
                            Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF252525), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_copy),
                            contentDescription = "Copy Address",
                            tint = Color(0xFF30E0A1)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF261D1D)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_error),
                        contentDescription = null,
                        tint = Color(0xFFFF9999),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Send only ETH to this address.\nSending other coins may result in permanent loss.",
                        color = Color(0xFFFF9999),
                        fontSize = 14.sp,
                        fontFamily = SpaceGrotesk
                    )
                }
            }
        }
    }
}