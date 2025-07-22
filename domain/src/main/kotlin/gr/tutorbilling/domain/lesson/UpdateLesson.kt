package gr.tutorbilling.domain.lesson

import gr.tutorbilling.data.model.Lesson
import gr.tutorbilling.data.repository.TutorBillingRepository
import javax.inject.Inject

class UpdateLesson @Inject constructor(
    private val repository: TutorBillingRepository
) {
    suspend operator fun invoke(lesson: Lesson) = repository.updateLesson(lesson)
}
