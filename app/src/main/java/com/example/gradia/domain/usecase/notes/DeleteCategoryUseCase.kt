package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.repository.NoteRepository

class DeleteCategoryUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deleteCategory(id)
    }
}
