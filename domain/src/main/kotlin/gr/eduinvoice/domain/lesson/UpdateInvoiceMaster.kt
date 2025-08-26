package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.model.DomainInvoiceMaster
import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class UpdateInvoiceMaster @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(master: DomainInvoiceMaster, userId: Long) = repository.updateInvoiceMaster(master, userId)
}
