package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CurriculumCatalog
import com.example.data.CurriculumDayLesson
import com.example.data.LessonProgressEntity
import com.example.data.ResourceProgressEntity
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
import com.example.ui.theme.StatusPartialAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.viewmodel.AcademyUiState

@Composable
fun RoadmapScreen(
    uiState: AcademyUiState,
    onSelectPhase: (Int) -> Unit,
    onSelectWeek: (Int) -> Unit,
    onSelectLessonDay: (Int) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onSaveLessonDetails: (
        dayNumber: Int,
        isCompleted: Boolean?,
        notes: String?,
        difficultyRating: Int?,
        quizScore: Int?,
        evidenceLink: String?,
        scheduledDate: String?
    ) -> Unit,
    onSaveResourceTracking: (
        resourceId: String,
        dayNumber: Int,
        isViewed: Boolean?,
        isCompleted: Boolean?,
        personalNotes: String?,
        difficultyEncountered: String?
    ) -> Unit,
    onStartTimerForLesson: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPhaseSpec = remember(uiState.selectedPhaseNumber) {
        CurriculumCatalog.phases.first { it.phaseNumber == uiState.selectedPhaseNumber }
    }
    val visibleLessons = remember(uiState.selectedWeekNumber, uiState.roadmapSearchQuery) {
        val q = uiState.roadmapSearchQuery.trim()
        if (q.isEmpty()) {
            CurriculumCatalog.getLessonsForWeek(uiState.selectedWeekNumber)
        } else {
            CurriculumCatalog.getAllLessons().filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.weekTheme.contains(q, ignoreCase = true) ||
                    it.commandsAndTerminology.any { cmd -> cmd.contains(q, ignoreCase = true) } ||
                    "day ${it.dayNumber}".contains(q, ignoreCase = true)
            }.take(21)
        }
    }

    val selectedLesson = remember(uiState.selectedLessonDay) {
        CurriculumCatalog.getLessonByDay(uiState.selectedLessonDay)
    }
    val selectedLessonProgress = uiState.lessonProgressMap[selectedLesson.dayNumber]
        ?: LessonProgressEntity(dayNumber = selectedLesson.dayNumber)
    val selectedResourceProgress = uiState.resourceProgressMap[selectedLesson.primaryResource.resourceId]
        ?: ResourceProgressEntity(
            resourceId = selectedLesson.primaryResource.resourceId,
            dayNumber = selectedLesson.dayNumber
        )

    var showLessonQuizDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("roadmap_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "26-Week Cybersecurity Roadmap",
                subtitle = "182-Day Practical Curriculum • Phases 1–7 (Linux, Networking, Security, Pentesting, SOC, Cloud, Capstone)",
                badgeText = "${uiState.completedLessonsCount}/182 Days"
            )
        }

        // Roadmap Overall Progress & Search
        item {
            CyberCard(borderColor = CyberCyanPrimary.copy(alpha = 0.45f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "182-Day Curriculum Completion",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = "${uiState.completedLessonsCount} of 182 daily lessons completed (${uiState.roadmapCompletionPercentage}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    Text(
                        text = "${uiState.roadmapCompletionPercentage}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberCyanPrimary,
                        modifier = Modifier.testTag("roadmap_completion_percentage")
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                CyberProgressBar(progress = uiState.completedLessonsCount / 182f)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.roadmapSearchQuery,
                    onValueChange = onUpdateSearchQuery,
                    label = { Text("Search 182 lessons, commands, or topics (e.g., Nmap, Wireshark, Sigma, IAM)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Curriculum",
                            tint = CyberCyanPrimary
                        )
                    },
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
                        .testTag("roadmap_search_input")
                )
            }
        }

        // Phase Selector (Phases 1 to 7)
        item {
            Column {
                Text(
                    text = "Select Curriculum Phase (Phases 1–7)",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondarySlate
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CurriculumCatalog.phases, key = { it.phaseNumber }) { phase ->
                        val selected = phase.phaseNumber == uiState.selectedPhaseNumber
                        Surface(
                            onClick = { onSelectPhase(phase.phaseNumber) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) CyberCyanPrimary.copy(alpha = 0.22f) else CyberNavySurface,
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) CyberCyanPrimary else CyberNavyBorder
                            ),
                            modifier = Modifier.testTag("roadmap_phase_${phase.phaseNumber}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = phase.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) CyberCyanPrimary else TextPrimaryWhite
                                )
                                Text(
                                    text = "Weeks ${phase.weeks.first}–${phase.weeks.last}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                        }
                    }
                }
            }
        }

        // Week Selector within Current Phase (or all 26 weeks)
        item {
            Column {
                Text(
                    text = "${currentPhaseSpec.title} • Weeks ${currentPhaseSpec.weeks.first}–${currentPhaseSpec.weeks.last}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryWhite
                )
                Text(
                    text = currentPhaseSpec.focusSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentPhaseSpec.weeks.toList(), key = { it }) { weekNum ->
                        val selected = weekNum == uiState.selectedWeekNumber
                        Surface(
                            onClick = { onSelectWeek(weekNum) },
                            shape = RoundedCornerShape(50),
                            color = if (selected) CyberCyanPrimary else CyberNavySurface,
                            border = BorderStroke(1.dp, if (selected) CyberCyanPrimary else CyberNavyBorder),
                            modifier = Modifier.testTag("roadmap_week_$weekNum")
                        ) {
                            Text(
                                text = "Week $weekNum",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) CyberNavyBackground else TextPrimaryWhite,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7 Daily Lesson Selector Cards for Selected Week
        item {
            CyberCard {
                Text(
                    text = if (uiState.roadmapSearchQuery.isBlank()) {
                        "Week ${uiState.selectedWeekNumber} Daily Schedule (Days ${(uiState.selectedWeekNumber - 1) * 7 + 1}–${uiState.selectedWeekNumber * 7})"
                    } else {
                        "Matching Curriculum Lessons (${visibleLessons.size})"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryWhite
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visibleLessons.forEach { lesson ->
                        val prog = uiState.lessonProgressMap[lesson.dayNumber]
                        val isCompleted = prog?.isCompleted == true
                        val isSelected = lesson.dayNumber == selectedLesson.dayNumber
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) CyberCyanPrimary.copy(alpha = 0.16f)
                                    else CyberNavyElevated.copy(alpha = 0.5f)
                                )
                                .clickable { onSelectLessonDay(lesson.dayNumber) }
                                .padding(12.dp)
                                .testTag("roadmap_day_item_${lesson.dayNumber}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Day ${lesson.dayNumber} (W${lesson.weekNumber}D${lesson.dayOfWeekInCurriculum})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CyberCyanPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${lesson.estimatedMinutes}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondarySlate
                                    )
                                    if (!prog?.scheduledDate.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Scheduled: ${prog?.scheduledDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusPartialAmber
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isCompleted) StatusPresentGreen else TextPrimaryWhite
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = if (isCompleted) "Completed" else "Pending",
                                tint = if (isCompleted) StatusPresentGreen else CyberNavyBorder,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Dedicated Practical Daily Lesson View for Selected Day
        item {
            PracticalLessonDetailCard(
                lesson = selectedLesson,
                progress = selectedLessonProgress,
                resourceProgress = selectedResourceProgress,
                onSaveLessonDetails = onSaveLessonDetails,
                onSaveResourceTracking = onSaveResourceTracking,
                onOpenQuiz = { showLessonQuizDialog = true },
                onStartTimerForLesson = { onStartTimerForLesson(selectedLesson.dayNumber) },
                onCopyText = { label, text ->
                    copyTextToClipboard(context, label, text)
                }
            )
        }
    }

    if (showLessonQuizDialog) {
        LessonFiveQuestionQuizDialog(
            lesson = selectedLesson,
            onDismiss = { showLessonQuizDialog = false },
            onQuizSubmitted = { score ->
                onSaveLessonDetails(
                    selectedLesson.dayNumber,
                    null,
                    null,
                    null,
                    score,
                    null,
                    null
                )
                showLessonQuizDialog = false
            }
        )
    }
}

@Composable
private fun PracticalLessonDetailCard(
    lesson: CurriculumDayLesson,
    progress: LessonProgressEntity,
    resourceProgress: ResourceProgressEntity,
    onSaveLessonDetails: (Int, Boolean?, String?, Int?, Int?, String?, String?) -> Unit,
    onSaveResourceTracking: (String, Int, Boolean?, Boolean?, String?, String?) -> Unit,
    onOpenQuiz: () -> Unit,
    onStartTimerForLesson: () -> Unit,
    onCopyText: (String, String) -> Unit
) {
    var draftNotes by rememberSaveable(lesson.dayNumber, progress.notes) { mutableStateOf(progress.notes) }
    var draftEvidence by rememberSaveable(lesson.dayNumber, progress.evidenceLink) { mutableStateOf(progress.evidenceLink) }
    var draftScheduledDate by rememberSaveable(lesson.dayNumber, progress.scheduledDate) { mutableStateOf(progress.scheduledDate) }
    var draftDifficulty by rememberSaveable(lesson.dayNumber, progress.difficultyRating) { mutableStateOf(progress.difficultyRating) }

    var draftResourceNotes by rememberSaveable(lesson.dayNumber, resourceProgress.personalNotes) {
        mutableStateOf(resourceProgress.personalNotes)
    }
    var draftResourceDiff by rememberSaveable(lesson.dayNumber, resourceProgress.difficultyEncountered) {
        mutableStateOf(resourceProgress.difficultyEncountered)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CyberCyanPrimary,
        unfocusedBorderColor = CyberNavyBorder,
        focusedLabelColor = CyberCyanPrimary,
        unfocusedLabelColor = TextSecondarySlate,
        focusedTextColor = TextPrimaryWhite,
        unfocusedTextColor = TextPrimaryWhite
    )

    CyberCard(
        borderColor = CyberCyanPrimary.copy(alpha = 0.6f),
        modifier = Modifier.testTag("lesson_detail_card")
    ) {
        // Header: Day, Week, Phase, Completion Checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CyberCyanPrimary.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, CyberCyanPrimary)
            ) {
                Text(
                    text = "DAY ${lesson.dayNumber} OF 182 • WEEK ${lesson.weekNumber} • PHASE ${lesson.phaseNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyanPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = progress.isCompleted,
                    onCheckedChange = { checked ->
                        onSaveLessonDetails(lesson.dayNumber, checked, draftNotes, draftDifficulty, null, draftEvidence, draftScheduledDate)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = StatusPresentGreen,
                        uncheckedColor = CyberCyanSecondary
                    ),
                    modifier = Modifier.testTag("lesson_complete_checkbox")
                )
                Text(
                    text = if (progress.isCompleted) "Completed" else "Mark Complete",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (progress.isCompleted) StatusPresentGreen else TextPrimaryWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = lesson.title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimaryWhite,
            modifier = Modifier.testTag("lesson_detail_title")
        )
        Text(
            text = "${lesson.phaseTitle} • ${lesson.weekTheme} • Est. ${lesson.estimatedMinutes} min",
            style = MaterialTheme.typography.bodySmall,
            color = CyberCyanSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 90-Minute Daily Time Breakdown
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = CyberNavySurface,
            border = BorderStroke(1.dp, CyberNavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "DAILY 90-MINUTE STUDY PLAN BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyanPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.timeAllocationPlan,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimaryWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Safe Lab Rule Banner
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = StatusPartialAmber.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, StatusPartialAmber.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Safe Lab Rules",
                    tint = StatusPartialAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = lesson.safeLabNotice,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimaryWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Learning Objectives & Theory
        LessonBlockSection(
            title = "Learning Objectives",
            body = lesson.objectives.joinToString("\n") { "• $it" }
        )
        LessonBlockSection(
            title = "Beginner-Friendly Theory",
            body = lesson.theorySummary
        )

        // Commands & Terminology
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CyberNavySurface)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Key Commands",
                    tint = CyberCyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Important Commands, Flags & Terminology",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyberCyanPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            lesson.commandsAndTerminology.forEach { cmd ->
                Text(
                    text = "$ $cmd",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyanSecondary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        LessonBlockSection(
            title = "Guided Practical Exercise (35 min)",
            body = lesson.guidedExercise
        )
        LessonBlockSection(
            title = "Independent Scenario-Based Challenge (15 min)",
            body = lesson.independentChallenge
        )
        LessonBlockSection(
            title = "Expected Results & Troubleshooting Tips",
            body = lesson.expectedResultsAndTroubleshooting
        )
        LessonBlockSection(
            title = "Short Practical Assessment & Completion Criteria",
            body = "${lesson.practicalAssessmentPrompt}\n• Criteria: ${lesson.completionCriteria}"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons: Launch 5-Question Quiz & Start 90m Timer for this Lesson
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenQuiz,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyanPrimary,
                    contentColor = CyberNavyBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("open_lesson_quiz_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = "5-Question Quiz",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("5-Q Quiz (Best: ${progress.quizBestScore}/5)")
            }

            OutlinedButton(
                onClick = onStartTimerForLesson,
                border = BorderStroke(1.dp, CyberCyanSecondary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Timer",
                    tint = CyberCyanSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Study Timer", color = TextPrimaryWhite)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Separate Daily Resource Tracking Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CyberNavySurface,
            border = BorderStroke(1.dp, CyberCyanSecondary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lesson_resource_tracking_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Resource",
                            tint = CyberCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Daily Resource Tracking (Separate from Lesson)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimaryWhite
                        )
                    }
                    Text(
                        text = lesson.primaryResource.verificationStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusPresentGreen
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Primary (${lesson.primaryResource.resourceType}): ${lesson.primaryResource.title}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimaryWhite
                )
                Text(
                    text = lesson.primaryResource.url,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyanPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Alternative Free Resource: ${lesson.primaryResource.alternativeTitle} (${lesson.primaryResource.alternativeUrl})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "YouTube Search Query (Not a direct link): \"${lesson.youtubeSearchQuery}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberCyanSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Separate Viewed & Completed Toggles for Resource
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = resourceProgress.isViewed,
                            onCheckedChange = { viewed ->
                                onSaveResourceTracking(
                                    lesson.primaryResource.resourceId,
                                    lesson.dayNumber,
                                    viewed,
                                    null,
                                    draftResourceNotes,
                                    draftResourceDiff
                                )
                            },
                            colors = CheckboxDefaults.colors(checkedColor = CyberCyanPrimary),
                            modifier = Modifier.testTag("resource_viewed_checkbox")
                        )
                        Text("Resource Viewed", style = MaterialTheme.typography.bodySmall, color = TextPrimaryWhite)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = resourceProgress.isCompleted,
                            onCheckedChange = { done ->
                                onSaveResourceTracking(
                                    lesson.primaryResource.resourceId,
                                    lesson.dayNumber,
                                    true,
                                    done,
                                    draftResourceNotes,
                                    draftResourceDiff
                                )
                            },
                            colors = CheckboxDefaults.colors(checkedColor = StatusPresentGreen),
                            modifier = Modifier.testTag("resource_completed_checkbox")
                        )
                        Text("Resource Completed", style = MaterialTheme.typography.bodySmall, color = TextPrimaryWhite)
                    }
                }

                OutlinedTextField(
                    value = draftResourceNotes,
                    onValueChange = { draftResourceNotes = it },
                    label = { Text("Resource Personal Notes & Difficulty Encountered") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resource_notes_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            onCopyText("Tutor Prompt", lesson.tutoringPrompt)
                        },
                        border = BorderStroke(1.dp, CyberNavyBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("copy_tutor_prompt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Tutoring Prompt",
                            tint = CyberCyanPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy AI Tutor Prompt", style = MaterialTheme.typography.labelSmall, color = TextPrimaryWhite)
                    }

                    Button(
                        onClick = {
                            onSaveResourceTracking(
                                lesson.primaryResource.resourceId,
                                lesson.dayNumber,
                                resourceProgress.isViewed,
                                resourceProgress.isCompleted,
                                draftResourceNotes,
                                draftResourceDiff
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyanSecondary,
                            contentColor = CyberNavyBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_resource_tracking_button")
                    ) {
                        Text("Save Resource Log", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Rescheduling Workflow (Separates Curriculum Day Number from Calendar Date)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draftScheduledDate,
                onValueChange = { draftScheduledDate = it },
                label = { Text("Reschedule Lesson Date (YYYY-MM-DD)") },
                placeholder = { Text("e.g., 2026-10-15") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = CyberCyanPrimary
                    )
                },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier
                    .weight(1f)
                    .testTag("lesson_reschedule_input")
            )
            Button(
                onClick = {
                    onSaveLessonDetails(
                        lesson.dayNumber,
                        null,
                        draftNotes,
                        draftDifficulty,
                        null,
                        draftEvidence,
                        draftScheduledDate
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusPartialAmber,
                    contentColor = CyberNavyBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("lesson_reschedule_save_button")
            ) {
                Text("Reschedule")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lesson Notes, Evidence Link & Difficulty Rating (1-5)
        OutlinedTextField(
            value = draftNotes,
            onValueChange = { draftNotes = it },
            label = { Text("Lesson Lab Notes & Command Outputs (Day ${lesson.dayNumber})") },
            colors = fieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lesson_notes_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = draftEvidence,
            onValueChange = { draftEvidence = it },
            label = { Text("Evidence Link (Screenshot path, GitHub commit, or report URL)") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = CyberCyanSecondary)
            },
            singleLine = true,
            colors = fieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lesson_evidence_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lesson Difficulty (1–5): $draftDifficulty/5",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimaryWhite
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (r in 1..5) {
                    Surface(
                        onClick = { draftDifficulty = r },
                        shape = RoundedCornerShape(8.dp),
                        color = if (draftDifficulty == r) CyberCyanPrimary else CyberNavySurface,
                        border = BorderStroke(1.dp, CyberCyanPrimary),
                        modifier = Modifier.testTag("lesson_difficulty_$r")
                    ) {
                        Text(
                            text = "$r",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (draftDifficulty == r) CyberNavyBackground else TextPrimaryWhite,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                onSaveLessonDetails(
                    lesson.dayNumber,
                    progress.isCompleted,
                    draftNotes,
                    draftDifficulty,
                    null,
                    draftEvidence,
                    draftScheduledDate
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyanPrimary,
                contentColor = CyberNavyBackground
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("lesson_save_progress_button")
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Day ${lesson.dayNumber} Notes, Evidence & Progress")
        }
    }
}

@Composable
private fun LessonBlockSection(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = CyberCyanPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimaryWhite
        )
    }
}

@Composable
private fun LessonFiveQuestionQuizDialog(
    lesson: CurriculumDayLesson,
    onDismiss: () -> Unit,
    onQuizSubmitted: (Int) -> Unit
) {
    val selectedAnswers = remember(lesson.dayNumber) {
        mutableStateMapOf<Int, Int>().apply {
            lesson.quizQuestions.forEach { q -> put(q.id, q.correctIndex) }
        }
    }
    var showExplanations by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberNavyCard,
        titleContentColor = TextPrimaryWhite,
        textContentColor = TextPrimaryWhite,
        title = {
            Text("Day ${lesson.dayNumber} Verification Quiz (5 Questions)")
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(380.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lesson.quizQuestions, key = { it.id }) { q ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberNavySurface)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "${q.id}. ${q.question}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = CyberCyanPrimary
                        )
                        q.options.forEachIndexed { idx, opt ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = selectedAnswers[q.id] == idx,
                                    onClick = { selectedAnswers[q.id] = idx },
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
                        if (showExplanations) {
                            Text(
                                text = "Explanation: ${q.explanation}",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusPresentGreen
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { showExplanations = !showExplanations }) {
                    Text(if (showExplanations) "Hide Explanations" else "Check Explanations", color = CyberCyanSecondary)
                }
                Button(
                    onClick = {
                        val score = lesson.quizQuestions.count { q ->
                            selectedAnswers[q.id] == q.correctIndex
                        }
                        onQuizSubmitted(score)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyanPrimary,
                        contentColor = CyberNavyBackground
                    ),
                    modifier = Modifier.testTag("submit_lesson_quiz_button")
                ) {
                    Text("Submit Score")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondarySlate)
            }
        }
    )
}

private fun copyTextToClipboard(context: Context, label: String, text: String) {
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
