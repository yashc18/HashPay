package com.example.hashpay.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal

class ProfileViewModel : ViewModel() {
    private val _userName = MutableStateFlow("John Doe")
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("john.doe@example.com")
    val userEmail = _userEmail.asStateFlow()

    private val _walletAddress = MutableStateFlow("0x7F45764657eB2552c3A8720aD5666bF83d52aE7c")
    val walletAddress = _walletAddress.asStateFlow()

    private val _walletBalance = MutableStateFlow(BigDecimal("1.234"))
    val walletBalance = _walletBalance.asStateFlow()

    private val _isWalletConnected = MutableStateFlow(true)
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

    fun toggleSetting(key: String) {
        _settings.value = _settings.value.toMutableMap().apply {
            this[key] = !(this[key] as Boolean)
        }
    }

    fun updateWalletConnection(isConnected: Boolean) {
        _isWalletConnected.value = isConnected
    }

    fun updateUserName(name: String) {
        if (name.isNotBlank()) _userName.value = name
    }

    fun updateUserEmail(email: String) {
        if (email.isNotBlank()) _userEmail.value = email
    }
}