package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BackupImportMode
import com.example.data.CurriculumCatalog
import com.example.ui.components.AttendanceBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberProgressBar
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.StudentProfileCard
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.CyberCyanSecondary
import com.example.ui.theme.CyberNavyBackground
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPartialAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.viewmodel.AcademyUiState

@Composable
fun ProgressSettingsScreen(
    uiState: AcademyUiState,
    onExportJson: ((String) -> Unit) -> Unit,
    onImportJson: (String, BackupImportMode) -> Unit,
    onResetDemoData: () -> Unit,
    onUndoLastResetOrReplace: () -> Unit = {},
    onExportCsv: ((String) -> Unit) -> Unit = {},
    onExportMarkdown: ((String) -> Unit) -> Unit = {},
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onToggleReducedMotion: (Boolean) -> Unit = {},
    onOpenAuthScreen: () -> Unit = {},
    onLogOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetConfirmation by rememberSaveable { mutableStateOf(false) }
    var showImportDialog by rememberSaveable { mutableStateOf(false) }
    var importJsonDraft by rememberSaveable { mutableStateOf("") }
    var selectedImportMode by rememberSaveable { mutableStateOf(BackupImportMode.REPLACE) }

    val createJsonDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportJson { json ->
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
            }
        }
    }

    val openJsonDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                if (!content.isNullOrBlank()) {
                    onImportJson(content, selectedImportMode)
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("progress_settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "Analytics, Backup & Settings",
                subtitle = "Task & 26-week progress, study time, attendance history, Merge/Replace JSON, CSV & Markdown export",
                badgeText = "Version 2.0"
            )
        }

        // 1. Core Learning Analytics Card
        item {
            CyberCard(borderColor = CyberCyanPrimary.copy(alpha = 0.45f)) {
                Text(
                    text = "Core Learning Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryWhite
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Task Completion Percentage",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = "${uiState.completedTasksCount} of ${uiState.totalTasksCount} Week 1 tasks • ${uiState.completedLessonsCount}/182 Roadmap lessons",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    Text(
                        text = "${uiState.taskCompletionPercentage}%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CyberCyanPrimary,
                        modifier = Modifier.testTag("progress_completion_percentage")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                CyberProgressBar(progress = uiState.overallProgressFraction)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberNavySurface)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Actual Study Time Logged",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = "${uiState.studySessions.size} saved session(s) • Daily goal: 90 min",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    Text(
                        text = "${uiState.totalStudyMinutes} min",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = StatusPresentGreen,
                        modifier = Modifier.testTag("progress_actual_study_time")
                    )
                }
            }
        }

        // 2. 7-Phase Roadmap Progress Breakdown
        item {
            CyberCard {
                Text(
                    text = "26-Week Phase Progress Breakdown (${uiState.completedLessonsCount}/182 Days)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryWhite
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurriculumCatalog.phases.forEach { phase ->
                        val phaseDays = ((phase.weeks.first - 1) * 7 + 1)..(phase.weeks.last * 7)
                        val totalPhaseDays = phaseDays.count()
                        val donePhaseDays = phaseDays.count { day ->
                            uiState.lessonProgressMap[day]?.isCompleted == true
                        }
                        val pct = if (totalPhaseDays > 0) (donePhaseDays * 100) / totalPhaseDays else 0
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Phase ${phase.phaseNumber}: ${phase.title} (Wks ${phase.weeks.first}-${phase.weeks.last})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimaryWhite
                                )
                                Text(
                                    text = "$donePhaseDays/$totalPhaseDays ($pct%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyanPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            CyberProgressBar(
                                progress = if (totalPhaseDays > 0) donePhaseDays.toFloat() / totalPhaseDays else 0f,
                                heightDp = 6
                            )
                        }
                    }
                }
            }
        }

        // 3. Attendance History Card
        item {
            CyberCard(
                modifier = Modifier.testTag("progress_attendance_history")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attendance History (${uiState.attendanceRecords.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimaryWhite
                    )
                    Text(
                        text = "Streak: ${uiState.learningStreakDays}d",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyanPrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.attendanceRecords.isEmpty()) {
                    Text(
                        text = "No attendance records recorded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate,
                        modifier = Modifier.testTag("progress_empty_attendance")
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.attendanceRecords.forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyberNavyElevated.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.date,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextPrimaryWhite
                                    )
                                    if (record.note.isNotBlank()) {
                                        Text(
                                            text = record.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondarySlate
                                        )
                                    }
                                }
                                AttendanceBadge(statusLabel = record.status)
                            }
                        }
                    }
                }
            }
        }

        // 4. Data Backup: JSON (Merge/Replace), CSV, Markdown Report & Undo Snapshot
        item {
            CyberCard {
                Text(
                    text = "Data Backup: JSON (Merge/Replace), CSV & Weekly Report",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Export Schema v2 JSON, CSV spreadsheets, or printable Markdown reports. Before any Replace or Reset, an automatic Undo Snapshot is saved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onExportJson { json ->
                                copyToClipboard(context, json)
                                saveLocalBackupFile(context, json)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyanPrimary,
                            contentColor = CyberNavyBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_json_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export JSON",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export JSON")
                    }

                    OutlinedButton(
                        onClick = {
                            importJsonDraft = uiState.lastExportedJson ?: ""
                            showImportDialog = true
                        },
                        border = BorderStroke(1.dp, CyberCyanSecondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("import_json_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Import JSON",
                            tint = CyberCyanSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import JSON", color = TextPrimaryWhite)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onExportCsv { csv -> copyToClipboard(context, csv) }
                        },
                        border = BorderStroke(1.dp, CyberNavyBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Export CSV",
                            tint = CyberCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", style = MaterialTheme.typography.labelMedium, color = TextPrimaryWhite)
                    }

                    OutlinedButton(
                        onClick = {
                            onExportMarkdown { md -> copyToClipboard(context, md) }
                        },
                        border = BorderStroke(1.dp, CyberNavyBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_markdown_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Export Weekly Report",
                            tint = CyberCyanSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Weekly Report", style = MaterialTheme.typography.labelMedium, color = TextPrimaryWhite)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            createJsonDocumentLauncher.launch("waqas_cyber_academy_backup.json")
                        },
                        border = BorderStroke(1.dp, CyberNavyBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = "Save JSON File",
                            tint = CyberCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save to File", style = MaterialTheme.typography.labelMedium, color = TextPrimaryWhite)
                    }

                    OutlinedButton(
                        onClick = {
                            openJsonDocumentLauncher.launch("application/json")
                        },
                        border = BorderStroke(1.dp, CyberNavyBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Load JSON File",
                            tint = CyberCyanSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Load from File", style = MaterialTheme.typography.labelMedium, color = TextPrimaryWhite)
                    }
                }

                if (uiState.latestUndoSnapshot != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onUndoLastResetOrReplace,
                        border = BorderStroke(1.dp, StatusPartialAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("undo_last_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Undo Last Reset or Replace",
                            tint = StatusPartialAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Undo Last Replace/Reset (${uiState.latestUndoSnapshot.label})",
                            color = StatusPartialAmber
                        )
                    }
                }

                if (!uiState.lastExportedJson.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exported JSON Payload (Copied to Clipboard)",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyanPrimary
                        )
                        TextButton(
                            onClick = { copyToClipboard(context, uiState.lastExportedJson) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy JSON",
                                tint = CyberCyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", color = CyberCyanPrimary)
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberNavySurface)
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = uiState.lastExportedJson,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyanSecondary,
                            modifier = Modifier.testTag("exported_json_preview")
                        )
                    }
                }
            }
        }

        // 5. Account Security & Preferences (Dark/Light Mode + Reduced Motion)
        item {
            CyberCard {
                Text(
                    text = "Account Security & Display Preferences",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryWhite
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.activeUser?.let { "Signed in: ${it.displayName} (${it.email})" }
                                ?: "Local Session (Not Signed In)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (uiState.activeUser != null) StatusPresentGreen else TextPrimaryWhite
                        )
                        Text(
                            text = "PBKDF2-HMAC-SHA256 salted password authentication",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onOpenAuthScreen,
                            border = BorderStroke(1.dp, CyberCyanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("progress_open_auth_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.activeUser != null) Icons.Default.VerifiedUser else Icons.Default.Lock,
                                contentDescription = null,
                                tint = CyberCyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (uiState.activeUser != null) "Account" else "Sign In", color = CyberCyanPrimary)
                        }
                        if (uiState.activeUser != null) {
                            OutlinedButton(
                                onClick = onLogOut,
                                border = BorderStroke(1.dp, StatusAbsentRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("progress_logout_button")
                            ) {
                                Text("Log Out", color = StatusAbsentRed)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Navy Cybersecurity Theme", style = MaterialTheme.typography.bodyMedium, color = TextPrimaryWhite)
                        Text("Toggle between Dark Navy and high-contrast Light mode", style = MaterialTheme.typography.bodySmall, color = TextSecondarySlate)
                    }
                    Switch(
                        checked = uiState.darkThemeEnabled,
                        onCheckedChange = onToggleDarkTheme,
                        modifier = Modifier.testTag("toggle_dark_theme_button")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Reduced Motion Mode", style = MaterialTheme.typography.bodyMedium, color = TextPrimaryWhite)
                        Text("Disables progress bar & transition animations for instant rendering", style = MaterialTheme.typography.bodySmall, color = TextSecondarySlate)
                    }
                    Switch(
                        checked = uiState.reducedMotionEnabled,
                        onCheckedChange = onToggleReducedMotion,
                        modifier = Modifier.testTag("toggle_reduced_motion_button")
                    )
                }
            }
        }

        // 6. Reset Demo Data Card with Confirmation
        item {
            CyberCard(
                borderColor = StatusAbsentRed.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "Reset Prototype Data",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Clears all attendance records, study timer sessions, learning summaries, and resets all 5 Week 1 tasks back to 0% completion (saves an Undo Snapshot first).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showResetConfirmation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusAbsentRed,
                        contentColor = TextPrimaryWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reset_demo_data_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Reset Demo Data",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Demo Data", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // 7. Persistent Storage Information Card
        item {
            CyberCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Persistent Storage Info",
                        tint = CyberCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Persistent Local Storage Architecture (Schema v2)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimaryWhite
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "All records (Daily Tasks, Attendance, Study Sessions, Learning Summaries, 182-Day Lesson & Resource Progress, Projects, Notes, and Auth Accounts) are persisted in SQLite via Jetpack Room ('waqas_cyber_academy_v1.db') with automatic v1->v2 non-destructive migration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
            }
        }

        // 8. Profile Reference
        item {
            StudentProfileCard(profile = uiState.profile)
        }
    }

    // Confirmation Dialog for Reset Demo Data
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            containerColor = CyberNavyCard,
            titleContentColor = TextPrimaryWhite,
            textContentColor = TextSecondarySlate,
            title = {
                Text("Confirm Reset Demo Data?")
            },
            text = {
                Text(
                    "This will clear saved attendance records, study timer sessions, and learning summaries, and uncheck all Week 1 tasks back to 0% progress. An Undo Snapshot will be created automatically."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDemoData()
                        showResetConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusAbsentRed,
                        contentColor = TextPrimaryWhite
                    ),
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirmation = false },
                    modifier = Modifier.testTag("cancel_reset_button")
                ) {
                    Text("Cancel", color = TextSecondarySlate)
                }
            }
        )
    }

    // Dialog for Pasting / Editing JSON to Import with Merge vs Replace selector
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = CyberNavyCard,
            titleContentColor = TextPrimaryWhite,
            textContentColor = TextPrimaryWhite,
            title = {
                Text("Import JSON Backup")
            },
            text = {
                Column {
                    Text(
                        text = "Select Import Mode (Merge keeps existing records; Replace creates an Undo Snapshot first):",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BackupImportMode.entries.forEach { mode ->
                            val selected = selectedImportMode == mode
                            OutlinedButton(
                                onClick = { selectedImportMode = mode },
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) CyberCyanPrimary else CyberNavyBorder
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("import_mode_${mode.name.lowercase()}")
                            ) {
                                Text(
                                    text = if (mode == BackupImportMode.MERGE) "Merge" else "Replace",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) CyberCyanPrimary else TextPrimaryWhite
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importJsonDraft,
                        onValueChange = { importJsonDraft = it },
                        label = { Text("JSON Backup Payload") },
                        minLines = 5,
                        maxLines = 10,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyanPrimary,
                            unfocusedBorderColor = CyberNavyBorder,
                            focusedLabelColor = CyberCyanPrimary,
                            unfocusedLabelColor = TextSecondarySlate,
                            focusedTextColor = TextPrimaryWhite,
                            unfocusedTextColor = TextPrimaryWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_json_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onImportJson(importJsonDraft, selectedImportMode)
                        showImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyanPrimary,
                        contentColor = CyberNavyBackground
                    ),
                    modifier = Modifier.testTag("confirm_import_json_button")
                ) {
                    Text("Import & Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportDialog = false },
                    modifier = Modifier.testTag("cancel_import_json_button")
                ) {
                    Text("Cancel", color = TextSecondarySlate)
                }
            }
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("Waqas Cyber Academy Backup", text))
    }
}

private fun saveLocalBackupFile(context: Context, json: String) {
    runCatching {
        context.openFileOutput("waqas_cyber_academy_backup.json", Context.MODE_PRIVATE).use { out ->
            out.write(json.toByteArray(Charsets.UTF_8))
        }
    }
}
