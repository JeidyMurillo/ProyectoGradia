package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import com.example.gradia.domain.usecase.CalculateCurrentAverageUseCase
import com.example.gradia.domain.usecase.CalculateRemainingPercentageUseCase
import com.example.gradia.domain.usecase.CalculateRequiredGradeUseCase
import com.example.gradia.domain.usecase.RequiredGradeResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FinalGradeUiState(
    val subjects: List<Subject> = emptyList(),
    val selectedSubject: Subject? = null,
    val activities: List<GradeItem> = emptyList(),
    val currentAverage: Double = 0.0,
    val remainingPercentage: Double = 0.0,
    val targetGrade: Double = 3.0,
    val requiredGradeResult: RequiredGradeResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class FinalGradeViewModel(
    private val subjectRepository: SubjectRepository,
    private val calculateCurrentAverage: CalculateCurrentAverageUseCase,
    private val calculateRemainingPercentage: CalculateRemainingPercentageUseCase,
    private val calculateRequiredGrade: CalculateRequiredGradeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinalGradeUiState())
    val uiState: StateFlow<FinalGradeUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        subjectRepository.getSubjects()
            .onEach { subjects ->
                _uiState.update { it.copy(subjects = subjects) }
                if (subjects.isNotEmpty() && _uiState.value.selectedSubject == null) {
                    selectSubject(subjects.first())
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectSubject(subject: Subject) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedSubject = subject) }

            subjectRepository.getGradeItemsBySubject(subject.id)
                .onEach { activities ->
                    _uiState.update {
                        it.copy(
                            activities = activities,
                            isLoading = false,
                            requiredGradeResult = null
                        )
                    }
                    calculateDerivedValues()
                }
                .launchIn(viewModelScope)
        }
    }

    fun updateTargetGrade(targetGrade: Double) {
        if (targetGrade !in 0.0..5.0) {
            _uiState.update { it.copy(error = "La nota debe estar entre 0.0 y 5.0") }
            return
        }

        _uiState.update {
            it.copy(targetGrade = targetGrade, error = null, requiredGradeResult = null)
        }
    }

    fun calculateRequiredGrade() {
        val state = _uiState.value

        if (state.selectedSubject == null) {
            _uiState.update { it.copy(error = "Selecciona una materia primero") }
            return
        }

        if (state.activities.isEmpty()) {
            _uiState.update { it.copy(error = "No hay actividades registradas") }
            return
        }

        val result = calculateRequiredGrade(state.activities, state.targetGrade)
        _uiState.update {
            it.copy(requiredGradeResult = result, error = null)
        }
    }

    private fun calculateDerivedValues() {
        val state = _uiState.value
        if (state.activities.isEmpty()) return

        val currentAverage = calculateCurrentAverage(state.activities)
        val remainingPercentage = calculateRemainingPercentage(state.activities)

        _uiState.update {
            it.copy(
                currentAverage = currentAverage,
                remainingPercentage = remainingPercentage
            )
        }
    }
}
