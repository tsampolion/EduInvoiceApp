package gr.tsambala.tutorbilling.domain.lesson

import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.repository.TutorBillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStudentLessons @Inject constructor(
    private val repository: TutorBillingRepository
) {
    operator fun invoke(studentId: Long): Flow<List<Lesson>> =
        repository.getLessonsForStudent(studentId)
}
