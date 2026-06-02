package com.example.gradia.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GradeItemTest {

    @Test
    fun `default grade is null`() {
        val item = GradeItem(id = 1, subjectId = 1, name = "Test", percentage = 100.0)

        assertNull(item.grade)
    }

    @Test
    fun `default icon is empty string`() {
        val item = GradeItem(id = 1, subjectId = 1, name = "Test", percentage = 50.0)

        assertEquals("", item.icon)
    }

    @Test
    fun `all fields are set correctly`() {
        val item = GradeItem(
            id = 5,
            subjectId = 10,
            name = "Examen Final",
            percentage = 35.0,
            grade = 4.5,
            icon = "📝"
        )

        assertEquals(5, item.id)
        assertEquals(10, item.subjectId)
        assertEquals("Examen Final", item.name)
        assertEquals(35.0, item.percentage, 0.01)
        assertEquals(4.5, item.grade!!, 0.01)
        assertEquals("📝", item.icon)
    }
}
