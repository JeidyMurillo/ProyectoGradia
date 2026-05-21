package com.example.gradia.domain.model

data class Category(
    val id: Long = 0,
    val userId: String = "",
    val name: String,
    val color: Long = 0xFFD0EFFF
)
