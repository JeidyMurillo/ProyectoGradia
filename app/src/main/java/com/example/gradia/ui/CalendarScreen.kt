package com.example.gradia.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.presentation.viewmodel.CalendarActivity
import com.example.gradia.presentation.viewmodel.CalendarUiState
import com.example.gradia.presentation.viewmodel.CalendarViewModel
import com.example.gradia.presentation.viewmodel.Proximidad
import com.example.gradia.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        CalendarCard(
            state = state,
            onMonthChange = { },
            onDateSelect = { viewModel.selectDate(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SelectedDayDetail(state)

        Spacer(modifier = Modifier.height(24.dp))

        Next7DaysSection(state)

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun CalendarCard(
    state: CalendarUiState,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelect: (LocalDate) -> Unit
) {
    val currentMonth = YearMonth.from(state.selectedDate)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, PurpleGradia),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { onDateSelect(currentMonth.minusMonths(1).atDay(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous month",
                        tint = PurpleGradia
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.clickable { }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = currentMonth.year.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.clickable { }
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = { onDateSelect(currentMonth.plusMonths(1).atDay(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next month",
                        tint = PurpleGradia
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sa").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            val firstDay = currentMonth.atDay(1)
            val daysInMonth = currentMonth.lengthOfMonth()
            val startingDayOfWeek = (firstDay.dayOfWeek.value % 7)

            val weeks = mutableListOf<List<Int?>>()
            var currentWeek = MutableList(7) { null as Int? }
            var dayIndex = 0

            for (i in startingDayOfWeek until 7) {
                currentWeek[i] = ++dayIndex
            }
            weeks.add(currentWeek)

            while (dayIndex < daysInMonth) {
                currentWeek = MutableList(7) { null }
                for (i in 0 until 7) {
                    if (dayIndex < daysInMonth) {
                        currentWeek[i] = ++dayIndex
                    }
                }
                weeks.add(currentWeek)
            }

            weeks.forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    week.forEach { day ->
                        if (day != null) {
                            val date = currentMonth.atDay(day)
                            val isSelected = date == state.selectedDate
                            val isToday = date == LocalDate.now()
                            val activitiesForDay = state.activitiesByDate[date].orEmpty()

                            DayCell(
                                day = day,
                                isSelected = isSelected,
                                isToday = isToday,
                                activities = activitiesForDay,
                                onClick = { onDateSelect(date) }
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    activities: List<CalendarActivity>,
    onClick: () -> Unit
) {
    val dotCount = activities.size.coerceIn(0, 3)
    val dotColor = if (activities.isNotEmpty()) {
        val closest = activities.minByOrNull { it.fecha }
        if (closest != null) closest.proximidad().proximidadColor()
        else Color.Transparent
    } else Color.Transparent

    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isSelected -> PurpleGradia
                        isToday -> PurpleGradia.copy(alpha = 0.2f)
                        else -> Color.Transparent
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        day < 10 -> PurpleGradia
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp
                )
            )
        }

        if (dotCount > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.height(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(dotCount) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SelectedDayDetail(state: CalendarUiState) {
    val date = state.selectedDate
    val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"))
    val dateTitle = date.format(formatter)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = dateTitle.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PurpleGradia,
                fontSize = 16.sp,
                fontFamily = InterFontFamily
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (state.selectedDateActivities.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay actividades programadas para este día",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            state.selectedDateActivities.forEach { activity ->
                ActivityCard(activity)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: CalendarActivity) {
    val isDark = isSystemInDarkTheme()
    val (bgColor, iconBg, iconRes) = when (activity.tipo) {
        "EVALUACION" -> Triple(
            if (isDark) DarkEventParcialBg else EventParcialBg,
            if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935), R.drawable.document
        )
        "TAREA" -> Triple(
            if (isDark) DarkEventTareasBg else EventTareasBg,
            if (isDark) Color(0xFF90CAF9) else Color(0xFF1E88E5), R.drawable.list
        )
        "ENTREGA" -> Triple(
            if (isDark) DarkEventosBg else EventosBg,
            if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047), R.drawable.calendar
        )
        "ACTIVIDAD" -> Triple(
            if (isDark) DarkEventTareasBg else EventTareasBg,
            if (isDark) Color(0xFFCE93D8) else Color(0xFF7B1FA2), R.drawable.calendar
        )
        else -> Triple(
            if (isDark) DarkEventParcialBg else EventParcialBg,
            Color(0xFF2C2C2C), R.drawable.document
        )
    }

    val textColor = when (activity.tipo) {
        "EVALUACION" -> if (isDark) DarkOnSurface else Color(0xFFC62828)
        "TAREA" -> if (isDark) DarkOnSurface else Color(0xFF1565C0)
        "ENTREGA" -> if (isDark) DarkOnSurface else Color(0xFF2E7D32)
        "ACTIVIDAD" -> if (isDark) DarkOnSurface else Color(0xFF7B1FA2)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = activity.tipo,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activity.fecha.toFormattedTime(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PurpleGradia,
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp
                    )
                )

                if (!activity.asignaturaNombre.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Materia: ${activity.asignaturaNombre}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                val prioridadLabel = when (activity.prioridad) {
                    "ALTA" -> "Alta"
                    "MEDIA" -> "Media"
                    "BAJA" -> "Baja"
                    else -> activity.prioridad
                }
                val tipoLabel = when (activity.tipo) {
                    "EVALUACION" -> "Parcial"
                    "TAREA" -> "Tarea"
                    "ENTREGA" -> "Evento"
                    "ACTIVIDAD" -> "Actividad"
                    else -> activity.tipo
                }
                val prioridadTexto = if (activity.esActividad) {
                    if (activity.prioridad == "PENDIENTE") "Sin calificar" else "Calificada"
                } else {
                    "Prioridad: $prioridadLabel"
                }
                Text(
                    text = "$tipoLabel — $prioridadTexto",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun Next7DaysSection(state: CalendarUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Próximos 7 días",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PurpleGradia,
                fontSize = 16.sp,
                fontFamily = InterFontFamily
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (state.next7DaysActivities.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay actividades programadas para los próximos 7 días",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            state.next7DaysActivities.forEach { activity ->
                UpcomingActivityCard(activity)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun UpcomingActivityCard(activity: CalendarActivity) {
    val isDark = isSystemInDarkTheme()
    val diasRestantes = activity.diasRestantes()
    val proximidad = activity.proximidad()
    val dotColor = proximidad.proximidadColor()

    val daysText = when {
        diasRestantes == 0L -> "Hoy"
        diasRestantes == 1L -> "Mañana"
        else -> "En $diasRestantes días"
    }

    val (bgColor, iconBg, iconRes) = when (activity.tipo) {
        "EVALUACION" -> Triple(
            if (isDark) DarkEventParcialBg else EventParcialBg,
            if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935), R.drawable.document
        )
        "TAREA" -> Triple(
            if (isDark) DarkEventTareasBg else EventTareasBg,
            if (isDark) Color(0xFF90CAF9) else Color(0xFF1E88E5), R.drawable.list
        )
        "ENTREGA" -> Triple(
            if (isDark) DarkEventosBg else EventosBg,
            if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047), R.drawable.calendar
        )
        "ACTIVIDAD" -> Triple(
            if (isDark) DarkEventTareasBg else EventTareasBg,
            if (isDark) Color(0xFFCE93D8) else Color(0xFF7B1FA2), R.drawable.calendar
        )
        else -> Triple(
            if (isDark) DarkEventParcialBg else EventParcialBg,
            Color(0xFF2C2C2C), R.drawable.document
        )
    }

    val titleColor = when (activity.tipo) {
        "EVALUACION" -> if (isDark) DarkOnSurface else Color(0xFFC62828)
        "TAREA" -> if (isDark) DarkOnSurface else Color(0xFF1565C0)
        "ENTREGA" -> if (isDark) DarkOnSurface else Color(0xFF2E7D32)
        "ACTIVIDAD" -> if (isDark) DarkOnSurface else Color(0xFF7B1FA2)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = activity.tipo,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                val tipoLabel = when (activity.tipo) {
                    "EVALUACION" -> "Parcial"
                    "TAREA" -> "Tarea"
                    "ENTREGA" -> "Evento"
                    "ACTIVIDAD" -> "Actividad"
                    else -> activity.tipo
                }
                Text(
                    text = "$daysText — ${activity.fecha.toFormattedTime()} — $tipoLabel" +
                            if (!activity.asignaturaNombre.isNullOrBlank()) " — ${activity.asignaturaNombre}" else "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun Long.toFormattedTime(): String {
    val time = java.time.Instant.ofEpochMilli(this)
        .atZone(java.time.ZoneId.systemDefault()).toLocalTime()
    val hour = time.hour
    val minute = time.minute
    val amPm = if (hour < 12) "AM" else "PM"
    val h12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return "%02d:%02d %s".format(h12, minute, amPm)
}

private fun CalendarActivity.proximidad(): Proximidad {
    val now = System.currentTimeMillis()
    val diff = fecha - now
    val dosDias = 2L * 24 * 60 * 60 * 1000
    val sieteDias = 7L * 24 * 60 * 60 * 1000
    return when {
        diff <= dosDias -> Proximidad.CERCANO
        diff <= sieteDias -> Proximidad.PROXIMO
        else -> Proximidad.LEJANO
    }
}

private fun CalendarActivity.diasRestantes(): Long {
    val today = LocalDate.now()
    val actDate = java.time.Instant.ofEpochMilli(fecha)
        .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    return java.time.temporal.ChronoUnit.DAYS.between(today, actDate)
}

private fun Proximidad.proximidadColor(): Color = when (this) {
    Proximidad.CERCANO -> Color(0xFFF44336)
    Proximidad.PROXIMO -> Color(0xFFFFC107)
    Proximidad.LEJANO -> Color(0xFF4CAF50)
}
