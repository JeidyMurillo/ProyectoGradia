package com.example.gradia.data.firebase

fun getFirebaseErrorMessage(exception: Throwable): String {
    val msg = exception.message ?: ""
    return when {
        msg.contains("ERROR_INVALID_EMAIL") || msg.contains("badly formatted") ->
            "El correo electrónico no es válido"
        msg.contains("ERROR_WRONG_PASSWORD") || msg.contains("password is invalid") || msg.contains("password is incorrect") ->
            "Contraseña incorrecta"
        msg.contains("ERROR_USER_NOT_FOUND") || msg.contains("no user record") ->
            "No existe una cuenta con este correo"
        msg.contains("ERROR_USER_DISABLED") ->
            "La cuenta ha sido deshabilitada"
        msg.contains("ERROR_TOO_MANY_REQUESTS") ->
            "Demasiados intentos, intenta más tarde"
        msg.contains("ERROR_EMAIL_ALREADY_IN_USE") || msg.contains("already in use") ->
            "Este correo ya está registrado"
        msg.contains("ERROR_WEAK_PASSWORD") || msg.contains("at least 6") ->
            "La contraseña debe tener al menos 6 caracteres"
        msg.contains("network error") || msg.contains("Network") ->
            "Error de conexión, revisa tu internet"
        msg.contains("Usuario no encontrado") ->
            "No existe una cuenta con este correo"
        msg.contains("sendPasswordResetEmail") && msg.contains("user") ->
            "No existe una cuenta con este correo"
        else -> "Ocurrió un error, intenta de nuevo"
    }
}
