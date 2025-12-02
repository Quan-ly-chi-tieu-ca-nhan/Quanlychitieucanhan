package navigation

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.personalexpensemanagementapplication.Destinations
import com.example.personalexpensemanagementapplication.R
import com.example.personalexpensemanagementapplication.SignInResult
import com.example.personalexpensemanagementapplication.UserData
import com.example.personalexpensemanagementapplication.ui.screen.ForgotPasswordScreen
import com.example.personalexpensemanagementapplication.ui.screen.HomeScreen
import com.example.personalexpensemanagementapplication.ui.screen.LoginScreen
import com.example.personalexpensemanagementapplication.ui.screen.RegisterScreen
import com.example.personalexpensemanagementapplication.ui.screen.UserManagementScreen
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Danh sách email của Admin
private val adminEmails = listOf("nhu0868210475@gmail.com", "minhnx0034@ut.edu.vn")

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = Firebase.firestore

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // --- TẠO HÀM LOGOUT DÙNG CHUNG ---
    val logout: () -> Unit = {
        coroutineScope.launch {
            googleSignInClient.signOut().await()
            FirebaseAuth.getInstance().signOut()
            LoginManager.getInstance().logOut()
            navController.navigate("login") {
                popUpTo(0) // Xóa toàn bộ backstack
            }
        }
    }

    // Hàm lưu thông tin người dùng vào Firestore
    fun writeUserToFirestore(userData: UserData) {
        val userDocument = db.collection("users").document(userData.userId)
        userDocument.set(userData).addOnSuccessListener {
            Log.d("AppNavigation", "User data saved to Firestore.")
        }.addOnFailureListener { e ->
            Log.w("AppNavigation", "Error writing user data to Firestore", e)
        }
    }

    // Hàm xử lý kết quả đăng nhập chung - NGUỒN CHÂN LÝ DUY NHẤT
    fun handleSignInResult(result: SignInResult) {
        if (result.data != null && result.data.email != null) {
            Log.d("AdminCheck", "Login success. Email: '${result.data.email}'. Checking for admin.")
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            writeUserToFirestore(result.data)
            
            if (adminEmails.contains(result.data.email)) {
                Log.d("AdminCheck", "Admin detected. Navigating to user_management.")
                navController.navigate("user_management") { popUpTo("login") { inclusive = true } }
            } else {
                Log.d("AdminCheck", "Regular user. Navigating to home.")
                navController.navigate(Destinations.HOME) { popUpTo("login") { inclusive = true } }
            }
        } else {
            val errorMessage = result.errorMessage ?: "Lỗi không xác định."
            Log.e("AdminCheck", "Login failed: $errorMessage")
            Toast.makeText(context, "Đăng nhập thất bại: $errorMessage", Toast.LENGTH_LONG).show()
        }
    }

    fun processFirebaseUser(user: FirebaseUser?): SignInResult {
        return if (user != null) {
            SignInResult(
                data = UserData(
                    userId = user.uid,
                    username = user.displayName,
                    profilePictureUrl = user.photoUrl?.toString(),
                    email = user.email
                ),
                errorMessage = null
            )
        } else {
            SignInResult(data = null, errorMessage = "User is null")
        }
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                coroutineScope.launch {
                    val signInResult = try {
                        val account = GoogleSignIn.getSignedInAccountFromIntent(intent).await()
                        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                        val user = FirebaseAuth.getInstance().signInWithCredential(credential).await().user
                        processFirebaseUser(user)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        SignInResult(data = null, errorMessage = e.message)
                    }
                    handleSignInResult(signInResult)
                }
            }
        }
    }

    val facebookCallbackManager = remember { CallbackManager.Factory.create() }
    DisposableEffect(Unit) {
        val callback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
                    val signInResult = processFirebaseUser(task.result?.user)
                    handleSignInResult(signInResult)
                }
            }
            override fun onCancel() { Log.w("AppNavigation", "Facebook login cancelled.") }
            override fun onError(error: FacebookException) { Log.e("AppNavigation", "Facebook login error.", error) }
        }
        LoginManager.getInstance().registerCallback(facebookCallbackManager, callback)
        onDispose { LoginManager.getInstance().unregisterCallback(facebookCallbackManager) }
    }

    val facebookLauncher = rememberLauncherForActivityResult(
        contract = LoginManager.getInstance().createLogInActivityResultContract(facebookCallbackManager, null),
        onResult = { /* Handled by callback */ }
    )
    
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        if (adminEmails.contains(FirebaseAuth.getInstance().currentUser?.email)) "user_management" else Destinations.HOME
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { /* Logic được xử lý bởi handleSignInResult */ },
                onGoogleLogin = { googleLauncher.launch(googleSignInClient.signInIntent) },
                onFacebookLogin = { facebookLauncher.launch(listOf("email", "public_profile")) },
                onRegisterClick = { navController.navigate("register") },
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }
        composable("register") { RegisterScreen({ navController.navigate("login") }, { navController.popBackStack() }) }
        composable("forgot_password") { ForgotPasswordScreen(navController = navController) }

        composable(Destinations.HOME) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            HomeScreen(navController::navigate, navBackStackEntry?.destination?.route ?: Destinations.HOME)
        }
        
        composable("user_management") {
            // TRUYỀN HÀM LOGOUT VÀO MÀN HÌNH ADMIN
            UserManagementScreen(navController = navController, onLogoutClick = logout)
        }

        composable(Destinations.SETTINGS) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                // SỬ DỤNG HÀM LOGOUT DÙNG CHUNG
                Button(onClick = logout) { Text("Đăng xuất") }
            }
        }

        composable(Destinations.INCOME) { Text("Khoản thu Screen") }
        composable(Destinations.EXPENSE) { Text("Khoản chi Screen") }
        composable(Destinations.STATISTICS) { Text("Thống kê Screen") }
        composable(Destinations.LIMIT) { Text("Limit Screen") }
        composable(Destinations.TRANSACTIONS) { Text("Transactions Screen") }
    }
}
