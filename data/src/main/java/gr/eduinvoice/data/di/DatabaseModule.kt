package gr.eduinvoice.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.data.database.DatabaseConstants
import gr.eduinvoice.data.database.DatabaseInitException
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.database.LegacyMigration
import gr.eduinvoice.data.repository.BackupRepository
import gr.eduinvoice.data.user.UserPreferencesRepository
import net.sqlcipher.database.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import android.database.sqlite.SQLiteException
import android.util.Log
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEduInvoiceDatabase(
        @ApplicationContext context: Context,
        prefs: UserPreferencesRepository
    ): EduInvoiceDatabase {
        val pass = runBlocking(Dispatchers.IO) { prefs.getDbPassphrase() }
        require(pass.isNotEmpty()) { "Database passphrase unavailable" }
        val passphrase = SQLiteDatabase.getBytes(pass.toCharArray())
        return try {
            val db = EduInvoiceDatabase.getDatabase(context, passphrase)
            // Force open to catch corruption immediately
            db.openHelper.writableDatabase
            db
        } catch (e: SQLiteException) {
            Log.e("DatabaseModule", "Database open failed, attempting recovery", e)
            val dbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
            return try {
                val legacyJson = LegacyMigration.migrateIfNeeded(context)
                if (!dbFile.delete()) {
                    Log.e(
                        "DatabaseModule",
                        "Failed to delete corrupt DB at ${dbFile.absolutePath}"
                    )
                    throw DatabaseInitException("Unable to delete corrupt database", e)
                }
                val db = EduInvoiceDatabase.getDatabase(context, passphrase)
                db.openHelper.writableDatabase
                legacyJson?.let {
                    val repo = BackupRepository(db)
                    runBlocking(Dispatchers.IO) { repo.restoreFromJson(it) }
                }
                db
            } catch (recovery: Exception) {
                Log.e("DatabaseModule", "Database recovery failed", recovery)
                throw DatabaseInitException("Database recovery failed", recovery)
            }
        }
    }

    // DatabaseModule only provides the Room database instance.
}
