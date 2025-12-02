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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.personalexpensemanagementapplication.viewmodel.LoginViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val loginState by loginViewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope() // <--- KHAI BÁO SCOPE Ở ĐÂY

    // --- AUTHENTICATION HANDLERS (GIỮ NGUYÊN) ---
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                coroutineScope.launch { // <--- SỬ DỤNG SCOPE ĐÃ KHAI BÁO
                    val signInResult = try {
                        val account = GoogleSignIn.getSignedInAccountFromIntent(intent).await()
                        val idToken = account.idToken!!
                        val credential = GoogleAuthProvider.getCredential(idToken, null)
                        val user = FirebaseAuth.getInstance().signInWithCredential(credential).await().user
                        SignInResult(
                            data = user?.let { UserData(userId = it.uid, username = it.displayName, profilePictureUrl = it.photoUrl?.toString()) },
                            errorMessage = null
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        SignInResult(data = null, errorMessage = e.message)
                    }
                    loginViewModel.onSignInResult(signInResult)
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
                    val signInResult = if (task.isSuccessful) {
                        val user = task.result?.user
                        SignInResult(data = user?.let { UserData(it.uid, it.displayName, it.photoUrl?.toString()) }, errorMessage = null)
                    } else {
                        SignInResult(data = null, errorMessage = task.exception?.message)
                    }
                    loginViewModel.onSignInResult(signInResult)
                }
            }
            override fun onCancel() { /* ... */ }
            override fun onError(error: FacebookException) { /* ... */ }
        }
        LoginManager.getInstance().registerCallback(facebookCallbackManager, callback)
        onDispose { LoginManager.getInstance().unregisterCallback(facebookCallbackManager) }
    }

    val facebookLauncher = rememberLauncherForActivityResult(
        contract = LoginManager.getInstance().createLogInActivityResultContract(facebookCallbackManager, null),
        onResult = { /* ... */ }
    )

    // --- NAVIGATION LOGIC ---
    LaunchedEffect(loginState.isSignInSuccessful) {
        if (loginState.isSignInSuccessful) {
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_LONG).show()
            navController.navigate(Destinations.HOME) { popUpTo("login") { inclusive = true } }
            loginViewModel.resetState()
        }
    }

    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) Destinations.HOME else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        // --- LUỒNG XÁC THỰC ---
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Destinations.HOME) { popUpTo("login") { inclusive = true } } },
                onGoogleLogin = { googleLauncher.launch(googleSignInClient.signInIntent) },
                onFacebookLogin = { facebookLauncher.launch(listOf("email", "public_profile")) },
                onRegisterClick = { navController.navigate("register") },
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("login") },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController = navController)
        }

        // --- LUỒNG CHÍNH CỦA ỨNG DỤNG ---
        composable(Destinations.HOME) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Destinations.HOME
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) },
                currentRoute = currentRoute
            )
        }

        // Thêm màn hình Cài đặt với chức năng Đăng xuất
        composable(Destinations.SETTINGS) {
            val scope = rememberCoroutineScope()
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    scope.launch {
                        googleSignInClient.signOut().await()
                        FirebaseAuth.getInstance().signOut()
                        LoginManager.getInstance().logOut()
                        navController.navigate("login") {
                            popUpTo(Destinations.HOME) { inclusive = true }
                        }
                    }
                }) {
                    Text("Đăng xuất")
                }
            }
        }

        // Placeholder cho các màn hình khác
        composable(Destinations.INCOME) { Text("Khoản thu Screen") }
        composable(Destinations.EXPENSE) { Text("Khoản chi Screen") }
        composable(Destinations.STATISTICS) { Text("Thống kê Screen") }
        composable(Destinations.LIMIT) { Text("Limit Screen") }
        composable(Destinations.TRANSACTIONS) { Text("Transactions Screen") }
    }
}
