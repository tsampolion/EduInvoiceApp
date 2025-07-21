package gr.tsambala.tutorbilling.ui.lessons

import gr.tsambala.tutorbilling.MainDispatcherRule
import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.database.LessonWithStudent
import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.model.Student
import gr.tsambala.tutorbilling.domain.lesson.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LessonsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessonFlow = MutableStateFlow<List<LessonWithStudent>>(emptyList())

    private val lessonDao = object : LessonDao {
        override suspend fun insert(lesson: Lesson): Long = 0
        override suspend fun update(lesson: Lesson) {}
        override suspend fun delete(lesson: Lesson) {}
        override suspend fun deleteById(lessonId: Long) {}
        override fun getLessonById(lessonId: Long) = flowOf(null)
        override fun getLessonsByStudentId(studentId: Long) = flowOf(emptyList<Lesson>())
        override fun getAllLessons() = flowOf(emptyList<Lesson>())
        override fun getLessonsInDateRange(startDate: String, endDate: String) = flowOf(emptyList<Lesson>())
        override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String) = flowOf(emptyList<Lesson>())
        override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String) = flowOf(emptyList<Lesson>())
        override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String) = flowOf(emptyList<Lesson>())
        override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean) {
            lessonFlow.value = lessonFlow.value.map {
                if (it.lesson.id in ids) it.copy(lesson = it.lesson.copy(isPaid = paid)) else it
            }
        }
        override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean) {}
        override fun isLessonInvoiced(lessonId: Long) = lessonFlow.map { list -> list.find { it.lesson.id == lessonId }?.lesson?.isInvoiced }
        override fun getLessonsWithStudents() = lessonFlow.asStateFlow()
        override fun getLessonsWithStudentsByStudent(studentId: Long) = flowOf(emptyList<LessonWithStudent>())
        override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String) = flowOf(emptyList<LessonWithStudent>())
        override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String) = flowOf(emptyList<LessonWithStudent>())
    }

    private val useCases = LessonUseCases(
        getAllLessons = GetAllLessons(lessonDao),
        getLessonById = GetLessonById(lessonDao),
        getStudentLessons = GetStudentLessons(TutorBillingRepositoryFake()),
        getLessonsWithStudents = GetLessonsWithStudents(lessonDao),
        getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(lessonDao),
        addLesson = AddLesson(TutorBillingRepositoryFake()),
        updateLesson = UpdateLesson(TutorBillingRepositoryFake()),
        deleteLesson = DeleteLesson(lessonDao),
        updateLessonPaidStatus = UpdateLessonPaidStatus(lessonDao),
        updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(lessonDao),
        isLessonInvoiced = IsLessonInvoiced(lessonDao)
    )

    @Test
    fun updatePaidShowsGenerateInvoice() = runTest {
        val student = Student(id = 1, name = "Bob", surname = "", parentMobile = "", className = "B", rate = 10.0)
        val lesson = Lesson(id = 1, studentId = 1, date = "2024-01-01", startTime = "10:00", durationMinutes = 60)
        lessonFlow.value = listOf(LessonWithStudent(lesson, student))
        val vm = LessonsViewModel(useCases)
        advanceUntilIdle()
        vm.updatePaid(1, true)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.dialog is LessonDialog.GenerateInvoice)
    }

    private class TutorBillingRepositoryFake : gr.tsambala.tutorbilling.data.repository.TutorBillingRepository(
        studentDao = object : gr.tsambala.tutorbilling.data.dao.StudentDao {
            override suspend fun insert(student: Student) = 0L
            override suspend fun update(student: Student) {}
            override suspend fun delete(student: Student) {}
            override suspend fun softDeleteStudent(studentId: Long) {}
            override fun getStudentById(studentId: Long) = flowOf(null)
            override fun getAllActiveStudents() = flowOf(emptyList<Student>())
            override fun getArchivedStudents() = flowOf(emptyList<Student>())
            override suspend fun restoreStudent(studentId: Long) {}
            override fun getStudentByIdAny(studentId: Long) = flowOf(null)
            override suspend fun getActiveStudentCount() = 0
            override suspend fun classNameExists(name: String) = 0
        },
        lessonDao = lessonDao
    )
}
