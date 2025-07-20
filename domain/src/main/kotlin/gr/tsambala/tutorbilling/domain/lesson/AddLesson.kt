package gr.tsambala.tutorbilling.domain.lesson

import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.repository.TutorBillingRepository
import javax.inject.Inject

class AddLesson @Inject constructor(
    private val repository: TutorBillingRepository
) {
    suspend operator fun invoke(lesson: Lesson): Long = repository.addLesson(lesson)
}
