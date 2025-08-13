package gr.eduinvoice.infrastructure

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import gr.eduinvoice.data.database.EduInvoiceDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

object TestDatabaseContainer {
    fun inMemory(): EduInvoiceDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val passphrase: ByteArray = SQLiteDatabase.getBytes("test-passphrase".toCharArray())
        val factory = SupportFactory(passphrase)
        return Room.inMemoryDatabaseBuilder(context, EduInvoiceDatabase::class.java)
            .openHelperFactory(factory)
            .allowMainThreadQueries()
            .build()
    }
}


