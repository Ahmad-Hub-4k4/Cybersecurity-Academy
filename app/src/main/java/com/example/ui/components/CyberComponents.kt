package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AttendanceStatus
import com.example.data.StudentProfile
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.CyberCyanSecondary
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPartialAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StatusRestIndigo
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CyberNavyBorder,
    containerColor: Color = CyberNavyCard,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun CyberSectionHeader(
    title: String,
    subtitle: String? = null,
    badgeText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimaryWhite
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
            }
        }
        if (!badgeText.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(50),
                color = CyberNavyElevated,
                border = BorderStroke(1.dp, CyberCyanPrimary.copy(alpha = 0.45f))
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyanPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun CyberProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    heightDp: Int = 12
) {
    val reducedMotion = com.example.ui.theme.LocalReducedMotion.current
    val target = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 250),
        label = "cyber_progress"
    )
    val effectiveProgress = if (reducedMotion) target else animatedProgress
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(50))
            .background(CyberNavySurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(effectiveProgress)
                .height(heightDp.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(CyberCyanPrimary, CyberCyanSecondary, StatusPresentGreen)
                    )
                )
        )
    }
}

@Composable
fun CyberStatCard(
    label: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color = CyberCyanPrimary,
    valueTestTag: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondarySlate
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimaryWhite,
                modifier = if (valueTestTag != null) Modifier.testTag(valueTestTag) else Modifier
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = accentColor
            )
        }
    }
}

fun attendanceStatusColor(statusLabel: String?): Color {
    if (statusLabel.isNullOrBlank()) return TextSecondarySlate
    return when (AttendanceStatus.fromLabel(statusLabel)) {
        AttendanceStatus.PRESENT -> StatusPresentGreen
        AttendanceStatus.PARTIAL -> StatusPartialAmber
        AttendanceStatus.ABSENT -> StatusAbsentRed
        AttendanceStatus.REST_DAY -> StatusRestIndigo
    }
}

@Composable
fun AttendanceBadge(
    statusLabel: String?,
    modifier: Modifier = Modifier
) {
    val label = statusLabel?.ifBlank { "Not Marked" } ?: "Not Marked"
    val color = if (statusLabel.isNullOrBlank()) TextSecondarySlate else attendanceStatusColor(statusLabel)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}

@Composable
fun StudentProfileCard(
    profile: StudentProfile,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier.testTag("student_profile_card"),
        borderColor = CyberCyanPrimary.copy(alpha = 0.35f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCyanPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Student Profile",
                    tint = CyberCyanPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryWhite
                )
                Text(
                    text = "${profile.education} • ${profile.semester}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberCyanPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ProfileDetailRow(
            icon = Icons.Default.School,
            label = "Goal",
            value = profile.goal
        )
        Spacer(modifier = Modifier.height(6.dp))
        ProfileDetailRow(
            icon = Icons.Default.Timer,
            label = "Study Plan",
            value = "${profile.dailyStudyMinutesGoal} minutes per day • ${profile.budgetPreference}"
        )
        Spacer(modifier = Modifier.height(6.dp))
        ProfileDetailRow(
            icon = Icons.Default.Computer,
            label = "Lab Environment",
            value = profile.environment
        )
        Spacer(modifier = Modifier.height(6.dp))
        ProfileDetailRow(
            icon = Icons.Default.Person,
            label = "Current Track",
            value = "${profile.currentWeek} • ${profile.currentPhase}"
        )
    }
}

@Composable
fun CyberGlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color = CyberCyanPrimary,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CyberNavyCard.copy(alpha = 0.86f),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.55f),
                    CyberNavyBorder.copy(alpha = 0.55f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun CyberWeeklyStudyBarChart(
    dailyMinutesMap: List<Pair<String, Int>>,
    dailyTargetMinutes: Int = 90,
    modifier: Modifier = Modifier
) {
    val maxVal = maxOf(
        dailyTargetMinutes,
        dailyMinutesMap.maxOfOrNull { it.second } ?: 0,
        30
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_study_bar_chart")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CyberNavySurface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            dailyMinutesMap.forEach { (dayLabel, minutes) ->
                val fraction = (minutes.toFloat() / maxVal.toFloat()).coerceIn(0.06f, 1f)
                val barColor = when {
                    minutes >= dailyTargetMinutes -> StatusPresentGreen
                    minutes > 0 -> CyberCyanPrimary
                    else -> CyberNavyBorder.copy(alpha = 0.45f)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                ) {
                    Text(
                        text = "${minutes}m",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = if (minutes > 0) TextPrimaryWhite else TextSecondarySlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height((64 * fraction).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(barColor)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondarySlate
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = CyberCyanSecondary,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextSecondarySlate
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimaryWhite,
            modifier = Modifier.weight(1f)
        )
    }
}
