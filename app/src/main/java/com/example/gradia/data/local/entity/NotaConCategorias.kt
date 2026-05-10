package com.example.gradia.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NotaConCategorias(
    @Embedded val nota: NotaContenidoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            NotaCategoriaCrossRef::class,
            parentColumn = "notaId",
            entityColumn = "categoriaId"
        )
    )
    val categorias: List<CategoriaEntity>
)
