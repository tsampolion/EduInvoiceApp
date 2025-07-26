package gr.eduinvoice.ui.lesson

import androidx.lifecycle.SavedStateHandle
import gr.eduinvoice.MainDispatcherRule
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.student.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class LessonViewModelGroupTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val studentFlow = MutableStateFlow<List<Student>>(emptyList())
    private val lessonFlow = MutableStateFlow<List<Lesson>>(emptyList())
    private val groupFlow = MutableStateFlow<List<StudentGroup>>(emptyList())
    private val relations = mutableMapOf<Long, MutableList<Long>>()

    private val studentDao = FakeStudentDao(studentFlow)
    private val lessonDao = FakeLessonDao(lessonFlow)
    private val groupDao = FakeGroupDao(groupFlow, studentFlow, relations)

    private val studentRepository = StudentRepository(studentDao)
    private val repository = TutorBillingRepository(studentDao, lessonDao, groupDao)
    private val groupRepository = GroupRepository(groupDao)

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
        getStudentLessons = GetStudentLessons(repository),
        getLessonsWithStudents = GetLessonsWithStudents(lessonDao),
        getLessonsWithStudentsByStudentAndDateRange = GetLessonsWithStudentsByStudentAndDateRange(lessonDao),
        addLesson = AddLesson(repository),
        addGroupLesson = AddGroupLesson(repository),
        updateLesson = UpdateLesson(repository),
        deleteLesson = DeleteLesson(lessonDao),
        updateLessonPaidStatus = UpdateLessonPaidStatus(lessonDao),
        updateLessonInvoicedStatus = UpdateLessonInvoicedStatus(lessonDao),
        isLessonInvoiced = IsLessonInvoiced(lessonDao)
    )
    private val groupUseCases = GroupUseCases(
        insertGroup = InsertGroup(groupRepository),
        updateGroup = UpdateGroup(groupRepository),
        deleteGroup = DeleteGroup(groupRepository),
        getAllGroups = GetAllGroups(groupRepository),
        getGroupById = GetGroupById(groupRepository),
        addStudentToGroup = AddStudentToGroup(groupRepository),
        removeStudentFromGroup = RemoveStudentFromGroup(groupRepository),
        getGroupStudents = GetGroupStudents(groupRepository)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun selectGroupLoadsStudentsAndSetsGroupLesson() = runTest {
        val s1 = Student(id = 1, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        val s2 = Student(id = 2, name = "Bob", surname = "", parentMobile = "", className = "", rate = 15.0)
        studentFlow.value = listOf(s1, s2)
        val group = StudentGroup(id = 1, name = "G1")
        groupFlow.value = listOf(group)
        relations[1L] = mutableListOf(1L, 2L)

        val vm = LessonViewModel(SavedStateHandle(mapOf("lessonId" to 0L)), lessonUseCases, studentUseCases, groupUseCases)
        advanceUntilIdle()

        vm.updateSelectedGroup(1)
        advanceUntilIdle()

        assertEquals(1L, vm.uiState.value.selectedGroupId)
        assertEquals(true, vm.uiState.value.isGroupLesson)
        assertEquals(null, vm.uiState.value.selectedStudentId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun saveLessonInsertsOnePerGroupMember() = runTest {
        val s1 = Student(id = 1, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        val s2 = Student(id = 2, name = "Bob", surname = "", parentMobile = "", className = "", rate = 15.0)
        studentFlow.value = listOf(s1, s2)
        val group = StudentGroup(id = 1, name = "G1")
        groupFlow.value = listOf(group)
        relations[1L] = mutableListOf(1L, 2L)

        val vm = LessonViewModel(SavedStateHandle(mapOf("lessonId" to 0L)), lessonUseCases, studentUseCases, groupUseCases)
        advanceUntilIdle()

        vm.updateSelectedGroup(1)
        vm.saveLesson()
        advanceUntilIdle()

        assertEquals(2, lessonFlow.value.size)
        assertEquals(setOf(1L, 2L), lessonFlow.value.map { it.studentId }.toSet())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadExistingGroupLessonPopulatesState() = runTest {
        val lesson = Lesson(
            id = 1,
            studentId = 1,
            groupId = 2,
            date = "2024-01-01",
            startTime = "10:00",
            durationMinutes = 60
        )
        lessonFlow.value = listOf(lesson)
        groupFlow.value = listOf(StudentGroup(id = 2, name = "G2"))

        val vm = LessonViewModel(
            SavedStateHandle(mapOf("lessonId" to 1L)),
            lessonUseCases,
            studentUseCases,
            groupUseCases
        )
        advanceUntilIdle()

        assertEquals(2L, vm.uiState.value.selectedGroupId)
        assertEquals(true, vm.uiState.value.isGroupLesson)
    }

    class FakeStudentDao(private val flow: MutableStateFlow<List<Student>>) : StudentDao {
        override suspend fun insert(student: Student): Long {
            flow.value = flow.value + student
            return student.id
        }
        override suspend fun update(student: Student) {}
        override suspend fun delete(student: Student) {}
        override suspend fun softDeleteStudent(studentId: Long) {}
        override fun getStudentById(studentId: Long, userId: Long): Flow<Student?> = flow.map { list -> list.find { it.id == studentId } }
        override fun getAllActiveStudents(userId: Long): Flow<List<Student>> = flow.asStateFlow()
        override fun getArchivedStudents(userId: Long): Flow<List<Student>> = flowOf(emptyList())
        override suspend fun restoreStudent(studentId: Long) {}
        override fun getStudentByIdAny(studentId: Long, userId: Long): Flow<Student?> = getStudentById(studentId, userId)
        override suspend fun getActiveStudentCount(userId: Long): Int = flow.value.size
        override suspend fun classNameExists(name: String, userId: Long): Int = flow.value.count { it.className.equals(name, true) }
    }

    class FakeLessonDao(private val flow: MutableStateFlow<List<Lesson>>) : LessonDao {
        override suspend fun insert(lesson: Lesson): Long {
            val id = (flow.value.maxOfOrNull { it.id } ?: 0L) + 1
            flow.value = flow.value + lesson.copy(id = id)
            return id
        }
        override suspend fun update(lesson: Lesson) {}
        override suspend fun delete(lesson: Lesson) {}
        override suspend fun deleteById(lessonId: Long) {}
        override fun getLessonById(lessonId: Long, userId: Long): Flow<Lesson?> = flow.map { list -> list.find { it.id == lessonId } }
        override fun getLessonsByStudentId(studentId: Long, userId: Long): Flow<List<Lesson>> = flow.map { list -> list.filter { it.studentId == studentId } }
        override fun getAllLessons(userId: Long): Flow<List<Lesson>> = flow.asStateFlow()
        override fun getLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = flowOf(emptyList())
        override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean) {}
        override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean) {}
        override fun isLessonInvoiced(lessonId: Long, userId: Long): Flow<Boolean?> = flow.map { list -> list.find { it.id == lessonId }?.isInvoiced }
        override fun getLessonsWithStudents(userId: Long): Flow<List<gr.eduinvoice.data.database.LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsByStudent(studentId: Long, userId: Long): Flow<List<gr.eduinvoice.data.database.LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<gr.eduinvoice.data.database.LessonWithStudent>> = flowOf(emptyList())
        override fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<gr.eduinvoice.data.database.LessonWithStudent>> = flowOf(emptyList())
    }

    class FakeGroupDao(
        private val groups: MutableStateFlow<List<StudentGroup>>, 
        private val students: MutableStateFlow<List<Student>>, 
        private val refs: MutableMap<Long, MutableList<Long>>
    ) : GroupDao {
        override suspend fun insertGroup(group: StudentGroup): Long {
            val id = if (group.id == 0L) (groups.value.maxOfOrNull { it.id } ?: 0L) + 1 else group.id
            groups.value = groups.value + group.copy(id = id)
            return id
        }
        override suspend fun updateGroup(group: StudentGroup) {}
        override suspend fun deleteGroup(group: StudentGroup) {}
        override fun getAllGroups(userId: Long): Flow<List<StudentGroup>> = groups.asStateFlow()
        override fun getGroupById(id: Long, userId: Long): Flow<StudentGroup?> = groups.map { list -> list.find { it.id == id } }
        override suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) {
            refs.getOrPut(crossRef.groupId) { mutableListOf() }.apply {
                if (!contains(crossRef.studentId)) add(crossRef.studentId)
            }
        }
        override suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long) {}
        override fun getStudentsForGroup(groupId: Long, userId: Long): Flow<List<Student>> = students.map { list ->
            val ids = refs[groupId] ?: emptyList<Long>()
            list.filter { it.id in ids }
        }
    }
}
