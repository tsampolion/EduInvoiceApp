package gr.eduinvoice.data.dao

import androidx.room.*
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    // StudentGroup CRUD
    @Insert
    suspend fun insertGroup(group: StudentGroup): Long

    @Update
    suspend fun updateGroup(group: StudentGroup)

    @Delete
    suspend fun deleteGroup(group: StudentGroup)

    @Query("SELECT * FROM ${gr.eduinvoice.data.database.DatabaseConstants.GROUPS_TABLE} WHERE ownerId = :userId ORDER BY name ASC")
    fun getAllGroups(userId: Long): Flow<List<StudentGroup>>

    @Query("SELECT * FROM ${gr.eduinvoice.data.database.DatabaseConstants.GROUPS_TABLE} WHERE id = :id AND ownerId = :userId")
    fun getGroupById(id: Long, userId: Long): Flow<StudentGroup?>

    // Cross-ref operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: GroupStudentCrossRef)

    @Query(
        "DELETE FROM ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE} WHERE groupId = :groupId AND studentId = :studentId AND ownerId = :userId"
    )
    suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long)

    @Transaction
    @Query(
        "SELECT students.* FROM students INNER JOIN ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE} ON students.id = ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE}.studentId WHERE ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE}.groupId = :groupId AND ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE}.ownerId = :userId AND students.ownerId = :userId"
    )
    fun getStudentsForGroup(groupId: Long, userId: Long): Flow<List<Student>>
}
