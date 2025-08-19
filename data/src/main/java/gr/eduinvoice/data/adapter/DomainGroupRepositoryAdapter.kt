package gr.eduinvoice.data.adapter

import gr.eduinvoice.domain.repository.DomainGroupRepository
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.model.DomainStudentGroup
import gr.eduinvoice.data.repository.GroupRepository
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.StudentGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DomainGroupRepositoryAdapter @Inject constructor(
    private val groupRepository: GroupRepository
) : DomainGroupRepository {

    override suspend fun insertGroup(group: DomainStudentGroup): Long =
        groupRepository.insertGroup(group.toDataModel())

    override suspend fun updateGroup(group: DomainStudentGroup) =
        groupRepository.updateGroup(group.toDataModel())

    override suspend fun deleteGroup(groupId: Long, userId: Long) {
        val group = groupRepository.getGroupById(groupId, userId).first()
        group?.let {
            // Clean up cross-refs then delete group
            groupRepository.deleteAllCrossRefsForGroup(groupId, userId)
            groupRepository.deleteGroup(it)
        }
    }

    override suspend fun archiveGroup(groupId: Long, userId: Long) {
        groupRepository.softArchiveGroup(groupId, userId)
    }

    override fun getAllGroups(userId: Long): Flow<List<DomainStudentGroup>> =
        groupRepository.getAllGroups(userId).map { groups ->
            groups.map { it.toDomainModel() }
        }

    override fun getGroupById(groupId: Long, userId: Long): Flow<DomainStudentGroup?> =
        groupRepository.getGroupById(groupId, userId).map { it?.toDomainModel() }

    override fun getStudentGroups(studentId: Long, userId: Long): Flow<List<DomainStudentGroup>> =
        groupRepository.getGroupsForStudent(studentId, userId).map { it.map { g -> g.toDomainModel() } }

    override suspend fun addStudentToGroup(studentId: Long, groupId: Long, userId: Long) =
        groupRepository.insertCrossRef(gr.eduinvoice.data.model.GroupStudentCrossRef(groupId, studentId, userId))

    override suspend fun removeStudentFromGroup(studentId: Long, groupId: Long, userId: Long) =
        groupRepository.deleteCrossRef(groupId, studentId, userId)

    override fun getGroupStudents(groupId: Long, userId: Long): Flow<List<DomainStudent>> =
        groupRepository.getStudentsForGroup(groupId, userId).map { students ->
            students.map { it.toDomainModel() }
        }

    private fun DomainStudentGroup.toDataModel(): StudentGroup = StudentGroup(
        id = id,
        ownerId = ownerId,
        name = name,
        className = className,
        rate = rate,
        rateType = rateType,
        isActive = isActive
    )

    private fun StudentGroup.toDomainModel(): DomainStudentGroup = DomainStudentGroup(
        id = id,
        ownerId = ownerId,
        name = name,
        className = className,
        rate = rate,
        rateType = rateType,
        isActive = isActive
    )

    private fun Student.toDomainModel(): DomainStudent = DomainStudent(
        id = id,
        ownerId = ownerId,
        name = name,
        surname = surname,
        parentMobile = parentMobile,
        parentEmail = parentEmail,
        className = className,
        rate = rate,
        rateType = rateType,
        isActive = isActive,
        lastModified = lastModified
    )
}
