package com.example.hashpay.ui.states

import com.example.hashpay.data.database.entities.InvoiceStatus

data class InvoiceListState(
    val invoices: List<InvoiceUIState> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterStatus: InvoiceStatus? = null,
    val showPaid: Boolean = true,
    val showPending: Boolean = true,
    val showOverdue: Boolean = true,
    val searchQuery: String = ""
)