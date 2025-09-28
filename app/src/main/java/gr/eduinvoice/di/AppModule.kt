package gr.eduinvoice.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.utils.BackgroundProcessor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBackgroundProcessor(): BackgroundProcessor = BackgroundProcessor()
    
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}
