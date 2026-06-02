package com.example.gradia.domain.usecase

import com.example.gradia.domain.model.GradeItem

class CalculateCurrentAverageUseCase {

    operator fun invoke(gradeItems: List<GradeItem>): Double {
        val gradedItems = gradeItems.filter { it.grade != null }

        if (gradedItems.isEmpty()) return 0.0

        val weightedSum = gradedItems.sumOf { (it.grade!! * it.percentage) }
        val totalCoursePercentage = gradeItems.sumOf { it.percentage }

        return if (totalCoursePercentage > 0) {
            roundToDecimal(weightedSum / totalCoursePercentage)
        } else {
            0.0
        }
    }

    private fun roundToDecimal(value: Double): Double {
        return kotlin.math.round(value * 10) / 10
    }
}
