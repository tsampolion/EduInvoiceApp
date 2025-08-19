package gr.eduinvoice.data.adapter

import gr.eduinvoice.domain.repository.DomainLessonRepository
import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.data.repository.EduInvoiceRepository
import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.model.Lesson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DomainLessonRepositoryAdapter @Inject constructor(
    private val eduInvoiceRepository: EduInvoiceRepository,
    private val lessonDao: LessonDao
) : DomainLessonRepository {

    override suspend fun addLesson(lesson: DomainLesson, userId: Long): Long =
        eduInvoiceRepository.addLesson(lesson.toDataModel(), userId)

    override suspend fun addGroupLesson(lesson: DomainLesson, userId: Long): Long {
        val lessonIds = eduInvoiceRepository.addGroupLesson(lesson.groupId ?: 0L, lesson.toDataModel(), userId)
        return lessonIds.firstOrNull() ?: 0L
    }

    override suspend fun addGroupLessonWithAbsences(
        lesson: DomainLesson,
        absentStudentIds: List<Long>,
        userId: Long
    ): List<Long> = eduInvoiceRepository.addGroupLessonWithAbsences(lesson.groupId ?: 0L, lesson.toDataModel(), absentStudentIds, userId)

    override suspend fun editGroupLesson(
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
    ) = eduInvoiceRepository.editGroupLesson(
        masterId,
        groupId,
        originalDate,
        originalStartTime,
        originalDuration,
        newDate,
        newStartTime,
        newDuration,
        newNotes,
        newAbsentStudentIds,
        userId
    )

    override suspend fun updateLesson(lesson: DomainLesson, userId: Long) =
        eduInvoiceRepository.updateLesson(lesson.toDataModel())

    override suspend fun deleteLesson(lessonId: Long, userId: Long) =
        eduInvoiceRepository.deleteLesson(lessonId, userId)

    override suspend fun updateLessonPaidStatus(lessonId: Long, isPaid: Boolean, userId: Long) =
        lessonDao.updatePaidStatus(listOf(lessonId), isPaid, userId)

    override suspend fun updateLessonInvoicedStatus(lessonId: Long, isInvoiced: Boolean, userId: Long) =
        lessonDao.updateInvoicedStatus(listOf(lessonId), isInvoiced, userId)

    override suspend fun isLessonInvoiced(lessonId: Long, userId: Long): Boolean =
        lessonDao.isLessonInvoiced(lessonId, userId).first() ?: false

    override fun getAllLessons(userId: Long): Flow<List<DomainLesson>> =
        lessonDao.getAllLessons(userId).map { lessons ->
            lessons.map { it.toDomainModel() }
        }

    override fun getLessonById(lessonId: Long, userId: Long): Flow<DomainLesson?> =
        lessonDao.getLessonById(lessonId, userId).map { it?.toDomainModel() }

    override fun getStudentLessons(studentId: Long, userId: Long): Flow<List<DomainLesson>> =
        eduInvoiceRepository.getLessonsForStudent(studentId, userId).map { lessons ->
            lessons.map { it.toDomainModel() }
        }

    override fun getLessonsWithStudents(userId: Long): Flow<List<DomainLesson>> =
        lessonDao.getLessonsWithStudents(userId).map { lessons ->
            lessons.map { it.lesson.toDomainModel() }
        }

    override fun getLessonsWithStudentsByStudentAndDateRange(
        studentId: Long,
        startDate: String,
        endDate: String,
        userId: Long
    ): Flow<List<DomainLesson>> =
        lessonDao.getLessonsWithStudentsByStudentAndDateRange(studentId, startDate, endDate, userId)
            .map { lessons ->
                lessons.map { it.lesson.toDomainModel() }
            }

    override suspend fun getLessonsWithStudentsPaginated(
        userId: Long,
        limit: Int,
        offset: Int
    ): List<DomainLesson> =
        lessonDao.getLessonsWithStudentsPaginated(userId, limit, offset)
            .map { it.lesson.toDomainModel() }

    override fun getGroupAbsences(userId: Long): Flow<List<gr.eduinvoice.domain.model.DomainAbsence>> =
        lessonDao.getAllAbsenceDetails(userId).map { rows ->
            rows.map { r -> gr.eduinvoice.domain.model.DomainAbsence(r.id, r.groupLessonId, r.groupId, r.studentId, r.studentName, r.studentSurname, r.date, r.startTime) }
        }

    override fun getAbsencesForStudent(studentId: Long, userId: Long): Flow<List<gr.eduinvoice.domain.model.DomainAbsence>> =
        lessonDao.getAbsenceDetailsForStudent(studentId, userId).map { rows ->
            rows.map { r -> gr.eduinvoice.domain.model.DomainAbsence(r.id, r.groupLessonId, r.groupId, r.studentId, r.studentName, r.studentSurname, r.date, r.startTime) }
        }

    override fun getGroupLessonMasters(groupId: Long, userId: Long): Flow<List<gr.eduinvoice.domain.model.DomainGroupLessonMaster>> =
        lessonDao.getGroupLessonMastersByGroup(groupId, userId).map { list ->
            list.map { m -> gr.eduinvoice.domain.model.DomainGroupLessonMaster(m.id, m.ownerId, m.groupId, m.date, m.startTime, m.durationMinutes, m.notes) }
        }

    override fun getGroupLessonMasterById(masterId: Long, userId: Long): Flow<gr.eduinvoice.domain.model.DomainGroupLessonMaster?> =
        lessonDao.getGroupLessonMasterById(masterId, userId).map { m ->
            m?.let { gr.eduinvoice.domain.model.DomainGroupLessonMaster(it.id, it.ownerId, it.groupId, it.date, it.startTime, it.durationMinutes, it.notes) }
        }

    override fun getAbsentStudentIdsForMaster(masterId: Long, userId: Long): Flow<List<Long>> =
        lessonDao.getAbsencesByMaster(masterId, userId).map { list -> list.map { it.studentId } }

    override suspend fun deleteGroupLesson(masterId: Long, userId: Long) =
        eduInvoiceRepository.deleteGroupLesson(masterId, userId)

    override suspend fun hasInvoicedOrPaidLessonsForMaster(masterId: Long, userId: Long): Boolean =
        lessonDao.countPaidOrInvoicedByMaster(masterId, userId) > 0

    override suspend fun createInvoiceMasterAndMarkLessons(
        studentId: Long,
        invoiceNumber: String,
        invoiceDate: String,
        notes: String?,
        lessonIds: List<Long>,
        userId: Long
    ): Long = eduInvoiceRepository.createInvoiceMasterAndMarkLessons(studentId, invoiceNumber, invoiceDate, notes, lessonIds, userId)

    override suspend fun archiveInvoiceMaster(id: Long, userId: Long) =
        eduInvoiceRepository.archiveInvoiceMaster(id, userId)

    override suspend fun deleteInvoiceMaster(id: Long, userId: Long) =
        eduInvoiceRepository.deleteInvoiceMaster(id, userId)

    override fun getInvoiceMastersByStudent(studentId: Long, userId: Long): kotlinx.coroutines.flow.Flow<List<gr.eduinvoice.domain.model.DomainInvoiceMaster>> =
        lessonDao.getInvoiceMastersByStudent(studentId, userId).map { list ->
            list.map { m -> gr.eduinvoice.domain.model.DomainInvoiceMaster(m.id, m.ownerId, m.studentId, m.invoiceNumber, m.invoiceDate, m.notes, m.isArchived, m.lastModified) }
        }

    override fun getInvoiceMasterById(id: Long, userId: Long): kotlinx.coroutines.flow.Flow<gr.eduinvoice.domain.model.DomainInvoiceMaster?> =
        lessonDao.getInvoiceMasterById(id, userId).map { m ->
            m?.let { gr.eduinvoice.domain.model.DomainInvoiceMaster(it.id, it.ownerId, it.studentId, it.invoiceNumber, it.invoiceDate, it.notes, it.isArchived, it.lastModified) }
        }

    override suspend fun updateInvoiceMaster(master: gr.eduinvoice.domain.model.DomainInvoiceMaster, userId: Long) =
        lessonDao.updateInvoiceMaster(
            gr.eduinvoice.data.model.InvoiceMaster(
                id = master.id,
                ownerId = userId,
                studentId = master.studentId,
                invoiceNumber = master.invoiceNumber,
                invoiceDate = master.invoiceDate,
                notes = master.notes,
                isArchived = master.isArchived,
                lastModified = master.lastModified
            )
        )

    private fun DomainLesson.toDataModel(): Lesson = Lesson(
        id = id,
        ownerId = ownerId,
        studentId = studentId,
        groupId = groupId,
        date = date,
        startTime = startTime,
        durationMinutes = durationMinutes,
        notes = notes,
        isPaid = isPaid,
        isInvoiced = isInvoiced,
        lastModified = lastModified
    )

    private fun Lesson.toDomainModel(): DomainLesson = DomainLesson(
        id = id,
        ownerId = ownerId,
        studentId = studentId,
        groupId = groupId,
        date = date,
        startTime = startTime,
        durationMinutes = durationMinutes,
        notes = notes,
        isPaid = isPaid,
        isInvoiced = isInvoiced,
        lastModified = lastModified
    )
}
