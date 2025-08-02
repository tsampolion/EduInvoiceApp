package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.dao.LessonDao
import javax.inject.Inject

class UpdateLessonPaidStatus @Inject constructor(
    private val dao: LessonDao
) {
    suspend operator fun invoke(ids: List<Long>, paid: Boolean, userId: Long) =
        dao.updatePaidStatus(ids, paid, userId)
}
