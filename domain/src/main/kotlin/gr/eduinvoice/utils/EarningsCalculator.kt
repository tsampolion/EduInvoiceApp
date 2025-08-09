package gr.eduinvoice.utils

import gr.eduinvoice.domain.model.DomainLesson
import gr.eduinvoice.domain.model.DomainStudent
import gr.eduinvoice.domain.utils.DomainFeeCalculator
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Utility object used to calculate a student's earnings.
 */
object EarningsCalculator {
    /**
     * Returns the weekly and monthly earnings for [student] based on [lessons].
     * [lessons] can contain lessons for many students; they will be filtered by the student's id.
     *
     * @return a [Pair] where `first` is the current week's earnings and `second` is the current month's earnings
     */
    fun calculate(student: DomainStudent, lessons: List<DomainLesson>): Pair<Double, Double> {
        val today = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val currentWeek = today.get(weekFields.weekOfWeekBasedYear())
        val currentMonth = today.monthValue
        val currentYear = today.year

        val studentLessons = lessons.filter { it.studentId == student.id }

        val weekEarnings = studentLessons
            .filter { lesson ->
                val lessonDate = LocalDate.parse(lesson.date)
                lessonDate.year == currentYear &&
                        lessonDate.get(weekFields.weekOfWeekBasedYear()) == currentWeek
            }
            .sumOf { DomainFeeCalculator.calculateFee(it, student) }

        val monthEarnings = studentLessons
            .filter { lesson ->
                val lessonDate = LocalDate.parse(lesson.date)
                lessonDate.year == currentYear &&
                        lessonDate.monthValue == currentMonth
            }
            .sumOf { DomainFeeCalculator.calculateFee(it, student) }

        return weekEarnings to monthEarnings
    }
}
