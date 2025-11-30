package navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val loginState by loginViewModel.state.collectAsState()

    // --- GOOGLE SIGN-IN HANDLER ---
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    CoroutineScope(Dispatchers.Main).launch {
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
    )

    // --- FACEBOOK LOGIN HANDLER ---
    val facebookCallbackManager = remember { CallbackManager.Factory.create() }
    LoginManager.getInstance().registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult) {
            val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    val userData = firebaseUser?.let { UserData(userId = it.uid, username = it.displayName, profilePictureUrl = it.photoUrl?.toString()) }
                    loginViewModel.onSignInResult(SignInResult(data = userData, errorMessage = null))
                } else {
                    loginViewModel.onSignInResult(SignInResult(data = null, errorMessage = task.exception?.message))
                }
            }
        }
        override fun onCancel() { /* ... */ }
        override fun onError(error: FacebookException) { /* ... */ }
    })
    val facebookLauncher = rememberLauncherForActivityResult(
        contract = LoginManager.getInstance().createLogInActivityResultContract(facebookCallbackManager, null),
        onResult = { /* Handled by callback */ }
    )

    // --- NAVIGATION ---
    LaunchedEffect(loginState.isSignInSuccessful) {
        if (loginState.isSignInSuccessful) {
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_LONG).show()
            navController.navigate("home") { popUpTo("login") { inclusive = true } }
            loginViewModel.resetState()
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                onGoogleLogin = { googleLauncher.launch(googleSignInClient.signInIntent) },
                onFacebookLogin = { facebookLauncher.launch(listOf("email", "public_profile")) },
                onRegisterClick = { navController.navigate("register") },
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("login") }, // Go back to login on success
                onBackToLogin = { navController.popBackStack() } // Go back one screen
            )
        }
        composable("forgot_password") {
            var isLoading by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }

            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onSendResetEmailClick = { email ->
                    if (email.isNotBlank()) {
                        isLoading = true
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Đã gửi email khôi phục, vui lòng kiểm tra hộp thư của bạn.", Toast.LENGTH_LONG).show()
                                    navController.popBackStack()
                                } else {
                                    error = task.exception?.localizedMessage ?: "Gửi email thất bại."
                                }
                            }
                    } else {
                        error = "Vui lòng nhập địa chỉ email."
                    }
                },
                isLoading = isLoading,
                error = error
            )
        }
        composable("home") {
            HomeScreen(
                onLogout = {
                    CoroutineScope(Dispatchers.Main).launch {
                        googleSignInClient.signOut().await()
                        FirebaseAuth.getInstance().signOut()
                        LoginManager.getInstance().logOut()
                        loginViewModel.resetState()
                        Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") { popUpTo("home") { inclusive = true } }
                    }
                }
            )
        }
    }
}
