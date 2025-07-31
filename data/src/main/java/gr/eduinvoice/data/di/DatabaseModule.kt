package gr.eduinvoice.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.data.database.DatabaseConstants
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.user.UserPreferencesRepository
import net.sqlcipher.database.SQLiteDatabase
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
        val pass = runBlocking { prefs.getDbPassphrase() }
        require(!pass.isNullOrEmpty()) { "Database passphrase unavailable" }
        val passphrase = SQLiteDatabase.getBytes(pass!!.toCharArray())
        return try {
            EduInvoiceDatabase.getDatabase(context, passphrase)
        } catch (e: SQLiteException) {
            Log.e("DatabaseModule", "Database open failed, attempting recovery", e)
            context.getDatabasePath(DatabaseConstants.DATABASE_NAME).delete()
            EduInvoiceDatabase.getDatabase(context, passphrase)
        }
    }

    // DatabaseModule only provides the Room database instance.
}
