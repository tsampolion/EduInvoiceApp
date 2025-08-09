package gr.eduinvoice.domain.testfixtures

import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.database.LessonWithStudent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of lesson-related repositories for testing
 * Uses in-memory storage with MutableStateFlow for reactive updates
 */
class FakeLessonRepository {
    private val lessons = MutableStateFlow<List<Lesson>>(emptyList())
    private val students = MutableStateFlow<List<Student>>(emptyList())
    private var nextId = 1L

    suspend fun insert(lesson: Lesson): Long {
        val newLesson = lesson.copy(id = nextId++)
        lessons.value = lessons.value + newLesson
        return newLesson.id
    }

    suspend fun insertGroupLessons(lessons: List<Lesson>): List<Long> {
        val ids = mutableListOf<Long>()
        lessons.forEach { lesson ->
            val newLesson = lesson.copy(id = nextId++)
            this.lessons.value = this.lessons.value + newLesson
            ids.add(newLesson.id)
        }
        return ids
    }

    suspend fun update(lesson: Lesson) {
        lessons.value = lessons.value.map { 
            if (it.id == lesson.id) lesson else it 
        }
    }

    suspend fun delete(lesson: Lesson) {
        lessons.value = lessons.value.filter { it.id != lesson.id }
    }

    suspend fun deleteById(lessonId: Long, userId: Long) {
        lessons.value = lessons.value.filter { 
            !(it.id == lessonId && it.ownerId == userId) 
        }
    }

    fun getLessonById(lessonId: Long, userId: Long): Flow<Lesson?> =
        lessons.map { lessonList ->
            lessonList.find { it.id == lessonId && it.ownerId == userId }
        }

    fun getLessonsByStudentId(studentId: Long, userId: Long): Flow<List<Lesson>> =
        lessons.map { lessonList ->
            lessonList.filter { it.studentId == studentId && it.ownerId == userId }
                .sortedByDescending { it.date }
                .sortedByDescending { it.startTime }
        }

    fun getAllLessons(userId: Long): Flow<List<Lesson>> =
        lessons.map { lessonList ->
            lessonList.filter { it.ownerId == userId }
                .sortedByDescending { it.date }
                .sortedByDescending { it.startTime }
        }

    fun getLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> =
        lessons.map { lessonList ->
            lessonList.filter { 
                it.ownerId == userId && 
                it.date >= startDate && 
                it.date <= endDate 
            }.sortedByDescending { it.date }
                .sortedByDescending { it.startTime }
        }

    fun getLessonsByStudentAndDateRange(
        studentId: Long, 
        startDate: String, 
        endDate: String, 
        userId: Long
    ): Flow<List<Lesson>> =
        lessons.map { lessonList ->
            lessonList.filter { 
                it.studentId == studentId && 
                it.ownerId == userId && 
                it.date >= startDate && 
                it.date <= endDate 
            }.sortedBy { it.date }
        }

    fun getUnpaidLessonsByStudentAndDateRange(
        studentId: Long, 
        startDate: String, 
        endDate: String, 
        userId: Long
    ): Flow<List<Lesson>> =
        lessons.map { lessonList ->
            lessonList.filter { 
                it.studentId == studentId && 
                it.ownerId == userId && 
                it.date >= startDate && 
                it.date <= endDate && 
                !it.isPaid 
            }.sortedBy { it.date }
        }

    fun getUnpaidLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> =
        lessons.map { lessonList ->
            lessonList.filter { 
                it.ownerId == userId && 
                it.date >= startDate && 
                it.date <= endDate && 
                !it.isPaid 
            }.sortedByDescending { it.date }
                .sortedByDescending { it.startTime }
        }

    fun getLessonsWithStudents(userId: Long): Flow<List<LessonWithStudent>> =
        lessons.map { lessonList ->
            lessonList.filter { it.ownerId == userId }
                .mapNotNull { lesson ->
                    val student = students.value.find { it.id == lesson.studentId }
                    if (student != null) {
                        LessonWithStudent(lesson, student)
                    } else null
                }
                .sortedByDescending { it.lesson.date }
                .sortedByDescending { it.lesson.startTime }
        }

    fun getLessonsWithStudentsByStudentAndDateRange(
        studentId: Long, 
        startDate: String, 
        endDate: String, 
        userId: Long
    ): Flow<List<LessonWithStudent>> =
        lessons.map { lessonList ->
            lessonList.filter { 
                it.studentId == studentId && 
                it.ownerId == userId && 
                it.date >= startDate && 
                it.date <= endDate 
            }.mapNotNull { lesson ->
                val student = students.value.find { it.id == lesson.studentId }
                if (student != null) {
                    LessonWithStudent(lesson, student)
                } else null
            }.sortedBy { it.lesson.date }
        }

    fun getLessonsWithStudentsPaginated(
        userId: Long, 
        limit: Int, 
        offset: Int
    ): List<LessonWithStudent> {
        val lessonWithStudents = lessons.value
            .filter { it.ownerId == userId }
            .mapNotNull { lesson ->
                val student = students.value.find { it.id == lesson.studentId }
                if (student != null) {
                    LessonWithStudent(lesson, student)
                } else null
            }
            .sortedByDescending { it.lesson.date }
            .sortedByDescending { it.lesson.startTime }
        return lessonWithStudents.drop(offset).take(limit)
    }

    suspend fun updateLessonPaidStatus(lessonId: Long, isPaid: Boolean, userId: Long) {
        lessons.value = lessons.value.map { 
            if (it.id == lessonId && it.ownerId == userId) {
                it.copy(isPaid = isPaid)
            } else it 
        }
    }

    suspend fun updateLessonInvoicedStatus(lessonId: Long, isInvoiced: Boolean, userId: Long) {
        lessons.value = lessons.value.map { 
            if (it.id == lessonId && it.ownerId == userId) {
                it.copy(isInvoiced = isInvoiced)
            } else it 
        }
    }

    suspend fun isLessonInvoiced(lessonId: Long, userId: Long): Boolean {
        return lessons.value.any { 
            it.id == lessonId && it.ownerId == userId && it.isInvoiced 
        }
    }

    /**
     * Test helper methods
     */
    fun addLesson(lesson: Lesson) {
        lessons.value = lessons.value + lesson
    }

    fun addStudent(student: Student) {
        students.value = students.value + student
    }

    fun clear() {
        lessons.value = emptyList()
        students.value = emptyList()
        nextId = 1L
    }

    fun getAllLessons(): List<Lesson> = lessons.value
    fun getAllStudents(): List<Student> = students.value
}
