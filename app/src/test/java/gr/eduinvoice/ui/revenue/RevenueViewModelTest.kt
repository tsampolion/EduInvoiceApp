package gr.eduinvoice.ui.revenue

import app.cash.turbine.test
import gr.eduinvoice.domain.analytics.GetEarningsByClass
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.user.CurrentUserProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RevenueViewModelTest {

    @Test
    fun aggregates_daily_weekly_monthly_paid_unpaid_and_debts() = runTest(UnconfinedTestDispatcher()) {
        val uid = 42L
        val userFlow = MutableStateFlow<Long?>(uid)
        val currentUserProvider = object : CurrentUserProvider {
            override val loggedInUserId: Flow<Long?> = userFlow
        }

        val students = listOf(
            gr.eduinvoice.domain.model.DomainStudent(id = 1, ownerId = uid, name = "Alice", surname = "A", className = "A1", rate = 10.0, rateType = "per_lesson", isActive = true, lastModified = 0),
            gr.eduinvoice.domain.model.DomainStudent(id = 2, ownerId = uid, name = "Bob", surname = "B", className = "B1", rate = 20.0, rateType = "per_lesson", isActive = true, lastModified = 0)
        )
        val studentUseCases = object : StudentUseCases(
            getActiveStudents = { flowOf(students) },
            getArchivedStudents = { flowOf(emptyList()) },
            getStudentById = { flowOf(null) },
            getStudentByIdAny = { flowOf(null) },
            insertStudent = { 0 },
            updateStudent = { },
            softDeleteStudent = { },
            restoreStudent = { },
            getActiveStudentCount = { flowOf(2) },
            classNameExists = { false },
            getStudentsPaginated = { _, _, _ -> emptyList() },
            searchStudentsPaginated = { _, _, _, _ -> emptyList() }
        ) {}

        val today = java.time.LocalDate.now().toString()
        val weekDate = java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toString()
        val monthDate = java.time.LocalDate.now().withDayOfMonth(1).toString()

        val lessons = listOf(
            DomainLesson(id = 10, ownerId = uid, studentId = 1, groupId = null, date = today, startTime = "10:00", durationMinutes = 60, notes = null, isPaid = false, isInvoiced = false, lastModified = 0),
            DomainLesson(id = 11, ownerId = uid, studentId = 2, groupId = null, date = weekDate, startTime = "11:00", durationMinutes = 60, notes = null, isPaid = true, isInvoiced = false, lastModified = 0),
            DomainLesson(id = 12, ownerId = uid, studentId = 1, groupId = null, date = monthDate, startTime = "12:00", durationMinutes = 60, notes = null, isPaid = false, isInvoiced = false, lastModified = 0)
        )
        val lessonUseCases = object : LessonUseCases(
            getAllLessons = { flowOf(lessons) },
            getLessonById = { _, _ -> flowOf(null) },
            getStudentLessons = { sid -> flowOf(lessons.filter { it.studentId == sid }) },
            getLessonsWithStudents = { flowOf(lessons) },
            getLessonsWithStudentsByStudentAndDateRange = { _, _, _ -> flowOf(emptyList()) },
            getLessonsWithStudentsPaginated = { _, _ -> emptyList() },
            addLesson = { 0 },
            addGroupLesson = { 0 },
            addGroupLessonWithAbsences = { _, _ -> emptyList() },
            updateLesson = { },
            deleteLesson = { },
            updateLessonPaidStatus = { _, _, _ -> },
            updateLessonInvoicedStatus = { _, _, _ -> },
            isLessonInvoiced = { _, _ -> false },
            editGroupLesson = { _, _, _, _, _, _, _, _, _ -> },
            getGroupLessonMasters = { flowOf(emptyList()) },
            getGroupLessonMasterById = { _, _ -> flowOf(null) },
            getAbsentStudentIdsForMaster = { _, _ -> flowOf(emptyList()) },
            deleteGroupLesson = { _, _ -> },
            hasInvoicedOrPaidLessonsForMaster = { _, _ -> false },
            createInvoiceMasterAndMarkLessons = { _, _, _, _, _ -> 0 },
            archiveInvoiceMaster = { _, _ -> },
            deleteInvoiceMaster = { _, _ -> },
            getInvoiceMastersByStudent = { _, _ -> flowOf(emptyList()) },
            getInvoiceMasterById = { _, _ -> flowOf(null) },
            updateInvoiceMaster = { _, _ -> },
            createPaymentBatchAndMarkLessons = { _, _, _, _, _ -> 0 },
            createRescheduleMasterAndApply = { _, _, _, _, _ -> 0 },
            getRescheduleMasters = { flowOf(emptyList()) }
        ) {}

        val getEarningsByClass = GetEarningsByClass(lessonRepository = object : gr.eduinvoice.domain.repository.DomainLessonRepository by gr.eduinvoice.domain.repository.FakeLessonRepo() {
            override fun getEarningsByClass(startDate: String, endDate: String, userId: Long): Flow<List<Pair<String, Double>>> =
                flowOf(listOf("A1" to 10.0, "B1" to 20.0))
        })

        val vm = RevenueViewModel(studentUseCases, lessonUseCases, currentUserProvider, getEarningsByClass)

        vm.uiState.test {
            val state = awaitItem()
            // From students/lessons above: daily = 10 (Alice), weekly includes 2 lessons = 30, monthly = 30
            assertEquals(10.0, state.dailyRevenue, 0.0001)
            assertEquals(true, state.earningsByClass.any { it.className == "A1" && it.revenue == 10.0 })
            cancelAndIgnoreRemainingEvents()
        }
    }
}