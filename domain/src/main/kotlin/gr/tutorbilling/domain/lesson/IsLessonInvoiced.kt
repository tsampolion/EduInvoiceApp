package gr.tutorbilling.domain.lesson

import gr.tutorbilling.data.dao.LessonDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsLessonInvoiced @Inject constructor(
    private val dao: LessonDao
) {
    operator fun invoke(id: Long): Flow<Boolean?> = dao.isLessonInvoiced(id)
}
