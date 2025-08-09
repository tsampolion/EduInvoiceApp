package gr.eduinvoice.data.testfixtures

import android.content.Context
import androidx.room.Room
import gr.eduinvoice.data.database.EduInvoiceDatabase

/**
 * Factory for creating test databases
 * Provides in-memory databases for testing
 */
object TestDbFactory {
    
    /**
     * Creates an in-memory database for testing
     */
    fun createInMemory(context: Context): EduInvoiceDatabase =
        Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    /**
     * Creates an in-memory database with a specific name
     */
    fun createInMemory(context: Context, name: String): EduInvoiceDatabase =
        Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .setDatabaseName(name)
            .allowMainThreadQueries()
            .build()

    /**
     * Creates an in-memory database with custom configuration
     */
    fun createInMemory(
        context: Context,
        name: String = "test_db",
        fallbackToDestructiveMigration: Boolean = true
    ): EduInvoiceDatabase =
        Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .setDatabaseName(name)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
}
