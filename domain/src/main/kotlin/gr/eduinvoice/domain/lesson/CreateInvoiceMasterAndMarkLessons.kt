package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class CreateInvoiceMasterAndMarkLessons @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(
        studentId: Long,
        invoiceNumber: String,
        invoiceDate: String,
        notes: String?,
        lessonIds: List<Long>,
        userId: Long
    ): Long = repository.createInvoiceMasterAndMarkLessons(studentId, invoiceNumber, invoiceDate, notes, lessonIds, userId)
}


