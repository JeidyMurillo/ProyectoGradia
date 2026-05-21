package com.example.gradia.data.firebase

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.tasks.await

object GoogleSignInUtil {

    private var _client: GoogleSignInClient? = null

    fun init(activity: Activity, webClientId: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        _client = GoogleSignIn.getClient(activity, gso)
    }

    fun getClient(): GoogleSignInClient {
        return _client ?: throw IllegalStateException("GoogleSignInUtil not initialized. Call init() first.")
    }

    fun getSignInIntent(): Intent {
        return getClient().signInIntent
    }

    fun getSignedInAccountFromIntent(data: Intent?) =
        GoogleSignIn.getSignedInAccountFromIntent(data)

    suspend fun signOut() {
        getClient().signOut().await()
    }
}
