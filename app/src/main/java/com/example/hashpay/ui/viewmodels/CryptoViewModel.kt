package com.example.hashpay.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashpay.WalletConnectionManager
import com.example.hashpay.ui.EthereumManager
import io.metamask.androidsdk.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CryptoViewModel(private val ethereumManager: EthereumManager,
                      private val walletManager: WalletConnectionManager
) : ViewModel() {

    private val _transactionResult = MutableStateFlow<TransactionState>(TransactionState.Idle)
    val transactionResult: StateFlow<TransactionState> = _transactionResult

    fun connectWallet() {
        viewModelScope.launch {
            _transactionResult.value = TransactionState.Loading
            when (val result = ethereumManager.connect()) {
                is Result.Success.Item -> {
                    walletManager.connectWallet(result.value, "metamask")
                    _transactionResult.value = TransactionState.Success("Connected: ${result.value}")                }
                is Result.Error -> {
                    _transactionResult.value = TransactionState.Error("Connection error: ${result.error.message}")
                }
                else -> {
                    _transactionResult.value = TransactionState.Error("Unknown error")
                }
            }
        }
    }

    fun sendCrypto(from: String, to: String, amountInWei: String) {
        viewModelScope.launch {
            _transactionResult.value = TransactionState.Loading
            when (val result = ethereumManager.sendTransaction(from, to, amountInWei)) {
                is Result.Success.Item -> {
                    _transactionResult.value = TransactionState.Success("Transaction Hash: ${result.value}")
                }
                is Result.Error -> {
                    _transactionResult.value = TransactionState.Error("Transaction error: ${result.error.message}")
                }
                else -> {
                    _transactionResult.value = TransactionState.Error("Unknown error")
                }
            }
        }
    }
    fun setTransactionResult(state: TransactionState) {
        _transactionResult.value = state
    }
}
sealed class TransactionState {
    abstract fun isNotBlank(): Boolean

    object Idle : TransactionState() {
        override fun isNotBlank(): Boolean = false
    }

    object Loading : TransactionState() {
        override fun isNotBlank(): Boolean = false
    }

    data class Success(val message: String) : TransactionState() {
        override fun isNotBlank(): Boolean = message.isNotBlank()
    }

    data class Error(val message: String) : TransactionState() {
        override fun isNotBlank(): Boolean = message.isNotBlank()
    }
}