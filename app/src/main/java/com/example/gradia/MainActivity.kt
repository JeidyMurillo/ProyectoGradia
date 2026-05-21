package com.example.gradia

import android.os.Bundle
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
import com.example.gradia.data.firebase.GoogleSignInUtil
import com.example.gradia.data.firebase.getFirebaseErrorMessage
import com.example.gradia.ui.HomeScreen
import com.example.gradia.ui.LoginScreen
import com.example.gradia.ui.SingUpScreen
import com.example.gradia.ui.TermsAndConditionsScreen
import com.example.gradia.ui.WelcomeScreen
import com.example.gradia.ui.theme.GradiaTheme
import kotlinx.coroutines.launch

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

            val startDestination = remember {
                if (app.isRememberMeEnabled && app.authRepository.isUserLoggedIn()) "home" else "welcome"
            }

            GradiaTheme {
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
                                val email = googleAccount.email ?: return@rememberLauncherForActivityResult
                                isLoginLoading = true
                                scope.launch {
                                    if (!app.authRepository.isEmailRegistered(email)) {
                                        isLoginLoading = false
                                        navController.navigate("register?fromGoogle=true") {
                                            popUpTo("welcome") { inclusive = false }
                                        }
                                    } else {
                                        app.authRepository.signInWithGoogle(idToken).fold(
                                            onSuccess = {
                                                app.isRememberMeEnabled = true
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
                                }
                            }
                        }

                        LoginScreen(
                            onBackClick = { navController.popBackStack() },
                            onRegisterClick = { navController.navigate("register") },
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
                                scope.launch {
                                    GoogleSignInUtil.signOut()
                                    googleLauncher.launch(GoogleSignInUtil.getSignInIntent())
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
                            onLogout = {
                                app.isRememberMeEnabled = false
                                app.authRepository.signOut()
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
}
