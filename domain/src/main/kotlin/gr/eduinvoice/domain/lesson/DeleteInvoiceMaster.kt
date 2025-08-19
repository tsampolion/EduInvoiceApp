package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class DeleteInvoiceMaster @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(id: Long, userId: Long) = repository.deleteInvoiceMaster(id, userId)
}


