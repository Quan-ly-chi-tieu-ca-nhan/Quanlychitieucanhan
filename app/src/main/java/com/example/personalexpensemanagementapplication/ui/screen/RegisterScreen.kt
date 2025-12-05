package com.example.personalexpensemanagementapplication.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // State variables
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // --- UI ---
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Đăng kí tài khoản", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Tên đăng nhập") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Địa chỉ Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Nhập lại mật khẩu") },
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            // Register Button
            Button(
                onClick = {
                    error = null // Clear previous errors
                    // --- Validation ---
                    if (username.isBlank() || email.isBlank() || password.isBlank()) {
                        error = "Vui lòng điền đầy đủ thông tin."
                        return@Button
                    }
                    if (password != confirmPassword) {
                        error = "Mật khẩu không khớp."
                        return@Button
                    }

                    isLoading = true // Bắt đầu loading
                    // --- Firebase Registration ---
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build()
                                user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                                    isLoading = false // Kết thúc loading
                                    if (profileTask.isSuccessful) {
                                        // Save user to Firestore collection 'users' so admin can list them
                                        user.uid.let { uid ->
                                            val userDoc = mapOf(
                                                "email" to email,
                                                "name" to username
                                            )
                                            db.collection("users").document(uid).set(userDoc)
                                                .addOnCompleteListener { dbTask ->
                                                    if (!dbTask.isSuccessful) {
                                                        Toast.makeText(context, "Đăng ký thành công nhưng không lưu được thông tin người dùng: ${dbTask.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                                                    }
                                                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                                    onRegisterSuccess()
                                                }
                                        }
                                    } else {
                                        // Vẫn thành công dù không lưu được tên; still write to Firestore using available uid
                                        user?.uid?.let { uid ->
                                            val userDoc = mapOf(
                                                "email" to email,
                                                "name" to username
                                            )
                                            db.collection("users").document(uid).set(userDoc)
                                                .addOnCompleteListener { dbTask ->
                                                    if (!dbTask.isSuccessful) {
                                                        Toast.makeText(context, "Đăng ký thành công nhưng không lưu được thông tin người dùng: ${dbTask.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                                                    }
                                                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                                    onRegisterSuccess()
                                                }
                                        } ?: run {
                                            Toast.makeText(context, "Đăng ký thành công nhưng không lấy được UID để lưu dữ liệu.", Toast.LENGTH_LONG).show()
                                            onRegisterSuccess()
                                        }
                                    }
                                }
                            } else {
                                isLoading = false // Kết thúc loading
                                error = task.exception?.localizedMessage ?: "Đăng ký thất bại."
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading, // Vô hiệu hóa nút khi đang tải
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Đăng kí", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBackToLogin) {
                Text("Đã có tài khoản? Đăng nhập")
            }
        }

        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}