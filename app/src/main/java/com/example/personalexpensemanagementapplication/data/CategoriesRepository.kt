package com.example.personalexpensemanagementapplication.data

import androidx.compose.runtime.mutableStateListOf
import com.example.personalexpensemanagementapplication.model.Category

object CategoriesRepository {
    private val _expenseCategories = mutableStateListOf<Category>()
    private val _incomeCategories = mutableStateListOf<Category>()

    val expenseCategories: List<Category> get() = _expenseCategories
    val incomeCategories: List<Category> get() = _incomeCategories

    init {
        // default expense categories
        _expenseCategories.addAll(listOf(
            Category("Ăn uống", "🍔"), Category("Sinh hoạt", "🏠"), Category("Di chuyển", "🚗"),
            Category("Học tập", "📚"), Category("Giải trí", "🎮"),
            Category("Y tế", "🩺"), Category("Mua sắm", "🛍️"), Category("Khác", "➕")
        ))

        // default income categories
        _incomeCategories.addAll(listOf(
            Category("Lương", "💵"), Category("Thưởng", "🎁"), Category("Trợ cấp", "💰"),
            Category("Kinh doanh", "📈"), Category("Đầu tư", "📊"), Category("Khác", "➕")
        ))
    }

    fun addExpenseCategory(cat: Category) {
        // don't duplicate labels
        if (_expenseCategories.none { it.label == cat.label }) {
            _expenseCategories.add(0, cat)
        }
    }

    fun addIncomeCategory(cat: Category) {
        if (_incomeCategories.none { it.label == cat.label }) {
            _incomeCategories.add(0, cat)
        }
    }

    fun removeExpenseCategory(label: String) {
        _expenseCategories.removeAll { it.label == label }
    }

    fun removeIncomeCategory(label: String) {
        _incomeCategories.removeAll { it.label == label }
    }
}

