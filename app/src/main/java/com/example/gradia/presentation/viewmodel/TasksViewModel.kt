package com.example.gradia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradia.data.local.entity.Asignatura
import com.example.gradia.data.local.entity.Evento
import com.example.gradia.data.repository.AsignaturaRepository
import com.example.gradia.data.repository.EventoRepository
import com.example.gradia.data.repository.UserRepository
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
    val urgencia: Urgencia
)

data class TasksUiState(
    val tareasHoy: List<TareaUi> = emptyList(),
    val tareasProximas: List<TareaUi> = emptyList(),
    val tareasCompletadas: List<TareaUi> = emptyList(),
    val asignaturas: List<Asignatura> = emptyList(),
    val currentTitle: String = "",
    val currentFecha: Long = System.currentTimeMillis(),
    val selectedAsignaturaId: Long? = null,
    val editingTaskId: Long? = null,
    val selectedTaskIds: Set<Long> = emptySet(),
    val isSaving: Boolean = false,
    val error: String? = null
)

class TasksViewModel(
    private val userRepository: UserRepository,
    private val eventoRepository: EventoRepository,
    private val asignaturaRepository: AsignaturaRepository
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
            urgencia = calcularUrgencia(fecha)
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

    fun onAsignaturaSelected(asignaturaId: Long?) {
        _uiState.update { it.copy(selectedAsignaturaId = asignaturaId) }
    }

    fun loadTaskForEditing(tarea: TareaUi) {
        _uiState.update {
            it.copy(
                editingTaskId = tarea.id,
                currentTitle = tarea.titulo,
                currentFecha = tarea.fecha,
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

                if (state.editingTaskId != null) {
                    eventoRepository.updateEvento(
                        Evento(
                            id = state.editingTaskId,
                            userId = uid,
                            asignaturaId = state.selectedAsignaturaId,
                            titulo = state.currentTitle,
                            fecha = state.currentFecha,
                            tipo = "TAREA"
                        )
                    )
                } else {
                    eventoRepository.insertEvento(
                        Evento(
                            userId = uid,
                            asignaturaId = state.selectedAsignaturaId,
                            titulo = state.currentTitle,
                            fecha = state.currentFecha,
                            tipo = "TAREA"
                        )
                    )
                }

                _uiState.update {
                    it.copy(
                        editingTaskId = null,
                        currentTitle = "",
                        currentFecha = System.currentTimeMillis(),
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
                eventoRepository.updateEstadoCompletado(id, !currentCompleted)
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
            ids.forEach { id ->
                try { eventoRepository.deleteEventoById(id) } catch (_: Exception) { }
            }
            _uiState.update { it.copy(selectedTaskIds = emptySet()) }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            try { eventoRepository.deleteEventoById(id) } catch (_: Exception) { }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
