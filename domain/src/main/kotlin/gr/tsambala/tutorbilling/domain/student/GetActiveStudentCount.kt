package gr.tsambala.tutorbilling.domain.student

import gr.tsambala.tutorbilling.data.repository.StudentRepository
import javax.inject.Inject

class GetActiveStudentCount @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(): Int = repository.getActiveStudentCount()
}
