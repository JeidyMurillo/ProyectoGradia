package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Category
import com.example.gradia.domain.repository.NoteRepository

class UpdateCategoryUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(category: Category) {
        repository.updateCategory(category)
    }
}
