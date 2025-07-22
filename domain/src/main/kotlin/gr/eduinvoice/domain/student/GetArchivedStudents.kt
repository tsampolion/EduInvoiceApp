package gr.eduinvoice.domain.student

import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArchivedStudents @Inject constructor(
    private val repository: StudentRepository
) {
    operator fun invoke(): Flow<List<Student>> = repository.getArchivedStudents()
}
