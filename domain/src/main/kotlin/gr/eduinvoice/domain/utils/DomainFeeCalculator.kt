package gr.eduinvoice.domain.utils

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.model.DomainRateTypes

/**
 * Domain-level fee calculation utility
 */
object DomainFeeCalculator {
    /**
     * Calculate lesson fee based on student's rate and rate type
     */
    fun calculateFee(lesson: DomainLesson, student: DomainStudent): Double {
        return if (student.rateType == DomainRateTypes.PER_LESSON) {
            student.rate
        } else {
            (lesson.durationMinutes / 60.0) * student.rate
        }
    }
}
