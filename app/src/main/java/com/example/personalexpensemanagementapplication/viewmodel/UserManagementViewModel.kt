package com.example.personalexpensemanagementapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.personalexpensemanagementapplication.UserData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserManagementViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users.asStateFlow()

    private val db = Firebase.firestore
    private val functions = Firebase.functions // Thêm tham chiếu đến Functions
    private val currentUser = Firebase.auth.currentUser

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        if (currentUser == null) {
            Log.w("UserManagementVM", "Current user is null, cannot fetch users.")
            return
        }

        // addSnapshotListener sẽ tự động cập nhật lại danh sách khi có ai đó bị xóa trong DB
        db.collection("users")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("UserManagementVM", "Listen failed.", e)
                    _users.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val allUsers = snapshots.toObjects<UserData>()
                    val filteredUsers = allUsers.filter { it.userId != currentUser.uid }
                    _users.value = filteredUsers
                } else {
                    Log.d("UserManagementVM", "Current data: null")
                }
            }
    }

    /**
     * Gọi đến Cloud Function 'deleteUserAccount' để xóa hoàn toàn tài khoản người dùng.
     * @param userId ID của người dùng cần xóa.
     */
    fun deleteUser(userId: String) {
        val data = hashMapOf("userId" to userId)

        functions
            .getHttpsCallable("deleteUserAccount") // Tên của Cloud Function đã triển khai
            .call(data)
            .addOnSuccessListener { result ->
                // Cloud function đã chạy thành công. addSnapshotListener sẽ tự động cập nhật UI.
                Log.d("UserManagementVM", "Cloud function 'deleteUserAccount' called successfully: ${result.data}")
            }
            .addOnFailureListener { e ->
                // In ra lỗi nếu không gọi được Cloud Function
                Log.e("UserManagementVM", "Error calling 'deleteUserAccount' cloud function", e)
            }
    }
}
