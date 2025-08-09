package gr.eduinvoice.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.testinfrastructure.BaseTest
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

/**
 * Integration tests for complete user flows and error recovery scenarios.
 * Tests end-to-end functionality and system resilience.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserFlowIntegrationTest : BaseTest() {
    
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
    fun testCompleteUserJourney() = runTest {
        // Step 1: User Registration and Authentication
        val user = createTestUser()
        assertTrue("User should be created with valid ID", user.id > 0)
        
        // Step 2: Create Students
        val students = createTestStudents(user.id, 3)
        assertEquals("Should create 3 students", 3, students.size)
        
        // Step 3: Create Groups
        val groups = createTestGroups(user.id, 2)
        assertEquals("Should create 2 groups", 2, groups.size)
        
        // Step 4: Add Students to Groups
        students.forEachIndexed { index, student ->
            val group = groups[index % groups.size]
            groupUseCases.addStudentToGroup(
                gr.eduinvoice.data.model.GroupStudentCrossRef(
                    groupId = group.id,
                    studentId = student.id,
                    ownerId = user.id
                )
            )
        }
        
        // Step 5: Create Lessons
        val lessons = createTestLessons(students, 5)
        assertEquals("Should create 5 lessons", 5, lessons.size)
        
        // Step 6: Verify Data Integrity
        val activeStudents = studentUseCases.getActiveStudents(user.id).first()
        assertEquals("Should have 3 active students", 3, activeStudents.size)
        
        val allGroups = groupUseCases.getAllGroups(user.id).first()
        assertEquals("Should have 2 groups", 2, allGroups.size)
        
        val allLessons = lessonUseCases.getAllLessons().first()
        assertEquals("Should have 5 lessons", 5, allLessons.size)
        
        // Step 7: Test Search and Pagination
        val searchResults = studentUseCases.searchStudentsPaginated(user.id, "Test", 10, 0)
        assertTrue("Should find students with search", searchResults.isNotEmpty())
        
        val paginatedStudents = studentUseCases.getStudentsPaginated(user.id, 2, 0)
        assertEquals("Should return 2 students per page", 2, paginatedStudents.size)
        
        // Step 8: Test Data Modifications
        val firstStudent = students.first()
        val updatedStudent = firstStudent.copy(name = "Updated_${firstStudent.name}")
        studentUseCases.updateStudent(updatedStudent)
        
        val retrievedStudent = studentUseCases.getStudentById(firstStudent.id, user.id).first()
        assertEquals("Student should be updated", "Updated_${firstStudent.name}", retrievedStudent?.name)
        
        // Step 9: Test Soft Delete and Restore
        studentUseCases.softDeleteStudent(firstStudent.id, user.id)
        val archivedStudents = studentUseCases.getArchivedStudents(user.id).first()
        assertEquals("Should have 1 archived student", 1, archivedStudents.size)
        
        studentUseCases.restoreStudent(firstStudent.id, user.id)
        val restoredActiveStudents = studentUseCases.getActiveStudents(user.id).first()
        assertEquals("Should have 3 active students after restore", 3, restoredActiveStudents.size)
    }
    
    @Test
    fun testErrorRecoveryScenarios() = runTest {
        val user = createTestUser()
        
        // Test 1: Invalid Student Creation
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
            fail("Should throw exception for invalid student")
        } catch (e: Exception) {
            // Expected error
            assertTrue("Should handle invalid student gracefully", e.message?.isNotEmpty() == true)
        }
        
        // Test 2: Non-existent Student Access
        try {
            val nonExistentStudent = studentUseCases.getStudentById(999999L, user.id).first()
            assertNull("Should return null for non-existent student", nonExistentStudent)
        } catch (e: Exception) {
            // Should handle gracefully
            assertTrue("Should handle non-existent student gracefully", e.message?.isNotEmpty() == true)
        }
        
        // Test 3: Database Integrity After Errors
        val validStudent = createTestStudent(user.id, "Valid_Student")
        val studentId = studentUseCases.insertStudent(validStudent)
        assertTrue("Should create valid student after errors", studentId > 0)
        
        val activeStudents = studentUseCases.getActiveStudents(user.id).first()
        assertEquals("Should have 1 active student", 1, activeStudents.size)
    }
    
    @Test
    fun testDatabaseCorruptionRecovery() = runTest {
        val user = createTestUser()
        val students = createTestStudents(user.id, 5)
        
        // Simulate database corruption by creating invalid data
        try {
            // This would normally be handled by database constraints
            // For testing, we simulate the scenario
            val corruptedStudent = Student(
                ownerId = user.id,
                name = "Corrupted_Student",
                surname = "",
                parentMobile = "",
                className = "",
                rate = Double.NaN // Invalid rate
            )
            studentUseCases.insertStudent(corruptedStudent)
        } catch (e: Exception) {
            // Expected error for corrupted data
            assertTrue("Should handle corrupted data gracefully", e.message?.isNotEmpty() == true)
        }
        
        // Verify system can still operate normally
        val activeStudents = studentUseCases.getActiveStudents(user.id).first()
        assertEquals("Should still have 5 valid students", 5, activeStudents.size)
        
        // Test normal operations still work
        val newStudent = createTestStudent(user.id, "Recovery_Student")
        val newStudentId = studentUseCases.insertStudent(newStudent)
        assertTrue("Should be able to create new student after corruption", newStudentId > 0)
    }
    
    @Test
    fun testNetworkFailureHandling() = runTest {
        val user = createTestUser()
        
        // Simulate network-like failures by introducing delays and timeouts
        val startTime = System.currentTimeMillis()
        
        try {
            // Simulate slow operations
            val students = createTestStudents(user.id, 3)
            
            // Simulate network timeout scenario
            val timeoutOperation = async {
                delay(TestConfiguration.Network.simulatedTimeout)
                studentUseCases.getActiveStudents(user.id).first()
            }
            
            // This should complete within reasonable time
            val result = timeoutOperation.await()
            assertTrue("Should handle timeout gracefully", result.isNotEmpty())
            
        } catch (e: Exception) {
            // Should handle network-like failures gracefully
            assertTrue("Should handle network failures gracefully", e.message?.isNotEmpty() == true)
        }
        
        val totalTime = System.currentTimeMillis() - startTime
        assertTrue("Should complete within reasonable time", totalTime < TestConfiguration.Network.maxOperationTime)
    }
    
    @Test
    fun testDataValidationErrors() = runTest {
        val user = createTestUser()
        
        // Test various validation scenarios
        val validationTests = listOf(
            // Empty name
            Student(ownerId = user.id, name = "", surname = "", parentMobile = "", className = "", rate = 20.0),
            // Negative rate
            Student(ownerId = user.id, name = "Test", surname = "", parentMobile = "", className = "", rate = -10.0),
            // Zero rate
            Student(ownerId = user.id, name = "Test", surname = "", parentMobile = "", className = "", rate = 0.0),
            // Very long name
            Student(ownerId = user.id, name = "A".repeat(1000), surname = "", parentMobile = "", className = "", rate = 20.0)
        )
        
        validationTests.forEachIndexed { index, invalidStudent ->
            try {
                studentUseCases.insertStudent(invalidStudent)
                fail("Should reject invalid student ${index}")
            } catch (e: Exception) {
                // Expected validation error
                assertTrue("Should provide validation error message", e.message?.isNotEmpty() == true)
            }
        }
        
        // Verify system still works with valid data
        val validStudent = createTestStudent(user.id, "Valid_After_Validation")
        val validStudentId = studentUseCases.insertStudent(validStudent)
        assertTrue("Should accept valid student after validation errors", validStudentId > 0)
    }
    
    @Test
    fun testConcurrentOperationConflicts() = runTest {
        val user = createTestUser()
        val student = createTestStudent(user.id, "Concurrent_Test_Student")
        val studentId = studentUseCases.insertStudent(student)
        
        // Simulate concurrent modifications
        val concurrentOperations = 10
        val jobs = (1..concurrentOperations).map { operationId ->
            async {
                try {
                    val currentStudent = studentUseCases.getStudentById(studentId, user.id).first()
                    currentStudent?.let {
                        val updatedStudent = it.copy(
                            name = "Concurrent_${operationId}",
                            rate = it.rate + operationId
                        )
                        studentUseCases.updateStudent(updatedStudent)
                    }
                    operationId
                } catch (e: Exception) {
                    // Some conflicts are expected
                    -operationId
                }
            }
        }
        
        val results = jobs.awaitAll()
        val successfulOperations = results.count { it > 0 }
        val failedOperations = results.count { it < 0 }
        
        // Verify some operations succeeded
        assertTrue("Should have some successful operations", successfulOperations > 0)
        
        // Verify final state is consistent
        val finalStudent = studentUseCases.getStudentById(studentId, user.id).first()
        assertNotNull("Final student should exist", finalStudent)
        assertTrue("Final student should have updated name", finalStudent!!.name.startsWith("Concurrent_"))
    }
    
    @Test
    fun testLargeDatasetPerformance() = runTest {
        val user = createTestUser()
        
        // Create large dataset
        val largeStudentCount = TestConfiguration.DataSize.mediumStudentCount
        val largeLessonCount = TestConfiguration.DataSize.mediumLessonCount
        
        val startTime = System.currentTimeMillis()
        
        val students = createTestStudents(user.id, largeStudentCount)
        val lessons = createTestLessons(students, largeLessonCount)
        
        val creationTime = System.currentTimeMillis() - startTime
        
        // Verify performance
        assertTrue("Dataset creation took too long: ${creationTime}ms", 
                  creationTime < TestConfiguration.Performance.maxInsertionTime)
        
        // Test query performance
        val queryStartTime = System.currentTimeMillis()
        val allStudents = studentUseCases.getActiveStudents(user.id).first()
        val queryTime = System.currentTimeMillis() - queryStartTime
        
        assertTrue("Query took too long: ${queryTime}ms", 
                  queryTime < TestConfiguration.Performance.maxQueryTime)
        assertEquals("Should have correct number of students", largeStudentCount, allStudents.size)
    }
    
    @Test
    fun testDataSynchronization() = runTest {
        val user = createTestUser()
        val students = createTestStudents(user.id, 3)
        
        // Simulate data synchronization scenario
        val syncOperations = listOf(
            // Add new student
            { createTestStudent(user.id, "Sync_New_Student") },
            // Update existing student
            { students.first().copy(name = "Sync_Updated_${students.first().name}") },
            // Delete student (soft delete)
            { students.last() }
        )
        
        val syncResults = mutableListOf<Boolean>()
        
        syncOperations.forEach { operation ->
            try {
                when {
                    operation() == students.last() -> {
                        // Delete operation
                        studentUseCases.softDeleteStudent(operation().id, user.id)
                        syncResults.add(true)
                    }
                    operation().id == 0L -> {
                        // Insert operation
                        val newStudent = operation()
                        val newId = studentUseCases.insertStudent(newStudent)
                        syncResults.add(newId > 0)
                    }
                    else -> {
                        // Update operation
                        val updatedStudent = operation()
                        studentUseCases.updateStudent(updatedStudent)
                        syncResults.add(true)
                    }
                }
            } catch (e: Exception) {
                syncResults.add(false)
            }
        }
        
        // Verify synchronization results
        val successCount = syncResults.count { it }
        assertTrue("Synchronization should have high success rate", 
                  successCount >= syncOperations.size * TestConfiguration.SuccessRates.syncSuccessRate)
        
        // Verify final state
        val finalStudents = studentUseCases.getActiveStudents(user.id).first()
        val archivedStudents = studentUseCases.getArchivedStudents(user.id).first()
        
        assertTrue("Should have active students after sync", finalStudents.isNotEmpty())
        assertTrue("Should have archived students after sync", archivedStudents.isNotEmpty())
    }
    
    @Test
    fun testBackupAndRestore() = runTest {
        val user = createTestUser()
        val students = createTestStudents(user.id, 5)
        val groups = createTestGroups(user.id, 2)
        val lessons = createTestLessons(students, 10)
        
        // Simulate backup operation
        val backupData = performBackup(user.id)
        assertTrue("Backup should contain data", backupData.isNotEmpty())
        
        // Simulate data loss scenario
        students.forEach { student ->
            studentUseCases.softDeleteStudent(student.id, user.id)
        }
        
        // Verify data is "lost"
        val activeStudentsAfterLoss = studentUseCases.getActiveStudents(user.id).first()
        assertEquals("Should have no active students after loss", 0, activeStudentsAfterLoss.size)
        
        // Simulate restore operation
        val restoreSuccess = performRestore(user.id, backupData)
        assertTrue("Restore should succeed", restoreSuccess)
        
        // Verify data is restored
        val restoredStudents = studentUseCases.getActiveStudents(user.id).first()
        assertEquals("Should have restored students", 5, restoredStudents.size)
        
        val restoredGroups = groupUseCases.getAllGroups(user.id).first()
        assertEquals("Should have restored groups", 2, restoredGroups.size)
        
        val restoredLessons = lessonUseCases.getAllLessons().first()
        assertEquals("Should have restored lessons", 10, restoredLessons.size)
    }
    
    // Helper functions
    private suspend fun createTestUser(): User {
        val user = User(
            username = "integrationuser_${System.currentTimeMillis()}",
            passwordHash = "integration_hash",
            fullName = "Integration Test User"
        )
        val userId = userUseCases.createUser(user)
        return user.copy(id = userId)
    }
    
    private fun createTestStudent(ownerId: Long, name: String): Student {
        return Student(
            ownerId = ownerId,
            name = name,
            surname = "Integration_Surname",
            parentMobile = "+30123456789",
            parentEmail = "integration@test.com",
            className = "Integration_Class",
            rate = 25.0
        )
    }
    
    private suspend fun createTestStudents(ownerId: Long, count: Int): List<Student> {
        val students = mutableListOf<Student>()
        
        repeat(count) { index ->
            val student = createTestStudent(ownerId, "Integration_Student_${index}")
            val studentId = studentUseCases.insertStudent(student)
            students.add(student.copy(id = studentId))
        }
        
        return students
    }
    
    private suspend fun createTestGroups(ownerId: Long, count: Int): List<StudentGroup> {
        val groups = mutableListOf<StudentGroup>()
        
        repeat(count) { index ->
            val group = StudentGroup(
                ownerId = ownerId,
                name = "Integration_Group_${index}"
            )
            val groupId = groupUseCases.insertGroup(group)
            groups.add(group.copy(id = groupId))
        }
        
        return groups
    }
    
    private suspend fun createTestLessons(students: List<Student>, count: Int): List<Lesson> {
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
                notes = "Integration lesson ${index}",
                ownerId = student.ownerId
            )
            
            val lessonId = lessonUseCases.addLesson(lesson)
            lessons.add(lesson.copy(id = lessonId))
        }
        
        return lessons
    }
    
    private suspend fun performDataSync(userId: Long): Boolean {
        // Simulate data synchronization
        return try {
            val students = studentUseCases.getActiveStudents(userId).first()
            val groups = groupUseCases.getAllGroups(userId).first()
            val lessons = lessonUseCases.getAllLessons().first()
            
            // Simulate sync operations
            students.isNotEmpty() && groups.isNotEmpty() && lessons.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun performBackup(userId: Long): Map<String, Any> {
        // Simulate backup operation
        return mapOf(
            "students" to studentUseCases.getActiveStudents(userId).first(),
            "groups" to groupUseCases.getAllGroups(userId).first(),
            "lessons" to lessonUseCases.getAllLessons().first(),
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    private suspend fun performRestore(userId: Long, backupData: Map<String, Any>): Boolean {
        // Simulate restore operation
        return try {
            @Suppress("UNCHECKED_CAST")
            val students = backupData["students"] as? List<Student> ?: emptyList()
            val groups = backupData["groups"] as? List<StudentGroup> ?: emptyList()
            val lessons = backupData["lessons"] as? List<Lesson> ?: emptyList()
            
            // Restore students
            students.forEach { student ->
                if (student.id == 0L) {
                    studentUseCases.insertStudent(student)
                } else {
                    studentUseCases.updateStudent(student)
                }
            }
            
            // Restore groups
            groups.forEach { group ->
                if (group.id == 0L) {
                    groupUseCases.insertGroup(group)
                } else {
                    groupUseCases.updateGroup(group)
                }
            }
            
            // Restore lessons
            lessons.forEach { lesson ->
                if (lesson.id == 0L) {
                    lessonUseCases.addLesson(lesson)
                } else {
                    lessonUseCases.updateLesson(lesson)
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
}
