package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateCategoryUseCaseTest {

    private lateinit var repository: FakeNoteRepository
    private lateinit var useCase: UpdateCategoryUseCase

    @Before
    fun setUp() {
        repository = FakeNoteRepository()
        useCase = UpdateCategoryUseCase(repository)
    }

    @Test
    fun `updates category name`() = runTest {
        val id = repository.createCategory(Category(name = "Original", userId = "user1"))

        useCase(Category(id = id, name = "Modificado", userId = "user1"))

        val categories = repository.getCategories("user1").first()
        assertEquals("Modificado", categories.first().name)
    }

    @Test
    fun `updates category color`() = runTest {
        val id = repository.createCategory(Category(name = "Tareas", color = 0xFFFF0000, userId = "user1"))

        useCase(Category(id = id, name = "Tareas", color = 0xFF00FF00, userId = "user1"))

        val categories = repository.getCategories("user1").first()
        assertEquals(0xFF00FF00.toInt(), categories.first().color.toInt())
    }
}
