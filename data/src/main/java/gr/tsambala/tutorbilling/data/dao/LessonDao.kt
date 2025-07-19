package gr.tsambala.tutorbilling.data.dao

import androidx.room.*
import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.database.LessonWithStudent
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Insert
    suspend fun insert(lesson: Lesson): Long

    @Update
    suspend fun update(lesson: Lesson)

    @Delete
    suspend fun delete(lesson: Lesson)

    @Query("DELETE FROM lessons WHERE id = :lessonId")
    suspend fun deleteById(lessonId: Long)

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun getLessonById(lessonId: Long): Flow<Lesson?>

    @Query("SELECT * FROM lessons WHERE studentId = :studentId ORDER BY date DESC, startTime DESC")
    fun getLessonsByStudentId(studentId: Long): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons ORDER BY date DESC, startTime DESC")
    fun getAllLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    fun getLessonsInDateRange(startDate: String, endDate: String): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE studentId = :studentId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE studentId = :studentId AND date BETWEEN :startDate AND :endDate AND isPaid = 0 ORDER BY date ASC")
    fun getUnpaidLessonsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE date BETWEEN :startDate AND :endDate AND isPaid = 0 ORDER BY date ASC")
    fun getUnpaidLessonsInDateRange(startDate: String, endDate: String): Flow<List<Lesson>>

    @Query("UPDATE lessons SET isPaid = :paid WHERE id IN (:ids)")
    suspend fun updatePaidStatus(ids: List<Long>, paid: Boolean)

    @Transaction
    @Query(
        """
        SELECT lessons.id AS lesson_id,
               lessons.studentId AS lesson_studentId,
               lessons.date AS lesson_date,
               lessons.startTime AS lesson_startTime,
               lessons.durationMinutes AS lesson_durationMinutes,
               lessons.notes AS lesson_notes,
               lessons.isPaid AS lesson_isPaid,
               students.id AS student_id,
               students.name AS student_name,
               students.surname AS student_surname,
               students.parentMobile AS student_parentMobile,
               students.parentEmail AS student_parentEmail,
               students.className AS student_className,
               students.rate AS student_rate,
               students.rateType AS student_rateType,
               students.isActive AS student_isActive
        FROM lessons JOIN students ON lessons.studentId = students.id
        ORDER BY lessons.date DESC, lessons.startTime DESC
        """
    )
    fun getLessonsWithStudents(): Flow<List<LessonWithStudent>>

    @Transaction
    @Query(
        """
        SELECT lessons.id AS lesson_id,
               lessons.studentId AS lesson_studentId,
               lessons.date AS lesson_date,
               lessons.startTime AS lesson_startTime,
               lessons.durationMinutes AS lesson_durationMinutes,
               lessons.notes AS lesson_notes,
               lessons.isPaid AS lesson_isPaid,
               students.id AS student_id,
               students.name AS student_name,
               students.surname AS student_surname,
               students.parentMobile AS student_parentMobile,
               students.parentEmail AS student_parentEmail,
               students.className AS student_className,
               students.rate AS student_rate,
               students.rateType AS student_rateType,
               students.isActive AS student_isActive
        FROM lessons JOIN students ON lessons.studentId = students.id
        WHERE lessons.studentId = :studentId
        ORDER BY lessons.date DESC, lessons.startTime DESC
        """
    )
    fun getLessonsWithStudentsByStudent(studentId: Long): Flow<List<LessonWithStudent>>

    @Transaction
    @Query(
        """
        SELECT lessons.id AS lesson_id,
               lessons.studentId AS lesson_studentId,
               lessons.date AS lesson_date,
               lessons.startTime AS lesson_startTime,
               lessons.durationMinutes AS lesson_durationMinutes,
               lessons.notes AS lesson_notes,
               lessons.isPaid AS lesson_isPaid,
               students.id AS student_id,
               students.name AS student_name,
               students.surname AS student_surname,
               students.parentMobile AS student_parentMobile,
               students.parentEmail AS student_parentEmail,
               students.className AS student_className,
               students.rate AS student_rate,
               students.rateType AS student_rateType,
               students.isActive AS student_isActive
        FROM lessons JOIN students ON lessons.studentId = students.id
        WHERE lessons.date BETWEEN :startDate AND :endDate
        ORDER BY lessons.date DESC, lessons.startTime DESC
        """
    )
    fun getLessonsWithStudentsInDateRange(startDate: String, endDate: String): Flow<List<LessonWithStudent>>

    @Transaction
    @Query(
        """
        SELECT lessons.id AS lesson_id,
               lessons.studentId AS lesson_studentId,
               lessons.date AS lesson_date,
               lessons.startTime AS lesson_startTime,
               lessons.durationMinutes AS lesson_durationMinutes,
               lessons.notes AS lesson_notes,
               lessons.isPaid AS lesson_isPaid,
               students.id AS student_id,
               students.name AS student_name,
               students.surname AS student_surname,
               students.parentMobile AS student_parentMobile,
               students.parentEmail AS student_parentEmail,
               students.className AS student_className,
               students.rate AS student_rate,
               students.rateType AS student_rateType,
               students.isActive AS student_isActive
        FROM lessons JOIN students ON lessons.studentId = students.id
        WHERE lessons.studentId = :studentId AND lessons.date BETWEEN :startDate AND :endDate
        ORDER BY lessons.date DESC, lessons.startTime DESC
        """
    )
    fun getLessonsWithStudentsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<LessonWithStudent>>
}
