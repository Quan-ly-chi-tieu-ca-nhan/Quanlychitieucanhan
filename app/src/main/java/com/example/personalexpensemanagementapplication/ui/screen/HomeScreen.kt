package com.example.personalexpensemanagementapplication.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.personalexpensemanagementapplication.data.TransactionsRepository
import com.example.personalexpensemanagementapplication.Destinations
import com.example.personalexpensemanagementapplication.data.Transaction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs

// Định nghĩa các màu sắc sử dụng trong ứng dụng
val PrimaryGreen = Color(0xFF4CAF50)
val PrimaryBlue = Color(0xFF2196F3)
val LightBlueBg = Color(0xFFE3F2FD)
val ExpenseRed = Color(0xFFF44336)
val IncomeGreen = Color(0xFF4CAF50)
val TextGray = Color(0xFF757575)

// Simple currency formatter helper
fun formatVnd(amount: Double): String {
    return String.format(Locale.forLanguageTag("vi-VN"), "%,.0f VNĐ", amount)
}

// Generate a visually distinct, deterministic color per label using HSL hue from hash
@Suppress("RedundantInitializer")
fun stableColorForLabel(label: String): Color {
    // hue 0..360 from label hash
    val raw = label.hashCode().toLong() and 0xffffffffL
    val hue = (raw % 360).toFloat()

    // fixed saturation & lightness for good contrast; tweak if needed
    val s = 0.65f
    val l = 0.55f

    // convert HSL to RGB
    val c = (1f - abs(2f * l - 1f)) * s
    val hh = hue / 60f
    val x = c * (1f - abs(hh % 2f - 1f))

    val (r1, g1, b1) = when (hh.toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        5, 6 -> Triple(c, 0f, x)
        else -> Triple(c, x, 0f)
    }

    val m = l - c / 2f
    val r = (r1 + m).coerceIn(0f, 1f)
    val g = (g1 + m).coerceIn(0f, 1f)
    val b = (b1 + m).coerceIn(0f, 1f)

    return Color(r, g, b, 1f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit, currentRoute: String) {
    Scaffold(
        topBar = { AppHeader() },
        bottomBar = { AppBottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            item { LimitAndBalanceCard(onNavigate = onNavigate) }
            item { QuickStatisticsCard(onNavigate) }
            item { RecentTransactionsCard(onNavigate) }
        }
    }
}

// =========================================================================
// Header (Thanh tiêu đề trên cùng)
// =========================================================================
@Composable
fun AppHeader() {
    // Custom decorated header with blue background and rounded bottom corners
    Surface(
        color = PrimaryBlue,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Center title - use weight to center it while keeping notification on right
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                DecorativeTitleLarge(title = "Quản lý chi tiêu cá nhân")
            }

            // Right: visible notification (aligned to end)
            Row(
                modifier = Modifier
                    .clickable { /* Xử lý sự kiện thông báo */ }
                    .padding(start = 8.dp)
                    .wrapContentWidth(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Thông báo",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Notification",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

// New title composable: single white title with a subtle rounded highlight behind it
@Composable
fun DecorativeTitleLarge(title: String) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
    ) {
        Text(
            text = title,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

// =========================================================================
// LimitAndBalanceCard (Thẻ hạn mức và số dư) - now dynamic and linked to statistics
// =========================================================================
@Composable
fun LimitAndBalanceCard(onNavigate: (String) -> Unit) {
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    // compute from TransactionsRepository (shared app-wide limit)
    val monthlyLimit = TransactionsRepository.monthlyLimit
    val used = TransactionsRepository.items.sumOf { tx -> if (tx.amount < 0) -tx.amount else 0.0 }
    val remaining = (monthlyLimit - used).coerceAtLeast(0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate("Thống kê") }, // liên kết với phần thống kê khi bấm
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightBlueBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            BalanceRowItem(icon = "🎯", label = "Hạn mức tháng", value = formatVnd(monthlyLimit))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.8.dp,
                color = dividerColor
            )

            // Số dư hiện tại = hạn mức - đã dùng (tạm quy ước)
            BalanceRowItem(icon = "💵", label = "Số dư hiện tại", value = formatVnd(remaining))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.8.dp,
                color = dividerColor
            )

            // Đã dùng: hiển thị tổng chi tiêu trong tháng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BalanceRowItem(
                    icon = "👀",
                    label = "Đã dùng",
                    value = formatVnd(used),
                    modifier = Modifier.weight(1f)
                )
                // removed the right arrow as requested; card clickable navigates to statistics
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Optional small hint linking to statistics
            Text(
                text = "Bấm vô để hiện qua trang Limit",
                color = TextGray,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable { onNavigate(Destinations.LIMIT) }
            )
        }
    }
}

@Composable
fun BalanceRowItem(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$label:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// =========================================================================
// QuickStatisticsCard (Thẻ thống kê nhanh) - cập nhật để tránh nhảy dòng quá nhiều
// =========================================================================
@Composable
fun QuickStatisticsCard(onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "📊 Thống kê nhanh :",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Use shared monthly limit
            val monthlyLimit = TransactionsRepository.monthlyLimit

            // Build breakdown dynamically from TransactionsRepository
            val items = TransactionsRepository.items
            val expenseMap = items.filter { it.amount < 0 }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { tx -> -tx.amount } }
                .toList()
                .sortedByDescending { it.second }

            val incomeMap = items.filter { it.amount > 0 }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { tx -> tx.amount } }
                .toList()
                .sortedByDescending { it.second }

            val expenseSum = expenseMap.sumOf { it.second }
            val expensesData = if (expenseSum < monthlyLimit) {
                expenseMap + listOf("Số dư hiện tại" to (monthlyLimit - expenseSum))
            } else expenseMap

            val incomes = incomeMap

            // Xếp dọc: Khoản chi ở trên, Khoản thu ở dưới (không cần cuộn ngang)
            PieChartWithLegend(
                data = expensesData.map { it.first to it.second },
                title = "Khoản chi (so với hạn mức)",
                modifier = Modifier.fillMaxWidth(),
                maxLegendItems = 4,
                onSegmentClick = { label ->
                    // set filter to show only this expense category and navigate
                    if (label == "Số dư hiện tại") {
                        StatisticsFilterStore.filter = StatsFilter(StatsType.ALL, null)
                    } else {
                        StatisticsFilterStore.filter = StatsFilter(StatsType.EXPENSE, label)
                    }
                    onNavigate("Thống kê")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PieChartWithLegend(
                data = incomes.map { it.first to it.second },
                title = "Khoản thu",
                modifier = Modifier.fillMaxWidth(),
                maxLegendItems = 4,
                onSegmentClick = { label ->
                    // Set income filter and navigate to the Income screen
                    StatisticsFilterStore.filter = StatsFilter(StatsType.INCOME, label)
                    onNavigate("Khoản thu")
                },
                collapseExtra = false // show all income categories so pie fills 100%
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Xem thêm",
                color = PrimaryBlue,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        StatisticsFilterStore.filter = StatsFilter(StatsType.ALL, null)
                        onNavigate("Thống kê")
                    }
                    .padding(top = 8.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

// =========================================================================
// PieChart và helper để hiện legend (chú giải) - compact và có collapse cho mục phụ
// =========================================================================
@Composable
fun PieChartWithLegend(
    data: List<Pair<String, Double>>,
    title: String,
    modifier: Modifier = Modifier,
    maxLegendItems: Int = 3,
    onSegmentClick: ((String) -> Unit)? = null,
    collapseExtra: Boolean = true,
    collapseLabel: String = "Số dư hiện tại"
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // color palette fallback; stableColorForLabel will be preferred
        val palette = listOf(
            Color(0xFF4CAF50),
            Color(0xFFF44336),
            Color(0xFFFFC107),
            Color(0xFF2196F3),
            Color(0xFF9C27B0),
            Color(0xFF795548)
        )

        val sum = data.sumOf { it.second }
        val total = if (sum <= 0.0) 1.0 else sum

        // determine color for each slice by label (stable)
        val sliceColors = data.map { (label, _) -> stableColorForLabel(label) }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Canvas bên trái: kích thước lớn hơn để đẹp mắt
            Canvas(modifier = Modifier.size(100.dp)) {
                var startAngle = -90f
                data.forEachIndexed { index, entry ->
                    val sweep = (entry.second / total * 360f).toFloat()
                    val color = sliceColors.getOrNull(index) ?: palette[index % palette.size]
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true
                    )
                    startAngle += sweep
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Legend bên phải: show limited items and collapse the rest into "Số dư hiện tại"
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!collapseExtra) {
                    // show all items in legend
                    data.forEachIndexed { index, entry ->
                        val color = sliceColors.getOrNull(index) ?: palette[index % palette.size]
                        val percent = if (total > 0.0) entry.second / total else 0.0
                        LegendRow(color = color, label = entry.first, value = entry.second, percent = percent, onClick = onSegmentClick)
                    }
                } else {
                    val visible = data.take(maxLegendItems)
                    val extra = if (data.size > maxLegendItems) data.drop(maxLegendItems) else emptyList()

                    visible.forEachIndexed { index, entry ->
                        val color = sliceColors.getOrNull(index) ?: palette[index % palette.size]
                        val percent = if (total > 0.0) entry.second / total else 0.0
                        LegendRow(color = color, label = entry.first, value = entry.second, percent = percent, onClick = onSegmentClick)
                    }

                    if (extra.isNotEmpty()) {
                        val extraSum = extra.sumOf { it.second }
                        val extraColor = stableColorForLabel(collapseLabel)
                        val percent = if (total > 0.0) extraSum / total else 0.0
                        LegendRow(color = extraColor, label = collapseLabel, value = extraSum, percent = percent, onClick = onSegmentClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String, value: Double, percent: Double, onClick: ((String) -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke(label) }
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${(percent * 100).toInt()}%", fontSize = 11.sp, color = TextGray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format(Locale.forLanguageTag("vi-VN"), "%,.0f VNĐ", value),
                    fontSize = 11.sp,
                    color = TextGray
                )
            }
        }
    }
}

// =========================================================================
// RecentTransactionsCard (Thẻ danh sách chi tiêu gần đây)
// =========================================================================
@Composable
fun RecentTransactionsCard(onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "🔥 Danh sách chi tiêu gần đây:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Render transactions from repository (newest first)
            for (tx in TransactionsRepository.items) {
                TransactionItem(icon = tx.icon, category = tx.category, amount = tx.amount, date = tx.date)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Destinations.TRANSACTIONS) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Xem tất cả", tint = PrimaryBlue)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Xem tất cả giao dịch",
                    color = PrimaryBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TransactionItem(icon: String, category: String, amount: Double, date: String) {
    val isExpense = amount < 0
    val absAmount = abs(amount)
    val amountText = String.format(Locale.forLanguageTag("vi-VN"), "%,.0f", absAmount) + " VNĐ"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = amountText,
            color = if (isExpense) ExpenseRed else IncomeGreen,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = date,
            color = TextGray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(70.dp),
            textAlign = TextAlign.End
        )
    }
}

// =========================================================================
// BottomNavigationBar (Thanh điều hướng dưới cùng)
// =========================================================================

// Lớp dữ liệu cho các mục điều hướng
data class BottomNavItem(
    val route: String,
    val displayLabel: String,
    val unicodeIcon: String?,
    val materialIcon: ImageVector?
)

@Composable
fun AppBottomNavigationBar(currentRoute: String, onNavigate: (String) -> Unit) {
    // Make bottom bar visually match the Statistics screen's bottom bar
    val items = listOf(
        BottomNavItem(Destinations.HOME, "Home", "🏠", Icons.Default.Home),
        BottomNavItem(Destinations.INCOME, "Khoản thu", "💵", null),
        BottomNavItem(Destinations.EXPENSE, "Khoản chi", "💸", null),
        BottomNavItem(Destinations.STATISTICS, "Thống kê", "📊", null),
        BottomNavItem("settings", "Cài đặt", "⚙️", Icons.Default.Settings)
    )

    // determine selected index from currentRoute
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.let { if (it >= 0) it else 0 }

    Surface(
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onNavigate(item.route) }
                        .padding(6.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else Color.Transparent)
                    ) {
                        if (item.materialIcon != null) {
                            Icon(
                                imageVector = item.materialIcon,
                                contentDescription = item.displayLabel,
                                tint = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = item.unicodeIcon ?: "",
                                fontSize = 20.sp,
                                color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.displayLabel,
                        fontSize = 11.sp,
                        color = if (isSelected) PrimaryBlue else TextGray
                    )
                }
            }
        }
    }
}
