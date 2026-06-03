package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.data.repository.UserRepository
import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import com.example.gradia.domain.usecase.CalculateCurrentAverageUseCase
import com.example.gradia.ui.SubjectPerformanceData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.math.round

data class StatsUiState(
    val generalAverage: Double = 0.0,
    val generalTrend: Double = 0.0,
    val totalCredits: Int = 0,
    val totalCreditsApproved: Int = 0,
    val excellentCount: Int = 0,
    val approvedCount: Int = 0,
    val regularCount: Int = 0,
    val failedCount: Int = 0,
    val totalSubjects: Int = 0,
    val totalGradedItems: Int = 0,
    val bestSubjectName: String = "",
    val bestSubjectAverage: Double = 0.0,
    val bestGradeName: String = "",
    val bestGradeValue: Double = 0.0,
    val subjectsPerformance: List<SubjectPerformanceData> = emptyList(),
    val hasData: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val calculateCurrentAverage: CalculateCurrentAverageUseCase
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> =
        userRepository.getCurrentUser()
            .flatMapLatest { user ->
                val currentSemester = user?.semestre?.toIntOrNull()
                subjectRepository.getSubjects()
                    .flatMapLatest { subjects ->
                        val filtered = if (currentSemester != null)
                            subjects.filter { it.semester == currentSemester }
                        else subjects

                        if (filtered.isEmpty()) {
                            flowOf(StatsUiState())
                        } else {
                            combine(
                                filtered.map { subject ->
                                    subjectRepository.getGradeItemsBySubject(subject.id)
                                        .map { grades -> subject to grades }
                                }
                            ) { pairs -> buildState(filtered, pairs.toList()) }
                        }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    private fun buildState(
        subjects: List<Subject>,
        perSubject: List<Pair<Subject, List<GradeItem>>>
    ): StatsUiState {
        val contributions = perSubject.mapNotNull { (subject, grades) ->
            val average = calculateCurrentAverage(grades)
            if (average > 0.0) Triple(subject, average, grades) else null
        }

        if (contributions.isEmpty()) return StatsUiState(totalSubjects = subjects.size)

        val totalCreditsAll = contributions.sumOf { it.first.creditHours }
        val general = if (totalCreditsAll > 0) {
            contributions.sumOf { it.second * it.first.creditHours } / totalCreditsAll
        } else {
            contributions.sumOf { it.second } / contributions.size
        }

        val allGradedItems = contributions
            .flatMap { it.third }
            .filter { it.grade != null && it.grade!! > 0 }
        val trend = if (allGradedItems.size >= 2) {
            round((allGradedItems.last().grade!! - allGradedItems.first().grade!!) * 10) / 10
        } else 0.0

        val creditsApproved = contributions
            .filter { it.second >= 3.0 }
            .sumOf { it.first.creditHours }

        var excellent = 0
        var approved = 0
        var regular = 0
        var failed = 0
        var totalGraded = 0
        contributions.forEach { (_, _, grades) ->
            grades.filter { it.grade != null && it.grade!! > 0 }.forEach { gradeItem ->
                totalGraded++
                val g = gradeItem.grade!!
                when {
                    g >= 4.6 -> excellent++
                    g >= 3.8 && g < 4.6 -> approved++
                    g >= 3.0 && g < 3.8 -> regular++
                    else -> failed++
                }
            }
        }

        val best = contributions.maxByOrNull { it.second }
        val bestSubjectName = best?.first?.name ?: ""
        val bestSubjectAverage = best?.second ?: 0.0

        val bestGrade = contributions
            .flatMap { it.third }
            .filter { it.grade != null }
            .maxByOrNull { it.grade!! }
        val bestGradeName = bestGrade?.name ?: ""
        val bestGradeValue = bestGrade?.grade ?: 0.0

        val performanceData = contributions.map { (subject, average, grades) ->
            val graded = grades
                .filter { it.grade != null && it.grade!! > 0 }
                .asReversed()
            val gradeValues = graded.map { it.grade!! }
            val labels = graded.map { it.name.take(8) }

            SubjectPerformanceData(
                subjectName = subject.name,
                currentAverage = average,
                trend = if (gradeValues.size >= 2)
                    round((gradeValues.last() - gradeValues.first()) * 10) / 10
                else 0.0,
                grades = gradeValues,
                activityLabels = labels,
                semester = subject.semester
            )
        }

        return StatsUiState(
            generalAverage = round(general * 10) / 10,
            generalTrend = trend,
            totalCredits = totalCreditsAll,
            totalCreditsApproved = creditsApproved,
            excellentCount = excellent,
            approvedCount = approved,
            regularCount = regular,
            failedCount = failed,
            totalSubjects = subjects.size,
            totalGradedItems = totalGraded,
            bestSubjectName = bestSubjectName,
            bestSubjectAverage = bestSubjectAverage,
            bestGradeName = bestGradeName,
            bestGradeValue = bestGradeValue,
            subjectsPerformance = performanceData,
            hasData = contributions.isNotEmpty()
        )
    }
}
