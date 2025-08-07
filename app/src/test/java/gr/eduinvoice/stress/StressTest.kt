package gr.eduinvoice.stress

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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Stress testing framework for extreme load conditions and edge cases.
 * Tests concurrent operations, memory pressure, and system stability.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class StressTest : TestBase() {
    
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
    fun testConcurrentOperations() = runTest {
        val user = createTestUser()
        
        // Stress test parameters
        val concurrentThreads = 50
        val operationsPerThread = 100
        val maxExecutionTime = 30000L // 30 seconds
        
        val successCounter = AtomicInteger(0)
        val errorCounter = AtomicInteger(0)
        val startTime = AtomicLong(System.currentTimeMillis())
        
        try {
            withTimeout(maxExecutionTime) {
                val jobs = (1..concurrentThreads).map { threadId ->
                    async {
                        repeat(operationsPerThread) { operationId ->
                            try {
                                // Perform various concurrent operations
                                when (operationId % 4) {
                                    0 -> {
                                        val student = createTestStudent(user.id, threadId * 1000 + operationId)
                                        studentUseCase.createStudent(student)
                                    }
                                    1 -> {
                                        val lesson = createTestLesson(1L, user.id, threadId * 1000 + operationId)
                                        lessonUseCase.createLesson(lesson)
                                    }
                                    2 -> {
                                        val group = createTestGroup(user.id, threadId * 1000 + operationId)
                                        groupUseCase.createGroup(group)
                                    }
                                    3 -> {
                                        studentUseCase.getAllStudents(user.id)
                                    }
                                }
                                successCounter.incrementAndGet()
                            } catch (e: Exception) {
                                errorCounter.incrementAndGet()
                                println("Error in thread $threadId, operation $operationId: ${e.message}")
                            }
                        }
                    }
                }
                
                awaitAll(*jobs.toTypedArray())
            }
        } catch (e: Exception) {
            println("Stress test timeout or error: ${e.message}")
        }
        
        val totalOperations = concurrentThreads * operationsPerThread
        val successRate = successCounter.get().toDouble() / totalOperations
        
        println("Concurrent Operations Results:")
        println("Total operations: $totalOperations")
        println("Successful operations: ${successCounter.get()}")
        println("Failed operations: ${errorCounter.get()}")
        println("Success rate: ${String.format("%.2f", successRate * 100)}%")
        
        // Assertions
        assertTrue("Success rate should be above 90%", successRate > 0.90)
        assertTrue("Error rate should be below 10%", errorCounter.get() < totalOperations * 0.1)
        
        // Verify data integrity
        val finalStudents = studentUseCase.getAllStudents(user.id)
        val finalLessons = lessonUseCase.getAllLessons(user.id)
        val finalGroups = groupUseCase.getAllGroups(user.id)
        
        assertTrue("Should have created students", finalStudents.isNotEmpty())
        assertTrue("Should have created lessons", finalLessons.isNotEmpty())
        assertTrue("Should have created groups", finalGroups.isNotEmpty())
    }
    
    @Test
    fun testMemoryPressure() = runTest {
        val user = createTestUser()
        
        // Memory pressure test parameters
        val iterations = 100
        val maxMemoryUsage = 200L * 1024 * 1024 // 200MB
        val maxMemoryLeak = 50L * 1024 * 1024 // 50MB leak threshold
        
        val initialMemory = getMemoryUsage()
        var peakMemory = initialMemory
        
        repeat(iterations) { iteration ->
            // Create and manipulate large datasets
            val students = createLargeStudentDataset(user.id, 100)
            val lessons = createLargeLessonDataset(students, 1000)
            
            // Perform memory-intensive operations
            students.forEach { student ->
                studentUseCase.getStudentWithLessons(student.id)
                studentUseCase.calculateStudentEarnings(student.id)
                
                // Create additional data
                repeat(10) { i ->
                    val additionalLesson = createTestLesson(student.id, user.id, iteration * 1000 + i)
                    lessonUseCase.createLesson(additionalLesson)
                }
            }
            
            // Check memory usage
            val currentMemory = getMemoryUsage()
            peakMemory = maxOf(peakMemory, currentMemory)
            
            if (iteration % 10 == 0) {
                println("Iteration $iteration: Memory usage = ${currentMemory / 1024 / 1024}MB")
                
                // Force garbage collection
                System.gc()
                delay(100) // Allow GC to complete
            }
            
            // Assert memory limits
            assertTrue("Memory usage should not exceed $maxMemoryUsage bytes at iteration $iteration", 
                      currentMemory < maxMemoryUsage)
        }
        
        // Final memory check
        val finalMemory = getMemoryUsage()
        val totalMemoryIncrease = finalMemory - initialMemory
        
        println("Memory Pressure Test Results:")
        println("Initial memory: ${initialMemory / 1024 / 1024}MB")
        println("Peak memory: ${peakMemory / 1024 / 1024}MB")
        println("Final memory: ${finalMemory / 1024 / 1024}MB")
        println("Total memory increase: ${totalMemoryIncrease / 1024 / 1024}MB")
        
        assertTrue("Total memory increase should be under $maxMemoryLeak bytes", 
                  totalMemoryIncrease < maxMemoryLeak)
    }
    
    @Test
    fun testDatabaseStress() = runTest {
        val user = createTestUser()
        
        // Database stress test parameters
        val maxOperations = 10000
        val maxExecutionTime = 60000L // 60 seconds
        val batchSize = 100
        
        val successCounter = AtomicInteger(0)
        val errorCounter = AtomicInteger(0)
        
        try {
            withTimeout(maxExecutionTime) {
                // Perform intensive database operations
                repeat(maxOperations / batchSize) { batch ->
                    val batchJobs = (1..batchSize).map { operationId ->
                        async {
                            try {
                                val operationType = operationId % 6
                                when (operationType) {
                                    0 -> {
                                        // Create student
                                        val student = createTestStudent(user.id, batch * batchSize + operationId)
                                        studentUseCase.createStudent(student)
                                    }
                                    1 -> {
                                        // Create lesson
                                        val lesson = createTestLesson(1L, user.id, batch * batchSize + operationId)
                                        lessonUseCase.createLesson(lesson)
                                    }
                                    2 -> {
                                        // Update student
                                        val students = studentUseCase.getAllStudents(user.id)
                                        if (students.isNotEmpty()) {
                                            val student = students.first()
                                            val updatedStudent = student.copy(name = "Updated ${student.name}")
                                            studentUseCase.updateStudent(updatedStudent)
                                        }
                                    }
                                    3 -> {
                                        // Delete lesson
                                        val lessons = lessonUseCase.getAllLessons(user.id)
                                        if (lessons.isNotEmpty()) {
                                            lessonUseCase.deleteLesson(lessons.first().id)
                                        }
                                    }
                                    4 -> {
                                        // Complex query
                                        studentUseCase.getAllStudents(user.id)
                                        lessonUseCase.getAllLessons(user.id)
                                    }
                                    5 -> {
                                        // Search operations
                                        studentUseCase.searchStudents(user.id, "Student")
                                    }
                                }
                                successCounter.incrementAndGet()
                            } catch (e: Exception) {
                                errorCounter.incrementAndGet()
                            }
                        }
                    }
                    
                    awaitAll(*batchJobs.toTypedArray())
                    
                    if (batch % 10 == 0) {
                        println("Completed batch $batch: ${successCounter.get()} successes, ${errorCounter.get()} errors")
                    }
                }
            }
        } catch (e: Exception) {
            println("Database stress test timeout: ${e.message}")
        }
        
        val successRate = successCounter.get().toDouble() / maxOperations
        
        println("Database Stress Test Results:")
        println("Total operations: $maxOperations")
        println("Successful operations: ${successCounter.get()}")
        println("Failed operations: ${errorCounter.get()}")
        println("Success rate: ${String.format("%.2f", successRate * 100)}%")
        
        assertTrue("Success rate should be above 95%", successRate > 0.95)
    }
    
    @Test
    fun testConcurrentDataModification() = runTest {
        val user = createTestUser()
        val student = createTestStudent(user.id, 1)
        
        // Test concurrent modifications of the same data
        val concurrentModifications = 20
        val modificationsPerThread = 50
        
        val successCounter = AtomicInteger(0)
        val conflictCounter = AtomicInteger(0)
        
        val jobs = (1..concurrentModifications).map { threadId ->
            async {
                repeat(modificationsPerThread) { modificationId ->
                    try {
                        // Concurrently modify the same student
                        val currentStudent = studentUseCase.getStudent(student.id)
                        if (currentStudent != null) {
                            val modifiedStudent = currentStudent.copy(
                                name = "Modified by thread $threadId - $modificationId",
                                hourlyRate = currentStudent.hourlyRate + modificationId
                            )
                            studentUseCase.updateStudent(modifiedStudent)
                            successCounter.incrementAndGet()
                        }
                    } catch (e: Exception) {
                        conflictCounter.incrementAndGet()
                        // Expected conflicts are acceptable in concurrent modification
                    }
                }
            }
        }
        
        awaitAll(*jobs.toTypedArray())
        
        val totalModifications = concurrentModifications * modificationsPerThread
        val successRate = successCounter.get().toDouble() / totalModifications
        
        println("Concurrent Data Modification Results:")
        println("Total modifications: $totalModifications")
        println("Successful modifications: ${successCounter.get()}")
        println("Conflicts: ${conflictCounter.get()}")
        println("Success rate: ${String.format("%.2f", successRate * 100)}%")
        
        // Verify final state
        val finalStudent = studentUseCase.getStudent(student.id)
        assertNotNull("Student should still exist after concurrent modifications", finalStudent)
        assertNotEquals("Student should have been modified", student.name, finalStudent.name)
    }
    
    @Test
    fun testExtremeLoadConditions() = runTest {
        val user = createTestUser()
        
        // Extreme load test parameters
        val maxMemoryUsage = 500L * 1024 * 1024 // 500MB
        val maxExecutionTime = 120000L // 2 minutes
        
        val startTime = System.currentTimeMillis()
        var operationCount = 0
        
        try {
            withTimeout(maxExecutionTime) {
                while (System.currentTimeMillis() - startTime < maxExecutionTime) {
                    // Create extreme load
                    val students = createLargeStudentDataset(user.id, 50)
                    val lessons = createLargeLessonDataset(students, 500)
                    
                    // Perform intensive operations
                    students.forEach { student ->
                        studentUseCase.getStudentWithLessons(student.id)
                        studentUseCase.calculateStudentEarnings(student.id)
                        
                        // Create additional data
                        repeat(5) { i ->
                            val lesson = createTestLesson(student.id, user.id, operationCount + i)
                            lessonUseCase.createLesson(lesson)
                        }
                    }
                    
                    operationCount += students.size
                    
                    // Check memory usage
                    val currentMemory = getMemoryUsage()
                    assertTrue("Memory usage should not exceed $maxMemoryUsage bytes", 
                              currentMemory < maxMemoryUsage)
                    
                    // Small delay to prevent overwhelming the system
                    delay(10)
                }
            }
        } catch (e: Exception) {
            println("Extreme load test completed: ${e.message}")
        }
        
        println("Extreme Load Test Results:")
        println("Total operations: $operationCount")
        println("Test duration: ${System.currentTimeMillis() - startTime}ms")
        println("Final memory usage: ${getMemoryUsage() / 1024 / 1024}MB")
        
        assertTrue("Should have performed significant number of operations", operationCount > 1000)
    }
    
    @Test
    fun testErrorRecoveryUnderStress() = runTest {
        val user = createTestUser()
        
        // Create initial data
        val students = createLargeStudentDataset(user.id, 100)
        val lessons = createLargeLessonDataset(students, 1000)
        
        // Simulate errors and recovery under stress
        val errorScenarios = listOf(
            { simulateDatabaseError() },
            { simulateMemoryError() },
            { simulateConcurrencyError() },
            { simulateValidationError() }
        )
        
        val recoverySuccessCount = AtomicInteger(0)
        val totalScenarios = errorScenarios.size * 10 // Test each scenario 10 times
        
        repeat(10) { iteration ->
            errorScenarios.forEach { scenario ->
                try {
                    scenario()
                    recoverySuccessCount.incrementAndGet()
                } catch (e: Exception) {
                    // Expected errors, but system should recover
                    println("Error scenario completed (expected): ${e.message}")
                }
            }
            
            // Verify system is still functional
            val remainingStudents = studentUseCase.getAllStudents(user.id)
            val remainingLessons = lessonUseCase.getAllLessons(user.id)
            
            assertTrue("System should remain functional after error scenarios", 
                      remainingStudents.isNotEmpty())
            assertTrue("System should remain functional after error scenarios", 
                      remainingLessons.isNotEmpty())
        }
        
        val recoveryRate = recoverySuccessCount.get().toDouble() / totalScenarios
        
        println("Error Recovery Under Stress Results:")
        println("Total scenarios: $totalScenarios")
        println("Successful recoveries: ${recoverySuccessCount.get()}")
        println("Recovery rate: ${String.format("%.2f", recoveryRate * 100)}%")
        
        assertTrue("Recovery rate should be above 80%", recoveryRate > 0.80)
    }
    
    // Helper methods
    private suspend fun createTestUser(): User {
        val user = User(
            id = 1L,
            username = "stresstestuser",
            email = "stress@test.com",
            passwordHash = "hashedpassword",
            createdAt = LocalDateTime.now()
        )
        return userUseCase.registerUser(user)
    }
    
    private suspend fun createTestStudent(ownerId: Long, id: Int): Student {
        val student = Student(
            id = id.toLong(),
            name = "Stress Student $id",
            email = "stress$id@student.com",
            phone = "+123456789$id",
            hourlyRate = 50.0 + (id % 50),
            ownerId = ownerId,
            isArchived = false,
            createdAt = LocalDateTime.now()
        )
        return student
    }
    
    private suspend fun createTestLesson(studentId: Long, ownerId: Long, id: Int): Lesson {
        val lesson = Lesson(
            id = id.toLong(),
            studentId = studentId,
            groupId = null,
            date = LocalDate.now().minusDays((id % 30).toLong()),
            duration = 30 + (id % 90),
            hourlyRate = 50.0 + (id % 50),
            notes = "Stress lesson $id",
            ownerId = ownerId,
            createdAt = LocalDateTime.now()
        )
        return lesson
    }
    
    private suspend fun createTestGroup(ownerId: Long, id: Int): Group {
        val group = Group(
            id = id.toLong(),
            name = "Stress Group $id",
            description = "Stress test group $id",
            ownerId = ownerId,
            createdAt = LocalDateTime.now()
        )
        return group
    }
    
    private suspend fun createLargeStudentDataset(ownerId: Long, count: Int): List<Student> {
        val students = mutableListOf<Student>()
        
        for (i in 1..count) {
            val student = createTestStudent(ownerId, i)
            students.add(studentUseCase.createStudent(student))
        }
        
        return students
    }
    
    private suspend fun createLargeLessonDataset(students: List<Student>, count: Int): List<Lesson> {
        val lessons = mutableListOf<Lesson>()
        
        for (i in 1..count) {
            val student = students[i % students.size]
            val lesson = createTestLesson(student.id, student.ownerId, i)
            lessons.add(lessonUseCase.createLesson(lesson))
        }
        
        return lessons
    }
    
    private fun getMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
    
    // Error simulation methods
    private suspend fun simulateDatabaseError() {
        // Simulate database connection issues
        delay(10)
        throw RuntimeException("Simulated database error")
    }
    
    private suspend fun simulateMemoryError() {
        // Simulate memory pressure
        val largeList = mutableListOf<String>()
        repeat(100000) { largeList.add("Memory pressure test $it") }
        delay(10)
        throw OutOfMemoryError("Simulated memory error")
    }
    
    private suspend fun simulateConcurrencyError() {
        // Simulate concurrency conflicts
        delay(10)
        throw IllegalStateException("Simulated concurrency error")
    }
    
    private suspend fun simulateValidationError() {
        // Simulate validation errors
        delay(10)
        throw IllegalArgumentException("Simulated validation error")
    }
}
