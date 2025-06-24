package com.example.hashpay.data.database.daos

import androidx.room.*
import com.example.hashpay.data.database.entities.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact): Long

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts ORDER BY lastTransactionDate DESC LIMIT :limit")
    fun getRecentContacts(limit: Int): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE walletAddress = :address LIMIT 1")
    suspend fun getContactByAddress(address: String): Contact?

    @Query("UPDATE contacts SET lastTransactionDate = :timestamp WHERE walletAddress = :address")
    suspend fun updateLastTransactionDate(address: String, timestamp: Long)
}