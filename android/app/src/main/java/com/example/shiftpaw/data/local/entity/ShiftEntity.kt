package com.example.shiftpaw.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shifts",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("employeeId"), Index("date")]
)
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    /** ISO date string: yyyy-MM-dd */
    val date: String,
    /** HH:mm */
    val startTime: String,
    /** HH:mm */
    val endTime: String,
    /** ShiftType name */
    val shiftType: String,
    val notes: String = "",
    /** ISO date string of the schedule document this was imported from */
    val sourceScheduleDate: String = ""
)
