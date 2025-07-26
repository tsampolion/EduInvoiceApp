package gr.eduinvoice.domain.student

import gr.eduinvoice.data.repository.StudentRepository
import javax.inject.Inject

class ClassNameExists @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(name: String, userId: Long = 0): Boolean =
        repository.classNameExists(name, userId)
}
