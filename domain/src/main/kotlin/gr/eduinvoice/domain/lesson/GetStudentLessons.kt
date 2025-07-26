package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.repository.TutorBillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStudentLessons @Inject constructor(
    private val repository: TutorBillingRepository
) {
    operator fun invoke(studentId: Long, userId: Long = 0): Flow<List<Lesson>> =
        repository.getLessonsForStudent(studentId, userId)
}
