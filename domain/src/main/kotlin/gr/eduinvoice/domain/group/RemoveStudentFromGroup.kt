package gr.eduinvoice.domain.group

import gr.eduinvoice.data.repository.GroupRepository
import javax.inject.Inject

class RemoveStudentFromGroup @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(groupId: Long, studentId: Long, userId: Long = 0) =
        repository.deleteCrossRef(groupId, studentId, userId)
}
