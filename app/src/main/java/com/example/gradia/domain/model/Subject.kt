package com.example.gradia.domain.model

data class Subject(
    val id: Long,
    val name: String,
    val icon: String = "\uD83D\uDCDA",
    val passingGrade: Double = 3.0,
    val creditHours: Int = 0,
    val semester: Int = 1,
    val professor: String = "",
    val classroom: String = ""
)
