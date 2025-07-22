package gr.eduinvoice.domain.student

import gr.eduinvoice.data.repository.StudentRepository
import javax.inject.Inject

class ClassNameExists @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(name: String): Boolean = repository.classNameExists(name)
}
