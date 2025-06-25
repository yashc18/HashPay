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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger

class HomeScreenViewModel(private val context: Context) : ViewModel() {

    private val ethereumManager = EthereumManager(context)
    private val walletManager = WalletConnectionManager.getInstance(context)

    // Wallet connection state
    private val _isWalletConnected = MutableStateFlow(false)
    val isWalletConnected: StateFlow<Boolean> = _isWalletConnected.asStateFlow()

    private val _walletAddress = MutableStateFlow("")
    val walletAddress: StateFlow<String> = _walletAddress.asStateFlow()

    // Balance state
    private val _balance = MutableStateFlow("0.0")
    val balance: StateFlow<String> = _balance.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error handling
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Toggle state for the wallet connection
    private val _isWalletToggleOn = MutableStateFlow(false)
    val isWalletToggleOn: StateFlow<Boolean> = _isWalletToggleOn.asStateFlow()

    init {
        // Subscribe to wallet connection state changes
        observeWalletConnectionState()
    }

    private fun observeWalletConnectionState() {
        viewModelScope.launch {
            try {
                // Subscribe to connection state from WalletConnectionManager
                walletManager.isConnected.collectLatest { isConnected ->
                    _isWalletConnected.value = isConnected
                    _isWalletToggleOn.value = isConnected

                    // If connection was just disconnected, reset balance
                    if (!isConnected) {
                        _balance.value = "0.0"
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error observing wallet state: ${e.message}")
            }
        }

        viewModelScope.launch {
            try {
                // Subscribe to wallet address from WalletConnectionManager
                walletManager.walletAddress.collectLatest { address ->
                    _walletAddress.value = address

                    // Only fetch balance if connected and address is valid
                    if (address.isNotBlank() && _isWalletConnected.value) {
                        try {
                            fetchWalletBalance()
                        } catch (e: Exception) {
                            Log.e("HomeScreenViewModel", "Error fetching balance: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error observing wallet address: ${e.message}")
            }
        }
    }

    fun toggleWalletConnection(isOn: Boolean) {
        viewModelScope.launch {
            _isWalletToggleOn.value = isOn

            if (isOn) {
                connectWallet()
            } else {
                disconnectWallet()
            }
        }
    }

    private fun connectWallet() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = ethereumManager.connect()
                if (result is Result.Success) {
                    val address = ethereumManager.getWalletAddress()
                    if (!address.isNullOrBlank()) {
                        walletManager.connectWallet(address, "metamask")
                    } else {
                        _errorMessage.value = "Connected but couldn't get wallet address"
                        _isWalletToggleOn.value = false
                    }
                } else if (result is Result.Error) {
                    _errorMessage.value = "Connection failed: ${result.error.message}"
                    _isWalletToggleOn.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Connection error: ${e.message ?: "Unknown error"}"
                _isWalletToggleOn.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun disconnectWallet() {
        viewModelScope.launch {
            walletManager.disconnectWallet()
            _balance.value = "0.0"
        }
    }

    private fun fetchWalletBalance() {
        if (!_isWalletConnected.value || _walletAddress.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Skip auto-connect when fetching balance
                val balanceResult = ethereumManager.getBalance(_walletAddress.value, skipAutoConnect = true)

                if (balanceResult is Result.Success.Item) {
                    // Convert wei to ETH (1 ETH = 10^18 wei)
                    val hexValue = balanceResult.value
                    if (!hexValue.isNullOrEmpty()) {
                        try {
                            val cleanHex = hexValue.removePrefix("0x")
                            val balanceInWei = BigInteger(cleanHex, 16)
                            val balanceInEth = BigDecimal(balanceInWei)
                                .divide(BigDecimal("1000000000000000000"))

                            _balance.value = balanceInEth.setScale(6, BigDecimal.ROUND_DOWN).toPlainString()
                        } catch (e: Exception) {
                            Log.e("HomeScreenViewModel", "Error parsing balance: ${e.message}")
                            _balance.value = "0.0"
                        }
                    }
                } else {
                    // If we couldn't get balance, don't crash - just set to 0
                    _balance.value = "0.0"
                }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Failed to fetch balance: ${e.message}")
                _balance.value = "0.0"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refreshWallet() {
        if (_isWalletConnected.value) {
            fetchWalletBalance()
        }
    }

    fun formatWalletAddress(address: String): String {
        if (address.length < 10) return address
        return "${address.take(6)}...${address.takeLast(4)}"
    }
}