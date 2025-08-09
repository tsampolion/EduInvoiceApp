package gr.eduinvoice.domain.student

import gr.eduinvoice.domain.repository.DomainStudentRepository
import javax.inject.Inject

class GetActiveStudentCount @Inject constructor(
    private val repository: DomainStudentRepository
) {
    suspend operator fun invoke(userId: Long = 0): Int =
        repository.getActiveStudentCount(userId)
}
