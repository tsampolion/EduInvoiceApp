package gr.eduinvoice.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.eduinvoice.domain.repository.DomainStudentRepository
import gr.eduinvoice.domain.repository.DomainLessonRepository
import gr.eduinvoice.domain.repository.DomainGroupRepository
import gr.eduinvoice.domain.repository.DomainUserRepository
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.student.*
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.domain.user.*
import gr.eduinvoice.domain.user.DeleteAccount
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    @Singleton
    fun provideStudentUseCases(repository: DomainStudentRepository): StudentUseCases =
        StudentUseCases(
            getActiveStudents = GetActiveStudents(repository),
            getArchivedStudents = GetArchivedStudents(repository),
            getStudentById = GetStudentById(repository),
            insertStudent = InsertStudent(repository),
            updateStudent = UpdateStudent(repository),
            softDeleteStudent = SoftDeleteStudent(repository),
            restoreStudent = RestoreStudent(repository),
            getActiveStudentCount = GetActiveStudentCount(repository),
            classNameExists = ClassNameExists(repository),
            getStudentsPaginated = GetStudentsPaginated(repository),
            searchStudentsPaginated = SearchStudentsPaginated(repository)
        )

    @Provides
    @Singleton
    fun provideGroupUseCases(repository: DomainGroupRepository): GroupUseCases =
        GroupUseCases(
            insertGroup = InsertGroup(repository),
            updateGroup = UpdateGroup(repository),
            deleteGroup = DeleteGroup(repository),
            archiveGroup = ArchiveGroup(repository),
            getAllGroups = GetAllGroups(repository),
            getGroupById = GetGroupById(repository),
            addStudentToGroup = AddStudentToGroup(repository),
            removeStudentFromGroup = RemoveStudentFromGroup(repository),
            getGroupStudents = GetGroupStudents(repository)
        )

    @Provides
    @Singleton
    fun provideUserUseCases(repository: DomainUserRepository): UserUseCases =
        UserUseCases(
            createUser = CreateUser(repository),
            authenticateUser = AuthenticateUser(repository),
            getUserProfile = GetUserProfile(repository),
            updateUser = UpdateUser(repository),
            resetPassword = ResetPassword(repository),
            deleteAccount = DeleteAccount(repository)
        )

    @Provides
    @Singleton
    fun provideLessonUseCases(repository: DomainLessonRepository): LessonUseCases =
        LessonUseCases(
            getAllLessons = GetAllLessons(repository),
            getLessonById = GetLessonById(repository),
            getStudentLessons = GetStudentLessons(repository),
            getLessonsWithStudents = GetLessonsWithStudents(repository),
            getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(repository),
            addLesson = AddLesson(repository),
            addGroupLesson = AddGroupLesson(repository),
            updateLesson = UpdateLesson(repository),
            deleteLesson = DeleteLesson(repository),
            updateLessonPaidStatus = UpdateLessonPaidStatus(repository),
            updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(repository),
            isLessonInvoiced = IsLessonInvoiced(repository),
            getLessonsWithStudentsPaginated = GetLessonsWithStudentsPaginated(repository)
        )
}
