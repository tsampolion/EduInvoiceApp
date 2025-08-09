package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsLessonInvoiced @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(id: Long, userId: Long = 0): Boolean =
        repository.isLessonInvoiced(id, userId)
}
