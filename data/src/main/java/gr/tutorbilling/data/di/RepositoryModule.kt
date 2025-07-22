package gr.tutorbilling.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.tutorbilling.data.dao.LessonDao
import gr.tutorbilling.data.dao.GroupDao
import gr.tutorbilling.data.dao.StudentDao
import gr.tutorbilling.data.repository.StudentRepository
import gr.tutorbilling.data.repository.EduInvoiceRepository
import gr.tutorbilling.data.repository.GroupRepository
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
    fun provideEduInvoiceRepository(
        studentDao: StudentDao,
        lessonDao: LessonDao,
        groupDao: GroupDao
    ): EduInvoiceRepository = EduInvoiceRepository(studentDao, lessonDao, groupDao)
}
