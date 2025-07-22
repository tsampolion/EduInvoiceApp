package gr.tutorbilling.data.dao

import androidx.room.*
import gr.tutorbilling.data.model.GroupStudentCrossRef
import gr.tutorbilling.data.model.Student
import gr.tutorbilling.data.model.StudentGroup
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

    @Query("SELECT * FROM ${gr.tutorbilling.data.database.DatabaseConstants.GROUPS_TABLE} ORDER BY name ASC")
    fun getAllGroups(): Flow<List<StudentGroup>>

    @Query("SELECT * FROM ${gr.tutorbilling.data.database.DatabaseConstants.GROUPS_TABLE} WHERE id = :id")
    fun getGroupById(id: Long): Flow<StudentGroup?>

    // Cross-ref operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: GroupStudentCrossRef)

    @Query(
        "DELETE FROM ${gr.tutorbilling.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE} WHERE groupId = :groupId AND studentId = :studentId"
    )
    suspend fun deleteCrossRef(groupId: Long, studentId: Long)

    @Transaction
    @Query(
        "SELECT students.* FROM students INNER JOIN ${gr.tutorbilling.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE} ON students.id = ${gr.tutorbilling.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE}.studentId WHERE ${gr.tutorbilling.data.database.DatabaseConstants.GROUP_STUDENT_CROSS_REF_TABLE}.groupId = :groupId"
    )
    fun getStudentsForGroup(groupId: Long): Flow<List<Student>>
}
