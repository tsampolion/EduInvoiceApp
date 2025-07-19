package gr.tsambala.tutorbilling.data.dao

import androidx.room.*
import gr.tsambala.tutorbilling.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Query("UPDATE students SET isActive = 0 WHERE id = :studentId")
    suspend fun softDeleteStudent(studentId: Long)

    @Query("SELECT * FROM students WHERE id = :studentId AND isActive = 1")
    fun getStudentById(studentId: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE isActive = 0 ORDER BY name ASC")
    fun getArchivedStudents(): Flow<List<Student>>

    @Query("UPDATE students SET isActive = 1 WHERE id = :studentId")
    suspend fun restoreStudent(studentId: Long)

    @Query("SELECT * FROM students WHERE id = :studentId")
    fun getStudentByIdAny(studentId: Long): Flow<Student?>


    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1")
    suspend fun getActiveStudentCount(): Int

    @Query("SELECT COUNT(*) FROM students WHERE LOWER(className) = LOWER(:name)")
    suspend fun classNameExists(name: String): Int
}
