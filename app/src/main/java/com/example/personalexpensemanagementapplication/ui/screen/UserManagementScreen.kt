package com.example.personalexpensemanagementapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.personalexpensemanagementapplication.UserData
import com.example.personalexpensemanagementapplication.viewmodel.UserManagementViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    navController: NavController,
    viewModel: UserManagementViewModel = viewModel(),
    onLogoutClick: () -> Unit // Thêm hàm callback
) {
    val users by viewModel.users.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý người dùng") },
                // KHÔNG CÓ NÚT QUAY LẠI
                actions = {
                    // Thêm nút đăng xuất vào thanh công cụ
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.Logout, contentDescription = "Đăng xuất")
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users) { user ->
                UserItem(user = user)
            }
        }
    }
}

@Composable
private fun UserItem(user: UserData) { // Bỏ tham số onDeleteClick
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.username ?: "N/A", fontWeight = FontWeight.Bold)
                Text(text = user.email ?: "N/A", style = MaterialTheme.typography.bodySmall)
                // Hiển thị ngày tạo
                user.createdAt?.let { timestamp ->
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    Text(
                        text = "Ngày tạo: ${sdf.format(timestamp.toDate())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            // KHÔNG CÓ NÚT XÓA
        }
    }
}
