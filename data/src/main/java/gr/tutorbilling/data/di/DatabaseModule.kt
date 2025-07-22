package gr.tutorbilling.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.tutorbilling.data.database.TutorBillingDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTutorBillingDatabase(
        @ApplicationContext context: Context
    ): TutorBillingDatabase {
        return TutorBillingDatabase.getDatabase(context)
    }

    // DatabaseModule only provides the Room database instance.
}
