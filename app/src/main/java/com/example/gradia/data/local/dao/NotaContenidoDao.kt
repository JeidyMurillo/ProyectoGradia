package com.example.gradia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gradia.data.local.entity.NotaConCategorias
import com.example.gradia.data.local.entity.NotaContenidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotaContenidoDao {
    @Transaction
    @Query("SELECT * FROM notas_contenido ORDER BY updatedAt DESC")
    fun getAllNotasConCategorias(): Flow<List<NotaConCategorias>>

    @Transaction
    @Query("SELECT * FROM notas_contenido WHERE id = :id")
    fun getNotaConCategoriasById(id: Long): Flow<NotaConCategorias?>

    @Query("SELECT * FROM notas_contenido ORDER BY updatedAt DESC")
    fun getAllNotas(): Flow<List<NotaContenidoEntity>>

    @Query("SELECT * FROM notas_contenido WHERE id = :id")
    fun getNotaById(id: Long): Flow<NotaContenidoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNota(nota: NotaContenidoEntity): Long

    @Update
    suspend fun updateNota(nota: NotaContenidoEntity)

    @Delete
    suspend fun deleteNota(nota: NotaContenidoEntity)

    @Query("DELETE FROM notas_contenido WHERE id = :id")
    suspend fun deleteNotaById(id: Long)

    @Transaction
    @Query("SELECT nc.* FROM notas_contenido nc " +
           "INNER JOIN nota_categoria ncc ON nc.id = ncc.notaId " +
           "WHERE ncc.categoriaId IN (:categoriaIds) " +
           "ORDER BY nc.updatedAt DESC")
    fun getNotasByCategorias(categoriaIds: List<Long>): Flow<List<NotaContenidoEntity>>
}
