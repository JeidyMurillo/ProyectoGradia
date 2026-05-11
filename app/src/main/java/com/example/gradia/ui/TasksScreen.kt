package com.example.gradia.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.presentation.viewmodel.TasksViewModel
import com.example.gradia.presentation.viewmodel.Urgencia
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showSubjectDropdown by remember { mutableStateOf(false) }

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
                            set(utcCal.get(java.util.Calendar.YEAR), utcCal.get(java.util.Calendar.MONTH), utcCal.get(java.util.Calendar.DAY_OF_MONTH), 12, 0, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                        viewModel.onFechaChange(localCal.timeInMillis)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
                    if (state.editingTaskId != null) "Editar Tarea:" else "Crear Tarea:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily,
                        color = Color(0xFF4A4A4A)
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (state.editingTaskId != null) {
                    TextButton(onClick = { viewModel.cancelEditing() }) {
                        Text("Cancelar", color = PurpleGradia, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            CreateTaskCard(
                title = state.currentTitle,
                onTitleChange = viewModel::onTitleChange,
                fecha = state.currentFecha,
                onFechaClick = { showDatePicker = true },
                asignaturas = state.asignaturas,
                selectedAsignaturaId = state.selectedAsignaturaId,
                onAsignaturaSelected = viewModel::onAsignaturaSelected,
                showSubjectDropdown = showSubjectDropdown,
                onSubjectDropdownChange = { showSubjectDropdown = it }
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color(0xFFE9E4F0), RoundedCornerShape(12.dp))
                    .clickable(enabled = state.currentTitle.isNotBlank() && !state.isSaving) {
                        viewModel.saveTask()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PurpleGradia,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.add_gray),
                        contentDescription = if (state.editingTaskId != null) "Guardar tarea" else "Crear tarea",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (state.tareasHoy.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Hoy",
                    count = "${state.tareasHoy.size} ${if (state.tareasHoy.size == 1) "Tarea" else "Tareas"}",
                    color = PurpleGradia
                )
            }
            items(state.tareasHoy, key = { it.id }) { tarea ->
                TaskCard(
                    title = tarea.titulo,
                    urgency = tarea.urgencia.name,
                    urgencyColor = urgencyColor(tarea.urgencia),
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
                SectionHeader(title = "Próximamente", color = Color.Gray)
            }
            items(state.tareasProximas, key = { it.id }) { tarea ->
                TaskCard(
                    title = tarea.titulo,
                    urgency = tarea.urgencia.name,
                    urgencyColor = urgencyColor(tarea.urgencia),
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
                SectionHeader(title = "Completadas", isCompleted = true, color = Color(0xFF453284))
            }
            items(state.tareasCompletadas, key = { it.id }) { tarea ->
                TaskCard(
                    title = tarea.titulo,
                    urgency = tarea.urgencia.name,
                    urgencyColor = urgencyColor(tarea.urgencia),
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
fun CreateTaskCard(
    title: String,
    onTitleChange: (String) -> Unit,
    fecha: Long,
    onFechaClick: () -> Unit,
    asignaturas: List<com.example.gradia.data.local.entity.Asignatura>,
    selectedAsignaturaId: Long?,
    onAsignaturaSelected: (Long?) -> Unit,
    showSubjectDropdown: Boolean,
    onSubjectDropdownChange: (Boolean) -> Unit
) {
    val selectedAsignatura = asignaturas.find { it.id == selectedAsignaturaId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0F8)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                UrgencyLabel("URGENCIA", Color(0xFFD1C4E9))
            }
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
                                "Nombre del evento",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF453284),
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
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            formatFecha(fecha),
                            color = if (fecha > 0) Color(0xFF453284) else Color.LightGray,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { onFechaClick() }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.graduation_cap_gray),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box {
                            Text(
                                selectedAsignatura?.nombre ?: "Asignatura",
                                color = if (selectedAsignatura != null) Color(0xFF453284) else Color.LightGray,
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
                                    text = { Text("Ninguna", color = Color.Gray) },
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
            Text(count, color = Color.LightGray, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    title: String,
    urgency: String,
    urgencyColor: Color,
    time: String? = null,
    subject: String? = null,
    isCompleted: Boolean = false,
    isSelected: Boolean = false,
    onToggleCompletion: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val alpha = if (isCompleted) 0.5f else 1f
    val borderColor = if (isSelected) PurpleGradia else Color.LightGray.copy(alpha = 0.3f)
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
        colors = CardDefaults.cardColors(containerColor = if (isCompleted) Color(0xFFF9F9F9) else Color(0xFFFDFBFF)),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                UrgencyLabel(
                    text = urgency,
                    color = urgencyColor,
                    textColor = if (urgency == "MEDIO") Color.Black else urgencyColor
                )
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
                            color = if (isCompleted) Color.Gray else Color(0xFF4A4A4A)
                        )
                    )
                    if (time != null || subject != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (time != null) {
                                Icon(
                                    painter = painterResource(id = R.drawable.clock),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(time, color = Color.Gray, fontSize = 13.sp)
                            }
                            if (subject != null) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.graduation_cap_gray),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(subject, color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UrgencyLabel(text: String, color: Color, textColor: Color = color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.3f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

private fun urgencyColor(urgencia: Urgencia): Color = when (urgencia) {
    Urgencia.URGENTE -> Color(0xFFFF8A80)
    Urgencia.MEDIO -> Color(0xFFFFF176)
    Urgencia.BAJO -> Color(0xFFA5D6A7)
}

private fun formatFecha(fecha: Long): String {
    if (fecha <= 0) return ""
    val cal = java.util.Calendar.getInstance()
    val hoyCal = java.util.Calendar.getInstance()
    val fechaCal = java.util.Calendar.getInstance().apply { timeInMillis = fecha }

    if (hoyCal.get(java.util.Calendar.YEAR) == fechaCal.get(java.util.Calendar.YEAR) &&
        hoyCal.get(java.util.Calendar.DAY_OF_YEAR) == fechaCal.get(java.util.Calendar.DAY_OF_YEAR)) {
        return SimpleDateFormat("'Hoy,' h:mm a", Locale.getDefault()).format(Date(fecha))
    }
    return SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(fecha))
}
