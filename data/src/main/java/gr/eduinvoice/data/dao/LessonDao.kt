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

    @Update
    suspend fun update(lesson: Lesson)

    @Delete
    suspend fun delete(lesson: Lesson)

    @Query("DELETE FROM lessons WHERE id = :lessonId AND ownerId = :userId")
    suspend fun deleteById(lessonId: Long, userId: Long)

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
}
