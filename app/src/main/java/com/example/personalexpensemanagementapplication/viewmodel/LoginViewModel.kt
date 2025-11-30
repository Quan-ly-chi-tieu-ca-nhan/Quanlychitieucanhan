package com.example.personalexpensemanagementapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalexpensemanagementapplication.SignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Data class này giữ trạng thái cho màn hình đăng nhập
data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    // Được gọi khi quá trình đăng nhập trả về kết quả
    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        ) }
    }

    // Đặt lại trạng thái, thường là sau khi điều hướng
    fun resetState() {
        _state.update { SignInState() }
    }
}
