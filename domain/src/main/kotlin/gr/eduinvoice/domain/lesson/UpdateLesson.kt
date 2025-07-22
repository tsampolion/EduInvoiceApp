package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.repository.TutorBillingRepository
import javax.inject.Inject

class UpdateLesson @Inject constructor(
    private val repository: TutorBillingRepository
) {
    suspend operator fun invoke(lesson: Lesson) = repository.updateLesson(lesson)
}
