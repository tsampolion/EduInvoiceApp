package gr.tutorbilling.domain.lesson

import gr.tutorbilling.data.model.Lesson
import gr.tutorbilling.data.repository.EduInvoiceRepository
import javax.inject.Inject

class AddGroupLesson @Inject constructor(
    private val repository: EduInvoiceRepository
) {
    suspend operator fun invoke(groupId: Long, lesson: Lesson): List<Long> =
        repository.addGroupLesson(groupId, lesson)
}
