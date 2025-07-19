package gr.tsambala.tutorbilling.domain.student

import gr.tsambala.tutorbilling.data.repository.StudentRepository
import gr.tsambala.tutorbilling.data.model.Student
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveStudents @Inject constructor(
    private val repository: StudentRepository
) {
    operator fun invoke(): Flow<List<Student>> = repository.getAllActiveStudents()
}
