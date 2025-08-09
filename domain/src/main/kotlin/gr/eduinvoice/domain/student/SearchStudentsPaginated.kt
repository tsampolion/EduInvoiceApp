package gr.eduinvoice.domain.student

import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.repository.DomainStudentRepository
import javax.inject.Inject

class SearchStudentsPaginated @Inject constructor(
    private val repository: DomainStudentRepository
) {
    suspend operator fun invoke(userId: Long, searchQuery: String, limit: Int, offset: Int): List<DomainStudent> {
        return repository.searchStudentsPaginated(userId, searchQuery, limit, offset)
    }
} 