package gr.eduinvoice.domain.repository

import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.model.DomainStudentGroup
import kotlinx.coroutines.flow.Flow

interface DomainGroupRepository {
    suspend fun insertGroup(group: DomainStudentGroup): Long
    suspend fun updateGroup(group: DomainStudentGroup)
    suspend fun deleteGroup(groupId: Long, userId: Long)
    suspend fun archiveGroup(groupId: Long, userId: Long)
    fun getAllGroups(userId: Long): Flow<List<DomainStudentGroup>>
    fun getGroupById(groupId: Long, userId: Long): Flow<DomainStudentGroup?>
    fun getStudentGroups(studentId: Long, userId: Long): Flow<List<DomainStudentGroup>>
    suspend fun addStudentToGroup(studentId: Long, groupId: Long, userId: Long)
    suspend fun removeStudentFromGroup(studentId: Long, groupId: Long, userId: Long)
    fun getGroupStudents(groupId: Long, userId: Long): Flow<List<DomainStudent>>
}
