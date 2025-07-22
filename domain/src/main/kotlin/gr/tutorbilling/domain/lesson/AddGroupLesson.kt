package gr.tutorbilling.domain.lesson

import gr.tutorbilling.data.model.Lesson
import gr.tutorbilling.data.repository.TutorBillingRepository
import javax.inject.Inject

class AddGroupLesson @Inject constructor(
    private val repository: TutorBillingRepository
) {
    suspend operator fun invoke(groupId: Long, lesson: Lesson): List<Long> =
        repository.addGroupLesson(groupId, lesson)
}
