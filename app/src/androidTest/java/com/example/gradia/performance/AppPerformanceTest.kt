package com.example.gradia.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gradia.data.local.AppDatabase
import com.example.gradia.data.local.entity.Asignatura
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppPerformanceTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = AppDatabase.getDatabase(context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insert50Subjects_under2seconds() {
        val dao = database.asignaturaDao()
        val userId = "perf_test_user"

        val startTime = System.currentTimeMillis()

        runBlocking {
            for (i in 1..50) {
                dao.insertAsignatura(
                    Asignatura(
                        nombre = "Asignatura $i",
                        creditos = (i % 6) + 1,
                        userId = userId
                    )
                )
            }
        }

        val elapsed = System.currentTimeMillis() - startTime
        assertTrue(
            "Insertar 50 asignaturas tomó ${elapsed}ms (límite: 2000ms)",
            elapsed < 2000
        )
    }

    @Test
    fun query50Subjects_under1second() {
        val dao = database.asignaturaDao()
        val userId = "perf_query_user"

        runBlocking {
            for (i in 1..50) {
                dao.insertAsignatura(
                    Asignatura(nombre = "Subject $i", creditos = 3, userId = userId)
                )
            }
        }

        val startTime = System.currentTimeMillis()

        runBlocking {
            val subjects = dao.getAsignaturasByUser(userId)
            subjects.collect { list ->
                assertTrue("Deberían haber 50 asignaturas", list.size == 50)
                return@collect
            }
        }

        val elapsed = System.currentTimeMillis() - startTime
        assertTrue(
            "Consultar 50 asignaturas tomó ${elapsed}ms (límite: 1000ms)",
            elapsed < 1000
        )
    }
}
