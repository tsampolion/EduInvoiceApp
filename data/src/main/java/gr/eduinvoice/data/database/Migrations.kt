package gr.eduinvoice.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE students ADD COLUMN ownerId INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE student_groups ADD COLUMN ownerId INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE lessons ADD COLUMN ownerId INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE group_student_cross_ref ADD COLUMN ownerId INTEGER NOT NULL DEFAULT 0")
    }
}

// Fix the migration from 13 to 15 to properly handle the lastModified column
val MIGRATION_13_15 = object : Migration(13, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Check if lastModified column already exists to avoid conflicts
        val cursor = db.query("PRAGMA table_info(students)")
        val columnExists = cursor.use {
            var exists = false
            while (it.moveToNext()) {
                val columnName = it.getString(1) // column name is at index 1
                if (columnName == "lastModified") {
                    exists = true
                    break
                }
            }
            exists
        }

        if (!columnExists) {
            // Add lastModified column to students table
            db.execSQL("ALTER TABLE students ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")

            // Update existing records with current timestamp
            val currentTime = System.currentTimeMillis()
            db.execSQL("UPDATE students SET lastModified = $currentTime WHERE lastModified = 0 OR lastModified IS NULL")
        }

        // Check if lastModified column exists in lessons table
        val lessonsCursor = db.query("PRAGMA table_info(lessons)")
        val lessonsColumnExists = lessonsCursor.use {
            var exists = false
            while (it.moveToNext()) {
                val columnName = it.getString(1)
                if (columnName == "lastModified") {
                    exists = true
                    break
                }
            }
            exists
        }

        if (!lessonsColumnExists) {
            // Add lastModified column to lessons table
            db.execSQL("ALTER TABLE lessons ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")

            // Update existing records with current timestamp
            val currentTime = System.currentTimeMillis()
            db.execSQL("UPDATE lessons SET lastModified = $currentTime WHERE lastModified = 0 OR lastModified IS NULL")
        }
    }
}

// Migration from 14 to 15 for safety
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // This migration is already handled by auto-migration, but adding explicit migration for safety
        // Check if lastModified columns exist and add them if they don't

        // Check students table
        val studentsCursor = db.query("PRAGMA table_info(students)")
        val studentsColumnExists = studentsCursor.use {
            var exists = false
            while (it.moveToNext()) {
                val columnName = it.getString(1)
                if (columnName == "lastModified") {
                    exists = true
                    break
                }
            }
            exists
        }

        if (!studentsColumnExists) {
            db.execSQL("ALTER TABLE students ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
            val currentTime = System.currentTimeMillis()
            db.execSQL("UPDATE students SET lastModified = $currentTime WHERE lastModified = 0 OR lastModified IS NULL")
        }

        // Check lessons table
        val lessonsCursor = db.query("PRAGMA table_info(lessons)")
        val lessonsColumnExists = lessonsCursor.use {
            var exists = false
            while (it.moveToNext()) {
                val columnName = it.getString(1)
                if (columnName == "lastModified") {
                    exists = true
                    break
                }
            }
            exists
        }

        if (!lessonsColumnExists) {
            db.execSQL("ALTER TABLE lessons ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
            val currentTime = System.currentTimeMillis()
            db.execSQL("UPDATE lessons SET lastModified = $currentTime WHERE lastModified = 0 OR lastModified IS NULL")
        }
    }
}

// Add group fields: className, rate, rateType
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DatabaseConstants.GROUPS_TABLE} ADD COLUMN className TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE ${DatabaseConstants.GROUPS_TABLE} ADD COLUMN rate REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE ${DatabaseConstants.GROUPS_TABLE} ADD COLUMN rateType TEXT NOT NULL DEFAULT 'hourly'")
    }
}

// Add isActive to groups for archiving
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DatabaseConstants.GROUPS_TABLE} ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
    }
}

// Add group lesson master and absences tables
val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS group_lesson_master (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "ownerId INTEGER NOT NULL DEFAULT 0, " +
                "groupId INTEGER NOT NULL, " +
                "date TEXT NOT NULL, " +
                "startTime TEXT NOT NULL, " +
                "durationMinutes INTEGER NOT NULL, " +
                "notes TEXT, " +
                "lastModified INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_group_lesson_master_groupId ON group_lesson_master(groupId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_group_lesson_master_date ON group_lesson_master(date)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS group_lesson_absences (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "ownerId INTEGER NOT NULL DEFAULT 0, " +
                "groupLessonId INTEGER NOT NULL, " +
                "studentId INTEGER NOT NULL, " +
                "FOREIGN KEY(groupLessonId) REFERENCES group_lesson_master(id) ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_group_lesson_absences_groupLessonId ON group_lesson_absences(groupLessonId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_group_lesson_absences_studentId ON group_lesson_absences(studentId)")
    }
}

// Add masterId to lessons for robust linkage to group lesson master
val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE lessons ADD COLUMN masterId INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_lessons_masterId ON lessons(masterId)")
    }
}