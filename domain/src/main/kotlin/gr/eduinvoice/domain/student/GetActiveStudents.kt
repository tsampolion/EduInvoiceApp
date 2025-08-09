package gr.eduinvoice.domain.student

import gr.eduinvoice.domain.repository.DomainStudentRepository
import gr.eduinvoice.domain.model.DomainStudent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveStudents @Inject constructor(
    private val repository: DomainStudentRepository
) {
    operator fun invoke(userId: Long = 0): Flow<List<DomainStudent>> =
        repository.getAllActiveStudents(userId)
}
