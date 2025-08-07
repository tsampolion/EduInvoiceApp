package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.database.LessonWithStudent
import javax.inject.Inject

class GetLessonsWithStudentsPaginated @Inject constructor(
    private val dao: LessonDao
) {
    suspend operator fun invoke(userId: Long, limit: Int, offset: Int): List<LessonWithStudent> {
        return dao.getLessonsWithStudentsPaginated(userId, limit, offset)
    }
} 