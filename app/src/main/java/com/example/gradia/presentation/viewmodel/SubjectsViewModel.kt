package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SubjectFilter { TODAS, ACTUAL, ANTIGUAS }

data class SubjectsUiState(
    val subjects: List<Subject> = emptyList(),
    val filter: SubjectFilter = SubjectFilter.TODAS,
    val currentSemester: Int = 1,
    val isSaving: Boolean = false,
    val error: String? = null
)

class SubjectsViewModel(
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val allSubjects: StateFlow<List<Subject>> = subjectRepository.getSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val filter = MutableStateFlow(SubjectFilter.TODAS)
    private val isSaving = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SubjectsUiState> =
        combine(allSubjects, filter, isSaving, error) { subjects, current, saving, err ->
            val currentSemester = subjects.maxOfOrNull { it.semester } ?: 1
            val visible = when (current) {
                SubjectFilter.TODAS -> subjects
                SubjectFilter.ACTUAL -> subjects.filter { it.semester == currentSemester }
                SubjectFilter.ANTIGUAS -> subjects.filter { it.semester < currentSemester }
            }
            SubjectsUiState(
                subjects = visible,
                filter = current,
                currentSemester = currentSemester,
                isSaving = saving,
                error = err
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubjectsUiState())

    fun onFilterChange(value: SubjectFilter) {
        filter.value = value
    }

    fun addSubject(subject: Subject, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isSaving.value = true
            error.value = null
            try {
                subjectRepository.insertSubject(subject)
                onSuccess()
            } catch (e: Exception) {
                error.value = e.message ?: "Error al guardar la asignatura"
            } finally {
                isSaving.value = false
            }
        }
    }

    fun deleteSubject(subjectId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            error.value = null
            try {
                subjectRepository.deleteSubject(subjectId)
                onSuccess()
            } catch (e: Exception) {
                error.value = e.message ?: "Error al eliminar la asignatura"
            }
        }
    }

    fun clearError() {
        error.value = null
    }
}
