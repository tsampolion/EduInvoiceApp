package gr.tutorbilling.ui.groups

import androidx.lifecycle.SavedStateHandle
import gr.tutorbilling.MainDispatcherRule
import gr.tutorbilling.data.dao.GroupDao
import gr.tutorbilling.data.dao.StudentDao
import gr.tutorbilling.data.model.GroupStudentCrossRef
import gr.tutorbilling.data.model.Student
import gr.tutorbilling.data.model.StudentGroup
import gr.tutorbilling.data.repository.GroupRepository
import gr.tutorbilling.data.repository.StudentRepository
import gr.tutorbilling.domain.group.*
import gr.tutorbilling.domain.student.*
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
    private val relations = mutableMapOf<Long, MutableList<Long>>()

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

    @Test
    fun toggleStudentUpdatesSelection() = runTest {
        val s1 = Student(id = 1, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        val s2 = Student(id = 2, name = "Bob", surname = "", parentMobile = "", className = "", rate = 15.0)
        studentFlow.value = listOf(s1, s2)

        val vm = GroupViewModel(groupUseCases, studentUseCases, SavedStateHandle())
        advanceUntilIdle()

        vm.toggleStudent(1)
        advanceUntilIdle()

        val selected = vm.uiState.value.students.find { it.id == 1L }?.selected
        assertEquals(true, selected)
    }

    @Test
    fun saveNewGroupInsertsGroupAndMembers() = runTest {
        val s1 = Student(id = 1, name = "Alice", surname = "", parentMobile = "", className = "", rate = 10.0)
        val s2 = Student(id = 2, name = "Bob", surname = "", parentMobile = "", className = "", rate = 15.0)
        studentFlow.value = listOf(s1, s2)

        val vm = GroupViewModel(groupUseCases, studentUseCases, SavedStateHandle())
        advanceUntilIdle()

        vm.updateName("Group A")
        vm.toggleStudent(1)
        vm.toggleStudent(2)
        vm.saveGroup()
        advanceUntilIdle()

        assertEquals(1, groupFlow.value.size)
        assertEquals(setOf(1L, 2L), relations[1L]?.toSet())
    }

    class FakeStudentDao(private val flow: MutableStateFlow<List<Student>>) : StudentDao {
        override suspend fun insert(student: Student): Long {
            flow.value = flow.value + student
            return student.id
        }
        override suspend fun update(student: Student) {}
        override suspend fun delete(student: Student) {}
        override suspend fun softDeleteStudent(studentId: Long) {}
        override fun getStudentById(studentId: Long): Flow<Student?> = flow.map { list -> list.find { it.id == studentId } }
        override fun getAllActiveStudents(): Flow<List<Student>> = flow.asStateFlow()
        override fun getArchivedStudents(): Flow<List<Student>> = flowOf(emptyList())
        override suspend fun restoreStudent(studentId: Long) {}
        override fun getStudentByIdAny(studentId: Long): Flow<Student?> = getStudentById(studentId)
        override suspend fun getActiveStudentCount(): Int = flow.value.size
        override suspend fun classNameExists(name: String): Int = flow.value.count { it.className.equals(name, true) }
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
        override suspend fun updateGroup(group: StudentGroup) {
            groups.value = groups.value.map { if (it.id == group.id) group else it }
        }
        override suspend fun deleteGroup(group: StudentGroup) {
            groups.value = groups.value.filterNot { it.id == group.id }
            refs.remove(group.id)
        }
        override fun getAllGroups(): Flow<List<StudentGroup>> = groups.asStateFlow()
        override fun getGroupById(id: Long): Flow<StudentGroup?> = groups.map { list -> list.find { it.id == id } }
        override suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) {
            refs.getOrPut(crossRef.groupId) { mutableListOf() }.apply {
                if (!contains(crossRef.studentId)) add(crossRef.studentId)
            }
        }
        override suspend fun deleteCrossRef(groupId: Long, studentId: Long) {
            refs[groupId]?.remove(studentId)
        }
        override fun getStudentsForGroup(groupId: Long): Flow<List<Student>> = students.map { list ->
            val ids = refs[groupId] ?: emptyList<Long>()
            list.filter { it.id in ids }
        }
    }
}
