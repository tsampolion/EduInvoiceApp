package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.model.DomainInvoiceMaster
import gr.eduinvoice.domain.repository.DomainLessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInvoiceMasterById @Inject constructor(
    private val repository: DomainLessonRepository
) {
    operator fun invoke(id: Long, userId: Long): Flow<DomainInvoiceMaster?> =
        repository.getInvoiceMasterById(id, userId)
}
