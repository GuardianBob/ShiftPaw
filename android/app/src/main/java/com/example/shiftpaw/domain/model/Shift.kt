package com.example.shiftpaw.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Shift(
    val id: Long = 0,
    val employeeId: Long,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val shiftType: ShiftType,
    val notes: String = ""
)

enum class ShiftType {
    DAY,       // morning/day shift
    EVENING,   // evening shift
    NIGHT,     // overnight shift
    ON_CALL,   // on-call
    OFF        // day off (explicitly recorded)
}
