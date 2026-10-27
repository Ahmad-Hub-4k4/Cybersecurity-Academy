package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AttendanceStatus
import com.example.data.CurriculumCatalog
import com.example.ui.components.AttendanceBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberProgressBar
import com.example.ui.components.CyberStatCard
import com.example.ui.components.StudentProfileCard
import com.example.ui.components.attendanceStatusColor
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.CyberCyanSecondary
import com.example.ui.theme.CyberNavyBackground
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusPartialAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.viewmodel.AcademySection
import com.example.viewmodel.AcademyUiState
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: AcademyUiState,
    onNavigate: (AcademySection) -> Unit,
    onQuickMarkPresentToday: () -> Unit,
    onToggleTask: (Int, Boolean) -> Unit,
    onOpenAuth: () -> Unit = {},
    onOpenLessonInRoadmap: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val recommendedDayNumber = remember(uiState.lessonProgressMap) {
        (1..182).firstOrNull { day ->
            uiState.lessonProgressMap[day]?.isCompleted != true
        } ?: 1
    }
    val recommendedLesson = remember(recommendedDayNumber) {
        CurriculumCatalog.getLessonByDay(recommendedDayNumber)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Welcome Card with Banner, Date, Study Plan, Current Week & Phase
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CyberCyanPrimary.copy(alpha = 0.45f)),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_cyber_hero_banner_1791611569291),
                        contentDescription = "Cybersecurity Command Center Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(225.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(225.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        CyberNavyBackground.copy(alpha = 0.65f),
                                        CyberNavyBackground.copy(alpha = 0.94f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = CyberCyanPrimary.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, CyberCyanPrimary.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "WEEK ${recommendedLesson.weekNumber} • PHASE ${recommendedLesson.phaseNumber}: ${recommendedLesson.phaseTitle.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyanPrimary,
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag("dashboard_week_phase")
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "Nav %.2fms", uiState.lastNavigationLatencyMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusPresentGreen,
                                modifier = Modifier.testTag("dashboard_nav_latency_badge")
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Welcome back, Waqas.",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimaryWhite,
                            modifier = Modifier.testTag("welcome_message")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.formattedTodayDate} • Today's Plan: ${uiState.profile.dailyStudyMinutesGoal} min hands-on Linux CLI in Kali VirtualBox",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate,
                            modifier = Modifier.testTag("dashboard_today_plan")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onNavigate(AcademySection.DAILY_TASKS) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberCyanPrimary,
                                    contentColor = CyberNavyBackground
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("dashboard_open_tasks_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = "Open Daily Tasks",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Week 1 Tasks")
                            }
                            OutlinedButton(
                                onClick = { onNavigate(AcademySection.ROADMAP) },
                                border = BorderStroke(1.dp, CyberCyanSecondary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyanSecondary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("dashboard_open_roadmap_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Open 26-Week Roadmap",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("26-Wk Roadmap")
                            }
                            OutlinedButton(
                                onClick = { onNavigate(AcademySection.STUDY_TIMER) },
                                border = BorderStroke(1.dp, CyberCyanPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyanPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("dashboard_open_timer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start 90m Timer",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("90m")
                            }
                        }
                    }
                }
            }
        }

        // 2. Overall Progress Card (Core Week 1 Tasks + 182-Day Curriculum Progress)
        item {
            CyberCard(
                borderColor = CyberCyanPrimary.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Overall Progress (Week 1 Foundations & 26-Week Roadmap)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = "${uiState.completedTasksCount} of ${uiState.totalTasksCount} core tasks • ${uiState.completedLessonsCount}/182 curriculum days • ${uiState.totalStudyMinutes} min logged",
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
                        modifier = Modifier.testTag("dashboard_overall_progress")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                CyberProgressBar(progress = uiState.overallProgressFraction)
            }
        }

        // 3. Key Metrics 2x2 Grid (Attendance Status, Total Study Minutes, Completed Tasks, Learning Streak)
        item {
            val attendanceLabel = uiState.todayAttendance?.status ?: "Not Marked"
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberStatCard(
                        label = "Attendance Status",
                        value = attendanceLabel,
                        subtitle = "Today (${uiState.todayIso})",
                        icon = Icons.Default.CalendarMonth,
                        accentColor = attendanceStatusColor(uiState.todayAttendance?.status),
                        valueTestTag = "dashboard_attendance_status",
                        modifier = Modifier.weight(1f)
                    )
                    CyberStatCard(
                        label = "Total Study Minutes",
                        value = "${uiState.totalStudyMinutes} min",
                        subtitle = "Today: ${uiState.todayStudyMinutes} / 90 min",
                        icon = Icons.Default.Timer,
                        accentColor = CyberCyanPrimary,
                        valueTestTag = "dashboard_total_minutes",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberStatCard(
                        label = "Completed Tasks",
                        value = "${uiState.completedTasksCount} / ${uiState.totalTasksCount}",
                        subtitle = "${uiState.taskCompletionPercentage}% Week 1 Complete",
                        icon = Icons.Default.CheckCircle,
                        accentColor = StatusPresentGreen,
                        valueTestTag = "dashboard_completed_tasks",
                        modifier = Modifier.weight(1f)
                    )
                    CyberStatCard(
                        label = "Learning Streak",
                        value = "${uiState.learningStreakDays} days",
                        subtitle = if (uiState.learningStreakDays > 0) "Active consistency!" else "Mark Present to start",
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = StatusPartialAmber,
                        valueTestTag = "dashboard_learning_streak",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Today's Recommended Curriculum Lesson Card (From 182-Day Roadmap)
        item {
            CyberCard(borderColor = CyberCyanSecondary.copy(alpha = 0.45f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NEXT RECOMMENDED LESSON • DAY ${recommendedLesson.dayNumber} OF 182",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyanPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recommendedLesson.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite,
                            modifier = Modifier.testTag("dashboard_recommended_lesson_title")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${recommendedLesson.estimatedMinutes} min (30m Concept • 45m Kali Lab • 15m Summary) • ${recommendedLesson.primaryResource.resourceType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onOpenLessonInRoadmap(recommendedLesson.dayNumber)
                            onNavigate(AcademySection.ROADMAP)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyanPrimary,
                            contentColor = CyberNavyBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("dashboard_start_recommended_lesson_button")
                    ) {
                        Text("Open Day ${recommendedLesson.dayNumber}")
                    }
                }
            }
        }

        // 5. Today's Attendance Quick Action Card
        item {
            CyberCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Today's Attendance Check-In",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AttendanceBadge(statusLabel = uiState.todayAttendance?.status)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (uiState.todayAttendance?.status != AttendanceStatus.PRESENT.label) {
                            Button(
                                onClick = onQuickMarkPresentToday,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StatusPresentGreen,
                                    contentColor = CyberNavyBackground
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("dashboard_quick_mark_present")
                            ) {
                                Text("Mark Present")
                            }
                        }
                        OutlinedButton(
                            onClick = { onNavigate(AcademySection.ATTENDANCE) },
                            border = BorderStroke(1.dp, CyberNavyBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("dashboard_manage_attendance_button")
                        ) {
                            Text("Calendar", color = CyberCyanPrimary)
                        }
                    }
                }
            }
        }

        // 6. Today's Study Plan & Week 1 Tasks Snapshot
        item {
            CyberCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = "Study Plan",
                            tint = CyberCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Today's Study Plan • Week 1 Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                    }
                    OutlinedButton(
                        onClick = { onNavigate(AcademySection.DAILY_TASKS) },
                        border = BorderStroke(1.dp, CyberCyanPrimary.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("All Tasks", style = MaterialTheme.typography.labelMedium, color = CyberCyanPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                uiState.tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberNavyElevated.copy(alpha = 0.55f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (task.isCompleted) StatusPresentGreen else TextPrimaryWhite
                            )
                            Text(
                                text = "${task.category} • Est. ${task.estimatedMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondarySlate
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (task.isCompleted) {
                                StatusPresentGreen.copy(alpha = 0.2f)
                            } else {
                                Color.Transparent
                            },
                            border = BorderStroke(
                                1.dp,
                                if (task.isCompleted) StatusPresentGreen else CyberNavyBorder
                            ),
                            onClick = { onToggleTask(task.id, !task.isCompleted) }
                        ) {
                            Text(
                                text = if (task.isCompleted) "Done" else "${task.estimatedMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (task.isCompleted) StatusPresentGreen else CyberCyanSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7. Quick Reflection, Projects & Account Security Shortcut Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigate(AcademySection.LEARNING_SUMMARY) },
                        border = BorderStroke(1.dp, CyberCyanSecondary.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_open_summary_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Write Summary",
                            tint = CyberCyanSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Summary (${uiState.learningSummaries.size})", color = TextPrimaryWhite)
                    }
                    OutlinedButton(
                        onClick = { onNavigate(AcademySection.PROGRESS_SETTINGS) },
                        border = BorderStroke(1.dp, CyberNavyBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_open_progress_button")
                    ) {
                        Text("Progress & Backup", color = CyberCyanPrimary)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigate(AcademySection.PROJECTS_LIBRARY) },
                        border = BorderStroke(1.dp, CyberNavyBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_open_projects_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Projects & Notes",
                            tint = CyberCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Projects (${uiState.projects.size})", color = TextPrimaryWhite)
                    }
                    OutlinedButton(
                        onClick = onOpenAuth,
                        border = BorderStroke(
                            1.dp,
                            if (uiState.activeUser != null) StatusPresentGreen else CyberCyanPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_open_auth_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.activeUser != null) Icons.Default.VerifiedUser else Icons.Default.Lock,
                            contentDescription = "Account Authentication",
                            tint = if (uiState.activeUser != null) StatusPresentGreen else CyberCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.activeUser?.displayName ?: "Account Login",
                            color = if (uiState.activeUser != null) StatusPresentGreen else CyberCyanPrimary
                        )
                    }
                }
            }
        }

        // 8. Student Profile Card (Waqas Ahmad)
        item {
            StudentProfileCard(profile = uiState.profile)
        }
    }
}
