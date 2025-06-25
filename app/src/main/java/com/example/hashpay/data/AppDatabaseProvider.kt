package com.example.hashpay.data

import android.content.Context
import com.example.hashpay.data.database.AppDatabase
import com.example.hashpay.data.repositories.ContactRepository
import com.example.hashpay.data.repositories.InvoiceRepository
import com.example.hashpay.data.repositories.TransactionRepository

object AppDatabaseProvider {

    private var database: AppDatabase? = null
    private var transactionRepository: TransactionRepository? = null
    private var contactRepository: ContactRepository? = null
    private var invoiceRepository: InvoiceRepository? = null

    fun initializeDatabase(context: Context) {
        if (database == null) {
            database = AppDatabase.getDatabase(context)
            transactionRepository = TransactionRepository(database!!.transactionDao())
            contactRepository = ContactRepository(database!!.contactDao())
            invoiceRepository = InvoiceRepository(database!!.invoiceDao())
        }
    }

    fun getTransactionRepository(): TransactionRepository {
        return transactionRepository ?: throw IllegalStateException("Database not initialized")
    }

    fun getContactRepository(): ContactRepository {
        return contactRepository ?: throw IllegalStateException("Database not initialized")
    }

    fun getInvoiceRepository(): InvoiceRepository {
        return invoiceRepository ?: throw IllegalStateException("Database not initialized")
    }
}