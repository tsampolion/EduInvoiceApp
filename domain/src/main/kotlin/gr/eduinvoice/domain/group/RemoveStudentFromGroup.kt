package gr.eduinvoice.domain.group

import gr.eduinvoice.domain.repository.DomainGroupRepository
import javax.inject.Inject

class RemoveStudentFromGroup @Inject constructor(
    private val repository: DomainGroupRepository
) {
    suspend operator fun invoke(groupId: Long, studentId: Long, userId: Long = 0) =
        repository.removeStudentFromGroup(studentId, groupId, userId)
}
