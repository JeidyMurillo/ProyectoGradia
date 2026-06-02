package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.data.local.entity.Asignatura
import com.example.gradia.data.local.entity.Evento
import com.example.gradia.data.repository.AsignaturaRepository
import com.example.gradia.data.repository.EventoRepository
import com.example.gradia.data.repository.UserRepository
import com.example.gradia.notifications.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class Urgencia { URGENTE, MEDIO, BAJO }

data class TareaUi(
    val id: Long,
    val titulo: String,
    val fecha: Long,
    val asignaturaId: Long?,
    val asignaturaNombre: String?,
    val completado: Boolean,
    val urgencia: Urgencia,
    val recordatorioMinutosAntes: Int = 30
)

data class TasksUiState(
    val tareasHoy: List<TareaUi> = emptyList(),
    val tareasProximas: List<TareaUi> = emptyList(),
    val tareasCompletadas: List<TareaUi> = emptyList(),
    val asignaturas: List<Asignatura> = emptyList(),
    val currentTitle: String = "",
    val currentFecha: Long = System.currentTimeMillis(),
    val currentHora: Int = 8,
    val currentMinuto: Int = 0,
    val currentRecordatorioMinutos: Int = 30,
    val selectedAsignaturaId: Long? = null,
    val editingTaskId: Long? = null,
    val selectedTaskIds: Set<Long> = emptySet(),
    val isSaving: Boolean = false,
    val error: String? = null
)

class TasksViewModel(
    private val userRepository: UserRepository,
    private val eventoRepository: EventoRepository,
    private val asignaturaRepository: AsignaturaRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val asignaturaMap = mutableMapOf<Long, String>()
    private var currentUserId: String? = null

    init {
        userRepository.getCurrentUser()
            .filterNotNull()
            .onEach { user ->
                currentUserId = user.id
                asignaturaRepository.getAsignaturasByUser(user.id)
                    .onEach { realSubjects ->
                        asignaturaMap.clear()
                        realSubjects.forEach { asignaturaMap[it.id] = it.nombre }
                        _uiState.update { it.copy(asignaturas = realSubjects) }
                    }
                    .launchIn(viewModelScope)
            }
            .launchIn(viewModelScope)

        userRepository.getCurrentUser()
            .filterNotNull()
            .flatMapLatest { user ->
                eventoRepository.getEventosByTipo("TAREA", user.id)
            }
            .onEach { eventos ->
                val hoy = mutableListOf<TareaUi>()
                val proximas = mutableListOf<TareaUi>()
                val completadas = mutableListOf<TareaUi>()

                eventos.forEach { evento ->
                    val tarea = evento.toTareaUi()
                    if (evento.completado) {
                        completadas.add(tarea)
                    } else if (esHoy(evento.fecha)) {
                        hoy.add(tarea)
                    } else {
                        proximas.add(tarea)
                    }
                }

                _uiState.update {
                    it.copy(
                        tareasHoy = hoy.sortedBy { t -> t.fecha },
                        tareasProximas = proximas.sortedBy { t -> t.fecha },
                        tareasCompletadas = completadas.sortedByDescending { t -> t.fecha }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun esHoy(fecha: Long): Boolean {
        val hoy = Calendar.getInstance()
        val cal = Calendar.getInstance().apply { timeInMillis = fecha }
        return hoy.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
               hoy.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
    }

    private fun Evento.toTareaUi(): TareaUi {
        return TareaUi(
            id = id,
            titulo = titulo,
            fecha = fecha,
            asignaturaId = asignaturaId,
            asignaturaNombre = asignaturaId?.let { asignaturaMap[it] },
            completado = completado,
            urgencia = calcularUrgencia(fecha),
            recordatorioMinutosAntes = recordatorioMinutosAntes
        )
    }

    private fun calcularUrgencia(fecha: Long): Urgencia {
        val ahora = System.currentTimeMillis()
        val diff = fecha - ahora
        val sieteDias = 7L * 24 * 60 * 60 * 1000
        val catorceDias = 14L * 24 * 60 * 60 * 1000

        return when {
            diff <= sieteDias -> Urgencia.URGENTE
            diff <= catorceDias -> Urgencia.MEDIO
            else -> Urgencia.BAJO
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(currentTitle = title) }
    }

    fun onFechaChange(fecha: Long) {
        _uiState.update { it.copy(currentFecha = fecha) }
    }

    fun onHoraChange(hora: Int, minuto: Int) {
        _uiState.update { it.copy(currentHora = hora, currentMinuto = minuto) }
    }

    fun onRecordatorioChange(minutos: Int) {
        _uiState.update { it.copy(currentRecordatorioMinutos = minutos) }
    }

    fun onAsignaturaSelected(asignaturaId: Long?) {
        _uiState.update { it.copy(selectedAsignaturaId = asignaturaId) }
    }

    fun loadTaskForEditing(tarea: TareaUi) {
        val cal = Calendar.getInstance().apply { timeInMillis = tarea.fecha }
        _uiState.update {
            it.copy(
                editingTaskId = tarea.id,
                currentTitle = tarea.titulo,
                currentFecha = tarea.fecha,
                currentHora = cal.get(Calendar.HOUR_OF_DAY),
                currentMinuto = cal.get(Calendar.MINUTE),
                currentRecordatorioMinutos = tarea.recordatorioMinutosAntes,
                selectedAsignaturaId = tarea.asignaturaId
            )
        }
    }

    fun cancelEditing() {
        _uiState.update {
            it.copy(
                editingTaskId = null,
                currentTitle = "",
                currentFecha = System.currentTimeMillis(),
                currentHora = 8,
                currentMinuto = 0,
                currentRecordatorioMinutos = 30,
                selectedAsignaturaId = null
            )
        }
    }

    fun saveTask() {
        val state = _uiState.value
        if (state.currentTitle.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val uid = currentUserId ?: return@launch

                val fechaCal = Calendar.getInstance().apply {
                    timeInMillis = state.currentFecha
                    set(Calendar.HOUR_OF_DAY, state.currentHora)
                    set(Calendar.MINUTE, state.currentMinuto)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val fechaFinal = fechaCal.timeInMillis

                if (state.editingTaskId != null) {
                    reminderScheduler.cancelReminder(state.editingTaskId)
                    eventoRepository.updateEvento(
                        Evento(
                            id = state.editingTaskId,
                            userId = uid,
                            asignaturaId = state.selectedAsignaturaId,
                            titulo = state.currentTitle,
                            fecha = fechaFinal,
                            tipo = "TAREA",
                            recordatorioMinutosAntes = state.currentRecordatorioMinutos
                        )
                    )
                    reminderScheduler.scheduleReminder(
                        state.editingTaskId, state.currentTitle, fechaFinal,
                        state.currentRecordatorioMinutos
                    )
                } else {
                    val newId = eventoRepository.insertEvento(
                        Evento(
                            userId = uid,
                            asignaturaId = state.selectedAsignaturaId,
                            titulo = state.currentTitle,
                            fecha = fechaFinal,
                            tipo = "TAREA",
                            recordatorioMinutosAntes = state.currentRecordatorioMinutos
                        )
                    )
                    reminderScheduler.scheduleReminder(
                        newId, state.currentTitle, fechaFinal,
                        state.currentRecordatorioMinutos
                    )
                }

                _uiState.update {
                    it.copy(
                        editingTaskId = null,
                        currentTitle = "",
                        currentFecha = System.currentTimeMillis(),
                        currentHora = 8,
                        currentMinuto = 0,
                        currentRecordatorioMinutos = 30,
                        selectedAsignaturaId = null,
                        isSaving = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }

    fun toggleTaskCompletion(id: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val newCompleted = !currentCompleted
                val uid = currentUserId ?: return@launch
                eventoRepository.updateEstadoCompletado(id, newCompleted, uid)
                if (newCompleted) {
                    reminderScheduler.cancelReminder(id)
                } else {
                    val evento = eventoRepository.getEventoByIdSync(id, uid)
                    if (evento != null) {
                        reminderScheduler.scheduleReminder(
                            evento.id, evento.titulo, evento.fecha, evento.recordatorioMinutosAntes
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleTaskSelection(id: Long) {
        _uiState.update { state ->
            val current = state.selectedTaskIds
            if (id in current) state.copy(selectedTaskIds = current - id)
            else state.copy(selectedTaskIds = current + id)
        }
    }

    fun clearTaskSelection() {
        _uiState.update { it.copy(selectedTaskIds = emptySet()) }
    }

    fun deleteSelectedTasks() {
        val ids = _uiState.value.selectedTaskIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val uid = currentUserId ?: return@launch
            ids.forEach { id ->
                try {
                    reminderScheduler.cancelReminder(id)
                    eventoRepository.deleteEventoById(id, uid)
                } catch (_: Exception) { }
            }
            _uiState.update { it.copy(selectedTaskIds = emptySet()) }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            val uid = currentUserId ?: return@launch
            try {
                reminderScheduler.cancelReminder(id)
                eventoRepository.deleteEventoById(id, uid)
            } catch (_: Exception) { }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}