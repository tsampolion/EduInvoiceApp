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
