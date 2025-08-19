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
    private val userDao: gr.eduinvoice.data.dao.UserDao,
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
        require(lesson.durationMinutes > 0) { "Lesson duration must be positive" }

        return concurrencyController.executeSafeOperation(
            operation = {
                // Validate student exists and is active within transaction
                val student = studentDao.getStudentById(lesson.studentId, userId).first()
                checkNotNull(student) { "Cannot add lesson for a non-existent student" }
                check(student.isActive) { "Cannot add lesson for an inactive student" }

                lessonDao.insert(lesson)
            },
            operationType = OperationType.WRITE,
            resourceId = "lesson_student_${lesson.studentId}",
            priority = OperationPriority.NORMAL,
            useTransaction = true,
            isolationLevel = TransactionIsolationLevel.SERIALIZABLE
        ).getOrThrow()
    }

    /**
     * Adds group lessons with concurrency safety
     */
    suspend fun addGroupLesson(groupId: Long, lesson: Lesson, userId: Long): List<Long> {
        require(lesson.durationMinutes > 0) { "Lesson duration must be positive" }

        return concurrencyController.executeSafeOperation(
            operation = {
                val students = groupDao.getStudentsForGroup(groupId, userId).first()
                val masterId = lessonDao.insertGroupLessonMaster(
                    gr.eduinvoice.data.model.GroupLessonMaster(
                        ownerId = userId,
                        groupId = groupId,
                        date = lesson.date,
                        startTime = lesson.startTime,
                        durationMinutes = lesson.durationMinutes,
                        notes = lesson.notes
                    )
                )
                val lessons = students.map { student ->
                    lesson.copy(studentId = student.id, groupId = groupId, masterId = masterId)
                }
                lessonDao.insertGroupLessons(lessons)
            },
            operationType = OperationType.BATCH,
            resourceId = "group_$groupId",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).getOrThrow()
    }

    suspend fun addGroupLessonWithAbsences(
        groupId: Long,
        lesson: Lesson,
        absentStudentIds: List<Long>,
        userId: Long
    ): List<Long> {
        require(lesson.durationMinutes > 0) { "Lesson duration must be positive" }
        return concurrencyController.executeSafeOperation(
            operation = {
                val students = groupDao.getStudentsForGroup(groupId, userId).first()
                val masterId = lessonDao.insertGroupLessonMaster(
                    gr.eduinvoice.data.model.GroupLessonMaster(
                        ownerId = userId,
                        groupId = groupId,
                        date = lesson.date,
                        startTime = lesson.startTime,
                        durationMinutes = lesson.durationMinutes,
                        notes = lesson.notes
                    )
                )
                val present = students.filter { it.id !in absentStudentIds }
                val lessons = present.map { student ->
                    lesson.copy(studentId = student.id, groupId = groupId, masterId = masterId)
                }
                val absenceRows = absentStudentIds.map { sid ->
                    gr.eduinvoice.data.model.GroupLessonAbsence(
                        ownerId = userId,
                        groupLessonId = masterId,
                        studentId = sid
                    )
                }
                if (absenceRows.isNotEmpty()) lessonDao.insertAbsences(absenceRows)
                lessonDao.insertGroupLessons(lessons)
            },
            operationType = OperationType.BATCH,
            resourceId = "group_$groupId",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Edit an existing group lesson by its masterId, updating master fields, absences, and individual lessons.
     * - present->absent: delete individual lessons for those students
     * - absent->present: insert individual lessons for those students
     * - details changed: update individual lessons for present students
     */
    suspend fun editGroupLesson(
        masterId: Long,
        groupId: Long,
        originalDate: String,
        originalStartTime: String,
        originalDuration: Int,
        newDate: String,
        newStartTime: String,
        newDuration: Int,
        newNotes: String?,
        newAbsentStudentIds: List<Long>,
        userId: Long
    ) {
        concurrencyController.executeSafeOperation(
            operation = {
                val master = lessonDao.getGroupLessonMasterById(masterId, userId).first()
                checkNotNull(master) { "Group lesson master not found" }

                // Update master
                lessonDao.updateGroupLessonMaster(
                    master.copy(
                        date = newDate,
                        startTime = newStartTime,
                        durationMinutes = newDuration,
                        notes = newNotes
                    )
                )

                // Determine original absences
                val originalAbsences = lessonDao.getAbsencesByMaster(masterId, userId).first()
                val originalAbsentIds = originalAbsences.map { it.studentId }.toSet()

                val students = groupDao.getStudentsForGroup(groupId, userId).first()
                val allStudentIds = students.map { it.id }.toSet()
                val newAbsent = newAbsentStudentIds.toSet()
                val originalPresent = allStudentIds - originalAbsentIds
                val newPresent = allStudentIds - newAbsent

                val nowAbsent = originalPresent - newPresent // present->absent
                val nowPresent = newPresent - originalPresent // absent->present
                val stillPresent = originalPresent intersect newPresent

                // present->absent: delete individual lessons for those students at original time
                if (nowAbsent.isNotEmpty()) {
                    lessonDao.deleteByGroupAndTimeForStudents(
                        userId = userId,
                        groupId = groupId,
                        date = originalDate,
                        startTime = originalStartTime,
                        duration = originalDuration,
                        studentIds = nowAbsent.toList()
                    )
                }

                // absent->present: insert individual lessons at new time
                if (nowPresent.isNotEmpty()) {
                    val lessonsToInsert = students.filter { it.id in nowPresent }.map { student ->
                        Lesson(
                            ownerId = userId,
                            studentId = student.id,
                            groupId = groupId,
                            masterId = masterId,
                            date = newDate,
                            startTime = newStartTime,
                            durationMinutes = newDuration,
                            notes = newNotes ?: master.notes,
                            isPaid = false,
                            isInvoiced = false
                        )
                    }
                    lessonDao.insertGroupLessons(lessonsToInsert)
                }

                // details changed: update individual lessons for still-present students
                if (stillPresent.isNotEmpty() && (
                        originalDate != newDate || originalStartTime != newStartTime || originalDuration != newDuration || (newNotes ?: master.notes) != master.notes
                    )
                ) {
                    // Prefer masterId linkage if available, else fallback by group/time
                    val existing = lessonDao.getLessonsByGroupAndTime(userId, groupId, originalDate, originalStartTime, originalDuration)
                    val candidates = existing.filter { it.masterId == masterId }
                    val idsToUpdate = if (candidates.isNotEmpty()) candidates.filter { it.studentId in stillPresent }.map { it.id } else emptyList()
                    if (idsToUpdate.isNotEmpty()) {
                        idsToUpdate.forEach { lid ->
                            lessonDao.update(
                                Lesson(
                                    id = lid,
                                    ownerId = userId,
                                    studentId = existing.first { it.id == lid }.studentId,
                                    groupId = groupId,
                                    masterId = masterId,
                                    date = newDate,
                                    startTime = newStartTime,
                                    durationMinutes = newDuration,
                                    notes = newNotes ?: master.notes,
                                    isPaid = existing.first { it.id == lid }.isPaid,
                                    isInvoiced = existing.first { it.id == lid }.isInvoiced
                                )
                            )
                        }
                    } else {
                        lessonDao.updateByGroupAndTimeForStudents(
                            userId = userId,
                            groupId = groupId,
                            oldDate = originalDate,
                            oldStartTime = originalStartTime,
                            oldDuration = originalDuration,
                            newDate = newDate,
                            newStartTime = newStartTime,
                            newDuration = newDuration,
                            newNotes = newNotes,
                            studentIds = stillPresent.toList()
                        )
                    }
                }

                // Update absences table to reflect newAbsent
                lessonDao.deleteAbsencesForMaster(masterId, userId)
                if (newAbsent.isNotEmpty()) {
                    val rows = newAbsent.map { sid ->
                        gr.eduinvoice.data.model.GroupLessonAbsence(
                            ownerId = userId,
                            groupLessonId = masterId,
                            studentId = sid
                        )
                    }
                    lessonDao.insertAbsences(rows)
                }
            },
            operationType = OperationType.BATCH,
            resourceId = "group_${groupId}_lesson_${masterId}",
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
     * Deletes a lesson with concurrency safety
     */
    suspend fun deleteLesson(lessonId: Long, userId: Long) {
        concurrencyController.executeSafeOperation(
            operation = { lessonDao.deleteById(lessonId, userId) },
            operationType = OperationType.DELETE,
            resourceId = "lesson_$lessonId",
            priority = OperationPriority.HIGH,
            useTransaction = true
        ).getOrThrow()
    }

    /**
     * Gets lessons for a student with read-only safety
     */
    fun getLessonsForStudent(studentId: Long, userId: Long): Flow<List<Lesson>> {
        return lessonDao.getLessonsByStudentId(studentId, userId)
    }

    /**
     * Gets lessons with student data with read-only safety
     */
    fun getLessonsWithStudentData(studentId: Long, userId: Long): Flow<List<LessonWithStudent>> {
        return lessonDao.getLessonsWithStudentsByStudent(studentId, userId)
    }

    // ===== Batch Operations =====

    /**
     * Performs batch student operations with concurrency safety
     */
    suspend fun batchUpdateStudents(students: List<Student>): List<Result<Unit>> {
        val operations = students.map { student ->
            suspend { studentDao.update(student) }
        }

        return concurrencyController.executeBatchSafeOperations(
            operations = operations,
            operationType = OperationType.BATCH,
            resourceId = "batch_students",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).map { results ->
            results.map { Result.success(Unit) }
        }.getOrElse {
            students.map { Result.failure(Exception("Batch operation failed")) }
        }
    }

    /**
     * Performs batch lesson operations with concurrency safety
     */
    suspend fun batchUpdateLessons(lessons: List<Lesson>): List<Result<Unit>> {
        val operations = lessons.map { lesson ->
            suspend { lessonDao.update(lesson) }
        }

        return concurrencyController.executeBatchSafeOperations(
            operations = operations,
            operationType = OperationType.BATCH,
            resourceId = "batch_lessons",
            priority = OperationPriority.NORMAL,
            useTransaction = true
        ).map { results ->
            results.map { Result.success(Unit) }
        }.getOrElse {
            lessons.map { Result.failure(Exception("Batch operation failed")) }
        }
    }

    // ===== Health Check =====

    /**
     * Performs health check on concurrency components
     */
    suspend fun performHealthCheck() = concurrencyController.performHealthCheck()

    /**
     * Gets concurrency statistics
     */
    fun getConcurrencyStatistics() = concurrencyController.getConcurrencyStatistics()

    /**
     * Permanently deletes a user account and all of their data (students, lessons, groups, cross-refs).
     * This operation is executed transactionally.
     */
    suspend fun deleteAccount(userId: Long) {
        concurrencyController.executeSafeOperation(
            operation = {
                // Order: delete lessons -> cross-refs -> groups -> students -> user
                lessonDao.deleteAllByOwner(userId)
                groupDao.deleteAllCrossRefsByOwner(userId)
                groupDao.deleteAllGroupsByOwner(userId)
                studentDao.deleteAllByOwner(userId)
                userDao.deleteById(userId)
            },
            operationType = OperationType.DELETE,
            resourceId = "user_$userId",
            priority = OperationPriority.HIGH,
            useTransaction = true,
            isolationLevel = TransactionIsolationLevel.SERIALIZABLE
        ).getOrThrow()
    }
}
