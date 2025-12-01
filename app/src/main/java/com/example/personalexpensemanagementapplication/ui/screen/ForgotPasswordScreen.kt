
package com.example.personalexpensemanagementapplication.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "ForgotPasswordScreen"

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var message by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Quên Mật Khẩu", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            enabled = !isLoading // Vô hiệu hóa khi đang tải
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    message = ""
                    errorMessage = ""
                    Log.d(TAG, "Nút 'Gửi liên kết' đã được nhấn.")
                    if (email.text.isNotEmpty()) {
                        isLoading = true // Bắt đầu tải
                        Log.d(TAG, "Bắt đầu gửi email đến: ${email.text}")
                        Firebase.auth.sendPasswordResetEmail(email.text)
                            .addOnCompleteListener { task ->
                                isLoading = false // Kết thúc tải
                                if (task.isSuccessful) {
                                    Log.d(TAG, "Gửi email thành công.")
                                    message = "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư của bạn (kể cả mục Spam)."
                                    errorMessage = ""
                                } else {
                                    Log.e(TAG, "Gửi email thất bại.", task.exception)
                                    errorMessage = task.exception?.localizedMessage ?: "Đã xảy ra lỗi không xác định."
                                    message = ""
                                }
                            }
                    } else {
                        Log.d(TAG, "Email rỗng.")
                        errorMessage = "Vui lòng nhập email của bạn."
                        message = ""
                    }
                },
                enabled = !isLoading // Nút bị vô hiệu hóa khi đang tải
            ) {
                Text("Gửi liên kết đặt lại")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotEmpty()) {
            Text(text = message, color = Color.Green)
        }
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !isLoading // Vô hiệu hóa khi đang tải
        ) {
            Text("Quay lại Đăng nhập")
        }
    }
}
