package com.example.gradia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val email: String,
    val fotoUrl: String? = null,
    val metaPromedio: Float = 3.5f
)