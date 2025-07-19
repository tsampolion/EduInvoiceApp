package gr.tsambala.tutorbilling.domain.lesson

import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.model.Lesson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllLessons @Inject constructor(
    private val dao: LessonDao
) {
    operator fun invoke(): Flow<List<Lesson>> = dao.getAllLessons()
}
