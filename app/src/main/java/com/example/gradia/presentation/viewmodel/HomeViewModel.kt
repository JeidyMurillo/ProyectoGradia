package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import com.example.gradia.domain.usecase.CalculateCurrentAverageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val subjects: List<Subject> = emptyList(),
    val generalAverage: Double = 0.0,
    val hasGrades: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val subjectRepository: SubjectRepository,
    private val calculateCurrentAverage: CalculateCurrentAverageUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        subjectRepository.getSubjects()
            .flatMapLatest { subjects ->
                if (subjects.isEmpty()) {
                    flowOf(HomeUiState())
                } else {
                    // Observa reactivamente las notas de cada asignatura y recalcula
                    // el promedio general cuando cualquiera de ellas cambie.
                    combine(
                        subjects.map { subject ->
                            subjectRepository.getGradeItemsBySubject(subject.id)
                                .map { grades -> subject to grades }
                        }
                    ) { pairs ->
                        buildState(subjects, pairs.toList())
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun buildState(
        subjects: List<Subject>,
        perSubject: List<Pair<Subject, List<GradeItem>>>
    ): HomeUiState {
        // Solo cuentan las asignaturas que ya tienen al menos una nota calificada.
        val contributions = perSubject.mapNotNull { (subject, grades) ->
            val average = calculateCurrentAverage(grades)
            if (average > 0.0) subject to average else null
        }

        if (contributions.isEmpty()) {
            return HomeUiState(subjects = subjects, generalAverage = 0.0, hasGrades = false)
        }

        // Promedio general ponderado por créditos; si no hay créditos definidos,
        // se usa un promedio simple entre asignaturas.
        val totalCredits = contributions.sumOf { it.first.creditHours }
        val general = if (totalCredits > 0) {
            contributions.sumOf { it.second * it.first.creditHours } / totalCredits
        } else {
            contributions.sumOf { it.second } / contributions.size
        }

        return HomeUiState(
            subjects = subjects,
            generalAverage = kotlin.math.round(general * 10) / 10,
            hasGrades = true
        )
    }
}
