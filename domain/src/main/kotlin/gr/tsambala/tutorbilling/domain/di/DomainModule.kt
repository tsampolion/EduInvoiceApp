package gr.tsambala.tutorbilling.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.repository.StudentRepository
import gr.tsambala.tutorbilling.data.repository.TutorBillingRepository
import gr.tsambala.tutorbilling.domain.lesson.*
import gr.tsambala.tutorbilling.domain.student.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    @Singleton
    fun provideStudentUseCases(repository: StudentRepository): StudentUseCases =
        StudentUseCases(
            getActiveStudents = GetActiveStudents(repository),
            getArchivedStudents = GetArchivedStudents(repository),
            getStudentById = GetStudentById(repository),
            insertStudent = InsertStudent(repository),
            updateStudent = UpdateStudent(repository),
            softDeleteStudent = SoftDeleteStudent(repository),
            restoreStudent = RestoreStudent(repository),
            getActiveStudentCount = GetActiveStudentCount(repository),
            classNameExists = ClassNameExists(repository)
        )

    @Provides
    @Singleton
    fun provideLessonUseCases(
        dao: LessonDao,
        repository: TutorBillingRepository
    ): LessonUseCases =
        LessonUseCases(
            getAllLessons = GetAllLessons(dao),
            getLessonById = GetLessonById(dao),
            getStudentLessons = GetStudentLessons(repository),
            getLessonsWithStudents = GetLessonsWithStudents(dao),
            getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(dao),
            addLesson = AddLesson(repository),
            updateLesson = UpdateLesson(repository),
            deleteLesson = DeleteLesson(dao),
            updateLessonPaidStatus = UpdateLessonPaidStatus(dao)
        )
}
