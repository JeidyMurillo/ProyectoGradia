package com.example.gradia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asignaturas")
data class Asignatura(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val nombre: String,
    val icono: String = "📚",
    val profesor: String = "",
    val salon: String = "",
    val creditos: Int = 0,
    val notaAprobacion: Float = 3.0f,
    val fechaCreacion: Long = System.currentTimeMillis()
)