package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import com.example.gradia.domain.validation.GradeValidation
import com.example.gradia.domain.validation.SubjectValidation
import com.example.gradia.ui.GradeIconType
import com.example.gradia.ui.resolveGradeIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GradeFilter { TODAS, PARCIALES, TALLERES }

enum class GradeSort { PREDETERMINADO, NOTA, PESO, PENDIENTES }

data class SubjectDetailUiState(
    val subject: Subject? = null,
    val grades: List<GradeItem> = emptyList(),
    val filter: GradeFilter = GradeFilter.TODAS,
    val sort: GradeSort = GradeSort.PREDETERMINADO,
    val currentAverage: Double = 0.0,
    // Suma de porcentajes de TODAS las notas (sin filtrar). El formulario lo usa
    // para mostrar cuánto queda disponible hasta el 100%.
    val totalPercentage: Double = 0.0,
    // Suma de porcentajes de las notas YA calificadas. Sirve para mostrar cuánto
    // de la materia se ha evaluado.
    val evaluatedPercentage: Double = 0.0,
    val isSaving: Boolean = false,
    val error: String? = null
)

class SubjectDetailViewModel(
    private val subjectId: Long,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val subjectFlow = subjectRepository.getSubjectById(subjectId)
    private val gradesFlow = subjectRepository.getGradeItemsBySubject(subjectId)

    /** Nombres de las demás asignaturas (excluye la actual) para avisar de
     *  duplicados en línea al editar, sin marcar la propia como repetida. */
    val otherSubjectNames: StateFlow<List<String>> =
        subjectRepository.getSubjects()
            .map { list -> list.filter { it.id != subjectId }.map { s -> s.name } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val filter = MutableStateFlow(GradeFilter.TODAS)
    private val sort = MutableStateFlow(GradeSort.PREDETERMINADO)
    private val isSaving = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    // Se pre-combinan filtro + orden para no exceder el límite de 5 flujos del
    // combine principal.
    private val filterAndSort = combine(filter, sort) { f, s -> f to s }

    val uiState: StateFlow<SubjectDetailUiState> =
        combine(subjectFlow, gradesFlow, filterAndSort, isSaving, error) { subject, grades, (currentFilter, currentSort), saving, err ->
            SubjectDetailUiState(
                subject = subject,
                grades = applySort(applyFilter(grades, currentFilter), currentSort),
                filter = currentFilter,
                sort = currentSort,
                currentAverage = computeAverage(grades),
                totalPercentage = grades.sumOf { it.percentage },
                evaluatedPercentage = grades.filter { it.grade != null }.sumOf { it.percentage },
                isSaving = saving,
                error = err
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubjectDetailUiState())

    fun onGradeSortChange(value: GradeSort) {
        sort.value = value
    }

    fun onFilterChange(value: GradeFilter) {
        filter.value = value
    }

    fun addGrade(grade: GradeItem, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isSaving.value = true
            error.value = null
            try {
                val target = grade.copy(subjectId = subjectId)
                GradeValidation.validate(target, gradesFlow.first())
                subjectRepository.insertGradeItem(target)
                onSuccess()
            } catch (e: Exception) {
                error.value = e.message ?: "Error al guardar la nota"
            } finally {
                isSaving.value = false
            }
        }
    }

    fun updateGrade(grade: GradeItem, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isSaving.value = true
            error.value = null
            try {
                val target = grade.copy(subjectId = subjectId)
                GradeValidation.validate(target, gradesFlow.first())
                subjectRepository.updateGradeItem(target)
                onSuccess()
            } catch (e: Exception) {
                error.value = e.message ?: "Error al actualizar la nota"
            } finally {
                isSaving.value = false
            }
        }
    }

    fun deleteGrade(grade: GradeItem, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            error.value = null
            try {
                subjectRepository.deleteGradeItem(grade)
                onSuccess()
            } catch (e: Exception) {
                error.value = e.message ?: "Error al eliminar la nota"
            }
        }
    }

    fun updateSubject(subject: Subject, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isSaving.value = true
            error.value = null
            try {
                val target = subject.copy(id = subjectId)
                // Snapshot puntual de las asignaturas del usuario para detectar
                // nombres duplicados (validate excluye la propia por id).
                SubjectValidation.validate(target, subjectRepository.getSubjects().first())
                subjectRepository.updateSubject(target)
                onSuccess()
            } catch (e: Exception) {
                error.value = e.message ?: "Error al actualizar la asignatura"
            } finally {
                isSaving.value = false
            }
        }
    }

    fun deleteSubject(onSuccess: () -> Unit = {}) {
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

    // El filtro se basa en el tipo (icono) que el usuario eligió explícitamente al
    // crear la nota; si no hay icono guardado, resolveGradeIcon lo deduce a partir
    // del nombre. Esto evita que las notas se "pierdan" del filtro cuando el nombre
    // no contiene una palabra clave concreta.
    private fun applyFilter(grades: List<GradeItem>, value: GradeFilter): List<GradeItem> =
        when (value) {
            GradeFilter.TODAS -> grades
            GradeFilter.PARCIALES -> grades.filter {
                val type = resolveGradeIcon(it.icon, it.name)
                type == GradeIconType.Examen || type == GradeIconType.Final
            }
            GradeFilter.TALLERES -> grades.filter {
                resolveGradeIcon(it.icon, it.name) == GradeIconType.Actividad
            }
        }

    private fun applySort(grades: List<GradeItem>, value: GradeSort): List<GradeItem> =
        when (value) {
            // Orden original (por fecha de creación, tal como llega del repositorio).
            GradeSort.PREDETERMINADO -> grades
            // Calificadas primero, de mayor a menor nota; las pendientes al final.
            GradeSort.NOTA -> grades.sortedWith(
                compareBy<GradeItem> { it.grade == null }.thenByDescending { it.grade ?: 0.0 }
            )
            GradeSort.PESO -> grades.sortedByDescending { it.percentage }
            // Pendientes (sin nota) primero, conservando el orden dentro de cada grupo.
            GradeSort.PENDIENTES -> grades.sortedBy { it.grade != null }
        }

    private fun computeAverage(grades: List<GradeItem>): Double {
        val graded = grades.filter { it.grade != null }
        if (graded.isEmpty()) return 0.0
        val totalCoursePercentage = grades.sumOf { it.percentage }
        if (totalCoursePercentage == 0.0) return 0.0
        return graded.sumOf { (it.grade ?: 0.0) * it.percentage } / totalCoursePercentage
    }
}
