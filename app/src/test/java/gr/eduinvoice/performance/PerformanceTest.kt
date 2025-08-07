package gr.eduinvoice.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.TestBase
import gr.eduinvoice.data.database.EduInvoiceDatabase
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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
 * Comprehensive performance tests for large datasets and memory usage.
 * Tests scalability and performance under load.
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
            getAllGroups = gr.eduinvoice.domain.group.GetAllGroups(groupRepository),
            getGroupById = gr.eduinvoice.domain.group.GetGroupById(groupRepository),
            insertGroup = gr.eduinvoice.domain.group.InsertGroup(groupRepository),
            updateGroup = gr.eduinvoice.domain.group.UpdateGroup(groupRepository),
            deleteGroup = gr.eduinvoice.domain.group.DeleteGroup(groupRepository),
            getStudentsForGroup = gr.eduinvoice.domain.group.GetStudentsForGroup(groupRepository),
            addStudentToGroup = gr.eduinvoice.domain.group.AddStudentToGroup(groupRepository),
            removeStudentFromGroup = gr.eduinvoice.domain.group.RemoveStudentFromGroup(groupRepository)
        )
        
        userUseCases = UserUseCases(
            getUserById = gr.eduinvoice.domain.user.GetUserById(userRepository),
            insertUser = gr.eduinvoice.domain.user.InsertUser(userRepository),
            updateUser = gr.eduinvoice.domain.user.UpdateUser(userRepository),
            deleteUser = gr.eduinvoice.domain.user.DeleteUser(userRepository),
            validateUserCredentials = gr.eduinvoice.domain.user.ValidateUserCredentials(userRepository)
        )
    }
    
    @After
    fun tearDown() {
        databaseContainer.cleanupTestDatabase()
    }
    
    @Test
    fun testLargeDatasetPerformance() = runTest {
        // Populate large dataset
        val startTime = System.currentTimeMillis()
        databaseContainer.populateLargeDataset(database, 1L, 1000)
        val populationTime = System.currentTimeMillis() - startTime
        
        println("Dataset population time: ${populationTime}ms")
        assertTrue("Dataset population should complete within 10 seconds", populationTime < 10000)
        
        // Test student retrieval performance
        val studentStartTime = System.currentTimeMillis()
        val students = studentUseCases.getActiveStudents(1L).first()
        val studentTime = System.currentTimeMillis() - studentStartTime
        
        println("Student retrieval time: ${studentTime}ms for ${students.size} students")
        assertTrue("Student retrieval should complete within 1 second", studentTime < 1000)
        assertEquals("Should retrieve all 1000 students", 1000, students.size)
        
        // Test paginated student retrieval
        val paginatedStartTime = System.currentTimeMillis()
        val paginatedStudents = studentUseCases.getStudentsPaginated(1L, 0, 50)
        val paginatedTime = System.currentTimeMillis() - paginatedStartTime
        
        println("Paginated student retrieval time: ${paginatedTime}ms")
        assertTrue("Paginated retrieval should complete within 500ms", paginatedTime < 500)
        assertEquals("Should retrieve 50 students", 50, paginatedStudents.size)
        
        // Test lesson retrieval performance
        val lessonStartTime = System.currentTimeMillis()
        val lessons = lessonUseCases.getAllLessons(1L).first()
        val lessonTime = System.currentTimeMillis() - lessonStartTime
        
        println("Lesson retrieval time: ${lessonTime}ms for ${lessons.size} lessons")
        assertTrue("Lesson retrieval should complete within 2 seconds", lessonTime < 2000)
        assertEquals("Should retrieve all lessons", 5000, lessons.size)
    }
    
    @Test
    fun testMemoryUsage() = runTest {
        // Get initial memory usage
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        
        // Populate dataset
        databaseContainer.populateLargeDataset(database, 1L, 5000)
        
        // Get memory after population
        val afterPopulationMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryIncrease = afterPopulationMemory - initialMemory
        
        println("Memory usage - Initial: ${initialMemory / 1024 / 1024}MB, After population: ${afterPopulationMemory / 1024 / 1024}MB")
        println("Memory increase: ${memoryIncrease / 1024 / 1024}MB")
        
        // Memory usage should be reasonable (less than 100MB increase)
        assertTrue("Memory increase should be less than 100MB", memoryIncrease < 100 * 1024 * 1024)
        
        // Test memory usage during operations
        val operationStartMemory = runtime.totalMemory() - runtime.freeMemory()
        
        // Perform multiple operations
        repeat(100) {
            studentUseCases.getStudentsPaginated(1L, it * 50, 50)
            lessonUseCases.getLessonsWithStudents(1L).first()
        }
        
        val operationEndMemory = runtime.totalMemory() - runtime.freeMemory()
        val operationMemoryIncrease = operationEndMemory - operationStartMemory
        
        println("Memory increase during operations: ${operationMemoryIncrease / 1024 / 1024}MB")
        
        // Memory should not increase significantly during operations
        assertTrue("Memory should not increase significantly during operations", operationMemoryIncrease < 50 * 1024 * 1024)
    }
    
    @Test
    fun testConcurrentOperationsPerformance() = runTest {
        // Populate dataset
        databaseContainer.populateLargeDataset(database, 1L, 1000)
        
        val startTime = System.currentTimeMillis()
        
        // Perform concurrent operations
        val operations = (1..10).map { index ->
            async {
                when (index % 4) {
                    0 -> studentUseCases.getStudentsPaginated(1L, (index * 10) % 1000, 50)
                    1 -> lessonUseCases.getLessonsWithStudents(1L).first()
                    2 -> groupUseCases.getAllGroups(1L).first()
                    3 -> userUseCases.getUserById(1L)
                }
            }
        }
        
        val results = operations.awaitAll()
        val totalTime = System.currentTimeMillis() - startTime
        
        println("Concurrent operations time: ${totalTime}ms")
        assertTrue("Concurrent operations should complete within 5 seconds", totalTime < 5000)
        
        // Verify all operations completed successfully
        assertTrue("All operations should complete successfully", results.all { it != null })
    }
    
    @Test
    fun testSearchPerformance() = runTest {
        // Populate dataset
        databaseContainer.populateLargeDataset(database, 1L, 1000)
        
        // Test search performance
        val searchStartTime = System.currentTimeMillis()
        val searchResults = studentUseCases.searchStudentsPaginated(1L, "Student", 0, 100)
        val searchTime = System.currentTimeMillis() - searchStartTime
        
        println("Search time: ${searchTime}ms for query 'Student'")
        assertTrue("Search should complete within 1 second", searchTime < 1000)
        
        // Test search with different patterns
        val patterns = listOf("Student1", "Student100", "Student999", "NonExistent")
        
        patterns.forEach { pattern ->
            val patternStartTime = System.currentTimeMillis()
            val patternResults = studentUseCases.searchStudentsPaginated(1L, pattern, 0, 100)
            val patternTime = System.currentTimeMillis() - patternStartTime
            
            println("Search time for '$pattern': ${patternTime}ms, results: ${patternResults.size}")
            assertTrue("Search for '$pattern' should complete within 1 second", patternTime < 1000)
        }
    }
    
    @Test
    fun testDatabaseQueryOptimization() = runTest {
        // Populate dataset
        databaseContainer.populateLargeDataset(database, 1L, 1000)
        
        // Test optimized queries
        val queries = listOf(
            { studentUseCases.getStudentsPaginated(1L, 0, 50) },
            { lessonUseCases.getLessonsWithStudentsPaginated(1L, 0, 50) },
            { groupUseCases.getAllGroups(1L).first() }
        )
        
        queries.forEachIndexed { index, query ->
            val startTime = System.currentTimeMillis()
            val result = query()
            val time = System.currentTimeMillis() - startTime
            
            println("Query $index time: ${time}ms")
            assertTrue("Query $index should complete within 500ms", time < 500)
        }
    }
    
    @Test
    fun testBulkOperationsPerformance() = runTest {
        // Test bulk insert performance
        val bulkStudents = (1..100).map { index ->
            gr.eduinvoice.data.model.Student(
                id = 0,
                name = "BulkStudent$index",
                surname = "BulkSurname$index",
                parentMobile = "123456789$index",
                className = "BulkClass${index % 10}",
                rate = 20.0 + (index % 30),
                ownerId = 1L
            )
        }
        
        val insertStartTime = System.currentTimeMillis()
        bulkStudents.forEach { student ->
            studentUseCases.insertStudent(student)
        }
        val insertTime = System.currentTimeMillis() - insertStartTime
        
        println("Bulk insert time: ${insertTime}ms for 100 students")
        assertTrue("Bulk insert should complete within 5 seconds", insertTime < 5000)
        
        // Test bulk update performance
        val updateStartTime = System.currentTimeMillis()
        val studentsToUpdate = studentUseCases.getActiveStudents(1L).first().take(50)
        studentsToUpdate.forEach { student ->
            studentUseCases.updateStudent(student.copy(rate = student.rate + 1.0))
        }
        val updateTime = System.currentTimeMillis() - updateStartTime
        
        println("Bulk update time: ${updateTime}ms for 50 students")
        assertTrue("Bulk update should complete within 3 seconds", updateTime < 3000)
    }
    
    @Test
    fun testPaginationPerformance() = runTest {
        // Populate dataset
        databaseContainer.populateLargeDataset(database, 1L, 1000)
        
        val pageSizes = listOf(10, 25, 50, 100)
        
        pageSizes.forEach { pageSize ->
            val startTime = System.currentTimeMillis()
            
            // Test multiple pages
            repeat(5) { page ->
                studentUseCases.getStudentsPaginated(1L, page * pageSize, pageSize)
            }
            
            val time = System.currentTimeMillis() - startTime
            println("Pagination time for page size $pageSize: ${time}ms")
            assertTrue("Pagination with page size $pageSize should complete within 2 seconds", time < 2000)
        }
    }
    
    @Test
    fun testMemoryPressureHandling() = runTest {
        // Simulate memory pressure by creating large objects
        val largeObjects = mutableListOf<String>()
        
        try {
            // Create large strings to simulate memory pressure
            repeat(1000) { index ->
                largeObjects.add("Large string $index".repeat(1000))
            }
            
            // Perform operations under memory pressure
            val startTime = System.currentTimeMillis()
            databaseContainer.populateLargeDataset(database, 1L, 500)
            val populationTime = System.currentTimeMillis() - startTime
            
            println("Population time under memory pressure: ${populationTime}ms")
            assertTrue("Population should complete even under memory pressure", populationTime < 15000)
            
            // Test operations under memory pressure
            val operationStartTime = System.currentTimeMillis()
            val students = studentUseCases.getActiveStudents(1L).first()
            val operationTime = System.currentTimeMillis() - operationStartTime
            
            println("Operation time under memory pressure: ${operationTime}ms")
            assertTrue("Operations should complete even under memory pressure", operationTime < 2000)
            
        } finally {
            // Clean up large objects
            largeObjects.clear()
            System.gc()
        }
    }
} 
