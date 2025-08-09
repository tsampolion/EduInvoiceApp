package gr.eduinvoice.test.support.extensions

import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.test.support.builders.*

/**
 * Extension functions for creating test data with convenient shortcuts
 */

/**
 * Creates a test student with default values
 */
fun createTestStudent(
    id: Long = 1L,
    ownerId: Long = 1L,
    name: String = "Test Student",
    rate: Double = 25.0
): Student = TestStudentBuilder()
    .withId(id)
    .withOwnerId(ownerId)
    .withName(name)
    .withRate(rate)
    .build()

/**
 * Creates a test lesson with default values
 */
fun createTestLesson(
    id: Long = 1L,
    studentId: Long = 1L,
    ownerId: Long = 1L,
    date: String = java.time.LocalDate.now().toString(),
    durationMinutes: Int = 60
): Lesson = TestLessonBuilder()
    .withId(id)
    .withStudentId(studentId)
    .withOwnerId(ownerId)
    .withDate(date)
    .withDurationMinutes(durationMinutes)
    .build()

/**
 * Creates a test group with default values
 */
fun createTestGroup(
    id: Long = 1L,
    ownerId: Long = 1L,
    name: String = "Test Group"
): StudentGroup = TestGroupBuilder()
    .withId(id)
    .withOwnerId(ownerId)
    .withName(name)
    .build()

/**
 * Creates a test cross-reference with default values
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
 * Creates multiple test students with sequential IDs
 */
fun createTestStudents(count: Int, ownerId: Long = 1L): List<Student> =
    TestStudentBuilder.createMultiple(count, ownerId)

/**
 * Creates multiple test lessons for a student
 */
fun createTestLessonsForStudent(
    studentId: Long,
    count: Int,
    ownerId: Long = 1L
): List<Lesson> = TestLessonBuilder.createMultipleForStudent(studentId, count, ownerId)

/**
 * Creates multiple test groups with sequential IDs
 */
fun createTestGroups(count: Int, ownerId: Long = 1L): List<StudentGroup> =
    TestGroupBuilder.createMultiple(count, ownerId)

/**
 * Creates cross-references for a group with multiple students
 */
fun createTestCrossReferencesForGroup(
    groupId: Long,
    studentIds: List<Long>,
    ownerId: Long = 1L
): List<GroupStudentCrossRef> = TestCrossRefBuilder.createForGroup(groupId, studentIds, ownerId)

/**
 * Creates cross-references for a student with multiple groups
 */
fun createTestCrossReferencesForStudent(
    studentId: Long,
    groupIds: List<Long>,
    ownerId: Long = 1L
): List<GroupStudentCrossRef> = TestCrossRefBuilder.createForStudent(studentId, groupIds, ownerId)

/**
 * Creates a complete test dataset with students, groups, and cross-references
 */
fun createCompleteTestDataset(
    studentCount: Int = 3,
    groupCount: Int = 2,
    ownerId: Long = 1L
): TestDataset {
    val students = createTestStudents(studentCount, ownerId)
    val groups = createTestGroups(groupCount, ownerId)
    
    // Create cross-references: each student belongs to one group
    val crossRefs = students.mapIndexed { index, student ->
        val groupIndex = index % groupCount
        createTestCrossRef(
            groupId = groups[groupIndex].id,
            studentId = student.id,
            ownerId = ownerId
        )
    }
    
    return TestDataset(
        students = students,
        groups = groups,
        crossReferences = crossRefs
    )
}

/**
 * Data class representing a complete test dataset
 */
data class TestDataset(
    val students: List<Student>,
    val groups: List<StudentGroup>,
    val crossReferences: List<GroupStudentCrossRef>
)
