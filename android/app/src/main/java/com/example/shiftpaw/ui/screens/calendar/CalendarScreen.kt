package com.example.shiftpaw.ui.screens.calendar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shiftpaw.domain.model.Employee
import com.example.shiftpaw.domain.model.ShiftDay
import com.example.shiftpaw.domain.model.ShiftType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    onNavigateToImport: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val shiftDays by viewModel.shiftDays.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val selectedEmployeeIds by viewModel.selectedEmployeeIds.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToImport) {
                Icon(Icons.Filled.Upload, contentDescription = "Import Schedule")
            }
        }
    ) { scaffoldPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp)
            .padding(scaffoldPadding)
    ) {
        Spacer(Modifier.height(8.dp))

        // Month header
        MonthHeader(
            month = currentMonth,
            onPrev = viewModel::prevMonth,
            onNext = viewModel::nextMonth,
            onJumpToMonth = viewModel::jumpToMonth
        )

        Spacer(Modifier.height(8.dp))

        // Day-of-week header
        DayOfWeekRow()

        Spacer(Modifier.height(4.dp))

        // Calendar grid
        CalendarGrid(
            month = currentMonth,
            shiftDays = shiftDays,
            employees = employees,
            selectedDate = selectedDate,
            selectedEmployeeIds = selectedEmployeeIds,
            onDayClick = viewModel::selectDate
        )

        Spacer(Modifier.height(12.dp))

        Spacer(Modifier.height(12.dp))

        SelectedDayCard(
            selectedDate = selectedDate,
            shiftDays = shiftDays,
            employees = employees,
            selectedEmployeeIds = selectedEmployeeIds,
            onDismiss = viewModel::dismissDate
        )

        Spacer(Modifier.height(8.dp))
    }
    } // end Scaffold
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onJumpToMonth: (YearMonth) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    var showPicker by remember { mutableStateOf(false) }
    var pickerYear by remember(month) { mutableIntStateOf(month.year) }
    var pickerMonth by remember(month) { mutableIntStateOf(month.monthValue) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Row(
            modifier = Modifier.clickable { showPicker = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = month.format(formatter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Pick month",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = {
                // Year selector row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { pickerYear-- }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous year")
                    }
                    Text(
                        text = pickerYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = { pickerYear++ }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next year")
                    }
                }
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(12) { idx ->
                        val m = idx + 1
                        val isSelected = pickerMonth == m && pickerYear == month.year
                        FilterChip(
                            selected = isSelected,
                            onClick = { pickerMonth = m },
                            label = {
                                Text(
                                    text = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPicker = false
                    onJumpToMonth(YearMonth.of(pickerYear, pickerMonth))
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DayOfWeekRow() {
    val days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    Row(Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    shiftDays: List<ShiftDay>,
    employees: List<Employee>,
    selectedDate: LocalDate?,
    selectedEmployeeIds: Set<Long>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val startOffset = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val daysInMonth = month.lengthOfMonth()

    val shiftDayMap = shiftDays.associateBy { it.date }
    val employeeMap = employees.associateBy { it.id }

    Column {
        repeat(6) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - startOffset + 1
                    val isValid = dayNumber in 1..daysInMonth
                    val date = if (isValid) month.atDay(dayNumber) else null

                    DayCell(
                        modifier = Modifier.weight(1f),
                        dayNumber = if (isValid) dayNumber else null,
                        date = date,
                        shiftDay = date?.let { shiftDayMap[it] },
                        employees = employeeMap,
                        isSelected = date != null && date == selectedDate,
                        selectedEmployeeIds = selectedEmployeeIds,
                        onClick = { if (date != null) onDayClick(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier = Modifier,
    dayNumber: Int?,
    date: LocalDate?,
    shiftDay: ShiftDay?,
    employees: Map<Long, Employee>,
    isSelected: Boolean,
    selectedEmployeeIds: Set<Long>,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val isToday = date != null && date == today

    val filteredShifts = shiftDay?.shifts?.filter {
        selectedEmployeeIds.isEmpty() || it.employeeId in selectedEmployeeIds
    } ?: emptyList()

    Box(
        modifier = modifier
            .height(52.dp)
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (isToday && !isSelected)
                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                else Modifier
            )
            .clickable(enabled = dayNumber != null, onClick = onClick),
        contentAlignment = Alignment.TopCenter
    ) {
        if (dayNumber != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = dayNumber.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                if (filteredShifts.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        filteredShifts
                            .mapNotNull { shift -> employees[shift.employeeId] }
                            .distinctBy { it.id }
                            .take(3)
                            .forEach { emp ->
                                val dotColor = try {
                                    Color(android.graphics.Color.parseColor(emp.color))
                                } catch (_: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun shiftTypeColor(type: ShiftType): Color {
    return when (type) {
        ShiftType.DAY -> MaterialTheme.colorScheme.primary
        ShiftType.EVENING -> MaterialTheme.colorScheme.secondary
        ShiftType.NIGHT -> Color(0xFF1565C0)
        ShiftType.ON_CALL -> MaterialTheme.colorScheme.tertiary
        ShiftType.OFF -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Inline selected-day card — replaces ModalBottomSheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SelectedDayCard(
    selectedDate: LocalDate?,
    shiftDays: List<ShiftDay>,
    employees: List<Employee>,
    selectedEmployeeIds: Set<Long>,
    onDismiss: () -> Unit
) {
    val dateHeaderFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    }
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .animateContentSize()
    ) {
        if (selectedDate == null) {
            // ── Empty / no-selection state ────────────────────────────────────
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Tap a day to see scheduled shifts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // ── Selected state ────────────────────────────────────────────────
            val filteredShifts = shiftDays
                .find { it.date == selectedDate }
                ?.shifts
                ?.filter { selectedEmployeeIds.isEmpty() || it.employeeId in selectedEmployeeIds }
                ?: emptyList()

            val employeeMap = employees.associateBy { it.id }

            Column {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDate.format(dateHeaderFormatter),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (filteredShifts.size > 1) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "BUSY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                if (filteredShifts.isEmpty()) {
                    Text(
                        text = "No shifts scheduled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    filteredShifts.forEachIndexed { index, shift ->
                        val employee = employeeMap[shift.employeeId]
                        val shiftColor = shiftTypeColor(shift.shiftType)
                        val dotColor = employee?.let {
                            try { Color(android.graphics.Color.parseColor(it.color)) }
                            catch (_: Exception) { MaterialTheme.colorScheme.primary }
                        } ?: MaterialTheme.colorScheme.primary

                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = employee?.name ?: "Unknown",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = shiftColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = shift.shiftType.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = shiftColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${shift.startTime.format(timeFormatter)} – ${shift.endTime.format(timeFormatter)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (index < filteredShifts.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}
