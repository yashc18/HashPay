package com.example.hashpay.assets

import android.content.Context
import com.example.hashpay.ui.utils.Constants
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.TypeReference
import org.web3j.abi.FunctionReturnDecoder

class SmartContractHelper(private val context: Context) {
    private val ganacheUrl = Constants.GANACHE_URL
    private  val privateKey = Constants.PRIVATE_KEY
    private val contractAddress = Constants.CONTRACT_ADDRESS

    private val web3 = Web3j.build(HttpService(ganacheUrl))
    private val credentials = Credentials.create(privateKey)

    fun getBalance(callback: (String) -> Unit) {
        Thread {
            try {
                val function = Function(
                    "getBalance",
                    listOf(),
                    listOf<TypeReference<*>>(TypeReference.create(Uint256::class.java))
                )
                val encoded = FunctionEncoder.encode(function)
                val response = web3.ethCall(
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        credentials.address, contractAddress, encoded
                    ), org.web3j.protocol.core.DefaultBlockParameterName.LATEST
                ).send()

                val decoded = FunctionReturnDecoder.decode(
                    response.result, function.outputParameters
                )
                val balance = decoded[0].value.toString()
                callback(balance)
            } catch (e: Exception) {
                e.printStackTrace()
                callback("Error: ${e.message}")
            }
        }.start()
    }

    fun getWalletAddress(): String {
        val credentials = Credentials.create(privateKey)
        return credentials.address
    }
}