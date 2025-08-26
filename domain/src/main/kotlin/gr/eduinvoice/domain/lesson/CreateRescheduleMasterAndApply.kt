package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class CreateRescheduleMasterAndApply @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(
        lessonIds: List<Long>,
        newDate: String,
        newStartTime: String,
        newDurationMinutes: Int,
        notes: String?,
        userId: Long
    ): Long = repository.createRescheduleMasterAndApply(lessonIds, newDate, newStartTime, newDurationMinutes, notes, userId)
}
