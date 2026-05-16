package com.example.shiftpaw.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.shiftpaw.data.local.dao.EmployeeDao
import com.example.shiftpaw.data.local.dao.ImportedScheduleDao
import com.example.shiftpaw.data.local.dao.ShiftDao
import com.example.shiftpaw.data.local.entity.EmployeeEntity
import com.example.shiftpaw.data.local.entity.ImportedScheduleEntity
import com.example.shiftpaw.data.local.entity.ShiftEntity

@Database(
    entities = [
        EmployeeEntity::class,
        ShiftEntity::class,
        ImportedScheduleEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ShiftPawDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun shiftDao(): ShiftDao
    abstract fun importedScheduleDao(): ImportedScheduleDao
}
