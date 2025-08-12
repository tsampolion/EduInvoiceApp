package gr.eduinvoice.data.dao

import androidx.room.*
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Query("UPDATE students SET isActive = 0 WHERE id = :studentId AND ownerId = :userId")
    suspend fun softDeleteStudent(studentId: Long, userId: Long)

    @Query("SELECT * FROM students WHERE id = :studentId AND ownerId = :userId AND isActive = 1")
    fun getStudentById(studentId: Long, userId: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE isActive = 1 AND ownerId = :userId ORDER BY name ASC")
    fun getAllActiveStudents(userId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE isActive = 0 AND ownerId = :userId ORDER BY name ASC")
    fun getArchivedStudents(userId: Long): Flow<List<Student>>

    @Query("""
        SELECT * FROM students
        WHERE isActive = 1 AND ownerId = :userId
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getStudentsPaginated(
        userId: Long,
        limit: Int,
        offset: Int
    ): List<Student>

    @Query("""
        SELECT * FROM students
        WHERE isActive = 1 AND ownerId = :userId
        AND (LOWER(name) LIKE '%' || LOWER(:searchQuery) || '%'
             OR LOWER(className) LIKE '%' || LOWER(:searchQuery) || '%')
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchStudentsPaginated(
        userId: Long,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): List<Student>

    @Query("UPDATE students SET isActive = 1 WHERE id = :studentId AND ownerId = :userId")
    suspend fun restoreStudent(studentId: Long, userId: Long)

    @Query("SELECT * FROM students WHERE id = :studentId AND ownerId = :userId")
    fun getStudentByIdAny(studentId: Long, userId: Long): Flow<Student?>


    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1 AND ownerId = :userId")
    suspend fun getActiveStudentCount(userId: Long): Int

    @Query("SELECT COUNT(*) FROM students WHERE LOWER(className) = LOWER(:name) AND ownerId = :userId")
    suspend fun classNameExists(name: String, userId: Long): Int
}
