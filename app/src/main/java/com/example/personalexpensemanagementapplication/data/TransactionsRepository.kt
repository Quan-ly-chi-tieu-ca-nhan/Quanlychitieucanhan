package com.example.personalexpensemanagementapplication.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object TransactionsRepository {

    // tháng mới → mặc định = 0
    var monthlyLimit by mutableStateOf(0.0)

    // danh sách giao dịch trống giống app mới cài
    private val _items = mutableStateListOf<Transaction>()

    // public read-only view
    val items: List<Transaction> get() = _items

    // thêm giao dịch
    fun addTransaction(tx: Transaction) {
        _items.add(0, tx) // thêm lên đầu
    }

    // xoá giao dịch
    fun removeTransaction(tx: Transaction) {
        _items.remove(tx)
    }
}
