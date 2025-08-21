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
import javax.inject.Provider

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEduInvoiceDatabase(
        @ApplicationContext context: Context,
        prefs: UserPreferencesRepository
    ): Provider<EduInvoiceDatabase> {
        return Provider {
            runBlocking(Dispatchers.IO) {
                val encryptionEnabled = BuildConfig.DB_ENCRYPTION_ENABLED
                var sqlCipherAvailable = false
                if (encryptionEnabled) {
                    try {
                        SQLiteDatabase.loadLibs(context)
                        sqlCipherAvailable = true
                    } catch (t: Throwable) {
                        Log.w("DatabaseModule", "SQLCipher native library not available on this device/emulator", t)
                    }
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

                Log.d("DatabaseModule", "Passphrase length: ${passphrase.size}")

                if (encryptionEnabled && sqlCipherAvailable) {
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
                } else {
                    // Unencrypted path
                    Log.w("DatabaseModule", "Using UNENCRYPTED Room database per flavor configuration")
                    Room.databaseBuilder(
                        context.applicationContext,
                        EduInvoiceDatabase::class.java,
                        DatabaseConstants.DATABASE_NAME + "_plain"
                    )
                        .fallbackToDestructiveMigration(true)
                        .build()
                }
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
    fun provideBackupRepository(
        @ApplicationContext context: Context,
        database: EduInvoiceDatabase
    ): BackupRepository {
        return BackupRepository(context, database)
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
