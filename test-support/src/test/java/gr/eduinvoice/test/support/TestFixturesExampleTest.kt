package gr.eduinvoice.test.support

import gr.eduinvoice.test.support.builders.*
import gr.eduinvoice.test.support.extensions.*
import gr.eduinvoice.test.support.utils.TestUtils
import gr.eduinvoice.test.support.fakes.NoopConcurrencyController
import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.test.runTest

/**
 * Example test demonstrating how to use the test fixtures
 */
class TestFixturesExampleTest {

    @Test
    fun `test using builders to create test data`() {
        // Using builders with fluent API
        val student = TestStudentBuilder()
            .withId(1L)
            .withName("Alice")
            .withRate(30.0)
            .withOwnerId(1L)
            .build()
        
        assertEquals("Alice", student.name)
        assertEquals(30.0, student.rate, 0.01)
        assertEquals(1L, student.ownerId)
        
        // Using companion object methods
        val lesson = TestLessonBuilder.createPaid(studentId = 1L, ownerId = 1L)
        assertTrue(lesson.isPaid)
        assertEquals(1L, lesson.studentId)
    }

    @Test
    fun `test using extension functions`() {
        // Using convenient extension functions
        val student = createTestStudent(
            id = 1L,
            name = "Bob",
            rate = 25.0
        )
        
        assertEquals("Bob", student.name)
        assertEquals(25.0, student.rate, 0.01)
        
        // Creating multiple test objects
        val students = createTestStudents(3, ownerId = 1L)
        assertEquals(3, students.size)
        assertEquals("Student 1", students[0].name)
        assertEquals("Student 2", students[1].name)
        assertEquals("Student 3", students[2].name)
    }

    @Test
    fun `test using fake repository`() = runTest {
        val fakeRepository = FakeLessonRepository()
        
        // Add test data
        val lesson = createTestLesson(studentId = 1L)
        val lessonId = fakeRepository.addLesson(lesson)
        
        assertEquals(1L, lessonId)
        
        // Query data
        val retrievedLesson = fakeRepository.getLessonById(1L, 1L).first()
        assertNotNull(retrievedLesson)
        assertEquals(1L, retrievedLesson!!.studentId)
    }

    @Test
    fun `test using noop concurrency controller`() = runTest {
        val controller = NoopConcurrencyController.create()
        
        // Test that operations complete immediately
        val result = controller.executeSafeOperation(
            operation = { "test result" },
            operationType = gr.eduinvoice.data.concurrency.OperationType.READ,
            resourceId = "test",
            priority = gr.eduinvoice.data.concurrency.OperationPriority.NORMAL
        )
        
        assertTrue(result.isSuccess)
        assertEquals("test result", result.getOrNull())
    }

    @Test
    fun `test using complete test environment`() = runTest {
        // Note: This test requires a Context, which would be provided by the actual test environment
        // For this example, we'll skip the database test since we don't have a Context
        val student = createTestStudent(name = "Test Student")
        assertEquals("Test Student", student.name)
    }

    @Test
    fun `test creating complete dataset`() {
        val dataset = createCompleteTestDataset(
            studentCount = 3,
            groupCount = 2,
            ownerId = 1L
        )
        
        assertEquals(3, dataset.students.size)
        assertEquals(2, dataset.groups.size)
        assertEquals(3, dataset.crossReferences.size)
        
        // Verify cross-references are correct
        val student1 = dataset.students[0]
        val group1 = dataset.groups[0]
        val crossRef = dataset.crossReferences.find { it.studentId == student1.id }
        
        assertNotNull(crossRef)
        assertEquals(group1.id, crossRef!!.groupId)
    }
}
