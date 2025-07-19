package gr.tsambala.tutorbilling.domain.lesson

import gr.tsambala.tutorbilling.data.dao.LessonDao
import gr.tsambala.tutorbilling.data.model.Lesson
import javax.inject.Inject

class UpdateLesson @Inject constructor(
    private val dao: LessonDao
) {
    suspend operator fun invoke(lesson: Lesson) = dao.update(lesson)
}
