package com.example.shiftpaw.ui.screens.importschedule

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiftpaw.data.parser.DocxScheduleParser
import com.example.shiftpaw.data.repository.EmployeeResolver
import com.example.shiftpaw.data.repository.ShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ────────────────────────────────────────────────────────────────

sealed class ImportUiState {
    object Idle : ImportUiState()
    object Parsing : ImportUiState()
    data class AwaitingConfirmation(
        val fileName: String,
        /** "yyyy-MM" — user can edit before confirming */
        val scheduleMonth: String,
        val employeeCount: Int,
        val shiftCount: Int,
        val warnings: List<String>
    ) : ImportUiState()
    object Saving : ImportUiState()
    data class Success(val shiftCount: Int, val employeeCount: Int) : ImportUiState()
    data class Error(val message: String) : ImportUiState()
}

// ── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val parser: DocxScheduleParser,
    private val resolver: EmployeeResolver,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    // Cached parse result so we can re-use it when the user confirms
    private var cachedParseResult: DocxScheduleParser.ParseResult? = null
    private var cachedFileName: String = ""

    /** Called when the user selects a DOCX file from the system file picker. */
    fun onFileSelected(uri: Uri, context: Context) {
        _uiState.value = ImportUiState.Parsing
        viewModelScope.launch {
            try {
                val fileName = uri.lastPathSegment
                    ?.substringAfterLast("/")
                    ?: uri.toString().substringAfterLast("/")

                val stream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Cannot open file stream")

                val result = parser.parse(stream, fileName)
                cachedParseResult = result
                cachedFileName = fileName

                _uiState.value = ImportUiState.AwaitingConfirmation(
                    fileName = fileName,
                    scheduleMonth = result.scheduleMonth,
                    employeeCount = result.employeeNames.size,
                    shiftCount = result.shifts.size,
                    warnings = result.errors
                )
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(
                    e.message ?: "Failed to parse file"
                )
            }
        }
    }

    /** Allows the user to correct an incorrectly detected schedule month. */
    fun onMonthEdited(newMonth: String) {
        val current = _uiState.value
        if (current is ImportUiState.AwaitingConfirmation) {
            _uiState.value = current.copy(scheduleMonth = newMonth)
        }
    }

    /** Resolves employees, remaps shift IDs, and persists everything. */
    fun onConfirmImport() {
        val current = _uiState.value as? ImportUiState.AwaitingConfirmation ?: return
        val parseResult = cachedParseResult ?: return

        _uiState.value = ImportUiState.Saving
        viewModelScope.launch {
            try {
                val confirmedMonth = current.scheduleMonth

                // Resolve / create employees; get tempId -> realId map
                val idMap = resolver.resolveOrCreate(parseResult.employeeNames)

                // Remap shifts: replace temp employeeId with real DB id
                val remappedShifts = parseResult.shifts.mapNotNull { shift ->
                    val realId = idMap[shift.employeeId]
                    if (realId == null || realId == 0L) null  // skip unresolvable
                    else shift.copy(
                        employeeId = realId,
                        sourceScheduleDate = confirmedMonth
                    )
                }

                shiftRepository.importShifts(remappedShifts, confirmedMonth, cachedFileName)

                _uiState.value = ImportUiState.Success(
                    shiftCount = remappedShifts.size,
                    employeeCount = idMap.values.count { it > 0 }
                )
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(
                    e.message ?: "Failed to save shifts"
                )
            }
        }
    }

    fun onDismissError() {
        _uiState.value = ImportUiState.Idle
    }

    fun onCancel() {
        cachedParseResult = null
        cachedFileName = ""
        _uiState.value = ImportUiState.Idle
    }
}
