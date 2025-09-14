package gr.eduinvoice.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import androidx.room.Room
import gr.eduinvoice.data.repository.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object LegacyMigration {
    fun migrateIfNeeded(context: Context): String? {
        val file = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
        if (!file.exists()) return null
        return try {
            SQLiteDatabase.openDatabase(
                file.path,
                null,
                SQLiteDatabase.OPEN_READONLY
            ).use { }
            // Use the current database configuration (re-baselined to version 1, no migrations during development)
            val db = Room.databaseBuilder(
                context,
                EduInvoiceDatabase::class.java,
                DatabaseConstants.DATABASE_NAME
            )
                .fallbackToDestructiveMigration(true)
                .build()
            val repo = BackupRepository(context, db)
            val json = runBlocking(Dispatchers.IO) { repo.exportJson() }
            db.close()
            json
        } catch (_: SQLiteException) {
            null
        }
    }
}
