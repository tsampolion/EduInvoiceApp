package gr.eduinvoice.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.data.user.CurrentUserProvider
import gr.eduinvoice.data.user.UserPreferencesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserPreferencesModule {
    @Binds
    @Singleton
    abstract fun bindCurrentUserProvider(repo: UserPreferencesRepository): CurrentUserProvider
}
