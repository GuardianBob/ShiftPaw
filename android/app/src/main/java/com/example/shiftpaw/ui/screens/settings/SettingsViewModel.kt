package com.example.shiftpaw.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftpaw.data.repository.ShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository
) : ViewModel() {

    private val _showClearMonthDialog = MutableStateFlow(false)
    val showClearMonthDialog: StateFlow<Boolean> = _showClearMonthDialog

    private val _showClearAllDialog = MutableStateFlow(false)
    val showClearAllDialog: StateFlow<Boolean> = _showClearAllDialog

    private val _clearMonthResult = MutableStateFlow<ClearResult?>(null)
    val clearMonthResult: StateFlow<ClearResult?> = _clearMonthResult

    sealed class ClearResult {
        data class Success(val month: String) : ClearResult()
        data class Error(val message: String) : ClearResult()
    }

    fun showClearMonthDialog() { _showClearMonthDialog.value = true }
    fun dismissClearMonthDialog() { _showClearMonthDialog.value = false }

    fun showClearAllDialog() { _showClearAllDialog.value = true }
    fun dismissClearAllDialog() { _showClearAllDialog.value = false }

    fun clearMonthResultShown() { _clearMonthResult.value = null }

    fun clearShiftsForMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                val monthStr = yearMonth.toString()
                shiftRepository.clearShiftsForMonth(monthStr)
                _clearMonthResult.value = ClearResult.Success(monthStr)
            } catch (e: Exception) {
                _clearMonthResult.value = ClearResult.Error(e.message ?: "Unknown error")
            } finally {
                _showClearMonthDialog.value = false
            }
        }
    }

    fun clearAllShifts() {
        viewModelScope.launch {
            try {
                shiftRepository.clearAllShifts()
                _clearMonthResult.value = ClearResult.Success("All data")
            } catch (e: Exception) {
                _clearMonthResult.value = ClearResult.Error(e.message ?: "Unknown error")
            } finally {
                _showClearAllDialog.value = false
            }
        }
    }

    // Expose current month for the dialog default
    val currentYearMonth: StateFlow<YearMonth> = MutableStateFlow(YearMonth.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), YearMonth.now())
}
