package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademyDao {
    // --- Daily Tasks (Version 1 Preserved) ---
    @Query("SELECT * FROM daily_tasks ORDER BY id ASC")
    fun observeTasks(): Flow<List<DailyTaskEntity>>

    @Query("SELECT * FROM daily_tasks ORDER BY id ASC")
    suspend fun getTasksSnapshot(): List<DailyTaskEntity>

    @Query("SELECT COUNT(*) FROM daily_tasks")
    suspend fun getTaskCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyTaskEntity>)

    @Update
    suspend fun updateTask(task: DailyTaskEntity)

    @Query("DELETE FROM daily_tasks")
    suspend fun clearAllTasks()

    // --- Attendance (Version 1 Preserved) ---
    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    fun observeAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    suspend fun getAttendanceSnapshot(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE date = :date LIMIT 1")
    suspend fun getAttendanceByDate(date: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttendance(record: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(records: List<AttendanceEntity>)

    @Query("DELETE FROM attendance_records WHERE date = :date")
    suspend fun deleteAttendanceByDate(date: String)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAllAttendance()

    // --- Study Sessions (Version 1 Preserved) ---
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun observeStudySessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    suspend fun getStudySessionsSnapshot(): List<StudySessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudySessions(sessions: List<StudySessionEntity>)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteStudySession(id: Long)

    @Query("DELETE FROM study_sessions")
    suspend fun clearAllStudySessions()

    // --- Learning Summaries (Version 1 Preserved) ---
    @Query("SELECT * FROM learning_summaries ORDER BY date DESC, updatedAt DESC")
    fun observeLearningSummaries(): Flow<List<LearningSummaryEntity>>

    @Query("SELECT * FROM learning_summaries ORDER BY date DESC, updatedAt DESC")
    suspend fun getLearningSummariesSnapshot(): List<LearningSummaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLearningSummary(summary: LearningSummaryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLearningSummaries(summaries: List<LearningSummaryEntity>)

    @Query("DELETE FROM learning_summaries WHERE id = :id")
    suspend fun deleteLearningSummary(id: Long)

    @Query("DELETE FROM learning_summaries")
    suspend fun clearAllLearningSummaries()

    // --- User Accounts & Secure Local Authentication (Version 2) ---
    @Query("SELECT * FROM user_accounts WHERE isActiveSession = 1 LIMIT 1")
    fun observeActiveUserAccount(): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun observeAllUserAccounts(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserAccountByEmail(email: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUserAccount(account: UserAccountEntity): Long

    @Query("UPDATE user_accounts SET isActiveSession = 0")
    suspend fun clearActiveUserSessions()

    @Query("UPDATE user_accounts SET isActiveSession = 1, lastLoginAt = :loginTime WHERE id = :userId")
    suspend fun setActiveUserSession(userId: Long, loginTime: Long = System.currentTimeMillis())

    // --- 182-Day Lesson Progress (Version 2) ---
    @Query("SELECT * FROM lesson_progress ORDER BY dayNumber ASC")
    fun observeLessonProgress(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress ORDER BY dayNumber ASC")
    suspend fun getLessonProgressSnapshot(): List<LessonProgressEntity>

    @Query("SELECT * FROM lesson_progress WHERE dayNumber = :dayNumber LIMIT 1")
    suspend fun getLessonProgressByDay(dayNumber: Int): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessonProgress(progress: LessonProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLessonProgress(list: List<LessonProgressEntity>)

    @Query("DELETE FROM lesson_progress")
    suspend fun clearAllLessonProgress()

    // --- Resource Progress (Version 2) ---
    @Query("SELECT * FROM resource_progress ORDER BY dayNumber ASC")
    fun observeResourceProgress(): Flow<List<ResourceProgressEntity>>

    @Query("SELECT * FROM resource_progress ORDER BY dayNumber ASC")
    suspend fun getResourceProgressSnapshot(): List<ResourceProgressEntity>

    @Query("SELECT * FROM resource_progress WHERE resourceId = :resourceId LIMIT 1")
    suspend fun getResourceProgressById(resourceId: String): ResourceProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResourceProgress(progress: ResourceProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllResourceProgress(list: List<ResourceProgressEntity>)

    @Query("DELETE FROM resource_progress")
    suspend fun clearAllResourceProgress()

    // --- Portfolio Projects (Version 2) ---
    @Query("SELECT * FROM projects ORDER BY weekAssociation ASC, id ASC")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY weekAssociation ASC, id ASC")
    suspend fun getProjectsSnapshot(): List<ProjectEntity>

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProject(project: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProjects(projects: List<ProjectEntity>)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("DELETE FROM projects")
    suspend fun clearAllProjects()

    // --- Searchable Notes Library (Version 2) ---
    @Query("SELECT * FROM library_notes ORDER BY updatedAt DESC")
    fun observeLibraryNotes(): Flow<List<LibraryNoteEntity>>

    @Query("SELECT * FROM library_notes ORDER BY updatedAt DESC")
    suspend fun getLibraryNotesSnapshot(): List<LibraryNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLibraryNote(note: LibraryNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLibraryNotes(notes: List<LibraryNoteEntity>)

    @Query("DELETE FROM library_notes WHERE id = :id")
    suspend fun deleteLibraryNoteById(id: Long)

    @Query("DELETE FROM library_notes")
    suspend fun clearAllLibraryNotes()

    // --- Topic Reviews & Flashcards Status (Version 2) ---
    @Query("SELECT * FROM topic_reviews ORDER BY weekNumber ASC")
    fun observeTopicReviews(): Flow<List<TopicReviewEntity>>

    @Query("SELECT * FROM topic_reviews ORDER BY weekNumber ASC")
    suspend fun getTopicReviewsSnapshot(): List<TopicReviewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTopicReview(review: TopicReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTopicReviews(reviews: List<TopicReviewEntity>)

    @Query("DELETE FROM topic_reviews")
    suspend fun clearAllTopicReviews()

    // --- Undo Backup Snapshots (Version 2) ---
    @Query("SELECT * FROM backup_snapshots ORDER BY createdAt DESC LIMIT 1")
    fun observeLatestBackupSnapshot(): Flow<BackupSnapshotEntity?>

    @Query("SELECT * FROM backup_snapshots ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestBackupSnapshot(): BackupSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupSnapshot(snapshot: BackupSnapshotEntity): Long

    @Query("DELETE FROM backup_snapshots WHERE id = :id")
    suspend fun deleteBackupSnapshot(id: Long)
}
