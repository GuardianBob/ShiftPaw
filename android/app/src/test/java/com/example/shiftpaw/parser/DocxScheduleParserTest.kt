package com.example.shiftpaw.parser

import com.example.shiftpaw.data.parser.DocxScheduleParser
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream

/**
 * Integration test for [DocxScheduleParser] against the 5 real sample DOCX files.
 *
 * Samples are read directly from the project's samples/ directory via absolute path.
 * These are JVM unit tests — no Android runtime needed.
 */
class DocxScheduleParserTest {

    private val parser = DocxScheduleParser()

    /** Absolute path to the samples directory. */
    private val samplesDir = File(
        "D:\\Documents\\Coding\\projects\\VetCalApp\\samples"
    )

    private val sampleFiles = listOf(
        "January 2026.docx",
        "February 2026 - Copy.docx",
        "March 2026.docx",
        "April 2026.docx",
        "May 2026 - Copy.docx",
    )

    @Test
    fun `January 2026 - parses correctly`() = assertParseSuccess("January 2026.docx", "2026-01")

    @Test
    fun `February 2026 - parses correctly`() = assertParseSuccess("February 2026 - Copy.docx", "2026-02")

    @Test
    fun `March 2026 - parses correctly`() = assertParseSuccess("March 2026.docx", "2026-03")

    @Test
    fun `April 2026 - parses correctly`() = assertParseSuccess("April 2026.docx", "2026-04")

    @Test
    fun `May 2026 - parses correctly`() = assertParseSuccess("May 2026 - Copy.docx", "2026-05")

    @Test
    fun `all sample files - non-empty shifts`() {
        val missing = mutableListOf<String>()
        val errors = mutableListOf<String>()
        val empty = mutableListOf<String>()

        for (fileName in sampleFiles) {
            val file = File(samplesDir, fileName)
            if (!file.exists()) {
                missing.add(fileName)
                continue
            }
            val result = parser.parse(FileInputStream(file), fileName)
            if (result.errors.isNotEmpty()) {
                errors.add("$fileName errors: ${result.errors}")
            }
            if (result.shifts.isEmpty()) {
                empty.add(fileName)
            }
        }

        assertTrue("Missing sample files: $missing", missing.isEmpty())
        assertTrue("Files parsed with empty shifts: $empty", empty.isEmpty())
        // Errors are non-fatal warnings — log them, don't fail
        if (errors.isNotEmpty()) {
            println("Non-fatal parser warnings: $errors")
        }
    }

    @Test
    fun `all sample files - valid shift dates and types`() {
        for (fileName in sampleFiles) {
            val file = File(samplesDir, fileName)
            if (!file.exists()) continue

            val result = parser.parse(FileInputStream(file), fileName)
            val datePattern = Regex("""\d{4}-\d{2}-\d{2}""")

            for (shift in result.shifts) {
                assertTrue(
                    "$fileName: shift has blank date",
                    shift.date.isNotBlank()
                )
                assertTrue(
                    "$fileName: shift date '${shift.date}' is not yyyy-MM-dd",
                    datePattern.matches(shift.date)
                )
                assertTrue(
                    "$fileName: shift has blank shiftType",
                    shift.shiftType.isNotBlank()
                )
                assertTrue(
                    "$fileName: shift employeeId must be > 0",
                    shift.employeeId > 0
                )
            }
        }
    }

    @Test
    fun `all sample files - scheduleMonth is not blank`() {
        for (fileName in sampleFiles) {
            val file = File(samplesDir, fileName)
            if (!file.exists()) continue
            val result = parser.parse(FileInputStream(file), fileName)
            assertFalse(
                "$fileName: scheduleMonth is blank",
                result.scheduleMonth.isBlank()
            )
            // Must match yyyy-MM
            assertTrue(
                "$fileName: scheduleMonth '${result.scheduleMonth}' is not yyyy-MM",
                result.scheduleMonth.matches(Regex("""\d{4}-\d{2}"""))
            )
        }
    }

    @Test
    fun `all sample files - employee names are not empty`() {
        for (fileName in sampleFiles) {
            val file = File(samplesDir, fileName)
            if (!file.exists()) continue
            val result = parser.parse(FileInputStream(file), fileName)
            assertFalse(
                "$fileName: employeeNames is empty",
                result.employeeNames.isEmpty()
            )
            // All names should be non-blank
            for (name in result.employeeNames) {
                assertFalse("$fileName: empty employee name in list", name.isBlank())
            }
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun assertParseSuccess(fileName: String, expectedMonth: String) {
        val file = File(samplesDir, fileName)
        assertTrue("Sample file not found: $fileName", file.exists())

        val result = parser.parse(FileInputStream(file), fileName)

        // Print diagnostics
        println("=== $fileName ===")
        println("  scheduleMonth: ${result.scheduleMonth}")
        println("  employees (${result.employeeNames.size}): ${result.employeeNames}")
        println("  shifts: ${result.shifts.size}")
        if (result.errors.isNotEmpty()) println("  warnings: ${result.errors}")

        assertTrue(
            "$fileName: expected scheduleMonth $expectedMonth but got ${result.scheduleMonth}",
            result.scheduleMonth == expectedMonth
        )
        assertFalse("$fileName: no shifts parsed", result.shifts.isEmpty())
        assertFalse("$fileName: no employees found", result.employeeNames.isEmpty())
        assertNotNull("$fileName: null shifts list", result.shifts)
    }
}
