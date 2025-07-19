package gr.tsambala.tutorbilling.data.database

import androidx.room.Embedded
import gr.tsambala.tutorbilling.data.model.Lesson
import gr.tsambala.tutorbilling.data.model.Student
import gr.tsambala.tutorbilling.data.model.RateTypes

data class LessonWithStudent(
    @Embedded(prefix = "lesson_") val lesson: Lesson,
    @Embedded(prefix = "student_") val student: Student
) {
    fun calculateFee(): Double {
        return if (student.rateType == RateTypes.PER_LESSON) {
            student.rate
        } else {
            (lesson.durationMinutes / 60.0) * student.rate
        }
    }
}
