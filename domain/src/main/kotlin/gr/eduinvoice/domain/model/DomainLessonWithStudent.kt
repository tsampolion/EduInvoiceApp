package gr.eduinvoice.domain.model

import gr.eduinvoice.domain.model.DomainRateTypes

data class DomainLessonWithStudent(
    val lesson: DomainLesson,
    val student: DomainStudent
) {
    fun calculateFee(): Double {
        return if (student.rateType == DomainRateTypes.PER_LESSON) {
            student.rate
        } else {
            (lesson.durationMinutes / 60.0) * student.rate
        }
    }
}
