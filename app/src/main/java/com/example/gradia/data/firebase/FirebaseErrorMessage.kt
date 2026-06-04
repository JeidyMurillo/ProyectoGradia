package com.example.gradia.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuthException

private const val TAG = "FirebaseError"

fun getFirebaseErrorMessage(exception: Throwable): String {
    Log.e(TAG, "Auth error [${exception::class.simpleName}]: ${exception.message}")

    if (exception is FirebaseAuthException) {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El correo electrónico no es válido"
            "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo"
            "ERROR_USER_DISABLED" -> "La cuenta ha sido deshabilitada"
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos, intenta más tarde"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo electrónico ya está registrado"
            "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres"
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                "Ya existe una cuenta con este correo usando otro método de inicio de sesión. Inicia sesión con el método que usaste originalmente (correo/contraseña, Google o Facebook)."
            "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                "Esta credencial ya está vinculada a otra cuenta. Cierra sesión e inicia con esa cuenta."
            "ERROR_PROVIDER_ALREADY_LINKED" ->
                "Este proveedor ya está vinculado a tu cuenta."
            "ERROR_REQUIRES_RECENT_LOGIN" ->
                "Esta operación requiere que hayas iniciado sesión recientemente. Vuelve a iniciar sesión e inténtalo de nuevo."
            "ERROR_OPERATION_NOT_ALLOWED" ->
                "Esta operación no está permitida. Contacta al soporte."
            "ERROR_INVALID_CREDENTIAL" ->
                "La credencial no es válida o ha expirado. Intenta de nuevo."
            "ERROR_NETWORK_REQUEST_FAILED" ->
                "Error de conexión, revisa tu internet"
            "ERROR_USER_TOKEN_EXPIRED" ->
                "Tu sesión ha expirado. Vuelve a iniciar sesión."
            else -> resolveByMessage(exception.message ?: "")
        }
    }

    return resolveByMessage(exception.message ?: "")
}

private fun resolveByMessage(msg: String): String {
    return when {
        msg.contains("ERROR_INVALID_EMAIL") || msg.contains("badly formatted") ->
            "El correo electrónico no es válido"
        msg.contains("ERROR_WRONG_PASSWORD") || msg.contains("password is invalid") || msg.contains("password is incorrect") ->
            "Contraseña incorrecta"
        msg.contains("ERROR_USER_NOT_FOUND") || msg.contains("no user record") || msg.contains("Usuario no encontrado") ->
            "No existe una cuenta con este correo"
        msg.contains("ERROR_USER_DISABLED") ->
            "La cuenta ha sido deshabilitada"
        msg.contains("ERROR_TOO_MANY_REQUESTS") ->
            "Demasiados intentos, intenta más tarde"
        msg.contains("ERROR_EMAIL_ALREADY_IN_USE") || msg.contains("already in use") ->
            "Este correo electrónico ya está registrado"
        msg.contains("ERROR_WEAK_PASSWORD") || msg.contains("at least 6") ->
            "La contraseña debe tener al menos 6 caracteres"
        msg.contains("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") ->
            "Ya existe una cuenta con este correo usando otro método de inicio de sesión. Inicia sesión con tu método de registro original."
        msg.contains("network error") || msg.contains("Network") || msg.contains("CONNECTION_FAILURE") ->
            "Error de conexión, revisa tu internet"
        msg.contains("sendPasswordResetEmail") && msg.contains("user") ->
            "No existe una cuenta con este correo"
        msg.contains("User cancelled") || msg.contains("canceled") ->
            "Inicio de sesión cancelado"
        else -> "Ocurrió un error, intenta de nuevo"
    }
}
