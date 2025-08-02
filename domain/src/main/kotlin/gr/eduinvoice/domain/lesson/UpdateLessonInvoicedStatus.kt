package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.dao.LessonDao
import javax.inject.Inject

class UpdateLessonInvoicedStatus @Inject constructor(
    private val dao: LessonDao
) {
    suspend operator fun invoke(ids: List<Long>, invoiced: Boolean, userId: Long) =
        dao.updateInvoicedStatus(ids, invoiced, userId)
}
