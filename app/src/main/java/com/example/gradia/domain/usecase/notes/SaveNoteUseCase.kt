package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Note
import com.example.gradia.domain.repository.NoteRepository

class SaveNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note): Long {
        return repository.saveNote(note)
    }
}
