package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.repository.NoteRepository

class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long, userId: String) {
        repository.deleteNote(id, userId)
    }
}
