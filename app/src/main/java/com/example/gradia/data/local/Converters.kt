package com.example.gradia.data.local

class Converters {
    @androidx.room.TypeConverter
    fun fromTipoEvento(tipo: String): String = tipo

    @androidx.room.TypeConverter
    fun toTipoEvento(value: String): String = value
}