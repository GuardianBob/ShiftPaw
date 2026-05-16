package com.example.shiftpaw.ui.screens.importschedule

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shiftpaw.ui.theme.ShiftPawTheme

private val SuccessGreen = Color(0xFF43A047)
private val WarningAmber = Color(0xFFFB8C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScheduleScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    viewModel: ImportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onFileSelected(uri, context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Schedule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ImportUiState.Idle -> IdleContent(
                    onSelectFile = {
                        filePicker.launch(
                            arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        )
                    }
                )

                is ImportUiState.Parsing -> LoadingContent("Parsing schedule…")

                is ImportUiState.AwaitingConfirmation -> ConfirmationContent(
                    state = state,
                    onMonthEdited = viewModel::onMonthEdited,
                    onCancel = viewModel::onCancel,
                    onConfirm = viewModel::onConfirmImport,
                )

                is ImportUiState.Saving -> LoadingContent("Saving shifts…")

                is ImportUiState.Success -> SuccessContent(
                    shiftCount = state.shiftCount,
                    employeeCount = state.employeeCount,
                    onViewCalendar = onNavigateToCalendar,
                )

                is ImportUiState.Error -> ErrorContent(
                    message = state.message,
                    onTryAgain = viewModel::onDismissError,
                )
            }
        }
    }
}

// ── Idle ─────────────────────────────────────────────────────────────────────

@Composable
private fun IdleContent(onSelectFile: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "No file selected",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Supported: .docx VetCalendar schedule files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = onSelectFile) {
                    Text("Select Schedule File")
                }
            }
        }
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent(label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Awaiting Confirmation ─────────────────────────────────────────────────────

@Composable
private fun ConfirmationContent(
    state: ImportUiState.AwaitingConfirmation,
    onMonthEdited: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = state.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = state.scheduleMonth,
                    onValueChange = onMonthEdited,
                    label = { Text("Schedule Month") },
                    placeholder = { Text("yyyy-MM  e.g. 2026-05") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Employee chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("${state.employeeCount} employees found") }
                    )
                }

                Text(
                    text = "${state.shiftCount} shifts found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Warnings
        if (state.warnings.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = WarningAmber.copy(alpha = 0.12f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Warnings (${state.warnings.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarningAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    state.warnings.forEach { warning ->
                        Text(
                            text = "• $warning",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningAmber
                        )
                    }
                }
            }
        }

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            ) {
                Text("Import")
            }
        }
    }
}

// ── Success ───────────────────────────────────────────────────────────────────

@Composable
private fun SuccessContent(
    shiftCount: Int,
    employeeCount: Int,
    onViewCalendar: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = SuccessGreen
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Import successful!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = SuccessGreen
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$shiftCount shifts imported for $employeeCount employees",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onViewCalendar) {
            Text("View Calendar")
        }
    }
}

// ── Error ─────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(message: String, onTryAgain: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Import failed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onTryAgain) {
            Text("Try Again")
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun IdlePreview() {
    ShiftPawTheme { IdleContent(onSelectFile = {}) }
}

@Preview(showBackground = true)
@Composable
fun SuccessPreview() {
    ShiftPawTheme { SuccessContent(shiftCount = 87, employeeCount = 4, onViewCalendar = {}) }
}

@Preview(showBackground = true)
@Composable
fun ErrorPreview() {
    ShiftPawTheme { ErrorContent(message = "No tables found in document", onTryAgain = {}) }
}
