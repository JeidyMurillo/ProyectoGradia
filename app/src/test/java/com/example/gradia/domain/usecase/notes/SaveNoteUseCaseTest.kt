package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveNoteUseCaseTest {

    private lateinit var repository: FakeNoteRepository
    private lateinit var useCase: SaveNoteUseCase

    @Before
    fun setUp() {
        repository = FakeNoteRepository()
        useCase = SaveNoteUseCase(repository)
    }

    @Test
    fun `saves note and returns generated id`() = runTest {
        val note = Note(userId = "user1", title = "Mi nota", content = "Contenido de prueba")

        val id = useCase(note)

        assertTrue(id > 0)
    }

    @Test
    fun `saved note is retrievable`() = runTest {
        val note = Note(userId = "user1", title = "Nota guardada", content = "Texto")
        val id = useCase(note)

        val notes = repository.getNotes("user1").first()

        assertEquals(1, notes.size)
        assertEquals("Nota guardada", notes.first().title)
        assertEquals(id, notes.first().id)
    }
}
