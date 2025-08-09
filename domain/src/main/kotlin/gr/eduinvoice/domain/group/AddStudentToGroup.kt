package gr.eduinvoice.domain.group

import gr.eduinvoice.domain.repository.DomainGroupRepository
import javax.inject.Inject

class AddStudentToGroup @Inject constructor(
    private val repository: DomainGroupRepository
) {
    suspend operator fun invoke(groupId: Long, studentId: Long, userId: Long = 0) =
        repository.addStudentToGroup(studentId, groupId, userId)
}
