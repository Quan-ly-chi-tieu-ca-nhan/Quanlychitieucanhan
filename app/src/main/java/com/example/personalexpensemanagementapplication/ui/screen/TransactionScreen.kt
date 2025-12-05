package com.example.personalexpensemanagementapplication.ui.screen

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalexpensemanagementapplication.Destinations
import com.example.personalexpensemanagementapplication.data.CategoriesRepository
import com.example.personalexpensemanagementapplication.data.TransactionsRepository
import com.example.personalexpensemanagementapplication.data.Transaction
import com.example.personalexpensemanagementapplication.model.Category as ModelCategory
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.MaterialTheme

// Thêm OptIn để sử dụng TopAppBar, NavigationBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(isExpense: Boolean, onNavigate: (String) -> Unit, currentRoute: String) {
    // colors is defined where needed inside child composables to keep scope minimal

    val title = if (isExpense) "Khoản chi" else "Khoản thu"
    val dateLabel = if (isExpense) "Ngày chi" else "Ngày thu"

    // State quản lý category được chọn (ví dụ)
    var selectedCategoryLabel by remember { mutableStateOf("") }
    var selectedCategoryIcon by remember { mutableStateOf("") }

    // State cho các input: amount, date, note
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var note by remember { mutableStateOf("") }

    // Scaffold cung cấp cấu trúc cơ bản
    Scaffold(
        topBar = { AppTopBar(title = title) },
        // AppBottomNavigationBar uses onNavigate callback and currentRoute to show selection
        bottomBar = { AppBottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Make inputs scrollable when they overflow, keep action buttons pinned at bottom
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    // 1. Chọn Danh mục (now returns label + icon)
                    CategorySelector(
                        isExpense = isExpense,
                        selectedCategoryLabel = selectedCategoryLabel,
                        onCategorySelected = { label, icon ->
                            selectedCategoryLabel = label
                            selectedCategoryIcon = icon
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item { // 2. Số tiền (editable)
                    AmountInput(value = amount, onValueChange = { amount = it })
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { // 3. Ngày chi/thu (editable + date picker)
                    DateInput(label = dateLabel, value = date, onValueChange = { date = it })
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // 5. Action buttons pinned at bottom
            ActionButtons(
                onCancel = { onNavigate(Destinations.HOME) },
                onSave = {
                    // build Transaction from current inputs
                    val categoryLabel = if (selectedCategoryLabel.isNotBlank()) selectedCategoryLabel else "Khác"
                    val icon = if (selectedCategoryIcon.isNotBlank()) selectedCategoryIcon else if (isExpense) "💸" else "💵"
                    // amount state is digits-only string (no separators). parse to Double safely
                    val parsed: Double = if (amount.isBlank()) 0.0 else (amount.toDoubleOrNull() ?: 0.0)
                    var amountValue: Double = parsed
                    if (isExpense) amountValue = -kotlin.math.abs(amountValue)

                    // Debug log of transaction values
                    Log.d("TransactionScreen", "Saving transaction: category=$categoryLabel icon=$icon amount=$amountValue date=$date note=$note")
                    try {
                        val tx = Transaction(icon = icon, category = categoryLabel, amount = amountValue, date = date)
                        TransactionsRepository.addTransaction(tx)
                        onNavigate(Destinations.HOME)
                    } catch (ex: Exception) {
                        Log.e("TransactionScreen", "Transaction save failed", ex)
                        onNavigate(Destinations.HOME)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp)) // end padding
        }
    }
}

// -------------------------------------------------------------------------
// COMPONENT 1: Header
// -------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHeader(title: String, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Header tương tự HomeScreen nhưng tùy chỉnh màu và icon
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Quay lại", tint = colors.onPrimary)
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Thông báo",
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Notification",
                    color = colors.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.primary, // Màu nền từ theme
            titleContentColor = colors.onPrimary
        )
    )
}

// -------------------------------------------------------------------------
// COMPONENT 2: Category Selector
// -------------------------------------------------------------------------

@Composable
fun CategorySelector(isExpense: Boolean, selectedCategoryLabel: String, onCategorySelected: (String, String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Use centralized repository so categories persist across screens
    val expenseCategories = CategoriesRepository.expenseCategories
    val incomeCategories = CategoriesRepository.incomeCategories
    val categories = if (isExpense) expenseCategories else incomeCategories
    val gridColumns = 3

    // Dialog state for adding custom category
    var showAddDialog by remember { mutableStateOf(false) }
    var newCatLabel by remember { mutableStateOf("") }
    var newCatIcon by remember { mutableStateOf("") }
    // list of default labels that should not be deletable
    val defaultExpenseLabels = listOf("Ăn uống","Sinh hoạt","Di chuyển","Học tập","Giải trí","Y tế","Mua sắm","Khác")
    val defaultIncomeLabels = listOf("Lương","Thưởng","Trợ cấp","Kinh doanh","Đầu tư","Khác")
    // delete mode and selected items to delete
    var deleteMode by remember { mutableStateOf(false) }
    val selectedToDelete = remember { mutableStateListOf<String>() }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header row: title + add / delete toggle
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Danh mục:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f), color = colors.onSurface)
            // removed header '+' button per request; 'Khác' grid item still opens add dialog
            IconButton(onClick = {
                // toggle delete mode
                deleteMode = !deleteMode
                if (!deleteMode) selectedToDelete.clear()
            }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = if (deleteMode) "Hủy xóa" else "Chế độ xóa",
                    tint = if (deleteMode) colors.error else colors.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Sử dụng LazyVerticalGrid cho bố cục lưới
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumns),
            modifier = Modifier.heightIn(max = 220.dp), // Giới hạn chiều cao cho scroll nếu nhiều
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 4.dp)
        ) {
            items(categories) { category ->
                val canDelete = if (isExpense) !defaultExpenseLabels.contains(category.label) else !defaultIncomeLabels.contains(category.label)
                // if in delete mode -> clicking toggles selection for deletion
                if (deleteMode) {
                    val isMarked = selectedToDelete.contains(category.label)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isMarked) colors.error.copy(alpha = 0.08f) else colors.surface)
                            .border(1.dp, if (isMarked) colors.error else colors.outline, RoundedCornerShape(8.dp))
                            .clickable(onClick = {
                                if (canDelete) {
                                    if (isMarked) selectedToDelete.remove(category.label) else selectedToDelete.add(category.label)
                                }
                            })
                            .padding(12.dp)
                    ) {
                        // main content
                        // add end padding so badge at top-right has space and won't overlap text
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterStart).padding(end = 28.dp)) {
                            Text(text = category.icon, fontSize = 22.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = category.label,
                                fontSize = 14.sp,
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            // don't display a "(mặc định)" label to avoid layout overflow in delete mode
                        }

                        // top-right selection badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isMarked) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(colors.primary, shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "✔", color = colors.onPrimary, fontSize = 10.sp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .border(1.dp, colors.outline, shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                } else {
                    CategoryItemWithDelete(
                        category = ModelCategory(category.label, category.icon),
                        isSelected = category.label == selectedCategoryLabel,
                        canDelete = canDelete,
                        onClick = {
                            if (category.label == "Khác") {
                                // open dialog to add custom category
                                newCatLabel = ""
                                newCatIcon = ""
                                showAddDialog = true
                            } else {
                                onCategorySelected(category.label, category.icon)
                            }
                        },
                        onDelete = {
                            // remove from repository
                            if (isExpense) CategoriesRepository.removeExpenseCategory(category.label) else CategoriesRepository.removeIncomeCategory(category.label)
                            // if deleted was selected, clear selection
                            if (selectedCategoryLabel == category.label) {
                                onCategorySelected("", "")
                            }
                        }
                    )
                }
            }
        }

        // Delete mode action row with confirmation
        var showConfirmDeleteDialog by remember { mutableStateOf(false) }
        if (deleteMode) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { selectedToDelete.clear(); deleteMode = false }) { Text("Hủy") }
                Button(onClick = {
                    // show confirmation dialog if any selected
                    if (selectedToDelete.isNotEmpty()) {
                        showConfirmDeleteDialog = true
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = colors.error)) {
                    Text("Xóa (${selectedToDelete.size})", color = colors.onError)
                }
            }

            if (showConfirmDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDeleteDialog = false },
                    title = { Text("Xác nhận xóa") },
                    text = { Text("Bạn có chắc muốn xóa ${selectedToDelete.size} mục đã chọn không? Hành động này không thể hoàn tác.") },
                    confirmButton = {
                        TextButton(onClick = {
                            for (label in selectedToDelete.toList()) {
                                if (isExpense) CategoriesRepository.removeExpenseCategory(label) else CategoriesRepository.removeIncomeCategory(label)
                            }
                            selectedToDelete.clear()
                            deleteMode = false
                            showConfirmDeleteDialog = false
                        }) { Text("Xóa") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDeleteDialog = false }) { Text("Hủy") }
                    }
                )
            }
        }

        // Add Category Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(text = "Thêm mục khác") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCatLabel,
                            onValueChange = { newCatLabel = it },
                            label = { Text("Tên mục (ví dụ: Từ thiện)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newCatIcon,
                            onValueChange = { newCatIcon = it },
                            label = { Text("Icon (emoji) - tùy chọn)") },
                            placeholder = { Text("Ví dụ: 🎗️ hoặc để trống") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val label = newCatLabel.trim().ifEmpty { "Khác" }
                        val icon = newCatIcon.trim().ifEmpty { "➕" }
                        // add to repository
                        if (isExpense) CategoriesRepository.addExpenseCategory(ModelCategory(label, icon)) else CategoriesRepository.addIncomeCategory(ModelCategory(label, icon))
                        onCategorySelected(label, icon)
                        showAddDialog = false
                    }) {
                        Text("Thêm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Hủy") }
                }
            )
        }
    }
}

@Composable
fun CategoryItemWithDelete(category: ModelCategory, isSelected: Boolean, canDelete: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.surfaceVariant else colors.surface)
            .border(
                1.dp,
                if (isSelected) colors.primary else colors.outline,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = category.icon, fontSize = 24.sp)
            Spacer(Modifier.width(8.dp))
            Text(text = category.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            if (canDelete) {
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(16.dp)) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Xóa mục", tint = colors.error)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// COMPONENT 3: Amount Input (editable)
// -------------------------------------------------------------------------

@Composable
fun AmountInput(value: String, onValueChange: (String) -> Unit) {
    // value holds raw digits (no separators)

    fun formatWithDots(digits: String): String {
        if (digits.isEmpty()) return ""
        val sb = StringBuilder()
        var count = 0
        for (i in digits.length - 1 downTo 0) {
            sb.append(digits[i])
            count++
            if (count == 3 && i != 0) {
                sb.append('.')
                count = 0
            }
        }
        return sb.reverse().toString()
    }

    val display = formatWithDots(value)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Số tiền:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = display,
            onValueChange = { new ->
                // when user types or pastes, keep only digits and update raw value
                val filtered = new.filter { it.isDigit() }
                onValueChange(filtered)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "0") },
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

// -------------------------------------------------------------------------
// COMPONENT 4: Date Input (editable + DatePickerDialog)
// -------------------------------------------------------------------------

@Composable
fun DateInput(label: String, value: String, onValueChange: (String) -> Unit) {
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Helper to parse existing value into calendar; fallback to today
    fun parseToCalendar(dateStr: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            val d = sdf.parse(dateStr)
            if (d != null) cal.time = d
        } catch (_: Exception) {
            // ignore and use today's date
        }
        return cal
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$label:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        val cal = parseToCalendar(value)

        // DatePickerDialog creation
        val datePicker = DatePickerDialog(
            context,
            { _: android.widget.DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val pickedCal = Calendar.getInstance()
                pickedCal.set(year, month, dayOfMonth)
                onValueChange(sdf.format(pickedCal.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) }, // allow manual typing
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* keep clickable for trailing icon */ },
            trailingIcon = {
                Text(text = "🗓️", fontSize = 20.sp, modifier = Modifier.clickable { datePicker.show() })
            },
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
        )
    }
}

// -------------------------------------------------------------------------
// COMPONENT 6: Action Buttons
// -------------------------------------------------------------------------

@Composable
fun ActionButtons(onCancel: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Nút Hủy (Hủy bỏ)
        val colors = MaterialTheme.colorScheme

        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = colors.error.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(text = "❌ Hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.onError)
        }

        // Nút Lưu (Lưu)
        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(text = "✔ Lưu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.onPrimary)
        }
    }
}

// -------------------------------------------------------------------------
// PREVIEWS (Để xem trước)
// -------------------------------------------------------------------------

@Preview(showBackground = true, name = "Khoản Chi Screen")
@Composable
fun PreviewKhoanChiScreen() {
    TransactionScreen(isExpense = true, onNavigate = {}, currentRoute = "transaction/expense")
}

@Preview(showBackground = true, name = "Khoản Thu Screen")
@Composable
fun PreviewKhoanThuScreen() {
    TransactionScreen(isExpense = false, onNavigate = {}, currentRoute = "transaction/income")
}
