package gr.eduinvoice.domain.testfixtures

import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.GroupStudentCrossRef
import java.time.LocalDate
import java.time.LocalTime

/**
 * Top-level helper functions for creating test data objects
 */

/**
 * Creates a test student with optional overrides
 */
fun createTestStudent(
    id: Long = 0L,
    ownerId: Long = 1L,
    name: String = "Test Student",
    surname: String = "Test Surname",
    parentMobile: String = "+30123456789",
    parentEmail: String? = "test@example.com",
    className: String = "Test Class",
    rate: Double = 25.0,
    rateType: String = gr.eduinvoice.data.model.RateTypes.HOURLY,
    isActive: Boolean = true
): Student = TestStudentBuilder()
    .withId(id)
    .withOwnerId(ownerId)
    .withName(name)
    .withSurname(surname)
    .withParentMobile(parentMobile)
    .withParentEmail(parentEmail)
    .withClassName(className)
    .withRate(rate)
    .withRateType(rateType)
    .withIsActive(isActive)
    .build()

/**
 * Creates a test lesson with optional overrides
 */
fun createTestLesson(
    id: Long = 0L,
    ownerId: Long = 1L,
    studentId: Long = 1L,
    groupId: Long? = null,
    date: String = LocalDate.now().toString(),
    startTime: String = LocalTime.of(14, 0).toString(),
    durationMinutes: Int = 60,
    notes: String? = null,
    isPaid: Boolean = false,
    isInvoiced: Boolean = false
): Lesson = TestLessonBuilder()
    .withId(id)
    .withOwnerId(ownerId)
    .withStudentId(studentId)
    .withGroupId(groupId)
    .withDate(date)
    .withStartTime(startTime)
    .withDurationMinutes(durationMinutes)
    .withNotes(notes)
    .withIsPaid(isPaid)
    .withIsInvoiced(isInvoiced)
    .build()

/**
 * Creates a test group with optional overrides
 */
fun createTestGroup(
    id: Long = 0L,
    ownerId: Long = 1L,
    name: String = "Test Group"
): StudentGroup = TestGroupBuilder()
    .withId(id)
    .withOwnerId(ownerId)
    .withName(name)
    .build()

/**
 * Creates a test cross reference with optional overrides
 */
fun createTestCrossRef(
    groupId: Long = 1L,
    studentId: Long = 1L,
    ownerId: Long = 1L
): GroupStudentCrossRef = TestCrossRefBuilder()
    .withGroupId(groupId)
    .withStudentId(studentId)
    .withOwnerId(ownerId)
    .build()

/**
 * Creates multiple test students
 */
fun createTestStudents(
    count: Int = 3,
    ownerId: Long = 1L,
    mutate: (Int) -> Unit = {}
): List<Student> = (1..count).map { index ->
    createTestStudent(
        id = index.toLong(),
        name = "Student $index",
        ownerId = ownerId
    )
}

/**
 * Creates multiple test lessons for a student
 */
fun createTestLessonsForStudent(
    studentId: Long,
    count: Int = 3,
    ownerId: Long = 1L
): List<Lesson> = (1..count).map { index ->
    createTestLesson(
        id = index.toLong(),
        studentId = studentId,
        ownerId = ownerId,
        date = LocalDate.now().plusDays(index.toLong()).toString()
    )
}

/**
 * Creates multiple test groups
 */
fun createTestGroups(
    count: Int = 2,
    ownerId: Long = 1L
): List<StudentGroup> = (1..count).map { index ->
    createTestGroup(
        id = index.toLong(),
        name = "Group $index",
        ownerId = ownerId
    )
}

/**
 * Creates a test lesson with a student
 */
fun createTestLessonWithStudent(
    student: Student,
    lessonId: Long = 0L,
    ownerId: Long = 1L
): Lesson = createTestLesson(
    id = lessonId,
    studentId = student.id,
    ownerId = ownerId
)

/**
 * Creates a complete test dataset
 */
fun createCompleteTestDataset(
    studentCount: Int = 3,
    groupCount: Int = 2,
    lessonsPerStudent: Int = 2,
    ownerId: Long = 1L
): TestDataset {
    val students = createTestStudents(studentCount, ownerId)
    val groups = createTestGroups(groupCount, ownerId)
    val lessons = students.flatMap { student ->
        createTestLessonsForStudent(student.id, lessonsPerStudent, ownerId)
    }
    val crossRefs = mutableListOf<GroupStudentCrossRef>()
    
    // Add some students to groups
    students.take(groupCount).forEachIndexed { index, student ->
        crossRefs.add(createTestCrossRef(
            groupId = groups[index].id,
            studentId = student.id,
            ownerId = ownerId
        ))
    }
    
    return TestDataset(
        students = students,
        groups = groups,
        lessons = lessons,
        crossRefs = crossRefs
    )
}

/**
 * Data class representing a complete test dataset
 */
data class TestDataset(
    val students: List<Student>,
    val groups: List<StudentGroup>,
    val lessons: List<Lesson>,
    val crossRefs: List<GroupStudentCrossRef>
)
