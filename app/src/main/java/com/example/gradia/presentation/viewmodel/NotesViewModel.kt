package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.domain.model.Category
import com.example.gradia.domain.model.Note
import com.example.gradia.domain.usecase.notes.CreateCategoryUseCase
import com.example.gradia.domain.usecase.notes.DeleteCategoryUseCase
import com.example.gradia.domain.usecase.notes.DeleteNoteUseCase
import com.example.gradia.domain.usecase.notes.GetCategoriesUseCase
import com.example.gradia.domain.usecase.notes.GetNotesUseCase
import com.example.gradia.domain.usecase.notes.SaveNoteUseCase
import com.example.gradia.domain.usecase.notes.UpdateCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesUiState(
    val savedNotes: List<Note> = emptyList(),
    val allCategories: List<Category> = emptyList(),
    val selectedCategoryIds: Set<Long> = emptySet(),
    val currentTitle: String = "",
    val currentContent: String = "",
    val currentColor: Long = 0xFFFFE0E0,
    val noteCategories: List<Category> = emptyList(),
    val editingNoteId: Long = 0,
    val isSaving: Boolean = false,
    val error: String? = null,
    val selectedNoteIds: Set<Long> = emptySet()
)

class NotesViewModel(
    private val userId: String,
    private val getNotesUseCase: GetNotesUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _selectedCategoryIds = MutableStateFlow<Set<Long>>(emptySet())

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        getCategoriesUseCase(userId)
            .onEach { categories ->
                _uiState.update { it.copy(allCategories = categories) }
            }
            .launchIn(viewModelScope)

        combine(
            getNotesUseCase(userId),
            _selectedCategoryIds
        ) { allNotes, selectedIds ->
            val filtered = if (selectedIds.isEmpty()) allNotes
            else allNotes.filter { note ->
                note.categories.any { it.id in selectedIds }
            }
            filtered to selectedIds
        }.onEach { (notes, selectedIds) ->
            _uiState.update {
                it.copy(
                    savedNotes = notes,
                    selectedCategoryIds = selectedIds
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(currentTitle = title) }
    }

    fun onContentChange(content: String) {
        _uiState.update { it.copy(currentContent = content) }
    }

    fun onColorChange(color: Long) {
        _uiState.update { it.copy(currentColor = color) }
    }

    fun toggleCategorySelection(categoryId: Long) {
        _selectedCategoryIds.update { current ->
            if (categoryId in current) current - categoryId
            else current + categoryId
        }
    }

    fun toggleNoteCategory(categoryId: Long) {
        _uiState.update { state ->
            val current = state.noteCategories.toMutableList()
            val category = state.allCategories.find { it.id == categoryId } ?: return@update state
            if (current.any { it.id == categoryId }) {
                state.copy(noteCategories = current.filter { it.id != categoryId })
            } else {
                current.add(category)
                state.copy(noteCategories = current)
            }
        }
    }

    fun saveNote() {
        val state = _uiState.value
        if (state.currentTitle.isBlank() && state.currentContent.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                saveNoteUseCase(
                    Note(
                        id = state.editingNoteId,
                        userId = userId,
                        title = state.currentTitle,
                        content = state.currentContent,
                        color = state.currentColor,
                        categories = state.noteCategories
                    )
                )
                _uiState.update {
                    it.copy(
                        currentTitle = "",
                        currentContent = "",
                        currentColor = 0xFFFFE0E0,
                        noteCategories = emptyList(),
                        editingNoteId = 0,
                        isSaving = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            deleteNoteUseCase(id, userId)
        }
    }

    fun loadNoteForEditing(note: Note) {
        _uiState.update {
            it.copy(
                currentTitle = note.title,
                currentContent = note.content,
                currentColor = note.color,
                noteCategories = note.categories,
                editingNoteId = note.id
            )
        }
    }

    fun createCategory(name: String, color: Long) {
        viewModelScope.launch {
            try {
                createCategoryUseCase(Category(userId = userId, name = name, color = color))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateCategory(id: Long, name: String, color: Long) {
        viewModelScope.launch {
            try {
                updateCategoryUseCase(Category(id = id, userId = userId, name = name, color = color))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            try {
                deleteCategoryUseCase(id, userId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleNoteSelection(id: Long) {
        _uiState.update { state ->
            val current = state.selectedNoteIds
            if (id in current) state.copy(selectedNoteIds = current - id)
            else state.copy(selectedNoteIds = current + id)
        }
    }

    fun clearNoteSelection() {
        _uiState.update { it.copy(selectedNoteIds = emptySet()) }
    }

    fun deleteSelectedNotes() {
        val ids = _uiState.value.selectedNoteIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { id -> deleteNoteUseCase(id, userId) }
            _uiState.update { it.copy(selectedNoteIds = emptySet()) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
