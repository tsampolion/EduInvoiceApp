package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import gr.eduinvoice.domain.model.DomainLesson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonsWithStudentsByStudentAndDateRange @Inject constructor(
    private val repository: DomainLessonRepository
) {
    operator fun invoke(
        studentId: Long,
        start: String,
        end: String,
        userId: Long = 0
    ): Flow<List<DomainLesson>> =
        repository.getLessonsWithStudentsByStudentAndDateRange(studentId, start, end, userId)
}
