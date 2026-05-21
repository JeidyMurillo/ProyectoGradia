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

    suspend fun linkWithGoogle(idToken: String): Result<Unit> {
        return authService.linkWithGoogle(idToken)
    }

    suspend fun unlinkProvider(providerId: String): Result<Unit> {
        return authService.unlinkProvider(providerId)
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
