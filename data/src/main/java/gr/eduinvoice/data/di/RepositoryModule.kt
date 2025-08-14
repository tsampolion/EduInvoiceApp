package gr.eduinvoice.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Binds
import gr.eduinvoice.data.repository.DataSearchHistoryRepository
import gr.eduinvoice.domain.user.SearchHistoryRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // All repositories have @Inject constructors, so they are provided automatically by Hilt
    // Bind domain interfaces where needed
    @Binds
    abstract fun bindSearchHistoryRepository(impl: DataSearchHistoryRepository): SearchHistoryRepository
}
