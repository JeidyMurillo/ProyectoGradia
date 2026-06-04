package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.data.repository.UserRepository
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
    // Promedio actual por asignatura (id -> promedio), para mostrar su barra de
    // progreso en la tarjeta de Home.
    val averages: Map<Long, Double> = emptyMap(),
    val generalAverage: Double = 0.0,
    val hasGrades: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val calculateCurrentAverage: CalculateCurrentAverageUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        userRepository.getCurrentUser()
            .flatMapLatest { user ->
                val currentSemester = user?.semestre?.toIntOrNull()
                subjectRepository.getSubjects()
                    .flatMapLatest { subjects ->
                        val filtered = if (currentSemester != null)
                            subjects.filter { it.semester == currentSemester }
                        else subjects

                        if (filtered.isEmpty()) {
                            flowOf(HomeUiState())
                        } else {
                            combine(
                                filtered.map { subject ->
                                    subjectRepository.getGradeItemsBySubject(subject.id)
                                        .map { grades -> subject to grades }
                                }
                            ) { pairs ->
                                buildState(filtered, pairs.toList())
                            }
                        }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun buildState(
        subjects: List<Subject>,
        perSubject: List<Pair<Subject, List<GradeItem>>>
    ): HomeUiState {
        val averages = perSubject.associate { (subject, grades) ->
            subject.id to calculateCurrentAverage(grades)
        }
        val contributions = perSubject.mapNotNull { (subject, grades) ->
            val average = averages[subject.id] ?: 0.0
            if (average > 0.0) subject to average else null
        }

        if (contributions.isEmpty()) {
            return HomeUiState(subjects = subjects, averages = averages, generalAverage = 0.0, hasGrades = false)
        }

        val totalCredits = contributions.sumOf { it.first.creditHours }
        val general = if (totalCredits > 0) {
            contributions.sumOf { it.second * it.first.creditHours } / totalCredits
        } else {
            contributions.sumOf { it.second } / contributions.size
        }

        return HomeUiState(
            subjects = subjects,
            averages = averages,
            generalAverage = kotlin.math.round(general * 10) / 10,
            hasGrades = true
        )
    }
}
