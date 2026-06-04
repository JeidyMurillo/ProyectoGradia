package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import com.example.gradia.domain.usecase.CalculateCurrentAverageUseCase
import com.example.gradia.domain.validation.SubjectValidation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SubjectFilter { TODAS, ACTUAL, ANTIGUAS }

enum class SubjectSort { NOMBRE, SEMESTRE, CREDITOS, PROMEDIO }

data class SubjectsUiState(
    val subjects: List<Subject> = emptyList(),
    val allSubjectNames: List<String> = emptyList(),
    val filter: SubjectFilter = SubjectFilter.TODAS,
    val sort: SubjectSort = SubjectSort.NOMBRE,
    val currentSemester: Int = 1,
    val isSaving: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class SubjectsViewModel(
    private val subjectRepository: SubjectRepository,
    private val calculateCurrentAverage: CalculateCurrentAverageUseCase
) : ViewModel() {

    private val allSubjects: StateFlow<List<Subject>> = subjectRepository.getSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Promedio actual por asignatura (id -> promedio). Carga las notas de cada
    // asignatura para poder ordenar por promedio.
    private val averagesBySubject: StateFlow<Map<Long, Double>> =
        allSubjects.flatMapLatest { subjects ->
            if (subjects.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(
                    subjects.map { subject ->
                        subjectRepository.getGradeItemsBySubject(subject.id)
                            .map { grades -> subject.id to calculateCurrentAverage(grades) }
                    }
                ) { pairs -> pairs.toMap() }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val filter = MutableStateFlow(SubjectFilter.TODAS)
    private val sort = MutableStateFlow(SubjectSort.NOMBRE)
    private val isSaving = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    // Se pre-combina subjects + promedios para no exceder el límite de 5 flujos
    // del combine principal.
    private val subjectsWithAverages =
        combine(allSubjects, averagesBySubject) { subjects, averages -> subjects to averages }

    val uiState: StateFlow<SubjectsUiState> =
        combine(subjectsWithAverages, filter, sort, isSaving, error) { (subjects, averages), currentFilter, currentSort, saving, err ->
            val currentSemester = subjects.maxOfOrNull { it.semester } ?: 1
            val filtered = when (currentFilter) {
                SubjectFilter.TODAS -> subjects
                SubjectFilter.ACTUAL -> subjects.filter { it.semester == currentSemester }
                SubjectFilter.ANTIGUAS -> subjects.filter { it.semester < currentSemester }
            }
            val visible = when (currentSort) {
                SubjectSort.NOMBRE -> filtered.sortedBy { it.name.lowercase() }
                SubjectSort.SEMESTRE -> filtered.sortedWith(
                    compareBy<Subject> { it.semester }.thenBy { it.name.lowercase() }
                )
                SubjectSort.CREDITOS -> filtered.sortedWith(
                    compareByDescending<Subject> { it.creditHours }.thenBy { it.name.lowercase() }
                )
                SubjectSort.PROMEDIO -> filtered.sortedWith(
                    compareByDescending<Subject> { averages[it.id] ?: 0.0 }.thenBy { it.name.lowercase() }
                )
            }
            SubjectsUiState(
                subjects = visible,
                allSubjectNames = subjects.map { it.name },
                filter = currentFilter,
                sort = currentSort,
                currentSemester = currentSemester,
                isSaving = saving,
                error = err
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubjectsUiState())

    fun onFilterChange(value: SubjectFilter) {
        filter.value = value
    }

    fun onSortChange(value: SubjectSort) {
        sort.value = value
    }

    fun addSubject(subject: Subject, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isSaving.value = true
            error.value = null
            try {
                SubjectValidation.validate(subject, allSubjects.value)
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
