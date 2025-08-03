package gr.eduinvoice.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // All repositories have @Inject constructors, so they are provided automatically by Hilt
    // No manual providers needed
}
