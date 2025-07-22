package gr.eduinvoice.domain.lesson

import gr.eduinvoice.data.dao.LessonDao
import gr.eduinvoice.data.model.Lesson
import javax.inject.Inject

class InsertLesson @Inject constructor(
    private val dao: LessonDao
) {
    suspend operator fun invoke(lesson: Lesson): Long = dao.insert(lesson)
}
