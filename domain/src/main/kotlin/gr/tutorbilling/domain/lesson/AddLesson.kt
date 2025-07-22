package gr.tutorbilling.domain.lesson

import gr.tutorbilling.data.model.Lesson
import gr.tutorbilling.data.repository.TutorBillingRepository
import javax.inject.Inject

class AddLesson @Inject constructor(
    private val repository: TutorBillingRepository
) {
    suspend operator fun invoke(lesson: Lesson): Long = repository.addLesson(lesson)
}
