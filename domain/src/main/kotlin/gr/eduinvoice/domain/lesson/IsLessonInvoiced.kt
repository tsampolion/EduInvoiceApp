package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.dao.LessonDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsLessonInvoiced @Inject constructor(
    private val dao: LessonDao
) {
    operator fun invoke(id: Long, userId: Long = 0): Flow<Boolean?> =
        dao.isLessonInvoiced(id, userId)
}
