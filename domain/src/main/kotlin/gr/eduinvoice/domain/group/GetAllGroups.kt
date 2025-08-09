package gr.eduinvoice.domain.group

import gr.eduinvoice.domain.repository.DomainGroupRepository
import gr.eduinvoice.domain.model.DomainStudentGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllGroups @Inject constructor(
    private val repository: DomainGroupRepository
) {
    operator fun invoke(userId: Long = 0): Flow<List<DomainStudentGroup>> =
        repository.getAllGroups(userId)
}
