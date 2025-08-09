package gr.eduinvoice.domain.group

import gr.eduinvoice.domain.repository.DomainGroupRepository
import gr.eduinvoice.domain.model.DomainStudentGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupById @Inject constructor(
    private val repository: DomainGroupRepository
) {
    operator fun invoke(id: Long, userId: Long = 0): Flow<DomainStudentGroup?> =
        repository.getGroupById(id, userId)
}
