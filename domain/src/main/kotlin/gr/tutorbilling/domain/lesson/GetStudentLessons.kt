package gr.tutorbilling.domain.lesson

import gr.tutorbilling.data.model.Lesson
import gr.tutorbilling.data.repository.EduInvoiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStudentLessons @Inject constructor(
    private val repository: EduInvoiceRepository
) {
    operator fun invoke(studentId: Long): Flow<List<Lesson>> =
        repository.getLessonsForStudent(studentId)
}
