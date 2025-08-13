package gr.eduinvoice.data.repository

import gr.eduinvoice.data.concurrency.ConcurrencyController
import gr.eduinvoice.data.concurrency.OperationType
import gr.eduinvoice.data.concurrency.OperationPriority
import gr.eduinvoice.data.concurrency.TransactionIsolationLevel
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
 * Enhanced EduInvoiceRepository with concurrency safety
 *
 * This repository now uses the ConcurrencyController to ensure:
 * - Thread-safe database operations
 * - Proper transaction management
 * - Conflict resolution for concurrent operations
 * - Automatic rollback on failures
 * - Operation queuing for conflicting operations
 */
@Singleton
class EduInvoiceRepository @Inject constructor(
    private val studentDao: StudentDao,
    private val lessonDao: LessonDao,
    private val groupDao: gr.eduinvoice.data.dao.GroupDao,
    private val concurrencyController: ConcurrencyController
) {

    // ===== Student Operations =====

    /**
     * Adds a new student to the database with concurrency safety
     */
    suspend fun addStudent(student: Student): Long {
        require(student.name.isNotBlank()) { "First name cannot be empty" }

        return concurrencyController.executeSafeOperation(
            operation = { studentDao.insert(student) },
            operationType = OperationType.WRITE,
            resourceId = "student_${student.ownerId}",
            priority = OperationPriority.NORMAL,
            useTransaction = true,
            isolationLevel = TransactionIsolationLevel.SERIALIZABLE
        ).getOrThrow()
    }

    /**
     * Updates an existing student's information with concurrency safety
     */
    suspend fun updateStudent(student: Student) {
        concurrencyController.executeSafeOperation(
            operation = { studentDao.update(student) },
            operationType = OperationType.UPDATE,
            resourceId = "student_${student.id}",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Soft deletes a student with concurrency safety
     */
    suspend fun deleteStudent(studentId: Long, userId: Long) {
        concurrencyController.executeSafeOperation(
            operation = { studentDao.softDeleteStudent(studentId, userId) },
            operationType = OperationType.DELETE,
            resourceId = "student_$studentId",
            priority = OperationPriority.HIGH,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Gets a single student by ID with read-only safety
     */
    suspend fun getStudent(studentId: Long, userId: Long): Student? {
        return concurrencyController.executeReadOnlyOperation(
            operation = { studentDao.getStudentById(studentId, userId).first() },
            resourceId = "student_$studentId"
        ).getOrNull()
    }

    /**
     * Gets all active students as a Flow (read-only)
     */
    fun getAllActiveStudents(userId: Long): Flow<List<Student>> {
        return studentDao.getAllActiveStudents(userId)
    }

    // ===== Lesson Operations =====

    /**
     * Adds a new lesson with concurrency safety and validation
     */
    suspend fun addLesson(lesson: Lesson, userId: Long): Long {
        require(lesson.studentId > 0) { "Student ID must be valid" }
        require(lesson.ownerId == userId) { "Lesson owner must match current user" }

        return concurrencyController.executeSafeOperation(
            operation = { lessonDao.insert(lesson) },
            operationType = OperationType.WRITE,
            resourceId = "lesson_${lesson.studentId}",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Adds a group lesson with concurrency safety
     */
    suspend fun addGroupLesson(groupId: Long, lesson: Lesson, userId: Long): List<Long> {
        require(groupId > 0) { "Group ID must be valid" }
        require(lesson.ownerId == userId) { "Lesson owner must match current user" }

        return concurrencyController.executeSafeOperation(
            operation = { lessonDao.insertGroupLesson(groupId, lesson) },
            operationType = OperationType.WRITE,
            resourceId = "group_lesson_$groupId",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Updates an existing lesson with concurrency safety
     */
    suspend fun updateLesson(lesson: Lesson) {
        concurrencyController.executeSafeOperation(
            operation = { lessonDao.update(lesson) },
            operationType = OperationType.UPDATE,
            resourceId = "lesson_${lesson.id}",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Soft deletes a lesson with concurrency safety
     */
    suspend fun deleteLesson(lessonId: Long, userId: Long) {
        concurrencyController.executeSafeOperation(
            operation = { lessonDao.softDeleteLesson(lessonId, userId) },
            operationType = OperationType.DELETE,
            resourceId = "lesson_$lessonId",
            priority = OperationPriority.HIGH,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Gets lessons for a specific student with read-only safety
     */
    fun getLessonsForStudent(studentId: Long, userId: Long): Flow<List<LessonWithStudent>> {
        return lessonDao.getLessonsForStudent(studentId, userId)
    }

    /**
     * Gets all active lessons as a Flow (read-only)
     */
    fun getAllActiveLessons(userId: Long): Flow<List<LessonWithStudent>> {
        return lessonDao.getAllActiveLessons(userId)
    }

    /**
     * Gets a single lesson by ID with read-only safety
     */
    suspend fun getLesson(lessonId: Long, userId: Long): Lesson? {
        return concurrencyController.executeReadOnlyOperation(
            operation = { lessonDao.getLessonById(lessonId, userId).first() },
            resourceId = "lesson_$lessonId"
        ).getOrNull()
    }

    // ===== Group Operations =====

    /**
     * Adds a new group with concurrency safety
     */
    suspend fun addGroup(group: gr.eduinvoice.data.model.Group): Long {
        require(group.name.isNotBlank()) { "Group name cannot be empty" }
        require(group.ownerId > 0) { "Group owner must be valid" }

        return concurrencyController.executeSafeOperation(
            operation = { groupDao.insert(group) },
            operationType = OperationType.WRITE,
            resourceId = "group_${group.ownerId}",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Updates an existing group with concurrency safety
     */
    suspend fun updateGroup(group: gr.eduinvoice.data.model.Group) {
        concurrencyController.executeSafeOperation(
            operation = { groupDao.update(group) },
            operationType = OperationType.UPDATE,
            resourceId = "group_${group.id}",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Soft deletes a group with concurrency safety
     */
    suspend fun deleteGroup(groupId: Long, userId: Long) {
        concurrencyController.executeSafeOperation(
            operation = { groupDao.softDeleteGroup(groupId, userId) },
            operationType = OperationType.DELETE,
            resourceId = "group_$groupId",
            priority = OperationPriority.HIGH,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Gets all active groups as a Flow (read-only)
     */
    fun getAllActiveGroups(userId: Long): Flow<List<gr.eduinvoice.data.model.Group>> {
        return groupDao.getAllActiveGroups(userId)
    }

    /**
     * Gets a single group by ID with read-only safety
     */
    suspend fun getGroup(groupId: Long, userId: Long): gr.eduinvoice.data.model.Group? {
        return concurrencyController.executeReadOnlyOperation(
            operation = { groupDao.getGroupById(groupId, userId).first() },
            resourceId = "group_$groupId"
        ).getOrNull()
    }

    /**
     * Adds a student to a group with concurrency safety
     */
    suspend fun addStudentToGroup(studentId: Long, groupId: Long, userId: Long) {
        require(studentId > 0) { "Student ID must be valid" }
        require(groupId > 0) { "Group ID must be valid" }

        concurrencyController.executeSafeOperation(
            operation = { groupDao.addStudentToGroup(studentId, groupId, userId) },
            operationType = OperationType.WRITE,
            resourceId = "group_student_${groupId}_${studentId}",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Removes a student from a group with concurrency safety
     */
    suspend fun removeStudentFromGroup(studentId: Long, groupId: Long, userId: Long) {
        require(studentId > 0) { "Student ID must be valid" }
        require(groupId > 0) { "Group ID must be valid" }

        concurrencyController.executeSafeOperation(
            operation = { groupDao.removeStudentFromGroup(studentId, groupId, userId) },
            operationType = OperationType.DELETE,
            resourceId = "group_student_${groupId}_${studentId}",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Gets all students in a group with read-only safety
     */
    fun getStudentsInGroup(groupId: Long, userId: Long): Flow<List<Student>> {
        return groupDao.getStudentsInGroup(groupId, userId)
    }

    /**
     * Gets all groups for a student with read-only safety
     */
    fun getGroupsForStudent(studentId: Long, userId: Long): Flow<List<gr.eduinvoice.data.model.Group>> {
        return groupDao.getGroupsForStudent(studentId, userId)
    }
}