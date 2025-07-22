package gr.tutorbilling.ui.invoice

import androidx.lifecycle.SavedStateHandle
import gr.tutorbilling.MainDispatcherRule
import gr.tutorbilling.data.dao.LessonDao
import gr.tutorbilling.data.dao.StudentDao
import gr.tutorbilling.data.dao.GroupDao
import gr.tutorbilling.data.model.StudentGroup
import gr.tutorbilling.data.model.GroupStudentCrossRef
import gr.tutorbilling.data.model.Lesson
import gr.tutorbilling.data.model.Student
import gr.tutorbilling.data.repository.StudentRepository
import gr.tutorbilling.data.repository.TutorBillingRepository
import gr.tutorbilling.domain.lesson.*
import gr.tutorbilling.domain.student.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InvoiceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val studentFlow = MutableStateFlow<List<Student>>(emptyList())
    private val lessonFlow = MutableStateFlow<List<Lesson>>(emptyList())

    private val studentDao = object : StudentDao {
        override suspend fun insert(student: Student): Long {
            studentFlow.value = studentFlow.value + student
            return student.id
        }
        override suspend fun update(student: Student) {}
        override suspend fun delete(student: Student) {}
        override suspend fun softDeleteStudent(studentId: Long) {}
        override fun getStudentById(studentId: Long) = studentFlow.map { list -> list.find { it.id == studentId } }
        override fun getAllActiveStudents() = studentFlow.asStateFlow()
        override fun getArchivedStudents() = flowOf(emptyList<Student>())
        override suspend fun restoreStudent(studentId: Long) {}
        override fun getStudentByIdAny(studentId: Long) = getStudentById(studentId)
        override suspend fun getActiveStudentCount() = studentFlow.value.size
        override suspend fun classNameExists(name: String) = 0
    }

    private val lessonDao = object : LessonDao {
        override suspend fun insert(lesson: Lesson): Long {
            lessonFlow.value = lessonFlow.value + lesson
            return lesson.id
        }
        override suspend fun update(lesson: Lesson) {}
        override suspend fun delete(lesson: Lesson) {}
        override suspend fun deleteById(lessonId: Long) {}
        override fun getLessonById(lessonId: Long) = lessonFlow.map { it.find { l -> l.id == lessonId } }
        override fun getLessonsByStudentId(studentId: Long) = lessonFlow.map { list -> list.filter { it.studentId == studentId } }
        override fun getAllLessons() = lessonFlow.asStateFlow()
        override fun getLessonsInDateRange(startDate: String, endDate: String) = flowOf(emptyList<Lesson>())
        override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String) = flowOf(emptyList<Lesson>())
        override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String) = flowOf(emptyList<Lesson>())
        override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String) = flowOf(emptyList<Lesson>())
        override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean) {
            lessonFlow.value = lessonFlow.value.map { if (it.id in ids) it.copy(isPaid = paid) else it }
        }
        override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean) {
            lessonFlow.value = lessonFlow.value.map { if (it.id in ids) it.copy(isInvoiced = invoiced) else it }
        }
        override fun isLessonInvoiced(lessonId: Long) = lessonFlow.map { list -> list.find { it.id == lessonId }?.isInvoiced }
        override fun getLessonsWithStudents() = flowOf(emptyList<gr.tutorbilling.data.database.LessonWithStudent>())
        override fun getLessonsWithStudentsByStudent(studentId: Long) = flowOf(emptyList<gr.tutorbilling.data.database.LessonWithStudent>())
        override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String) = flowOf(emptyList<gr.tutorbilling.data.database.LessonWithStudent>())
        override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String) = flowOf(emptyList<gr.tutorbilling.data.database.LessonWithStudent>())
    }

    private val groupDao = object : GroupDao {
        override suspend fun insertGroup(group: StudentGroup): Long = 0L
        override suspend fun updateGroup(group: StudentGroup) {}
        override suspend fun deleteGroup(group: StudentGroup) {}
        override fun getAllGroups() = flowOf(emptyList<StudentGroup>())
        override fun getGroupById(id: Long) = flowOf<StudentGroup?>(null)
        override suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) {}
        override suspend fun deleteCrossRef(groupId: Long, studentId: Long) {}
        override fun getStudentsForGroup(groupId: Long) = flowOf(emptyList<Student>())
    }

    private val studentRepository = StudentRepository(studentDao)
    private val tutorBillingRepository = TutorBillingRepository(studentDao, lessonDao, groupDao)

    private val studentUseCases = StudentUseCases(
        getActiveStudents = GetActiveStudents(studentRepository),
        getArchivedStudents = GetArchivedStudents(studentRepository),
        getStudentById = GetStudentById(studentRepository),
        insertStudent = InsertStudent(studentRepository),
        updateStudent = UpdateStudent(studentRepository),
        softDeleteStudent = SoftDeleteStudent(studentRepository),
        restoreStudent = RestoreStudent(studentRepository),
        getActiveStudentCount = GetActiveStudentCount(studentRepository),
        classNameExists = ClassNameExists(studentRepository)
    )

    private val lessonUseCases = LessonUseCases(
        getAllLessons = GetAllLessons(lessonDao),
        getLessonById = GetLessonById(lessonDao),
        getStudentLessons = GetStudentLessons(tutorBillingRepository),
        getLessonsWithStudents = GetLessonsWithStudents(lessonDao),
        getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(lessonDao),
        addLesson = AddLesson(tutorBillingRepository),
        addGroupLesson = AddGroupLesson(tutorBillingRepository),
        updateLesson = UpdateLesson(tutorBillingRepository),
        deleteLesson = DeleteLesson(lessonDao),
        updateLessonPaidStatus = UpdateLessonPaidStatus(lessonDao),
        updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(lessonDao),
        isLessonInvoiced = IsLessonInvoiced(lessonDao)
    )

    @Test
    fun markAsPaidMarksInvoiced() = runTest {
        val student = Student(id = 1, name = "Alice", surname = "", parentMobile = "", className = "A", rate = 10.0)
        studentFlow.value = listOf(student)
        val lesson = Lesson(id = 1, studentId = 1, date = "2024-01-01", startTime = "10:00", durationMinutes = 60)
        lessonFlow.value = listOf(lesson)

        val vm = InvoiceViewModel(SavedStateHandle(), lessonUseCases, studentUseCases)
        vm.markAsPaid(listOf(1))
        advanceUntilIdle()

        assertEquals(true, lessonFlow.value.first().isInvoiced)
    }
}
