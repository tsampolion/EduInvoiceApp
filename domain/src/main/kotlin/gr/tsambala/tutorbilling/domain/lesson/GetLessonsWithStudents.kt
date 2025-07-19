package gr.tsambala.tutorbilling.domain.lesson

import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.database.LessonWithStudent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonsWithStudents @Inject constructor(
    private val dao: LessonDao
) {
    operator fun invoke(): Flow<List<LessonWithStudent>> = dao.getLessonsWithStudents()
}
