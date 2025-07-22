package gr.tutorbilling.domain.lesson

import gr.tutorbilling.data.model.Lesson
import gr.tutorbilling.data.repository.EduInvoiceRepository
import javax.inject.Inject

class AddLesson @Inject constructor(
    private val repository: EduInvoiceRepository
) {
    suspend operator fun invoke(lesson: Lesson): Long = repository.addLesson(lesson)
}
