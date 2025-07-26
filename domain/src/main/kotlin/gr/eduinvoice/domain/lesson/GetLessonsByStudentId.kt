package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.model.Lesson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonsByStudentId @Inject constructor(
    private val dao: LessonDao
) {
    operator fun invoke(id: Long, userId: Long = 0): Flow<List<Lesson>> =
        dao.getLessonsByStudentId(id, userId)
}
