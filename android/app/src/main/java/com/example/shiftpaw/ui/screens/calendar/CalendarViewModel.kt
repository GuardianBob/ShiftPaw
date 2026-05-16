package com.example.shiftpaw.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftpaw.data.repository.ShiftRepository
import com.example.shiftpaw.data.repository.UserPreferencesRepository
import com.example.shiftpaw.domain.model.Employee
import com.example.shiftpaw.domain.model.ShiftDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth

    val shiftDays: StateFlow<List<ShiftDay>> = _currentMonth
        .flatMapLatest { month ->
            shiftRepository.getShiftsForMonth(month.format(monthFormatter))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val employees: StateFlow<List<Employee>> = shiftRepository.getActiveEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedEmployeeIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedEmployeeIds: StateFlow<Set<Long>> = _selectedEmployeeIds

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    init {
        viewModelScope.launch {
            val prefs = prefsRepository.preferences.first()
            if (prefs.primaryEmployeeId > 0L) {
                _selectedEmployeeIds.value = setOf(prefs.primaryEmployeeId)
            }
        }
    }

    fun prevMonth() {
        val newMonth = _currentMonth.value.minusMonths(1)
        _currentMonth.value = newMonth
        viewModelScope.launch {
            prefsRepository.setLastViewedMonth(newMonth.format(monthFormatter))
        }
    }

    fun nextMonth() {
        val newMonth = _currentMonth.value.plusMonths(1)
        _currentMonth.value = newMonth
        viewModelScope.launch {
            prefsRepository.setLastViewedMonth(newMonth.format(monthFormatter))
        }
    }

    fun jumpToMonth(month: YearMonth) {
        _currentMonth.value = month
        viewModelScope.launch {
            prefsRepository.setLastViewedMonth(month.format(monthFormatter))
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }

    fun dismissDate() {
        _selectedDate.value = null
    }

    fun toggleEmployee(id: Long) {
        val current = _selectedEmployeeIds.value
        _selectedEmployeeIds.value = if (id in current) current - id else current + id
    }

    fun clearEmployeeFilter() {
        _selectedEmployeeIds.value = emptySet()
    }

    /** Legacy stub — delegates to toggleEmployee for backward compat. */
    fun selectEmployee(id: Long) = toggleEmployee(id)
}
