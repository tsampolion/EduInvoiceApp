package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class AddGroupLesson @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(groupId: Long, lesson: DomainLesson, userId: Long = 0): Long =
        repository.addGroupLesson(lesson, userId)
}
