package gr.eduinvoice.domain.student

import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.repository.StudentRepository
import javax.inject.Inject

class GetStudentsPaginated @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(userId: Long, limit: Int, offset: Int): List<Student> {
        return repository.getStudentsPaginated(userId, limit, offset)
    }
} 