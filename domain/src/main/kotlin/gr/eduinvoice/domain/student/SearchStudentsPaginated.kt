package gr.eduinvoice.domain.student

import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.repository.StudentRepository
import javax.inject.Inject

class SearchStudentsPaginated @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(userId: Long, searchQuery: String, limit: Int, offset: Int): List<Student> {
        return repository.searchStudentsPaginated(userId, searchQuery, limit, offset)
    }
} 