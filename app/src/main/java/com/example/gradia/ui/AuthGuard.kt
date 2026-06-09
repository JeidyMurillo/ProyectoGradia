package com.example.gradia.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.gradia.GradiaApplication
import com.example.gradia.data.repository.AuthRepository

enum class AuthStatus {
    CHECKING, AUTHENTICATED, UNAUTHENTICATED
}

@Composable
fun AuthGuard(
    authRepository: AuthRepository,
    onUnauthenticated: () -> Unit,
    content: @Composable () -> Unit
) {
    var status by remember { mutableStateOf(AuthStatus.CHECKING) }

    LaunchedEffect(Unit) {
        status = if (authRepository.isUserLoggedIn()) {
            AuthStatus.AUTHENTICATED
        } else {
            AuthStatus.UNAUTHENTICATED
        }
    }

    when (status) {
        AuthStatus.CHECKING -> { }
        AuthStatus.AUTHENTICATED -> content()
        AuthStatus.UNAUTHENTICATED -> {
            LaunchedEffect(Unit) {
                onUnauthenticated()
            }
        }
    }
}

fun requireAuth(
    authRepository: AuthRepository,
    onResult: (Boolean) -> Unit
) {
    onResult(authRepository.isUserLoggedIn())
}
