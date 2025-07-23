package gr.eduinvoice.data.repository

import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val dao: GroupDao
) {
    suspend fun insertGroup(group: StudentGroup): Long = dao.insertGroup(group)
    suspend fun updateGroup(group: StudentGroup) = dao.updateGroup(group)
    suspend fun deleteGroup(group: StudentGroup) = dao.deleteGroup(group)
    fun getAllGroups(userId: Long = 0): Flow<List<StudentGroup>> = dao.getAllGroups(userId)
    fun getGroupById(id: Long, userId: Long = 0): Flow<StudentGroup?> = dao.getGroupById(id, userId)
    suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) = dao.insertCrossRef(crossRef)
    suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long = 0) = dao.deleteCrossRef(groupId, studentId, userId)
    fun getStudentsForGroup(groupId: Long, userId: Long = 0): Flow<List<Student>> = dao.getStudentsForGroup(groupId, userId)
}
