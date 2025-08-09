package gr.eduinvoice.domain.group

import gr.eduinvoice.domain.repository.DomainGroupRepository
import gr.eduinvoice.domain.model.DomainStudentGroup
import javax.inject.Inject

class UpdateGroup @Inject constructor(
    private val repository: DomainGroupRepository
) {
    suspend operator fun invoke(group: DomainStudentGroup) = repository.updateGroup(group)
}
