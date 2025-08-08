package gr.eduinvoice.testinfrastructure

import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.User
import gr.eduinvoice.data.database.LessonWithStudent
import java.time.LocalDate
import java.time.LocalTime

/**
 * Test data factory for creating test objects
 */
object TestDataFactory {
    
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
    
    fun createTestLesson(
        id: Long = 1L,
        studentId: Long = 1L,
        ownerId: Long = 1L,
        date: String = LocalDate.now().toString(),
        durationMinutes: Int = 60
    ): Lesson = Lesson(
        id = id,
        studentId = studentId,
        date = date,
        startTime = "10:00",
        durationMinutes = durationMinutes,
        notes = "Test lesson",
        ownerId = ownerId
    )
    
    fun createTestLessonWithStudent(
        lessonId: Long = 1L,
        studentId: Long = 1L,
        ownerId: Long = 1L
    ): LessonWithStudent = LessonWithStudent(
        lesson = createTestLesson(lessonId, studentId, ownerId),
        student = createTestStudent(studentId, ownerId)
    )
    
    fun createTestGroup(
        id: Long = 1L,
        ownerId: Long = 1L,
        name: String = "Test Group"
    ): StudentGroup = StudentGroup(
        id = id,
        ownerId = ownerId,
        name = name
    )
    
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
}
