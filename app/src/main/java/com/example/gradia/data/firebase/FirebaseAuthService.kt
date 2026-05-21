package com.example.gradia.data.firebase

import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {

    private val auth = Firebase.auth

    val currentUser = callbackFlow {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<com.google.firebase.auth.FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Usuario no encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<com.google.firebase.auth.FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                user.updateProfile(profileUpdates).await()
                Result.success(user)
            } ?: Result.failure(Exception("Error al crear usuario"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<com.google.firebase.auth.FirebaseUser> {
        return try {
            val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Error al iniciar sesión con Google"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isEmailRegistered(email: String): Boolean {
        return try {
            val methods = auth.fetchSignInMethodsForEmail(email).await()
            methods.signInMethods?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getLinkedProviders(): List<String> {
        return auth.currentUser?.providerData?.map { it.providerId }?.distinct() ?: emptyList()
    }

    suspend fun linkWithEmail(email: String, password: String): Result<Unit> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            auth.currentUser?.linkWithCredential(credential)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun linkWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.currentUser?.linkWithCredential(credential)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlinkProvider(providerId: String): Result<Unit> {
        return try {
            auth.currentUser?.unlink(providerId)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
