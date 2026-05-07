package com.example.gradia.domain.usecase

import com.example.gradia.domain.model.GradeItem

sealed class RequiredGradeResult {
    data class Success(val grade: Double) : RequiredGradeResult()
    data object AlreadyAchieved : RequiredGradeResult()
    data object Impossible : RequiredGradeResult()
    data object NoRemainingPercentage : RequiredGradeResult()
}

class CalculateRequiredGradeUseCase(
    private val calculateCurrentAverage: CalculateCurrentAverageUseCase,
    private val calculateRemainingPercentage: CalculateRemainingPercentageUseCase
) {

    operator fun invoke(
        gradeItems: List<GradeItem>,
        targetGrade: Double
    ): RequiredGradeResult {
        val currentAverage = calculateCurrentAverage(gradeItems)
        val remainingPercentage = calculateRemainingPercentage(gradeItems)

        val gradedItems = gradeItems.filter { it.grade != null && it.grade!! > 0 }
        val currentWeightedSum = gradedItems.sumOf { it.grade!! * it.percentage }

        return when {
            remainingPercentage <= 0 -> {
                if (currentAverage >= targetGrade) RequiredGradeResult.AlreadyAchieved
                else RequiredGradeResult.NoRemainingPercentage
            }
            currentAverage >= targetGrade -> RequiredGradeResult.AlreadyAchieved
            else -> {
                val required = (targetGrade * 100.0 - currentWeightedSum) / remainingPercentage
                val rounded = roundToDecimal(required)

                when {
                    rounded > 5.0 -> RequiredGradeResult.Impossible
                    rounded <= 0.0 -> RequiredGradeResult.AlreadyAchieved
                    else -> RequiredGradeResult.Success(rounded)
                }
            }
        }
    }

    private fun roundToDecimal(value: Double): Double {
        return kotlin.math.round(value * 10) / 10
    }
}
