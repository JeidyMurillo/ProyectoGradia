package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GradeFilter { TODAS, PARCIALES, TALLERES }

data class SubjectDetailUiState(
    val subject: Subject? = null,
    val grades: List<GradeItem> = emptyList(),
    val filter: GradeFilter = GradeFilter.TODAS,
    val currentAverage: Double = 0.0,
    val isSaving: Boolean = false,
    val error: String? = null
)

class SubjectDetailViewModel(
    private val subjectId: Long,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val subjectFlow = subjectRepository.getSubjectById(subjectId)
    private val gradesFlow = subjectRepository.getGradeItemsBySubject(subjectId)

    private val filter = MutableStateFlow(GradeFilter.TODAS)
    private val isSaving = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SubjectDetailUiState> =
        combine(subjectFlow, gradesFlow, filter, isSaving, error) { subject, grades, current, saving, err ->
            SubjectDetailUiState(
                subject = subject,
                grades = applyFilter(grades, current),
                filter = current,
                currentAverage = computeAverage(grades),
                isSaving = saving,
                error = err
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubjectDetailUiState())

    fun onFilterChange(value: GradeFilter) {
        filter.value = value
    }

    fun addGrade(grade: GradeItem, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isSaving.value = true
            error.value = null
            try {
                subjectRepository.insertGradeItem(grade.copy(subjectId = subjectId))
                onSuccess()
            } catch (e: Exception) {
                error.value = e.message ?: "Error al guardar la nota"
            } finally {
                isSaving.value = false
            }
        }
    }

    fun clearError() {
        error.value = null
    }

    private fun applyFilter(grades: List<GradeItem>, value: GradeFilter): List<GradeItem> =
        when (value) {
            GradeFilter.TODAS -> grades
            GradeFilter.PARCIALES -> grades.filter {
                it.name.contains("Parcial", ignoreCase = true) ||
                    it.name.contains("Examen", ignoreCase = true)
            }
            GradeFilter.TALLERES -> grades.filter {
                it.name.contains("Taller", ignoreCase = true) ||
                    it.name.contains("Tarea", ignoreCase = true) ||
                    it.name.contains("Quiz", ignoreCase = true) ||
                    it.name.contains("Laboratorio", ignoreCase = true) ||
                    it.name.contains("Proyecto", ignoreCase = true)
            }
        }

    private fun computeAverage(grades: List<GradeItem>): Double {
        val graded = grades.filter { it.grade != null }
        val totalWeight = graded.sumOf { it.percentage }
        if (totalWeight == 0.0) return 0.0
        return graded.sumOf { (it.grade ?: 0.0) * it.percentage } / totalWeight
    }
}
