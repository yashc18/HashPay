package com.example.hashpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hashpay.data.database.entities.InvoiceStatus
import com.example.hashpay.ui.states.InvoiceUIState
import com.example.hashpay.ui.theme.HashPayTheme

@Composable
fun InvoiceItem(
    invoice: InvoiceUIState,
    onClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        onClick = { onClick(invoice.id) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.formattedAmount,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                StatusChip(status = invoice.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "To: ${formatAddress(invoice.receiverAddress)}",
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (invoice.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = invoice.description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Created: ${invoice.formattedDate}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                if (invoice.dueDate != null) {
                    Text(
                        text = "Due: ${invoice.formattedDueDate}",
                        color = if (invoice.isOverdue) Color.Red else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: InvoiceStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        InvoiceStatus.PAID -> Triple(Color(0xFF0A3300), Color(0xFFB2FF59), "Paid")
        InvoiceStatus.PENDING -> Triple(Color(0xFF333300), Color(0xFFFFFF99), "Pending")
        InvoiceStatus.CANCELLED -> Triple(Color(0xFF330033), Color(0xFFFF99FF), "Cancelled")
        InvoiceStatus.OVERDUE -> Triple(Color(0xFF330000), Color(0xFFFF9999), "Overdue")
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(4.dp)
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

private fun formatAddress(address: String): String {
    if (address.length < 10) return address
    return "${address.take(6)}...${address.takeLast(4)}"
}

@Preview
@Composable
fun InvoiceItemPreview() {
    HashPayTheme {
        Surface(color = Color.Black, modifier = Modifier.padding(16.dp)) {
            InvoiceItem(
                invoice = InvoiceUIState(
                    id = 1,
                    receiverAddress = "0x1234567890abcdef1234567890abcdef12345678",
                    amount = 0.05,
                    description = "Payment for design services",
                    status = InvoiceStatus.PENDING,
                    formattedAmount = "0.0500 ETH",
                    formattedDate = "May 15, 2023",
                    formattedDueDate = "May 30, 2023"
                ),
                onClick = {}
            )
        }
    }
}