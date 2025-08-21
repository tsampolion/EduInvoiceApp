package gr.eduinvoice.data.repository

import gr.eduinvoice.data.dao.GroupDao
import gr.eduinvoice.data.model.GroupStudentCrossRef
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class GroupRepository @Inject constructor(
    private val dao: GroupDao
) {
    suspend fun insertGroup(group: StudentGroup): Long = withContext(Dispatchers.IO) { dao.insertGroup(group) }
    suspend fun updateGroup(group: StudentGroup) = withContext(Dispatchers.IO) { dao.updateGroup(group) }
    suspend fun deleteGroup(group: StudentGroup) = withContext(Dispatchers.IO) { dao.deleteGroup(group) }
    suspend fun softArchiveGroup(groupId: Long, userId: Long) = withContext(Dispatchers.IO) { dao.softArchiveGroup(groupId, userId) }
    fun getAllGroups(userId: Long): Flow<List<StudentGroup>> = dao.getAllGroups(userId)
    fun getGroupById(id: Long, userId: Long): Flow<StudentGroup?> =
        dao.getGroupById(id, userId)
    fun getGroupsForStudent(studentId: Long, userId: Long): Flow<List<StudentGroup>> =
        dao.getGroupsForStudent(studentId, userId)
    suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) = withContext(Dispatchers.IO) { dao.insertCrossRef(crossRef) }
    suspend fun deleteCrossRef(groupId: Long, studentId: Long, userId: Long) = withContext(Dispatchers.IO) {
        dao.deleteCrossRef(groupId, studentId, userId)
    }
    suspend fun deleteAllCrossRefsForGroup(groupId: Long, userId: Long) = withContext(Dispatchers.IO) {
        dao.deleteAllCrossRefsForGroup(groupId, userId)
    }
    fun getStudentsForGroup(groupId: Long, userId: Long): Flow<List<Student>> =
        dao.getStudentsForGroup(groupId, userId)
}
