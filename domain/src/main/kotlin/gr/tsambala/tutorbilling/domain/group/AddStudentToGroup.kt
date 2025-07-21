package gr.tsambala.tutorbilling.domain.group

import gr.tsambala.tutorbilling.data.repository.GroupRepository
import gr.tsambala.tutorbilling.data.model.GroupStudentCrossRef
import javax.inject.Inject

class AddStudentToGroup @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(groupId: Long, studentId: Long) =
        repository.insertCrossRef(GroupStudentCrossRef(groupId, studentId))
}
