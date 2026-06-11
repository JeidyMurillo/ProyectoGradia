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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class NotificationsUiState(
    val reminders: List<NotificationItem.Reminder> = emptyList(),
    val incompleteSubjects: List<NotificationItem.IncompleteSubject> = emptyList(),
    val lowAverages: List<NotificationItem.LowAverage> = emptyList(),
    val passingGrades: List<NotificationItem.PassingGrade> = emptyList(),
    val urgentNotifications: List<NotificationItem.ActivityProximityNotification> = emptyList(),
    val upcomingNotifications: List<NotificationItem.ActivityProximityNotification> = emptyList(),
    val completedNotifications: List<NotificationItem.ActivityProximityNotification> = emptyList()
) {
    val totalCount: Int get() =
        reminders.size + incompleteSubjects.size + lowAverages.size + passingGrades.size +
        urgentNotifications.size + upcomingNotifications.size + completedNotifications.size
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
                val userName = user.nombre.ifBlank { "Estudiante" }
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

                        val proximityFlow: Flow<List<NotificationItem.ActivityProximityNotification>> =
                            eventoRepository.getEventosByUser(userId).map { eventos ->
                                eventos.mapNotNull { ev ->
                                    val daysRemaining = calculateDaysRemaining(ev.fecha)
                                    buildProximityNotification(ev, daysRemaining, userName, subjectNameMap)
                                }
                                    .sortedWith(
                                        compareBy<NotificationItem.ActivityProximityNotification> { !it.isCompleted && it.daysRemaining < 0 }
                                            .thenBy { !it.isCompleted && it.daysRemaining <= 1 }
                                            .thenBy { !it.isCompleted && it.daysRemaining <= 3 }
                                            .thenBy { !it.isCompleted && it.daysRemaining <= 7 }
                                            .thenBy { it.isCompleted }
                                    )
                            }

                        combine(remindersFlow, subjectNotifsFlow, proximityFlow) { reminders, sn, proximity ->
                            val urgent = proximity.filter { !it.isCompleted && it.daysRemaining <= 1 }
                            val upcoming = proximity.filter { !it.isCompleted && it.daysRemaining > 1 }
                            val completed = proximity.filter { it.isCompleted }
                            NotificationsUiState(
                                reminders = reminders,
                                incompleteSubjects = sn.incomplete,
                                lowAverages = sn.low,
                                passingGrades = sn.passing,
                                urgentNotifications = urgent,
                                upcomingNotifications = upcoming,
                                completedNotifications = completed
                            )
                        }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    private fun buildProximityNotification(
        ev: com.example.gradia.data.local.entity.Evento,
        daysRemaining: Long,
        userName: String,
        subjectNameMap: Map<Long, String>
    ): NotificationItem.ActivityProximityNotification? {
        val subjectName = ev.asignaturaId?.let { subjectNameMap[it] }
        val activityName = ev.titulo
        val name = userName

        val message = when {
            ev.completado -> "Excelente trabajo, $name. Has completado '$activityName'. Una preocupación menos para tu semestre."
            daysRemaining < 0 -> "$name, tenemos una pequeña emergencia académica. '$activityName' ya venció. Revisémosla cuanto antes."
            daysRemaining == 0L -> "¡Hoy es el gran día, $name! '$activityName' vence hoy. Tu misión, si decides aceptarla, es entregarla antes de que termine el día."
            daysRemaining == 1L -> "¡Último aviso, $name! '$activityName' vence mañana. El tiempo corre más rápido que el WiFi durante un examen."
            daysRemaining in 2..3 -> "¡Atención, $name! '$activityName' vence en $daysRemaining días. Todavía hay tiempo, pero ya no es momento de ignorarla."
            daysRemaining in 4..6 -> "$name, tu actividad '$activityName' empieza a acercarse. Tu yo del futuro agradecerá que la adelantes un poco."
            daysRemaining == 7L -> "Hola, $name. Tu actividad '$activityName' está tranquila por ahora, pero dentro de una semana tocará ponerse manos a la obra."
            else -> null
        } ?: return null

        return NotificationItem.ActivityProximityNotification(
            activityTitle = activityName,
            activityType = ev.tipo,
            subjectName = subjectName,
            dueDateMillis = ev.fecha,
            daysRemaining = daysRemaining,
            isCompleted = ev.completado,
            message = message
        )
    }

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

    companion object {
        private fun calculateDaysRemaining(fechaMillis: Long): Long {
            val today = LocalDate.now()
            val date = Instant.ofEpochMilli(fechaMillis)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            return ChronoUnit.DAYS.between(today, date)
        }
    }
}
