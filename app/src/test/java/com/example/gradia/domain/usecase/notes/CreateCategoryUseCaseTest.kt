package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateCategoryUseCaseTest {

    private lateinit var repository: FakeNoteRepository
    private lateinit var useCase: CreateCategoryUseCase

    @Before
    fun setUp() {
        repository = FakeNoteRepository()
        useCase = CreateCategoryUseCase(repository)
    }

    @Test
    fun `creates category and returns id`() = runTest {
        val category = Category(name = "Tareas", color = 0xFFFF0000)

        val id = useCase(category)

        assertTrue(id > 0)
    }

    @Test
    fun `created category appears in getCategories`() = runTest {
        val category = Category(name = "Examenes", color = 0xFF00FF00, userId = "user1")
        useCase(category)

        val categories = repository.getCategories("user1").first()

        assertEquals(1, categories.size)
        assertEquals("Examenes", categories.first().name)
    }
}
