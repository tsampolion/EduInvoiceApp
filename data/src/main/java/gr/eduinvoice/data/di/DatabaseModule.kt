package gr.eduinvoice.data.di

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.data.BuildConfig
import gr.eduinvoice.data.database.DatabaseConstants
import gr.eduinvoice.data.database.DatabaseInitException
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.database.LegacyMigration
import gr.eduinvoice.data.fallback.DatabaseFallbackManager
import gr.eduinvoice.data.monitoring.DatabaseHealthMonitor
import gr.eduinvoice.data.repository.BackupRepository
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.data.validation.DatabaseIntegrityValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SQLiteDatabase
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
        SQLiteDatabase.loadLibs(context)
        
        // Get passphrase with better error handling
        val pass = try {
            runBlocking(Dispatchers.IO) { prefs.getDbPassphrase() }
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Failed to get passphrase", e)
            if (BuildConfig.DEBUG) {
                // In debug mode, use a fallback passphrase
                Log.w("DatabaseModule", "Using fallback passphrase for debug mode")
                "debug_passphrase_123"
            } else {
                throw DatabaseInitException("Failed to get database passphrase", e)
            }
        }
        
        require(pass.isNotBlank()) { "Database passphrase unavailable" }
        val passphrase = SQLiteDatabase.getBytes(pass.toCharArray())
        require(passphrase.isNotEmpty() && passphrase.any { it != 0.toByte() }) {
            "Invalid database passphrase"
        }
        Log.d("DatabaseModule", "Passphrase length: ${passphrase.size}")

        fun openDatabase(): EduInvoiceDatabase {
            val db = EduInvoiceDatabase.getDatabase(context, passphrase.copyOf())
            // Force open to catch corruption immediately
            db.openHelper.writableDatabase
            return db
        }

        val db = try {
            openDatabase()
        } catch (e: SQLiteException) {
            Log.e("DatabaseModule", "Database open failed, attempting recovery", e)
            val dbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
            try {
                // Always attempt recovery in both debug and release modes
                val legacyJson = LegacyMigration.migrateIfNeeded(context)
                if (dbFile.exists() && !dbFile.delete()) {
                    Log.e(
                        "DatabaseModule",
                        "Failed to delete corrupt DB at ${dbFile.absolutePath}"
                    )
                    throw DatabaseInitException("Unable to delete corrupt database", e)
                }
                val recovered = openDatabase()
                legacyJson?.let {
                    val repo = BackupRepository(context, recovered)
                    runBlocking(Dispatchers.IO) { repo.restoreFromJson(it) }
                }
                Log.i("DatabaseModule", "Database recovery successful")
                recovered
            } catch (recovery: Exception) {
                Log.e("DatabaseModule", "Database recovery failed", recovery)
                if (BuildConfig.DEBUG) {
                    // In debug mode, try one more time with a fresh database
                    Log.w("DatabaseModule", "Attempting fresh database creation in debug mode")
                    try {
                        val freshDb = openDatabase()
                        Log.i("DatabaseModule", "Fresh database creation successful")
                        freshDb
                    } catch (finalException: Exception) {
                        Log.e("DatabaseModule", "Final database creation attempt failed", finalException)
                        throw DatabaseInitException("Database recovery failed", finalException)
                    }
                } else {
                    throw DatabaseInitException("Database recovery failed", recovery)
                }
            }
        } finally {
            passphrase.fill(0)
        }
        return db
    }

    // DatabaseModule only provides the Room database instance.
    
    @Provides
    @Singleton
    fun provideDatabaseHealthMonitor(
        @ApplicationContext context: Context,
        database: EduInvoiceDatabase
    ): DatabaseHealthMonitor {
        return DatabaseHealthMonitor(context, database)
    }
    
    @Provides
    @Singleton
    fun provideDatabaseIntegrityValidator(
        database: EduInvoiceDatabase
    ): DatabaseIntegrityValidator {
        return DatabaseIntegrityValidator(database)
    }
    
    @Provides
    @Singleton
    fun provideDatabaseFallbackManager(
        @ApplicationContext context: Context,
        database: EduInvoiceDatabase,
        healthMonitor: DatabaseHealthMonitor,
        integrityValidator: DatabaseIntegrityValidator
    ): DatabaseFallbackManager {
        return DatabaseFallbackManager(context, database, healthMonitor, integrityValidator)
    }
    
    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
        database: EduInvoiceDatabase
    ): BackupRepository {
        return BackupRepository(context, database)
    }
}
