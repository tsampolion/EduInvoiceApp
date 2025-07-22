package gr.tutorbilling.domain.lesson

import gr.tutorbilling.data.dao.LessonDao
import gr.tutorbilling.data.database.LessonWithStudent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonsWithStudentsByStudentAndDateRange @Inject constructor(
    private val dao: LessonDao
) {
    operator fun invoke(studentId: Long, start: String, end: String): Flow<List<LessonWithStudent>> =
        dao.getLessonsWithStudentsByStudentAndDateRange(studentId, start, end)
}
