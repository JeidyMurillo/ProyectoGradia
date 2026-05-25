package com.example.gradia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gradia.data.local.dao.AsignaturaDao
import com.example.gradia.data.local.dao.CategoriaDao
import com.example.gradia.data.local.dao.EventoDao
import com.example.gradia.data.local.dao.NotaCategoriaDao
import com.example.gradia.data.local.dao.NotaContenidoDao
import com.example.gradia.data.local.dao.NotaDao
import com.example.gradia.data.local.dao.UserDao
import com.example.gradia.data.local.entity.Asignatura
import com.example.gradia.data.local.entity.CategoriaEntity
import com.example.gradia.data.local.entity.Evento
import com.example.gradia.data.local.entity.Nota
import com.example.gradia.data.local.entity.NotaCategoriaCrossRef
import com.example.gradia.data.local.entity.NotaContenidoEntity
import com.example.gradia.data.local.entity.User

@Database(
    entities = [User::class, Asignatura::class, Nota::class, Evento::class,
        NotaContenidoEntity::class, CategoriaEntity::class, NotaCategoriaCrossRef::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun asignaturaDao(): AsignaturaDao
    abstract fun notaDao(): NotaDao
    abstract fun eventoDao(): EventoDao
    abstract fun notaContenidoDao(): NotaContenidoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun notaCategoriaDao(): NotaCategoriaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gradia_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}