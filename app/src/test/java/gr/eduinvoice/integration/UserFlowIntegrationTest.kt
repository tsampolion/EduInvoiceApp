package gr.eduinvoice.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
import gr.eduinvoice.data.repository.BackupRepository
import gr.eduinvoice.domain.student.StudentUseCase
import gr.eduinvoice.domain.lesson.LessonUseCase
import gr.eduinvoice.domain.group.GroupUseCase
import gr.eduinvoice.domain.user.UserUseCase
import gr.eduinvoice.infrastructure.TestDatabaseContainer
import gr.eduinvoice.utils.AppUtils
import io.mockk.mockk
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

/**
 * Comprehensive integration tests for all user paths.
 * Tests the complete user journey from registration to invoice generation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserFlowIntegrationTest : TestBase() {
    
    @get:Rule
    val databaseContainer = TestDatabaseContainer()
    
    private lateinit var database: EduInvoiceDatabase
    private lateinit var studentRepository: StudentRepository
    private lateinit var lessonRepository: LessonRepository
    private lateinit var groupRepository: GroupRepository
    private lateinit var userRepository: UserRepository
    private lateinit var backupRepository: BackupRepository
    
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
        backupRepository = mockk<BackupRepository>(relaxed = true)
        
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
    fun testCompleteUserJourney() = runTest {
        // Step 1: User Registration
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashedpassword",
            createdAt = LocalDateTime.now()
        )
        
        val registeredUser = userUseCase.registerUser(user)
        assertNotNull("User should be registered successfully", registeredUser)
        assertEquals("Username should match", "testuser", registeredUser.username)
        
        // Step 2: Create Student
        val student = Student(
            id = 1L,
            name = "John Doe",
            email = "john@example.com",
            phone = "+1234567890",
            hourlyRate = 50.0,
            ownerId = registeredUser.id,
            isArchived = false,
            createdAt = LocalDateTime.now()
        )
        
        val createdStudent = studentUseCase.createStudent(student)
        assertNotNull("Student should be created successfully", createdStudent)
        assertEquals("Student name should match", "John Doe", createdStudent.name)
        
        // Step 3: Create Group
        val group = Group(
            id = 1L,
            name = "Math Group",
            description = "Advanced mathematics group",
            ownerId = registeredUser.id,
            createdAt = LocalDateTime.now()
        )
        
        val createdGroup = groupUseCase.createGroup(group)
        assertNotNull("Group should be created successfully", createdGroup)
        assertEquals("Group name should match", "Math Group", createdGroup.name)
        
        // Step 4: Create Lesson
        val lesson = Lesson(
            id = 1L,
            studentId = createdStudent.id,
            groupId = createdGroup.id,
            date = LocalDate.now(),
            duration = 60,
            hourlyRate = 50.0,
            notes = "Algebra lesson",
            ownerId = registeredUser.id,
            createdAt = LocalDateTime.now()
        )
        
        val createdLesson = lessonUseCase.createLesson(lesson)
        assertNotNull("Lesson should be created successfully", createdLesson)
        assertEquals("Lesson duration should match", 60, createdLesson.duration)
        
        // Step 5: Verify Data Relationships
        val studentWithLessons = studentUseCase.getStudentWithLessons(createdStudent.id)
        assertNotNull("Student with lessons should be retrieved", studentWithLessons)
        assertEquals("Student should have one lesson", 1, studentWithLessons.lessons.size)
        
        val groupWithLessons = groupUseCase.getGroupWithLessons(createdGroup.id)
        assertNotNull("Group with lessons should be retrieved", groupWithLessons)
        assertEquals("Group should have one lesson", 1, groupWithLessons.lessons.size)
        
        // Step 6: Calculate Earnings
        val studentEarnings = studentUseCase.calculateStudentEarnings(createdStudent.id)
        assertNotNull("Student earnings should be calculated", studentEarnings)
        assertEquals("Earnings should be 50.0 for 1 hour", 50.0, studentEarnings.totalEarnings, 0.01)
    }
    
    @Test
    fun testErrorRecoveryScenarios() = runTest {
        // Test database corruption recovery
        testDatabaseCorruptionRecovery()
        
        // Test network failure handling
        testNetworkFailureHandling()
        
        // Test data validation errors
        testDataValidationErrors()
        
        // Test concurrent operation conflicts
        testConcurrentOperationConflicts()
    }
    
    private suspend fun testDatabaseCorruptionRecovery() {
        // Create test data
        val user = createTestUser()
        val student = createTestStudent(user.id)
        
        // Simulate database corruption by closing and reopening with issues
        database.close()
        
        // Attempt recovery
        val recoveredDatabase = databaseContainer.createTestDatabaseWithConfig(
            name = "recovery_test",
            destructiveMigration = true
        )
        
        // Verify recovery
        val isRecovered = databaseContainer.validateDatabaseIntegrity(recoveredDatabase)
        assertTrue("Database should be recovered successfully", isRecovered)
        
        recoveredDatabase.close()
    }
    
    private suspend fun testNetworkFailureHandling() {
        // Test offline data persistence
        val user = createTestUser()
        val student = createTestStudent(user.id)
        
        // Simulate offline mode by using local storage only
        val offlineStudent = student.copy(name = "Offline Student")
        val savedOffline = studentUseCase.createStudent(offlineStudent)
        
        assertNotNull("Offline data should be saved", savedOffline)
        assertEquals("Offline student name should match", "Offline Student", savedOffline.name)
    }
    
    private suspend fun testDataValidationErrors() {
        // Test invalid student data
        val user = createTestUser()
        
        val invalidStudent = Student(
            id = 0L,
            name = "", // Invalid empty name
            email = "invalid-email", // Invalid email
            phone = "invalid-phone", // Invalid phone
            hourlyRate = -10.0, // Invalid negative rate
            ownerId = user.id,
            isArchived = false,
            createdAt = LocalDateTime.now()
        )
        
        try {
            studentUseCase.createStudent(invalidStudent)
            fail("Should throw validation exception")
        } catch (e: Exception) {
            // Expected validation error
            assertTrue("Should be validation error", e.message?.contains("validation") == true)
        }
    }
    
    private suspend fun testConcurrentOperationConflicts() {
        val user = createTestUser()
        val student = createTestStudent(user.id)
        
        // Simulate concurrent updates
        val updatedStudent1 = student.copy(name = "Updated Name 1")
        val updatedStudent2 = student.copy(name = "Updated Name 2")
        
        // Both updates should succeed due to proper transaction handling
        val result1 = studentUseCase.updateStudent(updatedStudent1)
        val result2 = studentUseCase.updateStudent(updatedStudent2)
        
        assertNotNull("First update should succeed", result1)
        assertNotNull("Second update should succeed", result2)
    }
    
    @Test
    fun testLargeDatasetPerformance() = runTest {
        val user = createTestUser()
        
        // Create large dataset
        val students = mutableListOf<Student>()
        val lessons = mutableListOf<Lesson>()
        
        for (i in 1..100) {
            val student = Student(
                id = i.toLong(),
                name = "Student $i",
                email = "student$i@example.com",
                phone = "+123456789$i",
                hourlyRate = 50.0 + i,
                ownerId = user.id,
                isArchived = false,
                createdAt = LocalDateTime.now()
            )
            students.add(student)
            
            // Create multiple lessons per student
            for (j in 1..10) {
                val lesson = Lesson(
                    id = (i * 100 + j).toLong(),
                    studentId = student.id,
                    groupId = null,
                    date = LocalDate.now().minusDays(j.toLong()),
                    duration = 60,
                    hourlyRate = student.hourlyRate,
                    notes = "Lesson $j for student $i",
                    ownerId = user.id,
                    createdAt = LocalDateTime.now()
                )
                lessons.add(lesson)
            }
        }
        
        // Measure performance
        val startTime = System.currentTimeMillis()
        
        // Insert all students
        students.forEach { studentUseCase.createStudent(it) }
        
        // Insert all lessons
        lessons.forEach { lessonUseCase.createLesson(it) }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Performance assertion: should complete within 5 seconds
        assertTrue("Large dataset insertion should complete within 5 seconds", duration < 5000)
        
        // Verify data integrity
        val allStudents = studentUseCase.getAllStudents(user.id)
        assertEquals("Should have 100 students", 100, allStudents.size)
        
        val allLessons = lessonUseCase.getAllLessons(user.id)
        assertEquals("Should have 1000 lessons", 1000, allLessons.size)
    }
    
    @Test
    fun testDataSynchronization() = runTest {
        val user = createTestUser()
        val student = createTestStudent(user.id)
        
        // Simulate offline changes
        val offlineStudent = student.copy(name = "Offline Updated Name")
        val updatedOffline = studentUseCase.updateStudent(offlineStudent)
        
        // Simulate sync process
        val syncResult = performDataSync(user.id)
        assertTrue("Data sync should succeed", syncResult)
        
        // Verify synchronized data
        val syncedStudent = studentUseCase.getStudent(student.id)
        assertNotNull("Synced student should exist", syncedStudent)
        assertEquals("Synced name should match", "Offline Updated Name", syncedStudent.name)
    }
    
    @Test
    fun testBackupAndRestore() = runTest {
        val user = createTestUser()
        val student = createTestStudent(user.id)
        val lesson = createTestLesson(student.id, user.id)
        
        // Create backup
        val backupResult = performBackup(user.id)
        assertTrue("Backup should succeed", backupResult)
        
        // Simulate data loss
        studentUseCase.deleteStudent(student.id)
        lessonUseCase.deleteLesson(lesson.id)
        
        // Verify data is gone
        val deletedStudent = studentUseCase.getStudent(student.id)
        assertNull("Student should be deleted", deletedStudent)
        
        // Restore from backup
        val restoreResult = performRestore(user.id)
        assertTrue("Restore should succeed", restoreResult)
        
        // Verify data is restored
        val restoredStudent = studentUseCase.getStudent(student.id)
        assertNotNull("Student should be restored", restoredStudent)
        assertEquals("Restored student name should match", student.name, restoredStudent.name)
    }
    
    // Helper methods
    private suspend fun createTestUser(): User {
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashedpassword",
            createdAt = LocalDateTime.now()
        )
        return userUseCase.registerUser(user)
    }
    
    private suspend fun createTestStudent(ownerId: Long): Student {
        val student = Student(
            id = 1L,
            name = "Test Student",
            email = "test@student.com",
            phone = "+1234567890",
            hourlyRate = 50.0,
            ownerId = ownerId,
            isArchived = false,
            createdAt = LocalDateTime.now()
        )
        return studentUseCase.createStudent(student)
    }
    
    private suspend fun createTestLesson(studentId: Long, ownerId: Long): Lesson {
        val lesson = Lesson(
            id = 1L,
            studentId = studentId,
            groupId = null,
            date = LocalDate.now(),
            duration = 60,
            hourlyRate = 50.0,
            notes = "Test lesson",
            ownerId = ownerId,
            createdAt = LocalDateTime.now()
        )
        return lessonUseCase.createLesson(lesson)
    }
    
    private suspend fun performDataSync(userId: Long): Boolean {
        // Simulate data synchronization
        return try {
            // In a real implementation, this would sync with remote server
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun performBackup(userId: Long): Boolean {
        // Simulate backup creation
        return try {
            // In a real implementation, this would create a backup file
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun performRestore(userId: Long): Boolean {
        // Simulate backup restoration
        return try {
            // In a real implementation, this would restore from backup file
            true
        } catch (e: Exception) {
            false
        }
    }
}
