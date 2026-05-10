package com.example.gradia.domain.repository

import com.example.gradia.domain.model.Category
import com.example.gradia.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    fun getNoteById(id: Long): Flow<Note?>
    suspend fun saveNote(note: Note): Long
    suspend fun deleteNote(id: Long)
    fun getCategories(): Flow<List<Category>>
    suspend fun createCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: Long)
    fun getNotesByCategories(categoryIds: List<Long>): Flow<List<Note>>
}
