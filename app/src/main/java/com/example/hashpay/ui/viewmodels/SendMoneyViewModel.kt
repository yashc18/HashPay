package com.example.hashpay.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashpay.WalletConnectionManager
import com.example.hashpay.data.AppDatabaseProvider
import com.example.hashpay.data.database.entities.Contact
import com.example.hashpay.data.database.entities.Transaction
import com.example.hashpay.ui.EthereumManager
import io.metamask.androidsdk.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.math.BigDecimal
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

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
    //RoomDB
    private val transactionRepository = AppDatabaseProvider.getTransactionRepository()
    private val contactRepository = AppDatabaseProvider.getContactRepository()

    private val _recentContacts = MutableStateFlow<List<Contact>>(emptyList())
    val recentContacts: StateFlow<List<Contact>> = _recentContacts

    private val _recentTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val recentTransactions: StateFlow<List<Transaction>> = _recentTransactions


    init {
        // Checks wallet connection status on initialization
        checkWalletConnection()
        // Loads recent contacts and transactions
        loadRecentContacts()
        loadRecentTransactions()
    }

    private fun loadRecentContacts() {
        viewModelScope.launch {
            contactRepository.getRecentContacts(5).collect { contacts ->
                _recentContacts.value = contacts
            }
        }
    }

    private fun loadRecentTransactions() {
        viewModelScope.launch {
            transactionRepository.allTransactions.collect { transactions ->
                _recentTransactions.value = transactions
            }
        }
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
        //Validates wallet connection
        if (!_isWalletConnected.value || _walletAddress.value.isBlank()) {
            _showConnectionDialog.value = true
            return
        }

        //Validates input fields
        if (_recipientAddress.value.isBlank() || _amountEth.value.isBlank()) {
            _transactionResult.value = TransactionState.Error("Please enter recipient address and amount")
            return
        }

        viewModelScope.launch {
            _isTransactionInProgress.value = true
            _transactionResult.value = TransactionState.Loading

            try {
                //Double-check wallet connection
                val currentAddress = ethereumManager.getWalletAddress()
                if (currentAddress.isNullOrBlank()) {
                    _isWalletConnected.value = false
                    _transactionResult.value = TransactionState.Error("Wallet is not connected")
                    return@launch
                }

                //Calculate amount in Wei hex format
                val amountWei = BigDecimal(_amountEth.value)
                    .multiply(BigDecimal("1000000000000000000"))
                    .toBigInteger().toString(16)
                val amountInWeiHex = "0x$amountWei"

                //Save transaction with pending status
                val txId = withContext(Dispatchers.IO) {
                    saveTransaction(
                        _recipientAddress.value,
                        amountInWeiHex,
                        _amountEth.value,
                        null
                    )
                }
                //Send the transaction and handle the result
                when (val result = ethereumManager.sendTransaction(currentAddress, _recipientAddress.value, amountInWeiHex)) {
                    is Result.Success.Item -> {
                        val txHash = result.value
                        // Update transaction status to completed with hash
                        transactionRepository.updateTransactionStatus(txId, "completed", txHash)
                        _transactionResult.value = TransactionState.Success("Transaction Hash: $txHash")
                    }
                    is Result.Error -> {
                        // Update transaction status to failed
                        transactionRepository.updateTransactionStatus(txId, "failed", null)
                        _transactionResult.value = TransactionState.Error("Transaction error: ${result.error.message}")
                    }
                    else -> {
                        // Handle unknown result
                        transactionRepository.updateTransactionStatus(txId, "failed", null)
                        _transactionResult.value = TransactionState.Error("Unknown error")
                    }
                }
            } catch (e: Exception) {
                _transactionResult.value = TransactionState.Error(e.message ?: "Transaction failed")
            } finally {
                _isTransactionInProgress.value = false
            }
        }
    }

    // Actual crypto sending function (previously in CryptoViewModel)
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

    //saveTransaction Function, It returns the transaction ID
    suspend fun saveTransaction(toAddress: String, amount: String, amountInEth: String, message: String? = null): Long {
        // Create transaction object
        val transaction = Transaction(
            fromAddress = _walletAddress.value,
            toAddress = toAddress,
            amount = amount, // Wei
            amountInEth = amountInEth, // ETH (formatted)
            message = message,
            status = "pending",
            type = "send",
            timestamp = System.currentTimeMillis(),
            txHash = null
        )

        // Insert transaction and get ID (directly, not in a new coroutine)
        val transactionId = transactionRepository.insert(transaction)

        // Handle contact management
        val existingContact = contactRepository.getContactByAddress(toAddress)
        if (existingContact == null) {
            val contactName = "Contact #${transactionId}"
            contactRepository.insert(Contact(
                name = contactName,
                walletAddress = toAddress,
                lastTransactionDate = System.currentTimeMillis()
            ))
        } else {
            contactRepository.updateLastTransactionDate(toAddress, System.currentTimeMillis())
        }

        return transactionId
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