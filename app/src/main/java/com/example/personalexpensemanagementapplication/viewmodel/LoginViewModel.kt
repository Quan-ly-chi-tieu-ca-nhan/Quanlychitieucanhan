package com.example.personalexpensemanagementapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.personalexpensemanagementapplication.SignInResult
import com.example.personalexpensemanagementapplication.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Cập nhật data class để giữ thông tin người dùng
data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val userData: UserData? = null // Thêm thuộc tính này
)

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    // Cập nhật logic để lưu lại UserData
    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage,
            userData = result.data // Lưu lại dữ liệu người dùng
        ) }
    }

    // Đặt lại trạng thái
    fun resetState() {
        _state.update { SignInState() }
    }
}
