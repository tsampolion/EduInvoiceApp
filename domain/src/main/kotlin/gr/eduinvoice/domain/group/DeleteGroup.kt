package gr.eduinvoice.domain.group

import gr.eduinvoice.domain.repository.DomainGroupRepository
import gr.eduinvoice.domain.model.DomainStudentGroup
import javax.inject.Inject

class DeleteGroup @Inject constructor(
    private val repository: DomainGroupRepository
) {
    suspend operator fun invoke(group: DomainStudentGroup, userId: Long = 0) =
        repository.deleteGroup(group.id, userId)
}
