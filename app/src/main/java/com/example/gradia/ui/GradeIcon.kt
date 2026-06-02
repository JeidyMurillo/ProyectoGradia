package com.example.gradia.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.example.gradia.R

enum class GradeIconType { Examen, Actividad, Final, General }

fun gradeIconType(name: String): GradeIconType = when {
    name.contains("final", ignoreCase = true) -> GradeIconType.Final
    listOf("taller", "tarea", "quiz", "laboratorio", "proyecto")
        .any { name.contains(it, ignoreCase = true) } -> GradeIconType.Actividad
    listOf("parcial", "examen")
        .any { name.contains(it, ignoreCase = true) } -> GradeIconType.Examen
    else -> GradeIconType.General
}

fun gradeIconStringFor(type: GradeIconType): String = when (type) {
    GradeIconType.Examen -> "examen"
    GradeIconType.Actividad -> "actividad"
    GradeIconType.Final -> "final"
    GradeIconType.General -> "general"
}

fun gradeIconTypeFromString(icon: String): GradeIconType = when (icon) {
    "examen" -> GradeIconType.Examen
    "actividad" -> GradeIconType.Actividad
    "final" -> GradeIconType.Final
    else -> GradeIconType.General
}

fun gradeIconLabel(type: GradeIconType): String = when (type) {
    GradeIconType.Examen -> "Examen / Parcial"
    GradeIconType.Actividad -> "Taller / Tarea / Quiz"
    GradeIconType.Final -> "Final"
    GradeIconType.General -> "General"
}

fun resolveGradeIcon(savedIcon: String, name: String): GradeIconType =
    if (savedIcon.isNotBlank()) gradeIconTypeFromString(savedIcon) else gradeIconType(name)

@Composable
fun GradeIconRender(type: GradeIconType, tint: Color, size: Dp) {
    when (type) {
        GradeIconType.Examen -> Icon(
            painter = painterResource(id = R.drawable.document),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
        GradeIconType.Actividad -> Icon(
            painter = painterResource(id = R.drawable.list),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
        GradeIconType.Final -> Icon(
            painter = painterResource(id = R.drawable.calendar),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
        GradeIconType.General -> Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}
