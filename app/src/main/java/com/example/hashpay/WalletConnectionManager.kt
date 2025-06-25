package com.example.hashpay

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hashpay.ui.EthereumManager
import io.metamask.androidsdk.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val Context.walletDataStore: DataStore<Preferences> by preferencesDataStore("wallet_preferences")

class WalletConnectionManager private constructor(private val context: Context) {

    companion object {
        private val WALLET_CONNECTED = booleanPreferencesKey("wallet_connected")
        private val WALLET_ADDRESS = stringPreferencesKey("wallet_address")
        private val WALLET_TYPE = stringPreferencesKey("wallet_type") // "metamask" or "smartcontract"

        @Volatile
        private var INSTANCE: WalletConnectionManager? = null

        fun getInstance(context: Context): WalletConnectionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WalletConnectionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Connection state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    // Wallet address
    private val _walletAddress = MutableStateFlow("")
    val walletAddress: StateFlow<String> = _walletAddress

    // Wallet type (metamask or smartcontract)
    private val _walletType = MutableStateFlow("smartcontract")
    val walletType: StateFlow<String> = _walletType

    // Show connection dialog flag
    private val _showConnectionDialog = MutableStateFlow(false)
    val showConnectionDialog: StateFlow<Boolean> = _showConnectionDialog

    init {
        CoroutineScope(Dispatchers.IO).launch {
            context.walletDataStore.data.collect { preferences ->
                _isConnected.value = preferences[WALLET_CONNECTED] ?: false
                _walletAddress.value = preferences[WALLET_ADDRESS] ?: ""
                _walletType.value = preferences[WALLET_TYPE] ?: "smartcontract"
            }
        }
    }

    suspend fun connectWallet(address: String, type: String = "metamask") {
        context.walletDataStore.edit { preferences ->
            preferences[WALLET_CONNECTED] = true
            preferences[WALLET_ADDRESS] = address
            preferences[WALLET_TYPE] = type
        }
        _isConnected.value = true
        _walletAddress.value = address
        _walletType.value = type
        _showConnectionDialog.value = false
        Log.d("WalletManager", "Wallet connected: $address ($type)")
    }

    suspend fun disconnectWallet() {
        context.walletDataStore.edit { preferences ->
            preferences[WALLET_CONNECTED] = false
        }
        _isConnected.value = false
        Log.d("WalletManager", "Wallet disconnected")
    }

    fun requestConnection() {
        _showConnectionDialog.value = true
    }

    fun dismissConnectionDialog() {
        _showConnectionDialog.value = false
    }

    // For smart contract wallets that are always "connected"
    suspend fun useSmartContractWallet(address: String) {
        connectWallet(address, "smartcontract")
    }

    // Modified to explicitly require user action
    suspend fun ensureWalletConnected(ethereumManager: EthereumManager, requireUserAction: Boolean = true): Boolean {
        // If we require user action, don't auto-connect
        if (requireUserAction) {
            return _isConnected.value
        }

        val isConnected = _isConnected.value
        val address = _walletAddress.value

        Log.d("WalletManager", "Ensuring wallet connected: current state=$isConnected, address=$address")

        if (isConnected && address.isNotBlank()) {
            // Verify connection with Ethereum provider
            try {
                val ethAddress = ethereumManager.getWalletAddress()
                if (!ethAddress.isNullOrBlank()) {
                    Log.d("WalletManager", "Connection verified with provider: $ethAddress")
                    return true // Connection verified
                }

                // Connection lost but we have saved credentials, try reconnecting
                Log.d("WalletManager", "Saved connection not verified, attempting silent reconnect")
                val result = ethereumManager.connect()
                if (result is Result.Success) {
                    val newAddress = ethereumManager.getWalletAddress()
                    if (!newAddress.isNullOrBlank()) {
                        connectWallet(newAddress, "metamask")
                        Log.d("WalletManager", "Silent reconnection successful: $newAddress")
                        return true
                    }
                }
                Log.d("WalletManager", "Silent reconnection failed")
            } catch (e: Exception) {
                Log.e("WalletManager", "Wallet verification failed: ${e.message}", e)
            }
        } else {
            Log.d("WalletManager", "No saved wallet connection")
        }

        return false
    }
}