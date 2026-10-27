package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class AttendanceStatus(val label: String) {
    PRESENT("Present"),
    PARTIAL("Partial"),
    ABSENT("Absent"),
    REST_DAY("Rest Day");

    companion object {
        fun fromLabel(label: String): AttendanceStatus {
            return entries.firstOrNull {
                it.label.equals(label, ignoreCase = true) || it.name.equals(label, ignoreCase = true)
            } ?: PRESENT
        }
    }
}

enum class BackupImportMode(val label: String, val description: String) {
    MERGE(
        "Merge (Safe Combine)",
        "Combines imported records with your current data without deleting existing progress."
    ),
    REPLACE(
        "Replace (Full Overwrite)",
        "Creates an automatic Undo snapshot first, then replaces current records with the backup file."
    )
}

// --- Version 1 Core Entities (Preserved Exactly for Non-Destructive Migration) ---

@JsonClass(generateAdapter = true)
@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey val id: Int,
    val week: Int = 1,
    val title: String,
    val category: String,
    val estimatedMinutes: Int,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val commandHint: String = "",
    val completedAt: Long? = null,
    val priority: String = "Medium", // High, Medium, Low
    val dueDate: String = "" // Optional YYYY-MM-DD for overdue & rescheduling
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "attendance_records")
data class AttendanceEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val status: String, // Present, Partial, Absent, Rest Day
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val durationMinutes: Int,
    val durationSeconds: Int,
    val targetMinutes: Int = 90,
    val topic: String = "Week 1: Linux Foundations",
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "learning_summaries")
data class LearningSummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val topicsLearned: String,
    val whatUnderstood: String,
    val whatWasDifficult: String,
    val questionsForNextSession: String,
    val selfRating: Int, // 1 to 5
    val updatedAt: Long = System.currentTimeMillis()
)

// --- Version 2.0 & 3.0 Entities (Authentication, RBAC, 182-Day Curriculum, Resources, Projects, Notes, Bookmarks, Quizzes, CMS, Preferences) ---

@Entity(
    tableName = "user_accounts",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val displayName: String,
    val passwordHash: String, // PBKDF2WithHmacSHA256 Base64
    val passwordSalt: String, // 16-byte SecureRandom Base64
    val isActiveSession: Boolean = false,
    val role: String = "student", // "student" or "admin"
    val emailVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

data class AuthenticatedUser(
    val id: Long,
    val email: String,
    val displayName: String,
    val lastLoginAt: Long,
    val role: String = "student",
    val emailVerified: Boolean = true
) {
    val isAdmin: Boolean
        get() = role.equals("admin", ignoreCase = true)
}

@JsonClass(generateAdapter = true)
@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val dayNumber: Int, // 1..182
    val isCompleted: Boolean = false,
    val notes: String = "",
    val difficultyRating: Int = 3, // 1..5
    val quizBestScore: Int = 0, // 0..5
    val quizAttempts: Int = 0,
    val evidenceLink: String = "",
    val scheduledDate: String = "", // Optional YYYY-MM-DD rescheduled/assigned calendar date
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "resource_progress")
data class ResourceProgressEntity(
    @PrimaryKey val resourceId: String, // e.g., "res_day_1"
    val dayNumber: Int,
    val isViewed: Boolean = false,
    val isCompleted: Boolean = false,
    val personalNotes: String = "",
    val difficultyEncountered: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val difficulty: String,
    val status: String, // "Planned", "In Progress", "Completed"
    val objectives: String,
    val requiredTools: String,
    val tasksMilestones: String,
    val notes: String = "",
    val evidenceLinks: String = "",
    val githubUrl: String = "",
    val completionDate: String = "",
    val weekAssociation: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "library_notes")
data class LibraryNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "Linux & CLI",
    val associatedDay: Int? = null,
    val associatedWeek: Int? = 1,
    val associatedProjectId: Long? = null,
    val tags: String = "",
    val userId: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "topic_reviews")
data class TopicReviewEntity(
    @PrimaryKey val topicId: String, // e.g., "fc_linux_1" or "day_1"
    val phaseNumber: Int,
    val weekNumber: Int,
    val topicTitle: String,
    val reviewStatus: String = "Needs Review", // "Needs Review", "Reviewing", "Mastered"
    val lastReviewedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 0,
    val itemType: String, // "LESSON", "RESOURCE", "PROJECT"
    val itemKey: String, // e.g., "day_1", "res_day_1", "proj_1"
    val title: String,
    val subtitle: String,
    val tags: String = "cybersecurity",
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 0,
    val dayNumber: Int,
    val quizTitle: String,
    val score: Int,
    val totalQuestions: Int = 5,
    val percentage: Int = 0,
    val weakTopicSummary: String = "",
    val attemptedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "custom_cms_lessons")
data class CustomCourseLessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekNumber: Int = 1,
    val phaseNumber: Int = 1,
    val title: String,
    val summary: String,
    val difficulty: String = "Beginner",
    val estimatedMinutes: Int = 90,
    val publicationStatus: String = "Published", // "Published", "Draft", "Archived"
    val authorEmail: String = "admin@cyberacademy.local",
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val displayName: String = "Waqas Ahmad",
    val learningGoal: String = "Learn cybersecurity from beginner to professional foundations & build a GitHub portfolio",
    val dailyStudyGoalMinutes: Int = 90,
    val timeZoneId: String = "UTC+05:00 (PKT)",
    val darkThemeEnabled: Boolean = true,
    val reducedMotionEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "backup_snapshots")
data class BackupSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val jsonPayload: String,
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class AcademyBackupPayload(
    val version: Int = 2,
    val academyName: String = "Waqas Cybersecurity Academy Test Dashboard",
    val studentName: String = "Waqas Ahmad",
    val exportedAt: String,
    val tasks: List<DailyTaskEntity> = emptyList(),
    val attendance: List<AttendanceEntity> = emptyList(),
    val studySessions: List<StudySessionEntity> = emptyList(),
    val learningSummaries: List<LearningSummaryEntity> = emptyList(),
    val lessonProgress: List<LessonProgressEntity> = emptyList(),
    val resourceProgress: List<ResourceProgressEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val libraryNotes: List<LibraryNoteEntity> = emptyList(),
    val topicReviews: List<TopicReviewEntity> = emptyList(),
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val quizAttempts: List<QuizAttemptEntity> = emptyList(),
    val customLessons: List<CustomCourseLessonEntity> = emptyList()
)

data class StudentProfile(
    val name: String = "Waqas Ahmad",
    val education: String = "BS Computer Engineering",
    val semester: String = "5th Semester",
    val goal: String = "Learn cybersecurity from beginner to professional foundations & build a GitHub portfolio",
    val careerInterests: String = "Cybersecurity, SOC Analysis, Ethical Penetration Testing, Cloud Security & Automation",
    val dailyStudyMinutesGoal: Int = 90,
    val environment: String = "Windows 11, Kali Linux in VirtualBox",
    val budgetPreference: String = "Prefer free resources",
    val currentWeek: String = "26-Week Roadmap (182 Days)",
    val currentPhase: String = "Phase 1–7: Complete Foundations to Capstone"
)

object DefaultAcademySeed {
    val profile = StudentProfile()

    fun getInitialWeek1Tasks(): List<DailyTaskEntity> = listOf(
        DailyTaskEntity(
            id = 1,
            week = 1,
            title = "Learn basic Linux commands.",
            category = "Core Concepts",
            estimatedMinutes = 20,
            isCompleted = false,
            notes = "",
            commandHint = "whoami, hostname, uname -a, man ls, --help"
        ),
        DailyTaskEntity(
            id = 2,
            week = 1,
            title = "Practise pwd, ls, cd, and mkdir.",
            category = "Directory Navigation",
            estimatedMinutes = 20,
            isCompleted = false,
            notes = "",
            commandHint = "pwd && ls -la && mkdir -p ~/kali_labs/week1 && cd ~/kali_labs/week1"
        ),
        DailyTaskEntity(
            id = 3,
            week = 1,
            title = "Practise cp, mv, and rm safely.",
            category = "Safe File Management",
            estimatedMinutes = 20,
            isCompleted = false,
            notes = "",
            commandHint = "cp -i notes.txt backup.txt && mv -i backup.txt archive/ && rm -i temp.txt"
        ),
        DailyTaskEntity(
            id = 4,
            week = 1,
            title = "Learn chmod and understand ls -l.",
            category = "Permissions & Security",
            estimatedMinutes = 15,
            isCompleted = false,
            notes = "",
            commandHint = "ls -l deploy.sh && chmod 750 deploy.sh && chmod u+x,g+r,o-rwx deploy.sh"
        ),
        DailyTaskEntity(
            id = 5,
            week = 1,
            title = "Complete a short Linux quiz.",
            category = "Knowledge Verification",
            estimatedMinutes = 15,
            isCompleted = false,
            notes = "",
            commandHint = "Verify CLI flags, relative vs absolute paths, and rwx permission bits"
        )
    )
}
