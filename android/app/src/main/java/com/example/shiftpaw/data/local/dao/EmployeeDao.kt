package com.example.shiftpaw.data.local.dao

import androidx.room.*
import com.example.shiftpaw.data.local.entity.EmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActive(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAll(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getById(id: Long): EmployeeEntity?

    @Query("SELECT * FROM employees WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getByName(name: String): EmployeeEntity?

    @Query("SELECT * FROM employees")
    suspend fun getAllOnce(): List<EmployeeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: EmployeeEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(employees: List<EmployeeEntity>): List<Long>

    @Update
    suspend fun update(employee: EmployeeEntity)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteById(id: Long)
}
