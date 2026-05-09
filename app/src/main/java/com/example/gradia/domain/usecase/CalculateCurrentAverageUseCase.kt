package com.example.gradia.domain.usecase

import com.example.gradia.domain.model.GradeItem

class CalculateCurrentAverageUseCase {

    operator fun invoke(gradeItems: List<GradeItem>): Double {
        val gradedItems = gradeItems.filter { it.grade != null && it.grade!! > 0 }

        if (gradedItems.isEmpty()) return 0.0

        val weightedSum = gradedItems.sumOf { (it.grade!! * it.percentage) }
        val totalPercentage = gradedItems.sumOf { it.percentage }

        return if (totalPercentage > 0) {
            roundToDecimal(weightedSum / totalPercentage)
        } else {
            0.0
        }
    }

    private fun roundToDecimal(value: Double): Double {
        return kotlin.math.round(value * 10) / 10
    }
}
