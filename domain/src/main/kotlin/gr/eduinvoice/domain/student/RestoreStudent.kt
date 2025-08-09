package gr.eduinvoice.domain.student

import gr.eduinvoice.domain.repository.DomainStudentRepository
import javax.inject.Inject

class RestoreStudent @Inject constructor(
    private val repository: DomainStudentRepository
) {
    suspend operator fun invoke(id: Long, userId: Long) =
        repository.restoreStudent(id, userId)
}
