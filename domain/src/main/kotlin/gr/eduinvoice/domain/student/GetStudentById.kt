package gr.eduinvoice.domain.student

import gr.eduinvoice.data.repository.StudentRepository
import gr.eduinvoice.data.model.Student
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStudentById @Inject constructor(
    private val repository: StudentRepository
) {
    operator fun invoke(id: Long): Flow<Student?> = repository.getStudentByIdAny(id)
}
