package com.example.shiftpaw.data.parser

import com.example.shiftpaw.data.local.entity.ShiftEntity
import com.example.shiftpaw.domain.model.ShiftType
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.InputStream
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses VetCalendar schedule DOCX files into [ShiftEntity] records.
 *
 * Document format (per DOCX_PARSER_SPEC.md):
 * - 14 columns in paired layout (col 0,2,4... = date numbers; col 1,3,5... = employee initials)
 * - Header row: weekday names (Monday..Sunday) — skip
 * - Date row: ≥2 numeric cells — sets current date mapping
 * - Shift row: first cell is a known shift name (Day, Swing 1, Swing 2, Night)
 *
 * Employee initials (e.g. "JD", "MS") are used as names.
 * Temp IDs are 1-based sequential, matching index in [ParseResult.employeeNames].
 */
@Singleton
class DocxScheduleParser @Inject constructor() {

    data class ParseResult(
        val shifts: List<ShiftEntity>,
        /** Schedule month as "yyyy-MM" */
        val scheduleMonth: String,
        /** Unique employee names/initials in order of first appearance. Index+1 = tempId. */
        val employeeNames: List<String>,
        val errors: List<String>
    )

    companion object {
        private val SHIFT_TIMES = mapOf(
            "day"     to Pair("07:00", "19:00"),
            "swing 1" to Pair("10:00", "22:00"),
            "swing1"  to Pair("10:00", "22:00"),
            "swing 2" to Pair("14:00", "02:00"),
            "swing2"  to Pair("14:00", "02:00"),
            "night"   to Pair("18:00", "06:00"),
        )
        private val SHIFT_TYPE_MAP = mapOf(
            "day"     to ShiftType.DAY,
            "swing 1" to ShiftType.EVENING,
            "swing1"  to ShiftType.EVENING,
            "swing 2" to ShiftType.ON_CALL,
            "swing2"  to ShiftType.ON_CALL,
            "night"   to ShiftType.NIGHT,
        )
        private val DAY_NAMES = setOf(
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"
        )
        private val MONTH_NAMES = mapOf(
            "january" to "01", "february" to "02", "march" to "03", "april" to "04",
            "may" to "05", "june" to "06", "july" to "07", "august" to "08",
            "september" to "09", "october" to "10", "november" to "11", "december" to "12",
            // Hungarian equivalents (from task spec)
            "januar" to "01", "januar\u00e1s" to "01",
            "februar" to "02", "febru\u00e1r" to "02",
            "marcius" to "03", "m\u00e1rcius" to "03",
            "aprilis" to "04", "\u00e1prilis" to "04",
            "majus" to "05", "m\u00e1jus" to "05",
            "junius" to "06", "j\u00fanius" to "06",
            "julius" to "07", "j\u00falius" to "07",
            "augusztus" to "08",
            "szeptember" to "09",
            "oktober" to "10", "okt\u00f3ber" to "10",
            "november" to "11",
            "december" to "12",
        )
    }

    fun parse(inputStream: InputStream, fileName: String): ParseResult {
        val errors = mutableListOf<String>()
        val shifts = mutableListOf<ShiftEntity>()
        // Track unique employee initials in first-encounter order
        val employeeOrder = mutableListOf<String>()   // index+1 = tempId
        val employeeTempIds = mutableMapOf<String, Long>() // initials -> tempId

        val scheduleMonth = parseScheduleMonthFromFileName(fileName, errors)
        val yearStr = scheduleMonth.substringBefore("-")
        val monthStr = scheduleMonth.substringAfter("-")

        try {
            val doc = XWPFDocument(inputStream)
            val tables = doc.tables

            if (tables.isEmpty()) {
                return ParseResult(emptyList(), scheduleMonth, emptyList(),
                    listOf("No tables found in document"))
            }

            for (table in tables) {
                val maxCols = table.rows.maxOfOrNull { it.tableCells.size } ?: 0
                // Must have at least 8 columns and an even number (shift rows have 14 cells)
                if (maxCols < 8 || maxCols % 2 != 0) {
                    errors.add("Skipped table with $maxCols columns (expected even ≥ 8)")
                    continue
                }
                val numDayColumns = maxCols / 2
                // currentDates maps col-index (0..numDayColumns-1) -> day-of-month string
                val currentDates = mutableMapOf<Int, String>()

                for (row in table.rows) {
                    val cells = row.tableCells.map { it.text.trim() }
                    if (cells.all { it.isEmpty() }) continue

                    when {
                        isDayHeaderRow(cells) -> {
                            currentDates.clear()
                        }
                        isDateRow(cells) -> {
                            currentDates.clear()
                            // Date rows have 7 cells (one per weekday col), indexed directly
                            for (colIdx in 0 until numDayColumns) {
                                if (colIdx < cells.size) {
                                    val cellVal = cells[colIdx].trim()
                                    if (cellVal.all { it.isDigit() } && cellVal.isNotEmpty()) {
                                        val dayNum = cellVal.toInt()
                                        try {
                                            LocalDate.of(yearStr.toInt(), monthStr.toInt(), dayNum)
                                            currentDates[colIdx] = cellVal
                                        } catch (_: Exception) {
                                            // Invalid day for this month — skip silently
                                        }
                                    }
                                }
                            }
                        }
                        isShiftRow(cells) -> {
                            val shiftName = cells[0].trim().lowercase()
                            val (startTime, endTime) = SHIFT_TIMES[shiftName] ?: ("07:00" to "19:00")
                            val shiftType = SHIFT_TYPE_MAP[shiftName] ?: ShiftType.DAY

                            for (colIdx in 0 until numDayColumns) {
                                val dateStr = currentDates[colIdx] ?: continue
                                val employeeCellPos = colIdx * 2 + 1
                                if (employeeCellPos >= cells.size) continue
                                val initials = cells[employeeCellPos].trim()
                                if (initials.isEmpty()) continue

                                // Resolve temp ID
                                val tempId = employeeTempIds.getOrPut(initials) {
                                    employeeOrder.add(initials)
                                    employeeOrder.size.toLong()
                                }

                                try {
                                    val date = LocalDate.of(
                                        yearStr.toInt(), monthStr.toInt(), dateStr.toInt()
                                    )
                                    shifts.add(
                                        ShiftEntity(
                                            employeeId = tempId,
                                            date = date.toString(),
                                            startTime = startTime,
                                            endTime = endTime,
                                            shiftType = shiftType.name,
                                            sourceScheduleDate = scheduleMonth
                                        )
                                    )
                                } catch (e: Exception) {
                                    errors.add("Skipped invalid shift date $yearStr-$monthStr-$dateStr: ${e.message}")
                                }
                            }
                        }
                        // else: unknown row type, skip
                    }
                }
            }

            doc.close()
        } catch (e: Exception) {
            errors.add("Parse failed: ${e.message}")
        }

        return ParseResult(shifts, scheduleMonth, employeeOrder.toList(), errors)
    }

    // ── Row classification ────────────────────────────────────────────────────

    private fun isDayHeaderRow(cells: List<String>): Boolean {
        var matches = 0
        for (cell in cells) {
            val lower = cell.lowercase().trim()
            if (DAY_NAMES.any { it in lower }) matches++
        }
        return matches >= 6
    }

    private fun isDateRow(cells: List<String>): Boolean {
        var numericCount = 0
        for (cell in cells) {
            val t = cell.trim()
            if (t.isNotEmpty() && t.all { it.isDigit() }) numericCount++
        }
        return numericCount >= 2
    }

    private fun isShiftRow(cells: List<String>): Boolean {
        if (cells.isEmpty()) return false
        return cells[0].trim().lowercase() in SHIFT_TIMES
    }

    // ── Month extraction ──────────────────────────────────────────────────────

    private fun parseScheduleMonthFromFileName(
        fileName: String, errors: MutableList<String>
    ): String {
        val baseName = fileName.substringBeforeLast(".").trim()

        // Pattern: "yyyy-MM" anywhere in filename
        val isoPattern = Regex("""(\d{4})[_\-](\d{2})""")
        isoPattern.find(fileName)?.let {
            return "${it.groupValues[1]}-${it.groupValues[2]}"
        }

        // Pattern: English/Hungarian month name + 4-digit year (or reverse)
        val yearPattern = Regex("""\b(\d{4})\b""")
        val yearMatch = yearPattern.find(baseName)
        val year = yearMatch?.groupValues?.get(1) ?: LocalDate.now().year.toString()

        val lowerName = baseName.lowercase()
        for ((monthName, monthNum) in MONTH_NAMES) {
            if (monthName in lowerName) {
                return "$year-$monthNum"
            }
        }

        errors.add("Could not parse schedule month from filename '$fileName', using current month")
        val now = LocalDate.now()
        return "${now.year}-${now.monthValue.toString().padStart(2, '0')}"
    }
}
