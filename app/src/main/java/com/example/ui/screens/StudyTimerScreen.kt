package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberProgressBar
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.CyberCyanSecondary
import com.example.ui.theme.CyberNavyBackground
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusPartialAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.viewmodel.AcademyUiState
import com.example.viewmodel.TimerStatus

@Composable
fun StudyTimerScreen(
    uiState: AcademyUiState,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onAdvanceSeconds: (Int) -> Unit,
    onSaveSession: () -> Unit,
    onUpdateTopic: (String) -> Unit,
    onConfigureDuration: (minutes: Int, isBreak: Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val timer = uiState.timer
    val statusColor = when (timer.status) {
        TimerStatus.IDLE -> TextSecondarySlate
        TimerStatus.RUNNING -> CyberCyanPrimary
        TimerStatus.PAUSED -> StatusPartialAmber
        TimerStatus.COMPLETED -> StatusPresentGreen
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("study_timer_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "90-Minute Study Timer",
                subtitle = "Wall-clock accurate Kali Linux & Cybersecurity Session Tracker with Break Presets",
                badgeText = "Total: ${uiState.totalStudyMinutes} min"
            )
        }

        // Configurable Duration & Break Presets Row
        item {
            CyberCard {
                Text(
                    text = "Session & Break Duration Presets",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        TimerPresetChip(
                            label = "90m Deep Study",
                            selected = !timer.isBreakMode && timer.targetMinutes == 90,
                            onClick = { onConfigureDuration(90, false) },
                            tag = "timer_preset_90m"
                        )
                    }
                    item {
                        TimerPresetChip(
                            label = "45m Lab Sprint",
                            selected = !timer.isBreakMode && timer.targetMinutes == 45,
                            onClick = { onConfigureDuration(45, false) },
                            tag = "timer_preset_45m"
                        )
                    }
                    item {
                        TimerPresetChip(
                            label = "25m Pomodoro",
                            selected = !timer.isBreakMode && timer.targetMinutes == 25,
                            onClick = { onConfigureDuration(25, false) },
                            tag = "timer_preset_25m"
                        )
                    }
                    item {
                        TimerPresetChip(
                            label = "5m Short Break",
                            selected = timer.isBreakMode && timer.targetMinutes == 5,
                            onClick = { onConfigureDuration(5, true) },
                            tag = "timer_preset_break_5m",
                            isBreak = true
                        )
                    }
                    item {
                        TimerPresetChip(
                            label = "15m Long Break",
                            selected = timer.isBreakMode && timer.targetMinutes == 15,
                            onClick = { onConfigureDuration(15, true) },
                            tag = "timer_preset_break_15m",
                            isBreak = true
                        )
                    }
                }
            }
        }

        // Main Countdown & Controls Card
        item {
            CyberCard(
                borderColor = statusColor.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (timer.isBreakMode) {
                                "Break Timer: ${timer.targetMinutes} Minutes"
                            } else {
                                "Target: ${timer.targetMinutes} Minutes (Day ${timer.associatedDayNumber})"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = timer.status.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .testTag("timer_status_badge")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Large Monospace Remaining Time Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberNavySurface)
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (timer.isBreakMode) "BREAK REMAINING" else "REMAINING TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondarySlate
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = timer.formattedRemaining,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (timer.isBreakMode) StatusPresentGreen else CyberCyanPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("timer_remaining_display")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Elapsed: ${timer.formattedElapsed} (${timer.elapsedMinutes} min studied)",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondarySlate,
                            modifier = Modifier.testTag("timer_elapsed_display")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                CyberProgressBar(progress = timer.progressFraction)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = timer.sessionTopic,
                    onValueChange = onUpdateTopic,
                    label = { Text("Session Focus / Associated Lesson Topic") },
                    singleLine = true,
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
                        .testTag("timer_topic_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Timer Control Buttons: Start, Pause, Resume, Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onStartTimer,
                        enabled = timer.status == TimerStatus.IDLE,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyanPrimary,
                            contentColor = CyberNavyBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("timer_start_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Timer",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start")
                    }

                    Button(
                        onClick = onPauseTimer,
                        enabled = timer.status == TimerStatus.RUNNING,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusPartialAmber,
                            contentColor = CyberNavyBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("timer_pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause Timer",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pause")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onResumeTimer,
                        enabled = timer.status == TimerStatus.PAUSED,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusPresentGreen,
                            contentColor = CyberNavyBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("timer_resume_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume Timer",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resume")
                    }

                    OutlinedButton(
                        onClick = onResetTimer,
                        border = BorderStroke(1.dp, CyberNavyBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("timer_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = CyberCyanSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", color = TextPrimaryWhite)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Elapsed Time Stepper for fast verification & manual adjustment
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyberNavySurface,
                    border = BorderStroke(1.dp, CyberNavyBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Fast forward elapsed study minutes",
                                tint = CyberCyanSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quick Elapsed Adjuster (Testing & Manual Adjustment)",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyanSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onAdvanceSeconds(60) },
                                border = BorderStroke(1.dp, CyberCyanSecondary.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("timer_advance_1m")
                            ) {
                                Text("+1m", style = MaterialTheme.typography.labelMedium, color = TextPrimaryWhite)
                            }
                            OutlinedButton(
                                onClick = { onAdvanceSeconds(15 * 60) },
                                border = BorderStroke(1.dp, CyberCyanSecondary.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("timer_advance_15m")
                            ) {
                                Text("+15m", style = MaterialTheme.typography.labelMedium, color = TextPrimaryWhite)
                            }
                            OutlinedButton(
                                onClick = { onAdvanceSeconds(30 * 60) },
                                border = BorderStroke(1.dp, CyberCyanSecondary.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("timer_advance_30m")
                            ) {
                                Text("+30m", style = MaterialTheme.typography.labelMedium, color = TextPrimaryWhite)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Save Actual Study Minutes Button
                Button(
                    onClick = onSaveSession,
                    enabled = timer.elapsedSeconds > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusPresentGreen,
                        contentColor = CyberNavyBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("timer_save_session_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Actual Study Minutes",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (timer.elapsedSeconds > 0) {
                            "Save Actual Study Session (${timer.savableMinutes} min)"
                        } else {
                            "Save Actual Study Minutes (Run timer first)"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: Clicking Start alone does not count a session as completed. Study minutes are recorded only when you save actual elapsed time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
            }
        }

        // Saved Study Sessions Log
        item {
            CyberCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Saved Study Sessions",
                        tint = CyberCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Saved Study Sessions (${uiState.studySessions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimaryWhite
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.studySessions.isEmpty()) {
                    Text(
                        text = "No study sessions saved yet. Total study minutes: 0 min.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate,
                        modifier = Modifier.testTag("empty_study_sessions_text")
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.studySessions.forEach { session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyberNavyElevated.copy(alpha = 0.55f))
                                    .padding(12.dp)
                                    .testTag("study_session_item_${session.id}"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = session.topic,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimaryWhite
                                    )
                                    Text(
                                        text = "Date: ${session.date}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondarySlate
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = CyberCyanPrimary.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, CyberCyanPrimary.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = CyberCyanPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${session.durationMinutes} min",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = CyberCyanPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerPresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String,
    isBreak: Boolean = false
) {
    val accent = if (isBreak) StatusPresentGreen else CyberCyanPrimary
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) accent.copy(alpha = 0.22f) else CyberNavySurface,
        border = BorderStroke(1.dp, if (selected) accent else CyberNavyBorder),
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isBreak) Icons.Default.Coffee else Icons.Default.Timer,
                contentDescription = label,
                tint = if (selected) accent else TextSecondarySlate,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) accent else TextPrimaryWhite
            )
        }
    }
}
