package com.example.gradia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gradia.data.local.entity.Asignatura
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignaturaDao {
    @Query("SELECT * FROM asignaturas WHERE userId = :userId ORDER BY fechaCreacion DESC")
    fun getAsignaturasByUser(userId: String): Flow<List<Asignatura>>

    @Query("SELECT * FROM asignaturas WHERE id = :id")
    fun getAsignaturaById(id: Long): Flow<Asignatura?>

    @Query("SELECT * FROM asignaturas WHERE id = :id")
    suspend fun getAsignaturaByIdSync(id: Long): Asignatura?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsignatura(asignatura: Asignatura): Long

    @Update
    suspend fun updateAsignatura(asignatura: Asignatura)

    @Delete
    suspend fun deleteAsignatura(asignatura: Asignatura)

    @Query("DELETE FROM asignaturas WHERE id = :id")
    suspend fun deleteAsignaturaById(id: Long)

    @Query("SELECT COUNT(*) FROM asignaturas WHERE userId = :userId")
    suspend fun getCantidadAsignaturas(userId: String): Int
}