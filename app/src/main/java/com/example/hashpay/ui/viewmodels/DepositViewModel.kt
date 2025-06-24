package com.example.hashpay.ui.viewmodels

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hashpay.WalletConnectionManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.widget.Toast

class DepositViewModel(private val context: Context) : ViewModel() {
    private val walletManager = WalletConnectionManager.getInstance(context)

    private val _walletAddress = MutableStateFlow("")
    val walletAddress: StateFlow<String> = _walletAddress.asStateFlow()

    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap: StateFlow<Bitmap?> = _qrCodeBitmap.asStateFlow()

    init {
        loadWalletAddress()
    }

    private fun loadWalletAddress() {
        viewModelScope.launch {
            val address = walletManager.walletAddress.first()
            _walletAddress.value = address
            generateQrCode(address)
        }
    }

    private fun generateQrCode(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            try {
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                    }
                }

                _qrCodeBitmap.value = bitmap
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun copyAddressToClipboard() {
        val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
        val clip = ClipData.newPlainText("Wallet Address", _walletAddress.value)
        clipboard?.setPrimaryClip(clip)

        Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

class DepositViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DepositViewModel::class.java)) {
            return DepositViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}