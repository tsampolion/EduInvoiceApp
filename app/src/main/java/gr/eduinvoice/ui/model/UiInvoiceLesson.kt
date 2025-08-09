package gr.eduinvoice.ui.model

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent

data class UiInvoiceLesson(
    val lesson: DomainLesson,
    val student: DomainStudent
) {
    val id: Long get() = lesson.id
    val date: String get() = lesson.date
    val startTime: String get() = lesson.startTime
    val durationMinutes: Int get() = lesson.durationMinutes
    val notes: String? get() = lesson.notes
    val isPaid: Boolean get() = lesson.isPaid
    val isInvoiced: Boolean get() = lesson.isInvoiced
    
    fun calculateFee(): Double {
        return if (student.rateType == "per_lesson") {
            student.rate
        } else {
            (lesson.durationMinutes / 60.0) * student.rate
        }
    }
}
