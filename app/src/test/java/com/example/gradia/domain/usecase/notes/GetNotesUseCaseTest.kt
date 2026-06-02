package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Category
import com.example.gradia.domain.model.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetNotesUseCaseTest {

    private lateinit var repository: FakeNoteRepository
    private lateinit var useCase: GetNotesUseCase

    @Before
    fun setUp() {
        repository = FakeNoteRepository()
        useCase = GetNotesUseCase(repository)
    }

    @Test
    fun `returns all notes when no category filter`() = runTest {
        repository.saveNote(Note(userId = "user1", title = "Nota 1", content = "Contenido 1"))
        repository.saveNote(Note(userId = "user1", title = "Nota 2", content = "Contenido 2"))

        val result = useCase("user1").first()

        assertEquals(2, result.size)
    }

    @Test
    fun `returns empty list when user has no notes`() = runTest {
        repository.saveNote(Note(userId = "user2", title = "Otra nota", content = "Contenido"))

        val result = useCase("user1").first()

        assertEquals(0, result.size)
    }

    @Test
    fun `filters notes by categories`() = runTest {
        val cat = Category(id = 1, name = "Tareas", userId = "user1")
        repository.createCategory(cat)

        repository.saveNote(
            Note(userId = "user1", title = "Con categoria", content = "X", categories = listOf(cat))
        )
        repository.saveNote(
            Note(userId = "user1", title = "Sin categoria", content = "Y")
        )

        val result = useCase("user1", listOf(1)).first()

        assertEquals(1, result.size)
        assertEquals("Con categoria", result.first().title)
    }
}
