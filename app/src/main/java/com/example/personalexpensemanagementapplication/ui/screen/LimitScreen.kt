@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.personalexpensemanagementapplication.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.personalexpensemanagementapplication.data.TransactionsRepository
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalexpensemanagementapplication.ui.theme.PersonalExpenseManagementApplicationTheme
import com.example.personalexpensemanagementapplication.Destinations
import androidx.compose.foundation.isSystemInDarkTheme

// A palette to assign colors per category (stable mapping)
val CategoryColors = listOf(
    Color(0xFF4CAF50), // green
    Color(0xFFFF9800), // orange
    Color(0xFF2196F3), // blue
    Color(0xFFE91E63), // pink
    Color(0xFF9C27B0), // purple
    Color(0xFFFFC107), // amber
    Color(0xFF009688), // teal
    Color(0xFF3F51B5)  // indigo
)

// =========================================================================
// Main screen
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitScreen(onBack: () -> Unit = {}, onNavigate: (String) -> Unit = {}, currentRoute: String = Destinations.LIMIT) {
    // --- Application state (shared in TransactionsRepository) ---
    // Read/write to TransactionsRepository.monthlyLimit (Double) and convert to Float where needed
    // Use derivedStateOf so Compose explicitly tracks the repository state and recomposes when it changes
    val monthlyLimit by remember { derivedStateOf { TransactionsRepository.monthlyLimit.toFloat() } }

    // Aggregate expenses and incomes by category directly from TransactionsRepository so UI updates on changes
    val expenseAggregates by remember { derivedStateOf<List<CategoryAmount>> {
        TransactionsRepository.items.filter { it.amount < 0 }
            .groupBy { it.category }
            .map { (cat, txs) -> CategoryAmount(cat, txs.sumOf { -it.amount }.toFloat()) }
    } }

    val incomeAggregates by remember { derivedStateOf<List<CategoryAmount>> {
        TransactionsRepository.items.filter { it.amount > 0 }
            .groupBy { it.category }
            .map { (cat, txs) -> CategoryAmount(cat, txs.sumOf { it.amount }.toFloat()) }
    } }

    // Derived totals
    val totalExpense by remember { derivedStateOf { expenseAggregates.sumOf { it.amount.toDouble() }.toFloat() } }
    val usedAmount = totalExpense.coerceAtLeast(0f)
    val remainingAmount = (monthlyLimit - usedAmount).coerceAtLeast(0f)
    val usedProgress = if (monthlyLimit <= 0f) 0f else (usedAmount / monthlyLimit).coerceIn(0f, 1f)

    Scaffold(
        topBar = { AppTopBar(title = "Hạn mức") },
        bottomBar = { AppBottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary card (limit + progress + used/remaining)
            LimitSummaryCard(
                monthlyLimit = monthlyLimit,
                usedAmount = usedAmount,
                remainingAmount = remainingAmount,
                usedProgress = usedProgress,
                onMonthlyLimitChange = { newLimit -> TransactionsRepository.monthlyLimit = newLimit.toDouble() }
            )

            // Three main sections: Income stats, Expense stats, Comparison chart
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // On larger screens we might want them side-by-side; keep column-stacked for phones
            }

            IncomeStats(incomeList = incomeAggregates)

            ExpenseStats(expenseList = expenseAggregates, monthlyLimit = monthlyLimit)

        }
    }
}


// =========================================================================
// Summary card
// =========================================================================

@Composable
fun LimitSummaryCard(
    monthlyLimit: Float,
    usedAmount: Float,
    remainingAmount: Float,
    usedProgress: Float,
    onMonthlyLimitChange: (Float) -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(targetValue = usedProgress, animationSpec = tween(durationMillis = 700))

    val colors = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) colors.surfaceVariant else colors.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // --- Edit dialog state and UI (moved up so header button can toggle it) ---
            var showEditDialog by remember { mutableStateOf(false) }
            var tempText by remember { mutableStateOf(TransactionsRepository.monthlyLimit.toLong().toString()) }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Hạn mức tháng: ${formatCurrency(monthlyLimit)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                // Edit button to change monthly limit
                IconButton(onClick = {
                    // initialize tempText from current repository value (fresh) and open dialog
                    tempText = TransactionsRepository.monthlyLimit.toLong().toString()
                    showEditDialog = true
                }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Sửa hạn mức")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = colors.error,
                trackColor = colors.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Đã dùng", color = colors.onSurface.copy(alpha = 0.7f))
                    Text(text = formatCurrency(usedAmount), fontWeight = FontWeight.SemiBold, color = colors.error)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Số dư hiện tại", color = colors.onSurface.copy(alpha = 0.7f))
                    Text(text = formatCurrency(remainingAmount), fontWeight = FontWeight.SemiBold, color = colors.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // actions removed (no bell, no add) per user request


            // Dialog (placed last so it appears above card content)
            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text(text = "Sửa hạn mức tháng") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = tempText,
                                onValueChange = { input -> tempText = input.filter { it.isDigit() } },
                                label = { Text("Nhập hạn mức (VNĐ)") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Lưu ý: nhập số nguyên (ví dụ 5000000)")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val parsed = tempText.toLongOrNull()?.toFloat() ?: TransactionsRepository.monthlyLimit.toFloat()
                            // update via callback (and repository if provided)
                            onMonthlyLimitChange(parsed)
                            // also ensure repository updated in case callback wasn't used
                            TransactionsRepository.monthlyLimit = parsed.toDouble()
                            showEditDialog = false
                        }) { Text("Lưu") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) { Text("Hủy") }
                    }
                )
            }
        }
    }
}

// =========================================================================
// Income stats: each category fills 100% across incomes and are linked to income list
// =========================================================================

@Composable
fun IncomeStats(incomeList: List<CategoryAmount>) {
    val colors = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) colors.surfaceVariant else colors.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Thống kê khoản thu", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            val total = incomeList.sumOf { it.amount.toDouble() }.toFloat().coerceAtLeast(0f)
            if (total <= 0f) {
                Text(text = "Chưa có khoản thu", color = colors.onSurface.copy(alpha = 0.6f))
            } else {
                incomeList.forEachIndexed { index, item ->
                    val pct = (item.amount / total * 100f)
                    val color = CategoryColors[index % CategoryColors.size]
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(14.dp).background(color, shape = RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item.category, modifier = Modifier.weight(1f), color = colors.onSurface)
                        Text(text = String.format(Locale.ROOT,"%,.0f VNĐ", item.amount), fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = String.format(Locale.ROOT,"%.0f%%", pct), color = colors.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

// =========================================================================
// Expense stats: segments represent categories and are linked to expense list
// =========================================================================

@Composable
fun ExpenseStats(expenseList: List<CategoryAmount>, monthlyLimit: Float) {
    val colors = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) colors.surfaceVariant else colors.surface
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Thống kê khoản chi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            val total = expenseList.sumOf { it.amount.toDouble() }.toFloat().coerceAtLeast(0f)

            // A horizontal indicator with colored segments (fixed color mapping)
            Box(modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant)) {
                // draw segments
                var accumulated = 0f
                expenseList.forEach { item ->
                    val start = accumulated / (monthlyLimit.coerceAtLeast(1f))
                    accumulated += item.amount
                    val end = accumulated / (monthlyLimit.coerceAtLeast(1f))
                    val widthFraction = (end - start).coerceIn(0f, 1f)
                    val color = getCategoryColor(item.category)
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(widthFraction).align(Alignment.CenterStart).offset(x = (start * 100).dp /* visual-only offset; small approximation */).background(color))
                    // Note: offset uses dp conversion approx - OK for demo; production should use Canvas
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List breakdown
            if (expenseList.isEmpty()) {
                Text(text = "Chưa có khoản chi", color = colors.onSurface.copy(alpha = 0.6f))
            } else {
                expenseList.forEach { item ->
                    val color = getCategoryColor(item.category)
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(14.dp).background(color, shape = RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item.category, modifier = Modifier.weight(1f), color = colors.onSurface)
                        Text(text = String.format(Locale.ROOT,"%,.0f VNĐ", item.amount), fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Totals
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Tổng chi", color = colors.onSurface.copy(alpha = 0.6f))
                    Text(text = String.format(Locale.ROOT,"%,.0f VNĐ", total), fontWeight = FontWeight.SemiBold, color = colors.error)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Hạn mức tháng", color = colors.onSurface.copy(alpha = 0.6f))
                    Text(text = String.format(Locale.ROOT,"%,.0f VNĐ", monthlyLimit), fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                }
            }
        }
    }
}

// Helper to get stable color for a category
@Composable
fun getCategoryColor(category: String): Color {
    // Keep a stable mapping in composition scope
    val map = remember { mutableStateMapOf<String, Color>() }
    if (!map.containsKey(category)) {
        val idx = map.size % CategoryColors.size
        map[category] = CategoryColors[idx]
    }
    return map[category] ?: CategoryColors.first()
}

// Comparison and explicit lists removed — LimitScreen now reads directly from TransactionsRepository and updates automatically.

// Small helper data and functions
data class CategoryAmount(val category: String, val amount: Float)

fun formatCurrency(value: Float): String {
    val locale = Locale.forLanguageTag("vi-VN")
    return String.format(locale, "%,.0f VNĐ", value.toDouble())
}


// =========================================================================
// Limit history section: removed arrow and simplified
// =========================================================================

@Suppress("unused")
@Composable
fun LimitHistorySection() {
    // kept for compatibility - can be extended / replaced by real data
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Lịch sử hạn mức:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        val colors = MaterialTheme.colorScheme
        val isDark = isSystemInDarkTheme()
        val cardBg = if (isDark) colors.surfaceVariant else colors.surface
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
            Column(modifier = Modifier.padding(16.dp)) {
                val historyData = listOf(
                    LimitHistoryItem("09/2025", 5000000f, 3800000f),
                    LimitHistoryItem("08/2025", 4000000f, 3600000f)
                )
                historyData.forEachIndexed { index, item ->
                    LimitHistoryRow(item)
                    if (index < historyData.size - 1) HorizontalDivider(color = colors.outline.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

data class LimitHistoryItem(val month: String, val limit: Float, val used: Float)

@Composable
fun LimitHistoryRow(item: LimitHistoryItem) {
    val colors = MaterialTheme.colorScheme
    val isOverLimit = item.used > item.limit
    val color = if (isOverLimit) colors.error else colors.onSurface.copy(alpha = 0.6f)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = item.month, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge, color = colors.primary)
        val locale = Locale.forLanguageTag("vi-VN")
        val formatted = String.format(locale, "%,.0f VNĐ", item.limit.toDouble())
        val usedFmt = String.format(locale, "%,.0f VNĐ", item.used.toDouble())
        Text(text = "$formatted (đã dùng $usedFmt)", style = MaterialTheme.typography.bodyMedium, color = color)
    }
}


// =========================================================================
// PREVIEW
// =========================================================================

@Preview(showBackground = true, name = "Limit Screen Preview")
@Composable
fun PreviewLimitScreen() {
    PersonalExpenseManagementApplicationTheme {
        LimitScreen()
    }
}