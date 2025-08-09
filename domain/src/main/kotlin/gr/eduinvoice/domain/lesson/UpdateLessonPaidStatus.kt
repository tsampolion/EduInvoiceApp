package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class UpdateLessonPaidStatus @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(ids: List<Long>, paid: Boolean, userId: Long) {
        ids.forEach { id ->
            repository.updateLessonPaidStatus(id, paid, userId)
        }
    }
}
