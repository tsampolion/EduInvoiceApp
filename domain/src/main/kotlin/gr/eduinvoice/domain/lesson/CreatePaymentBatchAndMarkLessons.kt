package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class CreatePaymentBatchAndMarkLessons @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(
        studentId: Long?,
        batchDate: String,
        notes: String?,
        lessonIds: List<Long>,
        userId: Long
    ): Long = repository.createPaymentBatchAndMarkLessons(studentId, batchDate, notes, lessonIds, userId)
}
