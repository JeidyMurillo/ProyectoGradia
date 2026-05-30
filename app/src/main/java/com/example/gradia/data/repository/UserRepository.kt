package com.example.gradia.data.repository

import com.example.gradia.data.firebase.FirebaseAuthService
import com.example.gradia.data.local.dao.UserDao
import com.example.gradia.data.local.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UserRepository(
    private val userDao: UserDao,
    private val authService: FirebaseAuthService
) {

    /**
     * Devuelve el usuario actualmente autenticado en Firebase.
     *
     * Se resuelve siempre por el uid de la sesión activa, nunca por el primer
     * registro de la tabla local: la tabla `users` puede contener varios usuarios
     * (los datos son locales y no se borran al cerrar sesión), así que filtrar por
     * el uid es lo que garantiza que cada usuario vea únicamente sus propios datos.
     */
    fun getCurrentUser(): Flow<User?> {
        val uid = authService.getCurrentUserId() ?: return flowOf(null)
        return userDao.getUserById(uid)
    }

    fun getUserById(userId: String): Flow<User?> = userDao.getUserById(userId)

    suspend fun insertUser(user: User) = userDao.insertUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun deleteUser(user: User) = userDao.deleteUser(user)

    suspend fun deleteAllUsers() = userDao.deleteAllUsers()
}