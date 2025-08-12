package gr.eduinvoice.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.domain.repository.DomainStudentRepository
import gr.eduinvoice.domain.repository.DomainLessonRepository
import gr.eduinvoice.domain.repository.DomainUserRepository
import gr.eduinvoice.domain.repository.DomainGroupRepository
import gr.eduinvoice.data.adapter.DomainStudentRepositoryAdapter
import gr.eduinvoice.data.adapter.DomainLessonRepositoryAdapter
import gr.eduinvoice.data.adapter.DomainUserRepositoryAdapter
import gr.eduinvoice.data.adapter.DomainGroupRepositoryAdapter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainAdapterModule {

    @Provides
    @Singleton
    fun provideDomainStudentRepository(adapter: DomainStudentRepositoryAdapter): DomainStudentRepository = adapter

    @Provides
    @Singleton
    fun provideDomainLessonRepository(adapter: DomainLessonRepositoryAdapter): DomainLessonRepository = adapter

    @Provides
    @Singleton
    fun provideDomainUserRepository(adapter: DomainUserRepositoryAdapter): DomainUserRepository = adapter

    @Provides
    @Singleton
    fun provideDomainGroupRepository(adapter: DomainGroupRepositoryAdapter): DomainGroupRepository = adapter
}
