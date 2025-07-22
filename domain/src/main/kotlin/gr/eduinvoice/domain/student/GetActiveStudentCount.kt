package gr.eduinvoice.domain.student

import gr.eduinvoice.data.repository.StudentRepository
import javax.inject.Inject

class GetActiveStudentCount @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(): Int = repository.getActiveStudentCount()
}
