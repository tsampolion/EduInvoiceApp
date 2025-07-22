package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.database.LessonWithStudent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonsWithStudents @Inject constructor(
    private val dao: LessonDao
) {
    operator fun invoke(): Flow<List<LessonWithStudent>> = dao.getLessonsWithStudents()
}
