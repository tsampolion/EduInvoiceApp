package gr.eduinvoice.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.data.database.EduInvoiceDatabase
import net.sqlcipher.database.SQLiteDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEduInvoiceDatabase(
        @ApplicationContext context: Context
    ): EduInvoiceDatabase {
        val passphrase = SQLiteDatabase.getBytes("eduinvoice".toCharArray())
        return EduInvoiceDatabase.getDatabase(context, passphrase)
    }

    // DatabaseModule only provides the Room database instance.
}
