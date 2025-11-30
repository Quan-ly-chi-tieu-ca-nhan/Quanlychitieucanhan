package com.example.personalexpensemanagementapplication

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.personalexpensemanagementapplication.ui.theme.PersonalExpenseManagementApplicationTheme
import navigation.AppNavigation
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- BẮT ĐẦU ĐOẠN CODE LẤY KEY HASH ---
        try {
            val packageName = "com.example.personalexpensemanagementapplication"
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }

            // Lấy chữ ký một cách an toàn và tương thích
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                info.signatures
            }

            // Lặp qua các chữ ký một cách an toàn (tránh lỗi null)
            signatures?.forEach { signature ->
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("KeyHash:", "PackageManager.NameNotFoundException", e)
        } catch (e: NoSuchAlgorithmException) {
            Log.e("KeyHash:", "NoSuchAlgorithmException", e)
        }
        // --- KẾT THÚC ĐOẠN CODE LẤY KEY HASH ---

        setContent {
            PersonalExpenseManagementApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
