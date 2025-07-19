package gr.tsambala.tutorbilling.domain.lesson

import gr.tsambala.tutorbilling.data.dao.LessonDao
import javax.inject.Inject

class UpdateLessonPaidStatus @Inject constructor(
    private val dao: LessonDao
) {
    suspend operator fun invoke(ids: List<Long>, paid: Boolean) = dao.updatePaidStatus(ids, paid)
}
