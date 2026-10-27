package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AcademyRepository
import com.example.data.AttendanceEntity
import com.example.data.AttendanceStatus
import com.example.data.AuthenticatedUser
import com.example.data.BackupImportMode
import com.example.data.BackupSnapshotEntity
import com.example.data.CurriculumCatalog
import com.example.data.CurriculumDayLesson
import com.example.data.DailyTaskEntity
import com.example.data.DefaultAcademySeed
import com.example.data.LessonProgressEntity
import com.example.data.LibraryNoteEntity
import com.example.data.LearningSummaryEntity
import com.example.data.ProjectEntity
import com.example.data.ResourceProgressEntity
import com.example.data.StudentProfile
import com.example.data.StudySessionEntity
import com.example.data.TopicReviewEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AcademySection(val title: String, val tag: String) {
    DASHBOARD("Dashboard", "nav_dashboard"),
    ATTENDANCE("Attendance", "nav_attendance"),
    DAILY_TASKS("Daily Tasks", "nav_tasks"),
    ROADMAP("26-Week Roadmap", "nav_roadmap"),
    STUDY_TIMER("Study Timer", "nav_timer"),
    LEARNING_SUMMARY("Summary & Review", "nav_summary"),
    PROJECTS_LIBRARY("Projects & Notes", "nav_projects"),
    PROGRESS_SETTINGS("Progress & Settings", "nav_progress")
}

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

data class SummaryFormState(
    val editingId: Long = 0L,
    val date: String = LocalDate.now().toString(),
    val topicsLearned: String = "",
    val whatUnderstood: String = "",
    val whatWasDifficult: String = "",
    val questionsForNextSession: String = "",
    val selfRating: Int = 4
)

data class TimerUiState(
    val status: TimerStatus = TimerStatus.IDLE,
    val targetMinutes: Int = 90,
    val remainingSeconds: Int = 90 * 60,
    val elapsedSeconds: Int = 0,
    val isBreakMode: Boolean = false,
    val associatedDayNumber: Int = 1,
    val sessionTopic: String = "Week 1: Linux Commands & Kali Lab"
) {
    val formattedRemaining: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format(Locale.US, "%02d:%02d", mins, secs)
        }

    val formattedElapsed: String
        get() {
            val mins = elapsedSeconds / 60
            val secs = elapsedSeconds % 60
            return String.format(Locale.US, "%02d:%02d", mins, secs)
        }

    val elapsedMinutes: Int
        get() = elapsedSeconds / 60

    val savableMinutes: Int
        get() = when {
            elapsedSeconds >= 60 -> elapsedSeconds / 60
            elapsedSeconds > 0 -> 1
            else -> 0
        }

    val progressFraction: Float
        get() {
            val totalSecs = (targetMinutes * 60).coerceAtLeast(1)
            return (elapsedSeconds.toFloat() / totalSecs.toFloat()).coerceIn(0f, 1f)
        }
}

private data class ExtendedDataBundle(
    val activeUser: AuthenticatedUser? = null,
    val registeredUsersCount: Int = 0,
    val lessonProgressMap: Map<Int, LessonProgressEntity> = emptyMap(),
    val resourceProgressMap: Map<String, ResourceProgressEntity> = emptyMap(),
    val projects: List<ProjectEntity> = emptyList(),
    val libraryNotes: List<LibraryNoteEntity> = emptyList(),
    val topicReviewsMap: Map<String, TopicReviewEntity> = emptyMap(),
    val latestUndoSnapshot: BackupSnapshotEntity? = null
)

private data class LocalControlState(
    val currentSection: AcademySection = AcademySection.DASHBOARD,
    val lastNavigationLatencyMs: Double = 0.2,
    val todayIso: String = LocalDate.now().toString(),
    val selectedCalendarYearMonth: YearMonth = YearMonth.now(),
    val selectedAttendanceDate: String = LocalDate.now().toString(),
    val selectedAttendanceStatus: AttendanceStatus = AttendanceStatus.PRESENT,
    val selectedAttendanceNote: String = "",
    val selectedPhaseNumber: Int = 1,
    val selectedWeekNumber: Int = 1,
    val selectedLessonDay: Int = 1,
    val roadmapSearchQuery: String = "",
    val librarySearchQuery: String = "",
    val darkThemeEnabled: Boolean = true,
    val reducedMotionEnabled: Boolean = false,
    val isAuthScreenOpen: Boolean = false,
    val authErrorMessage: String? = null,
    val summaryForm: SummaryFormState = SummaryFormState(),
    val statusBannerMessage: String? = null,
    val lastExportedJson: String? = null,
    val lastExportedCsv: String? = null,
    val lastExportedMarkdown: String? = null
)

data class AcademyUiState(
    val profile: StudentProfile = DefaultAcademySeed.profile,
    val currentSection: AcademySection = AcademySection.DASHBOARD,
    val lastNavigationLatencyMs: Double = 0.2,
    val todayIso: String = LocalDate.now().toString(),
    val formattedTodayDate: String = "",
    val tasks: List<DailyTaskEntity> = emptyList(),
    val attendanceRecords: List<AttendanceEntity> = emptyList(),
    val studySessions: List<StudySessionEntity> = emptyList(),
    val learningSummaries: List<LearningSummaryEntity> = emptyList(),
    val todayAttendance: AttendanceEntity? = null,
    val totalStudyMinutes: Int = 0,
    val todayStudyMinutes: Int = 0,
    val completedTasksCount: Int = 0,
    val totalTasksCount: Int = 5,
    val taskCompletionPercentage: Int = 0,
    val overallProgressFraction: Float = 0f,
    val learningStreakDays: Int = 0,
    val selectedCalendarYearMonth: YearMonth = YearMonth.now(),
    val selectedAttendanceDate: String = LocalDate.now().toString(),
    val selectedAttendanceStatus: AttendanceStatus = AttendanceStatus.PRESENT,
    val selectedAttendanceNote: String = "",
    val timer: TimerUiState = TimerUiState(),
    val summaryForm: SummaryFormState = SummaryFormState(),
    val statusBannerMessage: String? = null,
    val lastExportedJson: String? = null,
    val lastExportedCsv: String? = null,
    val lastExportedMarkdown: String? = null,
    // Version 2.0 State
    val activeUser: AuthenticatedUser? = null,
    val registeredUsersCount: Int = 0,
    val isAuthScreenOpen: Boolean = false,
    val authErrorMessage: String? = null,
    val selectedPhaseNumber: Int = 1,
    val selectedWeekNumber: Int = 1,
    val selectedLessonDay: Int = 1,
    val roadmapSearchQuery: String = "",
    val librarySearchQuery: String = "",
    val lessonProgressMap: Map<Int, LessonProgressEntity> = emptyMap(),
    val completedLessonsCount: Int = 0,
    val totalLessonsCount: Int = 182,
    val roadmapCompletionPercentage: Int = 0,
    val resourceProgressMap: Map<String, ResourceProgressEntity> = emptyMap(),
    val projects: List<ProjectEntity> = emptyList(),
    val libraryNotes: List<LibraryNoteEntity> = emptyList(),
    val topicReviewsMap: Map<String, TopicReviewEntity> = emptyMap(),
    val latestUndoSnapshot: BackupSnapshotEntity? = null,
    val darkThemeEnabled: Boolean = true,
    val reducedMotionEnabled: Boolean = false
)

class AcademyViewModel(
    private val repository: AcademyRepository,
    initialTodayDate: LocalDate = LocalDate.now()
) : ViewModel() {

    companion object {
        private val DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.US)
    }

    private val initialTodayIso = initialTodayDate.toString()
    private val formattedInitialToday = runCatching {
        initialTodayDate.format(DISPLAY_DATE_FORMATTER)
    }.getOrDefault(initialTodayIso)

    // Dedicated Timer StateFlow to avoid global recomposition overhead on every 1s tick
    private val _timerFlow = MutableStateFlow(TimerUiState())
    val timerFlow: StateFlow<TimerUiState> = _timerFlow.asStateFlow()

    private val controlState = MutableStateFlow(
        LocalControlState(
            todayIso = initialTodayIso,
            selectedCalendarYearMonth = YearMonth.from(initialTodayDate),
            selectedAttendanceDate = initialTodayIso,
            summaryForm = SummaryFormState(date = initialTodayIso)
        )
    )

    private var timerJob: Job? = null
    private var lastTickTimestampMs: Long = 0L
    private var lastSavedSessionSignature: String? = null

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private val extendedBundleFlow = combine(
        combine(
            repository.activeUserFlow,
            repository.allUsersFlow,
            repository.lessonProgressFlow,
            repository.resourceProgressFlow
        ) { activeAcc, allUsers, lessons, resources ->
            val authUser = activeAcc?.let {
                AuthenticatedUser(
                    id = it.id,
                    email = it.email,
                    displayName = it.displayName,
                    lastLoginAt = it.lastLoginAt
                )
            }
            Quad(
                authUser,
                allUsers.size,
                lessons.associateBy { it.dayNumber },
                resources.associateBy { it.resourceId }
            )
        },
        combine(
            repository.projectsFlow,
            repository.libraryNotesFlow,
            repository.topicReviewsFlow,
            repository.latestBackupSnapshotFlow
        ) { projects, notes, reviews, latestSnap ->
            Quad(
                projects,
                notes,
                reviews.associateBy { it.topicId },
                latestSnap
            )
        }
    ) { first, second ->
        ExtendedDataBundle(
            activeUser = first.a,
            registeredUsersCount = first.b,
            lessonProgressMap = first.c,
            resourceProgressMap = first.d,
            projects = second.a,
            libraryNotes = second.b,
            topicReviewsMap = second.c,
            latestUndoSnapshot = second.d
        )
    }

    private val coreDbFlow = combine(
        repository.tasksFlow,
        repository.attendanceFlow,
        repository.studySessionsFlow,
        repository.learningSummariesFlow
    ) { tasks, attendanceList, sessions, summaries ->
        Quad(tasks, attendanceList, sessions, summaries)
    }

    val uiState: StateFlow<AcademyUiState> = combine(
        coreDbFlow,
        extendedBundleFlow,
        controlState,
        _timerFlow
    ) { core, ext, ctrl, timerState ->
        val tasks = core.a
        val attendanceList = core.b
        val sessions = core.c
        val summaries = core.d

        val completedCount = tasks.count { it.isCompleted }
        val totalTasks = if (tasks.isNotEmpty()) tasks.size else 5
        val completionPct = if (totalTasks > 0) ((completedCount * 100f) / totalTasks).toInt() else 0
        val progressFrac = if (totalTasks > 0) completedCount.toFloat() / totalTasks.toFloat() else 0f

        val totalMinutes = sessions.sumOf { it.durationMinutes }
        val todayMinutes = sessions.filter { it.date == ctrl.todayIso }.sumOf { it.durationMinutes }
        val todayAtt = attendanceList.firstOrNull { it.date == ctrl.todayIso }
        val streak = calculateStreak(attendanceList, ctrl.todayIso)

        val completedLessons = ext.lessonProgressMap.values.count { it.isCompleted }
        val roadmapPct = ((completedLessons * 100f) / 182f).toInt()

        val formattedDate = if (ctrl.todayIso == initialTodayIso) {
            formattedInitialToday
        } else {
            runCatching { LocalDate.parse(ctrl.todayIso).format(DISPLAY_DATE_FORMATTER) }
                .getOrDefault(ctrl.todayIso)
        }

        AcademyUiState(
            profile = DefaultAcademySeed.profile,
            currentSection = ctrl.currentSection,
            lastNavigationLatencyMs = ctrl.lastNavigationLatencyMs,
            todayIso = ctrl.todayIso,
            formattedTodayDate = formattedDate,
            tasks = tasks,
            attendanceRecords = attendanceList,
            studySessions = sessions,
            learningSummaries = summaries,
            todayAttendance = todayAtt,
            totalStudyMinutes = totalMinutes,
            todayStudyMinutes = todayMinutes,
            completedTasksCount = completedCount,
            totalTasksCount = totalTasks,
            taskCompletionPercentage = completionPct,
            overallProgressFraction = progressFrac,
            learningStreakDays = streak,
            selectedCalendarYearMonth = ctrl.selectedCalendarYearMonth,
            selectedAttendanceDate = ctrl.selectedAttendanceDate,
            selectedAttendanceStatus = ctrl.selectedAttendanceStatus,
            selectedAttendanceNote = ctrl.selectedAttendanceNote,
            timer = timerState,
            summaryForm = ctrl.summaryForm,
            statusBannerMessage = ctrl.statusBannerMessage,
            lastExportedJson = ctrl.lastExportedJson,
            lastExportedCsv = ctrl.lastExportedCsv,
            lastExportedMarkdown = ctrl.lastExportedMarkdown,
            activeUser = ext.activeUser,
            registeredUsersCount = ext.registeredUsersCount,
            isAuthScreenOpen = ctrl.isAuthScreenOpen,
            authErrorMessage = ctrl.authErrorMessage,
            selectedPhaseNumber = ctrl.selectedPhaseNumber,
            selectedWeekNumber = ctrl.selectedWeekNumber,
            selectedLessonDay = ctrl.selectedLessonDay,
            roadmapSearchQuery = ctrl.roadmapSearchQuery,
            librarySearchQuery = ctrl.librarySearchQuery,
            lessonProgressMap = ext.lessonProgressMap,
            completedLessonsCount = completedLessons,
            totalLessonsCount = 182,
            roadmapCompletionPercentage = roadmapPct,
            resourceProgressMap = ext.resourceProgressMap,
            projects = ext.projects,
            libraryNotes = ext.libraryNotes,
            topicReviewsMap = ext.topicReviewsMap,
            latestUndoSnapshot = ext.latestUndoSnapshot,
            darkThemeEnabled = ctrl.darkThemeEnabled,
            reducedMotionEnabled = ctrl.reducedMotionEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AcademyUiState(
            todayIso = initialTodayIso,
            formattedTodayDate = formattedInitialToday,
            selectedCalendarYearMonth = YearMonth.from(initialTodayDate),
            selectedAttendanceDate = initialTodayIso,
            summaryForm = SummaryFormState(date = initialTodayIso)
        )
    )

    // --- Fast Client-Side Navigation with Actual Measured Latency ---
    fun navigateTo(section: AcademySection) {
        val startNs = System.nanoTime()
        controlState.update { state ->
            val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000.0).coerceAtLeast(0.05)
            state.copy(
                currentSection = section,
                lastNavigationLatencyMs = elapsedMs
            )
        }
    }

    fun clearBannerMessage() {
        controlState.update { it.copy(statusBannerMessage = null) }
    }

    // --- Theme & Accessibility Preferences ---
    fun toggleDarkTheme(enabled: Boolean) {
        controlState.update {
            it.copy(
                darkThemeEnabled = enabled,
                statusBannerMessage = if (enabled) "Dark Theme enabled" else "Light Theme enabled"
            )
        }
    }

    fun toggleReducedMotion(enabled: Boolean) {
        controlState.update {
            it.copy(
                reducedMotionEnabled = enabled,
                statusBannerMessage = if (enabled) "Reduced motion enabled" else "Standard motion enabled"
            )
        }
    }

    // --- User Authentication Actions ---
    fun setAuthScreenOpen(open: Boolean) {
        controlState.update {
            it.copy(isAuthScreenOpen = open, authErrorMessage = null)
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            val result = repository.registerUser(email, password, displayName)
            result.onSuccess { user ->
                controlState.update {
                    it.copy(
                        isAuthScreenOpen = false,
                        authErrorMessage = null,
                        statusBannerMessage = "Account created! Welcome, ${user.displayName} (${user.email})."
                    )
                }
            }.onFailure { err ->
                controlState.update {
                    it.copy(authErrorMessage = err.message ?: "Sign up failed.")
                }
            }
        }
    }

    fun logIn(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            result.onSuccess { user ->
                controlState.update {
                    it.copy(
                        isAuthScreenOpen = false,
                        authErrorMessage = null,
                        statusBannerMessage = "Logged in as ${user.displayName} (${user.email})."
                    )
                }
            }.onFailure { err ->
                controlState.update {
                    it.copy(authErrorMessage = err.message ?: "Login failed.")
                }
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            repository.logoutUser()
            controlState.update {
                it.copy(
                    isAuthScreenOpen = true,
                    authErrorMessage = null,
                    statusBannerMessage = "Logged out safely. Sign in to access your personalized session."
                )
            }
        }
    }

    // --- Attendance Actions ---
    fun selectAttendanceDate(dateIso: String) {
        val existing = uiState.value.attendanceRecords.firstOrNull { it.date == dateIso }
        controlState.update {
            it.copy(
                selectedAttendanceDate = dateIso,
                selectedAttendanceStatus = existing?.let { rec -> AttendanceStatus.fromLabel(rec.status) }
                    ?: AttendanceStatus.PRESENT,
                selectedAttendanceNote = existing?.note ?: ""
            )
        }
    }

    fun updateSelectedAttendanceStatus(status: AttendanceStatus) {
        controlState.update { it.copy(selectedAttendanceStatus = status) }
    }

    fun updateSelectedAttendanceNote(note: String) {
        controlState.update { it.copy(selectedAttendanceNote = note) }
    }

    fun changeCalendarMonth(offsetMonths: Long) {
        controlState.update {
            it.copy(selectedCalendarYearMonth = it.selectedCalendarYearMonth.plusMonths(offsetMonths))
        }
    }

    fun saveAttendance(
        dateIso: String = controlState.value.selectedAttendanceDate,
        status: AttendanceStatus = controlState.value.selectedAttendanceStatus,
        note: String = controlState.value.selectedAttendanceNote
    ) {
        viewModelScope.launch {
            val result = repository.markAttendance(
                date = dateIso,
                status = status,
                note = note,
                todayIso = controlState.value.todayIso
            )
            result.onSuccess {
                controlState.update { state ->
                    state.copy(
                        selectedAttendanceDate = dateIso,
                        selectedAttendanceStatus = status,
                        selectedAttendanceNote = note,
                        statusBannerMessage = "Attendance saved: ${status.label} on $dateIso"
                    )
                }
            }.onFailure { err ->
                controlState.update { state ->
                    state.copy(
                        statusBannerMessage = err.message ?: "Could not save attendance"
                    )
                }
            }
        }
    }

    // --- Daily Tasks Actions ---
    fun toggleTaskCompletion(taskId: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(taskId, completed)
            controlState.update {
                it.copy(
                    statusBannerMessage = if (completed) "Task #$taskId marked completed" else "Task #$taskId marked incomplete"
                )
            }
        }
    }

    fun updateTaskNotes(taskId: Int, notes: String) {
        viewModelScope.launch {
            repository.updateTaskNotes(taskId, notes)
            controlState.update {
                it.copy(statusBannerMessage = "Saved notes for Task #$taskId")
            }
        }
    }

    // --- 26-Week Roadmap & Daily Lesson Actions ---
    fun selectPhase(phaseNumber: Int) {
        val clampedPhase = phaseNumber.coerceIn(1, 7)
        val firstWeek = CurriculumCatalog.phases.first { it.phaseNumber == clampedPhase }.weeks.first
        val firstDay = (firstWeek - 1) * 7 + 1
        controlState.update {
            it.copy(
                selectedPhaseNumber = clampedPhase,
                selectedWeekNumber = firstWeek,
                selectedLessonDay = firstDay
            )
        }
    }

    fun selectWeek(weekNumber: Int) {
        val clampedWeek = weekNumber.coerceIn(1, 26)
        val phaseNum = CurriculumCatalog.phases.firstOrNull { clampedWeek in it.weeks }?.phaseNumber ?: 1
        val firstDay = (clampedWeek - 1) * 7 + 1
        controlState.update {
            it.copy(
                selectedPhaseNumber = phaseNum,
                selectedWeekNumber = clampedWeek,
                selectedLessonDay = firstDay
            )
        }
    }

    fun selectLessonDay(dayNumber: Int) {
        val lesson = CurriculumCatalog.getLessonByDay(dayNumber)
        controlState.update {
            it.copy(
                selectedPhaseNumber = lesson.phaseNumber,
                selectedWeekNumber = lesson.weekNumber,
                selectedLessonDay = lesson.dayNumber
            )
        }
    }

    fun updateRoadmapSearchQuery(query: String) {
        controlState.update { it.copy(roadmapSearchQuery = query) }
    }

    fun saveLessonDetails(
        dayNumber: Int,
        isCompleted: Boolean? = null,
        notes: String? = null,
        difficultyRating: Int? = null,
        quizScore: Int? = null,
        evidenceLink: String? = null,
        scheduledDate: String? = null
    ) {
        viewModelScope.launch {
            repository.saveLessonProgress(
                dayNumber = dayNumber,
                isCompleted = isCompleted,
                notes = notes,
                difficultyRating = difficultyRating,
                quizScore = quizScore,
                evidenceLink = evidenceLink,
                scheduledDate = scheduledDate
            )
            val statusMsg = when {
                scheduledDate != null -> "Rescheduled Day $dayNumber to $scheduledDate (original lesson preserved)"
                quizScore != null -> "Day $dayNumber Quiz scored: $quizScore/5"
                isCompleted == true -> "Day $dayNumber marked complete!"
                isCompleted == false -> "Day $dayNumber marked incomplete"
                else -> "Saved progress for Day $dayNumber"
            }
            controlState.update { it.copy(statusBannerMessage = statusMsg) }
        }
    }

    fun saveResourceTracking(
        resourceId: String,
        dayNumber: Int,
        isViewed: Boolean? = null,
        isCompleted: Boolean? = null,
        personalNotes: String? = null,
        difficultyEncountered: String? = null
    ) {
        viewModelScope.launch {
            repository.saveResourceProgress(
                resourceId = resourceId,
                dayNumber = dayNumber,
                isViewed = isViewed,
                isCompleted = isCompleted,
                personalNotes = personalNotes,
                difficultyEncountered = difficultyEncountered
            )
            controlState.update {
                it.copy(statusBannerMessage = "Updated resource tracking for Day $dayNumber (lesson completion stays separate)")
            }
        }
    }

    // --- Study Timer Actions (Accurate Wall-Clock Delta, Optional Breaks, Duplicate Guard) ---
    fun updateSessionTopic(topic: String) {
        _timerFlow.update { it.copy(sessionTopic = topic) }
    }

    fun configureTimerDuration(minutes: Int, isBreak: Boolean = false) {
        timerJob?.cancel()
        timerJob = null
        val validMins = minutes.coerceIn(1, 240)
        _timerFlow.update {
            it.copy(
                status = TimerStatus.IDLE,
                targetMinutes = validMins,
                remainingSeconds = validMins * 60,
                elapsedSeconds = 0,
                isBreakMode = isBreak
            )
        }
    }

    fun associateTimerWithLesson(dayNumber: Int) {
        val lesson = CurriculumCatalog.getLessonByDay(dayNumber)
        _timerFlow.update {
            it.copy(
                associatedDayNumber = lesson.dayNumber,
                sessionTopic = "Day ${lesson.dayNumber}: ${lesson.title}"
            )
        }
    }

    fun startTimer() {
        val current = _timerFlow.value
        if (current.status == TimerStatus.RUNNING) return

        lastSavedSessionSignature = null
        lastTickTimestampMs = System.currentTimeMillis()
        _timerFlow.update {
            val resetRemaining = if (it.remainingSeconds <= 0) it.targetMinutes * 60 else it.remainingSeconds
            it.copy(
                status = TimerStatus.RUNNING,
                remainingSeconds = resetRemaining
            )
        }
        startTickerLoop()
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerFlow.update {
            if (it.status == TimerStatus.RUNNING) {
                it.copy(status = TimerStatus.PAUSED)
            } else {
                it
            }
        }
    }

    fun resumeTimer() {
        val current = _timerFlow.value
        if (current.status != TimerStatus.PAUSED) return
        lastTickTimestampMs = System.currentTimeMillis()
        _timerFlow.update { it.copy(status = TimerStatus.RUNNING) }
        startTickerLoop()
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerFlow.update {
            TimerUiState(
                status = TimerStatus.IDLE,
                targetMinutes = it.targetMinutes,
                remainingSeconds = it.targetMinutes * 60,
                elapsedSeconds = 0,
                isBreakMode = it.isBreakMode,
                associatedDayNumber = it.associatedDayNumber,
                sessionTopic = it.sessionTopic
            )
        }
    }

    fun advanceTimerSeconds(secondsToAdvance: Int) {
        if (secondsToAdvance <= 0) return
        lastSavedSessionSignature = null
        _timerFlow.update { t ->
            val actualAdvance = secondsToAdvance.coerceAtMost(t.remainingSeconds)
            val newRemaining = (t.remainingSeconds - actualAdvance).coerceAtLeast(0)
            val newElapsed = t.elapsedSeconds + actualAdvance
            val newStatus = when {
                newRemaining == 0 -> {
                    timerJob?.cancel()
                    TimerStatus.COMPLETED
                }
                t.status == TimerStatus.IDLE -> TimerStatus.PAUSED
                else -> t.status
            }
            t.copy(
                status = newStatus,
                remainingSeconds = newRemaining,
                elapsedSeconds = newElapsed
            )
        }
    }

    fun saveStudySession(customMinutes: Int? = null) {
        val timer = _timerFlow.value
        val minutesToSave = customMinutes ?: timer.savableMinutes
        val secondsToSave = if (customMinutes != null) customMinutes * 60 else timer.elapsedSeconds

        if (minutesToSave <= 0 && secondsToSave <= 0) {
            controlState.update {
                it.copy(statusBannerMessage = "Study session not saved: 0 minutes elapsed.")
            }
            return
        }

        // Prevent duplicate session records when saving/finishing the same session
        val signature = "${controlState.value.todayIso}_${timer.sessionTopic}_${minutesToSave}_${secondsToSave}"
        if (signature == lastSavedSessionSignature && timer.status == TimerStatus.IDLE) {
            return
        }
        lastSavedSessionSignature = signature

        timerJob?.cancel()
        timerJob = null

        viewModelScope.launch {
            val effectiveMinutes = minutesToSave.coerceAtLeast(1)
            repository.saveStudySession(
                date = controlState.value.todayIso,
                durationMinutes = effectiveMinutes,
                durationSeconds = secondsToSave.coerceAtLeast(effectiveMinutes * 60),
                targetMinutes = timer.targetMinutes,
                topic = timer.sessionTopic
            )
            _timerFlow.update { t ->
                TimerUiState(
                    status = TimerStatus.IDLE,
                    targetMinutes = t.targetMinutes,
                    remainingSeconds = t.targetMinutes * 60,
                    elapsedSeconds = 0,
                    isBreakMode = false,
                    associatedDayNumber = t.associatedDayNumber,
                    sessionTopic = t.sessionTopic
                )
            }
            controlState.update { state ->
                state.copy(statusBannerMessage = "Saved $effectiveMinutes min study session!")
            }
        }
    }

    private fun startTickerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerFlow.value.status == TimerStatus.RUNNING) {
                delay(1000L)
                val now = System.currentTimeMillis()
                val deltaSecs = ((now - lastTickTimestampMs) / 1000L).toInt().coerceIn(1, 3600)
                lastTickTimestampMs = now
                _timerFlow.update { t ->
                    if (t.status != TimerStatus.RUNNING) return@update t
                    val step = deltaSecs.coerceAtMost(t.remainingSeconds)
                    val nextRemaining = (t.remainingSeconds - step).coerceAtLeast(0)
                    val nextElapsed = t.elapsedSeconds + step
                    val nextStatus = if (nextRemaining == 0) TimerStatus.COMPLETED else TimerStatus.RUNNING
                    t.copy(
                        status = nextStatus,
                        remainingSeconds = nextRemaining,
                        elapsedSeconds = nextElapsed
                    )
                }
            }
        }
    }

    // --- Daily Learning Summary Actions ---
    fun updateSummaryDate(date: String) {
        controlState.update { it.copy(summaryForm = it.summaryForm.copy(date = date)) }
    }

    fun updateSummaryTopics(topics: String) {
        controlState.update { it.copy(summaryForm = it.summaryForm.copy(topicsLearned = topics)) }
    }

    fun updateSummaryUnderstood(understood: String) {
        controlState.update { it.copy(summaryForm = it.summaryForm.copy(whatUnderstood = understood)) }
    }

    fun updateSummaryDifficult(difficult: String) {
        controlState.update { it.copy(summaryForm = it.summaryForm.copy(whatWasDifficult = difficult)) }
    }

    fun updateSummaryQuestions(questions: String) {
        controlState.update { it.copy(summaryForm = it.summaryForm.copy(questionsForNextSession = questions)) }
    }

    fun updateSummaryRating(rating: Int) {
        controlState.update { it.copy(summaryForm = it.summaryForm.copy(selfRating = rating.coerceIn(1, 5))) }
    }

    fun startEditingSummary(summary: LearningSummaryEntity) {
        controlState.update {
            it.copy(
                summaryForm = SummaryFormState(
                    editingId = summary.id,
                    date = summary.date,
                    topicsLearned = summary.topicsLearned,
                    whatUnderstood = summary.whatUnderstood,
                    whatWasDifficult = summary.whatWasDifficult,
                    questionsForNextSession = summary.questionsForNextSession,
                    selfRating = summary.selfRating
                )
            )
        }
    }

    fun cancelEditingSummary() {
        controlState.update {
            it.copy(summaryForm = SummaryFormState(date = it.todayIso))
        }
    }

    fun saveLearningSummary() {
        val form = controlState.value.summaryForm
        if (form.topicsLearned.isBlank() && form.whatUnderstood.isBlank()) {
            controlState.update {
                it.copy(statusBannerMessage = "Please enter topics learned before saving.")
            }
            return
        }
        viewModelScope.launch {
            val isEditing = form.editingId != 0L
            repository.saveLearningSummary(
                id = form.editingId,
                date = form.date.ifBlank { controlState.value.todayIso },
                topicsLearned = form.topicsLearned,
                whatUnderstood = form.whatUnderstood,
                whatWasDifficult = form.whatWasDifficult,
                questionsForNextSession = form.questionsForNextSession,
                selfRating = form.selfRating
            )
            controlState.update {
                it.copy(
                    summaryForm = SummaryFormState(date = it.todayIso),
                    statusBannerMessage = if (isEditing) "Learning summary updated!" else "Learning summary saved!"
                )
            }
        }
    }

    fun deleteLearningSummary(id: Long) {
        viewModelScope.launch {
            repository.deleteLearningSummary(id)
            controlState.update {
                val resetForm = if (it.summaryForm.editingId == id) {
                    SummaryFormState(date = it.todayIso)
                } else {
                    it.summaryForm
                }
                it.copy(
                    summaryForm = resetForm,
                    statusBannerMessage = "Summary deleted."
                )
            }
        }
    }

    // --- Projects, Notes Library, and Flashcard Review Actions ---
    fun updateLibrarySearchQuery(query: String) {
        controlState.update { it.copy(librarySearchQuery = query) }
    }

    fun saveProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.saveProject(project)
            controlState.update {
                it.copy(statusBannerMessage = "Saved project: ${project.title}")
            }
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            controlState.update {
                it.copy(statusBannerMessage = "Deleted project.")
            }
        }
    }

    fun saveLibraryNote(
        id: Long = 0,
        title: String,
        content: String,
        category: String,
        associatedDay: Int? = null,
        associatedWeek: Int? = 1
    ) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            repository.saveLibraryNote(
                LibraryNoteEntity(
                    id = id,
                    title = title.trim(),
                    content = content.trim(),
                    category = category.trim(),
                    associatedDay = associatedDay,
                    associatedWeek = associatedWeek
                )
            )
            controlState.update {
                it.copy(statusBannerMessage = "Saved note to Library: $title")
            }
        }
    }

    fun deleteLibraryNote(id: Long) {
        viewModelScope.launch {
            repository.deleteLibraryNote(id)
            controlState.update {
                it.copy(statusBannerMessage = "Removed note from Library.")
            }
        }
    }

    fun updateTopicReviewStatus(
        topicId: String,
        phaseNumber: Int,
        weekNumber: Int,
        topicTitle: String,
        status: String
    ) {
        viewModelScope.launch {
            repository.updateTopicReviewStatus(
                topicId = topicId,
                phaseNumber = phaseNumber,
                weekNumber = weekNumber,
                topicTitle = topicTitle,
                status = status
            )
            controlState.update {
                it.copy(statusBannerMessage = "Updated '$topicTitle' review status to $status")
            }
        }
    }

    // --- Progress, Settings, Backup, Merge/Replace & Undo Actions ---
    fun resetDemoData() {
        timerJob?.cancel()
        timerJob = null
        _timerFlow.value = TimerUiState()
        viewModelScope.launch {
            repository.resetAllDemoData(controlState.value.todayIso)
            controlState.update {
                it.copy(
                    selectedAttendanceDate = it.todayIso,
                    selectedAttendanceStatus = AttendanceStatus.PRESENT,
                    selectedAttendanceNote = "",
                    summaryForm = SummaryFormState(date = it.todayIso),
                    lastExportedJson = null,
                    statusBannerMessage = "Demo data reset to zero progress (Undo snapshot saved)."
                )
            }
        }
    }

    fun undoLastReplaceOrReset() {
        viewModelScope.launch {
            val result = repository.undoLastDestructiveOperation()
            result.onSuccess {
                controlState.update {
                    it.copy(statusBannerMessage = "Undo successful! Previous records restored.")
                }
            }.onFailure { err ->
                controlState.update {
                    it.copy(statusBannerMessage = "Undo unavailable: ${err.message}")
                }
            }
        }
    }

    fun exportDataToJson(onExportReady: (String) -> Unit = {}) {
        viewModelScope.launch {
            val json = repository.exportToJson(controlState.value.todayIso)
            controlState.update {
                it.copy(
                    lastExportedJson = json,
                    statusBannerMessage = "JSON backup exported successfully (Schema v2)."
                )
            }
            onExportReady(json)
        }
    }

    fun exportDataToCsv(onExportReady: (String) -> Unit = {}) {
        viewModelScope.launch {
            val csv = repository.exportAttendanceAndSessionsCsv()
            controlState.update {
                it.copy(
                    lastExportedCsv = csv,
                    statusBannerMessage = "CSV exported for attendance and study sessions."
                )
            }
            onExportReady(csv)
        }
    }

    fun exportDataToMarkdownReport(onExportReady: (String) -> Unit = {}) {
        viewModelScope.launch {
            val md = repository.exportWeeklyReportMarkdown(controlState.value.todayIso)
            controlState.update {
                it.copy(
                    lastExportedMarkdown = md,
                    statusBannerMessage = "Printable Markdown Weekly Report generated."
                )
            }
            onExportReady(md)
        }
    }

    fun importDataFromJson(
        jsonString: String,
        mode: BackupImportMode = BackupImportMode.REPLACE
    ) {
        viewModelScope.launch {
            val result = repository.importFromJson(jsonString, mode)
            result.onSuccess { payload ->
                controlState.update {
                    it.copy(
                        lastExportedJson = jsonString,
                        statusBannerMessage = "${mode.label}: Imported ${payload.tasks.size} tasks, ${payload.attendance.size} attendance, ${payload.studySessions.size} sessions, ${payload.learningSummaries.size} summaries."
                    )
                }
            }.onFailure { err ->
                controlState.update {
                    it.copy(statusBannerMessage = "JSON import failed: ${err.message}")
                }
            }
        }
    }

    private fun calculateStreak(records: List<AttendanceEntity>, todayIso: String): Int {
        if (records.isEmpty()) return 0
        val byDate = records.associateBy { it.date }
        val todayDate = runCatching { LocalDate.parse(todayIso) }.getOrNull() ?: return 0

        var streak = 0
        var cursor = todayDate

        val todayRec = byDate[cursor.toString()]
        if (todayRec == null) {
            cursor = cursor.minusDays(1)
        }

        var safetyCounter = 0
        while (safetyCounter < 366) {
            safetyCounter++
            val rec = byDate[cursor.toString()] ?: break
            when (AttendanceStatus.fromLabel(rec.status)) {
                AttendanceStatus.PRESENT, AttendanceStatus.PARTIAL -> {
                    streak++
                    cursor = cursor.minusDays(1)
                }
                AttendanceStatus.REST_DAY -> {
                    cursor = cursor.minusDays(1)
                }
                AttendanceStatus.ABSENT -> break
            }
        }
        return streak
    }

    class Factory(
        private val repository: AcademyRepository,
        private val initialTodayDate: LocalDate = LocalDate.now()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AcademyViewModel(repository, initialTodayDate) as T
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
