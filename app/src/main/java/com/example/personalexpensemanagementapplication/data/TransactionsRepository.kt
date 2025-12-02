package com.example.personalexpensemanagementapplication.data

object TransactionsRepository {
    var monthlyLimit = 25000000.0 // 25 triệu VNĐ

    val items = listOf(
        Transaction("🍔", "Ăn uống", -55000.0, "2h ago"),
        Transaction("🚌", "Di chuyển", -150000.0, "1d ago"),
        Transaction("👕", "Mua sắm", -1200000.0, "3d ago"),
        Transaction("💰", "Lương", 5000000.0, "5d ago"),
        Transaction("💡", "Tiền điện", -450000.0, "6d ago"),
        Transaction("🎬", "Giải trí", -250000.0, "1w ago")
    )
}
