package gr.eduinvoice.data.di

import android.content.Context
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
import gr.eduinvoice.data.fallback.DatabaseFallbackManager
import gr.eduinvoice.data.monitoring.DatabaseHealthMonitor
import gr.eduinvoice.data.repository.OfflineDataManager
import gr.eduinvoice.data.repository.SyncRepository
import gr.eduinvoice.data.concurrency.ConcurrencyController
import gr.eduinvoice.data.concurrency.OperationQueueManager
import gr.eduinvoice.data.concurrency.TransactionManager
import gr.eduinvoice.data.sync.ConflictResolver
import gr.eduinvoice.data.sync.SyncManager
import gr.eduinvoice.data.user.UserPreferencesRepository
import gr.eduinvoice.data.utils.NetworkMonitor
import gr.eduinvoice.data.validation.DatabaseIntegrityValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SQLiteDatabase
import javax.inject.Singleton
import androidx.room.Room
import net.sqlcipher.database.SupportFactory

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEduInvoiceDatabase(
        @ApplicationContext context: Context,
        prefs: UserPreferencesRepository
    ): EduInvoiceDatabase {
        // IMPORTANT: This provider uses `runBlocking` to bridge the async `getDbPassphrase()`
        // call into the synchronous Hilt dependency graph construction. This is an
        // architectural trade-off.
        //
        // CRITICAL USAGE NOTE: The first time this database is injected and accessed,
        // it MUST be done from a background thread (e.g., inside `withContext(Dispatchers.IO)`).
        // Failure to do so will block the main UI thread and cause the application to freeze.
        // The `MainActivity` demonstrates the correct usage pattern for initialization.
        return runBlocking(Dispatchers.IO) {
            try {
                // Always require SQLCipher for database operations
                try {
                    SQLiteDatabase.loadLibs(context)
                } catch (t: Throwable) {
                    Log.e("DatabaseModule", "SQLCipher native library not available on this device/emulator", t)
                    val hint = buildString {
                        append("SQLCipher native library could not be loaded. ")
                        append("Ensure you're running on a 64-bit device/emulator (arm64-v8a or x86_64). ")
                        append("If on Android 14/15 emulator, use a 64-bit system image. ")
                        append("Make sure ABI filters include only 'arm64-v8a' and 'x86_64'.")
                    }
                    throw DatabaseInitException(hint, t)
                }

                // Get passphrase with better error handling (already on IO dispatcher)
                val pass = try {
                    prefs.getDbPassphrase()
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

                // Avoid logging passphrase or even its length for security hygiene

                try {
                    EduInvoiceDatabase.getDatabase(context, passphrase)
                } catch (e: Exception) {
                    Log.e("DatabaseModule", "Failed to create database", e)

                    // Try to recover by using destructive migration as last resort
                    if (e.message?.contains("Migration didn't properly handle") == true) {
                        Log.w("DatabaseModule", "Migration failed, attempting recovery with destructive migration")
                        try {
                            val factory = SupportFactory(passphrase)
                            val recoveredInstance = Room.databaseBuilder(
                                context.applicationContext,
                                EduInvoiceDatabase::class.java,
                                DatabaseConstants.DATABASE_NAME
                            )
                                .openHelperFactory(factory)
                                .fallbackToDestructiveMigration(true) // Allow destructive migration for recovery
                                .build()

                            Log.i("DatabaseModule", "Database recovered successfully with destructive migration")
                            recoveredInstance
                        } catch (recoveryException: Exception) {
                            Log.e("DatabaseModule", "Recovery failed", recoveryException)
                            throw DatabaseInitException("Database recovery failed", recoveryException)
                        }
                    } else {
                        throw DatabaseInitException("Failed to create database", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("DatabaseModule", "Critical database initialization error", e)
                throw DatabaseInitException("Database initialization failed", e)
            }
        }
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
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor {
        return NetworkMonitor(context)
    }

    @Provides
    @Singleton
    fun provideOfflineDataManager(
        @ApplicationContext context: Context
    ): OfflineDataManager {
        return OfflineDataManager(context)
    }

    @Provides
    @Singleton
    fun provideConflictResolver(): ConflictResolver {
        return ConflictResolver()
    }

    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        offlineDataManager: OfflineDataManager,
        networkMonitor: NetworkMonitor,
        conflictResolver: ConflictResolver
    ): SyncManager {
        return SyncManager(context, offlineDataManager, networkMonitor, conflictResolver)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        @ApplicationContext context: Context,
        studentDao: gr.eduinvoice.data.dao.StudentDao,
        lessonDao: gr.eduinvoice.data.dao.LessonDao,
        groupDao: gr.eduinvoice.data.dao.GroupDao,
        offlineDataManager: OfflineDataManager,
        syncManager: SyncManager,
        networkMonitor: NetworkMonitor
    ): SyncRepository {
        return SyncRepository(context, studentDao, lessonDao, groupDao, offlineDataManager, syncManager, networkMonitor)
    }

    // ===== Concurrency Components =====

    @Provides
    @Singleton
    fun provideTransactionManager(
        database: EduInvoiceDatabase
    ): TransactionManager {
        return TransactionManager(database)
    }

    @Provides
    @Singleton
    fun provideOperationQueueManager(): OperationQueueManager {
        return OperationQueueManager()
    }

    @Provides
    @Singleton
    fun provideConcurrencyController(
        transactionManager: TransactionManager,
        operationQueueManager: OperationQueueManager
    ): ConcurrencyController {
        return ConcurrencyController(transactionManager, operationQueueManager)
    }
}
