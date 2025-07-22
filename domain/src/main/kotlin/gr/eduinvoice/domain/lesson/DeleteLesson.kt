package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.dao.LessonDao
import javax.inject.Inject

class DeleteLesson @Inject constructor(
    private val dao: LessonDao
) {
    suspend operator fun invoke(id: Long) = dao.deleteById(id)
}
