package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Note
import com.example.gradia.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(categoryIds: List<Long>? = null): Flow<List<Note>> {
        return if (categoryIds.isNullOrEmpty()) {
            repository.getNotes()
        } else {
            repository.getNotesByCategories(categoryIds)
        }
    }
}
