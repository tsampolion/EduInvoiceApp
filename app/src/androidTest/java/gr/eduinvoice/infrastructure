package gr.eduinvoice.infrastructure

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.DatabaseConstants
import gr.eduinvoice.data.database.EduInvoiceDatabase
import net.sqlcipher.database.SupportFactory
import org.junit.rules.ExternalResource
import java.io.File

class TestDatabaseContainer : ExternalResource() {
    lateinit var database: EduInvoiceDatabase
        private set

    private lateinit var dbFile: File

    override fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val passphrase = ByteArray(32) { 1 }
        val factory = SupportFactory(passphrase)
        dbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
        // Ensure a clean slate for each test
        if (dbFile.exists()) {
            dbFile.deleteRecursively()
        }
        database = Room.databaseBuilder(
            context,
            EduInvoiceDatabase::class.java,
            DatabaseConstants.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    override fun after() {
        try {
            database.close()
        } catch (_: Throwable) {}
        if (this::dbFile.isInitialized) {
            try { dbFile.deleteRecursively() } catch (_: Throwable) {}
        }
    }
}