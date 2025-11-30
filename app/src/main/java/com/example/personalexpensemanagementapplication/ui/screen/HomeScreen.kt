package com.example.personalexpensemanagementapplication.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// Định nghĩa các màu sắc sử dụng trong ứng dụng
val PrimaryGreen = Color(0xFF4CAF50)
val PrimaryBlue = Color(0xFF2196F3)
val LightBlueBg = Color(0xFFE3F2FD)
val ExpenseRed = Color(0xFFF44336)
val IncomeGreen = Color(0xFF4CAF50)
val TextGray = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// ⭐️ ĐÃ SỬA LỖI: THÊM THAM SỐ onLogout VÀO ĐỊNH NGHĨA HÀM
fun HomeScreen(onLogout: () -> Unit) {
    Scaffold(
        topBar = { AppHeader() },
        bottomBar = { AppBottomNavigationBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { LimitAndBalanceCard() }
            item { QuickStatisticsCard() }
            item { RecentTransactionsCard() }

            // ⭐️ BỔ SUNG: NÚT ĐĂNG XUẤT ĐỂ SỬ DỤNG THAM SỐ onLogout
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLogout, // Gọi hàm onLogout được truyền từ AppNavigation
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("ĐĂNG XUẤT", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// =========================================================================
// Header (Thanh tiêu đề trên cùng)
// (Không thay đổi)
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
            // Left placeholder: same structure as notification but invisible to keep title centered
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(0f)
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
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

            // Center title
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                DecorativeTitleLarge(title = "Quản lý chi tiêu cá nhân")
            }

            // Right: visible notification
            Row(
                modifier = Modifier
                    .clickable { /* Xử lý sự kiện thông báo */ }
                    .padding(start = 8.dp),
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
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

// =========================================================================
// LimitAndBalanceCard (Thẻ hạn mức và số dư)
// (Không thay đổi)
// =========================================================================
@Composable
fun LimitAndBalanceCard() {
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightBlueBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            BalanceRowItem(icon = "🎯", label = "Hạn mức tháng", value = "5.000.000 VNĐ")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.8.dp,
                color = dividerColor
            )
            BalanceRowItem(icon = "💵", label = "Số dư hiện tại", value = "3.870.000 VNĐ")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.8.dp,
                color = dividerColor
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Xử lý điều hướng đến chi tiết */ }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BalanceRowItem(
                    icon = "👀",
                    label = "Đã dùng",
                    value = "1.130.000 VNĐ",
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Xem chi tiết",
                    tint = TextGray
                )
            }
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
// QuickStatisticsCard (Thẻ thống kê nhanh)
// (Không thay đổi)
// =========================================================================
@Composable
fun QuickStatisticsCard() {
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
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartDisplay(label = "Khoản thu", percentageUsed = 40f, primaryColor = IncomeGreen)
                ChartDisplay(label = "Khoản chi", percentageUsed = 75f, primaryColor = ExpenseRed)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Xem thêm",
                color = PrimaryBlue,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { /* Xử lý click xem thêm */ }
                    .padding(top = 8.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ChartDisplay(label: String, percentageUsed: Float, primaryColor: Color) {
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val arcBackgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .border(2.dp, outlineColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                val sweepAngle = percentageUsed * 3.6f
                // Vòng tròn nền
                drawArc(
                    color = arcBackgroundColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                // Vòng tròn tiến độ
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            }
            Text(
                text = "${percentageUsed.toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// =========================================================================
// RecentTransactionsCard (Thẻ danh sách chi tiêu gần đây)
// (Không thay đổi)
// =========================================================================
@Composable
fun RecentTransactionsCard() {
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

            TransactionItem(icon = "🍜", category = "Ăn uống", amount = -30000.0, date = "01/10/2025")
            TransactionItem(icon = "🎮", category = "Giải trí", amount = -100000.0, date = "01/10/2025")
            TransactionItem(icon = "📚", category = "Học tập", amount = -50000.0, date = "02/10/2025")

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Xử lý click xem tất cả */ }
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
    val amountText = String.format(Locale.forLanguageTag("vi-VN"), "%,.0f", amount) + " VNĐ"

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
// (Không thay đổi)
// =========================================================================

// Lớp dữ liệu cho các mục điều hướng
data class BottomNavItem(
    val label: String,
    val unicodeIcon: String?,
    val materialIcon: ImageVector?
)

@Composable
fun AppBottomNavigationBar() {
    val indicatorColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 5.dp
    ) {
        val items = listOf(
            BottomNavItem("Trang chủ", "🏠", Icons.Default.Home),
            BottomNavItem("Khoản thu", "💵", null),
            BottomNavItem("Khoản chi", "💸", null),
            BottomNavItem("Thống kê", "📊", null),
            BottomNavItem("Cài đặt", "⚙️", Icons.Default.Settings)
        )

        // Cải tiến: Sử dụng `State` để theo dõi và cập nhật mục đang được chọn
        var selectedIndex by remember { mutableIntStateOf(0) }

        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            NavigationBarItem(
                selected = isSelected,
                onClick = { selectedIndex = index }, // Cập nhật trạng thái khi nhấn
                icon = {
                    if (item.materialIcon != null) {
                        Icon(
                            imageVector = item.materialIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Text(
                            text = item.unicodeIcon ?: "",
                            fontSize = 26.sp
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                // Cải tiến: Quản lý màu sắc tập trung và rõ ràng hơn
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    unselectedIconColor = TextGray,
                    selectedTextColor = PrimaryGreen,
                    unselectedTextColor = TextGray,
                    indicatorColor = indicatorColor // Ẩn indicator mặc định
                )
            )
        }
    }
}