package com.example.gradia.data.repository

import com.example.gradia.data.firebase.FirebaseAuthService
import com.example.gradia.data.local.entity.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val authService: FirebaseAuthService,
    private val userRepository: UserRepository
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
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<User> {
        return try {
            val result = authService.signUpWithEmail(email, password, name)
            result.fold(
                onSuccess = { firebaseUser ->
                    val user = saveUserToLocalDb(firebaseUser)
                    Result.success(user)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
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
