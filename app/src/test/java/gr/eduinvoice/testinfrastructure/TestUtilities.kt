package gr.eduinvoice.testinfrastructure

import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.database.LessonWithStudent
import java.time.LocalDate
import java.time.LocalTime
import java.io.File

/**
 * Test utilities for creating test data and managing test resources
 */
object TestUtilities {
    
    /**
     * Creates a test student with default values
     */
    fun createTestStudent(
        id: Long = 1L,
        ownerId: Long = 1L,
        name: String = "Test Student",
        rate: Double = 25.0
    ): Student = Student(
        id = id,
        ownerId = ownerId,
        name = name,
        surname = "Test Surname",
        parentMobile = "+30123456789",
        parentEmail = "test@example.com",
        className = "Test Class",
        rate = rate
    )
    
    /**
     * Creates a test lesson with default values
     */
    fun createTestLesson(
        id: Long = 1L,
        studentId: Long = 1L,
        ownerId: Long = 1L,
        date: String = LocalDate.now().toString(),
        durationMinutes: Int = 60
    ): Lesson = Lesson.create(
        studentId = studentId,
        date = LocalDate.parse(date),
        startTime = LocalTime.of(10, 0),
        durationMinutes = durationMinutes,
        notes = "Test lesson",
        ownerId = ownerId
    )
    
    /**
     * Creates a test lesson with a specific student
     */
    fun createTestLessonWithStudent(
        student: Student,
        lessonId: Long = 1L,
        date: String = LocalDate.now().toString(),
        durationMinutes: Int = 60
    ): Lesson = Lesson.create(
        studentId = student.id,
        date = LocalDate.parse(date),
        startTime = LocalTime.of(10, 0),
        durationMinutes = durationMinutes,
        notes = "Test lesson",
        ownerId = student.ownerId
    )
    
    /**
     * Creates a test group with default values
     */
    fun createTestGroup(
        id: Long = 1L,
        ownerId: Long = 1L,
        name: String = "Test Group"
    ): StudentGroup = StudentGroup(
        id = id,
        ownerId = ownerId,
        name = name
    )
    
    /**
     * Creates a test user with default values
     */
    fun createTestUser(
        id: Long = 1L,
        username: String = "testuser",
        fullName: String = "Test User"
    ): User = User(
        id = id,
        username = username,
        passwordHash = "test_hash",
        fullName = fullName
    )
    
    /**
     * Creates a test directory for file operations
     */
    fun createTestDirectory(): File {
        val testDir = File(System.getProperty("java.io.tmpdir"), "eduinvoice_test_${System.currentTimeMillis()}")
        testDir.mkdirs()
        return testDir
    }
    
    /**
     * Cleans up test resources
     */
    fun cleanupTestResources(file: File) {
        if (file.exists()) {
            if (file.isDirectory) {
                file.listFiles()?.forEach { cleanupTestResources(it) }
            }
            file.delete()
        }
    }
    
    /**
     * Creates a lesson with student for testing
     */
    fun createLessonWithStudent(
        student: Student,
        lesson: Lesson
    ): LessonWithStudent = LessonWithStudent(
        lesson = lesson,
        student = student
    )
    
    /**
     * Creates a group student cross reference
     */
    fun createGroupStudentCrossRef(
        groupId: Long,
        studentId: Long
    ): GroupStudentCrossRef = GroupStudentCrossRef(
        groupId = groupId,
        studentId = studentId
    )
}
