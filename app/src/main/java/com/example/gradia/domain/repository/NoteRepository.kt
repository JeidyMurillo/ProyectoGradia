package com.example.gradia.domain.repository

import com.example.gradia.domain.model.Category
import com.example.gradia.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(userId: String): Flow<List<Note>>
    fun getNoteById(id: Long, userId: String): Flow<Note?>
    suspend fun saveNote(note: Note): Long
    suspend fun deleteNote(id: Long, userId: String)
    fun getCategories(userId: String): Flow<List<Category>>
    suspend fun createCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: Long, userId: String)
    fun getNotesByCategories(categoryIds: List<Long>, userId: String): Flow<List<Note>>
}
