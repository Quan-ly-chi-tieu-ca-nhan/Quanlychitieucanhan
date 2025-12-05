package com.example.personalexpensemanagementapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalexpensemanagementapplication.ui.theme.PersonalExpenseManagementApplicationTheme
import com.example.personalexpensemanagementapplication.data.TransactionsRepository
import com.example.personalexpensemanagementapplication.Destinations
import kotlin.math.abs
import androidx.compose.ui.draw.clip
import com.example.personalexpensemanagementapplication.data.Transaction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications

// =========================================================================
// Màn hình DANH SÁCH GIAO DỊCH (dùng lại dữ liệu từ TransactionsRepository)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(onNavigate: (String) -> Unit = {}, currentRoute: String = Destinations.EXPENSE) {
    Scaffold(
        topBar = { AppTopBar(title = "Danh sách giao dịch") },
        bottomBar = { AppBottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate) }
    ) { paddingValues ->

        // 🔥 TỰ ĐỘNG SẮP XẾP NGÀY (dd/MM/yyyy) MỚI → CŨ
        val grouped by remember {
            derivedStateOf {

                // Định dạng ngày bạn đang dùng
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

                TransactionsRepository.items
                    .sortedByDescending { tx ->
                        sdf.parse(tx.date)
                    }
                    .groupBy { it.date }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            grouped.entries.forEach { (date, transactions) ->
                item(key = date) {
                    DailyTransactionGroup(date = date, transactions = transactions)
                }
            }
        }
    }
}



// =========================================================================
// COMPONENT 2: Daily Transaction Group (Nhóm Giao dịch theo Ngày)
// =========================================================================

@Composable
fun DailyTransactionGroup(date: String, transactions: List<Transaction>) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        // Tiêu đề ngày
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = colors.onSurface,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        // Card chứa danh sách giao dịch của ngày đó
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                for (i in transactions.indices) {
                    val tx = transactions[i]
                    TransactionRowItem(tx)
                    if (i < transactions.size - 1) {
                        // Đường kẻ mỏng, tinh tế giữa các item
                        HorizontalDivider(
                            color = colors.onSurface.copy(alpha = 0.12f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// COMPONENT 3: Transaction Row Item (Hàng Giao dịch)
// =========================================================================

@Composable
fun TransactionRowItem(item: Transaction) {
    val colors = MaterialTheme.colorScheme
    val isExpense = item.amount < 0
    val absAmount = abs(item.amount)
    val amountText = formatVnd(absAmount)
    val amountColor = if (isExpense) colors.error else colors.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Xử lý click vào giao dịch */ }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon và Danh mục
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(text = item.icon, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = item.category,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = colors.onSurface
            )
        }

        // Số tiền (không thêm dấu +/- vì màu đã biểu diễn)
        Text(
            text = amountText,
            color = amountColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = 100.dp)
        )
    }
}


// =========================================================================
// PREVIEW
// =========================================================================

@Preview(showBackground = true, name = "Transaction List Screen Preview")
@Composable
fun PreviewTransactionListScreen() {
    PersonalExpenseManagementApplicationTheme {
        TransactionListScreen(onNavigate = {}, currentRoute = Destinations.EXPENSE)
    }
}