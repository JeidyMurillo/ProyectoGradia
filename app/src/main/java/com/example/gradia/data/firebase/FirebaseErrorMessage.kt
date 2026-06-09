package com.example.gradia.data.firebase

import android.content.Context
import android.util.Log
import com.example.gradia.R
import com.google.firebase.auth.FirebaseAuthException

private const val TAG = "FirebaseError"

fun getFirebaseErrorMessage(context: Context, exception: Throwable): String {
    Log.e(TAG, "Auth error [${exception::class.simpleName}]: ${exception.message}")

    if (exception is FirebaseAuthException) {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> context.getString(R.string.firebase_invalid_email)
            "ERROR_WRONG_PASSWORD" -> context.getString(R.string.firebase_wrong_password)
            "ERROR_USER_NOT_FOUND" -> context.getString(R.string.firebase_user_not_found)
            "ERROR_USER_DISABLED" -> context.getString(R.string.firebase_user_disabled)
            "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.firebase_too_many_requests)
            "ERROR_EMAIL_ALREADY_IN_USE" -> context.getString(R.string.firebase_email_in_use)
            "ERROR_WEAK_PASSWORD" -> context.getString(R.string.firebase_weak_password)
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                context.getString(R.string.firebase_account_exists)
            "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                context.getString(R.string.firebase_credential_in_use)
            "ERROR_PROVIDER_ALREADY_LINKED" ->
                context.getString(R.string.firebase_provider_linked)
            "ERROR_REQUIRES_RECENT_LOGIN" ->
                context.getString(R.string.firebase_requires_recent_login)
            "ERROR_OPERATION_NOT_ALLOWED" ->
                context.getString(R.string.firebase_operation_not_allowed)
            "ERROR_INVALID_CREDENTIAL" ->
                context.getString(R.string.firebase_invalid_credential)
            "ERROR_NETWORK_REQUEST_FAILED" ->
                context.getString(R.string.firebase_network_error)
            "ERROR_USER_TOKEN_EXPIRED" ->
                context.getString(R.string.firebase_token_expired)
            else -> resolveByMessage(context, exception.message ?: "")
        }
    }

    return resolveByMessage(context, exception.message ?: "")
}

private fun resolveByMessage(context: Context, msg: String): String {
    return when {
        msg.contains("ERROR_INVALID_EMAIL") || msg.contains("badly formatted") ->
            context.getString(R.string.firebase_invalid_email)
        msg.contains("ERROR_WRONG_PASSWORD") || msg.contains("password is invalid") || msg.contains("password is incorrect") ->
            context.getString(R.string.firebase_wrong_password)
        msg.contains("ERROR_USER_NOT_FOUND") || msg.contains("no user record") || msg.contains("Usuario no encontrado") ->
            context.getString(R.string.firebase_user_not_found)
        msg.contains("ERROR_USER_DISABLED") ->
            context.getString(R.string.firebase_user_disabled)
        msg.contains("ERROR_TOO_MANY_REQUESTS") ->
            context.getString(R.string.firebase_too_many_requests)
        msg.contains("ERROR_EMAIL_ALREADY_IN_USE") || msg.contains("already in use") ->
            context.getString(R.string.firebase_email_in_use)
        msg.contains("ERROR_WEAK_PASSWORD") || msg.contains("at least 6") ->
            context.getString(R.string.firebase_weak_password)
        msg.contains("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") ->
            context.getString(R.string.firebase_account_exists)
        msg.contains("network error") || msg.contains("Network") || msg.contains("CONNECTION_FAILURE") ->
            context.getString(R.string.firebase_network_error)
        msg.contains("sendPasswordResetEmail") && msg.contains("user") ->
            context.getString(R.string.firebase_user_not_found)
        msg.contains("User cancelled") || msg.contains("canceled") ->
            context.getString(R.string.firebase_login_cancelled)
        else -> context.getString(R.string.firebase_generic_error)
    }
}
