package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DailyTaskEntity::class,
        AttendanceEntity::class,
        StudySessionEntity::class,
        LearningSummaryEntity::class,
        UserAccountEntity::class,
        LessonProgressEntity::class,
        ResourceProgressEntity::class,
        ProjectEntity::class,
        LibraryNoteEntity::class,
        TopicReviewEntity::class,
        BackupSnapshotEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AcademyDatabase : RoomDatabase() {
    abstract fun academyDao(): AcademyDao

    companion object {
        @Volatile
        private var INSTANCE: AcademyDatabase? = null

        /**
         * Non-destructive, reversible schema upgrade from Version 1 to Version 2.0.
         * Preserves 100% of existing rows in daily_tasks, attendance_records,
         * study_sessions, and learning_summaries while creating the Version 2 tables.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_accounts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `email` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `passwordHash` TEXT NOT NULL,
                        `passwordSalt` TEXT NOT NULL,
                        `isActiveSession` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `lastLoginAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_user_accounts_email` ON `user_accounts` (`email`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `lesson_progress` (
                        `dayNumber` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `difficultyRating` INTEGER NOT NULL,
                        `quizBestScore` INTEGER NOT NULL,
                        `quizAttempts` INTEGER NOT NULL,
                        `evidenceLink` TEXT NOT NULL,
                        `scheduledDate` TEXT NOT NULL,
                        `completedAt` INTEGER,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`dayNumber`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `resource_progress` (
                        `resourceId` TEXT NOT NULL,
                        `dayNumber` INTEGER NOT NULL,
                        `isViewed` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `personalNotes` TEXT NOT NULL,
                        `difficultyEncountered` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`resourceId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `projects` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `difficulty` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `objectives` TEXT NOT NULL,
                        `requiredTools` TEXT NOT NULL,
                        `tasksMilestones` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `evidenceLinks` TEXT NOT NULL,
                        `githubUrl` TEXT NOT NULL,
                        `completionDate` TEXT NOT NULL,
                        `weekAssociation` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `library_notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `associatedDay` INTEGER,
                        `associatedWeek` INTEGER,
                        `associatedProjectId` INTEGER,
                        `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `topic_reviews` (
                        `topicId` TEXT NOT NULL,
                        `phaseNumber` INTEGER NOT NULL,
                        `weekNumber` INTEGER NOT NULL,
                        `topicTitle` TEXT NOT NULL,
                        `reviewStatus` TEXT NOT NULL,
                        `lastReviewedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`topicId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `backup_snapshots` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `label` TEXT NOT NULL,
                        `jsonPayload` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AcademyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AcademyDatabase::class.java,
                    "waqas_cyber_academy_v1.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun createInMemory(context: Context): AcademyDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AcademyDatabase::class.java
            )
                .addMigrations(MIGRATION_1_2)
                .allowMainThreadQueries()
                .build()
        }
    }
}
