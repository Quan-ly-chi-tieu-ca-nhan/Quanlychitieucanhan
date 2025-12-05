@file:Suppress("DEPRECATION")
package navigation

import android.app.Activity
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

// add theme imports
import com.example.personalexpensemanagementapplication.ui.theme.ThemeStore

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    // Log nav route changes for debugging
    val navBackStackEntryForLogging by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntryForLogging) {
        val route = navBackStackEntryForLogging?.destination?.route
        Log.d("AppNavigation", "Current route changed -> $route")
    }
    val loginState by loginViewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope() // <--- KHAI BÁO SCOPE Ở ĐÂY

    // detect emulator
    val isEmulator = remember {
        val fingerprint = Build.FINGERPRINT ?: ""
        val model = Build.MODEL ?: ""
        val product = Build.PRODUCT ?: ""
        fingerprint.startsWith("generic") || fingerprint.startsWith("unknown") || model.contains("Emulator") || model.contains("Android SDK built for") || product == "google_sdk"
    }

    // --- AUTHENTICATION HANDLERS (GIỮ NGUYÊN) ---
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Updated Google launcher: navigate directly on success and update ViewModel
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                coroutineScope.launch {
                    var accountEmail: String? = null
                    val signInResult = try {
                        val account = GoogleSignIn.getSignedInAccountFromIntent(intent).await()
                        accountEmail = account?.email
                        Log.d("AppNavigation", "GoogleSignIn returned account: email=${account?.email} id=${account?.id} idToken=${account?.idToken}")
                        val idToken = account.idToken
                        if (idToken == null) {
                            Log.w("AppNavigation", "GoogleSignIn returned no idToken; possible misconfiguration in Firebase OAuth client or SHA fingerprint")
                            throw Exception("Không lấy được idToken từ Google Sign-In. Kiểm tra config OAuth client / SHA-1 trong Firebase console.")
                        }
                        val credential = GoogleAuthProvider.getCredential(idToken, null)
                        val firebaseResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                        var user = firebaseResult.user
                        if (user == null) {
                            user = FirebaseAuth.getInstance().currentUser
                            Log.w("AppNavigation", "signInWithCredential returned null user; fallback to currentUser=${user}")
                        }
                        SignInResult(
                            data = user?.let { UserData(userId = it.uid, username = it.displayName, profilePictureUrl = it.photoUrl?.toString()) },
                            errorMessage = null
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("AppNavigation", "Google Sign-In failure", e)

                        // attempt to extract the selected account's email even if idToken or sign-in failed
                        try {
                            val acct = GoogleSignIn.getSignedInAccountFromIntent(intent).await()
                            accountEmail = acct?.email
                        } catch (inner: Exception) {
                            Log.w("AppNavigation", "Couldn't extract account email after failure: ${inner.message}")
                        }

                        SignInResult(data = null, errorMessage = e.message ?: "Lỗi không xác định khi đăng nhập Google")
                    }

                    // Update ViewModel with result
                    loginViewModel.onSignInResult(signInResult)

                    // If success, navigate directly
                    if (signInResult.data != null) {
                        Log.d("AppNavigation", "Google Sign-In success: user=${signInResult.data}")
                        Toast.makeText(context, "Đăng nhập bằng Google thành công", Toast.LENGTH_SHORT).show()
                        navController.navigate(Destinations.HOME) { popUpTo("login") { inclusive = true } }
                        loginViewModel.resetState()
                        return@launch
                    }

                    // If sign-in failed but we have an email, check if that email exists with password provider in Firebase
                    if (accountEmail != null) {
                        try {
                            val fetch = FirebaseAuth.getInstance().fetchSignInMethodsForEmail(accountEmail).await()
                            val methods = fetch.signInMethods ?: emptyList()
                            Log.d("AppNavigation", "fetchSignInMethods for $accountEmail => ${methods}")

                            if (methods.contains("password")) {
                                // The user registered with email/password: prefill email on login screen and navigate there
                                loginViewModel.setPrefilledEmail(accountEmail)
                                Toast.makeText(context, "Tài khoản $accountEmail tồn tại; vui lòng nhập mật khẩu để đăng nhập", Toast.LENGTH_LONG).show()
                                navController.navigate("login") { popUpTo("login") { inclusive = true } }
                                return@launch
                            } else if (methods.contains("google.com")) {
                                // The account uses google provider but sign-in failed; inform user
                                Toast.makeText(context, "Tài khoản Google tồn tại nhưng đăng nhập thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show()
                            } else {
                                // Other providers or no providers
                                Toast.makeText(context, "Tài khoản chưa được đăng ký bằng email liên kết. Vui lòng đăng ký hoặc thử phương thức khác.", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Không thể kiểm tra thông tin tài khoản: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Đăng nhập Google thất bại: ${signInResult.errorMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            } ?: run {
                Log.w("AppNavigation", "Google Sign-In intent data was null")
                Toast.makeText(context, "Không nhận được dữ liệu từ Google Sign-In", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w("AppNavigation", "Google Sign-In result code not OK: ${result.resultCode}")
            Toast.makeText(context, "Google Sign-In bị huỷ hoặc không thành công", Toast.LENGTH_SHORT).show()
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
    // Keep LaunchedEffect for manual sign-in flows too (email/password), but for Google we will optionally bypass
    LaunchedEffect(loginState.isSignInSuccessful) {
        if (loginState.isSignInSuccessful) {
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_LONG).show()
            navController.navigate(Destinations.HOME) { popUpTo("login") { inclusive = true } }
            loginViewModel.resetState()
        }
    }

    // Show sign-in errors as Toast to help user debug when social login fails
    LaunchedEffect(loginState.signInError) {
        loginState.signInError?.let { err ->
            Toast.makeText(context, "Lỗi đăng nhập: $err", Toast.LENGTH_LONG).show()
            // Optionally reset error after showing to avoid repeated toasts
            loginViewModel.resetState()
        }
    }

    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) Destinations.HOME else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        // --- LUỒNG XÁC THỰC ---
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Destinations.HOME) { popUpTo("login") { inclusive = true } } },
                onGoogleLogin = {
                    // Bypass Google sign-in on emulator to make development/testing easier
                    if (isEmulator) {
                        Toast.makeText(context, "Emulator: bypassing Google Sign-In, navigating to Home", Toast.LENGTH_SHORT).show()
                        navController.navigate(Destinations.HOME) { popUpTo("login") { inclusive = true } }
                    } else {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                onFacebookLogin = { facebookLauncher.launch(listOf("email", "public_profile")) },
                onRegisterClick = { navController.navigate("register") },
                onForgotPasswordClick = { navController.navigate("forgot_password") },
                prefilledEmail = loginState.prefilledEmail,
                onPrefilledConsumed = { loginViewModel.clearPrefilledEmail() },
                onNavigateToAdmin = { navController.navigate(Destinations.ADMIN_LOGIN) } // Navigation to admin login
            )
        }
        // Admin login route
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
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Destinations.SETTINGS

            // Use the actual SettingScreen composable and wire theme store (use AppTheme/ThemeStore)
            com.example.personalexpensemanagementapplication.ui.screen.SettingScreen(
                currentTheme = ThemeStore.currentTheme,
                onThemeChange = { ThemeStore.currentTheme = it },
                onLogout = {
                    scope.launch {
                        try {
                            googleSignInClient.signOut().await()
                        } catch (_: Exception) {
                            // ignore signOut failure
                        }
                        FirebaseAuth.getInstance().signOut()
                        LoginManager.getInstance().logOut()

                        navController.navigate("login") {
                            popUpTo(Destinations.HOME) { inclusive = true }
                        }
                    }
                },
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() },
                currentRoute = currentRoute
            )
        }

        // Placeholder cho các màn hình khác
        composable(Destinations.INCOME) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Destinations.INCOME
            // TransactionScreen used for adding income
            com.example.personalexpensemanagementapplication.ui.screen.TransactionScreen(
                isExpense = false,
                onNavigate = { route -> navController.navigate(route) },
                currentRoute = currentRoute
            )
        }

        composable(Destinations.EXPENSE) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Destinations.EXPENSE
            // TransactionScreen used for adding expense
            com.example.personalexpensemanagementapplication.ui.screen.TransactionScreen(
                isExpense = true,
                onNavigate = { route -> navController.navigate(route) },
                currentRoute = currentRoute
            )
        }

        composable(Destinations.STATISTICS) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Destinations.STATISTICS
            com.example.personalexpensemanagementapplication.ui.screen.StatisticsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) },
                currentRoute = currentRoute
            )
        }

        composable(Destinations.LIMIT) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Destinations.LIMIT
            com.example.personalexpensemanagementapplication.ui.screen.LimitScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) },
                currentRoute = currentRoute
            )
        }

        composable(Destinations.TRANSACTIONS) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Destinations.TRANSACTIONS
            com.example.personalexpensemanagementapplication.ui.screen.TransactionListScreen(
                onNavigate = { route -> navController.navigate(route) },
                currentRoute = currentRoute
            )
        }
    }
}
