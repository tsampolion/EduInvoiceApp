package gr.eduinvoice.ui.groups

import androidx.lifecycle.SavedStateHandle
import gr.eduinvoice.MainDispatcherRule
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.domain.student.*
import gr.eduinvoice.data.user.CurrentUserProvider
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
class GroupViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val studentFlow = MutableStateFlow<List<Student>>(emptyList())
    private val groupFlow = MutableStateFlow<List<StudentGroup>>(emptyList())
    private val relations = mutableMapOf<Long, MutableMap<Long, Long>>()

    private val userProvider = FakeUserProvider(5L)

    private val studentDao = FakeStudentDao(studentFlow)
    private val groupDao = FakeGroupDao(groupFlow, studentFlow, relations)
    private val studentRepository = StudentRepository(studentDao)
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
    fun toggleStudentUpdatesSelection() = runTest {
        val s1 = Student(id = 1, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        val s2 = Student(id = 2, name = "Bob", surname = "", parentMobile = "", className = "", rate = 15.0)
        studentFlow.value = listOf(s1, s2)

        val vm = GroupViewModel(groupUseCases, studentUseCases, SavedStateHandle(), userProvider)
        advanceUntilIdle()

        vm.toggleStudent(1)
        advanceUntilIdle()

        val selected = vm.uiState.value.students.find { it.id == 1L }?.selected
        assertEquals(true, selected)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun saveNewGroupInsertsGroupAndMembers() = runTest {
        val s1 = Student(id = 1, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        val s2 = Student(id = 2, name = "Bob", surname = "", parentMobile = "", className = "", rate = 15.0)
        studentFlow.value = listOf(s1, s2)

        val vm = GroupViewModel(groupUseCases, studentUseCases, SavedStateHandle(), userProvider)
        advanceUntilIdle()

        vm.updateName("Group A")
        vm.toggleStudent(1)
        vm.toggleStudent(2)
        vm.saveGroup()
        advanceUntilIdle()

        assertEquals(1, groupFlow.value.size)
        assertEquals(mapOf(1L to 5L, 2L to 5L), relations[1L])
    }

    class FakeStudentDao(private val flow: MutableStateFlow<List<Student>>) : StudentDao {
        override suspend fun insert(student: Student): Long {
            flow.value = flow.value + student
            return student.id
        }
        override suspend fun update(student: Student) {}
        override suspend fun delete(student: Student) {}
        override suspend fun softDeleteStudent(studentId: Long, userId: Long) {}
        override fun getStudentById(studentId: Long, userId: Long): Flow<Student?> =
            flow.map { list -> list.find { it.id == studentId && it.ownerId == userId } }
        override fun getAllActiveStudents(userId: Long): Flow<List<Student>> =
            flow.map { list -> list.filter { it.ownerId == userId } }
        override fun getArchivedStudents(userId: Long): Flow<List<Student>> = flowOf(emptyList())
        override suspend fun restoreStudent(studentId: Long, userId: Long) {}
        override fun getStudentByIdAny(studentId: Long, userId: Long): Flow<Student?> = getStudentById(studentId, userId)
        override suspend fun getActiveStudentCount(userId: Long): Int = flow.value.count { it.ownerId == userId }
        override suspend fun classNameExists(name: String, userId: Long): Int = flow.value.count { it.className.equals(name, true) }
    }

    class FakeGroupDao(
        private val groups: MutableStateFlow<List<StudentGroup>>,
        private val students: MutableStateFlow<List<Student>>,
        private val refs: MutableMap<Long, MutableMap<Long, Long>>
    ) : GroupDao {
        override suspend fun insertGroup(group: StudentGroup): Long {
            val id = if (group.id == 0L) (groups.value.maxOfOrNull { it.id } ?: 0L) + 1 else group.id
            groups.value = groups.value + group.copy(id = id)
            return id
        }
        override suspend fun updateGroup(group: StudentGroup) {
            groups.value = groups.value.map { if (it.id == group.id) group else it }
        }
        override suspend fun deleteGroup(group: StudentGroup) {
            groups.value = groups.value.filterNot { it.id == group.id }
            refs.remove(group.id)
        }
        override fun getAllGroups(userId: Long): Flow<List<StudentGroup>> =
            groups.map { list -> list.filter { it.ownerId == userId } }
        override fun getGroupById(id: Long, userId: Long): Flow<StudentGroup?> =
            groups.map { list -> list.find { it.id == id && it.ownerId == userId } }
        override suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) {
            refs.getOrPut(crossRef.groupId) { mutableMapOf() }[crossRef.studentId] = crossRef.ownerId
        }
        override suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long) {
            refs[groupId]?.remove(studentId)
        }
        override fun getStudentsForGroup(groupId: Long, userId: Long): Flow<List<Student>> = students.map { list ->
            val ids = refs[groupId]?.filter { it.value == userId }?.keys ?: emptySet()
            list.filter { it.id in ids }
        }
    }

    class FakeUserProvider(id: Long?) : CurrentUserProvider {
        private val _id = MutableStateFlow(id)
        override val loggedInUserId: Flow<Long?> = _id
    }
}
