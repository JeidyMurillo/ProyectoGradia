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
    creditHours = creditos,
    semester = semestre,
    professor = profesor,
    classroom = salon
)

fun Subject.toEntity(userId: String): Asignatura = Asignatura(
    id = id,
    userId = userId,
    nombre = name,
    icono = icon,
    profesor = professor,
    salon = classroom,
    creditos = creditHours,
    semestre = semester,
    notaAprobacion = passingGrade.toFloat()
)

fun Nota.toDomain(): GradeItem = GradeItem(
    id = id,
    subjectId = asignaturaId,
    name = nombre,
    percentage = porcentaje.toDouble(),
    grade = valor?.toDouble(),
    icon = icono
)

fun GradeItem.toEntity(): Nota = Nota(
    id = id,
    asignaturaId = subjectId,
    nombre = name,
    valor = grade?.toFloat(),
    porcentaje = percentage.toFloat(),
    icono = icon
)
