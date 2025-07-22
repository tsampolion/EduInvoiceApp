package gr.eduinvoice.domain.student

import gr.eduinvoice.data.repository.StudentRepository
import javax.inject.Inject

class RestoreStudent @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(id: Long) = repository.restoreStudent(id)
}
