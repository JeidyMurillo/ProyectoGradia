package com.example.gradia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gradia.ui.HomeScreen
import com.example.gradia.ui.LoginScreen
import com.example.gradia.ui.SingUpScreen
import com.example.gradia.ui.WelcomeScreen
import com.example.gradia.ui.theme.GradiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.WHITE
        window.navigationBarColor = android.graphics.Color.WHITE
        
        installSplashScreen()
        
        enableEdgeToEdge()
        
        setContent {
            GradiaTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "welcome",
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
                        LoginScreen(
                            onBackClick = { navController.popBackStack() },
                            onRegisterClick = { navController.navigate("register") },
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("register") {
                        SingUpScreen(
                            onBackClick = { navController.popBackStack() },
                            onLoginClick = { navController.navigate("login") }
                        )
                    }
                    composable("home") {
                        HomeScreen(
                            onLogout = {
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
