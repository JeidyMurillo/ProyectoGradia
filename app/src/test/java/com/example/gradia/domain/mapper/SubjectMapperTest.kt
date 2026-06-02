package com.example.gradia.domain.mapper

import com.example.gradia.data.local.entity.Asignatura
import com.example.gradia.data.local.entity.Nota
import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject
import org.junit.Assert.assertEquals
import org.junit.Test

class SubjectMapperTest {

    @Test
    fun `Asignatura to Subject maps correctly`() {
        val entity = Asignatura(
            id = 1,
            userId = "user1",
            nombre = "Calculo IV",
            icono = "📐",
            profesor = "Dr. Perez",
            salon = "A101",
            creditos = 4,
            semestre = 4,
            notaAprobacion = 3.0f
        )

        val domain = entity.toDomain()

        assertEquals(1, domain.id)
        assertEquals("Calculo IV", domain.name)
        assertEquals("📐", domain.icon)
        assertEquals("Dr. Perez", domain.professor)
        assertEquals("A101", domain.classroom)
        assertEquals(4, domain.creditHours)
        assertEquals(4, domain.semester)
        assertEquals(3.0, domain.passingGrade, 0.01)
    }

    @Test
    fun `Subject to Asignatura maps correctly`() {
        val subject = Subject(
            id = 2,
            name = "Fisica II",
            icon = "⚛️",
            professor = "Dra. Lopez",
            classroom = "B202",
            creditHours = 3,
            semester = 3,
            passingGrade = 3.5
        )

        val entity = subject.toEntity("user1")

        assertEquals(2, entity.id)
        assertEquals("user1", entity.userId)
        assertEquals("Fisica II", entity.nombre)
        assertEquals("⚛️", entity.icono)
        assertEquals("Dra. Lopez", entity.profesor)
        assertEquals("B202", entity.salon)
        assertEquals(3, entity.creditos)
        assertEquals(3, entity.semestre)
        assertEquals(3.5f, entity.notaAprobacion)
    }

    @Test
    fun `Nota to GradeItem maps correctly`() {
        val nota = Nota(
            id = 1,
            asignaturaId = 5,
            nombre = "Parcial 1",
            valor = 4.5f,
            porcentaje = 25.0f,
            icono = "📝"
        )

        val gradeItem = nota.toDomain()

        assertEquals(1, gradeItem.id)
        assertEquals(5, gradeItem.subjectId)
        assertEquals("Parcial 1", gradeItem.name)
        assertEquals(4.5, gradeItem.grade!!, 0.01)
        assertEquals(25.0, gradeItem.percentage, 0.01)
        assertEquals("📝", gradeItem.icon)
    }

    @Test
    fun `Nota with null grade maps to GradeItem with null grade`() {
        val nota = Nota(
            id = 2,
            asignaturaId = 3,
            nombre = "Final",
            valor = null,
            porcentaje = 40.0f,
            icono = ""
        )

        val gradeItem = nota.toDomain()

        assertEquals(null, gradeItem.grade)
        assertEquals(40.0, gradeItem.percentage, 0.01)
    }

    @Test
    fun `GradeItem to Nota maps correctly`() {
        val gradeItem = GradeItem(
            id = 3,
            subjectId = 7,
            name = "Laboratorio",
            percentage = 15.0,
            grade = 4.2,
            icon = "🔬"
        )

        val nota = gradeItem.toEntity()

        assertEquals(3, nota.id)
        assertEquals(7, nota.asignaturaId)
        assertEquals("Laboratorio", nota.nombre)
        assertEquals(4.2f, nota.valor!!)
        assertEquals(15.0f, nota.porcentaje)
        assertEquals("🔬", nota.icono)
    }
}
