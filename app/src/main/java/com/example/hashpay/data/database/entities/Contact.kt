package com.example.hashpay.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val walletAddress: String,
    val isFavorite: Boolean = false,
    val lastTransactionDate: Long? = null
)