package gr.eduinvoice.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import gr.eduinvoice.domain.user.CurrentUserProvider
import gr.eduinvoice.domain.user.UserPreferencesRepository
import gr.eduinvoice.domain.settings.AppSettings
import gr.eduinvoice.domain.settings.SettingsRepository
import gr.eduinvoice.domain.repository.BackupRepository
import gr.eduinvoice.data.impl.user.DataCurrentUserProvider
import gr.eduinvoice.data.impl.user.DataUserPreferencesRepository
import gr.eduinvoice.data.impl.settings.DataAppSettings
import gr.eduinvoice.data.impl.settings.DataSettingsRepository
import gr.eduinvoice.data.impl.backup.DataBackupRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainBindingsModule {
    @Binds @Singleton abstract fun bindCurrentUserProvider(impl: DataCurrentUserProvider): CurrentUserProvider
    @Binds @Singleton abstract fun bindUserPreferencesRepository(impl: DataUserPreferencesRepository): UserPreferencesRepository
    @Binds @Singleton abstract fun bindAppSettings(impl: DataAppSettings): AppSettings
    @Binds @Singleton abstract fun bindSettingsRepository(impl: DataSettingsRepository): SettingsRepository
    @Binds @Singleton abstract fun bindBackupRepository(impl: DataBackupRepository): BackupRepository
}
