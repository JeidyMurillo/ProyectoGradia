package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DeleteCategoryUseCaseTest {

    private lateinit var repository: FakeNoteRepository
    private lateinit var useCase: DeleteCategoryUseCase

    @Before
    fun setUp() {
        repository = FakeNoteRepository()
        useCase = DeleteCategoryUseCase(repository)
    }

    @Test
    fun `deletes category by id`() = runTest {
        val category = Category(name = "Proyectos", userId = "user1")
        val id = repository.createCategory(category)
        assertEquals(1, repository.getCategories("user1").first().size)

        useCase(id, "user1")

        assertEquals(0, repository.getCategories("user1").first().size)
    }

    @Test
    fun `does not delete category of other user`() = runTest {
        repository.createCategory(Category(name = "Tareas", userId = "user1"))
        val id = repository.createCategory(Category(name = "Examenes", userId = "user2"))

        useCase(id, "user1")

        assertEquals(1, repository.getCategories("user2").first().size)
        assertEquals(1, repository.getCategories("user1").first().size)
    }
}
