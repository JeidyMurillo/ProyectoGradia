package com.example.gradia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gradia.data.local.entity.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias ORDER BY name ASC")
    fun getAllCategorias(): Flow<List<CategoriaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoria(categoria: CategoriaEntity): Long

    @Delete
    suspend fun deleteCategoria(categoria: CategoriaEntity)

    @Query("DELETE FROM categorias WHERE id = :id")
    suspend fun deleteCategoriaById(id: Long)

    @Query("UPDATE categorias SET name = :name, color = :color WHERE id = :id")
    suspend fun updateCategoria(id: Long, name: String, color: Long)
}
