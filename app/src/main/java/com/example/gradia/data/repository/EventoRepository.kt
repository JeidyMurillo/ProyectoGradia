package com.example.gradia.data.repository

import com.example.gradia.data.local.dao.EventoDao
import com.example.gradia.data.local.entity.Evento
import kotlinx.coroutines.flow.Flow

class EventoRepository(private val eventoDao: EventoDao) {

    fun getEventosByUser(userId: String): Flow<List<Evento>> =
        eventoDao.getEventosByUser(userId)

    fun getEventosByUserAndDateRange(userId: String, fechaInicio: Long, fechaFin: Long): Flow<List<Evento>> =
        eventoDao.getEventosByUserAndDateRange(userId, fechaInicio, fechaFin)

    fun getEventosPendientes(userId: String, fecha: Long): Flow<List<Evento>> =
        eventoDao.getEventosPendientes(userId, fecha)

    fun getEventoById(id: Long): Flow<Evento?> =
        eventoDao.getEventoById(id)

    suspend fun getEventoByIdSync(id: Long): Evento? =
        eventoDao.getEventoByIdSync(id)

    fun getEventosByAsignatura(asignaturaId: Long): Flow<List<Evento>> =
        eventoDao.getEventosByAsignatura(asignaturaId)

    fun getEventosByTipo(tipo: String, userId: String): Flow<List<Evento>> =
        eventoDao.getEventosByTipo(tipo, userId)

    suspend fun insertEvento(evento: Evento): Long =
        eventoDao.insertEvento(evento)

    suspend fun updateEvento(evento: Evento) =
        eventoDao.updateEvento(evento)

    suspend fun deleteEvento(evento: Evento) =
        eventoDao.deleteEvento(evento)

    suspend fun deleteEventoById(id: Long) =
        eventoDao.deleteEventoById(id)

    suspend fun updateEstadoCompletado(id: Long, completado: Boolean) =
        eventoDao.updateEstadoCompletado(id, completado)

    suspend fun deleteAllEventosByUser(userId: String) =
        eventoDao.deleteAllEventosByUser(userId)
}