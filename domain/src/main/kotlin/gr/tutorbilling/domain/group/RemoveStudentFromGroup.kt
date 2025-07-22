package gr.tutorbilling.domain.group

import gr.tutorbilling.data.repository.GroupRepository
import javax.inject.Inject

class RemoveStudentFromGroup @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(groupId: Long, studentId: Long) =
        repository.deleteCrossRef(groupId, studentId)
}
