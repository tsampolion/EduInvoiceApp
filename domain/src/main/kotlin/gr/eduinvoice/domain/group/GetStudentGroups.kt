package gr.eduinvoice.domain.group

import gr.eduinvoice.domain.model.DomainStudentGroup
import gr.eduinvoice.domain.repository.DomainGroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStudentGroups @Inject constructor(
    private val repository: DomainGroupRepository
) {
    operator fun invoke(studentId: Long, userId: Long): Flow<List<DomainStudentGroup>> =
        repository.getStudentGroups(studentId, userId)
}
