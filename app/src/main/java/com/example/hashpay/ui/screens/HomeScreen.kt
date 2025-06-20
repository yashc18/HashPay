package com.example.hashpay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hashpay.R
import com.example.hashpay.ui.theme.HashPayTheme

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
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
                color = Color.White,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet Section
            Text(
                text = "Your Wallet",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Wallet Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = "ETH", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        text = "$1,000,000",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Wallet Owner",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Yash Chaudhary",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
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
                    icon = R.drawable.ic_send, // Use your own icon
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
}

@Composable
fun ActionCard(
    title: String,
    backgroundColor: Color,
    icon: Int,  // Pass icon as a parameter
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() } // Making it clickable
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
                // White Circle with Icon
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

                // Title inside the colored box
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
        HomeScreen(onNavigate = {})
    }
}
