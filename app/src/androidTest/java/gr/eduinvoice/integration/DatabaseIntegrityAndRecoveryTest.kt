package gr.eduinvoice.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import gr.eduinvoice.data.monitoring.DatabaseHealthMonitor
import gr.eduinvoice.data.validation.DatabaseIntegrityValidator
import gr.eduinvoice.data.fallback.DatabaseFallbackManager
import gr.eduinvoice.data.database.DatabaseConstants
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import java.io.File

@RunWith(AndroidJUnit4::class)
class DatabaseIntegrityAndRecoveryTest {

    @get:Rule
    val databaseContainer = TestDatabaseContainer()

    @Test
    fun integrity_validation_and_maintenance_do_not_crash() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = databaseContainer.database

        val health = DatabaseHealthMonitor(context, db)
        val validator = DatabaseIntegrityValidator(db)

        val status = health.checkDatabaseHealth()
        val validation = validator.validateAllTables()
        val maintenance = health.performMaintenance()

        assertNotNull(status)
        assertNotNull(validation)
        assertNotNull(maintenance)
        assertTrue("Maintenance should succeed", maintenance.success)
    }

    @Test
    fun simulated_corruption_triggers_recovery_schedule() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = databaseContainer.database

        val health = DatabaseHealthMonitor(context, db)
        val validator = DatabaseIntegrityValidator(db)
        val fallback = DatabaseFallbackManager(context, db, health, validator)

        // Simulate corruption by deleting DB file
        val dbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
        if (dbFile.exists()) {
            dbFile.delete()
        }

        // Health should now report unhealthy
        val status = health.checkDatabaseHealth()
        assertFalse("Health should indicate issues after corruption", status.isHealthy)

        // Schedule recovery (we won't wait the full interval in test)
        fallback.scheduleRecovery()

        // Force a reset to validate recovery flow returns a result
        val reset = fallback.forceDatabaseReset()
        assertTrue("Force reset should report success", reset.success)
    }
}