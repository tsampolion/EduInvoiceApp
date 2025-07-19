package gr.tsambala.tutorbilling.data.database

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
