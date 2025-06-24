package com.example.hashpay.data.repositories

import com.example.hashpay.data.database.daos.TransactionDao
import com.example.hashpay.data.database.entities.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByStatus(status: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByStatus(status)
    }

    fun getTransactionsByAddress(address: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByAddress(address)
    }

    suspend fun insert(transaction: Transaction): Long {
        return transactionDao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun updateTransactionStatus(id: Long, status: String, txHash: String?) {
        transactionDao.updateTransactionStatus(id, status, txHash)
    }

    suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)
    }
}