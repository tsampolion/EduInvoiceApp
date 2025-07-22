package gr.tutorbilling.domain.lesson

import gr.tutorbilling.data.dao.LessonDao
import gr.tutorbilling.data.database.LessonWithStudent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonsWithStudents @Inject constructor(
    private val dao: LessonDao
) {
    operator fun invoke(): Flow<List<LessonWithStudent>> = dao.getLessonsWithStudents()
}
