package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import gr.eduinvoice.domain.model.DomainLesson
import javax.inject.Inject

class GetLessonsWithStudentsPaginated @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(userId: Long, limit: Int, offset: Int): List<DomainLesson> {
        return repository.getLessonsWithStudentsPaginated(userId, limit, offset)
    }
}
