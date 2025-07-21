package gr.tsambala.tutorbilling.ui.lessons

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.isToggleable
import gr.tsambala.tutorbilling.MainDispatcherRule
import gr.tsambala.tutorbilling.data.database.LessonWithStudent
import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.model.Student
import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.dao.StudentDao
import gr.tsambala.tutorbilling.data.repository.TutorBillingRepository
import gr.tsambala.tutorbilling.domain.lesson.*
import kotlinx.coroutines.flow.Flow
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
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class LessonsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val lessonFlow = MutableStateFlow<List<LessonWithStudent>>(emptyList())
    private val lessonDao = FakeLessonDao(lessonFlow)
    private val lessonUseCases = LessonUseCases(
        getAllLessons = GetAllLessons(lessonDao),
        getLessonById = GetLessonById(lessonDao),
        getStudentLessons = GetStudentLessons(TutorBillingRepository(FakeStudentDao(), lessonDao)),
        getLessonsWithStudents = GetLessonsWithStudents(lessonDao),
        getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(lessonDao),
        addLesson = AddLesson(TutorBillingRepository(FakeStudentDao(), lessonDao)),
        updateLesson = UpdateLesson(TutorBillingRepository(FakeStudentDao(), lessonDao)),
        deleteLesson = DeleteLesson(lessonDao),
        updateLessonPaidStatus = UpdateLessonPaidStatus(lessonDao)
    )

    @Test
    fun headersDisplayedInOrder() = runTest {
        val s1 = Student(id = 1, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        val s2 = Student(id = 2, name = "Bob", surname = "", parentMobile = "", className = "", rate = 10.0)
        val today = LocalDate.now().toString()
        lessonFlow.value = listOf(
            LessonWithStudent(Lesson(1, 1, today, "10:00", 60, null, false), s1),
            LessonWithStudent(Lesson(2, 2, today, "11:00", 60, null, false), s2)
        )
        val vm = LessonsViewModel(lessonUseCases)
        composeRule.setContent {
            LessonsScreen(onBack = {}, onLessonClick = { _, _ -> }, onAddLesson = {}, viewModel = vm)
        }
        composeRule.waitForIdle()
        val header1 = composeRule.onNodeWithTag("header_1").fetchSemanticsNode().positionInRoot.y
        val header2 = composeRule.onNodeWithTag("header_2").fetchSemanticsNode().positionInRoot.y
        assertTrue(header1 < header2)
    }

    @Test
    fun checkboxAndClickCallbacksWork() = runTest {
        val s1 = Student(id = 1, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        val today = LocalDate.now().toString()
        lessonFlow.value = listOf(
            LessonWithStudent(Lesson(1, 1, today, "10:00", 60, null, false), s1)
        )
        val vm = LessonsViewModel(lessonUseCases)
        var clicked = false
        composeRule.setContent {
            LessonsScreen(onBack = {}, onLessonClick = { _, _ -> clicked = true }, onAddLesson = {}, viewModel = vm)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("10:00 • 60 min").performClick()
        composeRule.waitForIdle()
        assertTrue(clicked)
        composeRule.onAllNodes(isToggleable())[0].performClick()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.lessons[1L]!!.first().lesson.isPaid)
    }

    class FakeStudentDao : StudentDao {
        override suspend fun insert(student: Student): Long = 0L
        override suspend fun update(student: Student) {}
        override suspend fun delete(student: Student) {}
        override suspend fun softDeleteStudent(studentId: Long) {}
        override fun getStudentById(studentId: Long): Flow<Student?> = flowOf(null)
        override fun getAllActiveStudents(): Flow<List<Student>> = flowOf(emptyList())
        override fun getArchivedStudents(): Flow<List<Student>> = flowOf(emptyList())
        override suspend fun restoreStudent(studentId: Long) {}
        override fun getStudentByIdAny(studentId: Long): Flow<Student?> = flowOf(null)
        override suspend fun getActiveStudentCount(): Int = 0
        override suspend fun classNameExists(name: String): Int = 0
    }

    class FakeLessonDao(private val flow: MutableStateFlow<List<LessonWithStudent>>) : LessonDao {
        override suspend fun insert(lesson: Lesson): Long = 0L
        override suspend fun update(lesson: Lesson) {}
        override suspend fun delete(lesson: Lesson) {}
        override suspend fun deleteById(lessonId: Long) {}
        override fun getLessonById(lessonId: Long): Flow<Lesson?> = flow.map { it.find { l -> l.lesson.id == lessonId }?.lesson }
        override fun getLessonsByStudentId(studentId: Long): Flow<List<Lesson>> = flow.map { list -> list.filter { it.lesson.studentId == studentId }.map { it.lesson } }
        override fun getAllLessons(): Flow<List<Lesson>> = flow.map { list -> list.map { it.lesson } }
        override fun getLessonsInDateRange(startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String): Flow<List<Lesson>> = flowOf(emptyList())
        override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean) {
            flow.value = flow.value.map { if (it.lesson.id in ids) it.copy(lesson = it.lesson.copy(isPaid = paid)) else it }
        }
        override fun getLessonsWithStudents(): Flow<List<LessonWithStudent>> = flow.asStateFlow()
        override fun getLessonsWithStudentsByStudent(studentId: Long): Flow<List<LessonWithStudent>> = flow.map { list -> list.filter { it.student.id == studentId } }
        override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String): Flow<List<LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<LessonWithStudent>> = flowOf(emptyList())
    }
}
