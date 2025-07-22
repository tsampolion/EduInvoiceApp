package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.model.Lesson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonById @Inject constructor(
    private val dao: LessonDao
) {
    operator fun invoke(id: Long): Flow<Lesson?> = dao.getLessonById(id)
}
