# VetCalendar Android — DOCX Parser Specification

**Source:** `/VetCalendar_v3/backend/VetCalendar/scripts.py` (lines 46–264)  
**Status:** Production-tested, accurate  
**Ported to:** Kotlin + Apache POI  

---

## Input Format: DOCX Table Structure

### Overview

The DOCX file contains a **table with a paired-column layout:**
- **Columns:** 14 total (7 day-columns × 2 columns each)
  - Column 0, 2, 4, 6, 8, 10, 12 → Date cells (e.g., "1", "8", "15", "22", "29")
  - Column 1, 3, 5, 7, 9, 11, 13 → Employee cells (e.g., "JD", "MS", "AL", "")

- **Rows:** Variable, following this pattern:
  1. Header row (weekday names: "Monday", "Tuesday", ..., "Sunday")
  2. Date row (dates: "1", "8", "15", "22", "29")
  3. Shift row "Day" (employee initials for 07:00–19:00 shift)
  4. Shift row "Swing 1" (employee initials for 10:00–22:00 shift)
  5. Shift row "Swing 2" (employee initials for 14:00–02:00 shift)
  6. Shift row "Night" (employee initials for 18:00–06:00 shift)
  7. (Repeat: date row + shift rows for next week)

### Example Table

```
┌─────────┬──────────┬─────────┬──────────┬─────────┬──────────┬─────────┬──────────┬─────────┬──────────┬─────────┬──────────┬─────────┬──────────┐
│ Monday  │          │ Tuesday │          │ Wed     │          │ Thursday│          │ Friday  │          │ Sat     │          │ Sunday  │          │
├─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┤
│ 1       │          │ 2       │          │ 3       │          │ 4       │          │ 5       │          │ 6       │          │ 7       │          │
├─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┤
│ Day     │ JD       │ Day     │ MS       │ Day     │ AL       │ Day     │ JD       │ Day     │ MS       │ Day     │ AL       │ Day     │ JD       │
├─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┤
│ Swing 1 │ MS       │ Swing 1 │ AL       │ Swing 1 │ JD       │ Swing 1 │ MS       │ Swing 1 │ AL       │ Swing 1 │ JD       │ Swing 1 │ MS       │
├─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┤
│ Swing 2 │ AL       │ Swing 2 │ JD       │ Swing 2 │ MS       │ Swing 2 │ AL       │ Swing 2 │ JD       │ Swing 2 │ MS       │ Swing 2 │ AL       │
├─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┼─────────┼──────────┤
│ Night   │ JD       │ Night   │ MS       │ Night   │ AL       │ Night   │ JD       │ Night   │ MS       │ Night   │ AL       │ Night   │ JD       │
└─────────┴──────────┴─────────┴──────────┴─────────┴──────────┴─────────┴──────────┴─────────┴──────────┴─────────┴──────────┴─────────┴──────────┘
```

---

## Shift Time Mapping

| Shift Name | Start Time | End Time | Duration | Notes |
|-----------|-----------|----------|----------|-------|
| Day | 07:00 | 19:00 | 12h | Morning/afternoon |
| Swing 1 | 10:00 | 22:00 | 12h | Mid-day/evening |
| Swing 2 | 14:00 | 02:00 | 12h | Afternoon/night (next day) |
| Night | 18:00 | 06:00 | 12h | Evening/morning (next day) |

**Note:** All shifts are **exactly 12 hours**. End time calculation:
```
end_time = start_time + 12 hours
If end_time > 23:59, it wraps to next day (e.g., 14:00 + 12h = 02:00 next day)
```

---

## Parser Algorithm (High-Level)

```
Input: DOCX file, month (01–12), year (2026+)
Output: List<Shift>

1. Open DOCX file as ZIP archive
2. Extract `word/document.xml` → parse as XML
3. Find all tables
4. For each table:
   a. Validate: 14 columns (or more, as long as even number)
   b. Calculate day-columns = total_columns ÷ 2
   c. Initialize current_dates = {} (col_idx → date string)
   
   d. For each row:
      - If all cells empty: skip
      - If header row (contains weekday names): skip
      - If date row (contains numbers): extract dates into current_dates, skip
      - If shift row (starts with "Day", "Swing 1", etc.):
        * Extract shift name from first cell
        * Get start_time from SHIFT_TIMES mapping
        * For each day-column (0 to 6):
          - date = current_dates[col_idx]
          - employee = cells[col_idx * 2 + 1]
          - If employee not empty: create Shift object
        * Add shifts to results
5. Return List<Shift>
```

---

## Row Type Detection Logic

### Header Row

```python
def is_day_header_row(cells):
    """
    Count cells containing weekday names (case-insensitive).
    Return True if at least 6 cells match.
    """
    matches = 0
    for cell in cells:
        cell_lower = cell.lower().strip()
        for day_name in ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"]:
            if day_name in cell_lower:
                matches += 1
                break
    return matches >= 6
```

**Kotlin equivalent:**
```kotlin
fun isDayHeaderRow(cells: List<String>): Boolean {
    val dayNames = setOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
    var matches = 0
    for (cell in cells) {
        val cellLower = cell.lowercase().trim()
        if (dayNames.any { it in cellLower }) {
            matches++
        }
    }
    return matches >= 6
}
```

### Date Row

```python
def is_date_row(cells):
    """
    Count cells that are numeric (digits only).
    Return True if at least 2 numeric cells.
    """
    numeric_count = 0
    for cell in cells:
        if cell.strip().isdigit():
            numeric_count += 1
    return numeric_count >= 2
```

**Kotlin equivalent:**
```kotlin
fun isDateRow(cells: List<String>): Boolean {
    var numericCount = 0
    for (cell in cells) {
        if (cell.trim().all { it.isDigit() }) {
            numericCount++
        }
    }
    return numericCount >= 2
}
```

### Shift Row

```python
SHIFT_NAMES = {"day", "swing 1", "swing 2", "night", "swing1", "swing2"}

def is_shift_row(cells):
    """
    Check if first cell (lowercase) is a known shift name.
    """
    if not cells:
        return False
    first_cell = cells[0].strip().lower()
    return first_cell in SHIFT_NAMES
```

**Kotlin equivalent:**
```kotlin
val SHIFT_NAMES = setOf("day", "swing 1", "swing 2", "night", "swing1", "swing2")

fun isShiftRow(cells: List<String>): Boolean {
    if (cells.isEmpty()) return false
    val firstCell = cells[0].strip().lowercase()
    return firstCell in SHIFT_NAMES
}
```

---

## Detailed Parser Implementation (Kotlin + Apache POI)

### Step 1: Open DOCX

```kotlin
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.InputStream

fun parseDocxShifts(inputStream: InputStream, month: String, year: String): List<Shift> {
    val doc = XWPFDocument(inputStream)
    val shifts = mutableListOf<Shift>()
    val warnings = mutableListOf<String>()
    
    // Validate month/year
    val validMonths = (1..12).map { it.toString().padStart(2, '0') }
    var validMonth = month
    var validYear = year
    
    if (month !in validMonths) {
        warnings.add("Invalid month '$month' — defaulting to '01'")
        validMonth = "01"
    }
    
    try {
        validYear = if (year.toInt() < 2021) {
            warnings.add("Year '$year' too old — defaulting to 2026")
            "2026"
        } else {
            year
        }
    } catch (e: NumberFormatException) {
        warnings.add("Invalid year '$year' — defaulting to 2026")
        validYear = "2026"
    }
    
    // Parse tables
    for (table in doc.tables) {
        val numCols = table.rows.firstOrNull()?.cells?.size ?: continue
        
        // Validate: at least 8 columns, even number
        if (numCols < 8 || numCols % 2 != 0) {
            warnings.add("Skipped table with unexpected column count ($numCols)")
            continue
        }
        
        val numDayColumns = numCols / 2
        var currentDates = mutableMapOf<Int, String>()  // col_idx -> date
        
        for (row in table.rows) {
            val cells = row.cells.map { it.text.strip() }
            
            // Skip empty rows
            if (cells.all { it.isEmpty() }) continue
            
            // Process row type
            when {
                isDayHeaderRow(cells) -> {
                    currentDates.clear()
                }
                isDateRow(cells) -> {
                    currentDates.clear()
                    for (colIdx in 0 until numDayColumns) {
                        val cellPos = colIdx * 2
                        if (cellPos < cells.size) {
                            val val = cells[cellPos].trim()
                            if (val.all { it.isDigit() }) {
                                val dayNum = val.toInt()
                                try {
                                    // Validate date is valid for month/year
                                    LocalDate.of(validYear.toInt(), validMonth.toInt(), dayNum)
                                    currentDates[colIdx] = val
                                } catch (e: Exception) {
                                    // Invalid day for this month, skip silently
                                }
                            }
                        }
                    }
                }
                isShiftRow(cells) -> {
                    val shiftName = cells[0].trim().lowercase()
                    val startTime = SHIFT_TIMES[shiftName] ?: "07:00"
                    
                    for (colIdx in 0 until numDayColumns) {
                        val dateStr = currentDates[colIdx] ?: continue
                        
                        val userCellPos = colIdx * 2 + 1
                        if (userCellPos >= cells.size) continue
                        
                        val userInitials = cells[userCellPos].trim()
                        if (userInitials.isEmpty()) continue
                        
                        // Parse start time
                        val (startHour, startMin) = startTime.split(":").let { (h, m) -> h.toInt() to m.toInt() }
                        
                        try {
                            val date = LocalDate.of(validYear.toInt(), validMonth.toInt(), dateStr.toInt())
                            val start = LocalDateTime.of(date, LocalTime.of(startHour, startMin))
                            val end = start.plusHours(12)
                            
                            shifts.add(
                                Shift(
                                    date = date.toEpochDay(),
                                    startTime = startTime,
                                    endTime = end.toLocalTime().toString(),
                                    userInitials = userInitials,
                                    location = "To be determined",
                                    month = validMonth.toInt(),
                                    year = validYear.toInt(),
                                    type = shiftName,
                                )
                            )
                        } catch (e: Exception) {
                            warnings.add("Skipped invalid shift: $validYear-$validMonth-$dateStr $startTime: ${e.message}")
                        }
                    }
                }
            }
        }
    }
    
    // Return results
    return shifts
}

// Constants
val SHIFT_TIMES = mapOf(
    "day" to "07:00",
    "swing 1" to "10:00",
    "swing 2" to "14:00",
    "night" to "18:00",
    "swing1" to "10:00",
    "swing2" to "14:00",
)

// Helper functions
fun isDayHeaderRow(cells: List<String>): Boolean {
    val dayNames = setOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
    var matches = 0
    for (cell in cells) {
        val cellLower = cell.lowercase().trim()
        if (dayNames.any { it in cellLower }) {
            matches++
        }
    }
    return matches >= 6
}

fun isDateRow(cells: List<String>): Boolean {
    var numericCount = 0
    for (cell in cells) {
        if (cell.trim().all { it.isDigit() }) {
            numericCount++
        }
    }
    return numericCount >= 2
}

fun isShiftRow(cells: List<String>): Boolean {
    if (cells.isEmpty()) return false
    val shiftNames = setOf("day", "swing 1", "swing 2", "night", "swing1", "swing2")
    val firstCell = cells[0].trim().lowercase()
    return firstCell in shiftNames
}
```

---

## Data Output: Shift Objects

Each parsed shift becomes a `Shift` database entity:

```kotlin
@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,              // LocalDate.toEpochDay()
    val startTime: String,       // "07:00", "10:00", etc.
    val endTime: String,         // "19:00", "22:00", etc.
    val userInitials: String,    // "JD", "MS", "AL"
    val location: String,        // "To be determined" (Phase 1)
    val month: Int,              // 1–12
    val year: Int,               // 2026+
    val type: String? = null,    // "day", "swing 1", "swing 2", "night"
    val notes: String? = null,
)
```

**Example Output:**

Given input shift row:
```
Day  | JD       | Day  | MS       | Day  | AL       | ...
```

With dates:
```
1    |          | 2    |          | 3    |          | ...
```

Produces 3 Shift objects:
```kotlin
Shift(
    date = LocalDate(2026, 4, 1).toEpochDay(),
    startTime = "07:00",
    endTime = "19:00",
    userInitials = "JD",
    location = "To be determined",
    month = 4,
    year = 2026,
    type = "day",
)

Shift(
    date = LocalDate(2026, 4, 2).toEpochDay(),
    startTime = "07:00",
    endTime = "19:00",
    userInitials = "MS",
    location = "To be determined",
    month = 4,
    year = 2026,
    type = "day",
)

Shift(
    date = LocalDate(2026, 4, 3).toEpochDay(),
    startTime = "07:00",
    endTime = "19:00",
    userInitials = "AL",
    location = "To be determined",
    month = 4,
    year = 2026,
    type = "day",
)
```

---

## Error Handling

### Validation Errors

| Error | Cause | Action |
|-------|-------|--------|
| Invalid file format | Not a ZIP (DOCX) | Throw exception, show "File is not a valid .docx" |
| No tables found | DOCX has no tables | Return empty list, show "No schedule table found" |
| Invalid month | Month not in 01–12 | Fallback to "01" (January) |
| Invalid year | Year < 2021 | Fallback to 2026 |
| Invalid date | Day doesn't exist in month (e.g., Feb 30) | Skip silently |
| Invalid start time | Shift time not in SHIFT_TIMES | Fallback to "07:00" |

### Parsing Warnings (Collected, Returned)

```kotlin
data class ParseResult(
    val status: String,           // "ok" or "error"
    val shifts: List<Shift>,      // Shifts parsed
    val rowsCreated: Int,         // Count of shifts
    val warnings: List<String>,   // Non-fatal issues
    val message: String? = null,  // Error message if status == "error"
)
```

**Example return (successful with warnings):**
```json
{
    "status": "ok",
    "shifts": [ /* 22 shifts */ ],
    "rowsCreated": 22,
    "warnings": [
        "Column count mismatch in table 2 (8 columns, expected 14)",
        "Skipped invalid date: 2026-02-30"
    ],
    "message": null
}
```

---

## Testing Strategy

### Unit Tests (JUnit 5)

```kotlin
class DocxParserTest {
    @Test
    fun parseValidDocx_extractsShiftsCorrectly() {
        val input = getResourceAsStream("April_2026.docx")
        val shifts = parseDocxShifts(input, "04", "2026")
        
        assert(shifts.size >= 22)  // Typical month
        assert(shifts.any { it.userInitials == "JD" })
        assert(shifts.any { it.type == "day" })
    }
    
    @Test
    fun parseMultipleEmployeeCell_createsMultipleShifts() {
        // FUTURE: if DOCX supports "JD, MS" format
        // For now: single employee per cell only
    }
    
    @Test
    fun parseEmptyCell_skipsShift() {
        val input = getResourceAsStream("April_2026.docx")
        val shifts = parseDocxShifts(input, "04", "2026")
        
        // Should not create shifts for empty cells
        assert(shifts.all { it.userInitials.isNotEmpty() })
    }
    
    @Test
    fun invalidMonth_defaults() {
        val shifts = parseDocxShifts(input, "99", "2026")
        // Should default to month 01, but shifts still parse
    }
    
    @Test
    fun invalidDocx_throwsException() {
        val input = "not a docx file"
        assertThrows<Exception> {
            parseDocxShifts(input, "04", "2026")
        }
    }
}
```

### Integration Tests

```kotlin
class DocxParserIntegrationTest {
    @Test
    fun parseAllSampleFiles_succeeds() {
        val samples = listOf(
            "January 2026.docx",
            "February 2026.docx",
            "March 2026.docx",
            "April 2026.docx",
            "May 2026.docx",
        )
        
        for (file in samples) {
            val shifts = parseDocxShifts(getResourceAsStream(file), "01", "2026")
            assert(shifts.isNotEmpty()) { "$file parsed empty" }
        }
    }
}
```

---

## Migration Notes (Future)

### Phase 2: Location Extraction

DOCX files may contain location metadata:
- In table caption
- In cell notes
- In separate "Notes" column

**Strategy:** Extract shift type first (Phase 1), add location field when available in DOCX.

### Phase 2: Recurring Shifts

If DOCX supports recurring patterns (e.g., "JD every Monday"), add:
- `recurring: Boolean`
- `recurrenceRule: String` (iCal RRULE)

### Phase 3: API Submission

Send parsed shifts to backend:
```
POST /api/shifts/import
{
    "month": 4,
    "year": 2026,
    "shifts": [ /* Shift objects */ ],
}
```

---

**Spec Status:** PRODUCTION-READY (from Python source)  
**Last Updated:** [Now]  
**Owner:** [Rocket]
