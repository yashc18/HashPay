package com.example.hashpay.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromAddress: String,
    val toAddress: String,
    val amount: String, // Amount in Wei
    val amountInEth: String, // Formatted amount in ETH
    val timestamp: Long = System.currentTimeMillis(),
    val txHash: String? = null, // Transaction hash from blockchain
    val message: String? = null, // Optional message
    val status: String = "pending", // pending, completed, failed
    val type: String = "send" // send or receive
)