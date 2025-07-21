package gr.tsambala.tutorbilling.domain.lesson

import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.repository.TutorBillingRepository
import javax.inject.Inject

class AddGroupLesson @Inject constructor(
    private val repository: TutorBillingRepository
) {
    suspend operator fun invoke(groupId: Long, lesson: Lesson): List<Long> =
        repository.addGroupLesson(groupId, lesson)
}
