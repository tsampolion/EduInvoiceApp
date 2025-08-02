package gr.eduinvoice.data.repository

import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.dao.StudentDao
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.database.LessonWithStudent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TutorBillingRepository is the single source of truth for all data operations.
 *
 * This class sits between the UI layer (ViewModels) and the data layer (DAOs),
 * providing a clean API that hides the complexity of data operations.
 * It offers methods to add, update, delete, and query students and lessons.
 *
 * @Inject tells Hilt to automatically provide the DAOs when creating this repository.
 * @Singleton ensures a single instance is used throughout the app.
 */
@Singleton
class TutorBillingRepository @Inject constructor(
    private val studentDao: StudentDao,
    private val lessonDao: LessonDao,
    private val groupDao: gr.eduinvoice.data.dao.GroupDao
) {

    // ===== Student Operations =====

    /**
     * Adds a new student to the database.
     * Validates the student data before saving.
     *
     * @return The ID of the newly created student
     * @throws IllegalArgumentException if student data is invalid
     */
    suspend fun addStudent(student: Student): Long {
        require(student.name.isNotBlank()) { "First name cannot be empty" }
        return studentDao.insert(student)
    }

    /**
     * Updates an existing student's information.
     * Automatically updates the 'updatedAt' timestamp.
     */
    suspend fun updateStudent(student: Student) {
        studentDao.update(student)
    }

    /**
     * Soft deletes a student by setting 'isActive' to false.
     */
    suspend fun deleteStudent(studentId: Long, userId: Long) {
        studentDao.softDeleteStudent(studentId, userId)
    }

    /**
     * Gets a single student by ID.
     */
    suspend fun getStudent(studentId: Long, userId: Long): Student? {
        return studentDao.getStudentById(studentId, userId).first()
    }

    /**
     * Gets all active students as a Flow.
     */
    fun getAllActiveStudents(userId: Long): Flow<List<Student>> {
        return studentDao.getAllActiveStudents(userId)
    }

    // ===== Lesson Operations =====

    /**
     * Adds a new lesson to the database.
     * Validates that the student exists and is active.
     *
     * @return The ID of the newly created lesson
     * @throws IllegalArgumentException if data is invalid
     * @throws IllegalStateException if student doesn't exist or is inactive
     */
    suspend fun addLesson(lesson: Lesson, userId: Long): Long {
        require(lesson.durationMinutes > 0) { "Lesson duration must be positive" }
        val student = studentDao.getStudentById(lesson.studentId, userId).first()
        checkNotNull(student) { "Cannot add lesson for a non-existent student" }
        check(student.isActive) { "Cannot add lesson for an inactive student" }
        return lessonDao.insert(lesson)
    }

    suspend fun addGroupLesson(groupId: Long, lesson: Lesson, userId: Long): List<Long> {
        require(lesson.durationMinutes > 0) { "Lesson duration must be positive" }
        val students = groupDao.getStudentsForGroup(groupId, userId).first()
        val lessons = students.map { student ->
            lesson.copy(studentId = student.id, groupId = groupId)
        }
        return lessonDao.insertGroupLessons(lessons)
    }

    /**
     * Updates an existing lesson.
     * Automatically updates the 'updatedAt' timestamp.
     */
    suspend fun updateLesson(lesson: Lesson) {
        lessonDao.update(lesson)
    }

    /**
     * Deletes a lesson permanently by its ID.
     */
    suspend fun deleteLesson(lessonId: Long, userId: Long) {
        lessonDao.deleteById(lessonId, userId)
    }

    /**
     * Gets all lessons for a specific student as a Flow.
     */
    fun getLessonsForStudent(studentId: Long, userId: Long): Flow<List<Lesson>> {
        return lessonDao.getLessonsByStudentId(studentId, userId)
    }

    /**
     * Gets lessons with full student data for a specific student.
     */
    fun getLessonsWithStudentData(studentId: Long, userId: Long): Flow<List<LessonWithStudent>> {
        return lessonDao.getLessonsWithStudentsByStudent(studentId, userId)
    }

}
