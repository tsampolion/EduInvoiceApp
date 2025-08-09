package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import gr.eduinvoice.domain.model.DomainLesson
import javax.inject.Inject

class InsertLesson @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(lesson: DomainLesson): Long = repository.addLesson(lesson, 0)
}
