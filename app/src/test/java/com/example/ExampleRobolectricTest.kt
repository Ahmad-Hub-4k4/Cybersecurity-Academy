package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.AcademyDatabase
import com.example.data.AcademyRepository
import com.example.data.BackupImportMode
import com.example.data.CurriculumCatalog
import com.example.data.ProjectEntity
import com.example.ui.AcademyApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AcademySection
import com.example.viewmodel.AcademyViewModel
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: AcademyDatabase
    private lateinit var repository: AcademyRepository
    private val fixedToday = LocalDate.of(2026, 10, 9)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = AcademyDatabase.createInMemory(context)
        repository = AcademyRepository(database.academyDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun waitForState(predicate: () -> Boolean) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.waitForIdle()
            predicate()
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Waqas Cyber Academy", appName)
    }

    @Test
    fun `test all 6 required academy dashboard workflows and persistence across refresh`() {
        val viewModel1 = AcademyViewModel(repository, initialTodayDate = fixedToday)

        composeTestRule.setContent {
            MyApplicationTheme {
                AcademyApp(viewModel = viewModel1)
            }
        }

        // Wait for initial 5 Week 1 tasks to be seeded
        waitForState { viewModel1.uiState.value.tasks.size == 5 }

        // Verify initial state: Welcome message, 0% progress, 0/5 tasks, 0 study minutes
        composeTestRule.onNodeWithTag("welcome_message")
            .assertIsDisplayed()
            .assertTextContains("Welcome back, Waqas.")
        composeTestRule.onNodeWithTag("dashboard_overall_progress")
            .assertTextContains("0%")

        composeTestRule.onNodeWithTag("dashboard_screen")
            .performScrollToNode(hasTestTag("dashboard_completed_tasks"))
        composeTestRule.onNodeWithTag("dashboard_completed_tasks", useUnmergedTree = true)
            .assertTextContains("0 / 5")
        composeTestRule.onNodeWithTag("dashboard_total_minutes", useUnmergedTree = true)
            .assertTextContains("0 min")

        // WORKFLOW 1: Mark today's attendance (and test editing attendance)
        composeTestRule.onNodeWithTag("nav_attendance").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("attendance_status_present").performClick()
        composeTestRule.onNodeWithTag("attendance_note_input").performTextInput("Kali Linux VM Lab Day 1")
        composeTestRule.onNodeWithTag("save_attendance_button").performClick()

        waitForState { viewModel1.uiState.value.todayAttendance?.status == "Present" }
        assertEquals("Present", viewModel1.uiState.value.todayAttendance?.status)
        assertEquals(1, viewModel1.uiState.value.learningStreakDays)

        // Verify editing attendance to Partial and back to Present
        composeTestRule.onNodeWithTag("attendance_status_partial").performClick()
        composeTestRule.onNodeWithTag("save_attendance_button").performClick()
        waitForState { viewModel1.uiState.value.todayAttendance?.status == "Partial" }
        assertEquals("Partial", viewModel1.uiState.value.todayAttendance?.status)

        composeTestRule.onNodeWithTag("attendance_status_present").performClick()
        composeTestRule.onNodeWithTag("save_attendance_button").performClick()
        waitForState { viewModel1.uiState.value.todayAttendance?.status == "Present" }
        assertEquals("Present", viewModel1.uiState.value.todayAttendance?.status)

        // Verify future dates are never marked absent automatically
        val tomorrowIso = fixedToday.plusDays(1).toString()
        assertTrue(viewModel1.uiState.value.attendanceRecords.none { it.date == tomorrowIso })

        // WORKFLOW 2: Complete a task and verify progress updates
        composeTestRule.onNodeWithTag("nav_tasks").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tasks_progress_percentage").assertTextContains("0%")
        composeTestRule.onNodeWithTag("task_checkbox_1").performClick()

        waitForState { viewModel1.uiState.value.completedTasksCount == 1 }
        assertEquals(1, viewModel1.uiState.value.completedTasksCount)
        assertEquals(20, viewModel1.uiState.value.taskCompletionPercentage)
        composeTestRule.onNodeWithTag("tasks_progress_percentage").assertTextContains("20%")

        // Save optional task notes on Task #1
        composeTestRule.onNodeWithTag("task_toggle_notes_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("task_notes_input_1").performTextInput("Practised man ls and uname -a in Kali")
        composeTestRule.onNodeWithTag("task_notes_save_1").performClick()

        waitForState {
            viewModel1.uiState.value.tasks.firstOrNull { it.id == 1 }?.notes == "Practised man ls and uname -a in Kali"
        }

        // WORKFLOW 3: Write and save a learning summary
        composeTestRule.onNodeWithTag("nav_summary").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("summary_topics_input")
            .performScrollTo()
            .performTextInput("Linux CLI navigation, pwd, ls -l, and chmod permissions")
        composeTestRule.onNodeWithTag("summary_understood_input")
            .performScrollTo()
            .performTextInput("Understood octal rwx bits (750) and safe file flags (-i)")
        composeTestRule.onNodeWithTag("summary_difficult_input")
            .performScrollTo()
            .performTextInput("Remembering symbolic chmod syntax")
        composeTestRule.onNodeWithTag("summary_questions_input")
            .performScrollTo()
            .performTextInput("How do special permissions like SUID affect security?")
        composeTestRule.onNodeWithTag("summary_rating_5")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("save_summary_button")
            .performScrollTo()
            .performClick()

        waitForState { viewModel1.uiState.value.learningSummaries.size == 1 }
        val savedSummary = viewModel1.uiState.value.learningSummaries.first()
        assertEquals(5, savedSummary.selfRating)
        assertTrue(savedSummary.topicsLearned.contains("chmod"))

        // WORKFLOW 4: Start and pause the timer (verify Start alone does not count a session completed)
        composeTestRule.onNodeWithTag("nav_timer").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("timer_remaining_display").assertTextContains("90:00")
        composeTestRule.onNodeWithTag("timer_start_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("timer_status_badge").assertTextContains("RUNNING")
        // Verify clicking Start alone did NOT count a session as completed
        assertEquals(0, viewModel1.uiState.value.totalStudyMinutes)
        assertTrue(viewModel1.uiState.value.studySessions.isEmpty())

        // Pause the timer
        composeTestRule.onNodeWithTag("timer_pause_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("timer_status_badge").assertTextContains("PAUSED")

        // Advance elapsed time by 15 minutes and save the session
        composeTestRule.onNodeWithTag("timer_advance_15m")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("timer_remaining_display").assertTextContains("75:00")

        composeTestRule.onNodeWithTag("timer_save_session_button")
            .performScrollTo()
            .performClick()
        waitForState { viewModel1.uiState.value.totalStudyMinutes == 15 }
        assertEquals(15, viewModel1.uiState.value.totalStudyMinutes)
        assertEquals(1, viewModel1.uiState.value.studySessions.size)

        // WORKFLOW 5: Refresh / Reopen the application (new ViewModel against persistent Room DB) and verify saved data remains
        val refreshedViewModel = AcademyViewModel(repository, initialTodayDate = fixedToday)
        waitForState {
            refreshedViewModel.uiState.value.tasks.size == 5 &&
                refreshedViewModel.uiState.value.completedTasksCount == 1 &&
                refreshedViewModel.uiState.value.totalStudyMinutes == 15 &&
                refreshedViewModel.uiState.value.learningSummaries.size == 1 &&
                refreshedViewModel.uiState.value.todayAttendance?.status == "Present"
        }
        assertEquals("Present", refreshedViewModel.uiState.value.todayAttendance?.status)
        assertEquals(1, refreshedViewModel.uiState.value.completedTasksCount)
        assertEquals(20, refreshedViewModel.uiState.value.taskCompletionPercentage)
        assertEquals(15, refreshedViewModel.uiState.value.totalStudyMinutes)
        assertEquals(1, refreshedViewModel.uiState.value.learningSummaries.size)

        // WORKFLOW 6: Export the data to JSON, Reset Demo Data, and Import JSON to restore
        composeTestRule.onNodeWithTag("nav_progress").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("progress_completion_percentage").assertTextContains("20%")
        composeTestRule.onNodeWithTag("progress_actual_study_time").assertTextContains("15 min")

        composeTestRule.onNodeWithTag("progress_settings_screen")
            .performScrollToNode(hasTestTag("export_json_button"))
        composeTestRule.onNodeWithTag("export_json_button").performClick()

        waitForState { !viewModel1.uiState.value.lastExportedJson.isNullOrBlank() }
        val exportedJson = viewModel1.uiState.value.lastExportedJson
        assertNotNull(exportedJson)
        assertTrue(exportedJson!!.contains("Waqas Cybersecurity Academy Test Dashboard"))
        assertTrue(exportedJson.contains("Practised man ls and uname -a in Kali"))

        // Test Reset Demo Data with confirmation
        composeTestRule.onNodeWithTag("progress_settings_screen")
            .performScrollToNode(hasTestTag("reset_demo_data_button"))
        composeTestRule.onNodeWithTag("reset_demo_data_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_reset_button").performClick()

        waitForState {
            viewModel1.uiState.value.completedTasksCount == 0 &&
                viewModel1.uiState.value.totalStudyMinutes == 0 &&
                viewModel1.uiState.value.attendanceRecords.isEmpty()
        }
        assertEquals(0, viewModel1.uiState.value.completedTasksCount)
        assertEquals(0, viewModel1.uiState.value.totalStudyMinutes)
        assertFalse(viewModel1.uiState.value.tasks.any { it.isCompleted })

        // Restore via Import JSON
        viewModel1.importDataFromJson(exportedJson)
        waitForState {
            viewModel1.uiState.value.completedTasksCount == 1 &&
                viewModel1.uiState.value.totalStudyMinutes == 15 &&
                viewModel1.uiState.value.learningSummaries.size == 1 &&
                viewModel1.uiState.value.todayAttendance?.status == "Present"
        }
        assertEquals(1, viewModel1.uiState.value.completedTasksCount)
        assertEquals(15, viewModel1.uiState.value.totalStudyMinutes)
        assertEquals(1, viewModel1.uiState.value.learningSummaries.size)
        assertEquals("Present", viewModel1.uiState.value.todayAttendance?.status)
    }

    @Test
    fun `test Waqas Cyber Academy 2_0 upgrade workflows without losing data`() {
        val viewModel = AcademyViewModel(repository, initialTodayDate = fixedToday)

        composeTestRule.setContent {
            AcademyApp(viewModel = viewModel)
        }

        // Wait for initial seed (5 Week 1 tasks + 4 Starter Capstone Projects)
        waitForState {
            viewModel.uiState.value.tasks.size == 5 &&
                viewModel.uiState.value.projects.size == 4
        }

        // 1. Verify 26-Week / 182-Day Curriculum Catalog integrity
        assertEquals(7, CurriculumCatalog.phases.size)
        assertEquals(182, (1..182).map { CurriculumCatalog.getLessonByDay(it) }.size)
        val day1 = CurriculumCatalog.getLessonByDay(1)
        assertEquals(1, day1.phaseNumber)
        assertEquals(1, day1.weekNumber)
        assertEquals(5, day1.quizQuestions.size)
        val day182 = CurriculumCatalog.getLessonByDay(182)
        assertEquals(7, day182.phaseNumber)
        assertEquals(26, day182.weekNumber)

        // 2. Test User Authentication (Sign Up, Log Out, Log In, Invalid Password rejection)
        viewModel.signUp("waqas@cyberacademy.edu", "KaliLab2026!", "Waqas Ahmad")
        waitForState { viewModel.uiState.value.activeUser?.email == "waqas@cyberacademy.edu" }
        assertEquals("Waqas Ahmad", viewModel.uiState.value.activeUser?.displayName)

        viewModel.logOut()
        waitForState { viewModel.uiState.value.activeUser == null }
        assertNull(viewModel.uiState.value.activeUser)

        // Reject wrong password
        viewModel.logIn("waqas@cyberacademy.edu", "WrongPassword123")
        waitForState { viewModel.uiState.value.authErrorMessage != null }
        assertNull(viewModel.uiState.value.activeUser)

        // Accept valid password
        viewModel.logIn("waqas@cyberacademy.edu", "KaliLab2026!")
        waitForState { viewModel.uiState.value.activeUser?.email == "waqas@cyberacademy.edu" }
        assertNotNull(viewModel.uiState.value.activeUser)

        // 3. Test Fast Navigation across all 8 sections & verify latency measurement
        AcademySection.entries.forEach { section ->
            composeTestRule.onNodeWithTag(section.tag).performClick()
            composeTestRule.waitForIdle()
            assertEquals(section, viewModel.uiState.value.currentSection)
            assertTrue(viewModel.uiState.value.lastNavigationLatencyMs >= 0.05)
        }

        // 4. Test 26-Week Roadmap: Independent Resource Tracking vs Lesson Completion, Quiz, and Rescheduling
        viewModel.navigateTo(AcademySection.ROADMAP)
        composeTestRule.waitForIdle()

        // Mark primary resource of Day 1 viewed and completed — verify Day 1 Lesson is NOT automatically completed
        viewModel.saveResourceTracking(
            resourceId = day1.primaryResource.resourceId,
            dayNumber = 1,
            isViewed = true,
            isCompleted = true,
            personalNotes = "Completed TryHackMe Linux Strength Training room",
            difficultyEncountered = "None"
        )
        waitForState {
            viewModel.uiState.value.resourceProgressMap[day1.primaryResource.resourceId]?.isCompleted == true
        }
        assertFalse(
            "Marking a resource completed must NOT automatically complete the lesson",
            viewModel.uiState.value.lessonProgressMap[1]?.isCompleted == true
        )

        // Now complete Day 1 Lesson, score 5/5 on quiz, and reschedule Day 2 without losing lesson content
        viewModel.saveLessonDetails(
            dayNumber = 1,
            isCompleted = true,
            notes = "Verified Kali snapshot & network hash",
            difficultyRating = 2,
            quizScore = 5,
            evidenceLink = "https://github.com/waqas-cyber/day1-kali-baseline"
        )
        viewModel.saveLessonDetails(
            dayNumber = 2,
            scheduledDate = "2026-10-12"
        )
        waitForState {
            viewModel.uiState.value.completedLessonsCount == 1 &&
                viewModel.uiState.value.lessonProgressMap[2]?.scheduledDate == "2026-10-12"
        }
        assertEquals(5, viewModel.uiState.value.lessonProgressMap[1]?.quizBestScore)
        assertEquals("2026-10-12", viewModel.uiState.value.lessonProgressMap[2]?.scheduledDate)
        assertTrue(CurriculumCatalog.getLessonByDay(2).title.isNotBlank())

        // 5. Test Portfolio Projects & Searchable Notes Library
        viewModel.saveLibraryNote(
            title = "Linux SUID Escalation Cheatsheet",
            content = "find / -perm -4000 -type f 2>/dev/null",
            category = "Linux Security",
            associatedDay = 3,
            associatedWeek = 1
        )
        waitForState { viewModel.uiState.value.libraryNotes.size == 1 }
        assertEquals("Linux SUID Escalation Cheatsheet", viewModel.uiState.value.libraryNotes.first().title)

        val firstProject = viewModel.uiState.value.projects.first()
        viewModel.saveProject(firstProject.copy(status = "Completed", githubUrl = "https://github.com/waqas/linux-audit"))
        waitForState {
            viewModel.uiState.value.projects.first { it.id == firstProject.id }.status == "Completed"
        }

        // 6. Test Spaced Repetition / Topic Review Status
        viewModel.updateTopicReviewStatus(
            topicId = "topic_linux_permissions",
            phaseNumber = 1,
            weekNumber = 1,
            topicTitle = "Linux Octal File Permissions & SUID",
            status = "Mastered"
        )
        waitForState {
            viewModel.uiState.value.topicReviewsMap["topic_linux_permissions"]?.reviewStatus == "Mastered"
        }

        // 7. Test Export Schema v2, Reset with Automatic Undo Snapshot, and Undo Restore
        viewModel.toggleTaskCompletion(1, true)
        waitForState { viewModel.uiState.value.completedTasksCount == 1 }

        viewModel.exportDataToJson()
        waitForState { !viewModel.uiState.value.lastExportedJson.isNullOrBlank() }
        val v2BackupJson = viewModel.uiState.value.lastExportedJson!!
        assertTrue(v2BackupJson.contains("\"version\": 2"))
        assertTrue(v2BackupJson.contains("Linux SUID Escalation Cheatsheet"))

        // Trigger Reset Demo Data — creates automatic Undo Snapshot
        viewModel.resetDemoData()
        waitForState {
            viewModel.uiState.value.completedTasksCount == 0 &&
                viewModel.uiState.value.completedLessonsCount == 0 &&
                viewModel.uiState.value.latestUndoSnapshot != null
        }
        assertEquals(0, viewModel.uiState.value.completedTasksCount)
        assertEquals(0, viewModel.uiState.value.completedLessonsCount)

        // Trigger Undo Last Replace/Reset and verify all records are restored
        viewModel.undoLastReplaceOrReset()
        waitForState {
            viewModel.uiState.value.completedTasksCount == 1 &&
                viewModel.uiState.value.completedLessonsCount == 1 &&
                viewModel.uiState.value.libraryNotes.size == 1
        }
        assertEquals(1, viewModel.uiState.value.completedTasksCount)
        assertEquals(1, viewModel.uiState.value.completedLessonsCount)
        assertEquals("Mastered", viewModel.uiState.value.topicReviewsMap["topic_linux_permissions"]?.reviewStatus)

        // Test Merge Import mode (adds/updates without wiping existing records)
        viewModel.toggleTaskCompletion(2, true)
        waitForState { viewModel.uiState.value.completedTasksCount == 2 }
        viewModel.importDataFromJson(v2BackupJson, mode = BackupImportMode.MERGE)
        waitForState { viewModel.uiState.value.completedTasksCount == 2 }
        assertEquals(2, viewModel.uiState.value.completedTasksCount)
    }
}
