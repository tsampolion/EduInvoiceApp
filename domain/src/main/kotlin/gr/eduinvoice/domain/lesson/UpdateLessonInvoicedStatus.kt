package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class UpdateLessonInvoicedStatus @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(ids: List<Long>, invoiced: Boolean, userId: Long) {
        ids.forEach { id ->
            repository.updateLessonInvoicedStatus(id, invoiced, userId)
        }
    }
}
