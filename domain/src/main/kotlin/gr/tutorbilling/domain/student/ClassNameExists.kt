package gr.tutorbilling.domain.student

import gr.tutorbilling.data.repository.StudentRepository
import javax.inject.Inject

class ClassNameExists @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(name: String): Boolean = repository.classNameExists(name)
}
