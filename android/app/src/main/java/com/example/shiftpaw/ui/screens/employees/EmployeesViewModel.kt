package com.example.shiftpaw.ui.screens.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftpaw.data.repository.ShiftRepository
import com.example.shiftpaw.data.repository.UserPreferencesRepository
import com.example.shiftpaw.domain.model.Employee
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeesViewModel @Inject constructor(
    shiftRepository: ShiftRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    val employees: StateFlow<List<Employee>> = shiftRepository.getActiveEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val primaryEmployeeId: StateFlow<Long> = prefsRepository.preferences
        .map { it.primaryEmployeeId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), -1L)

    fun setPrimary(id: Long) {
        viewModelScope.launch {
            // Toggle: if already primary, clear it; otherwise set it
            val current = primaryEmployeeId.value
            prefsRepository.setPrimaryEmployee(if (current == id) -1L else id)
        }
    }
}
