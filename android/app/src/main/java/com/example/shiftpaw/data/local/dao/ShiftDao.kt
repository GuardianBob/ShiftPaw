package com.example.shiftpaw.data.local.dao

import androidx.room.*
import com.example.shiftpaw.data.local.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts WHERE date = :date ORDER BY startTime ASC")
    fun getShiftsForDate(date: String): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, startTime ASC")
    fun getShiftsForRange(startDate: String, endDate: String): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE employeeId = :employeeId AND date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getShiftsForEmployee(employeeId: Long, startDate: String, endDate: String): Flow<List<ShiftEntity>>

    @Query("SELECT DISTINCT date FROM shifts WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getDatesWithShifts(startDate: String, endDate: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shifts: List<ShiftEntity>)

    @Query("DELETE FROM shifts WHERE sourceScheduleDate = :scheduleMonth")
    suspend fun deleteBySourceMonth(scheduleMonth: String)

    @Query("DELETE FROM shifts WHERE date >= :startDate AND date <= :endDate")
    suspend fun deleteForRange(startDate: String, endDate: String)
}
