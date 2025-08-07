package gr.eduinvoice.stress

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
import java.time.LocalTime
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
    fun testConcurrentOperations() = runTest {
        val user = createTestUser()
        
        // Stress test parameters
        val concurrentThreads = TestConfiguration.Stress.concurrentThreads
        val operationsPerThread = TestConfiguration.Stress.operationsPerThread
        val maxExecutionTime = TestConfiguration.Stress.maxExecutionTime
        
        val successCounter = AtomicInteger(0)
        val errorCounter = AtomicInteger(0)
        val startTime = AtomicLong(System.currentTimeMillis())
        
        try {
            withTimeout(maxExecutionTime) {
                val jobs = (1..concurrentThreads).map { threadId ->
                    async {
                        repeat(operationsPerThread) { operationId ->
                            try {
                                when (operationId % 4) {
                                    0 -> {
                                        val student = createTestStudent(user.id, "Student_${threadId}_${operationId}")
                                        studentUseCases.insertStudent(student)
                                    }
                                    1 -> {
                                        val students = studentUseCases.getActiveStudents(user.id).first()
                                        if (students.isNotEmpty()) {
                                            val student = students.first()
                                            val updatedStudent = student.copy(name = "Updated_${threadId}_${operationId}")
                                            studentUseCases.updateStudent(updatedStudent)
                                        }
                                    }
                                    2 -> {
                                        val count = studentUseCases.getActiveStudentCount(user.id)
                                        assertTrue("Student count should be non-negative", count >= 0)
                                    }
                                    3 -> {
                                        val students = studentUseCases.getActiveStudents(user.id).first()
                                        if (students.isNotEmpty()) {
                                            val student = students.first()
                                            studentUseCases.getStudentById(student.id, user.id).first()
                                        }
                                    }
                                }
                                successCounter.incrementAndGet()
                            } catch (e: Exception) {
                                errorCounter.incrementAndGet()
                                // Log error but continue
                            }
                        }
                        threadId
                    }
                }
                
                val results = jobs.awaitAll()
                val totalTime = System.currentTimeMillis() - startTime.get()
                
                // Verify results
                assertEquals(concurrentThreads, results.size)
                assertTrue("Too many errors occurred: ${errorCounter.get()}", 
                          errorCounter.get() < TestConfiguration.Stress.maxErrorRate * (concurrentThreads * operationsPerThread))
                assertTrue("Success rate too low", 
                          successCounter.get() > TestConfiguration.Stress.minSuccessRate * (concurrentThreads * operationsPerThread))
                assertTrue("Stress test took too long: ${totalTime}ms", totalTime < maxExecutionTime)
            }
        } catch (e: Exception) {
            fail("Stress test failed with exception: ${e.message}")
        }
    }
    
    @Test
    fun testMemoryPressure() = runTest {
        val user = createTestUser()
        val initialMemory = getMemoryUsage()
        
        // Create memory pressure by creating large datasets
        val students = createLargeStudentDataset(user.id, TestConfiguration.Stress.memoryPressureStudentCount)
        val lessons = createLargeLessonDataset(students, TestConfiguration.Stress.memoryPressureLessonCount)
        val groups = createLargeGroupDataset(user.id, TestConfiguration.Stress.memoryPressureGroupCount)
        
        val memoryAfterCreation = getMemoryUsage()
        val memoryIncrease = memoryAfterCreation - initialMemory
        
        // Verify memory usage is within acceptable limits
        assertTrue("Memory increase too high: ${memoryIncrease} bytes", 
                  memoryIncrease < TestConfiguration.Stress.maxMemoryIncrease)
        
        // Perform operations under memory pressure
        val operationsUnderPressure = TestConfiguration.Stress.operationsUnderMemoryPressure
        val successCount = AtomicInteger(0)
        
        repeat(operationsUnderPressure) { operationId ->
            try {
                when (operationId % 3) {
                    0 -> {
                        val newStudent = createTestStudent(user.id, "Pressure_Student_${operationId}")
                        studentUseCases.insertStudent(newStudent)
                    }
                    1 -> {
                        val allStudents = studentUseCases.getActiveStudents(user.id).first()
                        assertTrue("Should have students", allStudents.isNotEmpty())
                    }
                    2 -> {
                        val count = studentUseCases.getActiveStudentCount(user.id)
                        assertTrue("Count should be positive", count > 0)
                    }
                }
                successCount.incrementAndGet()
            } catch (e: Exception) {
                // Memory pressure might cause some failures, but not too many
                if (successCount.get() < operationsUnderPressure * TestConfiguration.Stress.minSuccessRateUnderPressure) {
                    throw e
                }
            }
        }
        
        // Verify success rate under memory pressure
        assertTrue("Success rate under memory pressure too low: ${successCount.get()}/${operationsUnderPressure}", 
                  successCount.get() > operationsUnderPressure * TestConfiguration.Stress.minSuccessRateUnderPressure)
    }
    
    @Test
    fun testDatabaseStress() = runTest {
        val user = createTestUser()
        
        // Create extreme dataset
        val students = createLargeStudentDataset(user.id, TestConfiguration.Stress.extremeStudentCount)
        val lessons = createLargeLessonDataset(students, TestConfiguration.Stress.extremeLessonCount)
        
        // Perform intensive database operations
        val intensiveOperations = TestConfiguration.Stress.intensiveDatabaseOperations
        val startTime = System.currentTimeMillis()
        
        repeat(intensiveOperations) { operationId ->
            when (operationId % 5) {
                0 -> {
                    // Bulk read operations
                    val allStudents = studentUseCases.getActiveStudents(user.id).first()
                    assertTrue("Should have students", allStudents.isNotEmpty())
                }
                1 -> {
                    // Search operations
                    val searchQuery = "Student_${operationId % 100}"
                    val searchResults = studentUseCases.searchStudentsPaginated(user.id, searchQuery, 10, 0)
                    assertNotNull("Search results should not be null", searchResults)
                }
                2 -> {
                    // Pagination operations
                    val pageSize = TestConfiguration.Database.pageSize
                    val offset = (operationId * 10) % students.size
                    val paginatedStudents = studentUseCases.getStudentsPaginated(user.id, pageSize, offset)
                    assertTrue("Should have paginated students", paginatedStudents.isNotEmpty())
                }
                3 -> {
                    // Update operations
                    if (students.isNotEmpty()) {
                        val student = students[operationId % students.size]
                        val updatedStudent = student.copy(name = "Stress_Updated_${operationId}")
                        studentUseCases.updateStudent(updatedStudent)
                    }
                }
                4 -> {
                    // Delete operations (soft delete)
                    if (students.isNotEmpty()) {
                        val student = students[operationId % students.size]
                        studentUseCases.softDeleteStudent(student.id, user.id)
                    }
                }
            }
        }
        
        val totalTime = System.currentTimeMillis() - startTime
        assertTrue("Database stress test took too long: ${totalTime}ms", 
                  totalTime < TestConfiguration.Stress.maxDatabaseStressTime)
    }
    
    @Test
    fun testConcurrentDataModification() = runTest {
        val user = createTestUser()
        val students = createLargeStudentDataset(user.id, TestConfiguration.Stress.concurrentModificationStudentCount)
        
        val modificationThreads = TestConfiguration.Stress.modificationThreads
        val modificationsPerThread = TestConfiguration.Stress.modificationsPerThread
        
        val successCounter = AtomicInteger(0)
        val conflictCounter = AtomicInteger(0)
        
        val jobs = (1..modificationThreads).map { threadId ->
            async {
                repeat(modificationsPerThread) { modificationId ->
                    try {
                        val student = students[modificationId % students.size]
                        val updatedStudent = student.copy(
                            name = "Concurrent_${threadId}_${modificationId}",
                            rate = student.rate + modificationId
                        )
                        studentUseCases.updateStudent(updatedStudent)
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        if (e.message?.contains("conflict", ignoreCase = true) == true) {
                            conflictCounter.incrementAndGet()
                        } else {
                            throw e
                        }
                    }
                }
                threadId
            }
        }
        
        val results = jobs.awaitAll()
        val totalModifications = modificationThreads * modificationsPerThread
        
        // Verify results
        assertEquals(modificationThreads, results.size)
        assertTrue("Too many conflicts: ${conflictCounter.get()}", 
                  conflictCounter.get() < totalModifications * TestConfiguration.Stress.maxConflictRate)
        assertTrue("Success rate too low: ${successCounter.get()}/${totalModifications}", 
                  successCounter.get() > totalModifications * TestConfiguration.Stress.minModificationSuccessRate)
    }
    
    @Test
    fun testExtremeLoadConditions() = runTest {
        val user = createTestUser()
        
        // Create extreme load
        val extremeLoadOperations = TestConfiguration.Stress.extremeLoadOperations
        val maxLoadTime = TestConfiguration.Stress.maxLoadTime
        
        val startTime = System.currentTimeMillis()
        val successCounter = AtomicInteger(0)
        
        try {
            withTimeout(maxLoadTime) {
                val jobs = (1..extremeLoadOperations).map { operationId ->
                    async {
                        try {
                            when (operationId % 6) {
                                0 -> {
                                    val student = createTestStudent(user.id, "Extreme_${operationId}")
                                    studentUseCases.insertStudent(student)
                                }
                                1 -> {
                                    val students = studentUseCases.getActiveStudents(user.id).first()
                                    assertTrue("Should have students", students.isNotEmpty())
                                }
                                2 -> {
                                    val count = studentUseCases.getActiveStudentCount(user.id)
                                    assertTrue("Count should be non-negative", count >= 0)
                                }
                                3 -> {
                                    val lesson = createTestLesson(user.id, operationId.toLong())
                                    lessonUseCases.addLesson(lesson)
                                }
                                4 -> {
                                    val group = createTestGroup(user.id, "Extreme_Group_${operationId}")
                                    groupUseCases.insertGroup(group)
                                }
                                5 -> {
                                    // Complex operation: search and update
                                    val searchQuery = "Extreme_${operationId % 10}"
                                    val searchResults = studentUseCases.searchStudentsPaginated(user.id, searchQuery, 5, 0)
                                    if (searchResults.isNotEmpty()) {
                                        val student = searchResults.first()
                                        val updatedStudent = student.copy(rate = student.rate + 1.0)
                                        studentUseCases.updateStudent(updatedStudent)
                                    }
                                }
                            }
                            successCounter.incrementAndGet()
                        } catch (e: Exception) {
                            // Under extreme load, some failures are expected
                            if (successCounter.get() < extremeLoadOperations * TestConfiguration.Stress.minExtremeLoadSuccessRate) {
                                throw e
                            }
                        }
                    }
                }
                
                jobs.awaitAll()
            }
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            assertTrue("Extreme load test failed too early: ${totalTime}ms", 
                      totalTime > TestConfiguration.Stress.minLoadTime)
        }
        
        val totalTime = System.currentTimeMillis() - startTime
        assertTrue("Extreme load test took too long: ${totalTime}ms", totalTime < maxLoadTime)
        assertTrue("Success rate under extreme load too low: ${successCounter.get()}/${extremeLoadOperations}", 
                  successCounter.get() > extremeLoadOperations * TestConfiguration.Stress.minExtremeLoadSuccessRate)
    }
    
    @Test
    fun testErrorRecoveryUnderStress() = runTest {
        val user = createTestUser()
        val students = createLargeStudentDataset(user.id, TestConfiguration.Stress.errorRecoveryStudentCount)
        
        val errorRecoveryOperations = TestConfiguration.Stress.errorRecoveryOperations
        val recoverySuccessCounter = AtomicInteger(0)
        
        repeat(errorRecoveryOperations) { operationId ->
            try {
                // Simulate potential error conditions
                when (operationId % 4) {
                    0 -> {
                        // Try to access non-existent student
                        try {
                            studentUseCases.getStudentById(999999L, user.id).first()
                        } catch (e: Exception) {
                            // Expected error, should recover gracefully
                            recoverySuccessCounter.incrementAndGet()
                        }
                    }
                    1 -> {
                        // Try to update with invalid data
                        try {
                            val invalidStudent = Student(
                                ownerId = user.id,
                                name = "", // Invalid empty name
                                surname = "",
                                parentMobile = "",
                                className = "",
                                rate = -1.0 // Invalid negative rate
                            )
                            studentUseCases.insertStudent(invalidStudent)
                        } catch (e: Exception) {
                            // Expected error, should recover gracefully
                            recoverySuccessCounter.incrementAndGet()
                        }
                    }
                    2 -> {
                        // Try to delete already deleted student
                        try {
                            val student = students[operationId % students.size]
                            studentUseCases.softDeleteStudent(student.id, user.id)
                            studentUseCases.softDeleteStudent(student.id, user.id) // Try again
                        } catch (e: Exception) {
                            // Expected error, should recover gracefully
                            recoverySuccessCounter.incrementAndGet()
                        }
                    }
                    3 -> {
                        // Normal operation that should succeed
                        val student = students[operationId % students.size]
                        val updatedStudent = student.copy(name = "Recovery_${operationId}")
                        studentUseCases.updateStudent(updatedStudent)
                        recoverySuccessCounter.incrementAndGet()
                    }
                }
            } catch (e: Exception) {
                // Unexpected error, should not happen frequently
                if (recoverySuccessCounter.get() < errorRecoveryOperations * TestConfiguration.Stress.minRecoverySuccessRate) {
                    throw e
                }
            }
        }
        
        // Verify error recovery success rate
        assertTrue("Error recovery success rate too low: ${recoverySuccessCounter.get()}/${errorRecoveryOperations}", 
                  recoverySuccessCounter.get() > errorRecoveryOperations * TestConfiguration.Stress.minRecoverySuccessRate)
    }
    
    // Helper functions
    private suspend fun createTestUser(): User {
        val user = User(
            username = "stressuser_${System.currentTimeMillis()}",
            passwordHash = "stress_hash",
            fullName = "Stress Test User"
        )
        val userId = userUseCases.createUser(user)
        return user.copy(id = userId)
    }
    
    private fun createTestStudent(ownerId: Long, name: String): Student {
        return Student(
            ownerId = ownerId,
            name = name,
            surname = "Stress_Surname",
            parentMobile = "+30123456789",
            parentEmail = "stress@test.com",
            className = "Stress_Class",
            rate = 25.0
        )
    }
    
    private fun createTestLesson(ownerId: Long, studentId: Long): Lesson {
        return Lesson.create(
            studentId = studentId,
            date = LocalDate.now(),
            startTime = LocalTime.of(10, 0),
            durationMinutes = 60,
            notes = "Stress test lesson",
            ownerId = ownerId
        )
    }
    
    private fun createTestGroup(ownerId: Long, name: String): StudentGroup {
        return StudentGroup(
            ownerId = ownerId,
            name = name
        )
    }
    
    private suspend fun createLargeStudentDataset(ownerId: Long, count: Int): List<Student> {
        val students = mutableListOf<Student>()
        
        repeat(count) { index ->
            val student = Student(
                ownerId = ownerId,
                name = "Stress_Student_${index}",
                surname = "Stress_Surname_${index}",
                parentMobile = "+30${index.toString().padStart(9, '0')}",
                parentEmail = "stress${index}@test.com",
                className = "Stress_Class_${index % 10}",
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
                notes = "Stress lesson ${index}",
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
                name = "Stress_Group_${index}"
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
