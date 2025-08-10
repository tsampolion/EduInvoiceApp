package gr.eduinvoice.domain.billing

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import java.math.BigDecimal
import java.math.RoundingMode

object BillingService {
    fun fee(lesson: DomainLesson, student: DomainStudent): Double {
        val rate = (student.hourlyRate ?: lesson.defaultRate ?: 0.0)
        val minutes = (lesson.durationMinutes ?: 0)
        val total = rate * (minutes / 60.0)
        return BigDecimal(total).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}

fun DomainLesson.calculateFeeWith(student: DomainStudent): Double =
    BillingService.fee(this, student)
