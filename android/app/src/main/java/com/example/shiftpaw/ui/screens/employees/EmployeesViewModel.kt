package com.example.shiftpaw.ui.screens.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftpaw.data.repository.ShiftRepository
import com.example.shiftpaw.domain.model.Employee
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EmployeesViewModel @Inject constructor(
    shiftRepository: ShiftRepository
) : ViewModel() {

    val employees: StateFlow<List<Employee>> = shiftRepository.getActiveEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
