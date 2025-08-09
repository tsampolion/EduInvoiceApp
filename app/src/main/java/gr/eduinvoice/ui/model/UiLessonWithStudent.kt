package gr.eduinvoice.ui.model

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent

data class UiLessonWithStudent(
    val lesson: DomainLesson,
    val student: DomainStudent
) {
    fun calculateFee(): Double {
        return if (student.rateType == "per_lesson") {
            student.rate
        } else {
            (lesson.durationMinutes / 60.0) * student.rate
        }
    }
}
