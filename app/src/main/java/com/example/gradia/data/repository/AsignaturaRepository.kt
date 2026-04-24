package com.example.gradia.data.repository

import com.example.gradia.data.local.dao.AsignaturaDao
import com.example.gradia.data.local.entity.Asignatura
import kotlinx.coroutines.flow.Flow

class AsignaturaRepository(private val asignaturaDao: AsignaturaDao) {

    fun getAsignaturasByUser(userId: String): Flow<List<Asignatura>> =
        asignaturaDao.getAsignaturasByUser(userId)

    fun getAsignaturaById(id: Long): Flow<Asignatura?> =
        asignaturaDao.getAsignaturaById(id)

    suspend fun getAsignaturaByIdSync(id: Long): Asignatura? =
        asignaturaDao.getAsignaturaByIdSync(id)

    suspend fun insertAsignatura(asignatura: Asignatura): Long =
        asignaturaDao.insertAsignatura(asignatura)

    suspend fun updateAsignatura(asignatura: Asignatura) =
        asignaturaDao.updateAsignatura(asignatura)

    suspend fun deleteAsignatura(asignatura: Asignatura) =
        asignaturaDao.deleteAsignatura(asignatura)

    suspend fun deleteAsignaturaById(id: Long) =
        asignaturaDao.deleteAsignaturaById(id)

    suspend fun getCantidadAsignaturas(userId: String): Int =
        asignaturaDao.getCantidadAsignaturas(userId)
}