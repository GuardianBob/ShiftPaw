package com.example.shiftpaw.ui.screens.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftpaw.data.repository.ShiftRepository
import com.example.shiftpaw.domain.model.Employee
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class EmployeeWithStats(
    val employee: Employee,
    val shiftsThisMonth: Int,
    val nextShiftDate: LocalDate?
)

@HiltViewModel
class EmployeesViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository
) : ViewModel() {

    val employeesWithStats: StateFlow<List<EmployeeWithStats>> =
        shiftRepository.getActiveEmployees()
            .flatMapLatest { employees ->
                if (employees.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val statFlows = employees.map { emp ->
                        combine(
                            shiftRepository.shiftsThisMonthCount(emp.id),
                            shiftRepository.nextShift(emp.id)
                        ) { count, next ->
                            EmployeeWithStats(
                                employee = emp,
                                shiftsThisMonth = count,
                                nextShiftDate = next?.let { LocalDate.parse(it.date) }
                            )
                        }
                    }
                    combine(statFlows) { it.toList() }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
