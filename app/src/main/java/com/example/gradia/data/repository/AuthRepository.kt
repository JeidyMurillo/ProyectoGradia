package com.example.gradia.data.repository

import android.util.Log
import com.example.gradia.data.email.EmailService
import com.example.gradia.data.firebase.FirebaseAuthService
import com.example.gradia.data.local.entity.User
import com.example.gradia.util.ErrorHandler
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthRepository(
    private val authService: FirebaseAuthService,
    private val userRepository: UserRepository,
    private val emailService: EmailService? = null,
    private val scope: CoroutineScope? = null
) {
    val currentUser = authService.currentUser

    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = authService.signInWithEmail(email, password)
            result.fold(
                onSuccess = { firebaseUser ->
                    val user = saveUserToLocalDb(firebaseUser)
                    Result.success(user)
                },
                onFailure = {
                    val message = ErrorHandler.handle("AuthRepository.signInWithEmail", it)
                    Result.failure(Exception(message))
                }
            )
        } catch (e: Exception) {
            val message = ErrorHandler.handle("AuthRepository.signInWithEmail", e)
            Result.failure(Exception(message))
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<User> {
        return try {
            val result = authService.signUpWithEmail(email, password, name)
            result.fold(
                onSuccess = { firebaseUser ->
                    val user = saveUserToLocalDb(firebaseUser)
                    emailService?.let { service ->
                        val emailScope = scope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO)
                        emailScope.launch {
                            val result = service.sendWelcomeEmail(user.nombre, user.email)
                            result.fold(
                                onSuccess = { Log.d("AuthRepository", "Email de bienvenida enviado a ${user.email}") },
                                onFailure = { e -> Log.e("AuthRepository", "Error al enviar email de bienvenida a ${user.email}", e) }
                            )
                        }
                    }
                    Result.success(user)
                },
                onFailure = {
                    val message = ErrorHandler.handle("AuthRepository.signUpWithEmail", it)
                    Result.failure(Exception(message))
                }
            )
        } catch (e: Exception) {
            val message = ErrorHandler.handle("AuthRepository.signUpWithEmail", e)
            Result.failure(Exception(message))
        }
    }

    suspend fun signInWithFacebook(token: String): Result<Pair<User, Boolean>> {
        return try {
            val result = authService.signInWithFacebook(token)
            result.fold(
                onSuccess = { (firebaseUser, isNewUser) ->
                    val user = saveUserToLocalDb(firebaseUser)
                    Result.success(Pair(user, isNewUser))
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Pair<User, Boolean>> {
        return try {
            val result = authService.signInWithGoogle(idToken)
            result.fold(
                onSuccess = { (firebaseUser, isNewUser) ->
                    val user = saveUserToLocalDb(firebaseUser)
                    Result.success(Pair(user, isNewUser))
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(clearLocalData: suspend () -> Unit): Result<Unit> {
        val result = authService.deleteAccount()
        result.fold(
            onSuccess = {
                clearLocalData()
            },
            onFailure = { }
        )
        return result
    }

    fun signOut() {
        authService.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return authService.isUserLoggedIn()
    }

    fun getCurrentUserId(): String? {
        return authService.getCurrentUserId()
    }

    fun getLinkedProviders(): List<String> {
        return authService.getLinkedProviders()
    }

    suspend fun linkWithEmail(email: String, password: String): Result<Unit> {
        return authService.linkWithEmail(email, password)
    }

    suspend fun linkWithFacebook(token: String): Result<Unit> {
        return authService.linkWithFacebook(token)
    }

    suspend fun linkWithGoogle(idToken: String): Result<Unit> {
        return authService.linkWithGoogle(idToken)
    }

    suspend fun unlinkProvider(providerId: String): Result<Unit> {
        return authService.unlinkProvider(providerId)
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return authService.updatePassword(newPassword)
    }

    suspend fun reauthenticate(email: String, password: String): Result<Unit> {
        return authService.reauthenticate(email, password)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authService.sendPasswordResetEmail(email)
    }

    suspend fun getLocalUser(userId: String): User? {
        return userRepository.getUserById(userId).first()
    }

    private suspend fun saveUserToLocalDb(firebaseUser: FirebaseUser): User {
        val user = User(
            id = firebaseUser.uid,
            nombre = firebaseUser.displayName ?: "Usuario",
            email = firebaseUser.email ?: "",
            fotoUrl = firebaseUser.photoUrl?.toString()
        )

        val existingUser = userRepository.getUserById(user.id).first()
        if (existingUser == null) {
            userRepository.insertUser(user)
        }

        return user
    }
}
