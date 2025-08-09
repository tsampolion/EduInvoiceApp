package gr.eduinvoice.domain.billing

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent

object BillingService {
    // TODO: replace 0.0 with real calculation using your fields.
    fun fee(lesson: DomainLesson, student: DomainStudent): Double = 0.0
}

// Convenience extension so call sites can do lesson.calculateFeeWith(student)
fun DomainLesson.calculateFeeWith(student: DomainStudent): Double =
    BillingService.fee(this, student)
