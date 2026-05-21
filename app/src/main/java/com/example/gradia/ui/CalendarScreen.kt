package com.example.gradia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

data class EventoUI(
    val id: String,
    val titulo: String,
    val tipo: String, // "PARCIAL", "TAREA", "EVENTO"
    val descripcion: String,
    val fecha: LocalDate
)

@Composable
fun CalendarScreen() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Mock data - reemplazar con datos reales del ViewModel
    val eventosHoy = listOf(
        EventoUI("1", "Parciales próximos", "PARCIAL", "Parcial ADA 2-Mar 09", LocalDate.now()),
        EventoUI("2", "Tareas pendientes", "TAREA", "5 tareas sin entregar\n2 tareas vencen mañana", LocalDate.now()),
        EventoUI("3", "Eventos", "EVENTO", "Reunión hoy 4:00 pm", LocalDate.now())
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalendarBg)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Calendar Card
        CalendarCard(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            onMonthChange = { currentMonth = it },
            onDateSelect = { selectedDate = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // "Eventos para hoy" section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Eventos para hoy",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PurpleGradia,
                    fontSize = 16.sp,
                    fontFamily = InterFontFamily
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Eventos cards
            eventosHoy.forEach { evento ->
                EventoCard(evento)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun CalendarCard(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelect: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, PurpleGradia),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Month and Year selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { onMonthChange(currentMonth.minusMonths(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous month",
                        tint = PurpleGradia
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Month dropdown
                Text(
                    text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4A4A4A),
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.clickable { /* Show month picker */ }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Year dropdown
                Text(
                    text = currentMonth.year.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4A4A4A),
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.clickable { /* Show year picker */ }
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = { onMonthChange(currentMonth.plusMonths(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next month",
                        tint = PurpleGradia
                    )
                }
            }

            // Day headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9E9E9E),
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Calendar grid
            val firstDay = currentMonth.atDay(1)
            val daysInMonth = currentMonth.lengthOfMonth()
            val startingDayOfWeek = firstDay.dayOfWeek.value % 7 // 0 = Sunday

            val weeks = mutableListOf<List<Int?>>()
            var currentWeek = MutableList(7) { null as Int? }
            var dayIndex = 0

            // Fill starting empty days
            for (i in startingDayOfWeek until 7) {
                currentWeek[i] = ++dayIndex
            }
            weeks.add(currentWeek)

            // Fill remaining days
            while (dayIndex < daysInMonth) {
                currentWeek = MutableList(7) { null }
                for (i in 0 until 7) {
                    if (dayIndex < daysInMonth) {
                        currentWeek[i] = ++dayIndex
                    }
                }
                weeks.add(currentWeek)
            }

            // Draw weeks
            weeks.forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    week.forEach { day ->
                        if (day != null) {
                            val date = currentMonth.atDay(day)
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> PurpleGradia
                                            isToday -> Color(0xFF7B39A3).copy(alpha = 0.2f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDateSelect(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = when {
                                            isSelected -> Color.White
                                            day < 10 -> PurpleGradia
                                            else -> Color(0xFF9E9E9E)
                                        },
                                        fontFamily = InterFontFamily,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        } else {
                            Box(modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventoCard(evento: EventoUI) {
    val (bgColor, iconBg, textColor) = when (evento.tipo) {
        "PARCIAL" -> Triple(EventParcialBg, Color(0xFF8B4513), Color(0xFF5D2C14))
        "TAREA" -> Triple(EventTareasBg, Color(0xFF1565C0), Color(0xFF0D47A1))
        "EVENTO" -> Triple(EventosBg, Color(0xFF2E7D32), Color(0xFF1B5E20))
        else -> Triple(EventParcialBg, Color(0xFF2C2C2C), Color(0xFF4A4A4A))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Icon background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = when (evento.tipo) {
                        "PARCIAL" -> painterResource(id = R.drawable.document)
                        "TAREA" -> painterResource(id = R.drawable.list)
                        "EVENTO" -> painterResource(id = R.drawable.calendar)
                        else -> painterResource(id = R.drawable.document)
                    },
                    contentDescription = evento.tipo,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = evento.descripcion,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF666666),
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp
                    ),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
