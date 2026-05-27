package com.example.gradia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gradia.data.firebase.FacebookSignInUtil
import com.example.gradia.data.firebase.GoogleSignInUtil
import com.example.gradia.data.firebase.getFirebaseErrorMessage
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.example.gradia.ui.ForgotPasswordScreen
import com.example.gradia.ui.HomeScreen
import com.example.gradia.ui.LoginScreen
import com.example.gradia.ui.SingUpScreen
import com.example.gradia.ui.TermsAndConditionsScreen
import com.example.gradia.ui.WelcomeScreen
import com.example.gradia.ui.theme.GradiaTheme
import kotlinx.coroutines.launch

private const val TAG = "GradiaFacebook"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.WHITE
        window.navigationBarColor = android.graphics.Color.WHITE

        enableEdgeToEdge()

        val webClientId = getString(R.string.default_web_client_id)
        GoogleSignInUtil.init(this, webClientId)

        setContent {
            val app = remember { application as GradiaApplication }
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()

            var startDestination by remember { mutableStateOf("welcome") }
            var checkingSession by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                if (app.isRememberMeEnabled && app.authRepository.isUserLoggedIn()) {
                    val userId = app.authRepository.getCurrentUserId()
                    val localUser = if (userId != null) app.authRepository.getLocalUser(userId) else null
                    startDestination = if (localUser != null) "home" else "welcome"
                }
                checkingSession = false
            }

            GradiaTheme {
                if (checkingSession) return@GradiaTheme

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { 1000 },
                            animationSpec = tween(400)
                        ) + fadeIn(animationSpec = tween(400))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -1000 },
                            animationSpec = tween(400)
                        ) + fadeOut(animationSpec = tween(400))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -1000 },
                            animationSpec = tween(400)
                        ) + fadeIn(animationSpec = tween(400))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { 1000 },
                            animationSpec = tween(400)
                        ) + fadeOut(animationSpec = tween(400))
                    }
                ) {
                    composable("welcome") {
                        WelcomeScreen(
                            onLoginClick = { navController.navigate("login") },
                            onRegisterClick = { navController.navigate("register") }
                        )
                    }
                    composable("login") {
                        var isLoginLoading by remember { mutableStateOf(false) }
                        var loginError by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(Unit) {
                            loginError = null
                        }

                        val googleLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.StartActivityForResult()
                        ) { result ->
                            val accountResult = GoogleSignInUtil.getSignedInAccountFromIntent(result.data)
                            if (accountResult.isSuccessful) {
                                val googleAccount = accountResult.result ?: return@rememberLauncherForActivityResult
                                val idToken = googleAccount.idToken ?: return@rememberLauncherForActivityResult
                                isLoginLoading = true
                                scope.launch {
                                    app.authRepository.signInWithGoogle(idToken).fold(
                                        onSuccess = { (_, isNewUser) ->
                                            if (isNewUser) {
                                                app.authRepository.signOut()
                                                isLoginLoading = false
                                                navController.navigate("register") {
                                                    popUpTo("welcome") { inclusive = false }
                                                }
                                            } else {
                                                app.isRememberMeEnabled = true
                                                isLoginLoading = false
                                                navController.navigate("home") {
                                                    popUpTo("welcome") { inclusive = true }
                                                }
                                            }
                                        },
                                        onFailure = { e ->
                                            isLoginLoading = false
                                            loginError = getFirebaseErrorMessage(e)
                                        }
                                    )
                                }
                            } else {
                                val exception = accountResult.exception
                                loginError = "Error con Google: ${exception?.localizedMessage ?: "Error desconocido"}"
                            }
                        }

                        val onFacebookLoginResult: (String) -> Unit = { accessToken ->
                            Log.d(TAG, "Login: Facebook token received, signing in with Firebase")
                            isLoginLoading = true
                            scope.launch {
                                app.authRepository.signInWithFacebook(accessToken).fold(
                                    onSuccess = { (_, isNewUser) ->
                                        Log.d(TAG, "Login: Firebase signIn success, isNewUser=$isNewUser")
                                        if (isNewUser) {
                                            app.authRepository.signOut()
                                            isLoginLoading = false
                                            navController.navigate("register") {
                                                popUpTo("welcome") { inclusive = false }
                                            }
                                        } else {
                                            app.isRememberMeEnabled = true
                                            isLoginLoading = false
                                            navController.navigate("home") {
                                                popUpTo("welcome") { inclusive = true }
                                            }
                                        }
                                    },
                                    onFailure = { e ->
                                        Log.e(TAG, "Login: Firebase signIn failed", e)
                                        isLoginLoading = false
                                        loginError = getFirebaseErrorMessage(e)
                                    }
                                )
                            }
                        }

                        LoginScreen(
                            onBackClick = { navController.popBackStack() },
                            onRegisterClick = { navController.navigate("register") },
                            onForgotPassword = { navController.navigate("forgot_password") },
                            isLoading = isLoginLoading,
                            errorMessage = loginError,
                            onLogin = { email, password, rememberMe ->
                                loginError = null
                                isLoginLoading = true
                                scope.launch {
                                    app.authRepository.signInWithEmail(email, password).fold(
                                        onSuccess = {
                                            app.isRememberMeEnabled = rememberMe
                                            isLoginLoading = false
                                            navController.navigate("home") {
                                                popUpTo("welcome") { inclusive = true }
                                            }
                                        },
                                        onFailure = { e ->
                                            isLoginLoading = false
                                            loginError = getFirebaseErrorMessage(e)
                                        }
                                    )
                                }
                            },
                            onGoogleSignIn = {
                                loginError = null
                                googleLauncher.launch(GoogleSignInUtil.getSignInIntent())
                            },
                            onFacebookSignIn = {
                                Log.d(TAG, "Login: Facebook button clicked")
                                LoginManager.getInstance().registerCallback(FacebookSignInUtil.callbackManager, object : FacebookCallback<LoginResult> {
                                    override fun onSuccess(result: LoginResult) {
                                        Log.d(TAG, "Login: Facebook onSuccess, token=${result.accessToken.token.take(20)}...")
                                        onFacebookLoginResult(result.accessToken.token)
                                    }
                                    override fun onCancel() {
                                        Log.d(TAG, "Login: Facebook onCancel")
                                    }
                                    override fun onError(error: FacebookException) {
                                        Log.e(TAG, "Login: Facebook onError", error)
                                        loginError = error.message ?: "Error al iniciar sesión con Facebook"
                                    }
                                })
                                Log.d(TAG, "Login: Calling loginWithAccountPicker")
                                FacebookSignInUtil.loginWithAccountPicker(
                                    this@MainActivity,
                                    listOf("email", "public_profile")
                                )
                            }
                        )
                    }
                    composable("forgot_password") {
                        var isSending by remember { mutableStateOf(false) }
                        var resetError by remember { mutableStateOf<String?>(null) }
                        var resetSuccess by remember { mutableStateOf<String?>(null) }

                        ForgotPasswordScreen(
                            onBackClick = { navController.popBackStack() },
                            isLoading = isSending,
                            errorMessage = resetError,
                            successMessage = resetSuccess,
                            onSendResetEmail = { email ->
                                resetError = null
                                resetSuccess = null
                                isSending = true
                                scope.launch {
                                    app.authRepository.sendPasswordResetEmail(email).fold(
                                        onSuccess = {
                                            isSending = false
                                            resetSuccess = "Se ha enviado un enlace de recuperación a tu correo."
                                        },
                                        onFailure = { e ->
                                            isSending = false
                                            resetError = getFirebaseErrorMessage(e)
                                        }
                                    )
                                }
                            }
                        )
                    }
                    composable(
                        route = "register?fromGoogle={fromGoogle}",
                        arguments = listOf(navArgument("fromGoogle") {
                            type = NavType.BoolType
                            defaultValue = false
                        })
                    ) { backStackEntry ->
                        val fromGoogle = backStackEntry.arguments?.getBoolean("fromGoogle") ?: false

                        var isRegisterLoading by remember { mutableStateOf(false) }
                        var registerError by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(Unit) {
                            registerError = null
                        }

                        val googleLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.StartActivityForResult()
                        ) { result ->
                            val accountResult = GoogleSignInUtil.getSignedInAccountFromIntent(result.data)
                            if (accountResult.isSuccessful) {
                                val googleAccount = accountResult.result ?: return@rememberLauncherForActivityResult
                                val idToken = googleAccount.idToken ?: return@rememberLauncherForActivityResult
                                isRegisterLoading = true
                                scope.launch {
                                    app.authRepository.signInWithGoogle(idToken).fold(
                                        onSuccess = { _ ->
                                            app.isRememberMeEnabled = true
                                            isRegisterLoading = false
                                            navController.navigate("home") {
                                                popUpTo("welcome") { inclusive = true }
                                            }
                                        },
                                        onFailure = { e ->
                                            isRegisterLoading = false
                                            registerError = getFirebaseErrorMessage(e)
                                        }
                                    )
                                }
                            } else {
                                val exception = accountResult.exception
                                registerError = "Error con Google: ${exception?.localizedMessage ?: "Error desconocido"}"
                            }
                        }

                        val onFacebookRegisterResult: (String) -> Unit = { accessToken ->
                            Log.d(TAG, "Register: Facebook token received, signing in with Firebase")
                            isRegisterLoading = true
                            scope.launch {
                                app.authRepository.signInWithFacebook(accessToken).fold(
                                    onSuccess = { _ ->
                                        Log.d(TAG, "Register: Firebase signIn success")
                                        app.isRememberMeEnabled = true
                                        isRegisterLoading = false
                                        navController.navigate("home") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    },
                                    onFailure = { e ->
                                        Log.e(TAG, "Register: Firebase signIn failed", e)
                                        isRegisterLoading = false
                                        registerError = getFirebaseErrorMessage(e)
                                    }
                                )
                            }
                        }

                        SingUpScreen(
                            onBackClick = { navController.popBackStack() },
                            onLoginClick = { navController.navigate("login") },
                            onTermsClick = { navController.navigate("terms_and_conditions") },
                            isLoading = isRegisterLoading,
                            errorMessage = registerError,
                            googleMessage = if (fromGoogle) "No tienes una cuenta con Gradia. Regístrate con Google para continuar." else null,
                            onRegister = { email, password, nombre ->
                                registerError = null
                                isRegisterLoading = true
                                scope.launch {
                                    app.authRepository.signUpWithEmail(email, password, nombre).fold(
                                        onSuccess = {
                                            app.isRememberMeEnabled = true
                                            isRegisterLoading = false
                                            navController.navigate("home") {
                                                popUpTo("welcome") { inclusive = true }
                                            }
                                        },
                                        onFailure = { e ->
                                            isRegisterLoading = false
                                            registerError = getFirebaseErrorMessage(e)
                                        }
                                    )
                                }
                            },
                            onGoogleSignUp = {
                                scope.launch {
                                    GoogleSignInUtil.signOut()
                                    googleLauncher.launch(GoogleSignInUtil.getSignInIntent())
                                }
                            },
                            onFacebookSignUp = {
                                Log.d(TAG, "Register: Facebook button clicked")
                                LoginManager.getInstance().registerCallback(FacebookSignInUtil.callbackManager, object : FacebookCallback<LoginResult> {
                                    override fun onSuccess(result: LoginResult) {
                                        Log.d(TAG, "Register: Facebook onSuccess, token=${result.accessToken.token.take(20)}...")
                                        onFacebookRegisterResult(result.accessToken.token)
                                    }
                                    override fun onCancel() {
                                        Log.d(TAG, "Register: Facebook onCancel")
                                    }
                                    override fun onError(error: FacebookException) {
                                        Log.e(TAG, "Register: Facebook onError", error)
                                        registerError = error.message ?: "Error al registrarse con Facebook"
                                    }
                                })
                                Log.d(TAG, "Register: Calling loginWithAccountPicker")
                                FacebookSignInUtil.loginWithAccountPicker(
                                    this@MainActivity,
                                    listOf("email", "public_profile")
                                )
                            }
                        )
                    }
                    composable("terms_and_conditions") {
                        TermsAndConditionsScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("home") {
                        val userId = app.authRepository.getCurrentUserId()
                        val user by if (userId != null) {
                            app.userRepository.getUserById(userId).collectAsState(initial = null)
                        } else {
                            remember { mutableStateOf(null) }
                        }

                        HomeScreen(
                            userName = user?.nombre ?: "Usuario",
                            userEmail = user?.email ?: "",
                            onNavigateToTerms = { navController.navigate("terms_and_conditions") },
                            onLogout = {
                                app.isRememberMeEnabled = false
                                app.authRepository.signOut()
                                navController.navigate("welcome") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onDeleteAccount = {
                                app.isRememberMeEnabled = false
                                navController.navigate("welcome") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult called: requestCode=$requestCode resultCode=$resultCode data=$data")
        val handled = FacebookSignInUtil.callbackManager.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: callbackManager handled=$handled")
        super.onActivityResult(requestCode, resultCode, data)
    }
}
