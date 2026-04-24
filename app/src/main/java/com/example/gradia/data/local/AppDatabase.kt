package com.example.gradia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gradia.data.local.dao.AsignaturaDao
import com.example.gradia.data.local.dao.EventoDao
import com.example.gradia.data.local.dao.NotaDao
import com.example.gradia.data.local.dao.UserDao
import com.example.gradia.data.local.entity.Asignatura
import com.example.gradia.data.local.entity.Evento
import com.example.gradia.data.local.entity.Nota
import com.example.gradia.data.local.entity.User

@Database(
    entities = [User::class, Asignatura::class, Nota::class, Evento::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun asignaturaDao(): AsignaturaDao
    abstract fun notaDao(): NotaDao
    abstract fun eventoDao(): EventoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gradia_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}