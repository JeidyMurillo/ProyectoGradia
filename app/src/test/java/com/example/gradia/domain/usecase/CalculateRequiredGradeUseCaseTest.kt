package com.example.gradia.domain.usecase

import com.example.gradia.domain.model.GradeItem
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalculateRequiredGradeUseCaseTest {

    private lateinit var currentAverageUseCase: CalculateCurrentAverageUseCase
    private lateinit var remainingPercentageUseCase: CalculateRemainingPercentageUseCase
    private lateinit var useCase: CalculateRequiredGradeUseCase

    @Before
    fun setUp() {
        currentAverageUseCase = CalculateCurrentAverageUseCase()
        remainingPercentageUseCase = CalculateRemainingPercentageUseCase()
        useCase = CalculateRequiredGradeUseCase(currentAverageUseCase, remainingPercentageUseCase)
    }

    @Test
    fun `returns Success with correct grade`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 25.0, 3.5),
            GradeItem(2, 1, "Parcial 2", 25.0, 2.8),
            GradeItem(3, 1, "Tareas", 15.0, 4.0),
            GradeItem(4, 1, "Examen Final", 35.0, null)
        )

        val result = useCase(items, 3.5)

        assertTrue(result is RequiredGradeResult.Success)
    }

    @Test
    fun `calculates required grade for Calculo IV scenario`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 25.0, 3.5),
            GradeItem(2, 1, "Parcial 2", 25.0, 2.8),
            GradeItem(3, 1, "Tareas", 15.0, 4.0),
            GradeItem(4, 1, "Examen Final", 35.0, null)
        )

        val result = useCase(items, 3.5) as RequiredGradeResult.Success

        val weightedSum = 3.5 * 25.0 + 2.8 * 25.0 + 4.0 * 15.0
        val remaining = 35.0
        val expected = (3.5 * 100.0 - weightedSum) / remaining

        assertEquals(expected, result.grade, 0.15)
    }

    @Test
    fun `returns AlreadyAchieved when current average meets target`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 40.0, 4.5),
            GradeItem(2, 1, "Parcial 2", 40.0, 4.2),
            GradeItem(3, 1, "Final", 20.0, null)
        )

        val result = useCase(items, 3.0)

        assertEquals(RequiredGradeResult.AlreadyAchieved, result)
    }

    @Test
    fun `returns Impossible when required grade exceeds 5`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 10.0, 1.0),
            GradeItem(2, 1, "Parcial 2", 10.0, 1.5),
            GradeItem(3, 1, "Final", 80.0, null)
        )

        val result = useCase(items, 4.5)

        assertEquals(RequiredGradeResult.Impossible, result)
    }

    @Test
    fun `returns NoRemainingPercentage when 100 percent evaluated`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 30.0, 3.0),
            GradeItem(2, 1, "Parcial 2", 30.0, 4.0),
            GradeItem(3, 1, "Final", 40.0, 5.0)
        )

        val result = useCase(items, 5.0)

        assertEquals(RequiredGradeResult.NoRemainingPercentage, result)
    }

    @Test
    fun `returns AlreadyAchieved for Fisica II when average exceeds target`() {
        val items = listOf(
            GradeItem(1, 2, "Parcial 1", 30.0, 4.2),
            GradeItem(2, 2, "Parcial 2", 30.0, 3.8),
            GradeItem(3, 2, "Laboratorios", 20.0, 4.5),
            GradeItem(4, 2, "Examen Final", 20.0, null)
        )

        val result = useCase(items, 4.0)

        assertEquals(RequiredGradeResult.AlreadyAchieved, result)
    }

    @Test
    fun `rounds required grade to 1 decimal`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 20.0, 3.3),
            GradeItem(2, 1, "Parcial 2", 20.0, 3.7),
            GradeItem(3, 1, "Final", 60.0, null)
        )

        val result = useCase(items, 4.0) as RequiredGradeResult.Success

        val rounded = kotlin.math.round(result.grade * 10) / 10
        assertEquals(rounded, result.grade, 0.01)
    }

    @Test
    fun `higher target requires higher grade`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 20.0, 3.0),
            GradeItem(2, 1, "Parcial 2", 20.0, 3.5),
            GradeItem(3, 1, "Examen Final", 60.0, null)
        )

        val resultLow = useCase(items, 3.5) as RequiredGradeResult.Success
        val resultHigh = useCase(items, 4.0) as RequiredGradeResult.Success

        assertTrue(resultLow.grade < resultHigh.grade)
    }

    @Test
    fun `handles edge case target 0`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 50.0, 2.0),
            GradeItem(2, 1, "Final", 50.0, null)
        )

        val result = useCase(items, 0.0)

        assertEquals(RequiredGradeResult.AlreadyAchieved, result)
    }

    @Test
    fun `returns Success with realistic grade for Calculo IV`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 25.0, 3.5),
            GradeItem(2, 1, "Parcial 2", 25.0, 2.8),
            GradeItem(3, 1, "Tareas", 15.0, 4.0),
            GradeItem(4, 1, "Examen Final", 35.0, null)
        )

        val result = useCase(items, 3.5) as RequiredGradeResult.Success

        assertTrue(result.grade > 0 && result.grade <= 5.0)
    }
}
