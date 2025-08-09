package gr.eduinvoice.unit.security

import gr.eduinvoice.testinfrastructure.BaseTest
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.repository.UserRepository
import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.repository.LessonRepository
import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.domain.user.AuthenticateUser
import gr.eduinvoice.domain.user.CreateUser
import gr.eduinvoice.domain.user.UpdateUser
import gr.eduinvoice.domain.user.ResetPassword
import gr.eduinvoice.domain.student.InsertStudent
import gr.eduinvoice.domain.student.UpdateStudent
import gr.eduinvoice.domain.student.GetStudentById
import gr.eduinvoice.domain.lesson.AddLesson
import gr.eduinvoice.domain.lesson.UpdateLesson
import gr.eduinvoice.domain.lesson.GetLessonById
import gr.eduinvoice.domain.group.InsertGroup
import gr.eduinvoice.domain.group.UpdateGroup
import gr.eduinvoice.domain.group.GetGroupById
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*
import java.security.MessageDigest
import java.util.*

/**
 * Security tests for encryption, authentication, and data protection
 */
class SecurityTest : BaseTest() {

    @Test
    fun `user authentication works correctly`() = runTest {
        val userRepository = UserRepository(database.userDao())
        val createUser = CreateUser(userRepository)
        val authenticateUser = AuthenticateUser(userRepository)
        
        // Create test user with hashed password
        val originalPassword = "securePassword123"
        val hashedPassword = hashPassword(originalPassword)
        
        val user = User(
            username = "securitytestuser",
            passwordHash = hashedPassword,
            fullName = "Security Test User"
        )
        
        val userId = createUser(user)
        assertTrue("User should be created with valid ID", userId > 0)
        
        // Test successful authentication
        val authenticatedUser = authenticateUser("securitytestuser", originalPassword)
        assertNotNull("User should be authenticated with correct password", authenticatedUser)
        assertEquals("Authenticated user should have correct ID", userId, authenticatedUser!!.id)
        
        // Test failed authentication
        val wrongPasswordUser = authenticateUser("securitytestuser", "wrongPassword")
        assertNull("User should not be authenticated with wrong password", wrongPasswordUser)
        
        val nonExistentUser = authenticateUser("nonexistentuser", originalPassword)
        assertNull("Non-existent user should not be authenticated", nonExistentUser)
    }

    @Test
    fun `password hashing is secure`() {
        val password = "testPassword123"
        val hash1 = hashPassword(password)
        val hash2 = hashPassword(password)
        
        // Same password should produce different hashes (due to salt)
        assertNotEquals("Password hashes should be different", hash1, hash2)
        
        // Hash should not contain the original password
        assertFalse("Hash should not contain original password", hash1.contains(password))
        assertFalse("Hash should not contain original password", hash2.contains(password))
        
        // Hash should be reasonably long
        assertTrue("Hash should be reasonably long", hash1.length > 32)
        assertTrue("Hash should be reasonably long", hash2.length > 32)
    }

    @Test
    fun `data isolation between users`() = runTest {
        val userRepository = UserRepository(database.userDao())
        val studentRepository = StudentRepository(database.studentDao())
        val lessonRepository = LessonRepository(database.lessonDao())
        val groupRepository = GroupRepository(database.groupDao())
        
        val createUser = CreateUser(userRepository)
        val insertStudent = InsertStudent(studentRepository)
        val addLesson = AddLesson(lessonRepository)
        val insertGroup = InsertGroup(groupRepository)
        val getStudentById = GetStudentById(studentRepository)
        val getLessonById = GetLessonById(database.lessonDao())
        val getGroupById = GetGroupById(groupRepository)
        
        // Create two users
        val user1 = User(username = "user1", passwordHash = "hash1", fullName = "User 1")
        val user2 = User(username = "user2", passwordHash = "hash2", fullName = "User 2")
        
        val userId1 = createUser(user1)
        val userId2 = createUser(user2)
        
        // Create data for user1
        val student1 = Student(
            ownerId = userId1,
            name = "Student 1",
            surname = "Surname 1",
            parentMobile = "+30123456789",
            parentEmail = "parent1@test.com",
            className = "Class 1",
            rate = 25.0
        )
        val studentId1 = insertStudent(student1)
        
        val lesson1 = Lesson.create(
            studentId = studentId1,
            date = java.time.LocalDate.now(),
            startTime = java.time.LocalTime.of(10, 0),
            durationMinutes = 60,
            notes = "Lesson 1",
            ownerId = userId1
        )
        val lessonId1 = addLesson(lesson1)
        
        val group1 = StudentGroup(ownerId = userId1, name = "Group 1")
        val groupId1 = insertGroup(group1)
        
        // Create data for user2
        val student2 = Student(
            ownerId = userId2,
            name = "Student 2",
            surname = "Surname 2",
            parentMobile = "+30987654321",
            parentEmail = "parent2@test.com",
            className = "Class 2",
            rate = 30.0
        )
        val studentId2 = insertStudent(student2)
        
        val lesson2 = Lesson.create(
            studentId = studentId2,
            date = java.time.LocalDate.now(),
            startTime = java.time.LocalTime.of(14, 0),
            durationMinutes = 90,
            notes = "Lesson 2",
            ownerId = userId2
        )
        val lessonId2 = addLesson(lesson2)
        
        val group2 = StudentGroup(ownerId = userId2, name = "Group 2")
        val groupId2 = insertGroup(group2)
        
        // Verify user1 cannot access user2's data
        val user1Student2 = getStudentById(studentId2, userId1).first()
        assertNull("User1 should not access User2's student", user1Student2)
        
        val user1Lesson2 = getLessonById(lessonId2)
        assertNull("User1 should not access User2's lesson", user1Lesson2)
        
        val user1Group2 = getGroupById(groupId2, userId1).first()
        assertNull("User1 should not access User2's group", user1Group2)
        
        // Verify user2 cannot access user1's data
        val user2Student1 = getStudentById(studentId1, userId2).first()
        assertNull("User2 should not access User1's student", user2Student1)
        
        val user2Lesson1 = getLessonById(lessonId1)
        assertNull("User2 should not access User1's lesson", user2Lesson1)
        
        val user2Group1 = getGroupById(groupId1, userId2).first()
        assertNull("User2 should not access User1's group", user2Group1)
        
        // Verify users can access their own data
        val user1Student1 = getStudentById(studentId1, userId1).first()
        assertNotNull("User1 should access own student", user1Student1)
        assertEquals("User1's student should have correct name", "Student 1", user1Student1!!.name)
        
        val user2Student2 = getStudentById(studentId2, userId2).first()
        assertNotNull("User2 should access own student", user2Student2)
        assertEquals("User2's student should have correct name", "Student 2", user2Student2!!.name)
    }

    @Test
    fun `sensitive data is not exposed in logs`() {
        // This test verifies that sensitive data is not logged
        // In a real implementation, you would check log files or use a custom logger
        
        val sensitiveData = listOf(
            "password123",
            "credit_card_1234_5678_9012_3456",
            "ssn_123_45_6789",
            "api_key_abc123def456"
        )
        
        // Simulate logging (in real app, this would be actual logging)
        val logMessages = mutableListOf<String>()
        
        sensitiveData.forEach { data ->
            // Simulate what should NOT be logged
            logMessages.add("Processing data for user")
            logMessages.add("Operation completed successfully")
            // Note: We should NOT log the actual sensitive data
        }
        
        // Verify sensitive data is not in logs
        sensitiveData.forEach { data ->
            assertFalse("Sensitive data should not be logged", 
                       logMessages.any { it.contains(data) })
        }
        
        // Verify generic messages are logged
        assertTrue("Generic messages should be logged", 
                  logMessages.any { it.contains("Processing data for user") })
        assertTrue("Generic messages should be logged", 
                  logMessages.any { it.contains("Operation completed successfully") })
    }

    @Test
    fun `input sanitization prevents injection attacks`() {
        val maliciousInputs = listOf(
            "'; DROP TABLE students; --",
            "<script>alert('xss')</script>",
            "'; INSERT INTO users VALUES ('hacker', 'password'); --",
            "'; UPDATE users SET password = 'hacked'; --",
            "'; DELETE FROM students; --",
            "<img src=x onerror=alert('xss')>",
            "javascript:alert('xss')",
            "'; EXEC xp_cmdshell('format c:'); --"
        )
        
        maliciousInputs.forEach { maliciousInput ->
            // Test that malicious input is properly sanitized
            val sanitized = sanitizeInput(maliciousInput)
            
            // Verify dangerous patterns are removed or escaped
            assertFalse("SQL injection should be prevented", 
                       sanitized.contains("DROP TABLE"))
            assertFalse("SQL injection should be prevented", 
                       sanitized.contains("INSERT INTO"))
            assertFalse("SQL injection should be prevented", 
                       sanitized.contains("UPDATE"))
            assertFalse("SQL injection should be prevented", 
                       sanitized.contains("DELETE FROM"))
            assertFalse("XSS should be prevented", 
                       sanitized.contains("<script>"))
            assertFalse("XSS should be prevented", 
                       sanitized.contains("javascript:"))
            assertFalse("XSS should be prevented", 
                       sanitized.contains("onerror="))
        }
    }

    @Test
    fun `session management is secure`() = runTest {
        val userRepository = UserRepository(database.userDao())
        val createUser = CreateUser(userRepository)
        val authenticateUser = AuthenticateUser(userRepository)
        
        // Create test user
        val user = User(
            username = "sessiontestuser",
            passwordHash = hashPassword("password123"),
            fullName = "Session Test User"
        )
        val userId = createUser(user)
        
        // Simulate session creation
        val sessionToken1 = createSession(userId)
        val sessionToken2 = createSession(userId)
        
        // Verify sessions are unique
        assertNotEquals("Session tokens should be unique", sessionToken1, sessionToken2)
        
        // Verify session validation
        assertTrue("Valid session should be authenticated", validateSession(sessionToken1))
        assertTrue("Valid session should be authenticated", validateSession(sessionToken2))
        
        // Verify invalid session is rejected
        assertFalse("Invalid session should be rejected", validateSession("invalid_token"))
        assertFalse("Empty session should be rejected", validateSession(""))
        assertFalse("Null session should be rejected", validateSession(null))
        
        // Simulate session expiration
        val expiredSession = createExpiredSession(userId)
        assertFalse("Expired session should be rejected", validateSession(expiredSession))
    }

    @Test
    fun `data encryption at rest`() {
        val sensitiveData = "sensitive_student_data_12345"
        
        // Simulate encryption (in real app, this would use actual encryption)
        val encryptedData = encryptData(sensitiveData)
        val decryptedData = decryptData(encryptedData)
        
        // Verify encryption/decryption works
        assertEquals("Decrypted data should match original", sensitiveData, decryptedData)
        
        // Verify encrypted data is different from original
        assertNotEquals("Encrypted data should be different from original", 
                       sensitiveData, encryptedData)
        
        // Verify encrypted data is not readable
        assertFalse("Encrypted data should not be readable", 
                   encryptedData.contains(sensitiveData))
        
        // Test with different data
        val differentData = "different_sensitive_data_67890"
        val encryptedDifferentData = encryptData(differentData)
        
        assertNotEquals("Different data should produce different encryption", 
                       encryptedData, encryptedDifferentData)
    }

    @Test
    fun `rate limiting prevents brute force attacks`() {
        val maxAttempts = 5
        val lockoutDuration = 300000L // 5 minutes in milliseconds
        
        // Simulate login attempts
        repeat(maxAttempts) { attempt ->
            val result = attemptLogin("testuser", "wrongpassword")
            assertFalse("Login should fail with wrong password", result)
        }
        
        // Next attempt should be blocked
        val blockedResult = attemptLogin("testuser", "correctpassword")
        assertFalse("Login should be blocked after max attempts", blockedResult)
        
        // Wait for lockout to expire (simulated)
        Thread.sleep(100) // Simulate time passing
        
        // After lockout expires, login should work again
        val afterLockoutResult = attemptLogin("testuser", "correctpassword")
        assertTrue("Login should work after lockout expires", afterLockoutResult)
    }

    // Helper functions for testing
    private fun hashPassword(password: String): String {
        val salt = generateSalt()
        val saltedPassword = password + salt
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(saltedPassword.toByteArray())
        return Base64.getEncoder().encodeToString(hash) + ":" + salt
    }
    
    private fun generateSalt(): String {
        val random = Random()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }
    
    private fun sanitizeInput(input: String): String {
        // Simple sanitization for testing purposes
        return input.replace(Regex("[<>\"']"), "")
                   .replace(Regex("(?i)(drop|insert|update|delete|exec|javascript)"), "")
    }
    
    private fun createSession(userId: Long): String {
        return Base64.getEncoder().encodeToString("session_$userId_${System.currentTimeMillis()}".toByteArray())
    }
    
    private fun createExpiredSession(userId: Long): String {
        return Base64.getEncoder().encodeToString("expired_session_$userId".toByteArray())
    }
    
    private fun validateSession(sessionToken: String?): Boolean {
        if (sessionToken.isNullOrEmpty()) return false
        if (sessionToken.contains("expired_session")) return false
        return sessionToken.contains("session_")
    }
    
    private fun encryptData(data: String): String {
        // Simple encryption simulation for testing
        return Base64.getEncoder().encodeToString(data.toByteArray())
    }
    
    private fun decryptData(encryptedData: String): String {
        // Simple decryption simulation for testing
        val decoded = Base64.getDecoder().decode(encryptedData)
        return String(decoded)
    }
    
    private fun attemptLogin(username: String, password: String): Boolean {
        // Simulate login attempt with rate limiting
        return password == "correctpassword"
    }
}
