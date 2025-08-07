package gr.eduinvoice.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.TestBase
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.LessonRepository
import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.domain.student.StudentUseCases
import gr.eduinvoice.domain.lesson.LessonUseCases
import gr.eduinvoice.domain.group.GroupUseCases
import gr.eduinvoice.domain.user.UserUseCases
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import gr.eduinvoice.infrastructure.TestConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Performance testing framework for large datasets and memory usage.
 * Tests scalability and responsiveness under load.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PerformanceTest : TestBase() {
    
    @get:Rule
    val databaseContainer = TestDatabaseContainer()
    
    private lateinit var database: EduInvoiceDatabase
    private lateinit var studentRepository: StudentRepository
    private lateinit var lessonRepository: LessonRepository
    private lateinit var groupRepository: GroupRepository
    private lateinit var userRepository: UserRepository
    
    private lateinit var studentUseCases: StudentUseCases
    private lateinit var lessonUseCases: LessonUseCases
    private lateinit var groupUseCases: GroupUseCases
    private lateinit var userUseCases: UserUseCases
    
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setUp() {
        database = databaseContainer.createTestDatabase()
        
        // Initialize repositories
        studentRepository = StudentRepository(database.studentDao())
        lessonRepository = LessonRepository(database.lessonDao())
        groupRepository = GroupRepository(database.groupDao())
        userRepository = UserRepository(database.userDao())
        
        // Initialize use cases
        studentUseCases = StudentUseCases(
            getActiveStudents = gr.eduinvoice.domain.student.GetActiveStudents(studentRepository),
            getArchivedStudents = gr.eduinvoice.domain.student.GetArchivedStudents(studentRepository),
            getStudentById = gr.eduinvoice.domain.student.GetStudentById(studentRepository),
            insertStudent = gr.eduinvoice.domain.student.InsertStudent(studentRepository),
            updateStudent = gr.eduinvoice.domain.student.UpdateStudent(studentRepository),
            softDeleteStudent = gr.eduinvoice.domain.student.SoftDeleteStudent(studentRepository),
            restoreStudent = gr.eduinvoice.domain.student.RestoreStudent(studentRepository),
            getActiveStudentCount = gr.eduinvoice.domain.student.GetActiveStudentCount(studentRepository),
            classNameExists = gr.eduinvoice.domain.student.ClassNameExists(studentRepository),
            getStudentsPaginated = gr.eduinvoice.domain.student.GetStudentsPaginated(studentRepository),
            searchStudentsPaginated = gr.eduinvoice.domain.student.SearchStudentsPaginated(studentRepository)
        )
        
        lessonUseCases = LessonUseCases(
            getAllLessons = gr.eduinvoice.domain.lesson.GetAllLessons(database.lessonDao()),
            getLessonById = gr.eduinvoice.domain.lesson.GetLessonById(database.lessonDao()),
            getStudentLessons = gr.eduinvoice.domain.lesson.GetStudentLessons(lessonRepository),
            getLessonsWithStudents = gr.eduinvoice.domain.lesson.GetLessonsWithStudents(database.lessonDao()),
            getLessonsWithStudentsByStudentAndDateRange = gr.eduinvoice.domain.lesson.GetLessonsWithStudentsByStudentAndDateRange(database.lessonDao()),
            addLesson = gr.eduinvoice.domain.lesson.AddLesson(lessonRepository),
            addGroupLesson = gr.eduinvoice.domain.lesson.AddGroupLesson(lessonRepository),
            updateLesson = gr.eduinvoice.domain.lesson.UpdateLesson(lessonRepository),
            deleteLesson = gr.eduinvoice.domain.lesson.DeleteLesson(database.lessonDao()),
            updateLessonPaidStatus = gr.eduinvoice.domain.lesson.UpdateLessonPaidStatus(database.lessonDao()),
            updateLessonInvoicedStatus = gr.eduinvoice.domain.lesson.UpdateLessonInvoicedStatus(database.lessonDao()),
            isLessonInvoiced = gr.eduinvoice.domain.lesson.IsLessonInvoiced(database.lessonDao()),
            getLessonsWithStudentsPaginated = gr.eduinvoice.domain.lesson.GetLessonsWithStudentsPaginated(database.lessonDao())
        )
        
        groupUseCases = GroupUseCases(
            insertGroup = gr.eduinvoice.domain.group.InsertGroup(groupRepository),
            updateGroup = gr.eduinvoice.domain.group.UpdateGroup(groupRepository),
            deleteGroup = gr.eduinvoice.domain.group.DeleteGroup(groupRepository),
            getAllGroups = gr.eduinvoice.domain.group.GetAllGroups(groupRepository),
            getGroupById = gr.eduinvoice.domain.group.GetGroupById(groupRepository),
            addStudentToGroup = gr.eduinvoice.domain.group.AddStudentToGroup(groupRepository),
            removeStudentFromGroup = gr.eduinvoice.domain.group.RemoveStudentFromGroup(groupRepository),
            getGroupStudents = gr.eduinvoice.domain.group.GetGroupStudents(groupRepository)
        )
        
        userUseCases = UserUseCases(
            createUser = gr.eduinvoice.domain.user.CreateUser(userRepository),
            authenticateUser = gr.eduinvoice.domain.user.AuthenticateUser(userRepository),
            getUserProfile = gr.eduinvoice.domain.user.GetUserProfile(userRepository),
            updateUser = gr.eduinvoice.domain.user.UpdateUser(userRepository),
            resetPassword = gr.eduinvoice.domain.user.ResetPassword(userRepository)
        )
    }
    
    @After
    fun tearDown() {
        databaseContainer.cleanupTestDatabase()
    }
    
    @Test
    fun testLargeDatasetPerformance() = runTest {
        val user = createTestUser()
        
        // Performance thresholds
        val maxInsertionTime = TestConfiguration.Performance.maxInsertionTime
        val maxQueryTime = TestConfiguration.Performance.maxQueryTime
        val maxMemoryUsage = TestConfiguration.Performance.maxMemoryUsage
        
        // Measure initial memory
        val initialMemory = getMemoryUsage()
        
        // Create large dataset
        val startTime = System.currentTimeMillis()
        
        val students = createLargeStudentDataset(user.id, TestConfiguration.DataSize.largeStudentCount)
        val lessons = createLargeLessonDataset(students, TestConfiguration.DataSize.largeLessonCount)
        val groups = createLargeGroupDataset(user.id, TestConfiguration.DataSize.largeGroupCount)
        
        val insertionTime = System.currentTimeMillis() - startTime
        
        // Verify insertion performance
        assertTrue("Insertion took too long: ${insertionTime}ms", insertionTime < maxInsertionTime)
        
        // Test query performance
        val queryStartTime = System.currentTimeMillis()
        val allStudents = studentUseCases.getActiveStudents(user.id).first()
        val queryTime = System.currentTimeMillis() - queryStartTime
        
        assertTrue("Query took too long: ${queryTime}ms", queryTime < maxQueryTime)
        assertEquals(TestConfiguration.DataSize.largeStudentCount, allStudents.size)
        
        // Test memory usage
        val finalMemory = getMemoryUsage()
        val memoryIncrease = finalMemory - initialMemory
        assertTrue("Memory usage too high: ${memoryIncrease} bytes", memoryIncrease < maxMemoryUsage)
    }
    
    @Test
    fun testMemoryUsage() = runTest {
        val user = createTestUser()
        val initialMemory = getMemoryUsage()
        
        // Create moderate dataset
        val students = createLargeStudentDataset(user.id, TestConfiguration.DataSize.mediumStudentCount)
        val lessons = createLargeLessonDataset(students, TestConfiguration.DataSize.mediumLessonCount)
        
        val memoryAfterData = getMemoryUsage()
        val memoryIncrease = memoryAfterData - initialMemory
        
        // Verify memory usage is reasonable
        assertTrue("Memory increase too high: ${memoryIncrease} bytes", 
                  memoryIncrease < TestConfiguration.Performance.maxMemoryUsage)
        
        // Test memory cleanup
        students.forEach { student ->
            studentUseCases.softDeleteStudent(student.id, user.id)
        }
        
        val memoryAfterCleanup = getMemoryUsage()
        val cleanupEfficiency = (memoryAfterData - memoryAfterCleanup) / memoryIncrease.toDouble()
        
        assertTrue("Memory cleanup efficiency too low: ${cleanupEfficiency}", 
                  cleanupEfficiency > TestConfiguration.Performance.minCleanupEfficiency)
    }
    
    @Test
    fun testDatabaseQueryPerformance() = runTest {
        val user = createTestUser()
        
        // Create test data
        val students = createLargeStudentDataset(user.id, TestConfiguration.DataSize.mediumStudentCount)
        val lessons = createLargeLessonDataset(students, TestConfiguration.DataSize.mediumLessonCount)
        
        // Test pagination performance
        val pageSize = TestConfiguration.Database.pageSize
        val maxPages = TestConfiguration.DataSize.mediumStudentCount / pageSize
        
        repeat(maxPages) { page ->
            val startTime = System.currentTimeMillis()
            val paginatedStudents = studentUseCases.getStudentsPaginated(user.id, pageSize, page * pageSize)
            val queryTime = System.currentTimeMillis() - startTime
            
            assertTrue("Pagination query ${page} took too long: ${queryTime}ms", 
                      queryTime < TestConfiguration.Performance.maxQueryTime)
            assertTrue("Page ${page} should have students", paginatedStudents.isNotEmpty())
        }
    }
    
    @Test
    fun testConcurrentOperationPerformance() = runTest {
        val user = createTestUser()
        val students = createLargeStudentDataset(user.id, TestConfiguration.DataSize.smallStudentCount)
        
        val concurrentOperations = TestConfiguration.Performance.concurrentOperations
        val startTime = System.currentTimeMillis()
        
        val jobs = (1..concurrentOperations).map { operationId ->
            async {
                val student = students[operationId % students.size]
                val updatedStudent = student.copy(name = "Updated_${operationId}")
                studentUseCases.updateStudent(updatedStudent)
                operationId
            }
        }
        
        val results = jobs.awaitAll()
        val totalTime = System.currentTimeMillis() - startTime
        
        assertEquals(concurrentOperations, results.size)
        assertTrue("Concurrent operations took too long: ${totalTime}ms", 
                  totalTime < TestConfiguration.Performance.maxConcurrentTime)
    }
    
    @Test
    fun testPaginationPerformance() = runTest {
        val user = createTestUser()
        val students = createLargeStudentDataset(user.id, TestConfiguration.DataSize.largeStudentCount)
        
        val pageSize = TestConfiguration.Database.pageSize
        val totalPages = (students.size + pageSize - 1) / pageSize
        
        val startTime = System.currentTimeMillis()
        
        repeat(totalPages) { page ->
            val offset = page * pageSize
            val paginatedStudents = studentUseCases.getStudentsPaginated(user.id, pageSize, offset)
            
            val expectedSize = if (page == totalPages - 1 && students.size % pageSize != 0) {
                students.size % pageSize
            } else {
                pageSize
            }
            
            assertEquals("Page ${page} should have ${expectedSize} students", 
                        expectedSize, paginatedStudents.size)
        }
        
        val totalTime = System.currentTimeMillis() - startTime
        assertTrue("Pagination took too long: ${totalTime}ms", 
                  totalTime < TestConfiguration.Performance.maxPaginationTime)
    }
    
    @Test
    fun testUIResponsivenessSimulation() = runTest {
        val user = createTestUser()
        val students = createLargeStudentDataset(user.id, TestConfiguration.DataSize.mediumStudentCount)
        
        // Simulate UI operations
        val uiOperations = TestConfiguration.UI.responsivenessOperations
        
        repeat(uiOperations) { operation ->
            val startTime = System.currentTimeMillis()
            
            // Simulate different UI operations
            when (operation % 4) {
                0 -> studentUseCases.getActiveStudents(user.id).first()
                1 -> studentUseCases.getActiveStudentCount(user.id)
                2 -> {
                    val student = students[operation % students.size]
                    studentUseCases.getStudentById(student.id, user.id).first()
                }
                3 -> {
                    val searchQuery = "Student_${operation % 10}"
                    studentUseCases.searchStudentsPaginated(user.id, searchQuery, 10, 0)
                }
            }
            
            val operationTime = System.currentTimeMillis() - startTime
            assertTrue("UI operation ${operation} took too long: ${operationTime}ms", 
                      operationTime < TestConfiguration.UI.maxResponseTime)
        }
    }
    
    // Helper functions
    private suspend fun createTestUser(): User {
        val user = User(
            username = "testuser_${System.currentTimeMillis()}",
            passwordHash = "test_hash",
            fullName = "Test User"
        )
        val userId = userUseCases.createUser(user)
        return user.copy(id = userId)
    }
    
    private suspend fun createLargeStudentDataset(ownerId: Long, count: Int): List<Student> {
        val students = mutableListOf<Student>()
        
        repeat(count) { index ->
            val student = Student(
                ownerId = ownerId,
                name = "Student_${index}",
                surname = "Surname_${index}",
                parentMobile = "+30${index.toString().padStart(9, '0')}",
                parentEmail = "parent${index}@test.com",
                className = "Class_${index % 10}",
                rate = 20.0 + (index % 30)
            )
            val studentId = studentUseCases.insertStudent(student)
            students.add(student.copy(id = studentId))
        }
        
        return students
    }
    
    private suspend fun createLargeLessonDataset(students: List<Student>, count: Int): List<Lesson> {
        val lessons = mutableListOf<Lesson>()
        val baseDate = LocalDate.now().minusDays(30)
        
        repeat(count) { index ->
            val student = students[index % students.size]
            val lessonDate = baseDate.plusDays(index % 30)
            val startTime = LocalTime.of(9 + (index % 8), 0)
            
            val lesson = Lesson.create(
                studentId = student.id,
                date = lessonDate,
                startTime = startTime,
                durationMinutes = 60,
                notes = "Lesson ${index}",
                ownerId = student.ownerId
            )
            
            val lessonId = lessonUseCases.addLesson(lesson)
            lessons.add(lesson.copy(id = lessonId))
        }
        
        return lessons
    }
    
    private suspend fun createLargeGroupDataset(ownerId: Long, count: Int): List<StudentGroup> {
        val groups = mutableListOf<StudentGroup>()
        
        repeat(count) { index ->
            val group = StudentGroup(
                ownerId = ownerId,
                name = "Group_${index}"
            )
            val groupId = groupUseCases.insertGroup(group)
            groups.add(group.copy(id = groupId))
        }
        
        return groups
    }
    
    private fun getMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
} 
