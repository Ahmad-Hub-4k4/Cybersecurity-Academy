package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class AcademyRepository(private val dao: AcademyDao) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val backupAdapter = moshi.adapter(AcademyBackupPayload::class.java).indent("  ")

    // Reactive Flows
    val tasksFlow: Flow<List<DailyTaskEntity>> = dao.observeTasks()
    val attendanceFlow: Flow<List<AttendanceEntity>> = dao.observeAttendance()
    val studySessionsFlow: Flow<List<StudySessionEntity>> = dao.observeStudySessions()
    val learningSummariesFlow: Flow<List<LearningSummaryEntity>> = dao.observeLearningSummaries()
    val activeUserFlow: Flow<UserAccountEntity?> = dao.observeActiveUserAccount()
    val allUsersFlow: Flow<List<UserAccountEntity>> = dao.observeAllUserAccounts()
    val lessonProgressFlow: Flow<List<LessonProgressEntity>> = dao.observeLessonProgress()
    val resourceProgressFlow: Flow<List<ResourceProgressEntity>> = dao.observeResourceProgress()
    val projectsFlow: Flow<List<ProjectEntity>> = dao.observeProjects()
    val libraryNotesFlow: Flow<List<LibraryNoteEntity>> = dao.observeLibraryNotes()
    val topicReviewsFlow: Flow<List<TopicReviewEntity>> = dao.observeTopicReviews()
    val latestBackupSnapshotFlow: Flow<BackupSnapshotEntity?> = dao.observeLatestBackupSnapshot()

    suspend fun ensureSeeded() {
        if (dao.getTaskCount() == 0) {
            dao.insertTasks(DefaultAcademySeed.getInitialWeek1Tasks())
        }
        if (dao.getProjectCount() == 0) {
            dao.insertAllProjects(CurriculumCatalog.getStarterProjects())
        }
    }

    // --- Authentication (Sign Up, Log In, Log Out) ---
    suspend fun registerUser(
        email: String,
        password: String,
        displayName: String = "Waqas Ahmad"
    ): Result<AuthenticatedUser> {
        val cleanEmail = email.trim().lowercase()
        if (!AuthSecurityManager.isValidEmail(cleanEmail)) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        val pwError = AuthSecurityManager.validatePasswordStrength(password)
        if (pwError != null) {
            return Result.failure(IllegalArgumentException(pwError))
        }
        val existing = dao.getUserAccountByEmail(cleanEmail)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists. Please log in."))
        }

        val salt = AuthSecurityManager.generateSaltBase64()
        val hash = AuthSecurityManager.hashPassword(password.toCharArray(), salt)
        val now = System.currentTimeMillis()
        dao.clearActiveUserSessions()

        val account = UserAccountEntity(
            email = cleanEmail,
            displayName = displayName.trim().ifBlank { "Waqas Ahmad" },
            passwordHash = hash,
            passwordSalt = salt,
            isActiveSession = true,
            createdAt = now,
            lastLoginAt = now
        )
        val newId = dao.insertUserAccount(account)
        return Result.success(
            AuthenticatedUser(
                id = newId,
                email = account.email,
                displayName = account.displayName,
                lastLoginAt = now
            )
        )
    }

    suspend fun loginUser(email: String, password: String): Result<AuthenticatedUser> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required."))
        }
        val account = dao.getUserAccountByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("Invalid email or password."))

        val verified = AuthSecurityManager.verifyPassword(
            password = password.toCharArray(),
            saltBase64 = account.passwordSalt,
            expectedHashBase64 = account.passwordHash
        )
        if (!verified) {
            return Result.failure(IllegalArgumentException("Invalid email or password."))
        }

        val now = System.currentTimeMillis()
        dao.clearActiveUserSessions()
        dao.setActiveUserSession(account.id, now)
        return Result.success(
            AuthenticatedUser(
                id = account.id,
                email = account.email,
                displayName = account.displayName,
                lastLoginAt = now
            )
        )
    }

    suspend fun logoutUser() {
        dao.clearActiveUserSessions()
    }

    // --- Daily Tasks (Version 1 Preserved) ---
    suspend fun toggleTaskCompletion(taskId: Int, completed: Boolean) {
        val tasks = dao.getTasksSnapshot()
        val target = tasks.firstOrNull { it.id == taskId } ?: return
        dao.updateTask(
            target.copy(
                isCompleted = completed,
                completedAt = if (completed) System.currentTimeMillis() else null
            )
        )
    }

    suspend fun updateTaskNotes(taskId: Int, notes: String) {
        val tasks = dao.getTasksSnapshot()
        val target = tasks.firstOrNull { it.id == taskId } ?: return
        dao.updateTask(target.copy(notes = notes.trim()))
    }

    // --- Attendance (Version 1 Preserved + Rescheduling Support) ---
    suspend fun markAttendance(
        date: String,
        status: AttendanceStatus,
        note: String = "",
        todayIso: String = LocalDate.now().toString()
    ): Result<AttendanceEntity> {
        if (date > todayIso && status == AttendanceStatus.ABSENT) {
            return Result.failure(IllegalArgumentException("Future dates cannot be marked Absent."))
        }
        val entity = AttendanceEntity(
            date = date,
            status = status.label,
            note = note.trim(),
            updatedAt = System.currentTimeMillis()
        )
        dao.upsertAttendance(entity)
        return Result.success(entity)
    }

    suspend fun deleteAttendance(date: String) {
        dao.deleteAttendanceByDate(date)
    }

    // --- Study Sessions (Version 1 Preserved) ---
    suspend fun saveStudySession(
        date: String,
        durationMinutes: Int,
        durationSeconds: Int,
        targetMinutes: Int = 90,
        topic: String = "Week 1: Linux Foundations"
    ): StudySessionEntity {
        val entity = StudySessionEntity(
            date = date,
            durationMinutes = durationMinutes,
            durationSeconds = durationSeconds,
            targetMinutes = targetMinutes,
            topic = topic.ifBlank { "Week 1: Linux Foundations" },
            timestamp = System.currentTimeMillis()
        )
        val id = dao.insertStudySession(entity)
        return entity.copy(id = id)
    }

    suspend fun deleteStudySession(id: Long) {
        dao.deleteStudySession(id)
    }

    // --- Learning Summaries (Version 1 Preserved) ---
    suspend fun saveLearningSummary(
        id: Long = 0,
        date: String,
        topicsLearned: String,
        whatUnderstood: String,
        whatWasDifficult: String,
        questionsForNextSession: String,
        selfRating: Int
    ): LearningSummaryEntity {
        val clampedRating = selfRating.coerceIn(1, 5)
        val entity = LearningSummaryEntity(
            id = id,
            date = date,
            topicsLearned = topicsLearned.trim(),
            whatUnderstood = whatUnderstood.trim(),
            whatWasDifficult = whatWasDifficult.trim(),
            questionsForNextSession = questionsForNextSession.trim(),
            selfRating = clampedRating,
            updatedAt = System.currentTimeMillis()
        )
        val newId = dao.upsertLearningSummary(entity)
        return if (id == 0L) entity.copy(id = newId) else entity
    }

    suspend fun deleteLearningSummary(id: Long) {
        dao.deleteLearningSummary(id)
    }

    // --- 182-Day Lesson & Resource Tracking (Version 2) ---
    suspend fun saveLessonProgress(
        dayNumber: Int,
        isCompleted: Boolean? = null,
        notes: String? = null,
        difficultyRating: Int? = null,
        quizScore: Int? = null,
        evidenceLink: String? = null,
        scheduledDate: String? = null
    ): LessonProgressEntity {
        val existing = dao.getLessonProgressByDay(dayNumber) ?: LessonProgressEntity(dayNumber = dayNumber)
        val completed = isCompleted ?: existing.isCompleted
        val updated = existing.copy(
            isCompleted = completed,
            notes = notes?.trim() ?: existing.notes,
            difficultyRating = (difficultyRating ?: existing.difficultyRating).coerceIn(1, 5),
            quizBestScore = if (quizScore != null) maxOf(existing.quizBestScore, quizScore) else existing.quizBestScore,
            quizAttempts = if (quizScore != null) existing.quizAttempts + 1 else existing.quizAttempts,
            evidenceLink = evidenceLink?.trim() ?: existing.evidenceLink,
            scheduledDate = scheduledDate?.trim() ?: existing.scheduledDate,
            completedAt = if (completed) (existing.completedAt ?: System.currentTimeMillis()) else null,
            updatedAt = System.currentTimeMillis()
        )
        dao.upsertLessonProgress(updated)
        return updated
    }

    /**
     * Updates resource progress strictly separately from lesson completion.
     * Viewing or completing a resource NEVER automatically marks a lesson complete.
     */
    suspend fun saveResourceProgress(
        resourceId: String,
        dayNumber: Int,
        isViewed: Boolean? = null,
        isCompleted: Boolean? = null,
        personalNotes: String? = null,
        difficultyEncountered: String? = null
    ): ResourceProgressEntity {
        val existing = dao.getResourceProgressById(resourceId)
            ?: ResourceProgressEntity(resourceId = resourceId, dayNumber = dayNumber)
        val updated = existing.copy(
            isViewed = isViewed ?: existing.isViewed,
            isCompleted = isCompleted ?: existing.isCompleted,
            personalNotes = personalNotes?.trim() ?: existing.personalNotes,
            difficultyEncountered = difficultyEncountered?.trim() ?: existing.difficultyEncountered,
            updatedAt = System.currentTimeMillis()
        )
        dao.upsertResourceProgress(updated)
        return updated
    }

    // --- Projects, Notes Library, and Flashcards/Reviews ---
    suspend fun saveProject(project: ProjectEntity): Long {
        return dao.upsertProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(id: Long) {
        dao.deleteProjectById(id)
    }

    suspend fun saveLibraryNote(note: LibraryNoteEntity): Long {
        return dao.upsertLibraryNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteLibraryNote(id: Long) {
        dao.deleteLibraryNoteById(id)
    }

    suspend fun updateTopicReviewStatus(
        topicId: String,
        phaseNumber: Int,
        weekNumber: Int,
        topicTitle: String,
        status: String
    ) {
        dao.upsertTopicReview(
            TopicReviewEntity(
                topicId = topicId,
                phaseNumber = phaseNumber,
                weekNumber = weekNumber,
                topicTitle = topicTitle,
                reviewStatus = status,
                lastReviewedAt = System.currentTimeMillis()
            )
        )
    }

    // --- Safe Undo Snapshots, Reset, Merge/Replace Import & Multi-Format Exports ---
    suspend fun createUndoSnapshot(label: String, exportedAtIso: String = LocalDate.now().toString()): BackupSnapshotEntity {
        val json = exportToJson(exportedAtIso)
        val snapshot = BackupSnapshotEntity(
            label = label,
            jsonPayload = json,
            createdAt = System.currentTimeMillis()
        )
        val id = dao.insertBackupSnapshot(snapshot)
        return snapshot.copy(id = id)
    }

    suspend fun undoLastDestructiveOperation(): Result<AcademyBackupPayload> {
        val latest = dao.getLatestBackupSnapshot()
            ?: return Result.failure(IllegalStateException("No undo snapshot available."))
        val result = importFromJson(
            jsonString = latest.jsonPayload,
            mode = BackupImportMode.REPLACE,
            createPreBackup = false
        )
        if (result.isSuccess) {
            dao.deleteBackupSnapshot(latest.id)
        }
        return result
    }

    suspend fun resetAllDemoData(todayIso: String = LocalDate.now().toString()) {
        createUndoSnapshot("Auto-Backup before Reset Demo Data", todayIso)
        dao.clearAllAttendance()
        dao.clearAllStudySessions()
        dao.clearAllLearningSummaries()
        dao.clearAllLessonProgress()
        dao.clearAllResourceProgress()
        dao.clearAllLibraryNotes()
        dao.clearAllTopicReviews()
        dao.clearAllTasks()
        dao.insertTasks(DefaultAcademySeed.getInitialWeek1Tasks())
        dao.clearAllProjects()
        dao.insertAllProjects(CurriculumCatalog.getStarterProjects())
    }

    suspend fun exportToJson(exportedAtIso: String = LocalDate.now().toString()): String {
        ensureSeeded()
        val payload = AcademyBackupPayload(
            version = 2,
            exportedAt = exportedAtIso,
            tasks = dao.getTasksSnapshot(),
            attendance = dao.getAttendanceSnapshot(),
            studySessions = dao.getStudySessionsSnapshot(),
            learningSummaries = dao.getLearningSummariesSnapshot(),
            lessonProgress = dao.getLessonProgressSnapshot(),
            resourceProgress = dao.getResourceProgressSnapshot(),
            projects = dao.getProjectsSnapshot(),
            libraryNotes = dao.getLibraryNotesSnapshot(),
            topicReviews = dao.getTopicReviewsSnapshot()
        )
        return backupAdapter.toJson(payload)
    }

    fun validateBackupJson(jsonString: String): Result<AcademyBackupPayload> {
        return runCatching {
            if (jsonString.isBlank()) {
                throw IllegalArgumentException("Backup JSON is empty.")
            }
            val parsed = backupAdapter.fromJson(jsonString)
                ?: throw IllegalArgumentException("Invalid JSON backup structure.")
            if (parsed.version < 1) {
                throw IllegalArgumentException("Unsupported backup schema version: ${parsed.version}")
            }
            parsed
        }
    }

    suspend fun importFromJson(
        jsonString: String,
        mode: BackupImportMode = BackupImportMode.REPLACE,
        createPreBackup: Boolean = true
    ): Result<AcademyBackupPayload> {
        val validatedResult = validateBackupJson(jsonString)
        val parsed = validatedResult.getOrElse { return Result.failure(it) }

        return runCatching {
            if (mode == BackupImportMode.REPLACE) {
                if (createPreBackup) {
                    createUndoSnapshot("Auto-Backup before Replace Import")
                }
                dao.clearAllAttendance()
                dao.clearAllStudySessions()
                dao.clearAllLearningSummaries()
                dao.clearAllLessonProgress()
                dao.clearAllResourceProgress()
                dao.clearAllLibraryNotes()
                dao.clearAllTopicReviews()
                dao.clearAllTasks()
                dao.clearAllProjects()

                val tasksToInsert = parsed.tasks.ifEmpty { DefaultAcademySeed.getInitialWeek1Tasks() }
                val projectsToInsert = parsed.projects.ifEmpty { CurriculumCatalog.getStarterProjects() }

                dao.insertTasks(tasksToInsert)
                dao.insertAllAttendance(parsed.attendance)
                dao.insertAllStudySessions(parsed.studySessions)
                dao.insertAllLearningSummaries(parsed.learningSummaries)
                dao.insertAllLessonProgress(parsed.lessonProgress)
                dao.insertAllResourceProgress(parsed.resourceProgress)
                dao.insertAllProjects(projectsToInsert)
                dao.insertAllLibraryNotes(parsed.libraryNotes)
                dao.insertAllTopicReviews(parsed.topicReviews)
            } else {
                // MERGE mode: combine without wiping existing records
                ensureSeeded()
                val existingTasks = dao.getTasksSnapshot().associateBy { it.id }
                val mergedTasks = parsed.tasks.map { imported ->
                    val current = existingTasks[imported.id]
                    if (current != null) {
                        current.copy(
                            isCompleted = current.isCompleted || imported.isCompleted,
                            notes = if (imported.notes.isNotBlank()) imported.notes else current.notes,
                            completedAt = current.completedAt ?: imported.completedAt
                        )
                    } else {
                        imported
                    }
                }
                if (mergedTasks.isNotEmpty()) {
                    dao.insertTasks(mergedTasks)
                }
                dao.insertAllAttendance(parsed.attendance)
                dao.insertAllStudySessions(parsed.studySessions)
                dao.insertAllLearningSummaries(parsed.learningSummaries)
                dao.insertAllLessonProgress(parsed.lessonProgress)
                dao.insertAllResourceProgress(parsed.resourceProgress)
                if (parsed.projects.isNotEmpty()) {
                    dao.insertAllProjects(parsed.projects)
                }
                if (parsed.libraryNotes.isNotEmpty()) {
                    dao.insertAllLibraryNotes(parsed.libraryNotes)
                }
                if (parsed.topicReviews.isNotEmpty()) {
                    dao.insertAllTopicReviews(parsed.topicReviews)
                }
            }
            parsed
        }
    }

    suspend fun exportAttendanceAndSessionsCsv(): String {
        val attendance = dao.getAttendanceSnapshot()
        val sessions = dao.getStudySessionsSnapshot()
        return buildString {
            appendLine("record_type,date,status_or_topic,duration_minutes,notes")
            attendance.forEach { rec ->
                appendLine("ATTENDANCE,${rec.date},\"${rec.status.replace("\"", "\"\"")}\",0,\"${rec.note.replace("\"", "\"\"")}\"")
            }
            sessions.forEach { ses ->
                appendLine("STUDY_SESSION,${ses.date},\"${ses.topic.replace("\"", "\"\"")}\",${ses.durationMinutes},\"Target ${ses.targetMinutes}m\"")
            }
        }
    }

    suspend fun exportWeeklyReportMarkdown(todayIso: String = LocalDate.now().toString()): String {
        val tasks = dao.getTasksSnapshot()
        val lessons = dao.getLessonProgressSnapshot()
        val attendance = dao.getAttendanceSnapshot()
        val sessions = dao.getStudySessionsSnapshot()
        val summaries = dao.getLearningSummariesSnapshot()

        val completedTasks = tasks.count { it.isCompleted }
        val completedLessons = lessons.count { it.isCompleted }
        val totalMinutes = sessions.sumOf { it.durationMinutes }

        return buildString {
            appendLine("# Waqas Cyber Academy 2.0 — Printable Progress & Learning Report")
            appendLine("- **Student:** Waqas Ahmad (BS Computer Engineering, 5th Semester)")
            appendLine("- **Environment:** Windows 11 • Kali Linux in VirtualBox")
            appendLine("- **Generated Date:** $todayIso")
            appendLine()
            appendLine("## 1. Overall Metrics")
            appendLine("- **Week 1 Core Tasks Completed:** $completedTasks / ${tasks.size}")
            appendLine("- **26-Week Roadmap Lessons Completed:** $completedLessons / 182 days")
            appendLine("- **Total Study Time Logged:** $totalMinutes minutes (${sessions.size} sessions)")
            appendLine("- **Attendance Records Logged:** ${attendance.size} days")
            appendLine()
            appendLine("## 2. Attendance History")
            if (attendance.isEmpty()) {
                appendLine("_No attendance records logged yet._")
            } else {
                attendance.forEach { rec ->
                    appendLine("- **${rec.date}**: ${rec.status} ${if (rec.note.isNotBlank()) "— ${rec.note}" else ""}")
                }
            }
            appendLine()
            appendLine("## 3. Daily Learning Summaries")
            if (summaries.isEmpty()) {
                appendLine("_No learning summaries logged yet._")
            } else {
                summaries.forEach { sum ->
                    appendLine("### ${sum.date} (Self-Rating: ${sum.selfRating}/5)")
                    appendLine("- **Topics Learned:** ${sum.topicsLearned}")
                    appendLine("- **What I Understood:** ${sum.whatUnderstood}")
                    appendLine("- **What Was Difficult:** ${sum.whatWasDifficult}")
                    appendLine("- **Next Session Questions:** ${sum.questionsForNextSession}")
                    appendLine()
                }
            }
        }
    }
}
