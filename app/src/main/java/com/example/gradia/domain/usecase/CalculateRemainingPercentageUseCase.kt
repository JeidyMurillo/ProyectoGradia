package com.example.gradia.domain.usecase

import com.example.gradia.domain.model.GradeItem

class CalculateRemainingPercentageUseCase {

    operator fun invoke(gradeItems: List<GradeItem>): Double {
        val evaluatedPercentage = gradeItems
            .filter { it.grade != null }
            .sumOf { it.percentage }
        return (100.0 - evaluatedPercentage).coerceAtLeast(0.0)
    }
}
