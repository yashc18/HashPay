package com.example.hashpay.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hashpay.data.AppDatabaseProvider
import com.example.hashpay.data.database.entities.Invoice
import com.example.hashpay.data.database.entities.InvoiceStatus
import com.example.hashpay.data.repositories.InvoiceRepository
import com.example.hashpay.ui.states.InvoiceUIState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class InvoiceViewModel(
    private val repository: InvoiceRepository
) : ViewModel() {

    private val _invoices = MutableStateFlow<List<InvoiceUIState>>(emptyList())
    val invoices: StateFlow<List<InvoiceUIState>> = _invoices

    private val _currentInvoice = MutableStateFlow<InvoiceUIState>(InvoiceUIState())
    val currentInvoice: StateFlow<InvoiceUIState> = _currentInvoice

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage



    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _filterStatus = MutableStateFlow<InvoiceStatus?>(null)
    val filterStatus: StateFlow<InvoiceStatus?> = _filterStatus

    // Fields for creating/editing invoices
    private val _receiverAddress = MutableStateFlow("")
    val receiverAddress: StateFlow<String> = _receiverAddress

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _dueDate = MutableStateFlow<Long?>(null)
    val dueDate: StateFlow<Long?> = _dueDate

    init {
        loadInvoices()
    }

    fun loadInvoices() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val status = _filterStatus.value
                val invoicesFlow = if (status != null) {
                    repository.getInvoicesByStatus(status)
                } else {
                    repository.getAllInvoices()
                }

                invoicesFlow.collect { invoices ->
                    _invoices.value = invoices.map { it.toUIState() }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load invoices: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun setFilterStatus(status: InvoiceStatus?) {
        _filterStatus.value = status
        loadInvoices()
    }

    fun setReceiverAddress(address: String) {
        _receiverAddress.value = address
    }

    fun setAmount(amount: String) {
        _amount.value = amount
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun setDueDate(timestamp: Long?) {
        _dueDate.value = timestamp
    }

    fun clearForm() {
        _receiverAddress.value = ""
        _amount.value = ""
        _description.value = ""
        _dueDate.value = null
        _currentInvoice.value = InvoiceUIState()
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    fun createInvoice(senderAddress: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val amountValue = _amount.value.toDoubleOrNull() ?: 0.0
                if (amountValue <= 0) {
                    _errorMessage.value = "Amount must be greater than 0"
                    _isLoading.value = false
                    return@launch
                }

                if (_receiverAddress.value.isBlank()) {
                    _errorMessage.value = "Receiver address cannot be empty"
                    _isLoading.value = false
                    return@launch
                }

                val invoice = Invoice(
                    receiverAddress = _receiverAddress.value,
                    senderAddress = senderAddress,
                    amount = amountValue,
                    description = _description.value,
                    status = InvoiceStatus.PENDING,
                    dueDate = _dueDate.value
                )

                val id = repository.createInvoice(invoice)
                _successMessage.value = "Invoice created successfully"
                clearForm()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create invoice: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadInvoiceById(invoiceId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val invoice = repository.getInvoiceById(invoiceId)
                if (invoice != null) {
                    _currentInvoice.value = invoice.toUIState()
                    _receiverAddress.value = invoice.receiverAddress
                    _amount.value = invoice.amount.toString()
                    _description.value = invoice.description
                    _dueDate.value = invoice.dueDate
                } else {
                    _errorMessage.value = "Invoice not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load invoice: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateInvoiceStatus(invoiceId: Long, newStatus: InvoiceStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val invoice = repository.getInvoiceById(invoiceId)
                if (invoice != null) {
                    repository.updateInvoiceStatus(invoice, newStatus)
                    _errorMessage.value = "Invoice status updated"
                } else {
                    _errorMessage.value = "Invoice not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update invoice: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markInvoiceAsPaid(invoiceId: Long, transactionHash: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.markInvoiceAsPaid(invoiceId, transactionHash)
                _errorMessage.value = "Invoice marked as paid"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to mark invoice as paid: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteInvoice(invoiceId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val invoice = repository.getInvoiceById(invoiceId)
                if (invoice != null) {
                    repository.deleteInvoice(invoice)
                    _errorMessage.value = "Invoice deleted"
                } else {
                    _errorMessage.value = "Invoice not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete invoice: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun Invoice.toUIState(): InvoiceUIState {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val isPaid = status == InvoiceStatus.PAID
        val isOverdue = status == InvoiceStatus.OVERDUE ||
                (dueDate != null && dueDate < System.currentTimeMillis() && status == InvoiceStatus.PENDING)

        return InvoiceUIState(
            id = id,
            receiverAddress = receiverAddress,
            senderAddress = senderAddress,
            amount = amount,
            description = description,
            status = status,
            createdAt = createdAt,
            paidAt = paidAt,
            dueDate = dueDate,
            transactionHash = transactionHash,
            formattedAmount = String.format("%.4f ETH", amount),
            formattedDate = dateFormat.format(Date(createdAt)),
            formattedDueDate = dueDate?.let { dateFormat.format(Date(it)) } ?: "No due date",
            isPaid = isPaid,
            isOverdue = isOverdue
        )
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

class InvoiceViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            AppDatabaseProvider.initializeDatabase(context)
            return InvoiceViewModel(AppDatabaseProvider.getInvoiceRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}