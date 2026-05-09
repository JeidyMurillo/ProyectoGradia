package com.example.gradia.domain.usecase

import com.example.gradia.domain.model.GradeItem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateRemainingPercentageUseCaseTest {

    private lateinit var useCase: CalculateRemainingPercentageUseCase

    @Before
    fun setUp() {
        useCase = CalculateRemainingPercentageUseCase()
    }

    @Test
    fun `returns 100 when no activities exist`() {
        val items = emptyList<GradeItem>()

        val result = useCase(items)

        assertEquals(100.0, result, 0.01)
    }

    @Test
    fun `returns correct remaining percentage`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 25.0, 4.0),
            GradeItem(2, 1, "Parcial 2", 25.0, 3.5),
            GradeItem(3, 1, "Tareas", 15.0, 4.0),
            GradeItem(4, 1, "Final", 35.0, null)
        )

        val result = useCase(items)

        assertEquals(35.0, result, 0.01)
    }

    @Test
    fun `returns 0 when all activities have grades`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 30.0, 4.0),
            GradeItem(2, 1, "Parcial 2", 30.0, 3.5),
            GradeItem(3, 1, "Final", 40.0, 5.0)
        )

        val result = useCase(items)

        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun `only counts graded activities toward evaluated percentage`() {
        val items = listOf(
            GradeItem(1, 1, "Parcial 1", 25.0, 4.0),
            GradeItem(2, 1, "Final", 75.0, null)
        )

        val result = useCase(items)

        assertEquals(75.0, result, 0.01)
    }
}
