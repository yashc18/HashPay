package com.example.hashpay.data.repositories

import android.util.Log
import com.example.hashpay.data.database.daos.InvoiceDao
import com.example.hashpay.data.database.entities.Invoice
import com.example.hashpay.data.database.entities.InvoiceStatus
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(private val invoiceDao: InvoiceDao) {

    fun getAllInvoices(): Flow<List<Invoice>> = invoiceDao.getAllInvoices()

    fun getInvoicesByStatus(status: InvoiceStatus): Flow<List<Invoice>> =
        invoiceDao.getInvoicesByStatus(status)

    fun getInvoicesByAddress(address: String): Flow<List<Invoice>> =
        invoiceDao.getInvoicesByAddress(address)

    suspend fun getInvoiceById(invoiceId: Long): Invoice? =
        invoiceDao.getInvoiceById(invoiceId)

    suspend fun createInvoice(invoice: Invoice): Long {
        Log.d("InvoiceDebug", "Repository creating invoice in database")
        try {
            val id = invoiceDao.insertInvoice(invoice)
            Log.d("InvoiceDebug", "Repository successfully created invoice with ID: $id")
            return id
        } catch (e: Exception) {
            Log.e("InvoiceDebug", "Repository failed to create invoice", e)
            throw e
        }
    }

    suspend fun updateInvoice(invoice: Invoice) =
        invoiceDao.updateInvoice(invoice)

    suspend fun deleteInvoice(invoice: Invoice) =
        invoiceDao.deleteInvoice(invoice)

    suspend fun markInvoiceAsPaid(invoiceId: Long, transactionHash: String) =
        invoiceDao.markInvoiceAsPaid(invoiceId, txHash = transactionHash)

    suspend fun updateInvoiceStatus(invoice: Invoice, newStatus: InvoiceStatus) {
        invoiceDao.updateInvoice(invoice.copy(status = newStatus))
    }
}