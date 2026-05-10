package com.example.gradia.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "nota_categoria",
    primaryKeys = ["notaId", "categoriaId"],
    foreignKeys = [
        ForeignKey(
            entity = NotaContenidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["notaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NotaCategoriaCrossRef(
    val notaId: Long,
    val categoriaId: Long
)
