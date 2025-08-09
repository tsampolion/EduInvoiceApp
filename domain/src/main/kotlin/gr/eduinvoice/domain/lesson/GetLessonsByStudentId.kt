package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import gr.eduinvoice.domain.model.DomainLesson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonsByStudentId @Inject constructor(
    private val repository: DomainLessonRepository
) {
    operator fun invoke(id: Long, userId: Long = 0): Flow<List<DomainLesson>> =
        repository.getStudentLessons(id, userId)
}
