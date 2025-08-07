package gr.eduinvoice.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.TestBase
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Group
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.LessonRepository
import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.domain.student.StudentUseCase
import gr.eduinvoice.domain.lesson.LessonUseCase
import gr.eduinvoice.domain.group.GroupUseCase
import gr.eduinvoice.domain.user.UserUseCase
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.LocalDate
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
    
    private lateinit var studentUseCase: StudentUseCase
    private lateinit var lessonUseCase: LessonUseCase
    private lateinit var groupUseCase: GroupUseCase
    private lateinit var userUseCase: UserUseCase
    
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
        studentUseCase = StudentUseCase(studentRepository)
        lessonUseCase = LessonUseCase(lessonRepository)
        groupUseCase = GroupUseCase(groupRepository)
        userUseCase = UserUseCase(userRepository)
    }
    
    @After
    fun tearDown() {
        databaseContainer.cleanupTestDatabase()
    }
    
    @Test
    fun testLargeDatasetPerformance() = runTest {
        val user = createTestUser()
        
        // Performance thresholds
        val maxInsertionTime = 5000L // 5 seconds
        val maxQueryTime = 1000L // 1 second
        val maxMemoryUsage = 100L * 1024 * 1024 // 100MB
        
        // Measure initial memory
        val initialMemory = getMemoryUsage()
        
        // Create large dataset
        val startTime = System.currentTimeMillis()
        
        val students = createLargeStudentDataset(user.id, 1000)
        val lessons = createLargeLessonDataset(students, 10000)
        val groups = createLargeGroupDataset(user.id, 100)
        
        val insertionTime = System.currentTimeMillis() - startTime
        
        // Verify insertion performance
        assertTrue("Large dataset insertion should complete within $maxInsertionTime ms", 
                  insertionTime < maxInsertionTime)
        
        // Test query performance
        val queryStartTime = System.currentTimeMillis()
        
        val allStudents = studentUseCase.getAllStudents(user.id)
        val allLessons = lessonUseCase.getAllLessons(user.id)
        val allGroups = groupUseCase.getAllGroups(user.id)
        
        val queryTime = System.currentTimeMillis() - queryStartTime
        
        // Verify query performance
        assertTrue("Large dataset queries should complete within $maxQueryTime ms", 
                  queryTime < maxQueryTime)
        
        // Verify data integrity
        assertEquals("Should have 1000 students", 1000, allStudents.size)
        assertEquals("Should have 10000 lessons", 10000, allLessons.size)
        assertEquals("Should have 100 groups", 100, allGroups.size)
        
        // Test memory usage
        val finalMemory = getMemoryUsage()
        val memoryIncrease = finalMemory - initialMemory
        
        assertTrue("Memory usage should be under $maxMemoryUsage bytes", 
                  memoryIncrease < maxMemoryUsage)
        
        println("Performance Results:")
        println("Insertion time: ${insertionTime}ms")
        println("Query time: ${queryTime}ms")
        println("Memory increase: ${memoryIncrease / 1024 / 1024}MB")
    }
    
    @Test
    fun testMemoryUsage() = runTest {
        val user = createTestUser()
        
        // Memory usage thresholds
        val maxMemoryUsage = 100L * 1024 * 1024 // 100MB
        val maxMemoryLeak = 10L * 1024 * 1024 // 10MB leak threshold
        
        val initialMemory = getMemoryUsage()
        
        // Perform operations that should not leak memory
        repeat(100) { iteration ->
            val students = createLargeStudentDataset(user.id, 100)
            val lessons = createLargeLessonDataset(students, 1000)
            
            // Perform various operations
            students.forEach { student ->
                studentUseCase.getStudent(student.id)
                studentUseCase.getStudentWithLessons(student.id)
                studentUseCase.calculateStudentEarnings(student.id)
            }
            
            // Force garbage collection
            System.gc()
            
            // Check memory every 10 iterations
            if (iteration % 10 == 0) {
                val currentMemory = getMemoryUsage()
                val memoryIncrease = currentMemory - initialMemory
                
                assertTrue("Memory usage should not exceed $maxMemoryUsage bytes at iteration $iteration", 
                          currentMemory < maxMemoryUsage)
                
                println("Iteration $iteration: Memory usage = ${currentMemory / 1024 / 1024}MB")
            }
        }
        
        // Final memory check
        val finalMemory = getMemoryUsage()
        val totalMemoryIncrease = finalMemory - initialMemory
        
        assertTrue("Total memory increase should be under $maxMemoryLeak bytes", 
                  totalMemoryIncrease < maxMemoryLeak)
        
        println("Final memory usage: ${finalMemory / 1024 / 1024}MB")
        println("Total memory increase: ${totalMemoryIncrease / 1024 / 1024}MB")
    }
    
    @Test
    fun testDatabaseQueryPerformance() = runTest {
        val user = createTestUser()
        
        // Create test data
        val students = createLargeStudentDataset(user.id, 500)
        val lessons = createLargeLessonDataset(students, 5000)
        
        // Test different query patterns
        val queryTests = listOf(
            QueryTest("Get all students", { studentUseCase.getAllStudents(user.id) }, 1000L),
            QueryTest("Get students with lessons", { 
                students.take(10).map { studentUseCase.getStudentWithLessons(it.id) }
            }, 2000L),
            QueryTest("Calculate earnings for all students", {
                students.take(10).map { studentUseCase.calculateStudentEarnings(it.id) }
            }, 3000L),
            QueryTest("Get lessons by date range", {
                lessonUseCase.getLessonsByDateRange(user.id, LocalDate.now().minusDays(30), LocalDate.now())
            }, 1500L),
            QueryTest("Search students by name", {
                studentUseCase.searchStudents(user.id, "Student")
            }, 1000L)
        )
        
        queryTests.forEach { test ->
            val startTime = System.currentTimeMillis()
            val result = test.query()
            val duration = System.currentTimeMillis() - startTime
            
            assertTrue("${test.name} should complete within ${test.maxTime}ms, took ${duration}ms", 
                      duration < test.maxTime)
            
            println("${test.name}: ${duration}ms")
        }
    }
    
    @Test
    fun testConcurrentOperationPerformance() = runTest {
        val user = createTestUser()
        
        // Performance thresholds
        val maxConcurrentTime = 3000L // 3 seconds
        val maxMemoryUsage = 150L * 1024 * 1024 // 150MB
        
        val initialMemory = getMemoryUsage()
        
        // Test concurrent operations
        val startTime = System.currentTimeMillis()
        
        val concurrentOperations = listOf(
            { createLargeStudentDataset(user.id, 100) },
            { createLargeLessonDataset(createLargeStudentDataset(user.id, 50), 500) },
            { createLargeGroupDataset(user.id, 20) },
            { studentUseCase.getAllStudents(user.id) },
            { lessonUseCase.getAllLessons(user.id) },
            { groupUseCase.getAllGroups(user.id) }
        )
        
        // Execute operations concurrently
        val results = concurrentOperations.map { operation ->
            kotlinx.coroutines.async {
                operation()
            }
        }.map { it.await() }
        
        val concurrentTime = System.currentTimeMillis() - startTime
        
        // Verify concurrent performance
        assertTrue("Concurrent operations should complete within $maxConcurrentTime ms", 
                  concurrentTime < maxConcurrentTime)
        
        // Verify memory usage
        val finalMemory = getMemoryUsage()
        val memoryIncrease = finalMemory - initialMemory
        
        assertTrue("Memory usage should be under $maxMemoryUsage bytes during concurrent operations", 
                  memoryIncrease < maxMemoryUsage)
        
        println("Concurrent operations completed in: ${concurrentTime}ms")
        println("Memory usage during concurrent operations: ${memoryIncrease / 1024 / 1024}MB")
    }
    
    @Test
    fun testPaginationPerformance() = runTest {
        val user = createTestUser()
        
        // Create large dataset
        val students = createLargeStudentDataset(user.id, 2000)
        val lessons = createLargeLessonDataset(students, 20000)
        
        // Test pagination performance
        val pageSizes = listOf(10, 50, 100, 200)
        val maxPageTime = 500L // 500ms per page
        
        pageSizes.forEach { pageSize ->
            val startTime = System.currentTimeMillis()
            
            // Simulate pagination
            repeat(5) { page ->
                val offset = page * pageSize
                val pageStudents = students.drop(offset).take(pageSize)
                
                // Perform operations on page
                pageStudents.forEach { student ->
                    studentUseCase.getStudent(student.id)
                }
            }
            
            val pageTime = System.currentTimeMillis() - startTime
            
            assertTrue("Pagination with page size $pageSize should complete within $maxPageTime ms", 
                      pageTime < maxPageTime)
            
            println("Pagination with page size $pageSize: ${pageTime}ms")
        }
    }
    
    @Test
    fun testUIResponsivenessSimulation() = runTest {
        val user = createTestUser()
        
        // Create moderate dataset for UI testing
        val students = createLargeStudentDataset(user.id, 200)
        val lessons = createLargeLessonDataset(students, 2000)
        
        // Simulate UI operations that should remain responsive
        val uiOperations = listOf(
            { studentUseCase.getAllStudents(user.id) }, // Student list
            { lessonUseCase.getAllLessons(user.id) }, // Lesson list
            { students.take(5).map { studentUseCase.getStudentWithLessons(it.id) } }, // Student details
            { lessons.take(10).map { lessonUseCase.getLesson(it.id) } }, // Lesson details
            { students.take(10).map { studentUseCase.calculateStudentEarnings(it.id) } } // Revenue calculation
        )
        
        val maxUITime = 1000L // 1 second for UI responsiveness
        
        uiOperations.forEachIndexed { index, operation ->
            val startTime = System.currentTimeMillis()
            val result = operation()
            val duration = System.currentTimeMillis() - startTime
            
            assertTrue("UI operation $index should complete within $maxUITime ms, took ${duration}ms", 
                      duration < maxUITime)
            
            println("UI operation $index: ${duration}ms")
        }
    }
    
    // Helper methods
    private suspend fun createTestUser(): User {
        val user = User(
            id = 1L,
            username = "perftestuser",
            email = "perf@test.com",
            passwordHash = "hashedpassword",
            createdAt = LocalDateTime.now()
        )
        return userUseCase.registerUser(user)
    }
    
    private suspend fun createLargeStudentDataset(ownerId: Long, count: Int): List<Student> {
        val students = mutableListOf<Student>()
        
        for (i in 1..count) {
            val student = Student(
                id = i.toLong(),
                name = "Student $i",
                email = "student$i@example.com",
                phone = "+123456789$i",
                hourlyRate = 50.0 + (i % 50),
                ownerId = ownerId,
                isArchived = false,
                createdAt = LocalDateTime.now()
            )
            students.add(studentUseCase.createStudent(student))
        }
        
        return students
    }
    
    private suspend fun createLargeLessonDataset(students: List<Student>, count: Int): List<Lesson> {
        val lessons = mutableListOf<Lesson>()
        
        for (i in 1..count) {
            val student = students[i % students.size]
            val lesson = Lesson(
                id = i.toLong(),
                studentId = student.id,
                groupId = null,
                date = LocalDate.now().minusDays((i % 365).toLong()),
                duration = 30 + (i % 90), // 30-120 minutes
                hourlyRate = student.hourlyRate,
                notes = "Lesson $i for ${student.name}",
                ownerId = student.ownerId,
                createdAt = LocalDateTime.now()
            )
            lessons.add(lessonUseCase.createLesson(lesson))
        }
        
        return lessons
    }
    
    private suspend fun createLargeGroupDataset(ownerId: Long, count: Int): List<Group> {
        val groups = mutableListOf<Group>()
        
        for (i in 1..count) {
            val group = Group(
                id = i.toLong(),
                name = "Group $i",
                description = "Description for group $i",
                ownerId = ownerId,
                createdAt = LocalDateTime.now()
            )
            groups.add(groupUseCase.createGroup(group))
        }
        
        return groups
    }
    
    private fun getMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
    
    private data class QueryTest(
        val name: String,
        val query: suspend () -> Any,
        val maxTime: Long
    )
}
