package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.data.local.entity.Evento
import com.example.gradia.data.repository.AsignaturaRepository
import com.example.gradia.data.repository.EventoRepository
import com.example.gradia.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth

data class CalendarActivity(
    val id: Long,
    val title: String,
    val fecha: Long,
    val asignaturaId: Long?,
    val asignaturaNombre: String?,
    val tipo: String,
    val prioridad: String
)

enum class Proximidad { CERCANO, PROXIMO, LEJANO }

data class CalendarUiState(
    val activitiesByDate: Map<LocalDate, List<CalendarActivity>> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateActivities: List<CalendarActivity> = emptyList(),
    val next7DaysActivities: List<CalendarActivity> = emptyList(),
    val isLoading: Boolean = true
)

class CalendarViewModel(
    private val userRepository: UserRepository,
    private val eventoRepository: EventoRepository,
    private val asignaturaRepository: AsignaturaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val asignaturaMap = mutableMapOf<Long, String>()
    private var currentUserId: String? = null

    init {
        observeUser()
    }

    private fun observeUser() {
        userRepository.getCurrentUser()
            .filterNotNull()
            .onEach { user ->
                currentUserId = user.id
                loadAsignaturas(user.id)
                observeEventos(user.id)
            }
            .launchIn(viewModelScope)
    }

    private fun loadAsignaturas(userId: String) {
        asignaturaRepository.getAsignaturasByUser(userId)
            .onEach { asignaturas ->
                asignaturaMap.clear()
                asignaturas.forEach { asignaturaMap[it.id] = it.nombre }
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeEventos(userId: String) {
        _uiState
            .map { it.selectedDate }
            .distinctUntilChanged()
            .flatMapLatest { selected ->
                val month = YearMonth.from(selected)
                val startMillis = month.atDay(1)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endMillis = month.plusMonths(1).atDay(1)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 7 * 24 * 60 * 60 * 1000
                eventoRepository.getEventosByUserAndDateRange(userId, startMillis, endMillis)
            }
            .onEach { eventos ->
                val mapped = eventos.filter { !it.completado }.map { it.toCalendarActivity() }
                val byDate = mapped.groupBy { it.fecha.toCalendarLocalDate() }
                val selectedDateActivities = byDate[_uiState.value.selectedDate].orEmpty()

                val today = LocalDate.now()
                val sevenDaysLater = today.plusDays(7)
                val next7Days = mapped
                    .filter { act ->
                        val date = act.fecha.toCalendarLocalDate()
                        (date.isEqual(today) || date.isAfter(today)) && date <= sevenDaysLater
                    }
                    .sortedWith(compareBy({ it.fecha }))

                _uiState.update {
                    it.copy(
                        activitiesByDate = byDate,
                        selectedDateActivities = selectedDateActivities.sortedBy { a -> a.fecha },
                        next7DaysActivities = next7Days,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectDate(date: LocalDate) {
        val activities = _uiState.value.activitiesByDate[date].orEmpty()
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedDateActivities = activities.sortedBy { a -> a.fecha }
            )
        }
    }

    private fun Evento.toCalendarActivity() = CalendarActivity(
        id = id,
        title = titulo,
        fecha = fecha,
        asignaturaId = asignaturaId,
        asignaturaNombre = asignaturaId?.let { asignaturaMap[it] },
        tipo = tipo,
        prioridad = prioridad
    )

}

private fun Long.toCalendarLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
