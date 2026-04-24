package com.example.gradia

import android.app.Application
import com.example.gradia.data.local.AppDatabase
import com.example.gradia.data.repository.AsignaturaRepository
import com.example.gradia.data.repository.EventoRepository
import com.example.gradia.data.repository.NotaRepository
import com.example.gradia.data.repository.UserRepository

class GradiaApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val userRepository by lazy { UserRepository(database.userDao()) }
    val asignaturaRepository by lazy { AsignaturaRepository(database.asignaturaDao()) }
    val notaRepository by lazy { NotaRepository(database.notaDao()) }
    val eventoRepository by lazy { EventoRepository(database.eventoDao()) }
}