package gr.eduinvoice.domain.student

import gr.eduinvoice.domain.repository.DomainStudentRepository
import javax.inject.Inject

class ClassNameExists @Inject constructor(
    private val repository: DomainStudentRepository
) {
    suspend operator fun invoke(name: String, userId: Long = 0): Boolean =
        repository.classNameExists(name, userId)
}
