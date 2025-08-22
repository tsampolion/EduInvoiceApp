package gr.eduinvoice.domain.student

import gr.eduinvoice.domain.repository.DomainStudentRepository
import gr.eduinvoice.domain.model.DomainStudent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStudentByIdAny @Inject constructor(
    private val repository: DomainStudentRepository
) {
    operator fun invoke(id: Long, userId: Long = 0): Flow<DomainStudent?> =
        repository.getStudentByIdAny(id, userId)
}
