package gr.eduinvoice.domain.student

import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.repository.DomainStudentRepository
import javax.inject.Inject

class GetStudentsPaginated @Inject constructor(
    private val repository: DomainStudentRepository
) {
    suspend operator fun invoke(userId: Long, limit: Int, offset: Int): List<DomainStudent> {
        return repository.getStudentsPaginated(userId, limit, offset)
    }
} 