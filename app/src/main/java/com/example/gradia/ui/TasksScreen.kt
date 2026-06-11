package com.example.gradia.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.presentation.viewmodel.TasksViewModel
import com.example.gradia.presentation.viewmodel.Urgencia
import com.example.gradia.ui.theme.GrayText
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia
import com.example.gradia.ui.theme.UrgencyAlta
import com.example.gradia.ui.theme.UrgencyBaja
import com.example.gradia.ui.theme.UrgencyMedia
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val recordatorioOpciones: List<Int> = listOf(15, 30, 60, 120, 1440)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Long?>(null) }
    var pendingSave by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.currentFecha
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = millis
                        }
                        val localCal = java.util.Calendar.getInstance().apply {
                            set(
                                utcCal.get(java.util.Calendar.YEAR),
                                utcCal.get(java.util.Calendar.MONTH),
                                utcCal.get(java.util.Calendar.DAY_OF_MONTH),
                                state.currentHora,
                                state.currentMinuto,
                                0
                            )
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                        viewModel.onFechaChange(localCal.timeInMillis)
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    taskToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = stringResource(R.string.delete_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = InterFontFamily
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_confirm_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = InterFontFamily
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(id)
                    taskToDelete = null
                }) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = PurpleGradia
                    )
                }
            }
        )
    }

    if (pendingSave) {
        AlertDialog(
            onDismissRequest = { pendingSave = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = stringResource(R.string.save_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = InterFontFamily
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.save_confirm_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = InterFontFamily
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingSave = false
                    viewModel.saveTask()
                }) {
                    Text(
                        text = stringResource(R.string.action_save),
                        color = PurpleGradia,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingSave = false }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (state.editingTaskId != null) stringResource(R.string.task_edit_title) else stringResource(R.string.task_create_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (state.editingTaskId != null) {
                    TextButton(onClick = { viewModel.cancelEditing() }) {
                        Text(stringResource(R.string.action_cancel), color = PurpleGradia, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            CreateTaskCard(
                title = state.currentTitle,
                onTitleChange = viewModel::onTitleChange,
                fecha = state.currentFecha,
                hora = state.currentHora,
                minuto = state.currentMinuto,
                onFechaClick = { showDatePicker = true },
                onHoraClick = {
                    TimePickerDialog(
                        ctx,
                        { _, h, m -> viewModel.onHoraChange(h, m) },
                        state.currentHora,
                        state.currentMinuto,
                        false
                    ).show()
                },
                recordatorioMinutos = state.currentRecordatorioMinutos,
                onRecordatorioChange = viewModel::onRecordatorioChange,
                asignaturas = state.asignaturas,
                selectedAsignaturaId = state.selectedAsignaturaId,
                onAsignaturaSelected = viewModel::onAsignaturaSelected,
                showSubjectDropdown = showSubjectDropdown,
                onSubjectDropdownChange = { showSubjectDropdown = it }
            )
        }

        item {
            Button(
                onClick = {
                    if (state.editingTaskId != null) {
                        pendingSave = true
                    } else {
                        viewModel.saveTask()
                    }
                },
                enabled = state.currentTitle.isNotBlank() && !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleGradia,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (state.editingTaskId != null) stringResource(R.string.action_save) else stringResource(R.string.action_add),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (state.selectedTaskIds.isNotEmpty()) {
            item {
                MultiSelectBanner(
                    selectedCount = state.selectedTaskIds.size,
                    onDeleteSelected = {
                        taskToDelete = state.selectedTaskIds.first()
                        viewModel.deleteSelectedTasks()
                    },
                    onClearSelection = viewModel::clearTaskSelection
                )
            }
        }

        if (state.tareasHoy.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.section_today),
                    count = "${state.tareasHoy.size} ${if (state.tareasHoy.size == 1) stringResource(R.string.task_singular) else stringResource(R.string.task_plural)}",
                    color = PurpleGradia
                )
            }
            items(state.tareasHoy, key = { it.id }) { tarea ->
                TaskCard(
                    title = tarea.titulo,
                    urgency = tarea.urgencia,
                    time = formatFecha(tarea.fecha),
                    subject = tarea.asignaturaNombre,
                    isCompleted = false,
                    isSelected = tarea.id in state.selectedTaskIds,
                    onToggleCompletion = { viewModel.toggleTaskCompletion(tarea.id, false) },
                    onClick = { viewModel.loadTaskForEditing(tarea) },
                    onLongClick = { viewModel.toggleTaskSelection(tarea.id) }
                )
            }
        }

        if (state.tareasProximas.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(R.string.section_upcoming), color = GrayText)
            }
            items(state.tareasProximas, key = { it.id }) { tarea ->
                TaskCard(
                    title = tarea.titulo,
                    urgency = tarea.urgencia,
                    time = formatFecha(tarea.fecha),
                    subject = tarea.asignaturaNombre,
                    isCompleted = false,
                    isSelected = tarea.id in state.selectedTaskIds,
                    onToggleCompletion = { viewModel.toggleTaskCompletion(tarea.id, false) },
                    onClick = { viewModel.loadTaskForEditing(tarea) },
                    onLongClick = { viewModel.toggleTaskSelection(tarea.id) }
                )
            }
        }

        if (state.tareasCompletadas.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(R.string.section_completed), isCompleted = true, color = MaterialTheme.colorScheme.primary)
            }
            items(state.tareasCompletadas, key = { it.id }) { tarea ->
                TaskCard(
                    title = tarea.titulo,
                    urgency = tarea.urgencia,
                    time = formatFecha(tarea.fecha),
                    subject = tarea.asignaturaNombre,
                    isCompleted = true,
                    isSelected = tarea.id in state.selectedTaskIds,
                    onToggleCompletion = { viewModel.toggleTaskCompletion(tarea.id, true) },
                    onClick = { viewModel.loadTaskForEditing(tarea) },
                    onLongClick = { viewModel.toggleTaskSelection(tarea.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun MultiSelectBanner(
    selectedCount: Int,
    onDeleteSelected: () -> Unit,
    onClearSelection: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$selectedCount ${stringResource(R.string.selected)}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onDeleteSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
            }
            IconButton(onClick = onClearSelection) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun CreateTaskCard(
    title: String,
    onTitleChange: (String) -> Unit,
    fecha: Long,
    hora: Int,
    minuto: Int,
    onFechaClick: () -> Unit,
    onHoraClick: () -> Unit,
    recordatorioMinutos: Int,
    onRecordatorioChange: (Int) -> Unit,
    asignaturas: List<com.example.gradia.data.local.entity.Asignatura>,
    selectedAsignaturaId: Long?,
    onAsignaturaSelected: (Long?) -> Unit,
    showSubjectDropdown: Boolean,
    onSubjectDropdownChange: (Boolean) -> Unit
) {
    val selectedAsignatura = asignaturas.find { it.id == selectedAsignaturaId }
    val horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", hora, minuto)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(2.dp, PurpleGradia, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        placeholder = {
                            Text(
                                stringResource(R.string.placeholder_event_name),
                                color = GrayText,
                                fontSize = 14.sp
                            )
                        },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = PurpleGradia
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            formatFechaCorta(fecha),
                            color = if (fecha > 0) MaterialTheme.colorScheme.onSurface else GrayText,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { onFechaClick() }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.clock),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            horaFormateada,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { onHoraClick() }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.graduation_cap_gray),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box {
                            Text(
                                selectedAsignatura?.nombre ?: stringResource(R.string.placeholder_subject_task),
                                color = if (selectedAsignatura != null) MaterialTheme.colorScheme.onSurface else GrayText,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { onSubjectDropdownChange(true) }
                            )
                            DropdownMenu(
                                expanded = showSubjectDropdown,
                                onDismissRequest = { onSubjectDropdownChange(false) }
                            ) {
                                asignaturas.forEach { asignatura ->
                                    DropdownMenuItem(
                                        text = { Text(asignatura.nombre) },
                                        onClick = {
                                            onAsignaturaSelected(asignatura.id)
                                            onSubjectDropdownChange(false)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.dropdown_none), color = GrayText) },
                                    onClick = {
                                        onAsignaturaSelected(null)
                                        onSubjectDropdownChange(false)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                stringResource(R.string.label_reminder),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = GrayText
            )
            Spacer(modifier = Modifier.height(6.dp))
            val opcionesRecordatorio = listOf(
                15 to stringResource(R.string.reminder_15min),
                30 to stringResource(R.string.reminder_30min),
                60 to stringResource(R.string.reminder_1hour),
                120 to stringResource(R.string.reminder_2hours),
                1440 to stringResource(R.string.reminder_1day)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(opcionesRecordatorio) { (minutos, etiqueta) ->
                    val seleccionado = recordatorioMinutos == minutos
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (seleccionado) PurpleGradia else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { onRecordatorioChange(minutos) }
                    ) {
                        Text(
                            text = etiqueta,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            fontSize = 12.sp,
                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                            color = if (seleccionado) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: String? = null, color: Color, isCompleted: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCompleted) {
            Icon(Icons.Default.Check, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = InterFontFamily,
                fontSize = 20.sp
            )
        )
        if (count != null) {
            Spacer(modifier = Modifier.weight(1f))
            Text(count, color = GrayText, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    title: String,
    urgency: Urgencia,
    time: String? = null,
    subject: String? = null,
    isCompleted: Boolean = false,
    isSelected: Boolean = false,
    onToggleCompletion: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val alpha = if (isCompleted) 0.5f else 1f
    val borderColor = if (isSelected) PurpleGradia else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .combinedClickable(
                onClick = onClick ?: {},
                onLongClick = onLongClick ?: {}
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                UrgencyLabel(urgency)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .then(
                            if (isCompleted) Modifier.background(PurpleGradia, CircleShape)
                            else Modifier.border(2.dp, PurpleGradia, CircleShape)
                        )
                        .clickable(enabled = onToggleCompletion != null) {
                            onToggleCompletion?.invoke()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) GrayText else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (time != null || subject != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (time != null) {
                                Icon(
                                    painter = painterResource(id = R.drawable.clock),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(time, color = GrayText, fontSize = 13.sp)
                            }
                            if (subject != null) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.graduation_cap_gray),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(subject, color = GrayText, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UrgencyLabel(urgency: Urgencia) {
    val (bgColor, textColor, label) = when (urgency) {
        Urgencia.URGENTE -> Triple(UrgencyAlta, Color.White, stringResource(R.string.urgency_urgent))
        Urgencia.MEDIO -> Triple(UrgencyMedia, Color.Black, stringResource(R.string.urgency_medium))
        Urgencia.BAJO -> Triple(UrgencyBaja, Color.White, stringResource(R.string.urgency_low))
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor.copy(alpha = 0.25f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

private fun formatFechaCorta(fecha: Long): String {
    if (fecha <= 0) return "Fecha"
    return SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(fecha))
}

private fun formatFecha(fecha: Long): String {
    if (fecha <= 0) return ""
    val hoyCal = java.util.Calendar.getInstance()
    val fechaCal = java.util.Calendar.getInstance().apply { timeInMillis = fecha }

    return if (hoyCal.get(java.util.Calendar.YEAR) == fechaCal.get(java.util.Calendar.YEAR) &&
        hoyCal.get(java.util.Calendar.DAY_OF_YEAR) == fechaCal.get(java.util.Calendar.DAY_OF_YEAR)) {
        SimpleDateFormat("'Hoy,' h:mm a", Locale.getDefault()).format(Date(fecha))
    } else {
        SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(fecha))
    }
}
