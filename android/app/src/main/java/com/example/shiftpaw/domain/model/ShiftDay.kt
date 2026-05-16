package com.example.shiftpaw.domain.model

import java.time.LocalDate

/** A single calendar day's aggregated view: which employees are working. */
data class ShiftDay(
    val date: LocalDate,
    val shifts: List<Shift>
) {
    val hasShifts: Boolean get() = shifts.isNotEmpty()
}
