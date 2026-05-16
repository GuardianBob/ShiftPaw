package com.example.shiftpaw.domain.model

data class Employee(
    val id: Long = 0,
    val name: String,
    val color: String = "#607D8B", // default Material Blue Grey
    val isActive: Boolean = true
)
