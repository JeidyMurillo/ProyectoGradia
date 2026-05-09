package com.example.gradia.domain.usecase

import com.example.gradia.domain.model.GradeItem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateCurrentAverageUseCaseTest {

    private lateinit var useCase: CalculateCurrentAverageUseCase

    @Before
    fun setUp() {
        useCase = CalculateCurrentAverageUseCase()
    }

    @Test
    fun `returns 0 when no activities have grades`() {
        val items = listOf(
            GradeItem(1, 1, "Examen 1", 30.0, null),
            GradeItem(2, 1, "Examen 2", 30.0, null)
        )

        val result = useCase(items)

        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun `calculates weighted average correctly`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 25.0, 3.5),
            GradeItem(2, 1, "Parcial 2", 25.0, 2.8),
            GradeItem(3, 1, "Tareas", 15.0, 4.0)
        )

        val result = useCase(items)

        val expected = ((3.5 * 25.0) + (2.8 * 25.0) + (4.0 * 15.0)) / (25.0 + 25.0 + 15.0)
        assertEquals(roundToDecimal(expected), result, 0.01)
    }

    @Test
    fun `ignores activities with null grade`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 25.0, 4.0),
            GradeItem(2, 1, "Parcial 2", 25.0, null),
            GradeItem(3, 1, "Final", 50.0, null)
        )

        val result = useCase(items)

        assertEquals(4.0, result, 0.01)
    }

    @Test
    fun `ignores activities with grade 0`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 25.0, 4.0),
            GradeItem(2, 1, "Parcial 2", 25.0, 0.0)
        )

        val result = useCase(items)

        assertEquals(4.0, result, 0.01)
    }

    @Test
    fun `rounds result to 1 decimal`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 33.0, 3.3),
            GradeItem(2, 1, "Parcial 2", 33.0, 3.7)
        )

        val result = useCase(items)

        assertEquals(result * 10, kotlin.math.round(result * 10), 0.01)
    }

    @Test
    fun `calculates for 100 percent graded`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 30.0, 4.0),
            GradeItem(2, 1, "Parcial 2", 30.0, 3.0),
            GradeItem(3, 1, "Final", 40.0, 5.0)
        )

        val result = useCase(items)

        val expected = (4.0 * 30.0 + 3.0 * 30.0 + 5.0 * 40.0) / 100.0
        assertEquals(roundToDecimal(expected), result, 0.01)
    }

    private fun roundToDecimal(value: Double): Double {
        return kotlin.math.round(value * 10) / 10
    }
}
