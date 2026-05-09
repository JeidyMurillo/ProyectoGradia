package com.example.gradia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gradia.data.local.entity.NotaCategoriaCrossRef

@Dao
interface NotaCategoriaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(crossRef: NotaCategoriaCrossRef)

    @Query("DELETE FROM nota_categoria WHERE notaId = :notaId")
    suspend fun deleteByNotaId(notaId: Long)

    @Query("DELETE FROM nota_categoria WHERE notaId = :notaId AND categoriaId = :categoriaId")
    suspend fun delete(notaId: Long, categoriaId: Long)
}
