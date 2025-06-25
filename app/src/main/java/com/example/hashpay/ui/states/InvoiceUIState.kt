package com.example.hashpay.ui.states

import com.example.hashpay.data.database.entities.InvoiceStatus
import java.util.Date

data class InvoiceUIState(
    val id: Long = 0,
    val receiverAddress: String = "",
    val senderAddress: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val status: InvoiceStatus = InvoiceStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null,
    val dueDate: Long? = null,
    val transactionHash: String? = null,
    val formattedAmount: String = "0.0000 ETH",
    val formattedDate: String = "",
    val formattedDueDate: String = "No due date",
    val isPaid: Boolean = false,
    val isOverdue: Boolean = false
) {
    companion object {
        fun empty() = InvoiceUIState()
    }
}