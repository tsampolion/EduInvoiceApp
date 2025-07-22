package gr.eduinvoice.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.data.database.EduInvoiceDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEduInvoiceDatabase(
        @ApplicationContext context: Context
    ): EduInvoiceDatabase {
        return EduInvoiceDatabase.getDatabase(context)
    }

    // DatabaseModule only provides the Room database instance.
}
