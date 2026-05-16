package com.example.shiftpaw.data.repository

import com.example.shiftpaw.data.local.dao.EmployeeDao
import com.example.shiftpaw.data.local.entity.EmployeeEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves parser-assigned temporary employee IDs to real DB IDs.
 *
 * The parser assigns sequential temp IDs (1, 2, 3...) to employee names/initials.
 * This resolver looks up each name in the DB (case-insensitive), creates missing employees,
 * and returns a map of tempId → realDbId.
 */
@Singleton
class EmployeeResolver @Inject constructor(private val employeeDao: EmployeeDao) {

    companion object {
        /** Deterministic 8-color palette — cycled by employee insertion order. */
        private val COLOR_PALETTE = listOf(
            "#E53935", "#8E24AA", "#1E88E5", "#00ACC1",
            "#43A047", "#FB8C00", "#F4511E", "#6D4C41"
        )
    }

    /**
     * Given a list of names (or initials) from the parser, returns a map of
     * tempId (1-based, matching parser's sequential counter) → real DB employeeId.
     *
     * Employees not yet in the DB are inserted with a deterministic color from [COLOR_PALETTE].
     */
    suspend fun resolveOrCreate(names: List<String>): Map<Long, Long> {
        val result = mutableMapOf<Long, Long>()
        // Track how many employees existed before this batch to pick correct palette offset
        val existingCount = employeeDao.getAllOnce().size

        names.forEachIndexed { index, name ->
            val tempId = (index + 1).toLong()
            val trimmedName = name.trim()
            if (trimmedName.isEmpty()) {
                // Skip blank names — map to 0 as sentinel
                result[tempId] = 0L
                return@forEachIndexed
            }

            val existing = employeeDao.getByName(trimmedName)
            if (existing != null) {
                result[tempId] = existing.id
            } else {
                val colorIndex = (existingCount + result.values.count { it > 0 }) % COLOR_PALETTE.size
                val newEntity = EmployeeEntity(
                    name = trimmedName,
                    color = COLOR_PALETTE[colorIndex],
                    isActive = true
                )
                val newId = employeeDao.insert(newEntity)
                result[tempId] = newId
            }
        }

        return result
    }
}
