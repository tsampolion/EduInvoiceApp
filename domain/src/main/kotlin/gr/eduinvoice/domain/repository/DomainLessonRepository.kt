package gr.eduinvoice.domain.repository

import gr.eduinvoice.domain.model.DomainLesson
import kotlinx.coroutines.flow.Flow
import gr.eduinvoice.domain.model.DomainAbsence

interface DomainLessonRepository {
    suspend fun addLesson(lesson: DomainLesson, userId: Long = 0): Long
    suspend fun addGroupLesson(lesson: DomainLesson, userId: Long = 0): Long
    suspend fun addGroupLessonWithAbsences(lesson: DomainLesson, absentStudentIds: List<Long>, userId: Long = 0): List<Long>
    suspend fun updateLesson(lesson: DomainLesson, userId: Long = 0)
    suspend fun deleteLesson(lessonId: Long, userId: Long = 0)
    suspend fun updateLessonPaidStatus(lessonId: Long, isPaid: Boolean, userId: Long = 0)
    suspend fun updateLessonInvoicedStatus(lessonId: Long, isInvoiced: Boolean, userId: Long = 0)
    suspend fun isLessonInvoiced(lessonId: Long, userId: Long = 0): Boolean
    fun getAllLessons(userId: Long = 0): Flow<List<DomainLesson>>
    fun getLessonById(lessonId: Long, userId: Long = 0): Flow<DomainLesson?>
    fun getStudentLessons(studentId: Long, userId: Long = 0): Flow<List<DomainLesson>>
    fun getLessonsWithStudents(userId: Long = 0): Flow<List<DomainLesson>>
    fun getLessonsWithStudentsByStudentAndDateRange(
        studentId: Long,
        startDate: String,
        endDate: String,
        userId: Long = 0
    ): Flow<List<DomainLesson>>
    suspend fun getLessonsWithStudentsPaginated(
        userId: Long = 0,
        limit: Int,
        offset: Int
    ): List<DomainLesson>
    fun getGroupAbsences(userId: Long = 0): Flow<List<DomainAbsence>>
    fun getAbsencesForStudent(studentId: Long, userId: Long = 0): Flow<List<DomainAbsence>>
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
        userId: Long = 0
    )
    fun getGroupLessonMasters(groupId: Long, userId: Long = 0): Flow<List<gr.eduinvoice.domain.model.DomainGroupLessonMaster>>
    fun getGroupLessonMasterById(masterId: Long, userId: Long = 0): Flow<gr.eduinvoice.domain.model.DomainGroupLessonMaster?>
    fun getAbsentStudentIdsForMaster(masterId: Long, userId: Long = 0): Flow<List<Long>>
    suspend fun deleteGroupLesson(masterId: Long, userId: Long = 0)
    suspend fun hasInvoicedOrPaidLessonsForMaster(masterId: Long, userId: Long = 0): Boolean

    // Invoice master operations
    suspend fun createInvoiceMasterAndMarkLessons(
        studentId: Long,
        invoiceNumber: String,
        invoiceDate: String,
        notes: String?,
        lessonIds: List<Long>,
        userId: Long
    ): Long

    suspend fun archiveInvoiceMaster(id: Long, userId: Long)
    suspend fun deleteInvoiceMaster(id: Long, userId: Long)

    fun getInvoiceMastersByStudent(studentId: Long, userId: Long): kotlinx.coroutines.flow.Flow<List<gr.eduinvoice.domain.model.DomainInvoiceMaster>>
    fun getInvoiceMasterById(id: Long, userId: Long): kotlinx.coroutines.flow.Flow<gr.eduinvoice.domain.model.DomainInvoiceMaster?>
    suspend fun updateInvoiceMaster(master: gr.eduinvoice.domain.model.DomainInvoiceMaster, userId: Long)
}
