package com.example.hashpay.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashpay.WalletConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val walletManager = WalletConnectionManager.getInstance(application)

    private val _userName = MutableStateFlow("John Doe")
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("john.doe@example.com")
    val userEmail = _userEmail.asStateFlow()

    private val _walletAddress = MutableStateFlow("")
    val walletAddress = _walletAddress.asStateFlow()

    private val _walletBalance = MutableStateFlow(BigDecimal("0.0"))
    val walletBalance = _walletBalance.asStateFlow()

    private val _isWalletConnected = MutableStateFlow(false)
    val isWalletConnected = _isWalletConnected.asStateFlow()

    private val _stats = MutableStateFlow(
        mapOf(
            "sent" to BigDecimal("5.67"),
            "received" to BigDecimal("7.89"),
            "transactions" to BigDecimal("12")
        )
    )
    val stats = _stats.asStateFlow()

    private val _settings = MutableStateFlow(
        mapOf(
            "darkMode" to true,
            "notifications" to true,
            "biometric" to false
        )
    )
    val settings = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            walletManager.isConnected.collectLatest { isConnected ->
                _isWalletConnected.value = isConnected
            }
        }

        viewModelScope.launch {
            walletManager.walletAddress.collectLatest { address ->
                _walletAddress.value = address
            }
        }
    }

    fun toggleSetting(key: String) {
        _settings.value = _settings.value.toMutableMap().apply {
            this[key] = !(this[key] as Boolean)
        }
    }

    fun updateWalletConnection(isConnected: Boolean) {
        viewModelScope.launch {
            if (isConnected) {
                // Don't try to connect here - just wait for the toggle to propagate
                // The HomeScreen will handle the actual connection
            } else {
                walletManager.disconnectWallet()
            }
        }
    }

    fun updateUserName(name: String) {
        if (name.isNotBlank()) _userName.value = name
    }

    fun updateUserEmail(email: String) {
        if (email.isNotBlank()) _userEmail.value = email
    }
}