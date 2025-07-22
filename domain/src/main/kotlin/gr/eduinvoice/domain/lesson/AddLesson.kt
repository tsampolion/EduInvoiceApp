package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.repository.TutorBillingRepository
import javax.inject.Inject

class AddLesson @Inject constructor(
    private val repository: TutorBillingRepository
) {
    suspend operator fun invoke(lesson: Lesson): Long = repository.addLesson(lesson)
}
