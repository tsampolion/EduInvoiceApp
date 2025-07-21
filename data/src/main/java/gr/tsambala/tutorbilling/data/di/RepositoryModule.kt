package gr.tsambala.tutorbilling.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.dao.GroupDao
import gr.tsambala.tutorbilling.data.dao.StudentDao
import gr.tsambala.tutorbilling.data.repository.StudentRepository
import gr.tsambala.tutorbilling.data.repository.TutorBillingRepository
import gr.tsambala.tutorbilling.data.repository.GroupRepository
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
