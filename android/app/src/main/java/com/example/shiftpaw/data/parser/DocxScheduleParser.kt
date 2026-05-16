package com.example.shiftpaw.data.parser

import android.content.Context
import com.example.shiftpaw.data.local.entity.ShiftEntity
import com.example.shiftpaw.domain.model.ShiftType
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses VetCalendar schedule DOCX files into [ShiftEntity] records.
 *
 * Document format (per DOCX_PARSER_SPEC.md):
 * - Title row: month name + year (e.g. "Január 2026")
 * - Header row: date columns (1..31)
 * - Data rows: employee name | shift codes per day
 *
 * Shift codes: D = Day, E = Evening, N = Night, OC = On-Call, "" = Off
 */
@Singleton
class DocxScheduleParser @Inject constructor() {

    data class ParseResult(
        val shifts: List<ShiftEntity>,
        val scheduleMonth: String,    // yyyy-MM
        val employeeNames: List<String>,
        val errors: List<String>
    )

    fun parse(inputStream: InputStream, fileName: String): ParseResult {
        val errors = mutableListOf<String>()
        val shifts = mutableListOf<ShiftEntity>()
        val employeeNames = mutableListOf<String>()

        try {
            val doc = XWPFDocument(inputStream)
            val tables = doc.tables
            if (tables.isEmpty()) {
                return ParseResult(emptyList(), "", emptyList(), listOf("No tables found in document"))
            }

            val table = tables[0]
            val rows = table.rows
            if (rows.size < 3) {
                return ParseResult(emptyList(), "", emptyList(), listOf("Table too small"))
            }

            // Row 0: title — extract month/year
            val titleText = rows[0].getCell(0)?.text?.trim() ?: ""
            val scheduleMonth = parseScheduleMonth(titleText, fileName, errors)

            // Row 1: header — day numbers
            val headerRow = rows[1]
            val dayColumns = mutableMapOf<Int, Int>() // colIndex -> dayOfMonth
            for (colIdx in 1 until headerRow.tableCells.size) {
                val cellText = headerRow.getCell(colIdx)?.text?.trim() ?: continue
                val day = cellText.toIntOrNull() ?: continue
                dayColumns[colIdx] = day
            }

            // Rows 2+: employee data
            var employeeId = 1L  // temporary IDs — caller must resolve against DB
            for (rowIdx in 2 until rows.size) {
                val row = rows[rowIdx]
                val nameCell = row.getCell(0)?.text?.trim() ?: continue
                if (nameCell.isEmpty()) continue

                employeeNames.add(nameCell)

                for ((colIdx, day) in dayColumns) {
                    val cellText = row.getCell(colIdx)?.text?.trim() ?: ""
                    val shiftType = parseShiftCode(cellText) ?: continue
                    if (shiftType == ShiftType.OFF) continue  // don't store explicit offs

                    val date = try {
                        LocalDate.parse("${scheduleMonth}-${day.toString().padStart(2, '0')}")
                    } catch (e: Exception) {
                        errors.add("Invalid date: $scheduleMonth-$day")
                        continue
                    }

                    shifts.add(
                        ShiftEntity(
                            employeeId = employeeId,
                            date = date.toString(),
                            startTime = shiftType.defaultStart(),
                            endTime = shiftType.defaultEnd(),
                            shiftType = shiftType.name,
                            sourceScheduleDate = scheduleMonth
                        )
                    )
                }
                employeeId++
            }

            doc.close()
        } catch (e: Exception) {
            errors.add("Parse failed: ${e.message}")
        }

        return ParseResult(shifts, "", employeeNames, errors)
    }

    private fun parseScheduleMonth(title: String, fileName: String, errors: MutableList<String>): String {
        // Try to extract from filename pattern like "2026-01" or "January_2026"
        val yearMonthRegex = Regex("""(\d{4})[_\-](\d{2})""")
        yearMonthRegex.find(fileName)?.let { match ->
            return "${match.groupValues[1]}-${match.groupValues[2]}"
        }
        // Fallback: use current month
        errors.add("Could not parse schedule month from title '$title', using current month")
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    private fun parseShiftCode(code: String): ShiftType? = when (code.uppercase().trim()) {
        "D", "DAY", "R"       -> ShiftType.DAY
        "E", "EVE", "EVENING" -> ShiftType.EVENING
        "N", "NIGHT", "É"     -> ShiftType.NIGHT
        "OC", "ON_CALL", "K"  -> ShiftType.ON_CALL
        ""                    -> ShiftType.OFF
        else                  -> null  // unknown code — skip
    }

    private fun ShiftType.defaultStart(): String = when (this) {
        ShiftType.DAY     -> "07:00"
        ShiftType.EVENING -> "15:00"
        ShiftType.NIGHT   -> "23:00"
        ShiftType.ON_CALL -> "08:00"
        ShiftType.OFF     -> "00:00"
    }

    private fun ShiftType.defaultEnd(): String = when (this) {
        ShiftType.DAY     -> "15:00"
        ShiftType.EVENING -> "23:00"
        ShiftType.NIGHT   -> "07:00"
        ShiftType.ON_CALL -> "08:00"
        ShiftType.OFF     -> "00:00"
    }
}
