package com.example.gradia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gradia.data.local.entity.Evento
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {
    @Query("SELECT * FROM eventos WHERE userId = :userId ORDER BY fecha ASC")
    fun getEventosByUser(userId: String): Flow<List<Evento>>

    @Query("SELECT * FROM eventos WHERE userId = :userId AND fecha >= :fechaInicio AND fecha <= :fechaFin ORDER BY fecha ASC")
    fun getEventosByUserAndDateRange(userId: String, fechaInicio: Long, fechaFin: Long): Flow<List<Evento>>

    @Query("SELECT * FROM eventos WHERE userId = :userId AND fecha >= :fecha AND completado = 0 ORDER BY fecha ASC")
    fun getEventosPendientes(userId: String, fecha: Long): Flow<List<Evento>>

    @Query("SELECT * FROM eventos WHERE id = :id AND userId = :userId")
    fun getEventoById(id: Long, userId: String): Flow<Evento?>

    @Query("SELECT * FROM eventos WHERE id = :id AND userId = :userId")
    suspend fun getEventoByIdSync(id: Long, userId: String): Evento?

    @Query("SELECT * FROM eventos WHERE asignaturaId = :asignaturaId AND userId = :userId ORDER BY fecha ASC")
    fun getEventosByAsignatura(asignaturaId: Long, userId: String): Flow<List<Evento>>

    @Query("SELECT * FROM eventos WHERE tipo = :tipo AND userId = :userId ORDER BY fecha ASC")
    fun getEventosByTipo(tipo: String, userId: String): Flow<List<Evento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvento(evento: Evento): Long

    @Update
    suspend fun updateEvento(evento: Evento)

    @Delete
    suspend fun deleteEvento(evento: Evento)

    @Query("DELETE FROM eventos WHERE id = :id AND userId = :userId")
    suspend fun deleteEventoById(id: Long, userId: String)

    @Query("UPDATE eventos SET completado = :completado WHERE id = :id AND userId = :userId")
    suspend fun updateEstadoCompletado(id: Long, completado: Boolean, userId: String)

    @Query("DELETE FROM eventos WHERE userId = :userId")
    suspend fun deleteAllEventosByUser(userId: String)
}