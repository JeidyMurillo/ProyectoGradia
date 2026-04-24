package com.example.gradia.data.repository

import com.example.gradia.data.local.dao.UserDao
import com.example.gradia.data.local.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getCurrentUser(): Flow<User?> = userDao.getCurrentUser()

    fun getUserById(userId: String): Flow<User?> = userDao.getUserById(userId)

    suspend fun insertUser(user: User) = userDao.insertUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun deleteUser(user: User) = userDao.deleteUser(user)

    suspend fun deleteAllUsers() = userDao.deleteAllUsers()
}