package com.example.gradia.data.repository

import com.example.gradia.data.local.dao.NotaDao
import com.example.gradia.data.local.entity.Nota
import kotlinx.coroutines.flow.Flow

class NotaRepository(private val notaDao: NotaDao) {

    fun getNotasByAsignatura(asignaturaId: Long): Flow<List<Nota>> =
        notaDao.getNotasByAsignatura(asignaturaId)

    fun getNotaById(id: Long): Flow<Nota?> =
        notaDao.getNotaById(id)

    suspend fun getNotaByIdSync(id: Long): Nota? =
        notaDao.getNotaByIdSync(id)

    suspend fun insertNota(nota: Nota): Long =
        notaDao.insertNota(nota)

    suspend fun updateNota(nota: Nota) =
        notaDao.updateNota(nota)

    suspend fun deleteNota(nota: Nota) =
        notaDao.deleteNota(nota)

    suspend fun deleteNotaById(id: Long) =
        notaDao.deleteNotaById(id)

    suspend fun deleteNotasByAsignatura(asignaturaId: Long) =
        notaDao.deleteNotasByAsignatura(asignaturaId)

    suspend fun getPromedioAsignatura(asignaturaId: Long): Float =
        notaDao.getPromedioAsignatura(asignaturaId) ?: 0f

    suspend fun getPorcentajeTotal(asignaturaId: Long): Float =
        notaDao.getPorcentajeTotal(asignaturaId) ?: 0f
}