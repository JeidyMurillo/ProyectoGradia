package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetCategoriesUseCaseTest {

    private lateinit var repository: FakeNoteRepository
    private lateinit var useCase: GetCategoriesUseCase

    @Before
    fun setUp() {
        repository = FakeNoteRepository()
        useCase = GetCategoriesUseCase(repository)
    }

    @Test
    fun `returns empty list when no categories exist`() = runTest {
        val result = useCase("user1").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns only categories for given user`() = runTest {
        repository.createCategory(Category(name = "User1 Cat", userId = "user1"))
        repository.createCategory(Category(name = "User2 Cat", userId = "user2"))

        val result = useCase("user1").first()

        assertEquals(1, result.size)
        assertEquals("User1 Cat", result.first().name)
    }

    @Test
    fun `reflects category updates`() = runTest {
        val id = repository.createCategory(Category(name = "Original", userId = "user1"))
        repository.updateCategory(Category(id = id, name = "Actualizado", userId = "user1"))

        val result = useCase("user1").first()

        assertEquals("Actualizado", result.first().name)
    }
}
