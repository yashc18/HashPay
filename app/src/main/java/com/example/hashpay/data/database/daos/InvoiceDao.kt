package com.example.hashpay.data.database.daos

import androidx.room.*
import com.example.hashpay.data.database.entities.Invoice
import com.example.hashpay.data.database.entities.InvoiceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY createdAt DESC")
    fun getInvoicesByStatus(status: InvoiceStatus): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE receiverAddress = :address OR senderAddress = :address ORDER BY createdAt DESC")
    fun getInvoicesByAddress(address: String): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    suspend fun getInvoiceById(invoiceId: Long): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("UPDATE invoices SET status = :status, paidAt = :paidAt, transactionHash = :txHash WHERE id = :invoiceId")
    suspend fun markInvoiceAsPaid(invoiceId: Long, status: InvoiceStatus = InvoiceStatus.PAID,
                                  paidAt: Long = System.currentTimeMillis(), txHash: String)
}