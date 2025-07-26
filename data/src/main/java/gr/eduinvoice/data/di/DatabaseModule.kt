package gr.eduinvoice.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.user.UserPreferencesRepository
import net.sqlcipher.database.SQLiteDatabase
import kotlinx.coroutines.runBlocking
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
        val passphrase = SQLiteDatabase.getBytes(pass.toCharArray())
        return EduInvoiceDatabase.getDatabase(context, passphrase)
    }

    // DatabaseModule only provides the Room database instance.
}
