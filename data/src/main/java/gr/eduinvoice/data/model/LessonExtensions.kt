package gr.eduinvoice.data.model

/**
 * Extension function to calculate lesson fee based on student's rate
 */
internal fun Lesson.calculateFee(student: Student): Double {
    return (durationMinutes / 60.0) * student.rate
}
