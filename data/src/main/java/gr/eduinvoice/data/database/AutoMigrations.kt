package gr.eduinvoice.data.database

import androidx.room.migration.AutoMigrationSpec
import androidx.room.RenameColumn
import androidx.room.DeleteColumn
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Custom auto-migration from version 5 to 6.
 * Handles recreating the lessons table to enforce
 * foreign key constraints and string columns for date and time.
 */
@RenameColumn(
    tableName = "students",
    fromColumnName = "hourlyRate",
    toColumnName = "rate"
)
@DeleteColumn(
    tableName = "students",
    columnName = "perLessonRate"
)
@DeleteColumn(
    tableName = "students",
    columnName = "createdAt"
)
@DeleteColumn(
    tableName = "students",
    columnName = "updatedAt"
)
@DeleteColumn(
    tableName = "lessons",
    columnName = "createdAt"
)
@DeleteColumn(
    tableName = "lessons",
    columnName = "updatedAt"
)
class AutoMigration5To6 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE students SET rate = 0.0 WHERE rate IS NULL")
        db.execSQL(
            """
            CREATE TABLE lessons_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                studentId INTEGER NOT NULL,
                date TEXT NOT NULL,
                startTime TEXT NOT NULL,
                durationMinutes INTEGER NOT NULL,
                notes TEXT,
                isPaid INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(studentId) REFERENCES students(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO lessons_new (id, studentId, date, startTime, durationMinutes, notes, isPaid)
            SELECT id, studentId, date, startTime, durationMinutes, notes, isPaid FROM lessons
            """.trimIndent()
        )
        db.execSQL("DROP TABLE lessons")
        db.execSQL("ALTER TABLE lessons_new RENAME TO lessons")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_lessons_date ON lessons(date)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_lessons_studentId ON lessons(studentId)")
    }
}

/**
 * Custom auto-migration from version 12 to 13.
 * Adds ownerId column to all tables for multi-user support.
 */
class AutoMigration12To13 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE students ADD COLUMN ownerId INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE student_groups ADD COLUMN ownerId INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE lessons ADD COLUMN ownerId INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE group_student_cross_ref ADD COLUMN ownerId INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Custom auto-migration from version 15 to 16.
 * Adds group fields: className, rate, rateType.
 */
class AutoMigration15To16 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE student_groups ADD COLUMN className TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE student_groups ADD COLUMN rate REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE student_groups ADD COLUMN rateType TEXT NOT NULL DEFAULT 'hourly'")
    }
}

/**
 * Custom auto-migration from version 16 to 17.
 * Adds isActive to groups for archiving.
 */
class AutoMigration16To17 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE student_groups ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
    }
}

/**
 * Custom auto-migration from version 17 to 18.
 * Creates group lesson master and absences tables.
 */
class AutoMigration17To18 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
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

/**
 * Custom auto-migration from version 18 to 19.
 * Adds masterId to lessons for robust linkage to group lesson master.
 */
class AutoMigration18To19 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE lessons ADD COLUMN masterId INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_lessons_masterId ON lessons(masterId)")
    }
}

/**
 * Custom auto-migration from version 19 to 20.
 * Creates invoice_master table for batch invoice runs.
 */
class AutoMigration19To20 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS invoice_master (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "ownerId INTEGER NOT NULL DEFAULT 0, " +
                "studentId INTEGER NOT NULL, " +
                "invoiceNumber TEXT NOT NULL, " +
                "invoiceDate TEXT NOT NULL, " +
                "notes TEXT, " +
                "isArchived INTEGER NOT NULL DEFAULT 0, " +
                "lastModified INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_invoice_master_studentId ON invoice_master(studentId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_invoice_master_invoiceDate ON invoice_master(invoiceDate)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_invoice_master_invoiceNumber ON invoice_master(invoiceNumber)")
    }
}

/**
 * Custom auto-migration from version 20 to 21.
 * Consolidated migration for payment batches, reschedules, and lesson links.
 */
class AutoMigration20To21 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        // lessons: add invoiceMasterId, paymentBatchId
        db.execSQL("ALTER TABLE lessons ADD COLUMN invoiceMasterId INTEGER")
        db.execSQL("ALTER TABLE lessons ADD COLUMN paymentBatchId INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_lessons_invoiceMasterId ON lessons(invoiceMasterId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_lessons_paymentBatchId ON lessons(paymentBatchId)")

        // payment_batch_master
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS payment_batch_master (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "ownerId INTEGER NOT NULL DEFAULT 0, " +
                "studentId INTEGER, " +
                "batchDate TEXT NOT NULL, " +
                "notes TEXT, " +
                "isArchived INTEGER NOT NULL DEFAULT 0, " +
                "lastModified INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_batch_master_studentId ON payment_batch_master(studentId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_batch_master_batchDate ON payment_batch_master(batchDate)")

        // reschedule_master
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS reschedule_master (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "ownerId INTEGER NOT NULL DEFAULT 0, " +
                "title TEXT, " +
                "newDate TEXT NOT NULL, " +
                "newStartTime TEXT NOT NULL, " +
                "newDurationMinutes INTEGER NOT NULL, " +
                "notes TEXT, " +
                "lastModified INTEGER NOT NULL DEFAULT 0)"
        )

        // reschedule_master_lessons junction
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS reschedule_master_lessons (" +
                "masterId INTEGER NOT NULL, " +
                "lessonId INTEGER NOT NULL, " +
                "PRIMARY KEY(masterId, lessonId))"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reschedule_master_lessons_masterId ON reschedule_master_lessons(masterId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reschedule_master_lessons_lessonId ON reschedule_master_lessons(lessonId)")
    }
}

/**
 * Custom auto-migration from version 21 to 22.
 * Adds role column to users table for role-based access control.
 */
class AutoMigration21To22 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        // Add role column to users table
        db.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'TEACHER'")

        // Update existing admin user to have ADMIN role
        db.execSQL("UPDATE users SET role = 'ADMIN' WHERE username = 'admin'")

        // Update all other existing users to have TEACHER role (if not already set)
        db.execSQL("UPDATE users SET role = 'TEACHER' WHERE role IS NULL OR role = ''")

        // Create index on role column for better performance
        db.execSQL("CREATE INDEX IF NOT EXISTS index_users_role ON users(role)")
    }
}
