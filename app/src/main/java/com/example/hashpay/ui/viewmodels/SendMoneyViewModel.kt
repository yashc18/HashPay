package com.example.hashpay.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashpay.WalletConnectionManager
import com.example.hashpay.ui.EthereumManager
import io.metamask.androidsdk.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.math.BigDecimal

class SendMoneyViewModel(private val context: Context) : ViewModel() {

    private val ethereumManager = EthereumManager(context)
    private val walletManager = WalletConnectionManager.getInstance(context)

    // UI state
    private val _isWalletConnected = MutableStateFlow(false)
    val isWalletConnected: StateFlow<Boolean> = _isWalletConnected.asStateFlow()

    private val _walletAddress = MutableStateFlow("")
    val walletAddress: StateFlow<String> = _walletAddress.asStateFlow()

    private val _showConnectionDialog = MutableStateFlow(false)
    val showConnectionDialog: StateFlow<Boolean> = _showConnectionDialog.asStateFlow()

    private val _recipientAddress = MutableStateFlow("")
    val recipientAddress: StateFlow<String> = _recipientAddress.asStateFlow()

    private val _amountEth = MutableStateFlow("")
    val amountEth: StateFlow<String> = _amountEth.asStateFlow()

    private val _transactionResult = MutableStateFlow<TransactionState>(TransactionState.Idle)
    val transactionResult: StateFlow<TransactionState> = _transactionResult.asStateFlow()

    private val _isTransactionInProgress = MutableStateFlow(false)
    val isTransactionInProgress: StateFlow<Boolean> = _isTransactionInProgress.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    init {
        // Check wallet connection status on initialization
        checkWalletConnection()
    }

    private fun checkWalletConnection() {
        viewModelScope.launch {
            try {
                // Try to get saved wallet info first
                val savedAddress = walletManager.walletAddress.first()
                val isConnected = walletManager.isConnected.first()

                if (isConnected && savedAddress.isNotBlank()) {
                    _walletAddress.value = savedAddress
                    _isWalletConnected.value = true
                    Log.d("SendMoneyViewModel", "Wallet found in storage: $savedAddress")
                } else {
                    // Try direct ethereum manager call with timeout
                    try {
                        withTimeout(3000) {
                            val ethAddress = ethereumManager.getWalletAddress()
                            if (!ethAddress.isNullOrBlank()) {
                                _walletAddress.value = ethAddress
                                _isWalletConnected.value = true
                                walletManager.connectWallet(ethAddress, "metamask")
                                Log.d("SendMoneyViewModel", "Wallet found via ethereum: $ethAddress")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SendMoneyViewModel", "Timeout checking wallet: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SendMoneyViewModel", "Error checking wallet: ${e.message}")
            }
        }
    }

    fun connectWallet() {
        if (_isConnecting.value) return

        viewModelScope.launch {
            _isConnecting.value = true
            _transactionResult.value = TransactionState.Loading

            try {
                Log.d("SendMoneyViewModel", "Starting wallet connection...")
                val result = ethereumManager.connect()

                if (result is io.metamask.androidsdk.Result.Success) {
                    Log.d("SendMoneyViewModel", "Connection successful, getting address")
                    // Try to get the wallet address
                    val address = ethereumManager.getWalletAddress()

                    if (!address.isNullOrBlank()) {
                        Log.d("SendMoneyViewModel", "Got wallet address: $address")
                        _walletAddress.value = address
                        _isWalletConnected.value = true
                        walletManager.connectWallet(address, "metamask")
                        _transactionResult.value = TransactionState.Success("Connected: $address")
                    } else {
                        Log.e("SendMoneyViewModel", "Failed to get wallet address after connection")
                        _transactionResult.value = TransactionState.Error("Connected but couldn't get wallet address")
                    }
                } else if (result is io.metamask.androidsdk.Result.Error) {
                    Log.e("SendMoneyViewModel", "Connection error: ${result.error.message}")
                    _transactionResult.value = TransactionState.Error("Connection failed: ${result.error.message}")
                }
            } catch (e: Exception) {
                Log.e("SendMoneyViewModel", "Exception during wallet connection: ${e.message}", e)
                _transactionResult.value = TransactionState.Error("Connection error: ${e.message ?: "Unknown error"}")
            } finally {
                _isConnecting.value = false
                _showConnectionDialog.value = false
            }
        }
    }

    fun sendTransaction() {
        if (!_isWalletConnected.value || _walletAddress.value.isBlank()) {
            _showConnectionDialog.value = true
            return
        }

        if (_recipientAddress.value.isBlank() || _amountEth.value.isBlank()) {
            _transactionResult.value = TransactionState.Error("Please enter recipient address and amount")
            return
        }

        viewModelScope.launch {
            _isTransactionInProgress.value = true
            _transactionResult.value = TransactionState.Loading

            try {
                // Double-check wallet is still connected
                val currentAddress = ethereumManager.getWalletAddress()
                if (currentAddress.isNullOrBlank()) {
                    _isWalletConnected.value = false
                    _transactionResult.value = TransactionState.Error("Wallet is not connected")
                    return@launch
                }

                val amountWei = BigDecimal(_amountEth.value)
                    .multiply(BigDecimal("1000000000000000000"))
                    .toBigInteger().toString(16)

                // Use current address from wallet
                sendCrypto(
                    from = currentAddress,
                    to = _recipientAddress.value,
                    amountInWei = "0x$amountWei"
                )
            } catch (e: Exception) {
                _transactionResult.value = TransactionState.Error(e.message ?: "Transaction failed")
            } finally {
                _isTransactionInProgress.value = false
            }
        }
    }

    // Actual crypto sending function
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

    // State updaters
    fun setShowConnectionDialog(show: Boolean) {
        _showConnectionDialog.value = show
    }

    fun setRecipientAddress(address: String) {
        _recipientAddress.value = address
    }

    fun setAmountEth(amount: String) {
        _amountEth.value = amount
    }

    fun setTransactionResult(result: TransactionState) {
        _transactionResult.value = result
    }
}
