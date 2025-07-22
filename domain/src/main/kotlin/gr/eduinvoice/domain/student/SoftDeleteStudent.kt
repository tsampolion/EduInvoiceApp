package gr.eduinvoice.domain.student

import gr.eduinvoice.data.repository.StudentRepository
import javax.inject.Inject

class SoftDeleteStudent @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(id: Long) = repository.softDeleteStudent(id)
}
