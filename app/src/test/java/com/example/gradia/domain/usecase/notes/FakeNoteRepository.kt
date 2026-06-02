package com.example.gradia.domain.usecase.notes

import com.example.gradia.domain.model.Category
import com.example.gradia.domain.model.Note
import com.example.gradia.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNoteRepository : NoteRepository {

    private val notes = mutableListOf<Note>()
    private val categories = mutableListOf<Category>()
    private val _notesFlow = MutableStateFlow<List<Note>>(emptyList())
    private val _categoriesFlow = MutableStateFlow<List<Category>>(emptyList())
    private var nextNoteId = 1L
    private var nextCategoryId = 1L

    override fun getNotes(userId: String): Flow<List<Note>> {
        return _notesFlow.map { list -> list.filter { it.userId == userId } }
    }

    override fun getNoteById(id: Long, userId: String): Flow<Note?> {
        return _notesFlow.map { list -> list.find { it.id == id && it.userId == userId } }
    }

    override suspend fun saveNote(note: Note): Long {
        val id = if (note.id == 0L) nextNoteId++ else note.id
        val saved = note.copy(id = id)
        notes.removeAll { it.id == id }
        notes.add(saved)
        emitNotes()
        return id
    }

    override suspend fun deleteNote(id: Long, userId: String) {
        notes.removeAll { it.id == id && it.userId == userId }
        emitNotes()
    }

    override fun getCategories(userId: String): Flow<List<Category>> {
        return _categoriesFlow.map { list -> list.filter { it.userId == userId } }
    }

    override suspend fun createCategory(category: Category): Long {
        val id = if (category.id == 0L) nextCategoryId++ else category.id
        val saved = category.copy(id = id)
        categories.add(saved)
        emitCategories()
        return id
    }

    override suspend fun updateCategory(category: Category) {
        val index = categories.indexOfFirst { it.id == category.id }
        if (index >= 0) {
            categories[index] = category
            emitCategories()
        }
    }

    override suspend fun deleteCategory(id: Long, userId: String) {
        categories.removeAll { it.id == id && it.userId == userId }
        emitCategories()
    }

    override fun getNotesByCategories(categoryIds: List<Long>, userId: String): Flow<List<Note>> {
        return _notesFlow.map { list ->
            list.filter { note ->
                note.userId == userId && note.categories.any { it.id in categoryIds }
            }
        }
    }

    private fun emitNotes() {
        _notesFlow.value = notes.toList()
    }

    private fun emitCategories() {
        _categoriesFlow.value = categories.toList()
    }
}
