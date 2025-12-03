package com.example.personalexpensemanagementapplication

import com.google.firebase.Timestamp

data class SignInResult(
    val data: UserData? = null,
    val errorMessage: String? = null
)

// Bắt buộc phải có giá trị mặc định cho tất cả các trường để Firestore có thể tự động
// chuyển đổi dữ liệu (deserialization).
data class UserData(
    val userId: String = "",
    val username: String? = null,
    val profilePictureUrl: String? = null,
    val email: String? = null,
    val createdAt: Timestamp? = null // Thêm trường thời gian tạo
)
