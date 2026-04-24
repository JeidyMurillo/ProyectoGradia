package com.example.gradia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.*

@Composable
fun TasksScreen(onBackClick: () -> Unit = {}) {
    // Only content, Scaffold is in HomeScreen
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Crear Tarea:",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = InterFontFamily,
                    color = Color(0xFF4A4A4A)
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            CreateTaskCard()
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color(0xFFE9E4F0), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add_gray),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        item {
            SectionHeader(title = "Hoy", count = "2 Tareas", color = PurpleGradia)
        }

        item {
            TaskCard(
                title = "Subir avance de proyecto (Flex-IA)",
                urgency = "URGENTE",
                urgencyColor = Color(0xFFFF8A80),
                time = "Hoy, 11:59 pm",
                subject = "Programación"
            )
        }

        item {
            TaskCard(
                title = "Reunión con grupo de trabajo: Proyecto Flex-IA",
                urgency = "MEDIO",
                urgencyColor = Color(0xFFFFF176),
                time = "4:30 pm"
            )
        }

        item {
            SectionHeader(title = "Próximamente", color = Color.Gray)
        }

        item {
            TaskCard(
                title = "Entrega Final Proyecto Alexandria",
                urgency = "MEDIO",
                urgencyColor = Color(0xFFFFF176)
            )
        }

        item {
            SectionHeader(title = "Completadas", isCompleted = true, color = Color(0xFF453284))
        }

        item {
            TaskCard(
                title = "Reunión con grupo de trabajo: Proyecto Alexandria",
                urgency = "BAJO",
                urgencyColor = Color(0xFFA5D6A7),
                time = "9:30 am",
                isCompleted = true
            )
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun CreateTaskCard() {
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
                Column {
                    Text(
                        "Nombre del evento",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF453284)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fecha", color = Color.LightGray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.graduation_cap_gray),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Asignatura", color = Color.LightGray, fontSize = 14.sp)
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

@Composable
fun TaskCard(
    title: String,
    urgency: String,
    urgencyColor: Color,
    time: String? = null,
    subject: String? = null,
    isCompleted: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCompleted) Color(0xFFF9F9F9) else Color(0xFFFDFBFF)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
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
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(PurpleGradia, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .border(2.dp, PurpleGradia, CircleShape)
                    )
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), // Thinner padding
            fontSize = 9.sp, // Slightly smaller font for a thinner look
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TasksScreenPreview() {
    GradiaTheme {
        TasksScreen()
    }
}
