package com.example.gradia.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notas",
    foreignKeys = [
        ForeignKey(
            entity = Asignatura::class,
            parentColumns = ["id"],
            childColumns = ["asignaturaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("asignaturaId")]
)
data class Nota(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val asignaturaId: Long,
    val nombre: String,
    val valor: Float,
    val porcentaje: Float,
    val fechaCreacion: Long = System.currentTimeMillis()
)