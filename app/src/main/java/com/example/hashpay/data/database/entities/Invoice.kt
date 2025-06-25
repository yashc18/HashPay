package com.example.hashpay.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiverAddress: String,
    val senderAddress: String = "", // Optional for new invoices
    val amount: Double,
    val description: String,
    val status: InvoiceStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null,
    val dueDate: Long? = null,
    val transactionHash: String? = null
)

enum class InvoiceStatus {
    PENDING,
    PAID,
    CANCELLED,
    OVERDUE
}