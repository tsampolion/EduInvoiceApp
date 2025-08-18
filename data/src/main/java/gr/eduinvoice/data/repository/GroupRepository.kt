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
    suspend fun softArchiveGroup(groupId: Long, userId: Long) = dao.softArchiveGroup(groupId, userId)
    fun getAllGroups(userId: Long): Flow<List<StudentGroup>> = dao.getAllGroups(userId)
    fun getGroupById(id: Long, userId: Long): Flow<StudentGroup?> =
        dao.getGroupById(id, userId)
    suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) = dao.insertCrossRef(crossRef)
    suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long) =
        dao.deleteCrossRef(groupId, studentId, userId)
    suspend fun deleteAllCrossRefsForGroup(groupId: Long, userId: Long) =
        dao.deleteAllCrossRefsForGroup(groupId, userId)
    fun getStudentsForGroup(groupId: Long, userId: Long): Flow<List<Student>> =
        dao.getStudentsForGroup(groupId, userId)
}
