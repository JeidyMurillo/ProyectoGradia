package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Category
import com.example.gradia.domain.repository.NoteRepository

class CreateCategoryUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(category: Category): Long {
        return repository.createCategory(category)
    }
}
