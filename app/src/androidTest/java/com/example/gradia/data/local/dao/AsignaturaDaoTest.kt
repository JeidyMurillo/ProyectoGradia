package com.example.gradia.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gradia.data.local.AppDatabase
import com.example.gradia.data.local.entity.Asignatura
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AsignaturaDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: AsignaturaDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.asignaturaDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveAsignatura() = runBlocking {
        val asignatura = Asignatura(
            nombre = "Matematicas",
            creditos = 4,
            userId = "user1"
        )
        val id = dao.insertAsignatura(asignatura)

        val result = dao.getAsignaturaById(id, "user1").first()
        assertNotNull(result)
        assertEquals("Matematicas", result!!.nombre)
        assertEquals(4, result.creditos)
    }

    @Test
    fun getAsignaturasByUser_returnsOnlyUserSubjects() = runBlocking {
        dao.insertAsignatura(Asignatura(nombre = "User1 Subject", creditos = 3, userId = "user1"))
        dao.insertAsignatura(Asignatura(nombre = "User2 Subject", creditos = 3, userId = "user2"))

        val user1Subjects = dao.getAsignaturasByUser("user1").first()
        val user2Subjects = dao.getAsignaturasByUser("user2").first()

        assertEquals(1, user1Subjects.size)
        assertEquals("User1 Subject", user1Subjects.first().nombre)
        assertEquals(1, user2Subjects.size)
        assertEquals("User2 Subject", user2Subjects.first().nombre)
    }

    @Test
    fun updateAsignatura_modifiesExistingRecord() = runBlocking {
        val id = dao.insertAsignatura(Asignatura(nombre = "Original", creditos = 3, userId = "user1"))

        dao.updateAsignatura(Asignatura(id = id, nombre = "Updated", creditos = 5, userId = "user1"))

        val result = dao.getAsignaturaById(id, "user1").first()
        assertNotNull(result)
        assertEquals("Updated", result!!.nombre)
        assertEquals(5, result.creditos)
    }

    @Test
    fun deleteAsignatura_removesRecord() = runBlocking {
        val id = dao.insertAsignatura(Asignatura(nombre = "Temp", creditos = 2, userId = "user1"))

        dao.deleteAsignaturaById(id, "user1")

        val result = dao.getAsignaturaById(id, "user1").first()
        assertNull(result)
    }

    @Test
    fun countAsignaturas_returnsCorrectCount() = runBlocking {
        dao.insertAsignatura(Asignatura(nombre = "A", creditos = 3, userId = "user1"))
        dao.insertAsignatura(Asignatura(nombre = "B", creditos = 3, userId = "user1"))
        dao.insertAsignatura(Asignatura(nombre = "C", creditos = 3, userId = "user2"))

        val count = dao.getCantidadAsignaturas("user1")
        assertEquals(2, count)
    }

    @Test
    fun getAsignaturaByIdSync_returnsCorrectRecord() = runBlocking {
        val id = dao.insertAsignatura(Asignatura(nombre = "Sync Test", creditos = 4, userId = "user1"))

        val result = dao.getAsignaturaByIdSync(id, "user1")
        assertNotNull(result)
        assertEquals("Sync Test", result!!.nombre)
    }

    @Test
    fun insertMultipleAsignaturas_returnsAllInOrder() = runBlocking {
        dao.insertAsignatura(Asignatura(nombre = "Primera", creditos = 3, userId = "user1"))
        Thread.sleep(10)
        dao.insertAsignatura(Asignatura(nombre = "Segunda", creditos = 4, userId = "user1"))
        Thread.sleep(10)
        dao.insertAsignatura(Asignatura(nombre = "Tercera", creditos = 5, userId = "user1"))

        val subjects = dao.getAsignaturasByUser("user1").first()
        assertEquals(3, subjects.size)
    }
}
