package gr.tsambala.tutorbilling.domain.lesson

import gr.tsambala.tutorbilling.data.dao.LessonDao
import javax.inject.Inject

class UpdateLessonInvoicedStatus @Inject constructor(
    private val dao: LessonDao
) {
    suspend operator fun invoke(ids: List<Long>, invoiced: Boolean) =
        dao.updateInvoicedStatus(ids, invoiced)
}
