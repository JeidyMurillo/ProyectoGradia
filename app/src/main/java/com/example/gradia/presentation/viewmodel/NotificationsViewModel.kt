package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.data.repository.EventoRepository
import com.example.gradia.data.repository.UserRepository
import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.NotificationItem
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import com.example.gradia.domain.usecase.CalculateCurrentAverageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class NotificationsUiState(
    val reminders: List<NotificationItem.Reminder> = emptyList(),
    val incompleteSubjects: List<NotificationItem.IncompleteSubject> = emptyList(),
    val lowAverages: List<NotificationItem.LowAverage> = emptyList(),
    val passingGrades: List<NotificationItem.PassingGrade> = emptyList()
) {
    val totalCount: Int get() = reminders.size + incompleteSubjects.size + lowAverages.size + passingGrades.size
}

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModel(
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val eventoRepository: EventoRepository,
    @Suppress("UNUSED_PARAMETER") calculateCurrentAverage: CalculateCurrentAverageUseCase
) : ViewModel() {

    val uiState: StateFlow<NotificationsUiState> =
        userRepository.getCurrentUser()
            .flatMapLatest { user ->
                val userId = user?.id ?: return@flatMapLatest flowOf(NotificationsUiState())
                val currentSemester = user.semestre.toIntOrNull()

                subjectRepository.getSubjects()
                    .flatMapLatest { allSubjects ->
                        val semesterSubjects = if (currentSemester != null)
                            allSubjects.filter { it.semester == currentSemester }
                        else allSubjects

                        val subjectNameMap = allSubjects.associate { it.id to it.name }

                        val remindersFlow: Flow<List<NotificationItem.Reminder>> =
                            eventoRepository.getEventosByUser(userId).map { eventos ->
                                val now = System.currentTimeMillis()
                                eventos
                                    .filter { ev ->
                                        !ev.completado &&
                                        ev.fecha > now &&
                                        ev.fecha - ev.recordatorioMinutosAntes * 60_000L <= now
                                    }
                                    .map { ev ->
                                        NotificationItem.Reminder(
                                            evento = ev,
                                            subjectName = ev.asignaturaId?.let { subjectNameMap[it] }
                                        )
                                    }
                            }

                        val subjectNotifsFlow: Flow<SubjectNotifs> =
                            if (semesterSubjects.isEmpty()) {
                                flowOf(SubjectNotifs())
                            } else {
                                combine(
                                    semesterSubjects.map { subject ->
                                        subjectRepository.getGradeItemsBySubject(subject.id)
                                            .map { grades -> subject to grades }
                                    }
                                ) { pairs -> buildSubjectNotifs(pairs.toList()) }
                            }

                        combine(remindersFlow, subjectNotifsFlow) { reminders, sn ->
                            NotificationsUiState(
                                reminders = reminders,
                                incompleteSubjects = sn.incomplete,
                                lowAverages = sn.low,
                                passingGrades = sn.passing
                            )
                        }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    private data class SubjectNotifs(
        val incomplete: List<NotificationItem.IncompleteSubject> = emptyList(),
        val low: List<NotificationItem.LowAverage> = emptyList(),
        val passing: List<NotificationItem.PassingGrade> = emptyList()
    )

    private fun buildSubjectNotifs(perSubject: List<Pair<Subject, List<GradeItem>>>): SubjectNotifs {
        val incomplete = mutableListOf<NotificationItem.IncompleteSubject>()
        val low = mutableListOf<NotificationItem.LowAverage>()
        val passing = mutableListOf<NotificationItem.PassingGrade>()

        for ((subject, grades) in perSubject) {
            val totalConfigured = grades.sumOf { it.percentage }
            val graded = grades.filter { it.grade != null }
            val evaluatedPct = graded.sumOf { it.percentage }

            if (grades.isNotEmpty() && totalConfigured < 100.0) {
                incomplete += NotificationItem.IncompleteSubject(
                    subject = subject,
                    remainingPercentage = 100.0 - totalConfigured
                )
            }

            if (graded.isEmpty() || evaluatedPct == 0.0) continue

            val partialAvg = graded.sumOf { it.grade!! * it.percentage } / evaluatedPct
            val rounded = Math.round(partialAvg * 100.0) / 100.0
            val evaluatedFraction = if (totalConfigured > 0) evaluatedPct / totalConfigured else 0.0

            if (evaluatedFraction >= 0.60 && partialAvg < 2.0) {
                low += NotificationItem.LowAverage(subject = subject, average = rounded)
            }
            if (partialAvg >= subject.passingGrade) {
                passing += NotificationItem.PassingGrade(subject = subject, average = rounded)
            }
        }

        return SubjectNotifs(incomplete, low, passing)
    }
}
