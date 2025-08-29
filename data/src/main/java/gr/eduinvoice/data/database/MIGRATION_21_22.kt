package gr.eduinvoice.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add role column to users table
        database.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'TEACHER'")
        
        // Update existing admin user to have ADMIN role
        database.execSQL("UPDATE users SET role = 'ADMIN' WHERE username = 'admin'")
        
        // Update all other existing users to have TEACHER role (if not already set)
        database.execSQL("UPDATE users SET role = 'TEACHER' WHERE role IS NULL OR role = ''")
        
        // Create index on role column for better performance
        database.execSQL("CREATE INDEX IF NOT EXISTS index_users_role ON users(role)")
    }
}
