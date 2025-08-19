package gr.eduinvoice.data.dao

import androidx.room.*
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.database.LessonWithStudent
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Insert
    suspend fun insert(lesson: Lesson): Long

    @Transaction
    suspend fun insertGroupLessons(lessons: List<Lesson>): List<Long> {
        val ids = mutableListOf<Long>()
        lessons.forEach { lesson -> ids += insert(lesson) }
        return ids
    }

    // Group lesson master + absences
    @Insert
    suspend fun insertGroupLessonMaster(master: gr.eduinvoice.data.model.GroupLessonMaster): Long

    // Invoice master
    @Insert
    suspend fun insertInvoiceMaster(master: gr.eduinvoice.data.model.InvoiceMaster): Long

    @Update
    suspend fun updateInvoiceMaster(master: gr.eduinvoice.data.model.InvoiceMaster)

    @Query("SELECT * FROM invoice_master WHERE id = :id AND ownerId = :userId")
    fun getInvoiceMasterById(id: Long, userId: Long): Flow<gr.eduinvoice.data.model.InvoiceMaster?>

    @Query("SELECT * FROM invoice_master WHERE studentId = :studentId AND ownerId = :userId ORDER BY invoiceDate DESC, id DESC")
    fun getInvoiceMastersByStudent(studentId: Long, userId: Long): Flow<List<gr.eduinvoice.data.model.InvoiceMaster>>

    @Query("UPDATE invoice_master SET isArchived = 1 WHERE id = :id AND ownerId = :userId")
    suspend fun archiveInvoiceMaster(id: Long, userId: Long)

    @Query("DELETE FROM invoice_master WHERE id = :id AND ownerId = :userId")
    suspend fun deleteInvoiceMaster(id: Long, userId: Long)

    // Payment batch master
    @Insert
    suspend fun insertPaymentBatchMaster(master: gr.eduinvoice.data.model.PaymentBatchMaster): Long

    @Update
    suspend fun updatePaymentBatchMaster(master: gr.eduinvoice.data.model.PaymentBatchMaster)

    @Query("SELECT * FROM payment_batch_master WHERE id = :id AND ownerId = :userId")
    fun getPaymentBatchMasterById(id: Long, userId: Long): Flow<gr.eduinvoice.data.model.PaymentBatchMaster?>

    @Query("SELECT * FROM payment_batch_master WHERE (studentId = :studentId OR :studentId IS NULL) AND ownerId = :userId ORDER BY batchDate DESC, id DESC")
    fun getPaymentBatchMasters(studentId: Long?, userId: Long): Flow<List<gr.eduinvoice.data.model.PaymentBatchMaster>>

    @Query("UPDATE payment_batch_master SET isArchived = 1 WHERE id = :id AND ownerId = :userId")
    suspend fun archivePaymentBatchMaster(id: Long, userId: Long)

    @Query("DELETE FROM payment_batch_master WHERE id = :id AND ownerId = :userId")
    suspend fun deletePaymentBatchMaster(id: Long, userId: Long)

    // Reschedule master
    @Insert
    suspend fun insertRescheduleMaster(master: gr.eduinvoice.data.model.RescheduleMaster): Long

    @Query("SELECT * FROM reschedule_master WHERE ownerId = :userId ORDER BY newDate DESC, newStartTime DESC")
    fun getRescheduleMasters(userId: Long): Flow<List<gr.eduinvoice.data.model.RescheduleMaster>>

    @Query("INSERT OR REPLACE INTO reschedule_master_lessons(masterId, lessonId) VALUES(:masterId, :lessonId)")
    suspend fun attachLessonToReschedule(masterId: Long, lessonId: Long)

    @Query("SELECT lessonId FROM reschedule_master_lessons WHERE masterId = :masterId")
    suspend fun getRescheduledLessonIds(masterId: Long): List<Long>

    @Query("DELETE FROM reschedule_master_lessons WHERE masterId = :masterId")
    suspend fun clearRescheduleLinks(masterId: Long)

    @Update
    suspend fun updateGroupLessonMaster(master: gr.eduinvoice.data.model.GroupLessonMaster)

    @Query("SELECT * FROM group_lesson_master WHERE id = :id AND ownerId = :userId")
    fun getGroupLessonMasterById(id: Long, userId: Long): Flow<gr.eduinvoice.data.model.GroupLessonMaster?>

    @Query("SELECT * FROM group_lesson_master WHERE groupId = :groupId AND ownerId = :userId ORDER BY date DESC, startTime DESC")
    fun getGroupLessonMastersByGroup(groupId: Long, userId: Long): Flow<List<gr.eduinvoice.data.model.GroupLessonMaster>>

    @Insert
    suspend fun insertAbsences(absences: List<gr.eduinvoice.data.model.GroupLessonAbsence>): List<Long>

    @Query("SELECT * FROM group_lesson_absences WHERE groupLessonId = :masterId AND ownerId = :userId")
    fun getAbsencesByMaster(masterId: Long, userId: Long): Flow<List<gr.eduinvoice.data.model.GroupLessonAbsence>>

    @Query("DELETE FROM group_lesson_absences WHERE groupLessonId = :masterId AND ownerId = :userId")
    suspend fun deleteAbsencesForMaster(masterId: Long, userId: Long)

    @Query("SELECT * FROM group_lesson_absences WHERE ownerId = :userId")
    fun getAllAbsences(userId: Long): Flow<List<gr.eduinvoice.data.model.GroupLessonAbsence>>

    @Query("SELECT a.* FROM group_lesson_absences a INNER JOIN group_lesson_master m ON a.groupLessonId = m.id WHERE a.studentId = :studentId AND a.ownerId = :userId ORDER BY m.date DESC, m.startTime DESC")
    fun getAbsencesForStudent(studentId: Long, userId: Long): Flow<List<gr.eduinvoice.data.model.GroupLessonAbsence>>

    @Query(
        "SELECT a.id AS id, a.groupLessonId AS groupLessonId, m.groupId AS groupId, a.studentId AS studentId, s.name AS studentName, s.surname AS studentSurname, m.date AS date, m.startTime AS startTime " +
            "FROM group_lesson_absences a INNER JOIN group_lesson_master m ON a.groupLessonId = m.id " +
            "LEFT JOIN students s ON s.id = a.studentId AND s.ownerId = a.ownerId " +
            "WHERE a.ownerId = :userId ORDER BY m.date DESC, m.startTime DESC"
    )
    fun getAllAbsenceDetails(userId: Long): Flow<List<gr.eduinvoice.data.database.AbsenceDetailRow>>

    @Query(
        "SELECT a.id AS id, a.groupLessonId AS groupLessonId, m.groupId AS groupId, a.studentId AS studentId, s.name AS studentName, s.surname AS studentSurname, m.date AS date, m.startTime AS startTime " +
            "FROM group_lesson_absences a INNER JOIN group_lesson_master m ON a.groupLessonId = m.id " +
            "LEFT JOIN students s ON s.id = a.studentId AND s.ownerId = a.ownerId " +
            "WHERE a.studentId = :studentId AND a.ownerId = :userId ORDER BY m.date DESC, m.startTime DESC"
    )
    fun getAbsenceDetailsForStudent(studentId: Long, userId: Long): Flow<List<gr.eduinvoice.data.database.AbsenceDetailRow>>

    @Update
    suspend fun update(lesson: Lesson)

    @Delete
    suspend fun delete(lesson: Lesson)

    @Query("DELETE FROM lessons WHERE id = :lessonId AND ownerId = :userId")
    suspend fun deleteById(lessonId: Long, userId: Long)

    @Query(
        "DELETE FROM lessons WHERE ownerId = :userId AND groupId = :groupId AND date = :date AND startTime = :startTime AND durationMinutes = :duration AND studentId IN (:studentIds)"
    )
    suspend fun deleteByGroupAndTimeForStudents(
        userId: Long,
        groupId: Long,
        date: String,
        startTime: String,
        duration: Int,
        studentIds: List<Long>
    )

    @Query(
        "UPDATE lessons SET date = :newDate, startTime = :newStartTime, durationMinutes = :newDuration, notes = :newNotes WHERE ownerId = :userId AND groupId = :groupId AND date = :oldDate AND startTime = :oldStartTime AND durationMinutes = :oldDuration AND studentId IN (:studentIds)"
    )
    suspend fun updateByGroupAndTimeForStudents(
        userId: Long,
        groupId: Long,
        oldDate: String,
        oldStartTime: String,
        oldDuration: Int,
        newDate: String,
        newStartTime: String,
        newDuration: Int,
        newNotes: String?,
        studentIds: List<Long>
    )

    @Query(
        "SELECT * FROM lessons WHERE ownerId = :userId AND groupId = :groupId AND date = :date AND startTime = :startTime AND durationMinutes = :duration"
    )
    suspend fun getLessonsByGroupAndTime(
        userId: Long,
        groupId: Long,
        date: String,
        startTime: String,
        duration: Int
    ): List<Lesson>

    @Query("DELETE FROM lessons WHERE ownerId = :userId")
    suspend fun deleteAllByOwner(userId: Long)

    @Query("SELECT * FROM lessons WHERE id = :lessonId AND ownerId = :userId")
    fun getLessonById(lessonId: Long, userId: Long): Flow<Lesson?>

    @Query("SELECT * FROM lessons WHERE studentId = :studentId AND ownerId = :userId ORDER BY date DESC, startTime DESC")
    fun getLessonsByStudentId(studentId: Long, userId: Long): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE ownerId = :userId ORDER BY date DESC, startTime DESC")
    fun getAllLessons(userId: Long): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE date BETWEEN :startDate AND :endDate AND ownerId = :userId ORDER BY date DESC, startTime DESC")
    fun getLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE studentId = :studentId AND date BETWEEN :startDate AND :endDate AND ownerId = :userId ORDER BY date ASC")
    fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE studentId = :studentId AND date BETWEEN :startDate AND :endDate AND isPaid = 0 AND ownerId = :userId ORDER BY date ASC")
    fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE date BETWEEN :startDate AND :endDate AND isPaid = 0 AND ownerId = :userId ORDER BY date ASC")
    fun getUnpaidLessonsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<Lesson>>

    @Query("UPDATE lessons SET isPaid = :paid WHERE id IN (:ids) AND ownerId = :userId")
    suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean, userId: Long)

    @Query("UPDATE lessons SET isInvoiced = :invoiced WHERE id IN (:ids) AND ownerId = :userId")
    suspend fun updateInvoicedStatus(ids: List<Long>, invoiced: Boolean, userId: Long)

    @Query("SELECT isInvoiced FROM lessons WHERE id = :lessonId AND ownerId = :userId")
    fun isLessonInvoiced(lessonId: Long, userId: Long): Flow<Boolean?>

    @Transaction
    @Query(
        """
       SELECT lessons.id AS lesson_id,
              lessons.studentId AS lesson_studentId,
              lessons.groupId AS lesson_groupId,
              lessons.masterId AS lesson_masterId,
              lessons.ownerId AS lesson_ownerId,
              lessons.date AS lesson_date,
               lessons.startTime AS lesson_startTime,
               lessons.durationMinutes AS lesson_durationMinutes,
               lessons.notes AS lesson_notes,
               lessons.isPaid AS lesson_isPaid,
               lessons.isInvoiced AS lesson_isInvoiced,
               lessons.lastModified AS lesson_lastModified,
               lessons.invoiceMasterId AS lesson_invoiceMasterId,
               lessons.paymentBatchId AS lesson_paymentBatchId,
               students.id AS student_id,
               students.name AS student_name,
               students.surname AS student_surname,
               students.parentMobile AS student_parentMobile,
               students.parentEmail AS student_parentEmail,
              students.className AS student_className,
              students.rate AS student_rate,
              students.rateType AS student_rateType,
              students.ownerId AS student_ownerId,
              students.isActive AS student_isActive,
              students.lastModified AS student_lastModified
        FROM lessons JOIN students ON lessons.studentId = students.id
        WHERE lessons.ownerId = :userId AND students.ownerId = :userId
        ORDER BY lessons.date DESC, lessons.startTime DESC
        """
    )
    fun getLessonsWithStudents(userId: Long): Flow<List<LessonWithStudent>>

    @Transaction
    @Query(
        """
       SELECT lessons.id AS lesson_id,
              lessons.studentId AS lesson_studentId,
              lessons.groupId AS lesson_groupId,
              lessons.masterId AS lesson_masterId,
              lessons.ownerId AS lesson_ownerId,
              lessons.date AS lesson_date,
               lessons.startTime AS lesson_startTime,
               lessons.durationMinutes AS lesson_durationMinutes,
               lessons.notes AS lesson_notes,
               lessons.isPaid AS lesson_isPaid,
               lessons.isInvoiced AS lesson_isInvoiced,
               lessons.lastModified AS lesson_lastModified,
               lessons.invoiceMasterId AS lesson_invoiceMasterId,
               lessons.paymentBatchId AS lesson_paymentBatchId,
               students.id AS student_id,
               students.name AS student_name,
               students.surname AS student_surname,
               students.parentMobile AS student_parentMobile,
               students.parentEmail AS student_parentEmail,
              students.className AS student_className,
              students.rate AS student_rate,
              students.rateType AS student_rateType,
              students.ownerId AS student_ownerId,
              students.isActive AS student_isActive,
              students.lastModified AS student_lastModified
        FROM lessons JOIN students ON lessons.studentId = students.id
        WHERE lessons.ownerId = :userId AND students.ownerId = :userId
        ORDER BY lessons.date DESC, lessons.startTime DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getLessonsWithStudentsPaginated(
        userId: Long,
        limit: Int,
        offset: Int
    ): List<LessonWithStudent>

    @Transaction
    @Query(
        """
       SELECT lessons.id AS lesson_id,
              lessons.studentId AS lesson_studentId,
              lessons.groupId AS lesson_groupId,
              lessons.masterId AS lesson_masterId,
              lessons.ownerId AS lesson_ownerId,
              lessons.date AS lesson_date,
               lessons.startTime AS lesson_startTime,
               lessons.durationMinutes AS lesson_durationMinutes,
               lessons.notes AS lesson_notes,
               lessons.isPaid AS lesson_isPaid,
               lessons.isInvoiced AS lesson_isInvoiced,
               lessons.lastModified AS lesson_lastModified,
               lessons.invoiceMasterId AS lesson_invoiceMasterId,
               lessons.paymentBatchId AS lesson_paymentBatchId,
               students.id AS student_id,
               students.name AS student_name,
               students.surname AS student_surname,
               students.parentMobile AS student_parentMobile,
               students.parentEmail AS student_parentEmail,
              students.className AS student_className,
              students.rate AS student_rate,
              students.rateType AS student_rateType,
              students.ownerId AS student_ownerId,
              students.isActive AS student_isActive,
              students.lastModified AS student_lastModified
        FROM lessons JOIN students ON lessons.studentId = students.id
        WHERE lessons.studentId = :studentId AND lessons.ownerId = :userId AND students.ownerId = :userId
        ORDER BY lessons.date DESC, lessons.startTime DESC
        """
    )
    fun getLessonsWithStudentsByStudent(studentId: Long, userId: Long): Flow<List<LessonWithStudent>>

    @Transaction
    @Query(
        """
       SELECT lessons.id AS lesson_id,
              lessons.studentId AS lesson_studentId,
              lessons.groupId AS lesson_groupId,
              lessons.masterId AS lesson_masterId,
              lessons.ownerId AS lesson_ownerId,
              lessons.date AS lesson_date,
               lessons.startTime AS lesson_startTime,
               lessons.durationMinutes AS lesson_durationMinutes,
               lessons.notes AS lesson_notes,
               lessons.isPaid AS lesson_isPaid,
               lessons.isInvoiced AS lesson_isInvoiced,
               lessons.lastModified AS lesson_lastModified,
               lessons.invoiceMasterId AS lesson_invoiceMasterId,
               lessons.paymentBatchId AS lesson_paymentBatchId,
               students.id AS student_id,
               students.name AS student_name,
               students.surname AS student_surname,
               students.parentMobile AS student_parentMobile,
               students.parentEmail AS student_parentEmail,
              students.className AS student_className,
              students.rate AS student_rate,
              students.rateType AS student_rateType,
              students.ownerId AS student_ownerId,
              students.isActive AS student_isActive,
              students.lastModified AS student_lastModified
        FROM lessons JOIN students ON lessons.studentId = students.id
        WHERE lessons.date BETWEEN :startDate AND :endDate AND lessons.ownerId = :userId AND students.ownerId = :userId
        ORDER BY lessons.date DESC, lessons.startTime DESC
        """
    )
    fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String, userId: Long): Flow<List<LessonWithStudent>>

    @Transaction
    @Query(
        """
       SELECT lessons.id AS lesson_id,
              lessons.studentId AS lesson_studentId,
              lessons.groupId AS lesson_groupId,
              lessons.masterId AS lesson_masterId,
              lessons.ownerId AS lesson_ownerId,
              lessons.date AS lesson_date,
               lessons.startTime AS lesson_startTime,
               lessons.durationMinutes AS lesson_durationMinutes,
               lessons.notes AS lesson_notes,
               lessons.isPaid AS lesson_isPaid,
               lessons.isInvoiced AS lesson_isInvoiced,
               lessons.lastModified AS lesson_lastModified,
               students.id AS student_id,
               students.name AS student_name,
               students.surname AS student_surname,
               students.parentMobile AS student_parentMobile,
               students.parentEmail AS student_parentEmail,
              students.className AS student_className,
              students.rate AS student_rate,
              students.rateType AS student_rateType,
              students.ownerId AS student_ownerId,
              students.isActive AS student_isActive,
              students.lastModified AS student_lastModified
        FROM lessons JOIN students ON lessons.studentId = students.id
        WHERE lessons.studentId = :studentId AND lessons.date BETWEEN :startDate AND :endDate AND lessons.ownerId = :userId AND students.ownerId = :userId
        ORDER BY lessons.date DESC, lessons.startTime DESC
        """
    )
    fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String, userId: Long): Flow<List<LessonWithStudent>>

    @Query("SELECT * FROM lessons WHERE ownerId = :userId AND masterId = :masterId")
    suspend fun getLessonsByMaster(masterId: Long, userId: Long): List<Lesson>

    @Query("SELECT COUNT(*) FROM group_lesson_absences WHERE ownerId = :userId AND groupLessonId = :masterId")
    fun getAbsenceCountForMaster(masterId: Long, userId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM lessons WHERE ownerId = :userId AND masterId = :masterId AND (isPaid = 1 OR isInvoiced = 1)")
    suspend fun countPaidOrInvoicedByMaster(masterId: Long, userId: Long): Int

    @Query("DELETE FROM lessons WHERE ownerId = :userId AND masterId = :masterId")
    suspend fun deleteLessonsByMaster(masterId: Long, userId: Long)

    @Query("DELETE FROM group_lesson_master WHERE ownerId = :userId AND id = :masterId")
    suspend fun deleteGroupLessonMasterById(masterId: Long, userId: Long)

    // Financial edit guards helpers
    @Query("SELECT COUNT(*) FROM lessons WHERE id IN (:ids) AND ownerId = :userId AND (isPaid = 1 OR isInvoiced = 1)")
    suspend fun countLockedLessons(ids: List<Long>, userId: Long): Int
}
