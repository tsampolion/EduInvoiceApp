package gr.tutorbilling.domain.group

import gr.tutorbilling.data.repository.GroupRepository
import gr.tutorbilling.data.model.GroupStudentCrossRef
import javax.inject.Inject

class AddStudentToGroup @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(groupId: Long, studentId: Long) =
        repository.insertCrossRef(GroupStudentCrossRef(groupId, studentId))
}
