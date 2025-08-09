package gr.eduinvoice.domain.group

import gr.eduinvoice.domain.repository.DomainGroupRepository
import gr.eduinvoice.domain.model.DomainStudentGroup
import javax.inject.Inject

class InsertGroup @Inject constructor(
    private val repository: DomainGroupRepository
) {
    suspend operator fun invoke(group: DomainStudentGroup): Long = repository.insertGroup(group)
}
