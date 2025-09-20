package gr.eduinvoice.domain.analytics

import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class GetEarningsByClass @Inject constructor(
    private val lessonRepository: DomainLessonRepository
) {
    operator fun invoke(startDate: String, endDate: String, userId: Long) =
        lessonRepository.getEarningsByClass(startDate, endDate, userId)
}