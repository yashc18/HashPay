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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _networkType = MutableStateFlow("sepolia")
    val networkType: StateFlow<String> = _networkType

    private var allTransactions: List<Transaction> = emptyList()

    init {
        loadTransactions()
        loadNetworkType()
    }

    private fun loadNetworkType() {
        viewModelScope.launch {
            _networkType.value = "sepolia"
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            val walletAddress = walletManager.walletAddress.first()
            Log.d("TransactionHistory", "Loading transactions for address: $walletAddress")

            if (walletAddress.isNotBlank()) {
                transactionRepository.getTransactionsByAddress(walletAddress).collectLatest { txList ->
                    Log.d("TransactionHistory", "Received ${txList.size} transactions")
                    allTransactions = txList
                    _transactions.value = txList
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun refreshTransactions() {
        loadTransactions()
    }

    fun filterTransactions(status: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            if (status != null) {
                // Filter in memory rather than calling a non-existent repository method
                _transactions.value = allTransactions.filter { it.status == status }
                Log.d("TransactionHistory", "Filtered to ${_transactions.value.size} transactions with status: $status")
            } else {
                // Reset to show all transactions
                _transactions.value = allTransactions
                Log.d("TransactionHistory", "Reset to all ${allTransactions.size} transactions")
            }

            _isLoading.value = false
        }
    }

    fun getEtherscanUrl(txHash: String): String {
        val baseUrl = when (networkType.value) {
            "mainnet" -> "https://etherscan.io/tx/"
            "sepolia" -> "https://sepolia.etherscan.io/tx/"
            "goerli" -> "https://goerli.etherscan.io/tx/"
            else -> "https://sepolia.etherscan.io/tx/"
        }
        return baseUrl + txHash
    }
}