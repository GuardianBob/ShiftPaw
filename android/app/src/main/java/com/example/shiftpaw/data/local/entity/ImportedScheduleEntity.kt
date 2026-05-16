package com.example.shiftpaw.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tracks which DOCX files have been imported to avoid re-importing. */
@Entity(tableName = "imported_schedules")
data class ImportedScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Original filename of the imported DOCX */
    val fileName: String,
    /** Schedule month/year derived from file (yyyy-MM) */
    val scheduleMonth: String,
    /** Epoch millis of import time */
    val importedAt: Long = System.currentTimeMillis()
)
