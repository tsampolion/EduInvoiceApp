package gr.eduinvoice.domain.lesson

import gr.eduinvoice.domain.repository.DomainLessonRepository
import javax.inject.Inject

class DeleteGroupLesson @Inject constructor(
    private val repository: DomainLessonRepository
) {
    suspend operator fun invoke(masterId: Long, userId: Long = 0) =
        repository.deleteGroupLesson(masterId, userId)
}
