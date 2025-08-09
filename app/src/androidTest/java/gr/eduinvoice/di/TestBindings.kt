package gr.eduinvoice.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.data.concurrency.ConcurrencyController
import gr.eduinvoice.test.support.fakes.NoopConcurrencyController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestBindings {
    
    @Provides
    @Singleton
    fun provideConcurrencyController(): ConcurrencyController = NoopConcurrencyController.create()
}
