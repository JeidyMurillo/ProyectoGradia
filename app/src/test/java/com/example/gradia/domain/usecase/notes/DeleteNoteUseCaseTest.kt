package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DeleteNoteUseCaseTest {

    private lateinit var repository: FakeNoteRepository
    private lateinit var useCase: DeleteNoteUseCase

    @Before
    fun setUp() {
        repository = FakeNoteRepository()
        useCase = DeleteNoteUseCase(repository)
    }

    @Test
    fun `deletes note by id and userId`() = runTest {
        val id = repository.saveNote(Note(userId = "user1", title = "Nota", content = "Texto"))
        assertEquals(1, repository.getNotes("user1").first().size)

        useCase(id, "user1")

        assertEquals(0, repository.getNotes("user1").first().size)
    }

    @Test
    fun `does not delete note of other user`() = runTest {
        repository.saveNote(Note(userId = "user1", title = "Nota user1", content = "A"))
        val id = repository.saveNote(Note(userId = "user2", title = "Nota user2", content = "B"))

        useCase(id, "user1")

        assertEquals(1, repository.getNotes("user1").first().size)
        assertEquals(1, repository.getNotes("user2").first().size)
    }
}
