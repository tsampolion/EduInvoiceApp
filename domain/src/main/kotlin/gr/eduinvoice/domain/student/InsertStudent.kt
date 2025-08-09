package gr.eduinvoice.domain.student

import gr.eduinvoice.domain.repository.DomainStudentRepository
import gr.eduinvoice.domain.model.DomainStudent
import javax.inject.Inject

class InsertStudent @Inject constructor(
    private val repository: DomainStudentRepository
) {
    suspend operator fun invoke(student: DomainStudent): Long = repository.insertStudent(student)
}
