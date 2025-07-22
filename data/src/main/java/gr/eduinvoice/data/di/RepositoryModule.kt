package gr.eduinvoice.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.data.repository.GroupRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideStudentRepository(studentDao: StudentDao): StudentRepository =
        StudentRepository(studentDao)

    @Provides
    @Singleton
    fun provideGroupRepository(groupDao: GroupDao): GroupRepository =
        GroupRepository(groupDao)

    @Provides
    @Singleton
    fun provideTutorBillingRepository(
        studentDao: StudentDao,
        lessonDao: LessonDao,
        groupDao: GroupDao
    ): TutorBillingRepository = TutorBillingRepository(studentDao, lessonDao, groupDao)
}
