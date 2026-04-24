package com.example.gradia.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "eventos",
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
data class Evento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val asignaturaId: Long? = null,
    val titulo: String,
    val descripcion: String = "",
    val fecha: Long,
    val tipo: String = "TAREA",
    val completado: Boolean = false,
    val recordatorioMinutosAntes: Int = 30,
    val fechaCreacion: Long = System.currentTimeMillis()
)

object TipoEvento {
    const val TAREA = "TAREA"
    const val EVALUACION = "EVALUACION"
    const val ENTREGA = "ENTREGA"
}