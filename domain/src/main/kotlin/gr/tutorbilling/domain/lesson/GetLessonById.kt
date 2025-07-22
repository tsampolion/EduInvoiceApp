package gr.tutorbilling.domain.lesson

import gr.tutorbilling.data.dao.LessonDao
import gr.tutorbilling.data.model.Lesson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonById @Inject constructor(
    private val dao: LessonDao
) {
    operator fun invoke(id: Long): Flow<Lesson?> = dao.getLessonById(id)
}
