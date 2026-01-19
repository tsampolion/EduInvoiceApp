package gr.eduinvoice.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.domain.billing.DomainPdfGenerator
import gr.eduinvoice.domain.billing.DomainPdfTheme
import gr.eduinvoice.domain.billing.DomainPdfThemes
import gr.eduinvoice.utils.AndroidPdfGenerator
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
