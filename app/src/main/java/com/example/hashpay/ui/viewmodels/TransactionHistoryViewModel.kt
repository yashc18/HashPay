package com.example.hashpay.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashpay.WalletConnectionManager
import com.example.hashpay.data.AppDatabaseProvider
import com.example.hashpay.data.database.entities.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransactionHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionRepository = AppDatabaseProvider.getTransactionRepository()
    private val walletManager = WalletConnectionManager.getInstance(application)

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    init {
        loadTransactions()
    }

    // In TransactionHistoryViewModel
    private fun loadTransactions() {
        viewModelScope.launch {
            val walletAddress = walletManager.walletAddress.first()
            Log.d("TransactionHistory", "Loading transactions for address: $walletAddress")

            if (walletAddress.isNotBlank()) {
                transactionRepository.getTransactionsByAddress(walletAddress).collectLatest { txList ->
                    Log.d("TransactionHistory", "Received ${txList.size} transactions")
                    _transactions.value = txList
                }
            }
        }
    }

    fun refreshTransactions() {
        loadTransactions()
    }

    // Call this when navigating to the history screen
    fun filterTransactions(status: String? = null) {
        viewModelScope.launch {
            if (status != null) {
                transactionRepository.getTransactionsByStatus(status).collectLatest { txList ->
                    _transactions.value = txList
                }
            } else {
                loadTransactions() // Reset to default view
            }
        }
    }


}