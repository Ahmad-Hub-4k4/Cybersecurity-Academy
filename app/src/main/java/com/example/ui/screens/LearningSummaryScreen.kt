package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.data.CurriculumCatalog
import com.example.data.LearningSummaryEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.CyberCyanSecondary
import com.example.ui.theme.CyberNavyBackground
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPartialAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.viewmodel.AcademyUiState

private data class ReviewFlashcardTopic(
    val topicId: String,
    val phaseNumber: Int,
    val weekNumber: Int,
    val title: String,
    val question: String,
    val answer: String
)

private val CORE_REVIEW_TOPICS = listOf(
    ReviewFlashcardTopic(
        topicId = "topic_linux_permissions",
        phaseNumber = 1,
        weekNumber = 1,
        title = "Linux Octal File Permissions & SUID",
        question = "What does chmod 750 set, and why is an unexpected SUID binary (4755) dangerous?",
        answer = "750 gives Owner rwx (7), Group r-x (5), Others --- (0). SUID executes the binary with the file owner's privileges (often root)."
    ),
    ReviewFlashcardTopic(
        topicId = "topic_tcp_handshake_wireshark",
        phaseNumber = 2,
        weekNumber = 6,
        title = "TCP 3-Way Handshake & SYN Scans",
        question = "How does a half-open SYN scan (-sS) differ from a full TCP connect scan (-sT) in packet captures?",
        answer = "A SYN scan sends SYN, receives SYN/ACK on open ports, and immediately sends RST instead of completing the handshake."
    ),
    ReviewFlashcardTopic(
        topicId = "topic_windows_event_ids",
        phaseNumber = 3,
        weekNumber = 10,
        title = "Windows Security Event IDs (SOC Triage)",
        question = "Which Windows Security Event IDs represent Successful Logon, Failed Logon, and Process Creation?",
        answer = "4624 = Successful Logon, 4625 = Failed Logon (brute-force indicator), 4688 = New Process Created (command-line auditing)."
    ),
    ReviewFlashcardTopic(
        topicId = "topic_owasp_sqli_defense",
        phaseNumber = 6,
        weekNumber = 21,
        title = "OWASP A03 Injection & Parameterized Queries",
        question = "Why do Prepared Statements / Parameterized Queries stop SQL Injection?",
        answer = "The database compiles the SQL structure first and treats bound parameters strictly as literal data, never executable SQL syntax."
    )
)

@Composable
fun LearningSummaryScreen(
    uiState: AcademyUiState,
    onUpdateDate: (String) -> Unit,
    onUpdateTopics: (String) -> Unit,
    onUpdateUnderstood: (String) -> Unit,
    onUpdateDifficult: (String) -> Unit,
    onUpdateQuestions: (String) -> Unit,
    onUpdateRating: (Int) -> Unit,
    onSaveSummary: () -> Unit,
    onEditSummary: (LearningSummaryEntity) -> Unit,
    onCancelEdit: () -> Unit,
    onDeleteSummary: (Long) -> Unit,
    onUpdateTopicReviewStatus: (
        topicId: String,
        phaseNumber: Int,
        weekNumber: Int,
        topicTitle: String,
        status: String
    ) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val form = uiState.summaryForm
    val isEditing = form.editingId != 0L
    var currentFlashcardIndex by rememberSaveable { mutableIntStateOf(0) }
    var isFlashcardFlipped by rememberSaveable { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CyberCyanPrimary,
        unfocusedBorderColor = CyberNavyBorder,
        focusedLabelColor = CyberCyanPrimary,
        unfocusedLabelColor = TextSecondarySlate,
        focusedTextColor = TextPrimaryWhite,
        unfocusedTextColor = TextPrimaryWhite
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("learning_summary_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "Daily Summary & Spaced Review",
                subtitle = "Log daily reflections, track topic mastery, and drill security flashcards",
                badgeText = "${uiState.learningSummaries.size} Saved"
            )
        }

        // 1. Learning Summary Form Card
        item {
            CyberCard(
                borderColor = if (isEditing) StatusPartialAmber else CyberCyanPrimary.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Learning Summary (#${form.editingId})" else "New Learning Reflection",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isEditing) StatusPartialAmber else TextPrimaryWhite,
                        modifier = Modifier.testTag("summary_form_header")
                    )
                    if (isEditing) {
                        OutlinedButton(
                            onClick = onCancelEdit,
                            border = BorderStroke(1.dp, StatusPartialAmber),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("cancel_edit_summary_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Edit",
                                tint = StatusPartialAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel", style = MaterialTheme.typography.labelMedium, color = StatusPartialAmber)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = form.date,
                    onValueChange = onUpdateDate,
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("summary_date_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = form.topicsLearned,
                    onValueChange = onUpdateTopics,
                    label = { Text("Topics learned") },
                    placeholder = { Text("e.g., pwd, ls -la, mkdir, chmod octal permissions") },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("summary_topics_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = form.whatUnderstood,
                    onValueChange = onUpdateUnderstood,
                    label = { Text("What I understood") },
                    placeholder = { Text("e.g., How rwx maps to 4, 2, 1 and safe file operations with -i") },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("summary_understood_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = form.whatWasDifficult,
                    onValueChange = onUpdateDifficult,
                    label = { Text("What was difficult") },
                    placeholder = { Text("e.g., Remembering symbolic vs octal chmod flags") },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("summary_difficult_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = form.questionsForNextSession,
                    onValueChange = onUpdateQuestions,
                    label = { Text("Questions for the next session") },
                    placeholder = { Text("e.g., How do SUID and SGID bits work in Linux privilege escalation?") },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("summary_questions_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Self-rating 1 to 5
                Text(
                    text = "Self-Rating (1 to 5): ${form.selfRating} / 5",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimaryWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (rating in 1..5) {
                        val selected = rating <= form.selfRating
                        val exactSelected = rating == form.selfRating
                        Surface(
                            onClick = { onUpdateRating(rating) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (exactSelected) {
                                CyberCyanPrimary.copy(alpha = 0.22f)
                            } else {
                                CyberNavySurface
                            },
                            border = BorderStroke(
                                width = if (exactSelected) 2.dp else 1.dp,
                                color = if (selected) CyberCyanPrimary else CyberNavyBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("summary_rating_$rating")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (selected) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Rating $rating",
                                    tint = if (selected) StatusPartialAmber else TextSecondarySlate,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$rating",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) TextPrimaryWhite else TextSecondarySlate
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSaveSummary,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEditing) StatusPresentGreen else CyberCyanPrimary,
                        contentColor = CyberNavyBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_summary_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Learning Summary",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Update Learning Summary" else "Save Learning Summary",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // 2. Spaced Repetition & Flashcard Review System (Version 2.0 Feature)
        item {
            val activeCard = CORE_REVIEW_TOPICS[currentFlashcardIndex % CORE_REVIEW_TOPICS.size]
            val currentStatus = uiState.topicReviewsMap[activeCard.topicId]?.reviewStatus ?: "Needs Review"

            CyberCard(borderColor = CyberCyanSecondary.copy(alpha = 0.5f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Spaced Repetition & Flashcard Drill",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = "Phase ${activeCard.phaseNumber} • Week ${activeCard.weekNumber}: ${activeCard.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyanPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CyberCyanSecondary.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, CyberCyanSecondary)
                    ) {
                        Text(
                            text = currentStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyanSecondary,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("flashcard_current_status")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyberNavySurface,
                    border = BorderStroke(1.dp, CyberNavyBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFlashcardFlipped = !isFlashcardFlipped }
                        .testTag("flashcard_flip_box")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isFlashcardFlipped) "ANSWER / DEFENSE PRINCIPLE" else "QUESTION (${currentFlashcardIndex + 1}/${CORE_REVIEW_TOPICS.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isFlashcardFlipped) StatusPresentGreen else CyberCyanPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Flip,
                                    contentDescription = "Flip Flashcard",
                                    tint = TextSecondarySlate,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tap to flip",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondarySlate
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isFlashcardFlipped) activeCard.answer else activeCard.question,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimaryWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Needs Review", "Reviewing", "Mastered").forEach { statusOption ->
                        val selected = currentStatus == statusOption
                        OutlinedButton(
                            onClick = {
                                onUpdateTopicReviewStatus(
                                    activeCard.topicId,
                                    activeCard.phaseNumber,
                                    activeCard.weekNumber,
                                    activeCard.title,
                                    statusOption
                                )
                            },
                            border = BorderStroke(
                                1.dp,
                                if (selected) StatusPresentGreen else CyberNavyBorder
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("review_status_${statusOption.lowercase().replace(" ", "_")}")
                        ) {
                            Text(
                                text = statusOption,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) StatusPresentGreen else TextPrimaryWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            isFlashcardFlipped = false
                            currentFlashcardIndex = (currentFlashcardIndex + 1) % CORE_REVIEW_TOPICS.size
                        },
                        border = BorderStroke(1.dp, CyberCyanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("next_flashcard_button")
                    ) {
                        Text("Next Topic Card", style = MaterialTheme.typography.labelSmall, color = CyberCyanPrimary)
                    }
                }
            }
        }

        // 3. Previous Summaries Header
        item {
            Text(
                text = "Previous Learning Summaries (${uiState.learningSummaries.size})",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimaryWhite,
                modifier = Modifier.testTag("previous_summaries_header")
            )
        }

        if (uiState.learningSummaries.isEmpty()) {
            item {
                CyberCard {
                    Text(
                        text = "No learning summaries saved yet. Complete the form above to record your first daily cybersecurity study log.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondarySlate,
                        modifier = Modifier.testTag("empty_summaries_message")
                    )
                }
            }
        } else {
            items(uiState.learningSummaries, key = { it.id }) { summary ->
                SavedSummaryCard(
                    summary = summary,
                    onEdit = { onEditSummary(summary) },
                    onDelete = { onDeleteSummary(summary.id) }
                )
            }
        }
    }
}

@Composable
private fun SavedSummaryCard(
    summary: LearningSummaryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    CyberCard(
        modifier = Modifier.testTag("summary_card_${summary.id}"),
        borderColor = CyberNavyBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.date,
                    style = MaterialTheme.typography.labelLarge,
                    color = CyberCyanPrimary
                )
                Text(
                    text = summary.topicsLearned,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryWhite,
                    modifier = Modifier.testTag("summary_saved_topics_${summary.id}")
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = StatusPartialAmber.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, StatusPartialAmber.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "Rating: ${summary.selfRating}/5",
                        style = MaterialTheme.typography.labelMedium,
                        color = StatusPartialAmber,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("summary_saved_rating_${summary.id}")
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_summary_${summary.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Summary",
                        tint = CyberCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_summary_${summary.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Summary",
                        tint = StatusAbsentRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        SummaryFieldDetail(label = "What I understood", value = summary.whatUnderstood)
        SummaryFieldDetail(label = "What was difficult", value = summary.whatWasDifficult)
        SummaryFieldDetail(label = "Questions for next session", value = summary.questionsForNextSession)
    }
}

@Composable
private fun SummaryFieldDetail(label: String, value: String) {
    if (value.isBlank()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CyberNavyElevated.copy(alpha = 0.45f))
            .padding(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CyberCyanSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimaryWhite
        )
    }
}
