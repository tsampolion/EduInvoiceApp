package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.model.Lesson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllLessons @Inject constructor(
    private val dao: LessonDao
) {
    operator fun invoke(userId: Long = 0): Flow<List<Lesson>> = dao.getAllLessons(userId)
}
