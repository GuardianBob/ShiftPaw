package com.example.shiftpaw.data.repository

import com.example.shiftpaw.data.local.dao.EmployeeDao
import com.example.shiftpaw.data.local.dao.ImportedScheduleDao
import com.example.shiftpaw.data.local.dao.ShiftDao
import com.example.shiftpaw.data.local.entity.EmployeeEntity
import com.example.shiftpaw.data.local.entity.ImportedScheduleEntity
import com.example.shiftpaw.data.local.entity.ShiftEntity
import com.example.shiftpaw.domain.model.Employee
import com.example.shiftpaw.domain.model.Shift
import com.example.shiftpaw.domain.model.ShiftDay
import com.example.shiftpaw.domain.model.ShiftType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiftRepository @Inject constructor(
    private val employeeDao: EmployeeDao,
    private val shiftDao: ShiftDao,
    private val importedScheduleDao: ImportedScheduleDao
) {
    // --- Employees ---
    fun getActiveEmployees(): Flow<List<Employee>> =
        employeeDao.getAllActive().map { list -> list.map { it.toDomain() } }

    suspend fun upsertEmployee(employee: Employee): Long =
        employeeDao.insert(employee.toEntity())

    // --- Shifts ---
    fun getShiftsForMonth(yearMonth: String): Flow<List<ShiftDay>> {
        val start = LocalDate.parse("$yearMonth-01")
        val end = start.withDayOfMonth(start.lengthOfMonth())
        return shiftDao.getShiftsForRange(start.toString(), end.toString())
            .map { entities -> groupToShiftDays(entities) }
    }

    fun getShiftsForDate(date: LocalDate): Flow<List<Shift>> =
        shiftDao.getShiftsForDate(date.toString()).map { it.map { e -> e.toDomain() } }

    suspend fun importShifts(shifts: List<ShiftEntity>, scheduleMonth: String, fileName: String) {
        shiftDao.deleteBySourceMonth(scheduleMonth)
        shiftDao.insertAll(shifts)
        if (importedScheduleDao.countByFileName(fileName) == 0) {
            importedScheduleDao.insert(ImportedScheduleEntity(fileName = fileName, scheduleMonth = scheduleMonth))
        }
    }

    fun getImportedSchedules() = importedScheduleDao.getAll()

    fun shiftsThisMonthCount(employeeId: Long): Flow<Int> {
        val yearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        return shiftDao.countShiftsForEmployeeInMonth(employeeId, yearMonth)
    }

    fun nextShift(employeeId: Long): Flow<ShiftEntity?> =
        shiftDao.nextShiftForEmployee(employeeId, LocalDate.now().toString())

    // --- Mapping helpers ---
    private fun groupToShiftDays(entities: List<ShiftEntity>): List<ShiftDay> =
        entities.groupBy { it.date }
            .map { (date, shifts) -> ShiftDay(LocalDate.parse(date), shifts.map { it.toDomain() }) }
            .sortedBy { it.date }

    private fun EmployeeEntity.toDomain() = Employee(id, name, color, isActive)
    private fun Employee.toEntity() = EmployeeEntity(id, name, color, isActive)
    private fun ShiftEntity.toDomain() = Shift(
        id, employeeId,
        LocalDate.parse(date),
        LocalTime.parse(startTime),
        LocalTime.parse(endTime),
        ShiftType.valueOf(shiftType),
        notes
    )
}
