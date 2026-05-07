package com.example.gradia.domain.mapper

import com.example.gradia.data.local.entity.Asignatura
import com.example.gradia.data.local.entity.Nota
import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject

fun Asignatura.toDomain(): Subject = Subject(
    id = id,
    name = nombre,
    icon = icono,
    passingGrade = notaAprobacion.toDouble(),
    creditHours = creditos
)

fun Nota.toDomain(): GradeItem = GradeItem(
    id = id,
    subjectId = asignaturaId,
    name = nombre,
    percentage = porcentaje.toDouble(),
    grade = valor.toDouble()
)

fun GradeItem.toEntity(): Nota = Nota(
    id = id,
    asignaturaId = subjectId,
    nombre = name,
    valor = grade?.toFloat() ?: 0f,
    porcentaje = percentage.toFloat()
)
