package gr.eduinvoice.domain.group

import gr.eduinvoice.domain.repository.DomainGroupRepository
import gr.eduinvoice.domain.model.DomainStudent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupStudents @Inject constructor(
    private val repository: DomainGroupRepository
) {
    operator fun invoke(groupId: Long, userId: Long = 0): Flow<List<DomainStudent>> =
        repository.getGroupStudents(groupId, userId)
}
