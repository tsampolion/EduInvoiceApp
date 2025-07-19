package gr.tsambala.tutorbilling.domain.student

import gr.tsambala.tutorbilling.data.repository.StudentRepository
import javax.inject.Inject

class ClassNameExists @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(name: String): Boolean = repository.classNameExists(name)
}
