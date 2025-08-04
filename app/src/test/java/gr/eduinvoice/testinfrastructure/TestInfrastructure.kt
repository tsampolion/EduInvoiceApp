package gr.eduinvoice.testinfrastructure

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import gr.eduinvoice.data.database.LessonWithStudent
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.RateTypes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Base class for all test infrastructure components
 */
abstract class TestInfrastructure {
    
    companion object {
        /**
         * Get the test context for Android operations
         */
        fun getTestContext(): Context = InstrumentationRegistry.getInstrumentation().targetContext
        
        /**
         * Create a temporary test directory
         */
        fun createTestDirectory(): File {
            val context = getTestContext()
            val testDir = File(context.cacheDir, "test_pdfs_${System.currentTimeMillis()}")
            if (!testDir.exists() && !testDir.mkdirs()) {
                throw IOException("Could not create test directory: ${testDir.absolutePath}")
            }
            return testDir
        }
        
        /**
         * Clean up test resources
         */
        fun cleanupTestResources(testDir: File) {
            try {
                if (testDir.exists()) {
                    testDir.deleteRecursively()
                }
            } catch (e: Exception) {
                // Log cleanup errors but don't fail tests
                println("Warning: Could not cleanup test directory: ${e.message}")
            }
        }
        
        /**
         * Create test lesson data
         */
        fun createTestLesson(
            id: Long = 1L,
            studentId: Long = 1L,
            ownerId: Long = 1L,
            date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            startTime: String = "10:00",
            durationMinutes: Int = 60,
            notes: String? = null,
            isPaid: Boolean = false
        ): Lesson = Lesson(
            id = id,
            studentId = studentId,
            ownerId = ownerId,
            date = date,
            startTime = startTime,
            durationMinutes = durationMinutes,
            notes = notes,
            isPaid = isPaid
        )
        
        /**
         * Create test student data
         */
        fun createTestStudent(
            id: Long = 1L,
            ownerId: Long = 1L,
            name: String = "Test Student",
            surname: String = "",
            parentMobile: String = "",
            className: String = "",
            rate: Double = 10.0,
            rateType: String = RateTypes.HOURLY
        ): Student = Student(
            id = id,
            ownerId = ownerId,
            name = name,
            surname = surname,
            parentMobile = parentMobile,
            className = className,
            rate = rate,
            rateType = rateType
        )
        
        /**
         * Create test lesson with student data
         */
        fun createTestLessonWithStudent(
            lesson: Lesson = createTestLesson(),
            student: Student = createTestStudent(id = lesson.studentId)
        ): LessonWithStudent = LessonWithStudent(lesson, student)
    }
}

/**
 * Enhanced test dispatcher rule for better coroutine handling
 */
class EnhancedTestDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    
    override fun starting(description: Description) {
        super.starting(description)
        // Additional setup if needed
    }
    
    override fun finished(description: Description) {
        super.finished(description)
        // Additional cleanup if needed
    }
    
    fun getDispatcher(): TestDispatcher = dispatcher
}

/**
 * Test data manager for coordinated test data management
 */
class TestDataManager {
    private val _studentFlow = MutableStateFlow<List<Student>>(emptyList())
    private val _lessonFlow = MutableStateFlow<List<Lesson>>(emptyList())
    private val _lessonWithStudentFlow = MutableStateFlow<List<LessonWithStudent>>(emptyList())
    
    val studentFlow: StateFlow<List<Student>> = _studentFlow
    val lessonFlow: StateFlow<List<Lesson>> = _lessonFlow
    val lessonWithStudentFlow: StateFlow<List<LessonWithStudent>> = _lessonWithStudentFlow
    
    /**
     * Setup test data with coordinated state management
     */
    fun setupTestData(
        students: List<Student> = emptyList(),
        lessons: List<Lesson> = emptyList(),
        lessonsWithStudents: List<LessonWithStudent> = emptyList()
    ) {
        _studentFlow.value = students
        _lessonFlow.value = lessons
        _lessonWithStudentFlow.value = lessonsWithStudents
    }
    
    /**
     * Add a student to the test data
     */
    fun addStudent(student: Student) {
        _studentFlow.value = _studentFlow.value + student
    }
    
    /**
     * Add a lesson to the test data
     */
    fun addLesson(lesson: Lesson) {
        _lessonFlow.value = _lessonFlow.value + lesson
    }
    
    /**
     * Add a lesson with student to the test data
     */
    fun addLessonWithStudent(lessonWithStudent: LessonWithStudent) {
        _lessonWithStudentFlow.value = _lessonWithStudentFlow.value + lessonWithStudent
    }
    
    /**
     * Clear all test data
     */
    fun clearTestData() {
        _studentFlow.value = emptyList()
        _lessonFlow.value = emptyList()
        _lessonWithStudentFlow.value = emptyList()
    }
    
    /**
     * Get student by ID
     */
    fun getStudentById(id: Long): Student? {
        return _studentFlow.value.find { it.id == id }
    }
    
    /**
     * Get lesson by ID
     */
    fun getLessonById(id: Long): Lesson? {
        return _lessonFlow.value.find { it.id == id }
    }
} 