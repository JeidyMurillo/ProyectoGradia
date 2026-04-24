package com.example.gradia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gradia.data.local.entity.Nota
import kotlinx.coroutines.flow.Flow

@Dao
interface NotaDao {
    @Query("SELECT * FROM notas WHERE asignaturaId = :asignaturaId ORDER BY fechaCreacion DESC")
    fun getNotasByAsignatura(asignaturaId: Long): Flow<List<Nota>>

    @Query("SELECT * FROM notas WHERE id = :id")
    fun getNotaById(id: Long): Flow<Nota?>

    @Query("SELECT * FROM notas WHERE id = :id")
    suspend fun getNotaByIdSync(id: Long): Nota?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNota(nota: Nota): Long

    @Update
    suspend fun updateNota(nota: Nota)

    @Delete
    suspend fun deleteNota(nota: Nota)

    @Query("DELETE FROM notas WHERE id = :id")
    suspend fun deleteNotaById(id: Long)

    @Query("DELETE FROM notas WHERE asignaturaId = :asignaturaId")
    suspend fun deleteNotasByAsignatura(asignaturaId: Long)

    @Query("SELECT SUM(valor * porcentaje / 100) FROM notas WHERE asignaturaId = :asignaturaId")
    suspend fun getPromedioAsignatura(asignaturaId: Long): Float?

    @Query("SELECT SUM(porcentaje) FROM notas WHERE asignaturaId = :asignaturaId")
    suspend fun getPorcentajeTotal(asignaturaId: Long): Float?
}