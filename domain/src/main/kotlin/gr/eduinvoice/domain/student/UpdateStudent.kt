package gr.eduinvoice.domain.student

import gr.eduinvoice.domain.repository.DomainStudentRepository
import gr.eduinvoice.domain.model.DomainStudent
import javax.inject.Inject

class UpdateStudent @Inject constructor(
    private val repository: DomainStudentRepository
) {
    suspend operator fun invoke(student: DomainStudent) = repository.updateStudent(student)
}
