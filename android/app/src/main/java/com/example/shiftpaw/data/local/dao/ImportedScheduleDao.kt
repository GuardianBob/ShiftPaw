package com.example.shiftpaw.data.local.dao

import androidx.room.*
import com.example.shiftpaw.data.local.entity.ImportedScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportedScheduleDao {
    @Query("SELECT * FROM imported_schedules ORDER BY importedAt DESC")
    fun getAll(): Flow<List<ImportedScheduleEntity>>

    @Query("SELECT COUNT(*) FROM imported_schedules WHERE fileName = :fileName")
    suspend fun countByFileName(fileName: String): Int

    @Insert
    suspend fun insert(schedule: ImportedScheduleEntity): Long

    @Query("DELETE FROM imported_schedules")
    suspend fun deleteAll()

    @Query("DELETE FROM imported_schedules WHERE scheduleMonth = :month")
    suspend fun deleteByMonth(month: String)
}
