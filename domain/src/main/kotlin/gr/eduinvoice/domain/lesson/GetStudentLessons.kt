package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.repository.DomainLessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStudentLessons @Inject constructor(
    private val repository: DomainLessonRepository
) {
    operator fun invoke(studentId: Long, userId: Long = 0): Flow<List<DomainLesson>> =
        repository.getStudentLessons(studentId, userId)
}
