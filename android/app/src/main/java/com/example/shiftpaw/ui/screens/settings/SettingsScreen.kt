package com.example.shiftpaw.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToImport: () -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val showClearMonthDialog by viewModel.showClearMonthDialog.collectAsState()
    val showClearAllDialog by viewModel.showClearAllDialog.collectAsState()
    val clearResult by viewModel.clearMonthResult.collectAsState()

    // Show snackbar when a clear operation completes
    LaunchedEffect(clearResult) {
        when (clearResult) {
            is SettingsViewModel.ClearResult.Success -> {
                val month = (clearResult as SettingsViewModel.ClearResult.Success).month
                snackbarHostState.showSnackbar(
                    message = "Cleared shifts for $month",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearMonthResultShown()
            }
            is SettingsViewModel.ClearResult.Error -> {
                val msg = (clearResult as SettingsViewModel.ClearResult.Error).message
                snackbarHostState.showSnackbar(
                    message = "Error: $msg",
                    duration = SnackbarDuration.Long
                )
                viewModel.clearMonthResultShown()
            }
            null -> Unit
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Section: Schedule
            SectionHeader("Schedule")

            ListItem(
                headlineContent = { Text("Import Schedule") },
                supportingContent = { Text("Load from DOCX file") },
                leadingContent = {
                    Icon(
                        Icons.Filled.FileUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable { onNavigateToImport() }
            )

            HorizontalDivider()

            // Section: Data Management
            SectionHeader("Data Management")

            ListItem(
                headlineContent = { Text("Clear Month's Shifts") },
                supportingContent = { Text("Remove all shifts from a specific month") },
                leadingContent = {
                    Icon(
                        Icons.Filled.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable { viewModel.showClearMonthDialog() }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Reset Calendar") },
                supportingContent = { Text("Clear all schedules completely") },
                leadingContent = {
                    Icon(
                        Icons.Filled.RestoreFromTrash,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable { viewModel.showClearAllDialog() }
            )

            HorizontalDivider()

            // Section: Display
            SectionHeader("Display")

            ListItem(
                headlineContent = { Text("Dark Mode") },
                supportingContent = { Text("Override system theme") },
                leadingContent = {
                    Icon(
                        Icons.Filled.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode() }
                    )
                }
            )

            HorizontalDivider()

            // Section: About
            SectionHeader("About")

            ListItem(
                headlineContent = { Text("ShiftPaw") },
                supportingContent = { Text("Version 1.0.0-beta") },
                leadingContent = {
                    Icon(
                        Icons.Filled.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            ListItem(
                headlineContent = { Text("About") },
                supportingContent = { Text("Veterinary staff schedule viewer") },
                leadingContent = {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            HorizontalDivider()
        }
    }

    // Clear Month dialog
    if (showClearMonthDialog) {
        ClearMonthDialog(
            onDismiss = viewModel::dismissClearMonthDialog,
            onConfirm = viewModel::clearShiftsForMonth
        )
    }

    // Clear All confirmation dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearAllDialog,
            icon = {
                Icon(
                    Icons.Filled.RestoreFromTrash,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Reset Calendar") },
            text = {
                Text(
                    "This will permanently delete ALL imported schedules and shifts. " +
                    "This action cannot be undone. Are you sure?"
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::clearAllShifts) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissClearAllDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ClearMonthDialog(
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    var pickerYear by remember { mutableIntStateOf(YearMonth.now().year) }
    var pickerMonth by remember { mutableIntStateOf(YearMonth.now().monthValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Filled.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Clear Month") },
        text = {
            Column {
                Text(
                    "Select a month to clear all shifts:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                // Year selector row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(onClick = { pickerYear-- }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous year")
                    }
                    Text(
                        text = pickerYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    androidx.compose.material3.IconButton(onClick = { pickerYear++ }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next year")
                    }
                }
                androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                // Month grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(12) { idx ->
                        val m = idx + 1
                        val isSelected = pickerMonth == m
                        FilterChip(
                            selected = isSelected,
                            onClick = { pickerMonth = m },
                            label = {
                                Text(
                                    text = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.error,
                                selectedLabelColor = MaterialTheme.colorScheme.onError
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(YearMonth.of(pickerYear, pickerMonth)) }) {
                Text("Clear", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}
