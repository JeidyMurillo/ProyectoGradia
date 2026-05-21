package com.example.gradia.domain.model

data class Note(
    val id: Long = 0,
    val userId: String = "",
    val title: String,
    val content: String,
    val color: Long = 0xFFFFE0E0,
    val categories: List<Category> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
