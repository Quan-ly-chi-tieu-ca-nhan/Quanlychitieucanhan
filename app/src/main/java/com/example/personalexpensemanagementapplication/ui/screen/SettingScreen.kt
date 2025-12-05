@file:Suppress("unused", "UNUSED_PARAMETER", "DEPRECATION")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.personalexpensemanagementapplication.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import com.example.personalexpensemanagementapplication.Destinations
import com.example.personalexpensemanagementapplication.data.TransactionsRepository
import com.example.personalexpensemanagementapplication.ui.theme.AppTheme
import com.example.personalexpensemanagementapplication.ui.theme.ThemeStore
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.google.firebase.auth.EmailAuthProvider



@Composable
fun SettingScreen(
    currentTheme: AppTheme = ThemeStore.currentTheme,
    onThemeChange: (AppTheme) -> Unit = { ThemeStore.currentTheme = it },
    onLogout: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    currentRoute: String = ""
) {
    val colors = MaterialTheme.colorScheme
    val auth = FirebaseAuth.getInstance()

    // email updates when auth state changes. username starts blank so user can enter a new display name.
    val emailState = remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var username by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // local UI state: theme selection (default from currentTheme)
    var selectedTheme by remember { mutableStateOf(currentTheme) }

    // register an auth state listener so the UI email updates when the user signs in/out
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { a ->
            val u = a.currentUser
            emailState.value = u?.email ?: ""
            // if username is empty, prefill with displayName to help user (but keep editable)
            if (username.isBlank()) username = u?.displayName ?: ""
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { AppTopBar(title = "Cài đặt") },
        bottomBar = {
            AppBottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate)
        }
    ) { paddingValues ->
        // Use theme-on-surface as local content color so text adapts to dark/light
        CompositionLocalProvider(LocalContentColor provides colors.onSurface) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // THEME
                    SettingsCard(colors = colors) {
                        Text("Nền:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.onSurface)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedTheme == AppTheme.Light,
                                onClick = {
                                    selectedTheme = AppTheme.Light
                                    onThemeChange(AppTheme.Light)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                            )
                            Text("Light", color = colors.onSurface)
                            Spacer(Modifier.width(16.dp))
                            RadioButton(
                                selected = selectedTheme == AppTheme.Dark,
                                onClick = {
                                    selectedTheme = AppTheme.Dark
                                    onThemeChange(AppTheme.Dark)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                            )
                            Text("Dark", color = colors.onSurface)
                            Spacer(Modifier.width(16.dp))
                            RadioButton(
                                selected = selectedTheme == AppTheme.System,
                                onClick = {
                                    selectedTheme = AppTheme.System
                                    onThemeChange(AppTheme.System)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                            )
                            Text("System", color = colors.onSurface)
                        }
                    }
                }
                //
                item {
                    // ACCOUNT: email read-only (from Firebase), username editable (initially blank)
                    SettingsCard(colors = colors) {
                        Text("Tài khoản:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.onSurface)
                        Spacer(Modifier.height(8.dp))

                        Text(text = "Email đăng ký:", color = colors.onSurface.copy(alpha = 0.9f))
                        Text(text = emailState.value, modifier = Modifier.padding(top = 4.dp), color = colors.onSurface.copy(alpha = 0.9f))
                        Spacer(Modifier.height(12.dp))

                        Text(text = "Tên hiển thị:", color = colors.onSurface.copy(alpha = 0.9f))
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "Nhập tên hiển thị") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface
                            )
                        )

                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(onClick = {
                                // update displayName on Firebase
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user != null) {
                                    user.updateProfile(userProfileChangeRequest {
                                        displayName = username
                                    }).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            scope.launch { snackbarHostState.showSnackbar("Lưu tên thành công") }
                                        } else {
                                            scope.launch { snackbarHostState.showSnackbar("Lưu tên thất bại") }
                                        }
                                    }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Người dùng chưa đăng nhập") }
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)) {
                                Text("Lưu")
                            }
                        }
                    }
                }


                // LIMIT
                item {
                    // read current value from repository so it stays in sync
                    val monthlyLimit = remember { mutableStateOf(TransactionsRepository.monthlyLimit.toFloat()) }
                    SettingsCard(colors = colors) {
                        Text("Ví khả dụng:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.onSurface)
                        Spacer(Modifier.height(12.dp))
                        Text("Hạn mức tháng hiện tại: ${formatCurrency(monthlyLimit.value)}", color = colors.onSurface)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { onNavigate(Destinations.LIMIT) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                        ){
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = "Go to limit",
                                tint = colors.onPrimary
                            )
                        }
                    }
                }

                // SECURITY — simplified: only change password form
                item {
                    // Form: current password + new password + change button
                    var currentPassword by remember { mutableStateOf("") }
                    var newPassword by remember { mutableStateOf("") }

                    SettingsCard(colors = colors) {
                        Text("Bảo mật:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.onSurface)
                        Spacer(Modifier.height(12.dp))

                        Text(text = "Đổi mật khẩu", color = colors.onSurface.copy(alpha = 0.9f))
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Mật khẩu hiện tại") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Mật khẩu mới") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface
                            )
                        )

                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(onClick = {
                                val user = FirebaseAuth.getInstance().currentUser
                                val email = emailState.value
                                if (user == null || email.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("Người dùng chưa đăng nhập") }
                                    return@Button
                                }

                                if (newPassword.length < 6) {
                                    scope.launch { snackbarHostState.showSnackbar("Mật khẩu mới phải có ít nhất 6 ký tự") }
                                    return@Button
                                }

                                // Reauthenticate with email+currentPassword then update
                                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                                user.reauthenticate(credential).addOnCompleteListener { reauth ->
                                    if (reauth.isSuccessful) {
                                        user.updatePassword(newPassword).addOnCompleteListener { upd ->
                                            if (upd.isSuccessful) {
                                                scope.launch { snackbarHostState.showSnackbar("Đổi mật khẩu thành công") }
                                                // clear fields
                                                currentPassword = ""
                                                newPassword = ""
                                            } else {
                                                scope.launch { snackbarHostState.showSnackbar("Đổi mật khẩu thất bại") }
                                            }
                                        }
                                    } else {
                                        scope.launch { snackbarHostState.showSnackbar("Mật khẩu hiện tại không đúng") }
                                    }
                                }

                            }, colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)) {
                                Text("Thay đổi mật khẩu")
                            }
                        }
                    }
                }

                // LOGOUT BUTTON
                item {
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError)
                    ) {
                        Text("Đăng xuất")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    colors: ColorScheme,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun AnimatedPressButton(text: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }

    Button(
        onClick = {
            onClick()
            scope.launch {
                // quick press animation
                scaleAnim.animateTo(0.95f, animationSpec = tween(durationMillis = 80))
                delay(120)
                scaleAnim.animateTo(1f, animationSpec = tween(durationMillis = 120))
            }
        },
        modifier = Modifier
            .height(40.dp)
            .scale(scaleAnim.value),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
    ) {
        Text(text = text, fontSize = 14.sp)
    }
}
