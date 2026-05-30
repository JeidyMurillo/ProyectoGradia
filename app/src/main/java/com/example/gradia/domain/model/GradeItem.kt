package com.example.gradia.domain.model

data class GradeItem(
    val id: Long,
    val subjectId: Long,
    val name: String,
    val percentage: Double,
    val grade: Double? = null,
    val icon: String = ""
)
