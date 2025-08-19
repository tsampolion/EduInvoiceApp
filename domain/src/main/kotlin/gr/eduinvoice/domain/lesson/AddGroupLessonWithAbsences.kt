package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class AddGroupLessonWithAbsences @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(
        groupId: Long,
        lesson: DomainLesson,
        absentStudentIds: List<Long>,
        userId: Long = 0
    ): List<Long> = repository.addGroupLessonWithAbsences(lesson.copy(groupId = groupId), absentStudentIds, userId)
}
