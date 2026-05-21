package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Category
import com.example.gradia.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase(private val repository: NoteRepository) {
    operator fun invoke(userId: String): Flow<List<Category>> {
        return repository.getCategories(userId)
    }
}
