package com.example.personalexpensemanagementapplication.ui.screen

enum class StatsType { ALL, INCOME, EXPENSE }

data class StatsFilter(val type: StatsType, val category: String?)

object StatisticsFilterStore {
    var filter: StatsFilter = StatsFilter(StatsType.ALL, null)
}
