package gr.tutorbilling.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.tutorbilling.data.dao.LessonDao
import gr.tutorbilling.data.repository.StudentRepository
import gr.tutorbilling.data.repository.TutorBillingRepository
import gr.tutorbilling.data.repository.GroupRepository
import gr.tutorbilling.domain.lesson.*
import gr.tutorbilling.domain.student.*
import gr.tutorbilling.domain.group.*
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
    fun provideGroupUseCases(repository: GroupRepository): GroupUseCases =
        GroupUseCases(
            insertGroup = InsertGroup(repository),
            updateGroup = UpdateGroup(repository),
            deleteGroup = DeleteGroup(repository),
            getAllGroups = GetAllGroups(repository),
            getGroupById = GetGroupById(repository),
            addStudentToGroup = AddStudentToGroup(repository),
            removeStudentFromGroup = RemoveStudentFromGroup(repository),
            getGroupStudents = GetGroupStudents(repository)
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
            addGroupLesson = AddGroupLesson(repository),
            updateLesson = UpdateLesson(repository),
            deleteLesson = DeleteLesson(dao),
            updateLessonPaidStatus = UpdateLessonPaidStatus(dao),
            updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(dao),
            isLessonInvoiced = IsLessonInvoiced(dao)
        )
}
