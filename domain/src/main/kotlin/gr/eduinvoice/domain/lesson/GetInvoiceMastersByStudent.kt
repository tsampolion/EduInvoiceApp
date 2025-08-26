package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.model.DomainInvoiceMaster
import gr.eduinvoice.domain.repository.DomainLessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInvoiceMastersByStudent @Inject constructor(
    private val repository: DomainLessonRepository
) {
    operator fun invoke(studentId: Long, userId: Long): Flow<List<DomainInvoiceMaster>> =
        repository.getInvoiceMastersByStudent(studentId, userId)
}
