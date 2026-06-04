package com.example.gradia.util

import android.util.Log

object ErrorHandler {

    private const val TAG = "GradiaError"

    fun handle(origin: String, throwable: Throwable, userMessage: String? = null): String {
        Log.e(TAG, "[$origin] ${throwable.message}", throwable)
        return userMessage ?: throwable.message ?: "Ha ocurrido un error inesperado."
    }

    fun getUserMessage(throwable: Throwable): String {
        return when {
            throwable.message?.contains("No hay usuario activo") == true ->
                "Debes iniciar sesión para realizar esta operación."
            throwable.message?.contains("Firebase") == true ->
                "Error de conexión con el servidor. Verifica tu conexión a internet."
            throwable.message?.contains("Network") == true ->
                "Error de red. Verifica tu conexión a internet."
            throwable.message?.contains("Permission") == true ->
                "No tienes permiso para realizar esta acción."
            throwable.message?.contains("already exists") == true ->
                "Este elemento ya existe."
            throwable.message?.contains("not found") == true ->
                "El elemento solicitado no fue encontrado."
            else -> "Ha ocurrido un error inesperado. Intenta de nuevo."
        }
    }

    fun isAuthError(throwable: Throwable): Boolean {
        return throwable.message?.contains("No hay usuario activo") == true
    }
}
