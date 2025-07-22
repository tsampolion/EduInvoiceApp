package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.repository.TutorBillingRepository
import javax.inject.Inject

class AddGroupLesson @Inject constructor(
    private val repository: TutorBillingRepository
) {
    suspend operator fun invoke(groupId: Long, lesson: Lesson): List<Long> =
        repository.addGroupLesson(groupId, lesson)
}
