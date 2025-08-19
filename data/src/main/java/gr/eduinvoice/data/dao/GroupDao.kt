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

    @Query("SELECT * FROM ${gr.eduinvoice.data.database.DatabaseConstants.GROUPS_TABLE} WHERE ownerId = :userId AND isActive = 1 ORDER BY name ASC")
    fun getAllGroups(userId: Long): Flow<List<StudentGroup>>

    @Query("SELECT * FROM ${gr.eduinvoice.data.database.DatabaseConstants.GROUPS_TABLE} WHERE id = :id AND ownerId = :userId")
    fun getGroupById(id: Long, userId: Long): Flow<StudentGroup?>

    @Transaction
    @Query(
        "SELECT g.* FROM ${gr.eduinvoice.data.database.DatabaseConstants.GROUPS_TABLE} g " +
            "INNER JOIN ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE} x ON g.id = x.groupId " +
            "WHERE x.studentId = :studentId AND g.ownerId = :userId AND x.ownerId = :userId"
    )
    fun getGroupsForStudent(studentId: Long, userId: Long): Flow<List<StudentGroup>>

    @Query("UPDATE ${gr.eduinvoice.data.database.DatabaseConstants.GROUPS_TABLE} SET isActive = 0 WHERE id = :groupId AND ownerId = :userId")
    suspend fun softArchiveGroup(groupId: Long, userId: Long)

    // Cross-ref operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: GroupStudentCrossRef)

    @Query(
        "DELETE FROM ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE} WHERE groupId = :groupId AND studentId = :studentId AND ownerId = :userId"
    )
    suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long)

    @Query("DELETE FROM ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE} WHERE groupId = :groupId AND ownerId = :userId")
    suspend fun deleteAllCrossRefsForGroup(groupId: Long, userId: Long)

    @Query("DELETE FROM ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE} WHERE ownerId = :userId")
    suspend fun deleteAllCrossRefsByOwner(userId: Long)

    @Query("DELETE FROM ${gr.eduinvoice.data.database.DatabaseConstants.GROUPS_TABLE} WHERE ownerId = :userId")
    suspend fun deleteAllGroupsByOwner(userId: Long)

    @Transaction
    @Query(
        "SELECT students.* FROM students INNER JOIN ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE} ON students.id = ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE}.studentId WHERE ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE}.groupId = :groupId AND ${gr.eduinvoice.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE}.ownerId = :userId AND students.ownerId = :userId"
    )
    fun getStudentsForGroup(groupId: Long, userId: Long): Flow<List<Student>>
}
