package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AttendanceEntity
import com.example.data.AttendanceStatus
import com.example.ui.components.AttendanceBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.attendanceStatusColor
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.CyberCyanSecondary
import com.example.ui.theme.CyberNavyBackground
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPartialAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StatusRestIndigo
import com.example.ui.theme.TextMutedSlate
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.viewmodel.AcademyUiState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AttendanceScreen(
    uiState: AcademyUiState,
    onSelectDate: (String) -> Unit,
    onSelectStatus: (AttendanceStatus) -> Unit,
    onUpdateNote: (String) -> Unit,
    onSaveAttendance: () -> Unit,
    onChangeMonth: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val existingForSelectedDate = uiState.attendanceRecords.firstOrNull {
        it.date == uiState.selectedAttendanceDate
    }
    val isFutureDate = uiState.selectedAttendanceDate > uiState.todayIso

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "Attendance Tracker",
                subtitle = "Mark Present, Partial, Absent, or Rest Day • Future dates are never marked absent automatically",
                badgeText = "Streak: ${uiState.learningStreakDays}d"
            )
        }

        // 1. Mark / Edit Attendance Card for Selected Date
        item {
            CyberCard(
                borderColor = CyberCyanPrimary.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (existingForSelectedDate != null) {
                                "Edit Attendance (${uiState.selectedAttendanceDate})"
                            } else {
                                "Mark Attendance (${uiState.selectedAttendanceDate})"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimaryWhite,
                            modifier = Modifier.testTag("attendance_selected_date_header")
                        )
                        Text(
                            text = if (uiState.selectedAttendanceDate == uiState.todayIso) {
                                "Today's Date • Tap a status below and Save"
                            } else if (isFutureDate) {
                                "Upcoming Date • Future dates are never marked absent automatically"
                            } else {
                                "Selected from Calendar • Editable record"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    if (uiState.selectedAttendanceDate != uiState.todayIso) {
                        OutlinedButton(
                            onClick = { onSelectDate(uiState.todayIso) },
                            border = BorderStroke(1.dp, CyberCyanPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("attendance_jump_today_button")
                        ) {
                            Text("Today", style = MaterialTheme.typography.labelMedium, color = CyberCyanPrimary)
                        }
                    } else {
                        AttendanceBadge(statusLabel = existingForSelectedDate?.status)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Status Buttons in 2x2 Grid
                val statuses = listOf(
                    Triple(AttendanceStatus.PRESENT, "attendance_status_present", StatusPresentGreen),
                    Triple(AttendanceStatus.PARTIAL, "attendance_status_partial", StatusPartialAmber),
                    Triple(AttendanceStatus.ABSENT, "attendance_status_absent", StatusAbsentRed),
                    Triple(AttendanceStatus.REST_DAY, "attendance_status_rest", StatusRestIndigo)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    statuses.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { (status, tag, accent) ->
                                val isSelected = uiState.selectedAttendanceStatus == status
                                val isDisabled = isFutureDate && status == AttendanceStatus.ABSENT
                                Surface(
                                    onClick = {
                                        if (!isDisabled) onSelectStatus(status)
                                    },
                                    enabled = !isDisabled,
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) {
                                        accent.copy(alpha = 0.22f)
                                    } else {
                                        CyberNavySurface
                                    },
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) accent else CyberNavyBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag(tag)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = accent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isDisabled) TextMutedSlate else accent)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(
                                            text = status.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = when {
                                                isDisabled -> TextMutedSlate
                                                isSelected -> accent
                                                else -> TextPrimaryWhite
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.selectedAttendanceNote,
                    onValueChange = onUpdateNote,
                    label = { Text("Optional Note (e.g., Kali VM lab session, exam day)") },
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
                        .testTag("attendance_note_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onSaveAttendance,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyanPrimary,
                        contentColor = CyberNavyBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_attendance_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Attendance",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingForSelectedDate != null) {
                            "Update Attendance (${uiState.selectedAttendanceStatus.label})"
                        } else {
                            "Save Attendance (${uiState.selectedAttendanceStatus.label})"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // 2. Monthly Attendance Calendar Card
        item {
            MonthlyAttendanceCalendarCard(
                yearMonth = uiState.selectedCalendarYearMonth,
                todayIso = uiState.todayIso,
                selectedDateIso = uiState.selectedAttendanceDate,
                attendanceRecords = uiState.attendanceRecords,
                onSelectDate = onSelectDate,
                onChangeMonth = onChangeMonth
            )
        }

        // 3. Saved Attendance Records History (Editable)
        item {
            CyberCard {
                Text(
                    text = "Saved Attendance Records (${uiState.attendanceRecords.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap Edit on any record to load and modify its attendance status.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.attendanceRecords.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberNavySurface)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No Attendance Yet",
                            tint = CyberCyanSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "No attendance records saved yet. Mark today's attendance above to begin tracking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.attendanceRecords.forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyberNavyElevated.copy(alpha = 0.55f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("attendance_record_${record.date}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = record.date,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = TextPrimaryWhite
                                        )
                                        if (record.date == uiState.todayIso) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(Today)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CyberCyanPrimary
                                            )
                                        }
                                    }
                                    if (record.note.isNotBlank()) {
                                        Text(
                                            text = record.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondarySlate
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AttendanceBadge(statusLabel = record.status)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { onSelectDate(record.date) },
                                        modifier = Modifier.testTag("edit_attendance_${record.date}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit attendance for ${record.date}",
                                            tint = CyberCyanPrimary,
                                            modifier = Modifier.size(18.dp)
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
private fun MonthlyAttendanceCalendarCard(
    yearMonth: YearMonth,
    todayIso: String,
    selectedDateIso: String,
    attendanceRecords: List<AttendanceEntity>,
    onSelectDate: (String) -> Unit,
    onChangeMonth: (Long) -> Unit
) {
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
    val recordsByDate = attendanceRecords.associateBy { it.date }
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    // Sunday = 0 .. Saturday = 6
    val leadingEmptyCells = firstDayOfMonth.dayOfWeek.value % 7

    CyberCard(
        modifier = Modifier.testTag("monthly_attendance_calendar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onChangeMonth(-1) },
                modifier = Modifier.testTag("calendar_prev_month")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = CyberCyanPrimary
                )
            }
            Text(
                text = yearMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimaryWhite
            )
            IconButton(
                onClick = { onChangeMonth(1) },
                modifier = Modifier.testTag("calendar_next_month")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = CyberCyanPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day of week headers
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { dayLabel ->
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondarySlate,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = ((leadingEmptyCells + daysInMonth + 6) / 7) * 7
        val rows = totalCells / 7

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - leadingEmptyCells + 1
                        if (dayNumber in 1..daysInMonth) {
                            val cellDate: LocalDate = yearMonth.atDay(dayNumber)
                            val dateIso = cellDate.toString()
                            val record = recordsByDate[dateIso]
                            val isSelected = dateIso == selectedDateIso
                            val isToday = dateIso == todayIso
                            val isFuture = dateIso > todayIso
                            val statusColor = record?.let { attendanceStatusColor(it.status) }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            statusColor != null -> statusColor.copy(alpha = 0.22f)
                                            isToday -> CyberCyanPrimary.copy(alpha = 0.14f)
                                            else -> CyberNavySurface
                                        }
                                    )
                                    .border(
                                        width = if (isSelected || isToday) 1.5.dp else 1.dp,
                                        color = when {
                                            isSelected -> CyberCyanPrimary
                                            statusColor != null -> statusColor
                                            isToday -> CyberCyanSecondary
                                            else -> CyberNavyBorder.copy(alpha = 0.5f)
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectDate(dateIso) }
                                    .testTag("calendar_day_$dateIso"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = when {
                                            statusColor != null -> statusColor
                                            isFuture -> TextMutedSlate
                                            else -> TextPrimaryWhite
                                        }
                                    )
                                    if (statusColor != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(statusColor)
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(label = "Present", color = StatusPresentGreen)
            LegendDot(label = "Partial", color = StatusPartialAmber)
            LegendDot(label = "Absent", color = StatusAbsentRed)
            LegendDot(label = "Rest Day", color = StatusRestIndigo)
            LegendDot(label = "Future (Unmarked)", color = TextMutedSlate)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondarySlate
        )
    }
}
