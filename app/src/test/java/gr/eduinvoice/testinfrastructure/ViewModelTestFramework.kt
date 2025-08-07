package gr.eduinvoice.testinfrastructure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.eduinvoice.data.concurrency.ConcurrencyController
import gr.eduinvoice.data.concurrency.OperationType
import gr.eduinvoice.data.concurrency.OperationPriority
import gr.eduinvoice.data.concurrency.TransactionIsolationLevel
import gr.eduinvoice.data.concurrency.ConcurrencyStats
import gr.eduinvoice.data.concurrency.HealthCheckResult
import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.TutorBillingRepository
import gr.eduinvoice.data.user.CurrentUserProvider
import gr.eduinvoice.domain.group.*
import gr.eduinvoice.domain.lesson.*
import gr.eduinvoice.domain.student.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.Result

/**
 * Creates a mock ConcurrencyController for testing
 */
fun createMockConcurrencyController(): ConcurrencyController {
    return mockk<ConcurrencyController>(relaxed = true) {
        coEvery { 
            executeSafeOperation<Any>(any(), any(), any(), any(), any(), any()) 
        } answers {
            val operation = firstArg<suspend () -> Any>()
            try {
                runBlocking { Result.success(operation()) }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        
        coEvery { 
            executeReadOnlyOperation<Any>(any(), any()) 
        } answers {
            val operation = firstArg<suspend () -> Any>()
            try {
                runBlocking { Result.success(operation()) }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        
        coEvery { 
            executeBatchSafeOperations<Any>(any(), any(), any(), any(), any()) 
        } answers {
            val operations = firstArg<List<suspend () -> Any>>()
            try {
                val results = mutableListOf<Any>()
                runBlocking {
                    for (operation in operations) {
                        results.add(operation())
                    }
                }
                Result.success(results)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        
        coEvery { performHealthCheck() } returns HealthCheckResult(isHealthy = true)
        every { getConcurrencyStatistics() } returns ConcurrencyStats()
        every { getActiveResourceLocks() } returns emptySet()
        coEvery { releaseAllResourceLocks() } returns Unit
        coEvery { emergencyCleanup() } returns Unit
    }
}

/**
 * Enhanced ViewModel testing framework with proper state management
 */
abstract class ViewModelTestBase : TestInfrastructure() {
    
    @get:org.junit.Rule
    val testDispatcherRule = EnhancedTestDispatcherRule(UnconfinedTestDispatcher())
    
    protected val testDataManager = TestDataManager()
    
    /**
     * Initialize test environment
     */
    protected fun initializeTestEnvironment() {
        testDataManager.clearTestData()
    }
    
    /**
     * Clean up test environment
     */
    protected fun cleanupTestEnvironment() {
        testDataManager.clearTestData()
    }
    
    /**
     * Assert ViewModel state
     */
    protected fun <T : ViewModel> assertViewModelState(
        viewModel: T,
        assertion: (T) -> Boolean,
        message: String = "ViewModel state assertion failed"
    ) {
        org.junit.Assert.assertTrue(message, assertion(viewModel))
    }
}

/**
 * Enhanced Fake Student DAO with proper state synchronization
 */
class EnhancedFakeStudentDao(
    private val studentFlow: MutableStateFlow<List<Student>>
) : StudentDao {
    
    override suspend fun insert(student: Student): Long {
        val newId = (studentFlow.value.maxOfOrNull { it.id } ?: 0L) + 1
        val newStudent = student.copy(id = newId)
        studentFlow.value = studentFlow.value + newStudent
        return newId
    }
    
    override suspend fun update(student: Student) {
        studentFlow.value = studentFlow.value.map { 
            if (it.id == student.id) student else it 
        }
    }
    
    override suspend fun delete(student: Student) {
        studentFlow.value = studentFlow.value.filter { it.id != student.id }
    }
    
    override suspend fun softDeleteStudent(studentId: Long, userId: Long) {
        studentFlow.value = studentFlow.value.map { student ->
            if (student.id == studentId && student.ownerId == userId) {
                student.copy(isActive = false)
            } else {
                student
            }
        }
    }
    
    override fun getStudentById(studentId: Long, userId: Long): Flow<Student?> = 
        studentFlow.map { students -> students.find { it.id == studentId && it.ownerId == userId } }
    
    override fun getAllActiveStudents(userId: Long): Flow<List<Student>> = 
        studentFlow.map { students -> students.filter { it.ownerId == userId && it.isActive } }
    
    override fun getArchivedStudents(userId: Long): Flow<List<Student>> = 
        studentFlow.map { students -> students.filter { it.ownerId == userId && !it.isActive } }
    
    override suspend fun restoreStudent(studentId: Long, userId: Long) {
        studentFlow.value = studentFlow.value.map { student ->
            if (student.id == studentId && student.ownerId == userId) {
                student.copy(isActive = true)
            } else {
                student
            }
        }
    }
    
    override fun getStudentByIdAny(studentId: Long, userId: Long): Flow<Student?> = 
        getStudentById(studentId, userId)
    
    override suspend fun getActiveStudentCount(userId: Long): Int = 
        studentFlow.value.count { it.ownerId == userId && it.isActive }
    
    override suspend fun classNameExists(name: String, userId: Long): Int = 
        studentFlow.value.count { it.className.equals(name, true) && it.ownerId == userId }
    
    override suspend fun getStudentsPaginated(userId: Long, limit: Int, offset: Int): List<Student> {
        return studentFlow.value
            .filter { it.ownerId == userId && it.isActive }
            .sortedBy { it.name }
            .drop(offset)
            .take(limit)
    }
    
    override suspend fun searchStudentsPaginated(userId: Long, searchQuery: String, limit: Int, offset: Int): List<Student> {
        return studentFlow.value
            .filter { 
                it.ownerId == userId && it.isActive &&
                (it.name.contains(searchQuery, true) || it.className.contains(searchQuery, true))
            }
            .sortedBy { it.name }
            .drop(offset)
            .take(limit)
    }
}

/**
 * Enhanced Fake Lesson DAO with proper state synchronization
 */
class EnhancedFakeLessonDao(
    private val lessonFlow: MutableStateFlow<List<Lesson>>,
    private val lessonWithStudentFlow: MutableStateFlow<List<LessonWithStudent>>
) : LessonDao {
    
    override suspend fun insert(lesson: Lesson): Long {
        val newId = (lessonFlow.value.maxOfOrNull { it.id } ?: 0L) + 1
        val newLesson = lesson.copy(id = newId)
        lessonFlow.value = lessonFlow.value + newLesson
        return newId
    }
    
    override suspend fun update(lesson: Lesson) {
        lessonFlow.value = lessonFlow.value.map { 
            if (it.id == lesson.id) lesson else it 
        }
    }
    
    override suspend fun delete(lesson: Lesson) {
        lessonFlow.value = lessonFlow.value.filter { it.id != lesson.id }
    }
    
    override suspend fun deleteById(lessonId: Long, userId: Long) {
        lessonFlow.value = lessonFlow.value.filter { 
            !(it.id == lessonId && it.ownerId == userId) 
        }
    }
    
    override fun getLessonById(lessonId: Long, userId: Long): Flow<Lesson?> = 
        lessonFlow.map { lessons -> lessons.find { it.id == lessonId && it.ownerId == userId } }
    
    override fun getLessonsByStudentId(studentId: Long, userId: Long): Flow<List<Lesson>> = 
        lessonFlow.map { lessons -> lessons.filter { it.studentId == studentId && it.ownerId == userId } }
    
    override fun getAllLessons(userId: Long): Flow<List<Lesson>> = 
        lessonFlow.map { lessons -> lessons.filter { it.ownerId == userId } }
    
    override fun getLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = 
        lessonFlow.map { lessons -> 
            lessons.filter { 
                it.ownerId == userId && it.date in startDate..endDate 
            } 
        }
    
    override fun getLessonsByStudentAndDateRange(
        studentId: Long, 
        startDate: String, 
        endDate: String, 
        userId: Long
    ): Flow<List<Lesson>> = lessonFlow.map { lessons -> 
        lessons.filter { 
            it.studentId == studentId && it.ownerId == userId && it.date in startDate..endDate 
        } 
    }
    
    override fun getUnpaidLessonsByStudentAndDateRange(
        studentId: Long, 
        startDate: String, 
        endDate: String, 
        userId: Long
    ): Flow<List<Lesson>> = lessonFlow.map { lessons -> 
        lessons.filter { 
            it.studentId == studentId && it.ownerId == userId && 
            it.date in startDate..endDate && !it.isPaid 
        } 
    }
    
    override fun getUnpaidLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> = 
        lessonFlow.map { lessons -> 
            lessons.filter { 
                it.ownerId == userId && it.date in startDate..endDate && !it.isPaid 
            } 
        }
    
    override suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean, userId: Long) {
        lessonFlow.value = lessonFlow.value.map { lesson ->
            if (lesson.id in ids && lesson.ownerId == userId) {
                lesson.copy(isPaid = paid)
            } else {
                lesson
            }
        }
    }
    
    override suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean, userId: Long) {
        lessonFlow.value = lessonFlow.value.map { lesson ->
            if (lesson.id in ids && lesson.ownerId == userId) {
                lesson.copy(isInvoiced = invoiced)
            } else {
                lesson
            }
        }
    }
    
    override fun isLessonInvoiced(lessonId: Long, userId: Long): Flow<Boolean?> = 
        lessonFlow.map { lessons -> 
            lessons.find { it.id == lessonId && it.ownerId == userId }?.isInvoiced 
        }
    
    override fun getLessonsWithStudents(userId: Long): Flow<List<LessonWithStudent>> = 
        lessonWithStudentFlow.map { lessons -> lessons.filter { it.lesson.ownerId == userId } }
    
    override fun getLessonsWithStudentsByStudent(studentId: Long, userId: Long): Flow<List<LessonWithStudent>> = 
        lessonWithStudentFlow.map { lessons -> 
            lessons.filter { it.student.id == studentId && it.lesson.ownerId == userId } 
        }
    
    override fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<LessonWithStudent>> = 
        lessonWithStudentFlow.map { lessons -> 
            lessons.filter { 
                it.lesson.ownerId == userId && it.lesson.date in startDate..endDate 
            } 
        }
    
    override fun getLessonsWithStudentsByStudentAndDateRange(
        studentId: Long, 
        startDate: String, 
        endDate: String, 
        userId: Long
    ): Flow<List<LessonWithStudent>> = lessonWithStudentFlow.map { lessons -> 
        lessons.filter { 
            it.student.id == studentId && it.lesson.ownerId == userId && 
            it.lesson.date in startDate..endDate 
        } 
    }
    
    override suspend fun insertGroupLessons(lessons: List<Lesson>): List<Long> {
        val ids = mutableListOf<Long>()
        lessons.forEach { lesson ->
            val id = insert(lesson)
            ids.add(id)
        }
        return ids
    }
    
    override suspend fun getLessonsWithStudentsPaginated(userId: Long, limit: Int, offset: Int): List<LessonWithStudent> {
        return lessonWithStudentFlow.value
            .filter { it.lesson.ownerId == userId }
            .sortedWith(compareByDescending<LessonWithStudent> { it.lesson.date }
                .thenByDescending { it.lesson.startTime })
            .drop(offset)
            .take(limit)
    }
}

/**
 * Enhanced Fake Group DAO with proper state synchronization
 */
class EnhancedFakeGroupDao(
    private val groupFlow: MutableStateFlow<List<StudentGroup>>,
    private val studentFlow: MutableStateFlow<List<Student>>,
    private val groupStudentRelations: MutableMap<Long, MutableSet<Long>>
) : GroupDao {
    
    override suspend fun insertGroup(group: StudentGroup): Long {
        val newId = if (group.id == 0L) {
            (groupFlow.value.maxOfOrNull { it.id } ?: 0L) + 1
        } else {
            group.id
        }
        val newGroup = group.copy(id = newId)
        groupFlow.value = groupFlow.value + newGroup
        return newId
    }
    
    override suspend fun updateGroup(group: StudentGroup) {
        groupFlow.value = groupFlow.value.map { 
            if (it.id == group.id) group else it 
        }
    }
    
    override suspend fun deleteGroup(group: StudentGroup) {
        groupFlow.value = groupFlow.value.filter { it.id != group.id }
        groupStudentRelations.remove(group.id)
    }
    
    override fun getAllGroups(userId: Long): Flow<List<StudentGroup>> = 
        groupFlow.map { groups -> groups.filter { it.ownerId == userId } }
    
    override fun getGroupById(id: Long, userId: Long): Flow<StudentGroup?> = 
        groupFlow.map { groups -> groups.find { it.id == id && it.ownerId == userId } }
    
    override suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) {
        groupStudentRelations.getOrPut(crossRef.groupId) { mutableSetOf() }.add(crossRef.studentId)
    }
    
    override suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long) {
        groupStudentRelations[groupId]?.remove(studentId)
    }
    
    override fun getStudentsForGroup(groupId: Long, userId: Long): Flow<List<Student>> = 
        studentFlow.map { students ->
            val studentIds = groupStudentRelations[groupId] ?: emptySet()
            students.filter { it.id in studentIds && it.ownerId == userId }
        }
}

/**
 * Enhanced Fake User Provider
 */
class EnhancedFakeUserProvider(
    private val userId: Long?
) : CurrentUserProvider {
    
    private val _loggedInUserId = MutableStateFlow(userId)
    
    override val loggedInUserId: Flow<Long?> = _loggedInUserId.asStateFlow()
    
    fun setUserId(newUserId: Long?) {
        _loggedInUserId.value = newUserId
    }
}

/**
 * ViewModel test utilities
 */
object ViewModelTestUtils {
    
    /**
     * Create enhanced fake DAOs with coordinated state management
     */
    fun createEnhancedFakeDaos(
        testDataManager: TestDataManager
    ): Triple<EnhancedFakeStudentDao, EnhancedFakeLessonDao, EnhancedFakeGroupDao> {
        val studentDao = EnhancedFakeStudentDao(testDataManager.studentFlow as MutableStateFlow)
        val lessonDao = EnhancedFakeLessonDao(
            testDataManager.lessonFlow as MutableStateFlow,
            testDataManager.lessonWithStudentFlow as MutableStateFlow
        )
        
        // Use the shared group-student relations map from TestDataManager
        val groupStudentRelations = testDataManager.getGroupStudentRelations()
        
        val groupDao = EnhancedFakeGroupDao(
            testDataManager.groupFlow as MutableStateFlow,
            testDataManager.studentFlow as MutableStateFlow,
            groupStudentRelations
        )
        
        return Triple(studentDao, lessonDao, groupDao)
    }
    
    /**
     * Create use cases with enhanced fake DAOs
     */
    fun createEnhancedUseCases(
        studentDao: EnhancedFakeStudentDao,
        lessonDao: EnhancedFakeLessonDao,
        groupDao: EnhancedFakeGroupDao
    ): Triple<StudentUseCases, LessonUseCases, GroupUseCases> {
        val studentRepository = StudentRepository(studentDao)
        val mockConcurrencyController = createMockConcurrencyController()
        val repository = TutorBillingRepository(studentDao, lessonDao, groupDao, mockConcurrencyController)
        val groupRepository = GroupRepository(groupDao)
        
        val studentUseCases = StudentUseCases(
            getActiveStudents = GetActiveStudents(studentRepository),
            getArchivedStudents = GetArchivedStudents(studentRepository),
            getStudentById = GetStudentById(studentRepository),
            insertStudent = InsertStudent(studentRepository),
            updateStudent = UpdateStudent(studentRepository),
            softDeleteStudent = SoftDeleteStudent(studentRepository),
            restoreStudent = RestoreStudent(studentRepository),
            getActiveStudentCount = GetActiveStudentCount(studentRepository),
            classNameExists = ClassNameExists(studentRepository),
            getStudentsPaginated = GetStudentsPaginated(studentRepository),
            searchStudentsPaginated = SearchStudentsPaginated(studentRepository)
        )
        
        val lessonUseCases = LessonUseCases(
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
            isLessonInvoiced = IsLessonInvoiced(lessonDao),
            getLessonsWithStudentsPaginated = GetLessonsWithStudentsPaginated(lessonDao)
        )
        
        val groupUseCases = GroupUseCases(
            insertGroup = InsertGroup(groupRepository),
            updateGroup = UpdateGroup(groupRepository),
            deleteGroup = DeleteGroup(groupRepository),
            getAllGroups = GetAllGroups(groupRepository),
            getGroupById = GetGroupById(groupRepository),
            addStudentToGroup = AddStudentToGroup(groupRepository),
            removeStudentFromGroup = RemoveStudentFromGroup(groupRepository),
            getGroupStudents = GetGroupStudents(groupRepository)
        )
        
        return Triple(studentUseCases, lessonUseCases, groupUseCases)
    }
} 