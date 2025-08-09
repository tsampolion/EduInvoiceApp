package gr.eduinvoice.test.support.fakes

import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.database.LessonWithStudent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

/**
 * Fake implementation of lesson repository for testing
 * Provides in-memory storage with Flow-based reactive updates
 */
class FakeLessonRepository {
    private val lessonsFlow = MutableStateFlow<List<Lesson>>(emptyList())
    private val lessonsWithStudentsFlow = MutableStateFlow<List<LessonWithStudent>>(emptyList())

    val lessons: StateFlow<List<Lesson>> = lessonsFlow.asStateFlow()
    val lessonsWithStudents: StateFlow<List<LessonWithStudent>> = lessonsWithStudentsFlow.asStateFlow()

    /**
     * Adds a lesson to the repository
     */
    suspend fun addLesson(lesson: Lesson): Long {
        val newId = (lessonsFlow.value.maxOfOrNull { it.id } ?: 0L) + 1
        val lessonWithId = lesson.copy(id = newId)
        lessonsFlow.value = lessonsFlow.value + lessonWithId
        return newId
    }

    /**
     * Updates an existing lesson
     */
    suspend fun updateLesson(lesson: Lesson) {
        lessonsFlow.value = lessonsFlow.value.map { 
            if (it.id == lesson.id) lesson else it 
        }
    }

    /**
     * Deletes a lesson by ID
     */
    suspend fun deleteLesson(lessonId: Long, userId: Long) {
        lessonsFlow.value = lessonsFlow.value.filterNot { 
            it.id == lessonId && it.ownerId == userId 
        }
    }

    /**
     * Gets a lesson by ID
     */
    fun getLessonById(lessonId: Long, userId: Long): Flow<Lesson?> =
        lessonsFlow.map { lessons ->
            lessons.find { it.id == lessonId && it.ownerId == userId }
        }

    /**
     * Gets all lessons for a student
     */
    fun getLessonsByStudentId(studentId: Long, userId: Long): Flow<List<Lesson>> =
        lessonsFlow.map { lessons ->
            lessons.filter { it.studentId == studentId && it.ownerId == userId }
        }

    /**
     * Gets all lessons for a user
     */
    fun getAllLessons(userId: Long): Flow<List<Lesson>> =
        lessonsFlow.map { lessons ->
            lessons.filter { it.ownerId == userId }
        }

    /**
     * Gets lessons in a date range
     */
    fun getLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> =
        lessonsFlow.map { lessons ->
            lessons.filter { lesson ->
                lesson.ownerId == userId &&
                lesson.date >= startDate &&
                lesson.date <= endDate
            }
        }

    /**
     * Gets lessons by student and date range
     */
    fun getLessonsByStudentAndDateRange(
        studentId: Long, 
        startDate: String, 
        endDate: String, 
        userId: Long
    ): Flow<List<Lesson>> =
        lessonsFlow.map { lessons ->
            lessons.filter { lesson ->
                lesson.studentId == studentId &&
                lesson.ownerId == userId &&
                lesson.date >= startDate &&
                lesson.date <= endDate
            }
        }

    /**
     * Gets unpaid lessons by student and date range
     */
    fun getUnpaidLessonsByStudentAndDateRange(
        studentId: Long, 
        startDate: String, 
        endDate: String, 
        userId: Long
    ): Flow<List<Lesson>> =
        lessonsFlow.map { lessons ->
            lessons.filter { lesson ->
                lesson.studentId == studentId &&
                lesson.ownerId == userId &&
                !lesson.isPaid &&
                lesson.date >= startDate &&
                lesson.date <= endDate
            }
        }

    /**
     * Gets unpaid lessons in date range
     */
    fun getUnpaidLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>> =
        lessonsFlow.map { lessons ->
            lessons.filter { lesson ->
                lesson.ownerId == userId &&
                !lesson.isPaid &&
                lesson.date >= startDate &&
                lesson.date <= endDate
            }
        }

    /**
     * Updates paid status for multiple lessons
     */
    suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean, userId: Long) {
        lessonsFlow.value = lessonsFlow.value.map { lesson ->
            if (ids.contains(lesson.id) && lesson.ownerId == userId) {
                lesson.copy(isPaid = paid)
            } else {
                lesson
            }
        }
    }

    /**
     * Updates invoiced status for multiple lessons
     */
    suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean, userId: Long) {
        lessonsFlow.value = lessonsFlow.value.map { lesson ->
            if (ids.contains(lesson.id) && lesson.ownerId == userId) {
                lesson.copy(isInvoiced = invoiced)
            } else {
                lesson
            }
        }
    }

    /**
     * Checks if a lesson is invoiced
     */
    fun isLessonInvoiced(lessonId: Long, userId: Long): Flow<Boolean?> =
        lessonsFlow.map { lessons ->
            lessons.find { it.id == lessonId && it.ownerId == userId }?.isInvoiced
        }

    /**
     * Gets lessons with student information
     */
    fun getLessonsWithStudents(userId: Long): Flow<List<LessonWithStudent>> =
        lessonsWithStudentsFlow.map { lessonsWithStudents ->
            lessonsWithStudents.filter { it.lesson.ownerId == userId }
        }

    /**
     * Gets lessons with students by student ID
     */
    fun getLessonsWithStudentsByStudent(studentId: Long, userId: Long): Flow<List<LessonWithStudent>> =
        lessonsWithStudentsFlow.map { lessonsWithStudents ->
            lessonsWithStudents.filter { 
                it.lesson.studentId == studentId && it.lesson.ownerId == userId 
            }
        }

    /**
     * Gets lessons with students in date range
     */
    fun getLessonsWithStudentsInDateRange(
        startDate: String, 
        endDate: String, 
        userId: Long
    ): Flow<List<LessonWithStudent>> =
        lessonsWithStudentsFlow.map { lessonsWithStudents ->
            lessonsWithStudents.filter { lessonWithStudent ->
                lessonWithStudent.lesson.ownerId == userId &&
                lessonWithStudent.lesson.date >= startDate &&
                lessonWithStudent.lesson.date <= endDate
            }
        }

    /**
     * Gets lessons with students by student and date range
     */
    fun getLessonsWithStudentsByStudentAndDateRange(
        studentId: Long,
        startDate: String,
        endDate: String,
        userId: Long
    ): Flow<List<LessonWithStudent>> =
        lessonsWithStudentsFlow.map { lessonsWithStudents ->
            lessonsWithStudents.filter { lessonWithStudent ->
                lessonWithStudent.lesson.studentId == studentId &&
                lessonWithStudent.lesson.ownerId == userId &&
                lessonWithStudent.lesson.date >= startDate &&
                lessonWithStudent.lesson.date <= endDate
            }
        }

    /**
     * Gets lessons with students paginated
     */
    suspend fun getLessonsWithStudentsPaginated(
        userId: Long, 
        limit: Int, 
        offset: Int
    ): List<LessonWithStudent> =
        lessonsWithStudentsFlow.value
            .filter { it.lesson.ownerId == userId }
            .drop(offset)
            .take(limit)

    /**
     * Clears all test data
     */
    fun clear() {
        lessonsFlow.value = emptyList()
        lessonsWithStudentsFlow.value = emptyList()
    }

    /**
     * Sets test data
     */
    fun setTestData(lessons: List<Lesson>, lessonsWithStudents: List<LessonWithStudent> = emptyList()) {
        lessonsFlow.value = lessons
        lessonsWithStudentsFlow.value = lessonsWithStudents
    }
}
