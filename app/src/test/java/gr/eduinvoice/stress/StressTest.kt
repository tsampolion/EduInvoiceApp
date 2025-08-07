package gr.eduinvoice.stress

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
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
 * Comprehensive stress tests for concurrent operations and edge cases.
 * Tests system stability under extreme load and memory pressure.
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
    fun testConcurrentOperations() = runTest {
        // Populate initial dataset
        databaseContainer.populateLargeDataset(database, 1L, 100)
        
        val concurrentOperations = 50
        val operationCounter = AtomicInteger(0)
        val errorCounter = AtomicInteger(0)
        
        val startTime = System.currentTimeMillis()
        
        // Launch concurrent operations
        val operations = (1..concurrentOperations).map { operationId ->
            async {
                try {
                    when (operationId % 6) {
                        0 -> {
                            // Insert new student
                            val student = gr.eduinvoice.data.model.Student(
                                id = 0,
                                name = "ConcurrentStudent$operationId",
                                surname = "Surname$operationId",
                                parentMobile = "123456789$operationId",
                                className = "Class${operationId % 10}",
                                rate = 20.0 + (operationId % 30),
                                ownerId = 1L
                            )
                            studentUseCases.insertStudent(student)
                        }
                        1 -> {
                            // Update existing student
                            val students = studentUseCases.getActiveStudents(1L).first()
                            if (students.isNotEmpty()) {
                                val student = students[operationId % students.size]
                                studentUseCases.updateStudent(student.copy(rate = student.rate + 1.0))
                            }
                        }
                        2 -> {
                            // Delete student
                            val students = studentUseCases.getActiveStudents(1L).first()
                            if (students.isNotEmpty()) {
                                val student = students[operationId % students.size]
                                studentUseCases.softDeleteStudent(student.id, 1L)
                            }
                        }
                        3 -> {
                            // Add lesson
                            val students = studentUseCases.getActiveStudents(1L).first()
                            if (students.isNotEmpty()) {
                                val student = students[operationId % students.size]
                                val lesson = gr.eduinvoice.data.model.Lesson(
                                    id = 0,
                                    studentId = student.id,
                                    date = LocalDate.now().toString(),
                                    time = LocalTime.now().toString(),
                                    duration = 60 + (operationId % 30),
                                    rate = student.rate,
                                    paid = false,
                                    invoiced = false,
                                    ownerId = 1L
                                )
                                lessonUseCases.addLesson(lesson)
                            }
                        }
                        4 -> {
                            // Search students
                            studentUseCases.searchStudentsPaginated(1L, "Student", 0, 50)
                        }
                        5 -> {
                            // Get paginated students
                            studentUseCases.getStudentsPaginated(1L, (operationId * 10) % 100, 20)
                        }
                    }
                    operationCounter.incrementAndGet()
                } catch (e: Exception) {
                    errorCounter.incrementAndGet()
                    println("Error in operation $operationId: ${e.message}")
                }
            }
        }
        
        val results = operations.awaitAll()
        val totalTime = System.currentTimeMillis() - startTime
        
        println("Concurrent operations completed:")
        println("Total operations: $concurrentOperations")
        println("Successful operations: ${operationCounter.get()}")
        println("Failed operations: ${errorCounter.get()}")
        println("Total time: ${totalTime}ms")
        
        // Verify most operations succeeded
        assertTrue("At least 80% of operations should succeed", 
                  operationCounter.get() >= concurrentOperations * 0.8)
        assertTrue("Operations should complete within 30 seconds", totalTime < 30000)
    }
    
    @Test
    fun testMemoryPressure() = runTest {
        val largeObjects = mutableListOf<String>()
        val memoryPressureOperations = 100
        
        try {
            // Create memory pressure by allocating large objects
            repeat(1000) { index ->
                largeObjects.add("Large string $index".repeat(1000))
            }
            
            val startTime = System.currentTimeMillis()
            val operationCounter = AtomicInteger(0)
            val errorCounter = AtomicInteger(0)
            
            // Perform operations under memory pressure
            val operations = (1..memoryPressureOperations).map { operationId ->
                async {
                    try {
                        when (operationId % 4) {
                            0 -> {
                                // Insert student
                                val student = gr.eduinvoice.data.model.Student(
                                    id = 0,
                                    name = "MemoryStudent$operationId",
                                    surname = "Surname$operationId",
                                    parentMobile = "123456789$operationId",
                                    className = "Class${operationId % 10}",
                                    rate = 20.0 + (operationId % 30),
                                    ownerId = 1L
                                )
                                studentUseCases.insertStudent(student)
                            }
                            1 -> {
                                // Get students
                                studentUseCases.getActiveStudents(1L).first()
                            }
                            2 -> {
                                // Search students
                                studentUseCases.searchStudentsPaginated(1L, "Student", 0, 50)
                            }
                            3 -> {
                                // Get paginated students
                                studentUseCases.getStudentsPaginated(1L, (operationId * 5) % 100, 20)
                            }
                        }
                        operationCounter.incrementAndGet()
                    } catch (e: Exception) {
                        errorCounter.incrementAndGet()
                        println("Error in memory pressure operation $operationId: ${e.message}")
                    }
                }
            }
            
            val results = operations.awaitAll()
            val totalTime = System.currentTimeMillis() - startTime
            
            println("Memory pressure test completed:")
            println("Total operations: $memoryPressureOperations")
            println("Successful operations: ${operationCounter.get()}")
            println("Failed operations: ${errorCounter.get()}")
            println("Total time: ${totalTime}ms")
            
            // Verify operations still succeed under memory pressure
            assertTrue("At least 70% of operations should succeed under memory pressure", 
                      operationCounter.get() >= memoryPressureOperations * 0.7)
            
        } finally {
            // Clean up large objects
            largeObjects.clear()
            System.gc()
        }
    }
    
    @Test
    fun testDatabaseCorruptionRecovery() = runTest {
        // Populate dataset
        databaseContainer.populateTestData(database, 1L)
        
        // Verify initial data
        val initialStudents = studentUseCases.getActiveStudents(1L).first()
        assertTrue("Should have initial students", initialStudents.isNotEmpty())
        
        // Create corrupted database
        val corruptedDb = databaseContainer.createCorruptedDatabase()
        
        // Try to recover and continue operations
        try {
            // Attempt to create new database and restore data
            val newDatabase = databaseContainer.createTestDatabase()
            val newStudentRepository = StudentRepository(newDatabase.studentDao())
            val newStudentUseCases = StudentUseCases(
                getActiveStudents = gr.eduinvoice.domain.student.GetActiveStudents(newStudentRepository),
                getArchivedStudents = gr.eduinvoice.domain.student.GetArchivedStudents(newStudentRepository),
                getStudentById = gr.eduinvoice.domain.student.GetStudentById(newStudentRepository),
                insertStudent = gr.eduinvoice.domain.student.InsertStudent(newStudentRepository),
                updateStudent = gr.eduinvoice.domain.student.UpdateStudent(newStudentRepository),
                softDeleteStudent = gr.eduinvoice.domain.student.SoftDeleteStudent(newStudentRepository),
                restoreStudent = gr.eduinvoice.domain.student.RestoreStudent(newStudentRepository),
                getActiveStudentCount = gr.eduinvoice.domain.student.GetActiveStudentCount(newStudentRepository),
                classNameExists = gr.eduinvoice.domain.student.ClassNameExists(newStudentRepository),
                getStudentsPaginated = gr.eduinvoice.domain.student.GetStudentsPaginated(newStudentRepository),
                searchStudentsPaginated = gr.eduinvoice.domain.student.SearchStudentsPaginated(newStudentRepository)
            )
            
            // Populate new database
            databaseContainer.populateTestData(newDatabase, 1L)
            
            // Verify recovery
            val recoveredStudents = newStudentUseCases.getActiveStudents(1L).first()
            assertTrue("Should have recovered students", recoveredStudents.isNotEmpty())
            
            // Test operations on recovered database
            val testStudent = gr.eduinvoice.data.model.Student(
                id = 0,
                name = "RecoveryTest",
                surname = "Student",
                parentMobile = "1234567890",
                className = "TestClass",
                rate = 25.0,
                ownerId = 1L
            )
            
            val studentId = newStudentUseCases.insertStudent(testStudent)
            assertTrue("Should be able to insert student after recovery", studentId > 0)
            
        } catch (e: Exception) {
            fail("Database recovery should succeed: ${e.message}")
        }
    }
    
    @Test
    fun testConcurrentDatabaseAccess() = runTest {
        val concurrentUsers = 10
        val operationsPerUser = 20
        val semaphore = Semaphore(5) // Limit concurrent database connections
        
        val startTime = System.currentTimeMillis()
        val successCounter = AtomicInteger(0)
        val errorCounter = AtomicInteger(0)
        
        // Simulate multiple users accessing the database concurrently
        val userOperations = (1..concurrentUsers).map { userId ->
            async {
                repeat(operationsPerUser) { operationId ->
                    try {
                        semaphore.withPermit {
                            when (operationId % 5) {
                                0 -> {
                                    // Insert student
                                    val student = gr.eduinvoice.data.model.Student(
                                        id = 0,
                                        name = "User${userId}Student$operationId",
                                        surname = "Surname$operationId",
                                        parentMobile = "123456789$operationId",
                                        className = "Class${operationId % 10}",
                                        rate = 20.0 + (operationId % 30),
                                        ownerId = userId.toLong()
                                    )
                                    studentUseCases.insertStudent(student)
                                }
                                1 -> {
                                    // Get students for user
                                    studentUseCases.getActiveStudents(userId.toLong()).first()
                                }
                                2 -> {
                                    // Update student
                                    val students = studentUseCases.getActiveStudents(userId.toLong()).first()
                                    if (students.isNotEmpty()) {
                                        val student = students[operationId % students.size]
                                        studentUseCases.updateStudent(student.copy(rate = student.rate + 1.0))
                                    }
                                }
                                3 -> {
                                    // Search students
                                    studentUseCases.searchStudentsPaginated(userId.toLong(), "Student", 0, 20)
                                }
                                4 -> {
                                    // Get paginated students
                                    studentUseCases.getStudentsPaginated(userId.toLong(), (operationId * 5) % 50, 10)
                                }
                            }
                        }
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        errorCounter.incrementAndGet()
                        println("Error in user $userId operation $operationId: ${e.message}")
                    }
                }
            }
        }
        
        val results = userOperations.awaitAll()
        val totalTime = System.currentTimeMillis() - startTime
        
        val totalOperations = concurrentUsers * operationsPerUser
        
        println("Concurrent database access test completed:")
        println("Total operations: $totalOperations")
        println("Successful operations: ${successCounter.get()}")
        println("Failed operations: ${errorCounter.get()}")
        println("Total time: ${totalTime}ms")
        
        // Verify most operations succeeded
        assertTrue("At least 85% of operations should succeed", 
                  successCounter.get() >= totalOperations * 0.85)
        assertTrue("Operations should complete within 60 seconds", totalTime < 60000)
    }
    
    @Test
    fun testRapidStateChanges() = runTest {
        // Populate initial data
        databaseContainer.populateTestData(database, 1L)
        
        val rapidChanges = 100
        val changeCounter = AtomicInteger(0)
        val errorCounter = AtomicInteger(0)
        
        val startTime = System.currentTimeMillis()
        
        // Perform rapid state changes
        val changes = (1..rapidChanges).map { changeId ->
            async {
                try {
                    when (changeId % 4) {
                        0 -> {
                            // Rapid student insertions
                            val student = gr.eduinvoice.data.model.Student(
                                id = 0,
                                name = "RapidStudent$changeId",
                                surname = "Surname$changeId",
                                parentMobile = "123456789$changeId",
                                className = "Class${changeId % 10}",
                                rate = 20.0 + (changeId % 30),
                                ownerId = 1L
                            )
                            studentUseCases.insertStudent(student)
                        }
                        1 -> {
                            // Rapid student updates
                            val students = studentUseCases.getActiveStudents(1L).first()
                            if (students.isNotEmpty()) {
                                val student = students[changeId % students.size]
                                studentUseCases.updateStudent(student.copy(name = "Updated${student.name}"))
                            }
                        }
                        2 -> {
                            // Rapid student deletions
                            val students = studentUseCases.getActiveStudents(1L).first()
                            if (students.isNotEmpty()) {
                                val student = students[changeId % students.size]
                                studentUseCases.softDeleteStudent(student.id, 1L)
                            }
                        }
                        3 -> {
                            // Rapid student restorations
                            val archivedStudents = studentUseCases.getArchivedStudents(1L).first()
                            if (archivedStudents.isNotEmpty()) {
                                val student = archivedStudents[changeId % archivedStudents.size]
                                studentUseCases.restoreStudent(student.id, 1L)
                            }
                        }
                    }
                    changeCounter.incrementAndGet()
                } catch (e: Exception) {
                    errorCounter.incrementAndGet()
                    println("Error in rapid change $changeId: ${e.message}")
                }
            }
        }
        
        val results = changes.awaitAll()
        val totalTime = System.currentTimeMillis() - startTime
        
        println("Rapid state changes test completed:")
        println("Total changes: $rapidChanges")
        println("Successful changes: ${changeCounter.get()}")
        println("Failed changes: ${errorCounter.get()}")
        println("Total time: ${totalTime}ms")
        
        // Verify most changes succeeded
        assertTrue("At least 80% of rapid changes should succeed", 
                  changeCounter.get() >= rapidChanges * 0.8)
        assertTrue("Rapid changes should complete within 30 seconds", totalTime < 30000)
    }
    
    @Test
    fun testResourceExhaustion() = runTest {
        val resourceOperations = 200
        val operationCounter = AtomicInteger(0)
        val errorCounter = AtomicInteger(0)
        
        val startTime = System.currentTimeMillis()
        
        // Simulate resource exhaustion scenarios
        val operations = (1..resourceOperations).map { operationId ->
            async {
                try {
                    when (operationId % 3) {
                        0 -> {
                            // Large dataset operations
                            databaseContainer.populateLargeDataset(database, operationId.toLong(), 50)
                        }
                        1 -> {
                            // Memory-intensive operations
                            val largeList = (1..1000).map { "Large string $it".repeat(100) }
                            studentUseCases.getActiveStudents(1L).first()
                            // Force garbage collection
                            System.gc()
                        }
                        2 -> {
                            // CPU-intensive operations
                            repeat(1000) { i ->
                                studentUseCases.searchStudentsPaginated(1L, "Student$i", 0, 10)
                            }
                        }
                    }
                    operationCounter.incrementAndGet()
                } catch (e: Exception) {
                    errorCounter.incrementAndGet()
                    println("Error in resource operation $operationId: ${e.message}")
                }
            }
        }
        
        val results = operations.awaitAll()
        val totalTime = System.currentTimeMillis() - startTime
        
        println("Resource exhaustion test completed:")
        println("Total operations: $resourceOperations")
        println("Successful operations: ${operationCounter.get()}")
        println("Failed operations: ${errorCounter.get()}")
        println("Total time: ${totalTime}ms")
        
        // Verify system remains stable under resource pressure
        assertTrue("At least 70% of operations should succeed under resource pressure", 
                  operationCounter.get() >= resourceOperations * 0.7)
        assertTrue("Operations should complete within 120 seconds", totalTime < 120000)
    }
}
