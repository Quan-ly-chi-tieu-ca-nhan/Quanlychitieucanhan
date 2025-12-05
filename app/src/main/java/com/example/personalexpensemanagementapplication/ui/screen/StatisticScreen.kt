package com.example.personalexpensemanagementapplication.ui.screen

import com.example.personalexpensemanagementapplication.Destinations
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import com.example.personalexpensemanagementapplication.data.TransactionsRepository
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(onBack: (() -> Unit)? = null, onNavigate: (String) -> Unit = {}, currentRoute: String = Destinations.STATISTICS) {
    val colors = MaterialTheme.colorScheme
    // Read live data from repository and shared filter
    val filter by remember { derivedStateOf { StatisticsFilterStore.filter } }
    val items = TransactionsRepository.items

    val incomeMap = items.filter { it.amount > 0 }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { tx -> tx.amount } }
        .toList()
        .sortedByDescending { it.second }

    val expenseMapAll = items.filter { it.amount < 0 }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { tx -> -tx.amount } }
        .toList()
        .sortedByDescending { it.second }

    // Totals
    val incomeTotal = incomeMap.sumOf { it.second }
    val expenseTotalActual = expenseMapAll.sumOf { it.second }

    // If a filter is set, prepare filtered lists for display (use actual expense categories)
    val displayExpenses = if (filter.type == StatsType.EXPENSE && filter.category != null) {
        expenseMapAll.filter { it.first == filter.category }
    } else expenseMapAll

    val displayIncomes = if (filter.type == StatsType.INCOME && filter.category != null) {
        incomeMap.filter { it.first == filter.category }
    } else incomeMap

    val isLoading = false

    Scaffold(
        topBar = {
            Surface(
                color = colors.primary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        DecorativeTitleLarge(title = "Thống kê")
                    }
                }
            }
        },
        bottomBar = {
            // Reuse the app-wide bottom navigation so icons/labels match HomeScreen
            AppBottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate)
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {

                // 1) Thống kê khoản thu
                Card(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💰", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(text = "Thống kê khoản thu", fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(text = formatVnd(incomeTotal), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.onSurface)

                        Spacer(Modifier.height(12.dp))

                        PieChartWithLegend(
                            data = displayIncomes.map { it.first to it.second },
                            title = "Khoản thu",
                            modifier = Modifier.fillMaxWidth(),
                            maxLegendItems = 6,
                            onSegmentClick = { label ->
                                StatisticsFilterStore.filter = StatsFilter(StatsType.INCOME, label)
                                onNavigate(com.example.personalexpensemanagementapplication.Destinations.INCOME)
                            },
                            collapseExtra = false
                        )

                        Spacer(Modifier.height(8.dp))
                        Text(text = "Chạm vào mục trong chú giải để lọc giao dịch tương ứng.", fontSize = 12.sp, color = colors.onSurface.copy(alpha = 0.6f))
                    }
                }

                // 2) Thống kê khoản chi
                Card(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💸", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(text = "Thống kê khoản chi", fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(text = formatVnd(expenseTotalActual), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.onSurface)

                        Spacer(Modifier.height(12.dp))

                        PieChartWithLegend(
                            data = displayExpenses.map { it.first to it.second },
                            title = "Khoản chi",
                            modifier = Modifier.fillMaxWidth(),
                            maxLegendItems = 6,
                            onSegmentClick = { label ->
                                if (label == "Số dư hiện tại") {
                                    StatisticsFilterStore.filter = StatsFilter(StatsType.ALL, null)
                                } else {
                                    StatisticsFilterStore.filter = StatsFilter(StatsType.EXPENSE, label)
                                }
                            }
                        )

                        Spacer(Modifier.height(8.dp))
                        Text(text = "Các mục trong chú giải có màu cố định. Chạm để lọc danh sách chi tiêu.", fontSize = 12.sp, color = colors.onSurface.copy(alpha = 0.6f))
                    }
                }

                // 3) So sánh Thu - Chi (biểu đồ cột)
                Card(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📊", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(text = "So sánh Thu - Chi", fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                        }

                        Spacer(Modifier.height(8.dp))

                        BarChartComparison(
                            income = incomeTotal,
                            expense = expenseTotalActual,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text(text = "Thu", fontWeight = FontWeight.Medium); Text(formatVnd(incomeTotal)) }
                            Column { Text(text = "Chi (đã dùng)", fontWeight = FontWeight.Medium); Text(formatVnd(expenseTotalActual)) }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(text = "Biểu đồ so sánh tổng thu và tổng chi trong tháng.", fontSize = 12.sp, color = colors.onSurface.copy(alpha = 0.6f))
                    }
                }

            }
        }
    }
}

@Composable
fun BarChartComparison(income: Double, expense: Double, modifier: Modifier = Modifier, barWidth: Dp = 56.dp, spaceBetween: Dp = 36.dp) {
    val colors = MaterialTheme.colorScheme
    // Polished XY-style comparison chart
    val maxVal = listOf(income, expense, 1.0).maxOrNull() ?: 1.0
    val chartHeight = 160.dp
    val divisions = 4 // number of horizontal gridlines (including top and bottom)

    // fractions to animate
    val targetInc = ((income / maxVal).coerceIn(0.0, 1.0)).toFloat()
    val targetExp = ((expense / maxVal).coerceIn(0.0, 1.0)).toFloat()

    val animInc by animateFloatAsState(targetValue = targetInc, animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing))
    val animExp by animateFloatAsState(targetValue = targetExp, animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing))

    // tooltip state
    var showIncTooltip by remember { mutableStateOf(false) }
    var showExpTooltip by remember { mutableStateOf(false) }

    // auto-hide tooltips after 2s
    LaunchedEffect(showIncTooltip) {
        if (showIncTooltip) {
            delay(2000)
            showIncTooltip = false
        }
    }
    LaunchedEffect(showExpTooltip) {
        if (showExpTooltip) {
            delay(2000)
            showExpTooltip = false
        }
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Left: fixed width Y-axis labels and unit
        Column(modifier = Modifier.width(72.dp).height(chartHeight), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
            Text(text = formatVnd(maxVal), fontSize = 12.sp, color = colors.onSurface.copy(alpha = 0.6f))
            Text(text = formatVnd(maxVal * 0.75), fontSize = 11.sp, color = colors.onSurface.copy(alpha = 0.6f))
            Text(text = formatVnd(maxVal / 2.0), fontSize = 11.sp, color = colors.onSurface.copy(alpha = 0.6f))
            Text(text = formatVnd(maxVal * 0.25), fontSize = 11.sp, color = colors.onSurface.copy(alpha = 0.6f))
            Text(text = formatVnd(0.0), fontSize = 11.sp, color = colors.onSurface.copy(alpha = 0.6f))
        }

        // Chart area: draw gridlines + axis then overlay bars
        Box(modifier = Modifier.weight(1f)) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)) {
                val w = size.width
                val h = size.height
                val gridColor = colors.outline.copy(alpha = 0.18f)
                val axisColor = colors.outline

                // horizontal gridlines (divisions)
                for (i in 0..divisions) {
                    val y = i * (h / divisions)
                    drawLine(color = gridColor, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
                }

                // vertical axis (x=0)
                drawLine(color = axisColor, start = Offset(0f, 0f), end = Offset(0f, h), strokeWidth = 2f)
                // x-axis baseline at bottom
                drawLine(color = axisColor, start = Offset(0f, h), end = Offset(w, h), strokeWidth = 2f)

                // simple vertical ticks approximate positions for two bars
                val x1 = w * 0.35f
                val x2 = w * 0.65f
                val tickH = 6f
                drawLine(color = axisColor, start = Offset(x1, h - tickH), end = Offset(x1, h), strokeWidth = 2f)
                drawLine(color = axisColor, start = Offset(x2, h - tickH), end = Offset(x2, h), strokeWidth = 2f)
            }

            // Bars and labels on top
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                // compute animated heights in dp
                val incHeightDp = (animInc * chartHeight.value).dp
                val expHeightDp = (animExp * chartHeight.value).dp

                // Income column
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    if (showIncTooltip) {
                        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 6.dp, modifier = Modifier.padding(bottom = 6.dp)) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Thu", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(text = formatVnd(income), fontSize = 12.sp)
                                Text(text = "${(targetInc * 100).toInt()}%", fontSize = 11.sp, color = colors.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    } else Spacer(Modifier.height(6.dp))

                    // Value label
                    Text(text = formatVnd(income), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))

                    Box(modifier = Modifier
                        .width(barWidth)
                        .height(chartHeight)
                        .padding(bottom = 0.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // track
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant))

                        // animated bar with subtle shadow
                        Box(modifier = Modifier
                            .width(barWidth)
                            .height(incHeightDp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(brush = Brush.verticalGradient(listOf(colors.primary, colors.primary.copy(alpha = 0.85f)))))

                        // percent label inside bar near top (if tall enough)
                        if (animInc > 0.05f) {
                            Box(modifier = Modifier
                                .width(barWidth)
                                .height(incHeightDp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(text = "${(animInc * 100).toInt()}%", fontSize = 11.sp, color = colors.onPrimary, modifier = Modifier.padding(top = 6.dp))
                            }
                        }

                        // clickable overlay
                        Box(modifier = Modifier
                            .width(barWidth)
                            .height(chartHeight)
                            .clickable { showIncTooltip = !showIncTooltip })
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Thu", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(spaceBetween))

                // Expense column
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    if (showExpTooltip) {
                        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 6.dp, modifier = Modifier.padding(bottom = 6.dp)) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Chi", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(text = formatVnd(expense), fontSize = 12.sp)
                                Text(text = "${(targetExp * 100).toInt()}%", fontSize = 11.sp, color = colors.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    } else Spacer(Modifier.height(6.dp))

                    // Value label
                    Text(text = formatVnd(expense), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))

                    Box(modifier = Modifier
                        .width(barWidth)
                        .height(chartHeight)
                        .padding(bottom = 0.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant))

                        Box(modifier = Modifier
                            .width(barWidth)
                            .height(expHeightDp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(brush = Brush.verticalGradient(listOf(colors.error, colors.error.copy(alpha = 0.85f)))))

                        if (animExp > 0.05f) {
                            Box(modifier = Modifier
                                .width(barWidth)
                                .height(expHeightDp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(text = "${(animExp * 100).toInt()}%", fontSize = 11.sp, color = colors.onError, modifier = Modifier.padding(top = 6.dp))
                            }
                        }

                        Box(modifier = Modifier
                            .width(barWidth)
                            .height(chartHeight)
                            .clickable { showExpTooltip = !showExpTooltip })
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Chi", fontSize = 12.sp)
                }
            }
        }
    }
}

// Note: StatsBottomNavigationBar removed; AppBottomNavigationBar from HomeScreen.kt is reused to keep icons and labels consistent across the app.

// end of file
