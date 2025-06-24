package com.example.hashpay.ui

import android.content.Context
import android.util.Log
import io.metamask.androidsdk.DappMetadata
import io.metamask.androidsdk.Ethereum
import io.metamask.androidsdk.EthereumFlow
import io.metamask.androidsdk.EthereumMethod
import io.metamask.androidsdk.EthereumRequest
import io.metamask.androidsdk.RequestError
import io.metamask.androidsdk.Result
import io.metamask.androidsdk.SDKOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.math.BigInteger
import java.security.MessageDigest

class EthereumManager(context: Context) {
    private val TAG = "EthereumManager"

    private val dappMetadata = DappMetadata("HashPay Dapp", "https://www.hashpay.io")
    private val infuraAPIKey = "fb5222d70fff4f56bf10adce5422c6ec" // Optional

    // First create the Ethereum instance
    private val ethereumInstance = Ethereum(
        context = context,
        dappMetadata = dappMetadata,
        sdkOptions = SDKOptions(infuraAPIKey)
    )

    // Now create EthereumFlow with the Ethereum instance
    private val ethereum = EthereumFlow(ethereumInstance)

    // Connect to MetaMask
    suspend fun connect(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting connection to MetaMask...")
        try {
            val result = ethereum.connect()
            Log.d(TAG, "Connection result: $result")

            // The working example shows that selectedAddress is directly accessible after connect
            Log.d(TAG, "Selected address after connect: ${ethereum.selectedAddress}")

            if (ethereum.selectedAddress.isNotEmpty()) {
                Log.d(TAG, "Successfully connected with address: ${ethereum.selectedAddress}")
                Result.Success.Item("Connected successfully: ${ethereum.selectedAddress}")
            } else {
                Log.d(TAG, "Connected but no address available")
                result // Pass through the original result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection error: ${e.message}")
            Log.e(TAG, "Stack trace:", e)
            Result.Error(RequestError(404, e.message ?: "Unknown error"))
        }
    }

    // Get connected wallet address
    suspend fun getWalletAddress(): String? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting wallet address, current value: ${ethereum.selectedAddress}")

        if (ethereum.selectedAddress.isNotEmpty()) {
            return@withContext ethereum.selectedAddress
        }

        // If not connected or no address available
        null
    }

    // Get account balance
    suspend fun getBalance(address: String): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting balance for address: $address")

        // First check if we're properly connected
        if (ethereum.selectedAddress.isEmpty()) {
            Log.d(TAG, "Not connected to MetaMask, attempting to connect first")
            val connectResult = connect()
            if (connectResult !is Result.Success) {
                Log.e(TAG, "Failed to connect to MetaMask")
                return@withContext Result.Error(RequestError(401, "Not connected to MetaMask"))
            }
        }

        try {
            // Use a timeout to prevent infinite recursion
            withTimeout(10000) {
                val balanceResult = ethereum.sendRequest(
                    EthereumRequest(
                        method = EthereumMethod.ETH_GET_BALANCE.value,
                        params = listOf(address, "latest")
                    )
                )

                Log.d(TAG, "Balance result: $balanceResult")

                when (balanceResult) {
                    is Result.Success.Item -> {
                        val hexValue = balanceResult.value
                        val cleanHexString = if (hexValue.startsWith("0x")) {
                            hexValue.substring(2)
                        } else {
                            hexValue
                        }

                        val balanceInWei = BigInteger(cleanHexString, 16)
                        Log.d(TAG, "Balance in wei: $balanceInWei")

                        Result.Success.Item(balanceInWei.toString())
                    }
                    else -> balanceResult
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting balance: ${e.message}")
            Result.Error(RequestError(404, e.message ?: "Unknown error"))
        }
    }

    // Add this helper method to ensure connection
    suspend fun ensureConnected(): Boolean = withContext(Dispatchers.IO) {
        if (ethereum.selectedAddress.isNotEmpty()) {
            return@withContext true
        }

        val result = connect()
        return@withContext result is Result.Success
    }

    // Send ETH (basic transaction)
    suspend fun sendTransaction(fromAddress: String, toAddress: String, amountInWei: String): Result =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Sending transaction. From: $fromAddress, To: $toAddress, Amount: $amountInWei")

            if (ethereum.selectedAddress.isEmpty()) {
                Log.e(TAG, "Cannot send transaction: Not connected to wallet")
                return@withContext Result.Error(RequestError(404, "Not connected to wallet"))
            }

            try {
                val params = mapOf(
                    "from" to fromAddress,
                    "to" to toAddress,
                    "value" to amountInWei
                )

                val request = EthereumRequest(
                    method = EthereumMethod.ETH_SEND_TRANSACTION.value,
                    params = listOf(params)
                )

                Log.d(TAG, "Sending transaction request...")
                val result = ethereum.sendRequest(request)
                Log.d(TAG, "Transaction result: $result")

                result
            } catch (e: Exception) {
                Log.e(TAG, "Error sending transaction: ${e.message}")
                Result.Error(RequestError(404, e.message ?: "Unknown error"))
            }
        }

    // Send to smart contract: pay(address,string)
    suspend fun sendPayTransaction(
        fromAddress: String,
        contractAddress: String,
        recipient: String,
        message: String,
        amountInWei: String
    ): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Sending contract transaction. From: $fromAddress, Contract: $contractAddress")

        if (ethereum.selectedAddress.isEmpty()) {
            Log.e(TAG, "Cannot send contract transaction: Not connected to wallet")
            return@withContext Result.Error(RequestError(404, "Not connected to wallet"))
        }

        try {
            val data = encodePayFunction(recipient, message)

            val txParams = mapOf(
                "from" to fromAddress,
                "to" to contractAddress,
                "value" to amountInWei,
                "data" to data
            )

            val request = EthereumRequest(
                method = EthereumMethod.ETH_SEND_TRANSACTION.value,
                params = listOf(txParams)
            )

            Log.d(TAG, "Sending contract transaction request...")
            val result = ethereum.sendRequest(request)
            Log.d(TAG, "Contract transaction result: $result")

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error sending contract transaction: ${e.message}")
            Result.Error(RequestError(404, e.message ?: "Unknown error"))
        }
    }

    // Disconnect from MetaMask
    suspend fun disconnect(clearSession: Boolean = true): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Disconnecting from MetaMask with clearSession=$clearSession")
        try {
            ethereum.disconnect(clearSession)
            Result.Success.Item("Disconnected successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during disconnect: ${e.message}")
            Result.Error(RequestError(404, e.message ?: "Unknown error"))
        }
    }

    // Check if connected to wallet
    fun isConnectedToWallet(): Boolean {
        val isConnected = ethereum.selectedAddress.isNotEmpty()
        Log.d(TAG, "Checking if connected to wallet: $isConnected (address: ${ethereum.selectedAddress})")
        return isConnected
    }

    // Encode the function call for: pay(address,string)
    private fun encodePayFunction(recipient: String, message: String): String {
        val methodSignature = "pay(address,string)"
        val methodId = keccak256(methodSignature).substring(0, 8)

        val recipientCleaned = recipient.removePrefix("0x").padStart(64, '0')

        val messageBytes = message.toByteArray(Charsets.UTF_8)
        val messageHex = messageBytes.joinToString("") { "%02x".format(it) }
        val messageLengthHex = messageBytes.size.toString(16).padStart(64, '0')
        val messagePadded = messageHex.padEnd(((messageHex.length + 63) / 64) * 64, '0')

        val dynamicOffset = "0000000000000000000000000000000000000000000000000000000000000040"

        return "0x$methodId$recipientCleaned$dynamicOffset$messageLengthHex$messagePadded"
    }

    // keccak256 helper for function selector
    private fun keccak256(input: String): String {
        val digest = MessageDigest.getInstance("KECCAK-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}