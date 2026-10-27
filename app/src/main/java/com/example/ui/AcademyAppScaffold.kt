package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AttendanceStatus
import com.example.ui.components.CyberProgressBar
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DailyTasksScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LearningSummaryScreen
import com.example.ui.screens.ProgressSettingsScreen
import com.example.ui.screens.ProjectsLibraryScreen
import com.example.ui.screens.RoadmapScreen
import com.example.ui.screens.StudyTimerScreen
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.CyberNavyBackground
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.viewmodel.AcademySection
import com.example.viewmodel.AcademyUiState
import com.example.viewmodel.AcademyViewModel
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun AcademyApp(
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.isAuthScreenOpen || uiState.currentSection != AcademySection.DASHBOARD) {
        if (uiState.isAuthScreenOpen) {
            viewModel.setAuthScreenOpen(false)
        } else {
            viewModel.navigateTo(AcademySection.DASHBOARD)
        }
    }

    MyApplicationTheme(
        darkTheme = uiState.darkThemeEnabled,
        reducedMotion = uiState.reducedMotionEnabled
    ) {
        AcademyAppContent(
            uiState = uiState,
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyAppContent(
    uiState: AcademyUiState,
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 720.dp

        if (isExpandedScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberNavyBackground)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                Surface(
                    modifier = Modifier
                        .width(270.dp)
                        .fillMaxHeight()
                        .testTag("permanent_sidebar"),
                    color = CyberNavySurface,
                    border = BorderStroke(1.dp, CyberNavyBorder)
                ) {
                    SidebarDrawerContent(
                        uiState = uiState,
                        onSelectSection = { section ->
                            viewModel.setAuthScreenOpen(false)
                            viewModel.navigateTo(section)
                        },
                        onOpenAuth = { viewModel.setAuthScreenOpen(true) }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    AcademyTopBar(
                        uiState = uiState,
                        showMenuButton = false,
                        onOpenDrawer = {},
                        onOpenAuth = { viewModel.setAuthScreenOpen(!uiState.isAuthScreenOpen) }
                    )
                    StatusBanner(
                        message = uiState.statusBannerMessage,
                        onDismiss = { viewModel.clearBannerMessage() }
                    )
                    ActiveSectionBody(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = CyberNavySurface,
                        drawerContentColor = TextPrimaryWhite,
                        modifier = Modifier
                            .width(290.dp)
                            .testTag("modal_sidebar_drawer")
                    ) {
                        SidebarDrawerContent(
                            uiState = uiState,
                            onSelectSection = { section ->
                                viewModel.setAuthScreenOpen(false)
                                viewModel.navigateTo(section)
                                coroutineScope.launch { drawerState.close() }
                            },
                            onOpenAuth = {
                                viewModel.setAuthScreenOpen(true)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CyberNavyBackground,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        AcademyTopBar(
                            uiState = uiState,
                            showMenuButton = true,
                            onOpenDrawer = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onOpenAuth = { viewModel.setAuthScreenOpen(!uiState.isAuthScreenOpen) }
                        )
                    },
                    bottomBar = {
                        CompactBottomNavigationBar(
                            currentSection = uiState.currentSection,
                            onSelectSection = {
                                viewModel.setAuthScreenOpen(false)
                                viewModel.navigateTo(it)
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        StatusBanner(
                            message = uiState.statusBannerMessage,
                            onDismiss = { viewModel.clearBannerMessage() }
                        )
                        ActiveSectionBody(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AcademyTopBar(
    uiState: AcademyUiState,
    showMenuButton: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenAuth: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CyberNavySurface,
            titleContentColor = TextPrimaryWhite
        ),
        navigationIcon = {
            if (showMenuButton) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("open_sidebar_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Sidebar Navigation",
                        tint = CyberCyanPrimary
                    )
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CyberCyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Waqas Cyber Academy 2.0",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimaryWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${uiState.currentSection.title} • ${uiState.taskCompletionPercentage}% Wk1 • ${uiState.completedLessonsCount}/182 Days",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyanPrimary
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onOpenAuth,
                modifier = Modifier.testTag("topbar_auth_button")
            ) {
                Icon(
                    imageVector = if (uiState.activeUser != null) Icons.Default.VerifiedUser else Icons.Default.Lock,
                    contentDescription = "Account Authentication",
                    tint = if (uiState.activeUser != null) StatusPresentGreen else CyberCyanPrimary
                )
            }
        }
    )
}

@Composable
private fun SidebarDrawerContent(
    uiState: AcademyUiState,
    onSelectSection: (AcademySection) -> Unit,
    onOpenAuth: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberCyanPrimary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Academy Logo",
                    tint = CyberCyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "WAQAS CYBER 2.0",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberCyanPrimary
                )
                Text(
                    text = String.format(Locale.US, "26-Wk Platform • %.2fms nav", uiState.lastNavigationLatencyMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sidebar Mini Progress Widget
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CyberNavyCard,
            border = BorderStroke(1.dp, CyberNavyBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAuth() }
                .testTag("sidebar_account_card")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = uiState.activeUser?.displayName ?: uiState.profile.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimaryWhite
                    )
                    Text(
                        text = "${uiState.taskCompletionPercentage}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyanPrimary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = uiState.activeUser?.email ?: "${uiState.profile.education} (${uiState.profile.semester})",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uiState.activeUser != null) StatusPresentGreen else TextSecondarySlate
                )
                Spacer(modifier = Modifier.height(8.dp))
                CyberProgressBar(progress = uiState.overallProgressFraction, heightDp = 8)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = CyberNavyBorder)
        Spacer(modifier = Modifier.height(8.dp))

        // 8 Sidebar Navigation Items
        AcademySection.entries.forEach { section ->
            val selected = !uiState.isAuthScreenOpen && uiState.currentSection == section
            NavigationDrawerItem(
                label = {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                icon = {
                    Icon(
                        imageVector = sectionIcon(section),
                        contentDescription = section.title
                    )
                },
                selected = selected,
                onClick = { onSelectSection(section) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = CyberCyanPrimary.copy(alpha = 0.18f),
                    selectedIconColor = CyberCyanPrimary,
                    selectedTextColor = CyberCyanPrimary,
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = TextSecondarySlate,
                    unselectedTextColor = TextPrimaryWhite
                ),
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("sidebar_${section.tag}")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Lab: ${uiState.profile.environment}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondarySlate
        )
    }
}

@Composable
private fun CompactBottomNavigationBar(
    currentSection: AcademySection,
    onSelectSection: (AcademySection) -> Unit
) {
    Surface(
        color = CyberNavySurface,
        border = BorderStroke(1.dp, CyberNavyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AcademySection.entries.forEach { section ->
                val selected = currentSection == section
                val shortTitle = when (section) {
                    AcademySection.DASHBOARD -> "Home"
                    AcademySection.ATTENDANCE -> "Attend"
                    AcademySection.DAILY_TASKS -> "Tasks"
                    AcademySection.ROADMAP -> "Roadmap"
                    AcademySection.STUDY_TIMER -> "Timer"
                    AcademySection.LEARNING_SUMMARY -> "Summary"
                    AcademySection.PROJECTS_LIBRARY -> "Projects"
                    AcademySection.PROGRESS_SETTINGS -> "Progress"
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selected) CyberCyanPrimary.copy(alpha = 0.16f) else Color.Transparent
                        )
                        .clickable { onSelectSection(section) }
                        .padding(vertical = 4.dp, horizontal = 1.dp)
                        .testTag(section.tag)
                ) {
                    Icon(
                        imageVector = sectionIcon(section),
                        contentDescription = section.title,
                        tint = if (selected) CyberCyanPrimary else TextSecondarySlate,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = shortTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selected) CyberCyanPrimary else TextSecondarySlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(
    message: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(visible = !message.isNullOrBlank()) {
        if (!message.isNullOrBlank()) {
            Surface(
                color = CyberNavyElevated,
                border = BorderStroke(1.dp, StatusPresentGreen.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("status_feedback_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StatusPresentGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimaryWhite
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss notification",
                            tint = TextSecondarySlate,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveSectionBody(
    uiState: AcademyUiState,
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier
) {
    if (uiState.isAuthScreenOpen) {
        AuthScreen(
            activeUser = uiState.activeUser,
            registeredUsersCount = uiState.registeredUsersCount,
            authErrorMessage = uiState.authErrorMessage,
            onSignUp = { email, pass, name -> viewModel.signUp(email, pass, name) },
            onLogIn = { email, pass -> viewModel.logIn(email, pass) },
            onLogOut = { viewModel.logOut() },
            onClose = { viewModel.setAuthScreenOpen(false) },
            modifier = modifier
        )
        return
    }

    when (uiState.currentSection) {
        AcademySection.DASHBOARD -> {
            DashboardScreen(
                uiState = uiState,
                onNavigate = { viewModel.navigateTo(it) },
                onQuickMarkPresentToday = {
                    viewModel.saveAttendance(
                        dateIso = uiState.todayIso,
                        status = AttendanceStatus.PRESENT,
                        note = "Marked Present from Dashboard"
                    )
                },
                onToggleTask = { id, checked ->
                    viewModel.toggleTaskCompletion(id, checked)
                },
                onOpenAuth = { viewModel.setAuthScreenOpen(true) },
                onOpenLessonInRoadmap = { dayNumber ->
                    viewModel.selectLessonDay(dayNumber)
                },
                modifier = modifier
            )
        }
        AcademySection.ATTENDANCE -> {
            AttendanceScreen(
                uiState = uiState,
                onSelectDate = { viewModel.selectAttendanceDate(it) },
                onSelectStatus = { viewModel.updateSelectedAttendanceStatus(it) },
                onUpdateNote = { viewModel.updateSelectedAttendanceNote(it) },
                onSaveAttendance = { viewModel.saveAttendance() },
                onChangeMonth = { viewModel.changeCalendarMonth(it) },
                modifier = modifier
            )
        }
        AcademySection.DAILY_TASKS -> {
            DailyTasksScreen(
                uiState = uiState,
                onToggleTask = { id, checked -> viewModel.toggleTaskCompletion(id, checked) },
                onSaveTaskNotes = { id, notes -> viewModel.updateTaskNotes(id, notes) },
                modifier = modifier
            )
        }
        AcademySection.ROADMAP -> {
            RoadmapScreen(
                uiState = uiState,
                onSelectPhase = { viewModel.selectPhase(it) },
                onSelectWeek = { viewModel.selectWeek(it) },
                onSelectLessonDay = { viewModel.selectLessonDay(it) },
                onUpdateSearchQuery = { viewModel.updateRoadmapSearchQuery(it) },
                onSaveLessonDetails = { day, completed, notes, diff, quizScore, evidence, scheduled ->
                    viewModel.saveLessonDetails(
                        dayNumber = day,
                        isCompleted = completed,
                        notes = notes,
                        difficultyRating = diff,
                        quizScore = quizScore,
                        evidenceLink = evidence,
                        scheduledDate = scheduled
                    )
                },
                onSaveResourceTracking = { resId, day, viewed, completed, notes, diff ->
                    viewModel.saveResourceTracking(
                        resourceId = resId,
                        dayNumber = day,
                        isViewed = viewed,
                        isCompleted = completed,
                        personalNotes = notes,
                        difficultyEncountered = diff
                    )
                },
                onStartTimerForLesson = { day ->
                    viewModel.associateTimerWithLesson(day)
                    viewModel.navigateTo(AcademySection.STUDY_TIMER)
                },
                modifier = modifier
            )
        }
        AcademySection.STUDY_TIMER -> {
            StudyTimerScreen(
                uiState = uiState,
                onStartTimer = { viewModel.startTimer() },
                onPauseTimer = { viewModel.pauseTimer() },
                onResumeTimer = { viewModel.resumeTimer() },
                onResetTimer = { viewModel.resetTimer() },
                onAdvanceSeconds = { viewModel.advanceTimerSeconds(it) },
                onSaveSession = { viewModel.saveStudySession() },
                onUpdateTopic = { viewModel.updateSessionTopic(it) },
                onConfigureDuration = { mins, isBreak -> viewModel.configureTimerDuration(mins, isBreak) },
                modifier = modifier
            )
        }
        AcademySection.LEARNING_SUMMARY -> {
            LearningSummaryScreen(
                uiState = uiState,
                onUpdateDate = { viewModel.updateSummaryDate(it) },
                onUpdateTopics = { viewModel.updateSummaryTopics(it) },
                onUpdateUnderstood = { viewModel.updateSummaryUnderstood(it) },
                onUpdateDifficult = { viewModel.updateSummaryDifficult(it) },
                onUpdateQuestions = { viewModel.updateSummaryQuestions(it) },
                onUpdateRating = { viewModel.updateSummaryRating(it) },
                onSaveSummary = { viewModel.saveLearningSummary() },
                onEditSummary = { viewModel.startEditingSummary(it) },
                onCancelEdit = { viewModel.cancelEditingSummary() },
                onDeleteSummary = { viewModel.deleteLearningSummary(it) },
                onUpdateTopicReviewStatus = { topicId, phase, week, title, status ->
                    viewModel.updateTopicReviewStatus(topicId, phase, week, title, status)
                },
                modifier = modifier
            )
        }
        AcademySection.PROJECTS_LIBRARY -> {
            ProjectsLibraryScreen(
                uiState = uiState,
                onUpdateSearchQuery = { viewModel.updateLibrarySearchQuery(it) },
                onSaveProject = { viewModel.saveProject(it) },
                onDeleteProject = { viewModel.deleteProject(it) },
                onSaveNote = { title, content, cat, day, week ->
                    viewModel.saveLibraryNote(
                        title = title,
                        content = content,
                        category = cat,
                        associatedDay = day,
                        associatedWeek = week
                    )
                },
                onDeleteNote = { viewModel.deleteLibraryNote(it) },
                modifier = modifier
            )
        }
        AcademySection.PROGRESS_SETTINGS -> {
            ProgressSettingsScreen(
                uiState = uiState,
                onExportJson = { callback -> viewModel.exportDataToJson(callback) },
                onImportJson = { json, mode -> viewModel.importDataFromJson(json, mode) },
                onResetDemoData = { viewModel.resetDemoData() },
                onUndoLastResetOrReplace = { viewModel.undoLastReplaceOrReset() },
                onExportCsv = { callback -> viewModel.exportDataToCsv(callback) },
                onExportMarkdown = { callback -> viewModel.exportDataToMarkdownReport(callback) },
                onToggleDarkTheme = { viewModel.toggleDarkTheme(it) },
                onToggleReducedMotion = { viewModel.toggleReducedMotion(it) },
                onOpenAuthScreen = { viewModel.setAuthScreenOpen(true) },
                onLogOut = { viewModel.logOut() },
                modifier = modifier
            )
        }
    }
}

private fun sectionIcon(section: AcademySection): ImageVector {
    return when (section) {
        AcademySection.DASHBOARD -> Icons.Default.Dashboard
        AcademySection.ATTENDANCE -> Icons.Default.CalendarMonth
        AcademySection.DAILY_TASKS -> Icons.AutoMirrored.Filled.FactCheck
        AcademySection.ROADMAP -> Icons.Default.Map
        AcademySection.STUDY_TIMER -> Icons.Default.Timer
        AcademySection.LEARNING_SUMMARY -> Icons.Default.EditNote
        AcademySection.PROJECTS_LIBRARY -> Icons.Default.Code
        AcademySection.PROGRESS_SETTINGS -> Icons.Default.Analytics
    }
}
