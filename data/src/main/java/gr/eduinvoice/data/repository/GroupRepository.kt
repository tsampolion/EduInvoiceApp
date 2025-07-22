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
    fun getAllGroups(): Flow<List<StudentGroup>> = dao.getAllGroups()
    fun getGroupById(id: Long): Flow<StudentGroup?> = dao.getGroupById(id)
    suspend fun insertCrossRef(crossRef: GroupStudentCrossRef) = dao.insertCrossRef(crossRef)
    suspend fun deleteCrossRef(groupId: Long, studentId: Long) = dao.deleteCrossRef(groupId, studentId)
    fun getStudentsForGroup(groupId: Long): Flow<List<Student>> = dao.getStudentsForGroup(groupId)
}
