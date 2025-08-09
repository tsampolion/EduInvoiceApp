package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class UpdateLesson @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(lesson: DomainLesson, userId: Long = 0) =
        repository.updateLesson(lesson, userId)
}
