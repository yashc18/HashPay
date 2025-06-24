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
        // Check wallet connection status on initialization
        checkWalletConnection()
    }

    private fun checkWalletConnection() {
        viewModelScope.launch {
            try {
                val savedAddress = walletManager.walletAddress.first()
                val isConnected = walletManager.isConnected.first()

                if (isConnected && savedAddress.isNotBlank()) {
                    _walletAddress.value = savedAddress
                    _isWalletConnected.value = true
                    _isWalletToggleOn.value = true
                    fetchWalletBalance()
                } else {
                    try {
                        withTimeout(3000) {
                            val ethAddress = ethereumManager.getWalletAddress()
                            if (!ethAddress.isNullOrBlank()) {
                                _walletAddress.value = ethAddress
                                _isWalletConnected.value = true
                                _isWalletToggleOn.value = true
                                walletManager.connectWallet(ethAddress, "metamask")
                                fetchWalletBalance()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("HomeScreenViewModel", "Timeout checking wallet: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error checking wallet: ${e.message}")
            }
        }
    }

    fun toggleWalletConnection(isOn: Boolean) {
        if (isOn) {
            connectWallet()
        } else {
            disconnectWallet()
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
                        _walletAddress.value = address
                        _isWalletConnected.value = true
                        _isWalletToggleOn.value = true
                        walletManager.connectWallet(address, "metamask")
                        fetchWalletBalance()
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
            _isWalletConnected.value = false
            _walletAddress.value = ""
            _balance.value = "0.0"
            _isWalletToggleOn.value = false
        }
    }

    private fun fetchWalletBalance() {
        if (!_isWalletConnected.value || _walletAddress.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val balanceResult = ethereumManager.getBalance(_walletAddress.value)
                if (balanceResult is Result.Success.Item) {
                    // Convert wei to ETH (1 ETH = 10^18 wei)
                    val balanceInWei = BigInteger(balanceResult.value.removePrefix("0x"), 16)
                    val balanceInEth = BigDecimal(balanceInWei)
                        .divide(BigDecimal("1000000000000000000"))

                    _balance.value = balanceInEth.setScale(6, BigDecimal.ROUND_DOWN).toPlainString()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch balance: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refreshWallet() {
        checkWalletConnection()
    }

    fun formatWalletAddress(address: String): String {
        if (address.length < 10) return address
        return "${address.take(6)}...${address.takeLast(4)}"
    }
}