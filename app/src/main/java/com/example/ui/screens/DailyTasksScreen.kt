package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.DailyTaskEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberProgressBar
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.CyberCyanSecondary
import com.example.ui.theme.CyberNavyBackground
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.viewmodel.AcademyUiState

@Composable
fun DailyTasksScreen(
    uiState: AcademyUiState,
    onToggleTask: (Int, Boolean) -> Unit,
    onSaveTaskNotes: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuizDialog by rememberSaveable { mutableStateOf(false) }

    val totalEstimatedMinutes = uiState.tasks.sumOf { it.estimatedMinutes }
    val completedMinutes = uiState.tasks.filter { it.isCompleted }.sumOf { it.estimatedMinutes }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("daily_tasks_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "Week 1 Daily Tasks",
                subtitle = "Phase 1: Linux & CLI Foundations • Kali Linux in VirtualBox",
                badgeText = "${uiState.completedTasksCount}/${uiState.totalTasksCount} Done"
            )
        }

        // Week 1 Progress Summary Card
        item {
            CyberCard(borderColor = CyberCyanPrimary.copy(alpha = 0.4f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Week 1 Lab Checklist Progress",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = "$completedMinutes / $totalEstimatedMinutes min estimated • ${uiState.completedTasksCount} of ${uiState.totalTasksCount} completed",
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
                        modifier = Modifier.testTag("tasks_progress_percentage")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                CyberProgressBar(progress = uiState.overallProgressFraction)
            }
        }

        // Individual Task Cards
        items(uiState.tasks, key = { it.id }) { task ->
            DailyTaskCard(
                task = task,
                onToggleTask = { checked -> onToggleTask(task.id, checked) },
                onSaveNotes = { notes -> onSaveTaskNotes(task.id, notes) },
                onOpenQuiz = if (task.id == 5) {
                    { showQuizDialog = true }
                } else null
            )
        }
    }

    if (showQuizDialog) {
        ShortLinuxQuizDialog(
            onDismiss = { showQuizDialog = false },
            onQuizCompleted = { scoreSummary ->
                onToggleTask(5, true)
                onSaveTaskNotes(5, scoreSummary)
                showQuizDialog = false
            }
        )
    }
}

@Composable
private fun DailyTaskCard(
    task: DailyTaskEntity,
    onToggleTask: (Boolean) -> Unit,
    onSaveNotes: (String) -> Unit,
    onOpenQuiz: (() -> Unit)? = null
) {
    var notesExpanded by rememberSaveable(task.id) { mutableStateOf(task.notes.isNotBlank()) }
    var draftNotes by rememberSaveable(task.id, task.notes) { mutableStateOf(task.notes) }

    CyberCard(
        modifier = Modifier.testTag("task_card_${task.id}"),
        borderColor = if (task.isCompleted) {
            StatusPresentGreen.copy(alpha = 0.6f)
        } else {
            CyberNavyBorder
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onToggleTask,
                colors = CheckboxDefaults.colors(
                    checkedColor = StatusPresentGreen,
                    uncheckedColor = CyberCyanSecondary,
                    checkmarkColor = CyberNavyBackground
                ),
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyberNavyElevated
                    ) {
                        Text(
                            text = "TASK #${task.id} • ${task.category.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyanSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CyberCyanPrimary.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, CyberCyanPrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Estimated duration",
                                tint = CyberCyanPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${task.estimatedMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyanPrimary,
                                modifier = Modifier.testTag("task_duration_${task.id}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) StatusPresentGreen else TextPrimaryWhite,
                    modifier = Modifier.testTag("task_title_${task.id}")
                )

                if (task.commandHint.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberNavySurface)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Command Reference",
                            tint = CyberCyanPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = task.commandHint,
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyanSecondary
                        )
                    }
                }

                if (task.notes.isNotBlank() && !notesExpanded) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Notes: ${task.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate,
                        modifier = Modifier.testTag("task_saved_note_preview_${task.id}")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { notesExpanded = !notesExpanded },
                        border = BorderStroke(1.dp, CyberNavyBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("task_toggle_notes_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Task Notes",
                            tint = CyberCyanSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (notesExpanded) "Hide Notes" else if (task.notes.isNotBlank()) "Edit Notes" else "Add Notes",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimaryWhite
                        )
                    }

                    if (onOpenQuiz != null) {
                        Button(
                            onClick = onOpenQuiz,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyanPrimary,
                                contentColor = CyberNavyBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_linux_quiz_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = "Take Short Linux Quiz",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Interactive Quiz",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = notesExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        OutlinedTextField(
                            value = draftNotes,
                            onValueChange = { draftNotes = it },
                            label = { Text("Optional lab notes for Task #${task.id}") },
                            placeholder = { Text("e.g., Tested chmod 750 in ~/cyber_lab on Kali VM") },
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
                                .testTag("task_notes_input_${task.id}")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { onSaveNotes(draftNotes) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberCyanPrimary,
                                    contentColor = CyberNavyBackground
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("task_notes_save_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Task Notes",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Note")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortLinuxQuizDialog(
    onDismiss: () -> Unit,
    onQuizCompleted: (String) -> Unit
) {
    var q1Answer by remember { mutableIntStateOf(0) }
    var q2Answer by remember { mutableIntStateOf(1) }
    var q3Answer by remember { mutableIntStateOf(2) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberNavyCard,
        titleContentColor = TextPrimaryWhite,
        textContentColor = TextPrimaryWhite,
        title = {
            Text(
                text = "Week 1: Short Linux Quiz",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuizQuestionBlock(
                    question = "1. Which command displays the full path of your current working directory?",
                    options = listOf("pwd", "ls -a", "mkdir"),
                    selectedIndex = q1Answer,
                    onSelect = { q1Answer = it }
                )
                QuizQuestionBlock(
                    question = "2. Which flag prompts for confirmation before overwriting or deleting files with cp, mv, or rm?",
                    options = listOf("-f (force)", "-i (interactive)", "-r (recursive)"),
                    selectedIndex = q2Answer,
                    onSelect = { q2Answer = it }
                )
                QuizQuestionBlock(
                    question = "3. What does 'chmod 750 script.sh' grant to the file owner?",
                    options = listOf("Read only (r--)", "Read and Execute (r-x)", "Read, Write, and Execute (rwx)"),
                    selectedIndex = q3Answer,
                    onSelect = { q3Answer = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var score = 0
                    if (q1Answer == 0) score++
                    if (q2Answer == 1) score++
                    if (q3Answer == 2) score++
                    onQuizCompleted("Completed Linux Quiz — Score: $score/3 (pwd, -i safe flag, chmod 750 rwx)")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyanPrimary,
                    contentColor = CyberNavyBackground
                ),
                modifier = Modifier.testTag("submit_linux_quiz_button")
            ) {
                Text("Submit & Complete Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondarySlate)
            }
        }
    )
}

@Composable
private fun QuizQuestionBlock(
    question: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = CyberCyanPrimary
        )
        options.forEachIndexed { idx, opt ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = selectedIndex == idx,
                    onClick = { onSelect(idx) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = CyberCyanPrimary,
                        unselectedColor = TextSecondarySlate
                    )
                )
                Text(
                    text = opt,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimaryWhite
                )
            }
        }
    }
}
