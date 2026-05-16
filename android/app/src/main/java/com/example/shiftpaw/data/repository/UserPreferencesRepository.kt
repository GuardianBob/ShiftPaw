package com.example.shiftpaw.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPreferences(
    val selectedEmployeeId: Long = -1L,
    val lastViewedMonth: String = "",  // yyyy-MM
    val primaryEmployeeId: Long = -1L
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val SELECTED_EMPLOYEE_ID = longPreferencesKey("selected_employee_id")
        val LAST_VIEWED_MONTH = stringPreferencesKey("last_viewed_month")
        val PRIMARY_EMPLOYEE_ID = longPreferencesKey("primary_employee_id")
    }

    val preferences = context.dataStore.data.map { prefs ->
        UserPreferences(
            selectedEmployeeId = prefs[Keys.SELECTED_EMPLOYEE_ID] ?: -1L,
            lastViewedMonth = prefs[Keys.LAST_VIEWED_MONTH] ?: "",
            primaryEmployeeId = prefs[Keys.PRIMARY_EMPLOYEE_ID] ?: -1L
        )
    }

    suspend fun setSelectedEmployee(id: Long) {
        context.dataStore.edit { it[Keys.SELECTED_EMPLOYEE_ID] = id }
    }

    suspend fun setLastViewedMonth(month: String) {
        context.dataStore.edit { it[Keys.LAST_VIEWED_MONTH] = month }
    }
}
